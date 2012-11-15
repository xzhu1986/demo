package au.com.isell.rlm.importing.function.supplier;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.time.DateUtils;

import au.com.isell.rlm.importing.constant.Constants;
import au.com.isell.rlm.importing.constant.supplier.SupplierColumns;
import au.com.isell.rlm.importing.dao.StoreHelper;
import au.com.isell.rlm.module.supplier.domain.PriceBreak;
import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntry;
@Deprecated
public class SupplierPriceBreakStoreFunction extends BaseOperation<List<PriceBreak>> implements Function<List<PriceBreak>> {
	private static final long serialVersionUID = 1089526440864934362L;

	@ConstructorProperties({ "fieldDeclaration" })
	public SupplierPriceBreakStoreFunction(Fields fieldDeclaration) {
		super(fieldDeclaration);

	}

	@Override
	public void prepare(FlowProcess flowProcess, OperationCall<List<PriceBreak>> operationCall) {
		List<PriceBreak> priceBreaks = operationCall.getContext();
		if (priceBreaks != null)
			priceBreaks.clear();
		else
			operationCall.setContext(new ArrayList<PriceBreak>(Constants.MAX_OBJECTS_PER_ADD));
	}

	@Override
	public void operate(FlowProcess flowProcess, FunctionCall<List<PriceBreak>> functionCall) {
		TupleEntry arguments = functionCall.getArguments();
		functionCall.getOutputCollector().add(arguments);

		UUID breakUUID = UUID.fromString(arguments.getString(SupplierColumns.BreakUUID));
		String breakID = arguments.getString(SupplierColumns.BreakID);
		String supplierID = arguments.getString(SupplierColumns.SupplierID);
		String disabled = arguments.getString(SupplierColumns.Disabled);
		String breakName = arguments.getString(SupplierColumns.BreakName);

		PriceBreak priceBreak = new PriceBreak().init();
		priceBreak.setCreateDate(new Date());
		priceBreak.setDisabledDate(DateUtils.addYears(new Date(), 1));
		priceBreak.setFullFeedDate(DateUtils.addYears(new Date(), 1));
		priceBreak.setIdataId(Integer.parseInt(breakID));
		priceBreak.setLastCacheDate(DateUtils.addYears(new Date(), 21));
		priceBreak.setCountry(Constants.CountryMap.get(supplierID.substring(0, supplierID.length() - 4)));
		priceBreak.setName(breakName);
		if ("0".equals(disabled)) {
			priceBreak.setStatus(PriceBreak.Status.Active);
		} else {
			priceBreak.setStatus(PriceBreak.Status.Disabled);
		}
		priceBreak.setSupplierId(Integer.parseInt(supplierID));
		priceBreak.setPriceBreakId(breakUUID);

		functionCall.getContext().add(priceBreak);
		flushInputObjects(functionCall.getContext(),false);
	}

	@Override
	public void cleanup(FlowProcess flowProcess, OperationCall<List<PriceBreak>> operationCall) {
		flushInputObjects(operationCall.getContext(), true);
	}

	private void flushInputObjects(List<PriceBreak> priceBreaks, boolean force) {
		if ((force && (priceBreaks.size() > 0)) || (priceBreaks.size() >= Constants.MAX_OBJECTS_PER_ADD)) {
			StoreHelper.save(priceBreaks.toArray(new PriceBreak[priceBreaks.size()]));
			priceBreaks.clear();
		}
	}

}
