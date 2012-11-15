package au.com.isell.rlm.common.utils;

import au.com.isell.rlm.module.supplier.domain.SupplierUser;
import au.com.isell.rlm.module.user.domain.User;
import au.com.isell.rlm.module.user.vo.ClientInfo;

public class GlobalAttrManager {
	private static ThreadLocal<ClientInfo> clientInfoHoder = new ThreadLocal<ClientInfo>();
	private static ThreadLocal<User> users = new ThreadLocal<User>();
	private static ClientInfo clientInfo = new ClientInfo().setIpAddress("127.0.0.1");
	private static User systemUser;

	public static void setSystemUser(User systemUser) {
		GlobalAttrManager.systemUser = systemUser;
	}

	public static void setClientInfo(ClientInfo clientInfo) {
		clientInfoHoder.set(clientInfo);
	}
	
	public static ClientInfo getClientInfo(){
		if(clientInfoHoder.get()==null){
			return clientInfo;
		}
		return clientInfoHoder.get();
	}

	public static User getCurrentUser() {
		if(clientInfoHoder.get()==null){
			return systemUser;
		}
		return users.get();
	}
	
	public static void setCurrentUser(User user) {
		users.set(user);
	}
	
	public static Integer getCurrentSupplierId() {
		SupplierUser sUser = (SupplierUser)getCurrentUser();
		return sUser.getSupplierId();
	}
}
