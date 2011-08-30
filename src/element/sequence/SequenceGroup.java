/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package element.sequence;
import java.util.*;
import java.io.File;

import element.sequence.SequenceGroup;
import element.sequence.PartitionChangeListener.PartitionChangeType;
import element.sequence.SequenceGroupChangeListener.ChangeType;

/**
 * A group of sequences. The sequences can be divided into a a set of 'partitions', with each site
 * in exactly one partition. Every site is always in a partition - if the user has set no partitions,
 * then every site is in partition 0. Partitions may be created via createPartition(...), and they
 * may be cleared, but we currently offer no support for mutating or deleting a single partition. 
 * When partitions change, they fire a partitionChangeEvent to interested parties. These
 * interested parties are usually Analyzable objects that are displaying data, which may be
 * based on the state of the partitions here. 
 * @author brendan
 */
public class SequenceGroup implements Partitionable, SequenceChangeListener {

    ArrayList<Sequence> seqs;
    String name; //A mutable, potentially non-unique identifier

    final private int persistentID; //Unmutable, unique

    ArrayList<Integer> partitions; //A list of which site is in which partition
    ArrayList<String> partitionLabels; //The labels of each partition number
    ArrayList<PartitionChangeListener> partitionListeners; //Things that listen for partition events
    
    ArrayList<SequenceGroupChangeListener> sgListeners; //Things that listen for sg change events
    
