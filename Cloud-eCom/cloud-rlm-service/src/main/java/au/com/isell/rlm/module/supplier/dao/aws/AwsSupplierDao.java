package au.com.isell.rlm.module.supplier.dao.aws;

import static org.elasticsearch.search.facet.FacetBuilders.termsFacet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.facet.AbstractFacetBuilder;
import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.filter.FieldMapper;
import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.common.filter.elasticsearch.ESFilterItem;
import au.com.isell.common.filter.elasticsearch.ESFilterMaker;
import au.com.isell.common.index.elasticsearch.IndexHelper;
import au.com.isell.common.index.elasticsearch.QueryParams;
import au.com.isell.common.storage.StorageException;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.dao.DAOSupport;
import au.com.isell.rlm.module.reseller.dao.ResellerDao;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap.ApprovalStatus;
import au.com.isell.rlm.module.supplier.dao.SupplierDao;
import au.com.isell.rlm.module.supplier.domain.PriceBreak;
import au.com.isell.rlm.module.supplier.domain.Supplier;
import au.com.isell.rlm.module.supplier.domain.SupplierBranch;
import au.com.isell.rlm.module.supplier.domain.SupplierSearchBean;
import au.com.isell.rlm.module.supplier.domain.SupplierUpdateExecute;
import au.com.isell.rlm.module.supplier.domain.SupplierUser;
import au.com.isell.rlm.module.user.dao.UserDAO;
import au.com.isell.rlm.module.user.domain.OperationHistory;
import au.com.isell.rlm.module.user.domain.OperationHistory.TargetType;

@Repository
public class AwsSupplierDao extends DAOSupport implements SupplierDao {
	private IndexHelper indexHelper = IndexHelper.getInstance();

	private static FieldMapper supplierMapper = new FieldMapper();

	private static FieldMapper branchMapper = new FieldMapper();

	private static FieldMapper priceBreakMapper = new FieldMapper();

	private static FieldMapper priceUpdatesMapper = new FieldMapper();

	private ESFilterMaker branchMaker;

	private ResellerDao resellerDao;
	private UserDAO userDAO;

	@Autowired
	public void setResellerDao(ResellerDao resellerDao) {
		this.resellerDao = resellerDao;
	}

