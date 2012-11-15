package au.com.isell.epd;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import au.com.isell.epd.importing.EPDImportFlow;

public class ImportOpenrange extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {
		EPDImportFlow flow = new EPDImportFlow();
		flow.execute(args[0], args[1],args[2]);
		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new ImportOpenrange(), args);
		System.exit(res);
	}
}
