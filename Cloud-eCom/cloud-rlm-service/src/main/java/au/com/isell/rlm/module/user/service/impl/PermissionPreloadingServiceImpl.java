package au.com.isell.rlm.module.user.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import au.com.isell.common.util.ClassResourceFilter;
import au.com.isell.common.util.TextFormat;
import au.com.isell.common.xml.SerializeUtils;
import au.com.isell.rlm.common.utils.DataLoadUtils;
import au.com.isell.rlm.module.user.domain.Permission;
import au.com.isell.rlm.module.user.service.PermissionPreloadingService;
import au.com.isell.rlm.module.user.vo.PermissionGroup;
import au.com.isell.rlm.module.user.vo.PermissionPackage;

import com.thoughtworks.xstream.XStream;

/**
 * @author frankw 27/03/2012
 */
@Service
public class PermissionPreloadingServiceImpl implements PermissionPreloadingService {
	private static Logger logger = LoggerFactory.getLogger(PermissionPreloadingService.class);

	public Set<PermissionPackage> agentPermissionPackages = new TreeSet<PermissionPackage>();
	public Set<PermissionPackage> resellerPermissionPackages = new TreeSet<PermissionPackage>();
	public Map<String, Permission> mapping = new HashMap<String, Permission>();

	private ApplicationContext context = null;
	private XStream xStream = SerializeUtils.getDefaultXStream();

	@Override
	public void preload(ApplicationContext applicationContext) {
		context = applicationContext;
		logger.info("loading permission definitions from xml ...");

		Set<PermissionPackage> tempAgentPackages = new HashSet<PermissionPackage>();
		Set<PermissionPackage> tempResellerPackages = new HashSet<PermissionPackage>();

		tempAgentPackages.addAll(agentPermissionPackages);
		tempResellerPackages.addAll(resellerPermissionPackages);
		try {
			for (Resource resource : ClassResourceFilter.doFilter("permissions/*/*.xml")) {
				if (resource.getURL().getPath().contains("permissions/agent/")) {
					loadSinglePermDef(applicationContext, tempAgentPackages, mapping, resource);
				} else if (resource.getURL().getPath().contains("permissions/reseller/")) {
					loadSinglePermDef(applicationContext, tempResellerPackages, mapping, resource);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		agentPermissionPackages.clear();
		agentPermissionPackages.addAll(tempAgentPackages);

		resellerPermissionPackages.clear();
		resellerPermissionPackages.addAll(tempResellerPackages);

		logger.info("finish loadding permission definitions,total elements:{},total agent package:{},total reseller package:{}", new Object[] { mapping.size(), agentPermissionPackages.size(), resellerPermissionPackages.size() });
	}

	private void loadSinglePermDef(ApplicationContext applicationContext, Set<PermissionPackage> tempPermissionPackages, Map<String, Permission> tempMapping, Resource resource) {
		InputStream in = null;
		PermissionPackage bean;
		try {
			in = resource.getInputStream();
			bean = (PermissionPackage) xStream.fromXML(in);
			// dynamic group handle

			List<PermissionGroup> finalGroups = new ArrayList<PermissionGroup>();
			for (PermissionGroup group : bean.getPermissionGroups()) {
				if (!group.isDynamic()) {
					finalGroups.add(group);
					continue;
				}

				Assert.hasText(group.getDataService(), "data service can not be null in dynamic group");
				Assert.hasText(group.getDataMethod(), "data method can not be null in dynamic group");

				createDynaGroups(finalGroups, group, applicationContext);
			}

			bean.setPermDefineFileName(resource.getFilename());
			bean.setPermissionGroups(finalGroups);

			if (tempPermissionPackages.contains(bean))
				tempPermissionPackages.remove(bean);// hash code!
			tempPermissionPackages.add(bean);

			addMapping(tempMapping, finalGroups);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			try {
				in.close();
			} catch (Exception e) {
			}
		}
	}

	private void addMapping(Map<String, Permission> tempMapping, List<PermissionGroup> finalGroups) {
		for (PermissionGroup group : finalGroups) {
			for (Permission permission : group.getPermissions()) {
				tempMapping.put(permission.getKey(), permission);
			}
		}
	}

	private void createDynaGroups(List<PermissionGroup> finalGroups, PermissionGroup group, ApplicationContext applicationContext) throws Exception {
		List<Object> dynaSource = (List) DataLoadUtils.loadServiceData4Spring(applicationContext, group.getDataService(), group.getDataMethod(), group.getDataParam());
		if (dynaSource != null) {
			for (Object obj : dynaSource) {
				Map paramMap = PropertyUtils.describe(obj);

				PermissionGroup newGroup = new PermissionGroup();
				newGroup.setDisplay(getFormatedVal(group.getDisplay(), paramMap));
				List<Permission> permissions = new ArrayList<Permission>();
				for (Permission itemPerm : group.getPermissions()) {
					Permission permission = (Permission) BeanUtils.cloneBean(itemPerm);
					permission.setKey(getFormatedVal(permission.getKey(), paramMap));
					permission.setDisplay(getFormatedVal(permission.getDisplay(), paramMap));
					permissions.add(permission);
				}
				newGroup.setPermissions(permissions);
				finalGroups.add(newGroup);
			}

		}
	}

	private String getFormatedVal(String rawVal, Map paramMap) {
		String temp = rawVal;
		if (TextFormat.isFormatedStr(temp)) {
			temp = TextFormat.format(temp, paramMap);
		}
		return temp;
	}

	@Override
	public Permission getPermission(String key) {
		return mapping.get(key);
	}

	/** freemarker use */
	@Override
	public Set<PermissionPackage> getAgentPermissionPackages() {
		return agentPermissionPackages;
	}

	@Override
	public Set<PermissionPackage> getResellerPermissionPackages() {
		return resellerPermissionPackages;
	}

	@Override
	public Iterable<String> getPermissions() {
		return mapping.keySet();
	}

	@Override
	public synchronized void updatePermissionCache(Resource permissionDefineFile) {
		logger.info("update permission definitions");
		Set<PermissionPackage> tempPermissionPackages = new HashSet<PermissionPackage>();
		try {
			if (permissionDefineFile.getURL().getPath().contains("permissions/agent/")) {
				updatePermission(permissionDefineFile, tempPermissionPackages, agentPermissionPackages);
			} else if (permissionDefineFile.getURL().getPath().contains("permissions/reseller/")) {
				updatePermission(permissionDefineFile, tempPermissionPackages, resellerPermissionPackages);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		logger.info("finish updating reseller permission definitions,total elements:{},total agent package:{},total reseller package:{}", new Object[] { mapping.size(), agentPermissionPackages.size(), resellerPermissionPackages.size() });

	}

	private void updatePermission(Resource permissionDefineFile, Set<PermissionPackage> tempPermissionPackages, Set<PermissionPackage> permissionPackages) {
		tempPermissionPackages.addAll(permissionPackages);
		loadSinglePermDef(context, tempPermissionPackages, mapping, permissionDefineFile);
		permissionPackages.clear();
		permissionPackages.addAll(tempPermissionPackages);
	}

}
