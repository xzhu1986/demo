package au.com.isell.rlm.module.jbe.service.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.utils.GlobalAttrManager;
import au.com.isell.rlm.common.utils.GlobalResellerAttrManager;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.address.domain.GeneralAddress;
import au.com.isell.rlm.module.address.service.AddressService;
import au.com.isell.rlm.module.jbe.common.Request;
import au.com.isell.rlm.module.jbe.domain.CustInfo;
import au.com.isell.rlm.module.jbe.domain.JobStatus;
import au.com.isell.rlm.module.jbe.service.JobService;
import au.com.isell.rlm.module.jbe.util.JBEServiceClient;
import au.com.isell.rlm.module.jbe.vo.JobAddBean;
import au.com.isell.rlm.module.jbe.vo.JobSearchFilter;
import au.com.isell.rlm.module.jbe.vo.JobSearchVals;
import au.com.isell.rlm.module.reseller.domain.Reseller;
import au.com.isell.rlm.module.reseller.domain.ResellerUser;
import au.com.isell.rlm.module.reseller.service.ResellerService;
import au.com.isell.rlm.module.user.constant.UserStatus;
import au.com.isell.rlm.module.user.service.UserService;

/**
 * @author frankw 26/06/2012
 */
@Service
public class JobServiceImpl implements JobService{
	@Autowired
	private JBEServiceClient jbeServiceClient;
	@Autowired
	private ResellerService resellerService;
	@Autowired
	private AddressService addressService;
	@Autowired
	private UserService userService;
	
	@Override
	public Pair<Integer,Collection> queryJobs(String filter,JobSearchVals searchVals,int pageSize,int pageNo){
		Request req = new Request();
		CustInfo accountInfo = GlobalResellerAttrManager.getAccountInfo();
		ResellerUser user= (ResellerUser)GlobalAttrManager.getCurrentUser();
		req.addChild("webAccountID", accountInfo.getWebAccountId());
		req.addChild("aliasAccountID", accountInfo.getAccountId());
		req.addChild("sortOrder", "3");
		
		JobSearchFilter jobSearchFilter=(JobSearchFilter)BeanUtils.getEnum(JobSearchFilter.class, filter);
		int status = jobSearchFilter.getFilterStatus()!=null?jobSearchFilter.getFilterStatus().getCode():-1;
		if(StringUtils.isNotEmpty(searchVals.getStatus())){
			status=Integer.valueOf(searchVals.getStatus());
		}
		if(status!=-1){
			req.addChild("jobStatus", status);
		}
		
		
		req.addChild("omitPermissionChecking", "1");
		
		req.addChild("maxResults", pageSize);
		req.addChild("pageNo", pageNo);
		
		Map<String, Object> resp =JBEServiceClient.createMap(jbeServiceClient.invoke("job.list", req, user.getSerialNo(),user.getEmail()));
		
		return new Pair(Integer.valueOf((String)resp.get("pages")),resp.get("job"));
	}
	
	@Override
	public int getJobCountByStatus(JobStatus jobStatus){
		Request req = new Request();
		CustInfo accountInfo = GlobalResellerAttrManager.getAccountInfo();
		ResellerUser user= (ResellerUser)GlobalAttrManager.getCurrentUser();
		req.addChild("webAccountID", accountInfo.getWebAccountId());
		req.addChild("aliasAccountID", accountInfo.getAccountId());
		req.addChild("omitPermissionChecking", "1");
		req.addChild("jobStatus", jobStatus.ordinal()+1);//1 pending
		req.addChild("maxResults", 1);
		req.addChild("pageNo", 1);
		Map<String, Object> resp =JBEServiceClient.createMap(jbeServiceClient.invoke("job.list", req, user.getSerialNo(),user.getEmail()));
		if(resp.get("total")==null) return 0;
		return Integer.parseInt(resp.get("total").toString());
	}
	
