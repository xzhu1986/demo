package au.com.isell.rlm.module.supplier.processor;

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
import au.com.isell.rlm.module.supplier.domain.Supplier;
import au.com.isell.rlm.module.supplier.service.SupplierService;

@Component
@MessageDef(queue = "main", type = "supplierImport")
public class SupplierImportProcessor implements MessageProcessor {

	@Autowired
	private SupplierService service;

	@Override
	public List<MessageContainer> processMessage(List<Pair<String, Long>> messages) {
		Map<Integer, Supplier> map = new HashMap<Integer, Supplier>();
		for (Pair<String, Long> message : messages) {
			String[] content = message.getKey().split(":");
			int supplierId = Integer.parseInt(content[0]);
			Supplier sup = map.get(supplierId);
			if (sup == null) {
				sup = service.getSupplier(supplierId);
				if (sup == null) continue;
				map.put(supplierId, sup);
			}
			sup.setProductCount(Integer.parseInt(content[1]));
			sup.setLastImportDate(new Date(Long.parseLong(content[2])));
		}
		for (Supplier sup : map.values()) {
			service.saveSupplier(sup, false);
		}
		return null;
	}

	@Override
	public boolean deleteAfterProcess() {
		return true;
	}

}
