package au.com.isell.idworker;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import au.com.isell.common.index.elasticsearch.IndexHelper;
import au.com.isell.idworker.dao.IDWorkerDao;

public class IDWorker {
	private String type;
	private AtomicInteger lastNumber;
	private IDWorkerDao dao;
	
	public IDWorker(String type, ApplicationContext ctx) {
		dao = ctx.getBean(IDWorkerDao.class);
		this.type = type;
		lastNumber = new AtomicInteger(dao.getLastNumber(type));
	}
	
	public synchronized void setLastNumber(int number) {
		lastNumber.set(number);
		dao.putLastNumber(type, number);
	}
	
	public synchronized int nextId() {
		return nextId(1)[0];
	}

	public synchronized int[] nextId(int count) {

		int[] ids = new int[count];
		for (int i = 0; i < count; i++) {
			ids[i] = lastNumber.addAndGet(1);
		}
		dao.putLastNumber(type, ids[ids.length-1]);
		return ids;
	}
	
	public static void main(String[] args) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:/applicationContext.xml");
		IndexHelper.getInstance().registerType(IndexId.class);
		IDWorker worker = new IDWorker("test", ctx);
		System.out.println(worker.nextId());
	}
}
