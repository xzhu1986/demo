package au.com.isell.rlm.module.reseller.processor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import sun.misc.BASE64Decoder;
import au.com.isell.common.filter.FilterItem.Type;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.message.MessageContainer;
import au.com.isell.common.message.MessageProcessor;
import au.com.isell.common.message.annotation.MessageDef;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.reseller.dao.ResellerDao;
import au.com.isell.rlm.module.reseller.domain.Reseller;
import au.com.isell.rlm.module.reseller.domain.ResellerSearchBean;
import au.com.isell.rlm.module.reseller.domain.VersionHistory;

@Component
@MessageDef(queue = "main", type = "resellerInfoV1")
public class ResellerInfoProcessor implements MessageProcessor {

	private Logger logger = LoggerFactory.getLogger(ResellerInfoProcessor.class);
	
	@Autowired
	private ResellerDao dao;
	
	@Override
	public List<MessageContainer> processMessage(
			List<Pair<String, Long>> messages) {
		BASE64Decoder decoder = new BASE64Decoder();
		FilterMaker maker = dao.getResellerSearchMaker();
		int count = 0;
		for (Pair<String, Long> message : messages) {
			try {
				Map<Integer, Object[]> syncDates = new HashMap<Integer, Object[]>();
				byte[] data = decoder.decodeBuffer(message.getKey());
				List<VersionHistory> histories = new ArrayList<VersionHistory>();
				Cursor cursor = new Cursor();
				while (data.length > cursor.point) {
					int serialNo = takeInt(data, cursor, 3);
					Reseller reseller = dao.getReseller(serialNo);
//					Reseller reseller = new Reseller();
//					reseller.setSerialNo(serialNo);
					if (reseller == null) continue;
					String version = takeVersion(data, cursor);
					boolean changed = false;
					if (!version.equals(reseller.getVersion())) {
						changed = true;
						reseller.setVersion(version);
					}
					String regKey = takeString(data, cursor, 16);
					if (!regKey.equals(reseller.getRegKey())) {
						changed = true;
						reseller.setRegKey(regKey);
					}
					String hardwareInfo = takeString(data, cursor, 17);
					if (!hardwareInfo.equals(reseller.getHardwareInfo())) {
						changed = true;
						reseller.setHardwareInfo(hardwareInfo);
					}
					int tempInt = takeInt(data, cursor, 2);
					if (reseller.getUsage().getTotalUsers()!=tempInt) {
						changed = true;
						reseller.getUsage().setTotalUsers(tempInt);
					}
					tempInt = takeInt(data, cursor, 2);
					if (reseller.getUsage().getQuotingUsers()!=tempInt) {
						changed = true;
						reseller.getUsage().setQuotingUsers(tempInt);
					}
					tempInt = takeInt(data, cursor, 2);
					if (reseller.getUsage().getQuoteCount()!=tempInt) {
						changed = true;
						reseller.getUsage().setQuoteCount(tempInt);
					}
					tempInt = takeInt(data, cursor, 2);
					if (reseller.getUsage().getOrderingUsers()!=tempInt) {
						changed = true;
						reseller.getUsage().setOrderingUsers(tempInt);
					}
					tempInt = takeInt(data, cursor, 2);
					if (reseller.getUsage().getOrderCount()!=tempInt) {
						changed = true;
						reseller.getUsage().setOrderCount(tempInt);
					}
					tempInt = takeInt(data, cursor, 2);
					if (reseller.getUsage().getJobUsers()!=tempInt) {
						changed = true;
						reseller.getUsage().setJobUsers(tempInt);
					}
					tempInt = takeInt(data, cursor, 2);
					if (reseller.getUsage().getJobCount()!=tempInt) {
						changed = true;
						reseller.getUsage().setJobCount(tempInt);
					}
					long tempLong = takeLong(data, cursor, 5);
					if (reseller.getUsage().getEpdProductCount()!=tempLong) {
						changed = true;
						reseller.getUsage().setEpdProductCount(tempLong);
					}
					tempLong = takeLong(data, cursor, 5);
					if (reseller.getUsage().getEpdMatchCount()!=tempLong) {
						changed = true;
						reseller.getUsage().setEpdMatchCount(tempLong);
					}
					tempLong = takeLong(data, cursor, 5);
					if (reseller.getUsage().getEpdImageCount()!=tempLong) {
						changed = true;
						reseller.getUsage().setEpdImageCount(tempLong);
					}
					cursor.stepFwd(4); // reseller vendor count
					Date lastSync = takeDate(data, cursor);
					if (lastSync != null) syncDates.put(serialNo, new Object[]{lastSync, regKey, hardwareInfo});
					for (int i = 1; i <= 3; i++) {
						String ver = takeVersion(data, cursor);
						if (ver.startsWith("0.")) {
							cursor.stepFwd(4);
							continue;
						}
						VersionHistory history = new VersionHistory();
						history.setSerialNo(serialNo);
						history.setVersion(version);
						try {
							history.setDatetime(takeDate(data, cursor));
						} catch (Exception e) {
						}
						histories.add(history);
					}
					System.out.println(reseller);
					if (changed) {
						dao.save(true, reseller);
					}
					count++;
				}
				dao.saveVersionHistories(histories.toArray(new VersionHistory[histories.size()]));
				String[] keys = new String[syncDates.size()];
				int i = 0;
				for (Integer serialNo : syncDates.keySet()) {
					keys[i] = serialNo.toString();
					i++;
				}
				List<ResellerSearchBean> beans = new ArrayList<ResellerSearchBean>();
				for (ResellerSearchBean bean : dao.queryResellerBeans(maker.makePickFilter("serialNo", keys, Type.Int), null)) {
					Object[] beanData = syncDates.get(bean.getSerialNo());
					bean.setSyncDate((Date) beanData[0]);
					bean.setRegKey((String) beanData[1]);
					bean.setHardwareInfo((String) beanData[2]);
					beans.add(bean);
				}
				dao.indexResellerSearchBeans(beans.toArray(new ResellerSearchBean[beans.size()]));
			} catch (IOException e) {
				logger.warn("Error when decode reseller info sent at " + new Date(message.getValue()), e);
				continue;
			}
		}
		System.out.println("Total reseller: "+count);
		return null;
	}

