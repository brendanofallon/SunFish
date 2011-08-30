package element.sequence;

import java.util.Set;


public interface Partitionable {

	public int getPartitionCount();
	
	public Integer getIndexForKey(String key);
	
	public Integer getPartitionNumForSite(int site);
	
	public String getPartitionKeyForSite(int site);
	
	public Partition getPartitionForIndex(int index);
	
	public Partition getPartitionForKey(String key);
	
}
