package au.com.isell.rlm.importing;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import au.com.isell.rlm.importing.flow.FlowManager;

public class ImportRLM extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {
		FlowManager flowManager = new FlowManager();
		flowManager.execute(args[0], args[1],args[2],args[3],args[4]);
		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new ImportRLM(), args);
		System.exit(res);
	}
}
