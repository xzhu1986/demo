package au.com.isell.common.message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import au.com.isell.common.aws.SQSHelper;
import au.com.isell.common.index.elasticsearch.IndexHelper;
import au.com.isell.common.message.annotation.MessageDef;
import au.com.isell.common.util.ClassFilter;
import au.com.isell.common.util.ClassMatcher;
import au.com.isell.remote.common.model.Pair;

/**
 * Message format will be type:content
 * 
 * @author yezhou
 * 
 */
public class MessageChecker {

	private static Map<String, MessageChecker> queueMap;
	private static Map<Class<?>, Pair<String, String>> containerMap;
	private static ApplicationContext context;
	private static Logger logger = LoggerFactory.getLogger(MessageChecker.class);

	private SQSHelper helper;
	private Map<String, MessageProcessor> typeMap;
	private String queue;
	private static Properties bundle;
	private boolean checkingMessages;

	private static void init() {
		bundle = new Properties();
		try {
			bundle.load(IndexHelper.class.getResourceAsStream("/settings.properties"));
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		containerMap = new Hashtable<Class<?>, Pair<String, String>>();
		ClassFilter.doFilter("au.com.isell", new ClassMatcher() {
			@Override
			public void match(Class<?> cls) {
				if (cls.isAnnotationPresent(MessageDef.class)) {
					MessageDef def = cls.getAnnotation(MessageDef.class);
					String queueName = bundle.getProperty("queue.name."+def.queue());
					if (MessageProcessor.class.isAssignableFrom(cls)) {
						MessageChecker.getInstance(def.queue()).setQueueName(queueName);
						MessageChecker.getInstance(def.queue()).registerProcessor(def.type(), cls);
					} else if (MessageContainer.class.isAssignableFrom(cls)) {
						registerContainer(queueName, def.type(), cls);
					}
					SQSHelper.getInstance().initQueue(queueName);
				}
			}
		});
	}

	private MessageChecker() {
		helper = SQSHelper.getInstance();
		typeMap = new Hashtable<String, MessageProcessor>();
	}

	public synchronized static MessageChecker getInstance(String queue) {
		if (queueMap == null) {
			queueMap = new Hashtable<String, MessageChecker>();
		}
		MessageChecker checker = queueMap.get(queue);
		if (checker == null) {
			checker = new MessageChecker();
			queueMap.put(queue, checker);
		}
		return queueMap.get(queue);
	}
	
	public void setQueueName(String queueName) {
		this.queue = queueName;
	}

	private void registerProcessor(String type, Class<?> cls) {
		MessageProcessor processor = typeMap.get(type);
		if (processor == null) {
			if (context == null) return;
			typeMap.put(type, (MessageProcessor) context.getBean(cls));
		} else if (!processor.getClass().isAssignableFrom(cls)) {
			throw new RuntimeException("Type " + type + " in queue " + queue
					+ " has been defined multiple times: ["
					+ processor.getClass() + ", " + cls + "]");
		}
	}

	private static void registerContainer(String queue, String type,
			Class<?> cls) {
		Pair<String, String> keys = containerMap.get(cls);
		if (keys == null) {
			containerMap.put(cls, new Pair<String, String>(queue, type));
		}
	}

	public void checkMessages() throws IOException {
		if (checkingMessages) {
			logger.info("It's checking messages");
			return;
		}
		if (context == null) return;
		synchronized(this) {
			checkingMessages = true;
			while (true) {
				try {
					List<String[]> messages = helper.loadMessages(queue, 400);
					if (messages.size() == 0) {
						checkingMessages = false;
						return;
					}
					logger.info("Received "+ messages.size() + " messages");
					List<String> typeQueue = new ArrayList<String>();
					Map<String, List<Pair<String, Long>>> messageMap = new HashMap<String, List<Pair<String, Long>>>();
					for (int i = 0; i < messages.size(); i++) {
						String[] message = messages.get(i);
						String body = message[1];
						String type = body.substring(0, body.indexOf(':'));
						String content = body.substring(body.indexOf(':') + 1);
						if (!typeQueue.contains(type))
							typeQueue.add(type);
						List<Pair<String, Long>> msgList = messageMap.get(type);
						if (msgList == null) {
							msgList = new ArrayList<Pair<String, Long>>();
							messageMap.put(type, msgList);
						}
						msgList.add(new Pair<String, Long>(content, new Long(message[3])));
						if (typeMap.get(type) != null && !typeMap.get(type).deleteAfterProcess()) {
							messages.remove(i);
							i--;
						}
					}
					logger.info("Generated "+ typeQueue.size() + " types in queue");
					for (String type : typeQueue) {
						MessageProcessor processor = typeMap.get(type);
						if (processor == null) continue;
						List<MessageContainer> returnMessages = processor
								.processMessage(messageMap.get(type));
						if (returnMessages == null) continue;
						for (MessageContainer container : returnMessages) {
							Pair<String, String> key = containerMap.get(container
									.getClass());
							helper.postMessage(key.getKey(), key.getValue() + ':'
									+ container.toMessage());
						}
					}
					helper.deleteMessages(queue, messages);
				} catch (Throwable e) {
					logger.warn(e.getMessage(), e);
				}
			}
		}
	}

	public void sendMessages(MessageContainer... messages) throws IOException {
		if (containerMap == null) return;
		for (MessageContainer container : messages) {
			Pair<String, String> key = containerMap.get(container.getClass());
			if (key == null) continue;
			helper.postMessage(key.getKey(),
					key.getValue() + ':' + container.toMessage());
		}
	}

	public void sendMessages(List<MessageContainer> messages)
			throws IOException {
		if (containerMap == null) return;
		for (MessageContainer container : messages) {
			Pair<String, String> key = containerMap.get(container.getClass());
			helper.postMessage(key.getKey(),
					key.getValue() + ':' + container.toMessage());
		}
	}

	public static void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		context = applicationContext;
		init();
	}
}
