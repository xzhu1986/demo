package au.com.isell.common.util;

/**
 * @author frankw 19/04/2012
 */
public enum ContentType {
	Jpeg("image/jpeg"), Png("image/png"), Gif("image/gif"), Bmp("image/bmp"), Tiff("image/tiff"), Plain("text/plain"), Rtf("text/rtf"), Msword(
			"application/msword"), Zip("application/zip"), Mpeg("audio/mpeg"), Pdf("application/pdf"), Gzip("application/x-gzip"), Compressed(
			"application/x-compressed");

	private String value;

	private ContentType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static ContentType geType(String fileName) {
		String suffix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
		if ("doc".equals(suffix) || "docx".equals(suffix)) {
			return Msword;
		}
		for (ContentType type : ContentType.values()) {
			if (type.getValue().endsWith(suffix))
				return type;
		}
		return null;
	}
}