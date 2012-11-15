package au.com.isell.rlm.importing.flow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import cascading.cascade.Cascade;
import cascading.cascade.CascadeConnector;
import cascading.flow.Flow;
import cascading.flow.FlowConnector;

import com.amazonaws.auth.PropertiesCredentials;

public class FlowManager {
	private FlowConnector flowConnector = null;

	public FlowManager() throws IOException {
		PropertiesCredentials awsCredentials = new PropertiesCredentials(FlowManager.class.getResourceAsStream("/AwsCredentials.properties"));
		Properties properties = new Properties();
		properties.setProperty("fs.s3n.awsAccessKeyId", awsCredentials.getAWSAccessKeyId());
		properties.setProperty("fs.s3n.awsSecretAccessKey", awsCredentials.getAWSSecretKey());
		properties.setProperty("fs.sn.awsAccessKeyId", awsCredentials.getAWSAccessKeyId());
		properties.setProperty("fs.sn.awsSecretAccessKey", awsCredentials.getAWSSecretKey());
		FlowConnector.setApplicationJarClass(properties, FlowManager.class);
		flowConnector = new FlowConnector(properties);
	}

	public void execute(String inputPath, String outputPath, String oldDataPath, String oldUUIDPath, String onlyUpdateChange) {

		List<Flow> flowList = new ArrayList<Flow>();
		flowList.add(new SupplierFlow(flowConnector, inputPath, outputPath, oldDataPath, oldUUIDPath, onlyUpdateChange).createFlow());
		flowList.add(new ResellerFlow(flowConnector, inputPath, outputPath, oldDataPath, oldUUIDPath, onlyUpdateChange).createFlow());
		flowList.add(new InvoicesFlow(flowConnector, inputPath, outputPath, oldDataPath, oldUUIDPath, onlyUpdateChange).createFlow());
		flowList.add(new SetMaxIDFlow(flowConnector, inputPath, outputPath, oldDataPath, oldUUIDPath, onlyUpdateChange).createFlow());

		CascadeConnector cascadeConnector = new CascadeConnector();
		Cascade cascade = cascadeConnector.connect(flowList.toArray(new Flow[0]));
		cascade.complete();
	}
}
