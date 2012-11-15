package au.com.isell.rlm.module.report.constant;

import java.util.HashMap;
import java.util.Map;

import au.com.isell.common.util.Formatter;

public class ReportPath {
	private ReportType type;
	private ReportFormat format;

	public ReportPath(ReportType type,ReportFormat format) {
		this.type = type;
		this.format = format;
	}

	private Map<String, String> params = new HashMap<String, String>();

	public ReportPath addParam(String key, String val) {
		params.put(key, val);
		return this;
	}

	public String getTplName() {
		return Formatter.DollarBraceName.format(type.getTplName(format ), params);
	}

	public String getTplInfoCloudKey() {
		return Formatter.DollarBraceName.format(type.getTplInfoCloudKey(format), params);
	}

	public String getRenderFileCloudKey() {
		return Formatter.DollarBraceName.format(type.getRenderFileCloudKey(format), params);
	}

	public String getOutputName() {
		return type.getOutputName(format);
	}

	public ReportType getType() {
		return type;
	}

}
