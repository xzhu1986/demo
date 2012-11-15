package au.com.isell.rlm.module.jbe.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.isell.rlm.common.utils.GlobalAttrManager;
import au.com.isell.rlm.module.jbe.common.Request;
import au.com.isell.rlm.module.jbe.common.Response;
import au.com.isell.rlm.module.jbe.domain.CustInfo;
import au.com.isell.rlm.module.jbe.service.JbeService;
import au.com.isell.rlm.module.jbe.util.JBEServiceClient;
import au.com.isell.rlm.module.reseller.domain.ResellerUser;

@Service
public class JbeServiceImpl implements JbeService{
	@Autowired
	private JBEServiceClient jbeServiceClient;
	
	@Override
	public CustInfo getResellerUserAccountInfo(){
		CustInfo info=new CustInfo();
		ResellerUser user= (ResellerUser)GlobalAttrManager.getCurrentUser();
		int serialNo=user.getSerialNo();
		String email=user.getEmail();
		
		Request req = new Request();
		
		req.addChild("serialNo", serialNo);
		req.addChild("email", email);
		
		Response resp = jbeServiceClient.invoke("customer.wsLogin", req, serialNo, email);
		
		info.setAccountId(resp.getValueAsInt("accountId",-1));
		info.setWebAccountId(resp.getValueAsInt("webAccountId",-1));
		info.setContactId(resp.getValueAsInt("contactId",-1));
		return info;
	}
}
