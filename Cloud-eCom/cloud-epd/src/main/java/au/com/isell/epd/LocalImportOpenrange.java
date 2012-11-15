package au.com.isell.epd;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import au.com.isell.epd.importing.EPDImportFlow;

public class LocalImportOpenrange extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {
		EPDImportFlow flow = new EPDImportFlow();
		long starttime = System.currentTimeMillis();
		flow.execute("data/epd/cn/", "output/epd/","old-data/");
		System.out.println(System.currentTimeMillis() - starttime);
		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new LocalImportOpenrange(), args);
		System.exit(res);
	}
}
