package au.com.isell.rlm.importing;

import au.com.isell.rlm.importing.flow.FlowManager;

public class LocalImportRLM {

	public static void main(String[] args) throws Exception {
		FlowManager flowManager = new FlowManager();
		long starttime = System.currentTimeMillis();
		flowManager.execute("data/Apollo/", "output/apollo/","old-data/Apollo/","old-uuid/Apollo/","false");
		System.out.println(System.currentTimeMillis() - starttime);
	}
}
