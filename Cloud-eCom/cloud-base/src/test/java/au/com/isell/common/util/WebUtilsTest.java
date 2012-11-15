package au.com.isell.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class WebUtilsTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// List<String[]> result = WebUtils.getCountryList();
		try {
			// 执行命令
			Process process = Runtime.getRuntime().exec("cmd /c systeminfo");
			// 取得命令结果的输出流
			InputStream fis = process.getInputStream();
			// 用一个读输出流类去读
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			// 逐行读取输出到控制台
			StringBuffer keyValue = new StringBuffer();
			StringBuffer sbValue = new StringBuffer();
			int lastIndex = 0;
			List<String[]> result = new ArrayList<String[]>();
			while ((line = br.readLine()) != null) {
				if(StringUtils.isNotEmpty(line)){
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