	@Autowired
	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}

	@Override
	public Supplier getSupplierById(int supplierId) {
		Supplier supplier = new Supplier();
		supplier.setSupplierId(supplierId);
		return get(supplier);
	}

	@Override
	public long getResellersCountByPriceBreakId(FilterItem filterItem) {
		if (filterItem == null)
			filterItem = new ESFilterItem();
		Assert.isTrue(filterItem instanceof ESFilterItem);
		return indexHelper.count(ResellerSupplierMap.class, ((ESFilterItem) filterItem).generateQueryBuilder());
	}

	@Override
	public List<PriceBreak> listPriceBreak(int supplierId, Pair<String, Boolean>[] sorts) throws StorageException {
		FilterMaker maker = getPriceBreakMaker();
		FilterItem item = maker.makeNameFilter("supplierId", TextMatchOption.Is, String.valueOf(supplierId));
		return queryPriceBreaks(item, sorts);
	}

	@Override
	public Map<String, Long> getStatisticsByActiveSupplier() {
		Map<String, Long> result = new HashMap<String, Long>();

		AbstractFacetBuilder[] facetBuilders = new AbstractFacetBuilder[] { termsFacet("facet").size(10).field("country") };
		Facets facets = indexHelper.facet(SupplierSearchBean.class, facetBuilders,
				QueryBuilders.termQuery("status", Supplier.Status.Active.ordinal()));
		if (facets == null)
			return result;
		TermsFacet termsFacet = (TermsFacet) facets.getFacets().get("facet");
		if (CollectionUtils.isNotEmpty(termsFacet.getEntries())) {
			for (org.elasticsearch.search.facet.terms.TermsFacet.Entry entry : termsFacet.getEntries()) {
				result.put(entry.getTerm(), ((Integer) entry.count()).longValue());
			}
		}
		return result;
	}

	@Override
	public Map<String, Long> getStatisticsByActivePriceBreak() {
		Map<String, Long> result = new HashMap<String, Long>();

		AbstractFacetBuilder[] facetBuilders = new AbstractFacetBuilder[] { termsFacet("facet").size(10).field("country") };
		Facets facets = indexHelper.facet(PriceBreak.class, facetBuilders, QueryBuilders.termQuery("status", PriceBreak.Status.Active.ordinal()));
		if (facets == null)
			return result;
		TermsFacet termsFacet = (TermsFacet) facets.getFacets().get("facet");
		if (CollectionUtils.isNotEmpty(termsFacet.getEntries())) {
			for (org.elasticsearch.search.facet.terms.TermsFacet.Entry entry : termsFacet.getEntries()) {
				result.put(entry.getTerm(), ((Integer) entry.count()).longValue());
			}
		}
		return result;
	}

	@Override
	public SupplierUser getSupplierUserBySupplierId(int supplierId) {
		SupplierUser supplierUser = new SupplierUser();
		supplierUser.setSupplierId(supplierId);
		return get(supplierUser);
	}

	@Override
	public void indexSupplier(int supplierId) {
		Supplier supplier = getSupplierById(supplierId);
		SupplierBranch supplierBranch = getBranch(supplierId, supplier.getDefaultBranchId());
		FilterMaker maker = resellerDao.getResellerSupplierMapMaker();
		FilterItem item1 = maker.makeNameFilter("supplierId", TextMatchOption.Is, String.valueOf(supplierId));
		FilterItem item2 = maker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(ApprovalStatus.Approved.ordinal()));
		int count = (int) resellerDao.getResellerSupplierMapCount(maker.linkWithAnd(item1, item2));
		indexHelper.indexValues(new SupplierSearchBean(supplier, supplierBranch != null ? supplierBranch.getAddress() : null, count));

		// FilterMaker maker = getPriceBreakMaker();
		// List<PriceBreak> priceBreaks=queryPriceBreaks(maker.makeNameFilter("supplierId",
		// TextMatchOption.Is,String.valueOf(supplierId)), null);
		for (PriceBreak pb : supplier.getPriceBreaks().values()) {
			if (!supplier.getCountry().equals(pb.getCountry())) {
				pb.setCountry(supplier.getCountry());
				indexHelper.indexValues(pb);
			}
		}
	}

	@Override
	public FilterMaker getSupplierSearchMaker() {
		ESFilterMaker maker = new ESFilterMaker();
		maker.setType(SupplierSearchBean.class);
		maker.setFieldMapper(supplierMapper);
		return maker;
	}

	@Override
	public Pair<Long, List<SupplierSearchBean>> query(FilterItem filterItem, Pair<String, Boolean>[] sorts, Integer pageSize, Integer pageNo) {
		if (filterItem == null)
			filterItem = new ESFilterItem();
		Assert.isTrue(filterItem instanceof ESFilterItem);
		return indexHelper.queryBeans(SupplierSearchBean.class,
				new QueryParams(((ESFilterItem) filterItem).generateQueryBuilder(), sorts).withPaging(pageNo, pageSize));
	}

	@Override
	public FilterMaker getPriceBreakMaker() {
		ESFilterMaker maker = new ESFilterMaker();
		maker.setType(PriceBreak.class);
		maker.setFieldMapper(priceBreakMapper);
		return maker;
	}

	@Override
	public Pair<Long, List<PriceBreak>> queryPriceBreak(FilterItem filterItem, Pair<String, Boolean>[] sorts, Integer pageSize, Integer pageNo) {
		if (filterItem == null)
			filterItem = new ESFilterItem();
		Assert.isTrue(filterItem instanceof ESFilterItem);
		return indexHelper.queryBeans(PriceBreak.class,
				new QueryParams(((ESFilterItem) filterItem).generateQueryBuilder(), sorts).withPaging(pageNo, pageSize));
	}

	@Override
	public List<PriceBreak> getPriceBreaks(int supplierId, UUID... priceBreakIds) {
		Supplier supplier = getSupplierById(supplierId);
		Map<UUID, PriceBreak> prMap = supplier.getPriceBreaks();
		List<PriceBreak> r = new ArrayList();
		for (UUID id : priceBreakIds) {
			PriceBreak priceBreak = prMap.get(id);
			if (priceBreak != null)
				r.add(priceBreak);
		}
		return r;
	}

	@Override
	public List<PriceBreak> queryPriceBreaks(FilterItem filterItem, Pair<String, Boolean>[] sorts) {
		Pair<Long, List<PriceBreak>> result = queryPriceBreak(filterItem, sorts, null, null);
		if (result == null)
			return null;
		return result.getValue();
	}

	@Override
	public Pair<Long, List<SupplierBranch>> queryBranch(FilterItem filterItem, Pair<String, Boolean>[] sorts, Integer pageSize, Integer pageNo) {
		if (filterItem == null)
			filterItem = new ESFilterItem();
		Assert.isTrue(filterItem instanceof ESFilterItem);
		return indexHelper.queryBeans(SupplierBranch.class,
				new QueryParams(((ESFilterItem) filterItem).generateQueryBuilder(), sorts).withPaging(pageNo, pageSize));
	}

	@Override
	public synchronized FilterMaker getBranchMaker() {
		if (branchMaker != null)
			return branchMaker;
		branchMaker = new ESFilterMaker();
		branchMaker.setType(SupplierBranch.class);
		branchMaker.setFieldMapper(branchMapper);
		return branchMaker;
	}

	@Override
	public SupplierBranch getBranch(int supplierId, UUID branchId) {
		if (branchId == null)
			return null;
		SupplierBranch branch = new SupplierBranch();
		branch.setSupplierId(supplierId);
		branch.setBranchId(branchId);
		return get(branch);
	}

	@Override
	public void saveOrUpdateSupplier(Supplier supplier) {
		super.save(supplier);
	}

	@Override
	public void saveBranch(SupplierBranch branch) {
		super.save(branch);
		indexSupplier(branch.getSupplierId());
	}

	@Override
	public void savePriceBreak(PriceBreak priceBreak) {
		Supplier supplier = getSupplierById(priceBreak.getSupplierId());
		priceBreak.setCountry(getSupplierById(priceBreak.getSupplierId()).getCountry());
		supplier.addPriceBreak(priceBreak);
		this.saveOrUpdateSupplier(supplier);
		super.indexHelper.indexValues(priceBreak);
	}

	@Override
	public void deleteBranch(int supplierId, String branchId) {
		SupplierBranch branch = getBranch(supplierId, UUID.fromString(branchId));
		branch.setDeleted(true);
		this.saveBranch(branch);
	}

	@Override
	public FilterMaker getSupplierUpdatesMaker() {
		ESFilterMaker maker = new ESFilterMaker();
		maker.setType(SupplierUpdateExecute.class);
		maker.setFieldMapper(priceUpdatesMapper);
		return maker;
	}

	@Override
	public Pair<Long, List<SupplierUpdateExecute>> querySupplierUpdates(FilterItem filterItem, Pair<String, Boolean>[] sorts, Integer pageSize,
			Integer pageNo) {
		if (filterItem == null)
			filterItem = new ESFilterItem();
		Assert.isTrue(filterItem instanceof ESFilterItem);
		return indexHelper.queryBeans(SupplierUpdateExecute.class,
				new QueryParams(((ESFilterItem) filterItem).generateQueryBuilder(), sorts).withPaging(pageNo, pageSize));
	}

	@Override
	public void saveSupplierUpdate(Supplier supplier, SupplierUpdateExecute supplierUpdateExecute) {
		Supplier dbSup = get(supplier);
		BeanUtils.copyPropsExcludeNull(dbSup, supplier);
		this.save(dbSup);
		indexHelper.indexValues(supplierUpdateExecute);
	}

	@Override
	public SupplierUpdateExecute getSupplierUpdateExecuteByScheduleId(String scheduleId) {
		FilterMaker maker = getSupplierUpdatesMaker();
		FilterItem item = maker.makeNameFilter("scheduleId", TextMatchOption.Is, scheduleId);
		Pair<Long, List<SupplierUpdateExecute>> r = indexHelper.queryBeans(SupplierUpdateExecute.class,
				new QueryParams(((ESFilterItem) item).generateQueryBuilder(), null));
		List<SupplierUpdateExecute> rList = r.getValue();
		if (rList != null && rList.size() > 0) {
			return rList.get(0);
		}
		return null;
	}

	@Override
	public void saveSupplierUpdateExecute(SupplierUpdateExecute supplierUpdateExecute) {
		indexHelper.indexValues(supplierUpdateExecute);
	}

	@Override
	public void deletePriceBreakIndex(PriceBreak priceBreak) {
		indexHelper.deleteByObj(priceBreak);
	}

	@Override
	public Iterable<SupplierSearchBean> querySupplierBeans(FilterItem filterItem, Pair<String, Boolean>[] sorts) {
		if (filterItem == null)
			filterItem = new ESFilterItem();
		Assert.isTrue(filterItem instanceof ESFilterItem);
		return indexHelper.iterateBeans(SupplierSearchBean.class, new QueryParams(((ESFilterItem) filterItem).generateQueryBuilder(), sorts));
	}
	@Override
	public Iterable<Map> querySupplierMaps(FilterItem filterItem, Pair<String, Boolean>[] sorts) {
		if (filterItem == null)
			filterItem = new ESFilterItem();
		Assert.isTrue(filterItem instanceof ESFilterItem); 
		QueryParams params = new QueryParams(((ESFilterItem) filterItem).generateQueryBuilder(), sorts);
		return indexHelper.iterateMaps(SupplierSearchBean.class, params);
	}
	
	@Override
	public Pair<Long, List<SupplierSearchBean>> querySupplierMapsWithExcludeIds(FilterItem filterItem, Set<Integer> excludeIds, Pair<String, Boolean>[] sorts, Integer pageSize,
			Integer pageNo) {
		if (filterItem == null)
			filterItem = new ESFilterItem();
		Assert.isTrue(filterItem instanceof ESFilterItem);
		QueryParams params = new QueryParams(((ESFilterItem) filterItem).generateQueryBuilder(), sorts);
		String[] ids = new String[excludeIds.size()];
		int i = 0;
		for (Integer id : excludeIds) {
			ids[i] = String.valueOf(id);
			i++;
		}
		params.setFilterBuilder(FilterBuilders.notFilter(FilterBuilders.idsFilter().addIds(ids)));
		params.withPaging(pageNo, pageSize);
		return indexHelper.queryBeans(SupplierSearchBean.class, params);
	}

	@Override
	public void saveOperationHistory(Supplier supplier) {
		OperationHistory his = new OperationHistory(supplier, supplier.getSupplierId().toString(), TargetType.Supplier);
		userDAO.saveOperationHistory(his);
	}

	@Override
	public int[] statisticRsMap4ResellerPortal(int serialNo) {
		int[] r=new int[2];

		AbstractFacetBuilder[] facetBuilders = new AbstractFacetBuilder[] { termsFacet("facet").size(10).field("status") };
		Facets facets = indexHelper.facet(ResellerSupplierMap.class, facetBuilders, QueryBuilders.termQuery("serialNo", serialNo));
		
		if (facets == null)
			return r;
		TermsFacet termsFacet = (TermsFacet) facets.getFacets().get("facet");
		if (CollectionUtils.isNotEmpty(termsFacet.getEntries())) {
			for (org.elasticsearch.search.facet.terms.TermsFacet.Entry entry : termsFacet.getEntries()) {
				if(entry.getTerm().equals(String.valueOf(ApprovalStatus.Approved.ordinal())))
					r[0]=entry.count();
				else if(entry.getTerm().equals(String.valueOf(ApprovalStatus.Pending.ordinal())))
					r[1]=entry.count();
			}
		}
		return r;
	}


}
