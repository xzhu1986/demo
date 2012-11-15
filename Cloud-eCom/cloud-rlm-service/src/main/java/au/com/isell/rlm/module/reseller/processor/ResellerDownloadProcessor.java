package au.com.isell.rlm.module.reseller.processor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.isell.common.message.MessageContainer;
import au.com.isell.common.message.MessageProcessor;
import au.com.isell.common.message.annotation.MessageDef;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap;
import au.com.isell.rlm.module.reseller.service.ResellerService;

@Component
@MessageDef(queue = "main", type = "resellerDownloadDate")
public class ResellerDownloadProcessor implements MessageProcessor {

	@Autowired
	private ResellerService service;

	@Override
	public List<MessageContainer> processMessage(List<Pair<String, Long>> messages) {
		Map<String, ResellerSupplierMap> map = new HashMap<String, ResellerSupplierMap>();
		for (Pair<String, Long> message : messages) {
			String[] content = message.getKey().split(":");
			String key = content[0] + ':' + content[1]+':'+content[2];
			ResellerSupplierMap resellerBreak = map.get(key);
			if (resellerBreak == null) {
				resellerBreak = service.getResellerSupplierMap(Integer.parseInt(content[0]),Integer.parseInt(content[1]));
				if (resellerBreak == null) continue;
				map.put(key, resellerBreak);
			}
			resellerBreak.setLastDownload(new Date(Long.parseLong(content[3])));
		}
		for (ResellerSupplierMap resellerBreak : map.values()) {
			service.saveResellerSupplierMapFromForm(resellerBreak,
					resellerBreak.getSerialNo(),
					resellerBreak.getSupplierId(), false);
		}
		return null;
	}

	@Override
	public boolean deleteAfterProcess() {
		return true;
	}

}
