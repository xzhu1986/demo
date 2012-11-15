package au.com.isell.rlm.module.supplier.processor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.isell.common.message.MessageContainer;
import au.com.isell.common.message.MessageProcessor;
import au.com.isell.common.message.annotation.MessageDef;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.supplier.domain.PriceBreak;
import au.com.isell.rlm.module.supplier.service.SupplierService;

@Component
@MessageDef(queue = "main", type = "priceBreakCacheDate")
public class PriceBreakCacheProcessor implements MessageProcessor {

	@Autowired
	private SupplierService service;

	@Override
	public List<MessageContainer> processMessage(List<Pair<String, Long>> messages) {
		List<PriceBreak> breaks = new ArrayList<PriceBreak>();
		for (Pair<String, Long> message : messages) {
			String[] content = message.getKey().split(":");
			String[] names = content[1].split("|");
			int supplierId = Integer.parseInt(content[0]);
			for (String name : names) {
				PriceBreak pb = service.getPriceBreakByName(supplierId, name);
				if (pb == null) continue;
				if (content[2].equals("full"))
					pb.setFullFeedDate(new Date(Long.parseLong(content[3])));
				else
					pb.setLastCacheDate(new Date(Long.parseLong(content[3])));
				breaks.add(pb);
			}
			service.savePriceBreaks(supplierId, false, breaks.toArray(new PriceBreak[breaks.size()]));
		}
		return null;
	}

	@Override
	public boolean deleteAfterProcess() {
		return true;
	}

}
