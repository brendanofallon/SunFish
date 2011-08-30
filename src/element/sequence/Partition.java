package element.sequence;

import java.util.HashSet;
import java.util.Set;

/**
 * A list of sites that describe which sites of a sequence, or sequence group, are in a given 
 * set. This list of sites is associated with a key, as in "Partition A", which can be queried
 * to obtain a list of site indices 
 * @author brendan
 *
 */
public class Partition {

	Set<Integer> sites;
	String key;
	int size = 0;
	
	public Partition(String key) {
		sites = new HashSet<Integer>();
		this.key = key;
	}
	
	public void addSite(Integer site) {
		if (sites.add(site)) {
			size++;	
		}
	}
	
	public void removeSite(Integer site) {
		if (sites.remove(site)) {
			size--;
		}
		
	}
	
	public boolean containsSite(Integer site) {
		return sites.contains(site);
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}

	public void clear() {
		sites.clear();
	}
	
	public void addSites(int[] siteArray) {
		for(int i=0; i<siteArray.length; i++) {
			addSite(siteArray[i]);
		}
	}
	
	public int size() {
		return size;
	}
}
