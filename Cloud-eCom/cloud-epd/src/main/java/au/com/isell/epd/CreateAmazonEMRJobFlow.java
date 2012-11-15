package au.com.isell.epd;

import java.io.IOException;
import java.util.List;
import java.util.TimerTask;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.model.AddInstanceGroupsRequest;
import com.amazonaws.services.elasticmapreduce.model.AddJobFlowStepsRequest;
import com.amazonaws.services.elasticmapreduce.model.DescribeJobFlowsRequest;
import com.amazonaws.services.elasticmapreduce.model.DescribeJobFlowsResult;
import com.amazonaws.services.elasticmapreduce.model.HadoopJarStepConfig;
import com.amazonaws.services.elasticmapreduce.model.InstanceGroupConfig;
import com.amazonaws.services.elasticmapreduce.model.InstanceGroupDetail;
import com.amazonaws.services.elasticmapreduce.model.InstanceGroupModifyConfig;
import com.amazonaws.services.elasticmapreduce.model.InstanceRoleType;
import com.amazonaws.services.elasticmapreduce.model.JobFlowDetail;
import com.amazonaws.services.elasticmapreduce.model.JobFlowInstancesConfig;
import com.amazonaws.services.elasticmapreduce.model.MarketType;
import com.amazonaws.services.elasticmapreduce.model.ModifyInstanceGroupsRequest;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowRequest;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowResult;
import com.amazonaws.services.elasticmapreduce.model.StepConfig;
import com.amazonaws.services.elasticmapreduce.util.StepFactory;

public class CreateAmazonEMRJobFlow {
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		PropertiesCredentials awsCredentials = new PropertiesCredentials(
				CreateAmazonEMRJobFlow.class.getResourceAsStream("/AwsCredentials.properties"));
		
		AWSCredentials credentials = new BasicAWSCredentials(awsCredentials.getAWSAccessKeyId(), awsCredentials.getAWSSecretKey());
		AmazonElasticMapReduceClient emr = new AmazonElasticMapReduceClient(credentials);

//		RunJobFlowResult result = createJobFlow(emr);
		getJobFlowDetail(emr, "j-26ZYVA9SQD1MR");
		
//		Timer timer = new Timer();
//		timer.schedule(new jobFlowListenerTask(emr,result.getJobFlowId()), 60000,60000);
		
//		addFlowStep(emr,"j-26ZYVA9SQD1MR");
		
//		addInstanceGroups(emr,"j-26ZYVA9SQD1MR");
		modifyInstanceGroups(emr,"ig-3APREUO533RAA");
	}

	private static String getJobFlowDetail(AmazonElasticMapReduceClient emr,String jobFlowId) {
		DescribeJobFlowsResult r = emr.describeJobFlows(new DescribeJobFlowsRequest().withJobFlowIds(jobFlowId));
		List<JobFlowDetail> flows = r.getJobFlows();
		String state = null;
		for (JobFlowDetail flow : flows) {
			state = flow.getExecutionStatusDetail().getState();
			System.out.println(flow.getName() + ":" + state);
			System.out.println(flow.toString());
			for(InstanceGroupDetail instanceGroupDetail : flow.getInstances().getInstanceGroups()){
				System.out.println(instanceGroupDetail.toString());
			}
		}
		return state;
	}

	private static RunJobFlowResult createJobFlow(AmazonElasticMapReduceClient emr) {
		StepFactory stepFactory = new StepFactory();
		StepConfig enableDebugging = new StepConfig()
		       .withName("Enable Debugging")
		       .withActionOnFailure("TERMINATE_JOB_FLOW")
		       .withHadoopJarStep(stepFactory.newEnableDebuggingStep());
		
		StepConfig installCustom = new StepConfig()
		       .withName("Install Custom Jar 1")
		       .withActionOnFailure("TERMINATE_JOB_FLOW")
		       .withHadoopJarStep(new HadoopJarStepConfig()
		       			.withJar("s3://isell.test/epd/execute/wordcount.jar")
		       			.withArgs("s3://isell.test/epd/data/wordcount.txt","s3://isell.test/epd/output/wordcount1"));

		RunJobFlowRequest request = new RunJobFlowRequest()
		       .withName("Word Count")
		       .withSteps(enableDebugging, installCustom)
		       .withLogUri("s3://isell.test/epd/logs/")
		       .withInstances(new JobFlowInstancesConfig()
		           .withHadoopVersion("0.20")
		           .withInstanceCount(2)
		           .withKeepJobFlowAliveWhenNoSteps(true)
		           .withMasterInstanceType("m1.small")
		           .withSlaveInstanceType("m1.small"));

		RunJobFlowResult result = emr.runJobFlow(request);
		System.out.println(result.toString());
		return result;
	}

	private static void addFlowStep(AmazonElasticMapReduceClient emr,String jobFlowId) {
		HadoopJarStepConfig stepConfig = new HadoopJarStepConfig()
											.withJar("s3://isell.test/epd/execute/wordcount.jar")
											.withArgs("s3://isell.test/epd/data/wordcount.txt","s3://isell.test/epd/output/wordcount2");
		
		AddJobFlowStepsRequest r = new AddJobFlowStepsRequest()
										.withJobFlowId(jobFlowId)
										.withSteps(new StepConfig()
										.withName("Install Custom Jar 2")
										.withActionOnFailure("TERMINATE_JOB_FLOW")
										.withHadoopJarStep(stepConfig));
		emr.addJobFlowSteps(r);
	}
	
	private static void addInstanceGroups(AmazonElasticMapReduceClient emr,String jobFlowId) {
		InstanceGroupConfig instanceGroupConfig = new InstanceGroupConfig()
													.withInstanceCount(2)
													.withInstanceType("m1.small")
													.withMarket(MarketType.ON_DEMAND)
													.withInstanceRole(InstanceRoleType.CORE)
													.withName("slave");
		emr.addInstanceGroups(new AddInstanceGroupsRequest()
	 							.withInstanceGroups(instanceGroupConfig)
	 							.withJobFlowId(jobFlowId)); 
	}
	
	private static void modifyInstanceGroups(AmazonElasticMapReduceClient emr,String instanceGroupId) {
		InstanceGroupModifyConfig instanceGroupModifyConfig = new InstanceGroupModifyConfig()
																	.withInstanceCount(2)
																	.withInstanceGroupId(instanceGroupId);
		emr.modifyInstanceGroups(new ModifyInstanceGroupsRequest()
	 							.withInstanceGroups(instanceGroupModifyConfig)); 
	}
	
	static class jobFlowListenerTask extends TimerTask {
		private AmazonElasticMapReduceClient emr = null;
		private String jobFlowId = null;

		public jobFlowListenerTask(AmazonElasticMapReduceClient emr, String jobFlowId) {
			super();
			this.emr = emr;
			this.jobFlowId = jobFlowId;
		}

		@Override
		public void run() {
			String state = getJobFlowDetail(emr,jobFlowId);
			if("RUNNING".equalsIgnoreCase(state)){
				System.out.println("add a step");
				addFlowStep(emr,jobFlowId);
			}
		}
		
	}
}
