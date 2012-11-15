package au.com.isell.rlm.module.upload.vo.s3file;


import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.util.Assert;

/**
 * see <a href="http://docs.amazonwebservices.com/AmazonS3/latest/dev/HTTPPOSTForms.html">condition refference on line</a>
 * @author frankw 22/03/2012
 */
@JsonSerialize(using = CustomDateSerializer.class)
public class PolicyCondition {
	public enum Compare {
		EQ("eq"), STARTS_WITH("starts-with");
		private String val;

		private Compare(String val) {
			this.val = val;
		}

		public String getVal() {
			return val;
		};
	}
	
	public static String NAME_BUCKET="bucket";
	public static String NAME_KEY="key";
	public static String NAME_ACL="acl";
	public static String NAME_SUCCESS_ACTION_REDIRECT="success_action_redirect";
	public static String NAME_REDIRECT="redirect";
	public static String NAME_SUCCESS_ACTION_STATUS="success_action_status";
	public static String NAME_CONTENT_TYPE="Content-Type";
	public static String NAME_PREFIX_META="x-amz-meta-";
	
	public static String VALUE_ACL_PUBLIC_READ="public-read";
	public static String VALUE_AUTHENTICATED_READ="authenticated-read";

	private String name;
	private Object value;
	private Compare compare;

	public PolicyCondition(String name, Object value) {
		this(name, value, null);
	}

	public PolicyCondition(String name, Object value, Compare compare) {
		Assert.notNull(name,"name should not be null");
		Assert.notNull(value,"value should not be null");

		this.name = name;
		this.value = value;
		this.compare = compare;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	public Compare getCompare() {
		return compare;
	}
}
