package au.com.isell.rlm.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class TestGetResellerSystemInfo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Process process = Runtime.getRuntime().exec("cmd /c systeminfo");
			InputStream fis = process.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			StringBuffer keyValue = new StringBuffer();
			StringBuffer sbValue = new StringBuffer();
			int lastIndex = 0;
			List<String[]> result = new ArrayList<String[]>();
			while ((line = br.readLine()) != null) {
				if(StringUtils.isNotBlank(line)){
					if(!Character.isSpaceChar(line.charAt(0))){
						result.add(new String[]{keyValue.toString(),sbValue.toString()});
						keyValue.setLength(0);
						sbValue.setLength(0);
						lastIndex = line.lastIndexOf(": ");
						keyValue.append(line.substring(0,lastIndex).trim());
						sbValue.append(line.substring(lastIndex+1).trim());
					}else{
						sbValue.append("\n");
						sbValue.append(line.trim());
					}
				}
			}
			result.remove(0);
			for (String[] item : result) {
				System.out.println(item[0] +":"+ item[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
