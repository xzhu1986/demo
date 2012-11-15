package au.com.isell.epd.importing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import cascading.cascade.Cascade;
import cascading.cascade.CascadeConnector;
import cascading.flow.Flow;
import cascading.flow.FlowConnector;

import com.amazonaws.auth.PropertiesCredentials;

public class EPDImportFlow {
	private FlowConnector flowConnector = null;

	public EPDImportFlow() throws IOException {
		PropertiesCredentials awsCredentials = new PropertiesCredentials(EPDImportFlow.class.getResourceAsStream("/AwsCredentials.properties"));
		Properties properties = new Properties();
		properties.setProperty("fs.s3n.awsAccessKeyId", awsCredentials.getAWSAccessKeyId());
		properties.setProperty("fs.s3n.awsSecretAccessKey", awsCredentials.getAWSSecretKey());
		properties.setProperty("fs.sn.awsAccessKeyId", awsCredentials.getAWSAccessKeyId());
		properties.setProperty("fs.sn.awsSecretAccessKey", awsCredentials.getAWSSecretKey());
		FlowConnector.setApplicationJarClass(properties, EPDImportFlow.class);
		flowConnector = new FlowConnector(properties);
	}

	public void execute(String inputPath, String outputPath,String oldDataPath) {
		Flow flow = new TempProductsFlow(flowConnector,inputPath, outputPath,oldDataPath).createFlow();
		flow.start();
		flow.complete();
		
		
		List<Flow> flowList = new ArrayList<Flow>();
		flowList.add(new ProductsFlow(flowConnector,inputPath, outputPath,oldDataPath).createFlow());
		flowList.add(new EPDIdsFlow(flowConnector,inputPath, outputPath,oldDataPath).createFlow());
		flowList.add(new OptionsFlow(flowConnector,inputPath, outputPath,oldDataPath).createFlow());
		flowList.add(new UnspscFlow(flowConnector,inputPath, outputPath,oldDataPath).createFlow());
		flowList.add(new SpecFlow(flowConnector,inputPath, outputPath,oldDataPath).createFlow());
		flowList.add(new AttributesFlow(flowConnector,inputPath, outputPath,oldDataPath).createFlow());
		
		CascadeConnector cascadeConnector = new CascadeConnector();
		Cascade cascade = cascadeConnector.connect(flowList.toArray(new Flow[0]));
		cascade.complete();
	}

}