    char[] partitionLetters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'W', 'X', 'Y', 'Z'};


    boolean haplotypesKnown = false;
    HashMap<String, Integer> hapMap; //A map of sequence names to haplotypes
    
    //This flag suppresses firing of SGChangeEvents when true, for the following reason:
    //Operations on Sequences by default generate SGChange events, which in turn can trigger long operations
    //Therefore, for operations that affect all Sequences we'd like to suppress firing of SGChangeEvents 
    //for each sequence and then manually fire our own when all operations have completed. 
    private boolean suppressEvents = false;
    
    public SequenceGroup() {
        Random rng = new Random();
        persistentID = rng.nextInt();
        seqs = new ArrayList<Sequence>();
        partitions = new ArrayList<Integer>();
        partitionLabels = new ArrayList<String>();
        partitionListeners = new ArrayList<PartitionChangeListener>();
        sgListeners = new ArrayList<SequenceGroupChangeListener>(3);
        clearPartitions();
    }

    public SequenceGroup(String name) {
        this.name = name;
        Random rng = new Random();
        persistentID = rng.nextInt();
        seqs = new ArrayList<Sequence>();
        partitions = new ArrayList<Integer>();
        partitionLabels = new ArrayList<String>();
        partitionListeners = new ArrayList<PartitionChangeListener>();
        sgListeners = new ArrayList<SequenceGroupChangeListener>(3);
        clearPartitions();
    }
    

    /**
     * Return the haplotype index for the sequence with the given name
     * @param name
     * @return
     */
    public Integer getHaplotypeForName(String name) {
    	if (!haplotypesKnown)
    		calculateHaplotypes();
    	return hapMap.get(name);
    }
    
    public Integer getHaplotypeForRow(int row) {
    	return getHaplotypeForName(seqs.get(row).getName());
    }
    
    private void calculateHaplotypes() {
    	if (hapMap == null) 
    		hapMap = new HashMap<String, Integer>();
    	hapMap.clear();
    	
    	int count = 0;
    	for(int i=0; i<seqs.size(); i++) {
    		int index = hapMapContains(seqs.get(i));
    		if (index<0) {
    			hapMap.put(seqs.get(i).getName(), count);
    			count++;
    		}
    		else {
    			hapMap.put(seqs.get(i).getName(), index);
    		}
    	}
		haplotypesKnown = true;
	}
    
    private int hapMapContains(Sequence seq) {
    	for(String name : hapMap.keySet()) {
    		Sequence hapSeq = getSequenceForName(name);
    		if (hapSeq.toString().equals(seq.toString()))
    			return hapMap.get(name);
    	}
    	return -1;
    }
    
	public int getPersistentId() {
        return persistentID;
    }

    public void setName(String newName) {
        this.name = newName;
        fireSGChanged(ChangeType.SequenceModified);
    }

    public String getName() {
        return name;
    }

    public boolean contains(String name) {
        for( Sequence seq : seqs ) {
            if (seq.getName().equals(name) )
                return true;
        }
        return false;
    }

    public Sequence getSequenceForName(String name) {
    	for(Sequence seq : seqs) {
    		if (seq.getName().equals(name)) 
    			return seq;
    	}
    	return null;
    }
    
	public int getMinSeqLength() {
        if (seqs==null)
            return 0;
        else {
            int max = Integer.MAX_VALUE;
            for( Sequence seq : seqs ) {
                if (max>seq.toString().length() )
                    max = seq.toString().length();
            }
            return max;
        }
    }

    public int getMaxSeqLength() {
        if (seqs==null)
            return 0;
        else {
            int max = 0;
            for( Sequence seq : seqs ) {
                if (max<seq.length() )
                    max = seq.length();
            }
            return max;
        }
    }


	 public int getMaxNameLength() {
        if (seqs==null)
            return 0;
        else {
            int max = 0;
			for(int i=0; i<seqs.size(); i++)  {
                if (max<seqs.get(i).getName().length() )
                    max = seqs.get(i).getName().length();

            }
            return max;
        }
    }
	 
	 public String getLongestName() {
		 String maxStr = "";
	        if (seqs==null)
	            return "";
	        else {
	            int max = 0;
				for(int i=0; i<seqs.size(); i++)  {
	                if (max<seqs.get(i).getName().length() ) {
	                    max = seqs.get(i).getName().length();
	                    maxStr = seqs.get(i).getName();
	                }
	            }
	            return maxStr;
	        }
	    }

	 /**
	  * Returns true if any sequence contains a Sequence.GAP or a ' ' (space) character at the given position
	  * @param site
	  * @return
	  */
	 public boolean hasGap(int site) {
		 if (site>getMaxSeqLength())
			 return false;
		 
		 if (site>=getMinSeqLength()) //If some seqs are longer than others, and we're looking past the end of 
			 return true;			 //the shortest ones, that's a gap (otherwise, there can be no gaps at ends)
		 
		 boolean gap = seqs.get(0).isGap(site);
		 for(int i=1; i<seqs.size() && ! gap; i++)
			 if ( seqs.get(i).isGap(site))
				 gap = true;
		 
		 return gap;
	 }
	 
	 /**
	  * Returns true if any sequence contains an Sequence.UNKNOWN character at the given position
	  * @param site
	  * @return
	  */
	 public boolean hasUnknown(int site) {
		 if (site>getMaxSeqLength())
			 return false;
		 		 
		 boolean gap = seqs.get(0).isUnknown(site);
		 for(int i=1; i<seqs.size() && ! gap; i++)
			 if ( seqs.get(i).isUnknown(site))
				 gap = true;
		 
		 return gap;
	 }

	 /**
	  * Add the sequence to this sequence group. We also add this SequenceGroup as a
	  * change listener to the sequence. 
	  * @param s
	  */
    public void add(Sequence s) {
        if (s!=null) {
        	if (seqs == null)
        		seqs = new ArrayList<Sequence>();
            seqs.add(s);
            revalidatePartitions();
        }
        
        s.addSequenceChangeListener(this);
        fireSGChanged(ChangeType.SequenceAdded);
    }

    public Sequence get(int i) {
        if (i<seqs.size())
            return seqs.get(i);
        else
            return null;
    }

    public int size() {
        return seqs.size();
    }

     public String toString() {
         return "SequenceGroup with " + seqs.size() + " sequences max length length " + this.getMaxSeqLength();
     }

     public Vector<String> getNames() {
         Vector<String> names = new Vector<String>();
         for(Sequence sq : seqs)
             names.add(sq.getName());

         return names;
     }

     /**
      * Return all sequences as a list
      * @return
      */
      public List<Sequence> getSequences() {
         return seqs;
     }

	 public char[] getColumn(int i) {
		 char[] col = new char[seqs.size()];
		 for(int j=0; j<seqs.size(); j++) {
			 if (seqs.get(j).length()<i)
				col[j] = '-';
			 else
				col[j] = seqs.get(j).at(i);
		 }
		 return col;
	 }

	 public void setSequences(SequenceGroup source) {
		 seqs.clear();
		 for(Sequence seq : source.getSequences()) {
			 seqs.add(seq);
		 }
		 haplotypesKnown = false;
		 revalidatePartitions();
		 fireSGChanged(ChangeType.SequenceAdded);
	 }
	 
	 /**
	  * Obtain a complete clone of this sequence group, with partitions, haplotypes, listeners, etc. preserved
	  */
	 public SequenceGroup clone() {
		 SequenceGroup cloned = new SequenceGroup(name);
		 for(Sequence sq : seqs) {
            cloned.add( sq.clone() );
		 }
		 
		 cloned.partitionLabels = (ArrayList<String>) partitionLabels.clone();
		 cloned.partitions = (ArrayList<Integer>) partitions.clone();
		 cloned.partitionListeners = (ArrayList<PartitionChangeListener>) partitionListeners.clone();
		 cloned.haplotypesKnown = haplotypesKnown;
		 cloned.sgListeners = (ArrayList<SequenceGroupChangeListener>) sgListeners.clone();
		 if (hapMap != null)
			 cloned.hapMap = (HashMap<String, Integer>) hapMap.clone();

		 return cloned;
	 }
	 
	 public SequenceGroup getCols(int[] cols) {
		 SequenceGroup newSG = new SequenceGroup();
		 for(Sequence sq : seqs) {
			 newSG.add( sq.getCols(cols));
		 }
		 return newSG;
	 }
	 
	 public SequenceGroup getRows(int[] rows) {
		 SequenceGroup newSG = new SequenceGroup();
		 int i;
		 for(i=0; i<rows.length; i++) {
			 newSG.add( seqs.get(rows[i]).clone());
		 }
		 return newSG;
	 }
	 
	 public void removeRows(int[] rows) {
		 int i;
		 int dif = 0;
		 java.util.Arrays.sort(rows);
		 for(i=0; i<rows.length; i++) {
			 if (rows[i]-dif < 0) {
				 System.err.println("Uh-oh, dif thing didn't work: " + dif);
				 for(int j=0; j<rows.length; j++) { 
					 System.err.println(rows[j]); 
				 }
			 }
			 else {
				 System.out.println("Removing item #" + (rows[i]-dif) + " name : " + seqs.get(rows[i]-dif).getName());
				 Sequence removed = seqs.remove(rows[i]-dif);
				 removed.removeSequenceChangeListener(this);
				 dif++;
			 }
		 }
		 fireSGChanged(ChangeType.SequenceRemoved);
	 }
	 
	 /**
	  * Remove the specified columns from all sequences. This doesn't fire any sequence change events,
	  * but does fire a partitionChangeEvent if a partition is removed completely. 
	  * @param cols
	  */
	 public void removeCols(int[] cols) {
		 int beforeCount = getPartitionCount();
		 
		 Arrays.sort(cols);
		 
		 suppressEvents = true;
		 for(Sequence sq : seqs) {
			 sq.removeCols(cols); 
		 }
		 
		 suppressEvents = false;
		 for(int i=cols.length-1; i>=0; i--) {
			 if (cols[i] < partitions.size())
				 partitions.remove(cols[i]);
		 }
		 
		 revalidatePartitions();
		 
		 int afterCount = getPartitionCount();
		 
		 if (beforeCount > afterCount)
			 firePartitionChangeEvent(PartitionChangeType.REMOVED_PARTITION);
		 
		 fireSGChanged(ChangeType.SequenceModified);
		 haplotypesKnown = false;
	 }


	 
	    /**
	     * Adjusts the partition array to match the length of the longest sequence by adding
	     * elements to or removing elements from the end of the partitions array. This 
	     */
	    private void revalidatePartitions() {
	    	int max = getMaxSeqLength();
	    	while(partitions.size()<max) {
	    		partitions.add(0);
	    	}
	    	while(partitions.size()>max) {
	    		partitions.remove(partitions.size()-1);
	    	}

	    	//Now check to see if we have deleted the last element in any partition. This is expensive,
	    	//but will probably only happen when we delete columns from this group.
	    	List<String> keysToRemove = new ArrayList<String>(10);
	    	for(String key : partitionLabels) {
	    		Partition part = getPartitionForKey(key);
	    		if (part.size()==0) {
	    			keysToRemove.add(key);
	    		}
	    	}
	    	
	    	for(String key : keysToRemove) {
	    		partitionLabels.remove(key);
	    	}
	    	
	    }

	    public void clearPartitions() {
	    	partitionLabels.clear();
	    	partitions.clear();
	    	partitionLabels.add("Default partition");
	    	int max = getMaxSeqLength();
	    	for(int i=0; i<max; i++) {
	    		partitions.add(0);
	    	}
	    	firePartitionChangeEvent(PartitionChangeType.PARTITIONS_CLEARED);
	    }
	 
	 /**
	  * Create a new partition from the given sites. 
	  * @param sites
	  */
	 public void createPartition(int[] sites) {
		 //Ughh. if we somehow try to 'create' partition #0, such that partitionLabels.size()==0 when we
		 //get here, this will break. I don't think this will ever happen, though...
		 String name = "Partition " + String.valueOf(partitionLetters[(partitionLabels.size()-1)%partitionLetters.length]);
		 
		 int index = partitionLabels.size();
		 partitionLabels.add(name);
		 
		 for(int i=0; i<sites.length; i++) {
			 partitions.set(sites[i], index);
		 }
		 
		 firePartitionChangeEvent(PartitionChangeType.NEW_PARTITION);
	 }

	 
	 public void addPartitionListener(PartitionChangeListener listener) {
		 partitionListeners.add(listener);
	 }
	 
	 public void removePartitionListener(PartitionChangeListener listener) {
		 partitionListeners.remove(listener);
	 }
	 
	 public void firePartitionChangeEvent(PartitionChangeType type) {
		 //This avoids comodification issues if listeners would like add or remove themselves
		 //as listeners as a result of this change.
		 Object[] currentListeners = partitionListeners.toArray();
		 for(int i=0; i<currentListeners.length; i++) {
			 PartitionChangeListener pcl = (PartitionChangeListener)currentListeners[i];
			 pcl.partitionStateChanged(this, type);
		 }

	 }
	 
	 ///////////////////// Partitionable implementation //////////////////////
	 
	public Integer getIndexForKey(String key) {
		for(int i=0; i<partitionLabels.size(); i++) {
			if (partitionLabels.get(i).equals(key))
				return i;
		}
		return -1;
	}

	public String getPartitionKeyForIndex(int index) {
		if (index<partitionLabels.size()) {
			return partitionLabels.get(index);
		}
		else
			return null;
	}

	public int getPartitionCount() {
		return partitionLabels.size();
	}


	public String getPartitionKeyForSite(int site) {
		int index = getPartitionNumForSite(site);
		return partitionLabels.get(index);
	}

	
	public Integer getPartitionNumForSite(int site) {
		if (site<partitions.size())
			return partitions.get(site);
		else
			return null;
	}

	/**
	 * Returns the number of sites in the given partition (that is, whose index is equal to partitionIndex)
	 * @param partitionIndex
	 * @return
	 */
	public int getPartitionSiteCount(int partitionIndex) {
		int count = 0;
		for(int i=0; i<partitions.size(); i++) {
			if (partitions.get(i)==partitionIndex)
				count++;
		}
		return count;
	}

	public Partition getPartitionForIndex(int index) {
		Partition part = null;
		if (index<partitionLabels.size()) {
			part = new Partition(partitionLabels.get(index));
			for(int i=0; i<partitions.size(); i++) {
				if ( partitions.get(i)==index)
					part.addSite(i);
			}
		}

		return part;
	}


	public Partition getPartitionForKey(String key) {
		int index = partitionLabels.indexOf(key);
		if (index>-1) {
			return getPartitionForIndex(index);
		}
		else {
			return null;
		}
	}

	
	/**
	 * Called when a sequence has changed, we need to do is update the partitions array appropriately
	 * and notify all SG change listeners that sequences have been modifed (by firing an SGChanged event)
	 */
	public void sequenceChanged(Sequence source, SequenceEventType eventType) {
		if (eventType==SequenceEventType.LENGTH_CHANGE) {
			revalidatePartitions();
		}
		haplotypesKnown = false;
		if (!suppressEvents)
			fireSGChanged(ChangeType.SequenceModified);
	}
	 	 
	
	/**
	 * Add a new listener to be notified of sequence group changes
	 * @param newListener
	 */
	public void addSGChangeListener(SequenceGroupChangeListener newListener) {
		if (! sgListeners.contains(newListener)) 
			sgListeners.add(newListener);
	}
	
	/**
	 * Remove the given listener from the list of sg change listeners
	 * @param listener
	 */
	public void removeSGChangeListener(SequenceGroupChangeListener listener) { 
			sgListeners.remove(listener);
	}
	
	/**
	 * Fire a new change event to all listeners
	 * @param type
	 */
	protected void fireSGChanged(SequenceGroupChangeListener.ChangeType type) {
		for(SequenceGroupChangeListener l : sgListeners) {
			l.sgChanged(this, type);
			//System.out.println("Firing SG change event to listener: " + l);
		}
	}
	 
 }
