package au.com.isell.idworker;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import au.com.isell.common.index.elasticsearch.IndexHelper;
import au.com.isell.idworker.dao.IDWorkerDao;

public class RegKeyWorker {
	@Autowired
	private IDWorkerDao dao;
	
	public RegKeyWorker(ApplicationContext ctx) {
		dao = ctx.getBean(IDWorkerDao.class);
	}
	
	private static final String GENERATE_CHARS = "ABCDEFGHJKPQRTUVWXY346789";

	public synchronized String generateRegKey(String type, int serialNo) {
		RegKey regKey = dao.getRegKey(type, serialNo);
		if (regKey != null) {
			return regKey.getRegKey();
		}
		int length = GENERATE_CHARS.length();
        StringBuilder sb = new StringBuilder();
        Random rdm = new Random();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                char ch = GENERATE_CHARS.charAt(rdm.nextInt(length));
                sb.append(ch);
            }
        }
        String key = sb.toString();
		if (!dao.isRegKeyExists(type, key)) {
			regKey = new RegKey();
			regKey.setRegKey(key);
			regKey.setSerialNo(serialNo);
			regKey.setType(type);
			dao.putRegKey(regKey);
			return key;
		} else {
			return generateRegKey(type, serialNo);
		}
	}

	public synchronized void setKey(String type, int serialNo, String key) {
		RegKey regKey = new RegKey();
		regKey.setRegKey(key);
		regKey.setSerialNo(serialNo);
		regKey.setType(type);
		dao.putRegKey(regKey);
	}
	
	public static void main(String[] args) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:/applicationContext.xml");
		IndexHelper.getInstance().registerType(RegKey.class);
		RegKeyWorker worker = new RegKeyWorker(ctx);
		System.out.println(worker.generateRegKey("reseller", 6100001));
	}
}
