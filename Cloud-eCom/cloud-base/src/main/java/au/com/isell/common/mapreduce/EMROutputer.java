package au.com.isell.common.mapreduce;

import java.io.IOException;
import java.util.Map;

public interface EMROutputer {
	void writeRecord(String key, Map<String, String> content) throws IOException;
	void writeMessage(String key, String message) throws IOException;
	void close() throws IOException;
}
