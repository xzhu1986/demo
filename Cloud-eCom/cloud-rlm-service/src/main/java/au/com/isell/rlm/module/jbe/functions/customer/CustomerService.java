package au.com.isell.rlm.module.jbe.functions.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.isell.rlm.module.jbe.common.JBEException;
import au.com.isell.rlm.module.jbe.common.Request;
import au.com.isell.rlm.module.jbe.common.Response;
import au.com.isell.rlm.module.jbe.common.XMLException;
import au.com.isell.rlm.module.jbe.domain.CustInfo;
import au.com.isell.rlm.module.jbe.util.JBEServiceClient;
import au.com.isell.rlm.module.reseller.domain.ResellerUser;

@Service
public class CustomerService {
	
	@Autowired
	private JBEServiceClient client;
	
	public CustInfo getCustInfo(ResellerUser user) throws JBEException {
		Request req = new Request();
		req.addChild("serialNo", user.getSerialNo());
		req.addChild("email", user.getEmail());
		try {
			Response resp = client.invoke("customer.wsLogin", req, user.getSerialNo(), user.getEmail());
			CustInfo info = new CustInfo();
			info.setAccountId(resp.getValueAsInt("accountId", 0));
			info.setContactId(resp.getValueAsInt("contactId", 0));
			info.setWebAccountId(resp.getValueAsInt("webAccountId", 0));
			return info;
		}catch (XMLException e) {
			throw new JBEException(e);
		}
		
	}
}