	private String takeVersion(byte[] data, Cursor cursor) {
		return takeInt(data, cursor, 1) +"."+takeInt(data, cursor, 1)+"."+takeInt(data, cursor, 1)+"."+takeInt(data, cursor, 3);
	}

	private int takeInt(byte[] data, Cursor cursor, int offset) {
		int val = 0;
		for (int i = 0; i < offset; i++) {
			val = (val << 8) + (data[cursor.point] & 0xff);
			cursor.stepFwd(1);
		}
		return val;
	}
	
	private long takeLong(byte[] data, Cursor cursor, int offset) {
		long val = 0;
		for (int i = 0; i < offset; i++) {
			val = (val << 8) + (data[cursor.point] & 0xff);
			cursor.stepFwd(1);
		}
		return val;
	}
	
	private String takeString(byte[] data, Cursor cursor, int offset) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < offset; i++) {
			sb.append((char) data[cursor.point]);
			cursor.stepFwd(1);
		}
		return sb.toString();
	}
	
	private Date takeDate(byte[] data, Cursor cursor) {
		int epoh = takeInt(data, cursor, 4);
		return new Date(epoh*1000);
	}
	
	private static class Cursor {
		private int point;
		
		public Cursor() {
			point = 0;
		}
		
		public void stepFwd(int steps) {
			point += steps;
		}
	}

	@Override
	public boolean deleteAfterProcess() {
		return true;
	}
	
	public static void main(String[] args) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader r = new BufferedReader(new FileReader("/Users/yezhou/msg.txt"));
			boolean start = true;
			for (String line = r.readLine(); line != null; line = r.readLine()) {
				if (start) start = false;
				else sb.append('\n');
				sb.append(line);
			}
			List<Pair<String, Long>> messages = new ArrayList<Pair<String, Long>>();
			messages.add(new Pair<String, Long>(sb.toString(), System.currentTimeMillis()));
			ResellerInfoProcessor p = new ResellerInfoProcessor();
			p.processMessage(messages);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
