package au.com.isell.rlm.importing.function.supplier;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import au.com.isell.rlm.importing.constant.Constants;
import au.com.isell.rlm.importing.constant.supplier.SupplierColumns;
import au.com.isell.rlm.importing.dao.StoreHelper;
import au.com.isell.rlm.module.address.domain.GeneralAddress;
import au.com.isell.rlm.module.supplier.domain.SupplierBranch;
import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntry;

public class SupplierBranchStoreFunction extends BaseOperation<List<SupplierBranch>> implements Function<List<SupplierBranch>> {
	private static final long serialVersionUID = 1089526440864934362L;
	
	@ConstructorProperties({ "fieldDeclaration" })
	public SupplierBranchStoreFunction(Fields fieldDeclaration) {
		super(fieldDeclaration);

	}
	@Override
	public void prepare(FlowProcess flowProcess, OperationCall<List<SupplierBranch>> operationCall) {
		List<SupplierBranch> supplierBranchs = operationCall.getContext();
		if (supplierBranchs != null)
			supplierBranchs.clear();
		else
			operationCall.setContext(new ArrayList<SupplierBranch>(Constants.MAX_OBJECTS_PER_ADD));
	}
	@Override
	public void operate(FlowProcess flowProcess, FunctionCall<List<SupplierBranch>> functionCall) {
		TupleEntry arguments = functionCall.getArguments();
		functionCall.getOutputCollector().add(arguments);
		
		String SupplierID = arguments.getString(SupplierColumns.SupplierID);
		UUID supplierBranchID = UUID.fromString(arguments.getString(SupplierColumns.SupplierBranchID));
		String branchName = arguments.getString(SupplierColumns.BranchName);
		String phone = arguments.getString(SupplierColumns.Phone);
		String fax = arguments.getString(SupplierColumns.Fax);
		String address1 = arguments.getString(SupplierColumns.Address1);
		String address2 = arguments.getString(SupplierColumns.Address2);
		String address3 = arguments.getString(SupplierColumns.Address3);
		String city = arguments.getString(SupplierColumns.City);
		String state = arguments.getString(SupplierColumns.State);
		String zip = arguments.getString(SupplierColumns.Zip);
//		String country = arguments.getString(SupplierColumns.SupplierBranchCountry);
		String countryCode = arguments.getString(SupplierColumns.SupplierBranchCountryCode);
		countryCode = Constants.CountryMap.get(countryCode);
		
		String postalAddress1 = arguments.getString(SupplierColumns.PostalAddress1);
		String postalAddress2 = arguments.getString(SupplierColumns.PostalAddress2);
		String postalAddress3 = arguments.getString(SupplierColumns.PostalAddress3);
		String postalCity = arguments.getString(SupplierColumns.PostalCity);
		String postalState = arguments.getString(SupplierColumns.PostalState);
		String postalZip = arguments.getString(SupplierColumns.PostalZip);
		
		String warehouseAddress1 = arguments.getString(SupplierColumns.WarehouseAddress1);
		String warehouseAddress2 = arguments.getString(SupplierColumns.WarehouseAddress2);
		String warehouseAddress3 = arguments.getString(SupplierColumns.WarehouseAddress3);
		String warehouseCity = arguments.getString(SupplierColumns.WarehouseCity);
		String warehouseState = arguments.getString(SupplierColumns.WarehouseState);
		String warehouseZip = arguments.getString(SupplierColumns.WarehouseZip);

		SupplierBranch branch = new SupplierBranch().init();
		branch.setName(branchName);
		branch.setStatus(SupplierBranch.Status.Active);
		branch.setAddress(new GeneralAddress(address1, address2, address3, city, state,countryCode, zip));
		branch.setPostalAddress(new GeneralAddress(postalAddress1, postalAddress2, postalAddress3, postalCity, postalState, countryCode,postalZip));
		branch.setWarehouseAddress(new GeneralAddress(warehouseAddress1, warehouseAddress2, warehouseAddress3,warehouseCity,warehouseState,countryCode,warehouseZip));
		branch.setSupplierId(Integer.parseInt(SupplierID));
		branch.setBranchId(supplierBranchID);
		branch.setCountry(countryCode);
		branch.setFax(fax);
		branch.setPhone(phone);
		
		functionCall.getContext().add(branch);
		
		flushInputObjects(functionCall.getContext(),true);;
	}
	
	@Override
	public void cleanup(FlowProcess flowProcess, OperationCall<List<SupplierBranch>> operationCall) {
		flushInputObjects(operationCall.getContext(), true);
	}

	private void flushInputObjects(List<SupplierBranch> supplierBranchs, boolean force) {
		if ((force && (supplierBranchs.size() > 0)) || (supplierBranchs.size() >= Constants.MAX_OBJECTS_PER_ADD)) {
			StoreHelper.save(supplierBranchs.toArray(new SupplierBranch[supplierBranchs.size()]));
			supplierBranchs.clear();
		}
	}
}