	@Override
	public Map<String, Object> getDetail(String jobId){
		Request req = new Request();
		CustInfo accountInfo = GlobalResellerAttrManager.getAccountInfo();
		ResellerUser user= (ResellerUser)GlobalAttrManager.getCurrentUser();
		req.addChild("webAccountID", accountInfo.getWebAccountId());
		req.addChild("jobID", jobId);
		req.addChild("omitPermissionChecking", "1");
		return JBEServiceClient.createMap(jbeServiceClient.invoke("job.detail", req,  user.getSerialNo(),user.getEmail()));
	}
	@Override
	public Collection getTasks(String jobId){
		Request req = new Request();
		CustInfo accountInfo = GlobalResellerAttrManager.getAccountInfo();
		ResellerUser user= (ResellerUser)GlobalAttrManager.getCurrentUser();
		req.addChild("webAccountID", accountInfo.getWebAccountId());
		req.addChild("jobID", jobId);
		req.addChild("omitPermissionChecking", "1");
		Map<String, Object> resp =JBEServiceClient.createMap(jbeServiceClient.invoke("task.list", req,  user.getSerialNo(),user.getEmail()));
		return (Collection) resp.get("task");
	}
	@Override
	public Collection getNotes(String jobId){
		Request req = new Request();
		CustInfo accountInfo = GlobalResellerAttrManager.getAccountInfo();
		ResellerUser user= (ResellerUser)GlobalAttrManager.getCurrentUser();
		req.addChild("webAccountID", accountInfo.getWebAccountId());
		req.addChild("jobID", jobId);
		req.addChild("omitPermissionChecking", "1");
		Map<String, Object> resp =JBEServiceClient.createMap(jbeServiceClient.invoke("jobNote.list", req,  user.getSerialNo(),user.getEmail()));
		return (Collection) resp.get("jobnote");
	}
	
	@Override
	public Collection getJobTypes(){
		Request req = new Request();
		ResellerUser user= (ResellerUser)GlobalAttrManager.getCurrentUser();
		
		req.addChild("typeOfType", "140");
		
		Map<String, Object> resp =JBEServiceClient.createMap(jbeServiceClient.invoke("gen.ListTypes", req, user.getSerialNo(), user.getEmail()));
		
		return (Collection)resp.get("type");
	}
	
