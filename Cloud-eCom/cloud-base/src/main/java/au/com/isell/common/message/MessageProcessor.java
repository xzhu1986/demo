package au.com.isell.common.message;

import java.util.List;

import au.com.isell.remote.common.model.Pair;

/**
 * Message format will be type:content
 * @author yezhou
 *
 */
public interface MessageProcessor {
	List<MessageContainer> processMessage(List<Pair<String, Long>> messages);
	boolean deleteAfterProcess();
}
