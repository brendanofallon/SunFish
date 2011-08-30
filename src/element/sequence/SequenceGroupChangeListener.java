package element.sequence;


/**
 * An interface for items that listen for changes to sequence groups - things like adding, removing,
 * or modifying sequences. 
 * @author brendan
 *
 */
public interface SequenceGroupChangeListener {

	enum ChangeType {SequenceAdded, SequenceRemoved, SequenceModified};
	
	public void sgChanged(SequenceGroup source, ChangeType type);
}
