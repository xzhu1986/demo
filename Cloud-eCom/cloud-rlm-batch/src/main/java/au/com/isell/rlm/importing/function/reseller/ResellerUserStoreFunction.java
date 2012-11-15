package au.com.isell.rlm.importing.function.reseller;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import au.com.isell.common.util.SecurityUtils;
import au.com.isell.rlm.importing.constant.Constants;
import au.com.isell.rlm.importing.constant.reseller.ResellerColumns;
import au.com.isell.rlm.importing.dao.StoreHelper;
import au.com.isell.rlm.importing.utils.SpringUtils;
import au.com.isell.rlm.module.reseller.domain.ResellerUser;
import au.com.isell.rlm.module.user.constant.UserStatus;
import au.com.isell.rlm.module.user.service.PermissionService;
import au.com.isell.rlm.module.user.service.impl.PermissionServiceImpl;
import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntry;

public class ResellerUserStoreFunction extends BaseOperation<ResellerUserStoreFunction.Context> implements Function<ResellerUserStoreFunction.Context> {
	
	private static final long serialVersionUID = 1089526440864934362L;
	
	protected static class Context {
		List<ResellerUser> resellerUsers;
		Set<String> rpJobPermissions;
		public Context(Set<String> rpJobPerms) {
			resellerUsers = new ArrayList<ResellerUser>(Constants.MAX_OBJECTS_PER_ADD);
			rpJobPermissions = Collections.unmodifiableSet(rpJobPerms);
		}
		public Context reset() {
			resellerUsers.clear();
			return this;
		}
	}
	
	@ConstructorProperties({ "fieldDeclaration" })
	public ResellerUserStoreFunction(Fields fieldDeclaration) {
		super(fieldDeclaration);

	}

	@Override
	public void prepare(FlowProcess flowProcess, OperationCall<ResellerUserStoreFunction.Context> operationCall) {
		Context context = operationCall.getContext();
		if (context != null){
			context.reset();
		}else{
			PermissionService permissionService = SpringUtils.getBean("permissionServiceImpl", PermissionServiceImpl.class);
			operationCall.setContext(new Context(permissionService.getResellerPortalJobPermissions()));
		}
	}
	
	@Override
	public void operate(FlowProcess flowProcess, FunctionCall<ResellerUserStoreFunction.Context> functionCall) {
		TupleEntry arguments = functionCall.getArguments();
		functionCall.getOutputCollector().add(arguments);
		
		String resellerUserWebUserName = arguments.getString(ResellerColumns.ResellerUserWebUserName);
		String resellerUserFirstName = arguments.getString(ResellerColumns.ResellerUserFirstName);
		String resellerUserLastName = arguments.getString(ResellerColumns.ResellerUserLastName);
		String resellerUserEmail = arguments.getString(ResellerColumns.ResellerUserEmail);
		String resellerUserAccountNumber = arguments.getString(ResellerColumns.ResellerUserAccountNumber);
		UUID resellerUserID = UUID.fromString(arguments.getString(ResellerColumns.ResellerUserID));
		String resellerUserPassword = arguments.getString(ResellerColumns.ResellerUserPassword);
		String resellerUserJobTitle = arguments.getString(ResellerColumns.ResellerUserJobTitle);
		Context context = functionCall.getContext();
		ResellerUser resellerUser = makeResellerUser(resellerUserAccountNumber,resellerUserID,resellerUserFirstName,resellerUserLastName,resellerUserJobTitle,resellerUserEmail,resellerUserPassword,context.rpJobPermissions);
		context.resellerUsers.add(resellerUser);
		
		flushInputObjects(context,false);;
	}
	
	@Override
	public void cleanup(FlowProcess flowProcess, OperationCall<ResellerUserStoreFunction.Context> operationCall) {
		Context context = operationCall.getContext();
		flushInputObjects(context,true);
	}
	


	private ResellerUser makeResellerUser(String resellerUserAccountNumber,UUID resellerUserID,String resellerUserFirstName,
			String resellerUserLastName,String resellerUserJobTitle,String resellerUserEmail,String resellerUserPassword,Set<String> rpJobPermissions) {
		ResellerUser user = new ResellerUser();
		user.setFirstName(resellerUserFirstName);
		user.setLastName(resellerUserLastName);
		user.setEmail(resellerUserEmail);
		user.setStatus(UserStatus.ACTIVE);
		user.setLoginFailureCount(0);
		user.setUserId(resellerUserID);
		user.setSerialNo(Integer.parseInt(resellerUserAccountNumber));
		user.setPrimary(false);
		user.setJobPosition(resellerUserJobTitle);
		user.setPermissions(rpJobPermissions);
		user.setUsername(user.getToken());
		user.setPassword(SecurityUtils.digestPassword(user.getToken(), resellerUserPassword));
		return user;
	}


	private void flushInputObjects(Context context, boolean force) {
		if ((force && (context.resellerUsers.size() > 0)) || (context.resellerUsers.size() >= Constants.MAX_OBJECTS_PER_ADD)) {
			StoreHelper.save(context.resellerUsers.toArray(new ResellerUser[context.resellerUsers.size()]));
			context.reset();
		}
	}
}
