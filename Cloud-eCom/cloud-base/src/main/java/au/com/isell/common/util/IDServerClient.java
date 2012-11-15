package au.com.isell.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.jackson.map.ObjectMapper;

public class IDServerClient {
	private static String serverGetIntAddr;
	private static String serverSetIntAddr;
	private static String serverGetRegKeyAddr;
	private static String serverSetRegKeyAddr;
	private static ObjectMapper mapper = new ObjectMapper();
	static {
		try {
			Properties bundle = new Properties();
			bundle.load(IDServerClient.class.getResourceAsStream("/settings.properties"));
			serverGetIntAddr = bundle.getProperty("id.server.addr.get");
			serverSetIntAddr = bundle.getProperty("id.server.addr.set");
			serverGetRegKeyAddr = bundle.getProperty("regkey.server.addr.get");
			serverSetRegKeyAddr = bundle.getProperty("regkey.server.addr.set");
		} catch (Exception ex) {
		}
	}
	
	@SuppressWarnings("unchecked")
	public static int[] getIntId(String type, int count) {
		Map<String, Object> var = new HashMap<String, Object>();
		var.put("type", type);
		String addr = TextFormat.format(serverGetIntAddr, var);
		Properties param = new Properties();
		param.setProperty("count", count < 1 ? "1" : String.valueOf(count));
		InputStream in = null;
		try {
			in = NetworkUtils.makeHTTPGetRequest(addr, param, null);
			Map<String, Object> result = mapper.readValue(in, Map.class);
			List<Integer> ids = (List<Integer>)result.get("id");
			int[] idArray = new int[ids.size()];
			for (int i = 0; i < idArray.length; i++) {
				idArray[i] = ids.get(i);
			}
			return idArray;
		} catch(IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) {
			}
		}
	}
	
	public static void setIntId(String type, int number) {
		Map<String, Object> var = new HashMap<String, Object>();
		var.put("type", type);
		String addr = TextFormat.format(serverSetIntAddr, var);
		Properties param = new Properties();
		param.setProperty("number", number < 0 ? "0" : String.valueOf(number));
		InputStream in = null;
		try {
			in = NetworkUtils.makeHTTPGetRequest(addr, param, null);
			for (int a = in.read(); a != -1; a=in.read());
		} catch(IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) {
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public static String getRegKey(String type, int serialNo) {
		Map<String, Object> var = new HashMap<String, Object>();
		var.put("type", type);
		String addr = TextFormat.format(serverGetRegKeyAddr, var);
		Properties param = new Properties();
		param.setProperty("serialNo", String.valueOf(serialNo));
		InputStream in = null;
		try {
			in = NetworkUtils.makeHTTPGetRequest(addr, param, null);
			Map<String, Object> result = mapper.readValue(in, Map.class);
			return (String)result.get("key");
		} catch(IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) {
			}
		}
	}
	
	public static void setRegKey(String type, int serialNo, String key) {
		Map<String, Object> var = new HashMap<String, Object>();
		var.put("type", type);
		String addr = TextFormat.format(serverSetRegKeyAddr, var);
		Properties param = new Properties();
		param.setProperty("key", key);
		param.setProperty("serialNo", String.valueOf(serialNo));
		InputStream in = null;
		try {
			in = NetworkUtils.makeHTTPGetRequest(addr, param, null);
			for (int a = in.read(); a != -1; a=in.read());
		} catch(IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) {
			}
		}
		
	}

}
