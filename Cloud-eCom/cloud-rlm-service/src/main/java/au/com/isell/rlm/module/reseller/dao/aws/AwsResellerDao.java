package au.com.isell.rlm.module.reseller.dao.aws;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.search.facet.FacetBuilders.rangeFacet;
import static org.elasticsearch.search.facet.FacetBuilders.termsFacet;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.facet.AbstractFacetBuilder;
import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.facet.range.RangeFacet;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import au.com.isell.common.bean.BeanUtils;
import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.common.filter.elasticsearch.ESFilterItem;
import au.com.isell.common.filter.elasticsearch.ESFilterMaker;
import au.com.isell.common.index.elasticsearch.IndexHelper;
import au.com.isell.common.index.elasticsearch.QueryParams;
import au.com.isell.common.util.UTCDateUtils;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.dao.DAOSupport;
import au.com.isell.rlm.module.address.dao.AddressDao;
import au.com.isell.rlm.module.address.domain.AddressItem;
import au.com.isell.rlm.module.agent.dao.AgentDao;
import au.com.isell.rlm.module.agent.domain.Agent;
import au.com.isell.rlm.module.mail.domain.EmailNotifyHistory;
import au.com.isell.rlm.module.reseller.dao.ResellerDao;
import au.com.isell.rlm.module.reseller.domain.Reseller;
import au.com.isell.rlm.module.reseller.domain.Reseller.ResellerStatus;
import au.com.isell.rlm.module.reseller.domain.Reseller.ResellerType;
import au.com.isell.rlm.module.reseller.domain.ResellerSearchBean;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap.ApprovalStatus;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMapHistory;
import au.com.isell.rlm.module.reseller.domain.VersionHistory;
import au.com.isell.rlm.module.supplier.dao.SupplierDao;
import au.com.isell.rlm.module.supplier.domain.PriceBreak;
import au.com.isell.rlm.module.supplier.domain.Supplier;
import au.com.isell.rlm.module.supplier.domain.SupplierSearchBean;
import au.com.isell.rlm.module.user.dao.UserDAO;
import au.com.isell.rlm.module.user.domain.OperationHistory;
import au.com.isell.rlm.module.user.domain.OperationHistory.TargetType;
import au.com.isell.rlm.module.user.service.PermissionService;

@Repository
public class AwsResellerDao extends DAOSupport implements ResellerDao {
	private static Logger logger = LoggerFactory.getLogger(AwsResellerDao.class);
	private IndexHelper indexHelper = IndexHelper.getInstance();
	@Autowired
	private SupplierDao supplierDao;
	@Autowired
	private AddressDao addressDao;
	@Autowired
	private AgentDao agentDao;
	@Autowired
	private PermissionService permissionService;
	private UserDAO userDAO;

