package element.sequence;

/**
 * These items listen for sequence change events, which are thrown when sequences of any type
 * are modified. 
 * 
 * @author brendan
 *
 */
public interface SequenceChangeListener {

	public enum SequenceEventType {LENGTH_CHANGE, NAME_CHANGE, DATA_CHANGE};
	
	public void sequenceChanged(Sequence source, SequenceEventType eventType);
}
