package element.sequence;

public interface PartitionChangeListener {

	public enum PartitionChangeType {NEW_PARTITION, REMOVED_PARTITION, CHANGED_PARTITION, PARTITIONS_CLEARED};
	
	public void partitionStateChanged(Partitionable source, PartitionChangeType type);
	
}
