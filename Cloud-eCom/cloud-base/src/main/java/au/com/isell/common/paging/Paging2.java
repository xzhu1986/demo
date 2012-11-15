package au.com.isell.common.paging;

import java.util.ArrayList;
import java.util.List;

import au.com.isell.remote.common.model.Pair;

/**
 * @author Frank Wu 25/05/2011
 */
public class Paging2 {
	// pre setting
	private int displayLength = 8;
	// init value
	private String url;
	private int pageSize;
	private int current;
	private int totalSize;
	// output
	List<Pair<Integer, String>> navList = new ArrayList<Pair<Integer, String>>();
	Pair<Integer, String> previous = null;
	Pair<Integer, String> next = null;

	// return url : ? page=
	public Paging2(String url, int currentPage, int pageSize, int totalSize) {
		this.url = url;
		this.pageSize = pageSize;
		this.current = currentPage;
		this.totalSize = totalSize;
		calculate();
	}

	private void calculate() {
		int first;
		int last;
		int totalPage;
		int displayStart;
		int displayEnd;
		int displayHalfLen = displayLength/2;
		// calcalate
		totalPage = totalSize/pageSize + (totalSize%pageSize>0 ? 1 : 0);
		first = 1;
		last = totalPage;
		//displayStart & displayEnd
		displayStart = (current-displayHalfLen)>=first ? current-displayHalfLen : first;
		displayEnd = (displayStart+displayLength-1) <= last ? displayStart+displayLength-1 : last;
		//correct
		if(displayEnd==last && displayStart>first){
			displayStart = displayEnd-displayLength+1 >= first ? displayEnd-displayLength+1 : first;
		}
		// format url
		formatUrl();
		//navlist
		for(int i=displayStart;i<=displayEnd;i++){
			navList.add(new Pair<Integer, String>(i, String.format(url, i)));
		}
		//previous & next
		if(displayStart<current){
			previous = new Pair<Integer, String>(current-1, String.format(url, current-1));
		}
		if(displayEnd>current){
			next = new Pair<Integer, String>(current+1, String.format(url, current+1));
		}
	}

	private void formatUrl() {
		url = url.contains("?") ? url : url + "?";
		url = url.endsWith("?") ? url : url + "&";
		url += "pageNo=%s";
	}


	public List<Pair<Integer, String>> getNavList() {
		return navList;
	}

	public Pair<Integer, String> getPrevious() {
		return previous;
	}

	public Pair<Integer, String> getNext() {
		return next;
	}
	
	public int getCurrent() {
		return current;
	}

	public static void main(String[] args) {
		for(int i=1;i<=21;i++){
			Paging2 paging = new Paging2("www.test.com?p1=2", i, 20, 401);
			System.out.println("current page:"+i);
			System.out.println(paging.getPrevious());
			System.out.println(paging.getNext());
			System.out.println(paging.getNavList());
			System.out.println();
		}
	}
	
}
