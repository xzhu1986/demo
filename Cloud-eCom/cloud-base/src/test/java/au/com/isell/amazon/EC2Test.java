package au.com.isell.amazon;

import java.io.IOException;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2AsyncClient;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;

public class EC2Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			PropertiesCredentials awsCredentials = new PropertiesCredentials(
					EC2Test.class.getResourceAsStream("/AwsCredentials.properties"));
			AWSCredentials credentials = new BasicAWSCredentials(awsCredentials.getAWSAccessKeyId(), awsCredentials.getAWSSecretKey());
			AmazonEC2AsyncClient ec2Client = new AmazonEC2AsyncClient(credentials);
			RunInstancesResult runInstancesResult = ec2Client.runInstances(new RunInstancesRequest().withImageId("ami-809235e9")
																 .withInstanceType(InstanceType.M1Small)
																 .withSecurityGroups("mongoDB")
																 .withKeyName("isell")
																 .withMaxCount(3)
																 .withMinCount(3));
			List<Instance> instances = runInstancesResult.getReservation().getInstances();
			int idx = 1;
			for (Instance instance : instances) {
			  CreateTagsRequest createTagsRequest = new CreateTagsRequest();
			  createTagsRequest.withResources(instance.getInstanceId()) //
			      									  .withTags(new Tag("Name", "mongo-replica-" + idx));
			  ec2Client.createTags(createTagsRequest);
			  idx++;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

}
