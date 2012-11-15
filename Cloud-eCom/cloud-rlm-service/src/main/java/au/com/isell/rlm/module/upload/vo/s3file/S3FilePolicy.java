package au.com.isell.rlm.module.upload.vo.s3file;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;

public class S3FilePolicy {
	private Date expiration;
	private List<PolicyCondition> conditions = new ArrayList<PolicyCondition>();

	public S3FilePolicy(Date expiration) {
		super();
		this.expiration = expiration;
	}

	public Date getExpiration() {
		return expiration;
	}

	public List<PolicyCondition> getConditions() {
		return conditions;
	}

	public S3FilePolicy withCondition(PolicyCondition condition) {
		conditions.add(condition);
		return this;
	}
	private static ObjectMapper objectMapper=new ObjectMapper();
	static {
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
	}

	@Override
	public String toString() {
		try {
			return objectMapper.writeValueAsString(this);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
class CustomDateSerializer extends JsonSerializer<PolicyCondition> {
	@Override
	public void serialize(PolicyCondition src, JsonGenerator gen, SerializerProvider provider) throws IOException, JsonProcessingException {
		if (src.getCompare() == null) {
			Map m = new HashMap();
			m.put(src.getName(), src.getValue());
			gen.writeObject(m);
		} else {
			String name = src.getName();
			name = "$" + name;
			Object[] arr = new Object[] { src.getCompare().getVal(), name, src.getValue() };
			gen.writeObject(arr);
		}
	}
}