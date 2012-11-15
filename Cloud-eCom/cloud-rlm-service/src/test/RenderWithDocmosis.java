import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class RenderWithDocmosis {
	private static final String DWS_RENDER_URL = "https://dws.docmosis.com/services/rs/render";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Set your access Key
		String accessKey = "OWIxODlkNDYtZDNiZS00MThlLWI1ZTktNzYzMGMyN2VjNTVlOjQ1MTM2NDM";
		if ("".equals(accessKey)) {
			System.err
					.println("Please set your private access key from your Docmosis cloud account.");
			System.exit(1);
		}
		BufferedReader br = null;

		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(DWS_RENDER_URL).openConnection();
			System.out.println("Connecting [directly] to " + DWS_RENDER_URL);

			// set connection parameters
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);
			conn.setDoOutput(true);
			conn.setDoInput(true);

			// this example uses JSON format
			conn.setRequestProperty("Content-Type",
					"application/json; charset=utf-8");

			conn.connect();
			System.out.println("Connected");
			final String outputFileName = "Invoice Payments Due - Six Weeks.pdf";
			final String outputFile = "c:/work/reports/"+outputFileName;

			// build request
			String templateName = "agent/ae7b1620-7740-11e1-b0c4-0800200c9a66/Invoice Payments Due.doc";
			
//			String templateName = "agent/ae7b1620-7740-11e1-b0c4-0800200c9a66/12 Month Forcast.doc";
			StringBuffer sb = new StringBuffer();

			// Start building the instruction
			sb.append("{\n");
			sb.append("\"accessKey\":\"").append(accessKey).append("\",\n");
			sb.append("\"templateName\":\"").append(templateName)
					.append("\",\n");
			sb.append("\"outputName\":\"").append(outputFileName)
					.append("\",\n");

			// now add the data specifically for this template
			sb.append("\"data\":\n");
			br = new BufferedReader(new FileReader("c:/work/reports/Invoice Payments Due - Six Weeks.json"));
//			br = new BufferedReader(new FileReader("c:/work/reports/12 Month Forcast.json"));
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				sb.append(line);
			}
			sb.append("\n");
			sb.append("}\n");

			System.out.println("Sending request:" + sb.toString());

			// send the instruction in UTF-8 encoding so that most character
			// sets are available
			OutputStreamWriter os = new OutputStreamWriter(
					conn.getOutputStream(), "UTF-8");
			os.write(sb.toString());
			os.flush();

			int status = conn.getResponseCode();
			if (status == 200) {
				// successful render,
				// save our document to a file
				byte[] buff = new byte[1000];
				int bytesRead = 0;

				File file = new File(outputFile);
				FileOutputStream fos = new FileOutputStream(file);
				try {
					while ((bytesRead = conn.getInputStream().read(buff, 0,
							buff.length)) != -1) {
						fos.write(buff, 0, bytesRead);
					}
				} finally {
					fos.close();
				}

				System.out.println("Created file:" + file.getAbsolutePath());
			} else {
				// something went wrong - tell the user
				System.err.println("Our call failed: status = " + status);
				System.err.println("message:" + conn.getResponseMessage());
				BufferedReader errorReader = new BufferedReader(
						new InputStreamReader(conn.getErrorStream()));
				String msg;
				while ((msg = errorReader.readLine()) != null) {
					System.err.println(msg);
				}
				errorReader = null;
			}

		} catch (Exception e) {
			// can't make the connection
			System.err.println("Unable to connect to the docmosis cloud:"
					+ e.getMessage());
			System.err
					.println("If you have a proxy, you will need the Proxy aware example code.");
			System.exit(2);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
			try {
				br.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
