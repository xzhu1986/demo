package au.com.isell.rlm.module.upload.vo.s3file;

import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.time.DateUtils;

import au.com.isell.common.util.SecurityUtils;
import au.com.isell.rlm.module.upload.vo.s3file.PolicyCondition.Compare;

public class S3FileBean {
	public String AWSAccessKeyId;
	public String signature;
	public String policy;
	public String bucket;
	public List<PolicyCondition> conditions;

	public S3FileBean(String accessKey, String secretKey, S3FilePolicy policy) {
		this.AWSAccessKeyId = accessKey;
		try {
			String base64 = new String(Base64.encodeBase64(policy.toString().getBytes("UTF-8"), false), "UTF-8");
			this.policy = base64;
			this.signature = SecurityUtils.digestHMACSHA1(base64, secretKey);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		conditions = policy.getConditions();

		for (PolicyCondition condition : conditions) {
			if (condition.getName().equals(PolicyCondition.NAME_BUCKET)) {
				this.bucket = condition.getValue().toString();
				conditions.remove(condition);
				break;
			}
		}
	}

	public String getAWSAccessKeyId() {
		return AWSAccessKeyId;
	}

	public String getSignature() {
		return signature;
	}

	public String getPolicy() {
		return policy;
	}

	public String getBucket() {
		return bucket;
	}

	public List<PolicyCondition> getConditions() {
		return conditions;
	}
	

	@Override
	public String toString() {
		return "S3FileBean [AWSAccessKeyId=" + AWSAccessKeyId + ", signature=" + signature + ", policy=" + policy + ", bucket=" + bucket
				+ ", conditions=" + conditions + "]";
	}

	public static void main(String[] args) {
		S3FilePolicy policy = new S3FilePolicy(DateUtils.addMinutes(new Date(), 1));
		policy.withCondition(new PolicyCondition(PolicyCondition.NAME_ACL, PolicyCondition.VALUE_ACL_PUBLIC_READ));
		policy.withCondition(new PolicyCondition(PolicyCondition.NAME_BUCKET, "test/bk1"));
		policy.withCondition(new PolicyCondition(PolicyCondition.NAME_KEY, "testkey", Compare.STARTS_WITH));
		S3FileBean bean = new S3FileBean("assskey", "sceky", policy);
		System.out.println(bean);
	}
}
