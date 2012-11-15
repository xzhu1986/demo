package au.com.isell.remote.common.solr.vo;

import java.io.Serializable;

import org.apache.solr.client.solrj.response.FacetField.Count;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Frank Wu 27/09/2011
 */
@XStreamAlias("groupCount")
public class GroupCount implements Serializable {
	private static final long serialVersionUID = -5868012484807960126L;
	
	private String name = null;
	private long count = 0;
	private Object sortVal;

	
	
	public GroupCount() {
		super();
		
	}

	public GroupCount(Count count) {
//		this.name = SolrFieldEncoder.decodeWhiteSpace(count.getName());
		this.name = count.getName();
		this.count = count.getCount();
	}

	public String getName() {
		return name;
	}

	public long getCount() {
		return count;
	}

	public Object getSortVal() {
		return sortVal;
	}

	public void setSortVal(Object sortVal) {
		this.sortVal = sortVal;
	}

	
}