	@Autowired
	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}

	private ESFilterMaker resellerMaker;

	@Override
	public Reseller getReseller(int serialNo) {
		Reseller sample = new Reseller();
		sample.setSerialNo(serialNo);
		return get(sample);
	}

	public List<ResellerSupplierMap> getMaps(int serialNo, Pair<String, Boolean>[] sorts) {
		QueryBuilder queryBuilder = termQuery("serialNo", String.valueOf(serialNo));
		return indexHelper.queryBeans(ResellerSupplierMap.class, new QueryParams(queryBuilder, sorts)).getValue();
	}

	@Override
	public void indexResellerSearchBean(int... serialNos) {
		ResellerSearchBean[] beans = new ResellerSearchBean[serialNos.length];
		for (int i = 0; i < serialNos.length; i++) {
			Reseller reseller = getReseller(serialNos[i]);
			List<ResellerSupplierMap> maps = getMaps(serialNos[i], null);
			String phoneCode = getPhoneCode(reseller);
			beans[i] = new ResellerSearchBean(reseller, maps, phoneCode);
		}
		indexResellerSearchBeans(beans);
	}
	
	@Override
	public void saveOperationHistory(Reseller reseller) {
		OperationHistory his = new OperationHistory(reseller, reseller.getSerialNo().toString(), TargetType.Reseller);
		userDAO.saveOperationHistory(his);
	}

	@Override
	public void indexResellerSearchBeans(ResellerSearchBean... beans) {
		indexHelper.indexValues(beans);
	}

	private String getPhoneCode(Reseller reseller) {
		AddressItem country = addressDao.getAddressItem(reseller.getCountry());
		String phoneCode = country != null ? String.valueOf(country.getPhoneAreaCodeBind()) : "";
		return phoneCode;
	}

	@Override
	public synchronized FilterMaker getResellerSearchMaker() {
		if (resellerMaker != null)
			return resellerMaker;
		resellerMaker = new ESFilterMaker();
		resellerMaker.setType(ResellerSearchBean.class);
		return resellerMaker;
	}

	@Override
	public Pair<Long, List<ResellerSearchBean>> query(FilterItem filterItem, Pair<String, Boolean>[] sorts, Integer pageSize, Integer pageNo) {
		if (filterItem == null)
			filterItem = new ESFilterItem();
		Assert.isTrue(filterItem instanceof ESFilterItem);
		return indexHelper.queryBeans(ResellerSearchBean.class,
				new QueryParams(((ESFilterItem) filterItem).generateQueryBuilder(), sorts).withPaging(pageNo, pageSize));
	}

	/**
	 * Active Resellers,Resellers not synchronized > 1 day, Resellers being setup,Active waiting on supplier approval
	 */
	@Override
	public long[] getStatisticsByStatus() {
		long[] counts = new long[4];
		// status
		AbstractFacetBuilder[] facetBuilders = new AbstractFacetBuilder[] { termsFacet("facet").size(10).field(
				getResellerSearchMaker().getPhysicalField("status")) };

		BoolQueryBuilder queryBuilder = boolQuery().mustNot(termQuery("type", ResellerType.TestLicense.ordinal()))
				.mustNot(termQuery("type", ResellerType.Internal.ordinal())).mustNot(termQuery("type", ResellerType.Supplier.ordinal()))
				.mustNot(QueryBuilders.termQuery("status", ResellerStatus.Disabled.ordinal()));

		Facets facets = indexHelper.facet(ResellerSearchBean.class, facetBuilders, queryBuilder);

		if (facets != null) {
			TermsFacet termsFacet = (TermsFacet) facets.getFacets().get("facet");

			if (CollectionUtils.isNotEmpty(termsFacet.getEntries())) {
				for (org.elasticsearch.search.facet.terms.TermsFacet.Entry entry : termsFacet.getEntries()) {
					if (entry.getTerm().equals(String.valueOf(ResellerStatus.Active.ordinal()))) {
						counts[0] = Long.valueOf(entry.getCount());
					} else if (entry.getTerm().equals(String.valueOf(ResellerStatus.Setup.ordinal()))) {
						counts[2] = Long.valueOf(entry.getCount());
					}

				}
			}
		}
		// not synchronized > 1 day
		facetBuilders = new AbstractFacetBuilder[] { rangeFacet("facet").addUnboundedFrom(UTCDateUtils.format(DateUtils.addDays(new Date(), -1)))
				.field("lastSyncDate") };
		facets = indexHelper.facet(ResellerSearchBean.class, facetBuilders, queryBuilder);
		if (facets != null) {
			RangeFacet rangeFacet = (RangeFacet) facets.getFacets().get("facet");
			if (CollectionUtils.isNotEmpty(rangeFacet.getEntries())) {
				for (org.elasticsearch.search.facet.range.RangeFacet.Entry entry : rangeFacet.getEntries()) {
					counts[1] = entry.getCount();
				}
			}
		}
		// Active waiting on supplier approval
		counts[3] = indexHelper.count(ResellerSearchBean.class, queryBuilder.must(QueryBuilders.rangeQuery("supplierApprovalRemain").gt(0)));

		return counts;
	}

	@Override
	public Map<String, Map<Integer, Long>> getStatisticsByCountry() {
		List<Agent> agents = agentDao.searchAgents(agentDao.getAgentMaker().makeAllQuery(), null, null).getValue();
		Map<String, Map<Integer, Long>> counts = new HashMap<String, Map<Integer, Long>>();

		AbstractFacetBuilder[] facetBuilders = new AbstractFacetBuilder[] { termsFacet("facet").size(10).field("country") };
		BoolQueryBuilder boolQueryBuilder = boolQuery();
		for (int i = 0; agents != null && i < agents.size(); i++) {
			UUID agentId = agents.get(i).getAgentId();
			if (permissionService.isAllowedAgentOfResellerPerm(agentId)) {
				boolQueryBuilder = boolQueryBuilder.should(termQuery("agencyId", agentId));
			}
		}
		QueryBuilder queryBuilder = boolQuery().mustNot(termQuery("type", ResellerType.TestLicense.ordinal()))
				.mustNot(termQuery("type", ResellerType.Internal.ordinal())).mustNot(termQuery("type", ResellerType.Supplier.ordinal()))
				.must(boolQueryBuilder);

		Facets facets = indexHelper.facet(ResellerSearchBean.class, facetBuilders, queryBuilder);
		if (facets == null)
			return counts;
		TermsFacet termsFacet = (TermsFacet) facets.getFacets().get("facet");
		if (CollectionUtils.isNotEmpty(termsFacet.getEntries())) {
			for (org.elasticsearch.search.facet.terms.TermsFacet.Entry entry : termsFacet.getEntries()) {
				AbstractFacetBuilder[] subFacetBuilder = new AbstractFacetBuilder[] { termsFacet("subFacet").size(10).field("status") };
				QueryBuilder subQB = boolQuery().must(termQuery("country", entry.getTerm().toLowerCase()));
				Facets subFacets = indexHelper.facet(ResellerSearchBean.class, subFacetBuilder, boolQuery().must(queryBuilder).must(subQB));
				TermsFacet subTermsFacet = (TermsFacet) subFacets.getFacets().get("subFacet");

				if (CollectionUtils.isNotEmpty(subTermsFacet.getEntries())) {
					Map<Integer, Long> group = new HashMap<Integer, Long>();
					for (org.elasticsearch.search.facet.terms.TermsFacet.Entry subEntry : subTermsFacet.getEntries()) {
						group.put(Integer.valueOf(subEntry.getTerm()), Integer.valueOf(subEntry.getCount()).longValue());
					}
					counts.put(entry.getTerm(), group);
				}
			}
		}
		return counts;
	}

	@Override
	public void save(boolean reindex, Reseller... resellers) {
		if (resellers == null || resellers.length == 0)
			return;
		super.save(resellers);
		// if (reindex) {
		// ResellerSearchBean[] beans = new ResellerSearchBean[resellers.length];
		// for (int i = 0; i < resellers.length; i++) {
		// List<ResellerSupplierMap> maps = getMaps(resellers[i].getSerialNo(), null);
		// beans[i] = new ResellerSearchBean(resellers[i], maps,getPhoneCode(resellers[i]));
		//
		// }
		// indexHelper.indexValues(beans);
		// }
	}

	@Override
	public FilterMaker getResellerSupplierMapMaker() {
		ESFilterMaker maker = new ESFilterMaker();
		maker.setType(ResellerSupplierMap.class);
		return maker;
	}

	@Override
	public FilterMaker getResellerSupplierMapHistoryMaker() {
		ESFilterMaker maker = new ESFilterMaker();
		maker.setType(ResellerSupplierMapHistory.class);
		return maker;
	}

	@Override
	public Pair<Long, List<ResellerSupplierMap>> queryResellerSupplierMap(FilterItem filterItem, Pair<String, Boolean>[] sorts, Integer pageSize,
			Integer pageNo) {
		if (filterItem == null)
			filterItem = new ESFilterItem();
		Assert.isTrue(filterItem instanceof ESFilterItem);
		return indexHelper.queryBeans(ResellerSupplierMap.class,
				new QueryParams(((ESFilterItem) filterItem).generateQueryBuilder(), sorts).withPaging(pageNo, pageSize));
	}

	@Override
	public long getResellerSupplierMapCount(FilterItem filterItem) {
		return indexHelper.count(ResellerSupplierMap.class, ((ESFilterItem) filterItem).generateQueryBuilder());
	}

	@Override
	public void deleteResellerSupplierMap(Integer serialNo, Integer supplierId) {
		super.delete(new ResellerSupplierMap(serialNo, supplierId));
	}

	@Override
	public void deleteResellerSupplierMapHistories(ResellerSupplierMapHistory... histories) {
		super.delete(histories);
	}

	@Override
	public ResellerSupplierMap getResellerSupplierMap(Integer serialNo, Integer supplierId) {
		ResellerSupplierMap map = new ResellerSupplierMap(serialNo, supplierId);
		return super.get(map);
	}

	@Override
	public void saveResellerSupplierMap(ResellerSupplierMap... maps) {
		List<Integer> resellers = new ArrayList<Integer>();
		for (ResellerSupplierMap map : maps) {
			// Supplier supplier=supplierDao.getSupplierById(map.getSerialNo());
			// map.setSupEmail(supplier.getBillingInfo().getEmail());
			// map.setSupPhone(supplier.getBillingInfo().getPhone());
			int resellerId = queryIndexVals(map);
			if (!resellers.contains(resellerId) && resellerId != 0)
				resellers.add(resellerId);
		}
		super.save(maps);
		for (ResellerSupplierMap map : maps) {
			indexResellerSupplierMap(map.getSerialNo(), map.getSupplierId());
			supplierDao.indexSupplier(map.getSupplierId());
		}
		int[] ary = new int[resellers.size()];
		for (int i = 0; i < ary.length; i++) {
			ary[i] = resellers.get(i);
		}
		indexResellerSearchBean(ary);
	}

	@Override
	public void indexResellerSupplierMap(Integer serialNo, Integer supplierId) {
		Assert.isTrue(serialNo != null || supplierId != null, "");
		FilterMaker filterMaker = getResellerSupplierMapMaker();
		FilterItem item2 = null;
		FilterItem item3 = null;
		Reseller reseller = null;
		if (serialNo != null) {
			item2 = filterMaker.makeNameFilter("serialNo", TextMatchOption.Is, serialNo.toString());
			reseller = getReseller(serialNo);
		}
		Supplier supplier = null;
		if (supplierId != null) {
			item3 = filterMaker.makeNameFilter("supplierId", TextMatchOption.Is, supplierId.toString());
			supplier = supplierDao.getSupplierById(supplierId);
		}
		List<ResellerSupplierMap> maps = queryResellerSupplierMap(filterMaker.linkWithAnd(item2, item3), null, null, null).getValue();
		if (maps != null) {
			PriceBreak priceBreak = null;
			for (ResellerSupplierMap map : maps) {
				if (supplier != null) {
					priceBreak = supplier.getPriceBreaks().get(map.getPriceBreakId());
					if (priceBreak != null) {
						map.setBreakName(priceBreak.getName());
					}
				}
				BeanUtils.copyFromMutliSources(map, reseller, supplier);
				indexHelper.indexValues(map);
			}
		}
	}

	private int queryIndexVals(ResellerSupplierMap map) {
		Supplier supplier = supplierDao.getSupplierById(map.getSupplierId());
		map.setSupplierName(supplier.getName());
		map.setSupplierCountry(supplier.getCountry());
		Reseller reseller = getReseller(map.getSerialNo());
		if (reseller == null)
			return 0;
		map.setCompany(reseller.getCompany());
		List<PriceBreak> priceBreaks = supplierDao.getPriceBreaks(map.getSupplierId(), map.getPriceBreakId());
		if (priceBreaks.size() > 0) {
			map.setBreakName(priceBreaks.get(0).getName());
		} else {
			map.setBreakName("");
		}
		return reseller.getSerialNo();
	}

	@Override
	public Pair<Long, List<VersionHistory>> getVersionHistory(int serialNo, Integer pageNo, Integer size) {
		@SuppressWarnings("unchecked")
		Pair<String, Boolean>[] sorts = new Pair[] { new Pair<String, Boolean>("datetime", false) };
		return IndexHelper.getInstance().queryBeans(VersionHistory.class,
				new QueryParams(QueryBuilders.termQuery("serialNo", serialNo), sorts).withPaging(pageNo, size).withRoutings(new String[0]));
	}

	@Override
	public void saveVersionHistories(VersionHistory... histories) {
		List<VersionHistory> updated = new ArrayList<VersionHistory>();
		for (VersionHistory history : histories) {
			VersionHistory dbHistory = get(history);
			if (dbHistory == null)
				updated.add(history);
			else if (!dbHistory.getDatetime().equals(history.getDatetime()))
				updated.add(history);
		}
		if (updated.size() > 0) {
			super.save(updated.toArray(new VersionHistory[updated.size()]));
		}
	}

	@Override
	public void saveNotifyHistory(EmailNotifyHistory notifyHistory) {
		super.indexHelper.indexValues(notifyHistory);
	}

	@Override
	public Iterable<String> queryResellerKeys(FilterItem filterItem) {
		if (filterItem == null)
			filterItem = new ESFilterItem();
		Assert.isTrue(filterItem instanceof ESFilterItem);
		return indexHelper.iterateKeys(ResellerSearchBean.class, new QueryParams(((ESFilterItem) filterItem).generateQueryBuilder(), null));
	}

	@Override
	public Iterable<ResellerSearchBean> queryResellerBeans(FilterItem filterItem, Pair<String, Boolean>[] sorts) {
		if (filterItem == null)
			filterItem = new ESFilterItem();
		Assert.isTrue(filterItem instanceof ESFilterItem);
		return indexHelper.iterateBeans(ResellerSearchBean.class, new QueryParams(((ESFilterItem) filterItem).generateQueryBuilder(), sorts));
	}

	@Override
	public void saveResellerSupplierMapHistory(ResellerSupplierMapHistory history) {
		super.save(history);
	}

	@Override
	public List<ResellerSupplierMapHistory> getResellerSupplierMapHistories(FilterItem filterItem) {
		return indexHelper.queryBeans(ResellerSupplierMapHistory.class,
				new QueryParams(((ESFilterItem) filterItem).generateQueryBuilder(), new Pair[] { new Pair<String, Boolean>("date", false) }))
				.getValue();
	}

	@Override
	public Iterable<ResellerSupplierMap> queryResellerSupplierMap(FilterItem filterItem, Pair<String, Boolean>[] sorts) {
		if (filterItem == null)
			filterItem = new ESFilterItem();
		Assert.isTrue(filterItem instanceof ESFilterItem);
		return indexHelper.iterateBeans(ResellerSupplierMap.class, new QueryParams(((ESFilterItem) filterItem).generateQueryBuilder(), sorts));
	}

	@Override
	public Set<Integer> querySupplierIdsInRSMap(FilterItem filterItem) {
		if (filterItem == null)
			filterItem = new ESFilterItem();
		Assert.isTrue(filterItem instanceof ESFilterItem);
		// query supplier id count at most
		long count = indexHelper.count(SupplierSearchBean.class, QueryBuilders.matchAllQuery());
		// query from rsmap
		AbstractFacetBuilder[] facetBuilders = new AbstractFacetBuilder[] { termsFacet("facet").size((int) count).field(

		getResellerSupplierMapMaker().getPhysicalField("supplierId")) };
		FilterMaker maker = getResellerSupplierMapMaker();
		FilterItem item = maker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(ApprovalStatus.Approved.ordinal()));

		Set<Integer> supplierIds = new HashSet<Integer>();

		Facets facets = indexHelper.facet(ResellerSupplierMap.class, facetBuilders, ((ESFilterItem) item).generateQueryBuilder());
		if (facets != null) {
			TermsFacet termsFacet = (TermsFacet) facets.getFacets().get("facet");

			if (CollectionUtils.isNotEmpty(termsFacet.getEntries())) {
				for (org.elasticsearch.search.facet.terms.TermsFacet.Entry entry : termsFacet.getEntries()) {
					supplierIds.add(Integer.valueOf(entry.getTerm()));
				}
			}
		}

		return supplierIds;
	}

}