	@Override
	public String createJob(JobAddBean addBean) {
		Request req = new Request();
		ResellerUser user= (ResellerUser)GlobalAttrManager.getCurrentUser();
		Reseller reseller= resellerService.getReseller(user.getSerialNo());
		AddressItem country=addressService.getAddressItem(reseller.getCountry());
		
		CustInfo accountInfo = GlobalResellerAttrManager.getAccountInfo();
		req.addChild("webAccountID", accountInfo.getWebAccountId());
		req.addChild("aliasAccountID", accountInfo.getAccountId());
		
		req.addChild("jobCountry",country.getName());
		req.addChild("jobPostCode", country.getPostcodeBind());
		GeneralAddress generalAddress = (GeneralAddress)reseller.getAddress();
		req.addChild("jobAddress", generalAddress.getAddress1()+","+generalAddress.getAddress2()+","+generalAddress.getAddress3());
		AddressItem region = addressService.getAddressItem(generalAddress.getRegion());
		req.addChild("jobState", region.getShortName());
		req.addChild("jobCity", generalAddress.getCity());
		req.addChild("aliasAccountID", accountInfo.getAccountId());
		
		addBean.setJobEmail(user.getEmail());
		
		addBean.addParams(req);
		
		Map<String, Object> resp =JBEServiceClient.createMap(jbeServiceClient.invoke("job.create", req, user.getSerialNo(), user.getEmail()));
		
		if(StringUtils.isNotBlank((String)resp.get("errorDescription"))){
			throw new RuntimeException((String)resp.get("errorDescription"));
		}
		
		return (String)resp.get("jobId");
	
	}
	@Override
	public Collection getJobStages() {
		Request req = new Request();
		ResellerUser user = (ResellerUser) GlobalAttrManager.getCurrentUser();

		req.addChild("allStatus", "1");

		Map<String, Object> resp = JBEServiceClient.createMap(jbeServiceClient.invoke("gen.FindStageForJob", req, user.getSerialNo(), user.getEmail()));
		Collection collection = (Collection) resp.get("Stage");
		if (collection != null) {
			for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
				Map m = (Map) iterator.next();
				if("1".equals(m.get("isDisabled").toString())){
					collection.remove(m);
				}
			}
		}
		return collection;
	}
	
	
	
	@Override
	public void createNote(String jobId,String note) {
		Assert.hasText(note,"note should note be blank");
		Request req = new Request();
		CustInfo accountInfo = GlobalResellerAttrManager.getAccountInfo();
		ResellerUser user= (ResellerUser)GlobalAttrManager.getCurrentUser();
		
		req.addChild("noteDetails", note);
		req.addChild("jobID", jobId);
		req.addChild("webAccountID", accountInfo.getWebAccountId());
		req.addChild("omitPermissionChecking", "1");
		
		jbeServiceClient.invoke("jobNote.create", req,  user.getSerialNo(),user.getEmail());
	}
	
	@Override
	public Map<String, Object> createOrUpdateCustomer(ResellerUser formUser) {
		Request req = new Request();
		CustInfo accountInfo = GlobalResellerAttrManager.getAccountInfo();
		ResellerUser user= (ResellerUser)GlobalAttrManager.getCurrentUser();
		Reseller reseller = resellerService.getReseller(formUser.getSerialNo());
		AddressItem addressItem = addressService.getAddressItem(reseller.getCountry());
		String phonePrefix =(addressItem != null ? "+"+addressItem.getPhoneAreaCodeBind() : "");
		
		req.addChild("email", formUser.getEmail());
		req.addChild("password", "password");
		req.addChild("firstName", formUser.getFirstName());
		req.addChild("lastName", formUser.getLastName());
		
		req.addChild("phone", phonePrefix+formUser.getPhone());
		req.addChild("jobTitle", formUser.getJobPosition());
		req.addChild("mobile", phonePrefix+formUser.getMobile());
		req.addChild("type","5");
		if(formUser.getStatus().equals(UserStatus.ACTIVE)){
			req.addChild("status","1");
		}else{
			req.addChild("status","2");
		}
		
		req.addChild("accountID", accountInfo.getAccountId());
		req.addChild("webAccountID", accountInfo.getWebAccountId());
		req.addChild("omitPermissionChecking", "1");
		if(formUser.getUserId()==null){
			return JBEServiceClient.createMap(jbeServiceClient.invoke("customer.create", req,  user.getSerialNo(),user.getEmail()));
		}else{
			ResellerUser dbUser = userService.getUser(ResellerUser.class, formUser.getUserId());
			Request wsLoginReq = new Request();
			wsLoginReq.addChild("email", dbUser.getEmail());
			wsLoginReq.addChild("serialNo",dbUser.getSerialNo());
			wsLoginReq.addChild("noLogin", "1");
			Map<String, Object> wsLoginResp =JBEServiceClient.createMap(jbeServiceClient.invoke("customer.wsLogin", wsLoginReq,  user.getSerialNo(),user.getEmail()));
			if(!"0".equals(wsLoginResp.get("responseCode"))) return wsLoginResp;
			
			if(wsLoginResp.get("contactId")!=null){
				req.addChild("aliasID",wsLoginResp.get("contactId"));
				req.addChild("ignoreAddressCheck","1");
				return JBEServiceClient.createMap(jbeServiceClient.invoke("customer.update", req,  user.getSerialNo(),user.getEmail()));
			}else{
				return JBEServiceClient.createMap(jbeServiceClient.invoke("customer.create", req,  user.getSerialNo(),user.getEmail()));
			}
		}
	}
}
