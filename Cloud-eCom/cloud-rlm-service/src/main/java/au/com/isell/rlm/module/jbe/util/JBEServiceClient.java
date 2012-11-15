package au.com.isell.rlm.module.jbe.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import au.com.isell.common.util.NetworkUtils;
import au.com.isell.rlm.module.jbe.common.DataElement;
import au.com.isell.rlm.module.jbe.common.Header;
import au.com.isell.rlm.module.jbe.common.Message;
import au.com.isell.rlm.module.jbe.common.MessageParser;
import au.com.isell.rlm.module.jbe.common.MessageWriter;
import au.com.isell.rlm.module.jbe.common.Request;
import au.com.isell.rlm.module.jbe.common.Response;

@Service
public class JBEServiceClient {
	private static final String UTF8 = "UTF-8";
	private static final String HMACSHA1 = "HmacSHA1";

	private String remoteUrl;
	private String username;
	private String password;

	@Autowired
	public JBEServiceClient(@Value("${isell.support.url}") String remoteUrl, 
			@Value("${isell.support.username}") String username, @Value("${isell.support.password}") String password) {
		this.remoteUrl = remoteUrl;
		this.username = username;
		this.password = password;
	}
	
	public Response invoke(String functionName, Request req, int serialNo, String email) {
		long expire = System.currentTimeMillis()+5*60*1000;
		if (password==null || password.length() == 0) password = "NULL";
		String signature = calcSignature(functionName, expire, username, password, serialNo, email);
		String url = remoteUrl + '/' + functionName.replace('.', '/');
		Properties params= new Properties();
		params.setProperty("expire", String.valueOf(expire));
		params.setProperty("signature", signature);
		params.setProperty("username", username);
		params.setProperty("serialNo", String.valueOf(serialNo));
		params.setProperty("email", email);
		Message responseMessage=null;
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			MessageWriter writer = new MessageWriter(new PrintStream(bout));
			Message message = createRequestMessage(functionName, req);
			writer.writeMessage(message);
			bout.close();
			params.setProperty("data", bout.toString());
			InputStream in = NetworkUtils.makeHTTPPostRequest(url, params, null);
			responseMessage = MessageParser.parseMessage(in);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (SAXException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
        Response response = new Response();
        response.setChildren(responseMessage.getBody().getChildren());
        response.setErrorcode(0);
        response.setMessage("");
		return response;
	}
	
	private static Message createRequestMessage(String functionName, Request request){
        Message requestMessage = new Message(request);
        Header header = requestMessage.getHeader();

        header.addChild(Header.REQ_NAME, functionName);
        header.addChild(Header.REQ_USERNAME, "iSell");
        header.addChild(Header.REQ_PASSWORD, "iSell");

        return requestMessage;
    }

	private String calcSignature(String function, long expire, String username,
			String passwd, int serialNo, String email) {
    	try {
			byte[] dataBytes = ("function:"+function+"\nserialNo:"+serialNo+"\nemail:"+email+"\nexpire:"+expire+"\nusername:"+username.toLowerCase()).getBytes(UTF8);
			byte[] secretBytes = ConvertPass.getEncryptString(username.toLowerCase(), passwd).getBytes(UTF8);
			SecretKeySpec signingKey = new SecretKeySpec(secretBytes, HMACSHA1);
	
			Mac mac = Mac.getInstance(HMACSHA1);
			mac.init(signingKey);
			byte[] signature = mac.doFinal(dataBytes);

			return new String(Base64.encodeBase64(signature, false), UTF8);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> createMap(DataElement element) {

        Vector<DataElement> children = element.getChildren();
        if ((children == null) || (children.size() == 0)) {
            Map<String, Object> map = new HashMap<String, Object>(0);
            return map;
        }
        Map<String, Object> map = new HashMap<String, Object>(children.size());
        for (DataElement child : children) {
            Object value;
            if (child.hasChildren()) {
                value = createMap(child);
            } else {
                value = child.getValue();
            }
            if (child.isRepeatable()) {
                List<Object> list = (List<Object>) map.get(child.getName());
                if (list == null) {
                    list = new ArrayList<Object>();
                }
                list.add(value);
                value = list;
            }
            map.put(child.getName(), value);
        }
        return map;
    }
	
	public static void main(String[] args) {
		JBEServiceClient client = new JBEServiceClient("http://46.51.241.227:18080/support/ws", "test", "test");
		Request req = new Request();
		req.addChild("webAccountID", "1");
		req.addChild("aliasAccountID", "2");
		req.addChild("omitPermissionChecking", "1");
		Response resp = client.invoke("job.list", req, 6100007, "ru4@ru.com");
		System.out.println(resp.toString());
		createMap(resp);
		
		req = new Request();
		req.addChild("allStatus", "1");
		resp =client.invoke("gen.FindStageForJob", req, 6100007, "ru4@ru.com");
		System.out.println(resp.toString());
		
		createMap(resp);
		
		
		
	}
}
