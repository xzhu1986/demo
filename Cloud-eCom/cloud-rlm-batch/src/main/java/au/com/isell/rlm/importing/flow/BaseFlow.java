package au.com.isell.rlm.importing.flow;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;

import au.com.isell.rlm.importing.function.common.SetEmptyStringToNullFunction;
import au.com.isell.rlm.importing.function.common.SetNullValueFunction;
import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.pipe.Each;
import cascading.pipe.Pipe;
import cascading.tap.Tap;
import cascading.tuple.Fields;

@SuppressWarnings("deprecation")
abstract class BaseFlow {
	protected String strRegExp = "\"?,(?=[^\"]*(?:(?:\"[^\"]*){2})*$)\"?";
	protected Map<String, Tap> sourceTaps = new HashMap<String, Tap>();
	protected Map<String, Tap> sinkTaps = new HashMap<String, Tap>();
	private String todayStr = null;
	protected String commonOldDataPath = "s3n://isell.test/epd/old-data/";
	protected String commonOldUUIDPath = "s3n://isell.test/epd/old-uuid/";
	protected String commonInputPath = "s3n://isell.test/epd/import/pending/China/";
	protected String commonOutputPath = "s3n://isell.test/epd/output/"+todayStr+"/";
	protected FlowConnector flowConnector = null;
	protected String onlyUpdateChange = "true";

	protected BaseFlow(FlowConnector flowConnector,String commonInputPath,String commonOutputPath,String commonOldDataPath,String commonOldUUIDPath,String onlyUpdateChange){
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		this.todayStr = df.format(new Date());
		this.flowConnector = flowConnector;
		this.commonInputPath = commonInputPath;
		this.commonOutputPath = commonOutputPath;
		this.commonOldDataPath = commonOldDataPath;
		this.commonOldUUIDPath = commonOldUUIDPath;
		this.onlyUpdateChange = onlyUpdateChange;
		sourceTaps.clear();
		sinkTaps.clear();
	}

	protected Pipe createFormatEmptyStringToNullPipe(Pipe pipe) {
		return new Each(pipe, new SetEmptyStringToNullFunction(Fields.ALL));
	}
	protected Pipe createFormatNullPipe(Pipe pipe) {
		return new Each(pipe, new SetNullValueFunction(Fields.ALL));
	}
	
	protected void copyFile(String src, String dst) {
		FileSystem fs = null;
		try {
			fs = FileSystem.get(URI.create(src),new JobConf());
			FileStatus[] oldFiles = fs.listStatus(new Path(dst));
			for (int i = 0; oldFiles != null && i < oldFiles.length; i++) {
				fs.delete(oldFiles[i].getPath(), true);
			}
			FileStatus[] files = fs.listStatus(new Path(src));
			for (int i = 0; files != null && i < files.length; i++) {
				fs.rename(files[i].getPath(), new Path(dst + files[i].getPath().getName()));
			}
		} catch (Exception e) {
			System.out.println("copyFile:"+e.getMessage());
		} finally {
			try {
				if (fs != null) {
					fs.close();
				}
			} catch (IOException e) {
				// Ignore it
			}
		}
	}
	
	protected void createDir(String dirPath) {
		FileSystem fs = null;
		try {
			fs = FileSystem.get(URI.create(dirPath),new JobConf());
			if (!fs.exists(new Path(dirPath)))
				fs.mkdirs(new Path(dirPath));
		} catch (Exception e) {
			System.out.println("createDir:"+e.getMessage());
		} finally {
			try {
				if (fs != null) {
					fs.close();
				}
			} catch (IOException e) {
				// Ignore it
			}
		}
	}
	public abstract Flow createFlow(); 
}
