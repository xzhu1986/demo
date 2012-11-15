package au.com.isell.common.aws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.BatchResultErrorEntry;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.DeleteMessageBatchResult;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

public class SQSHelper {
	private static SQSHelper instance;
	
	AmazonSQSClient client;

	private SQSHelper() {
		try {
			client = new AmazonSQSClient(new PropertiesCredentials(
					SQSHelper.class.getResourceAsStream("/AwsCredentials.properties")));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized static SQSHelper getInstance() {
		if (instance == null) {
			instance = new SQSHelper();
		}
		return instance;
	}
	
	public String postMessage(String queueName, String message) throws IOException {
		GetQueueUrlRequest urlReq = new GetQueueUrlRequest(queueName);
		GetQueueUrlResult urlResult = client.getQueueUrl(urlReq);
		SendMessageRequest req = new SendMessageRequest(urlResult.getQueueUrl(), message);
		SendMessageResult result =  client.sendMessage(req);
		return result.getMessageId();
	}
	
	/**
	 * 
	 * @param queueName
	 * @param accessKey
	 * @param securityKey
	 * @return {messageId, messageBody, message receiptHandle, sent time in milliseconds}
	 * @throws IOException
	 */
	public List<String[]> loadMessages(String queueName, int limit) throws IOException {
		GetQueueUrlRequest urlReq = new GetQueueUrlRequest(queueName);
		GetQueueUrlResult urlResult = client.getQueueUrl(urlReq);
		List<String[]> messages = new ArrayList<String[]>();
		int count = 0;
		while (limit <= 0 || limit > count) {
			ReceiveMessageRequest req = new ReceiveMessageRequest(urlResult.getQueueUrl());
			int fetchSize = limit <= 0 ? 10 : (limit - count > 10 ? 10 : limit - count);
			req.setMaxNumberOfMessages(fetchSize);
			List<String> attributeNames = new ArrayList<String>();
			attributeNames.add("SentTimestamp");
			req.setAttributeNames(attributeNames);
			ReceiveMessageResult result = client.receiveMessage(req);
			if (result.getMessages().size() == 0) break;
			for(Message message : result.getMessages()) {
				long sentTime = Long.parseLong(message.getAttributes().get("SentTimestamp")) * 1000;
				messages.add(new String[]{message.getMessageId(), message.getBody(), message.getReceiptHandle(), String.valueOf(sentTime)});
				count++;
			}
		}
		return messages;
	}

	public void deleteMessages(String queueName, List<String[]> messages) throws IOException {
		GetQueueUrlRequest urlReq = new GetQueueUrlRequest(queueName);
		GetQueueUrlResult urlResult = client.getQueueUrl(urlReq);
		List<DeleteMessageBatchRequestEntry> entries = new ArrayList<DeleteMessageBatchRequestEntry>();
		List<String> failedIds = new ArrayList<String>();
		int i = 0;
		for (String[] message : messages) {
			DeleteMessageBatchRequestEntry entry = new DeleteMessageBatchRequestEntry(message[0], message[2]);
			entries.add(entry);
			i++;
			if (i == 10) {
				DeleteMessageBatchRequest batch = new DeleteMessageBatchRequest(urlResult.getQueueUrl(), entries);
				DeleteMessageBatchResult result = client.deleteMessageBatch(batch);
				List<BatchResultErrorEntry> failedEntries = result.getFailed();
				for (BatchResultErrorEntry failed : failedEntries) {
					failedIds.add(failed.getId());
				}
				entries.clear();
				i = 0;
			}
		}
		if (i > 0) {
			DeleteMessageBatchRequest batch = new DeleteMessageBatchRequest(urlResult.getQueueUrl(), entries);
			DeleteMessageBatchResult result = client.deleteMessageBatch(batch);
			List<BatchResultErrorEntry> failedEntries = result.getFailed();
			for (BatchResultErrorEntry failed : failedEntries) {
				failedIds.add(failed.getId());
			}
		}
		if (failedIds.size() > 0) throw new RuntimeException("Filed: "+failedIds);
	}

	public String initQueue(String queueName) {
		CreateQueueRequest req = new CreateQueueRequest(queueName);
		CreateQueueResult result = client.createQueue(req);
		return result.getQueueUrl();
	}
}
