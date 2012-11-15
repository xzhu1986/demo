package au.com.isell.remote.common.solr.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Frank Wu 27/09/2011
 */
@XStreamAlias("groupItem")
public class GroupItem implements Serializable {
	private static final long serialVersionUID = 2435785476444053672L;
	
	private String name;
	private int total;
	private List<GroupCount> groupCounts = new ArrayList<GroupCount>();

	
	public GroupItem() {
	}

	public GroupItem(FacetField facetField) {
		construct(facetField, facetField.getValueCount(), false);
	}

	public GroupItem(FacetField facetField, int total) {
		construct(facetField, total, true);
	}

	private void construct(FacetField facetField, int total, boolean inPaging) {
		name = facetField.getName();
		this.total = total;
		if (facetField.getValues() == null) {
			return;
		}
		for (Count c : facetField.getValues()) {
			if (c.getCount() == 0l) {
				if (!inPaging)
					this.total=this.total-1;
				continue;
			}
			groupCounts.add(new GroupCount(c));
		}
	}

	public String getName() {
		return name;
	}

	public List<GroupCount> getGroupCounts() {
		return groupCounts;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTotal() {
		return total;
	}

	@Override
	public String toString() {
		return "GroupItem [name=" + name + ", total=" + total + ", groupCounts=" + groupCounts + "]";
	}

	
}
