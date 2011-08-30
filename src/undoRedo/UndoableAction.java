package undoRedo;

/**
 * An object representing an action that can be undone / redone/ 
 * @author brendan
 *
 */
public abstract class UndoableAction {

	UndoableActionSource source;
	
	public UndoableAction(UndoableActionSource source) {
		this.source = source;
	}
	
	/**
	 * The object from which this action originated (a display, analyzer, etc), and to
	 * which the action will be redirected if it gets undone
	 * @return
	 */
	public UndoableActionSource getSource() {
		return source;
	}
	
	/**
	 * A very short user-readable description of the action, i.e. "Remove rows" or "Insert sequences"
	 * @return
	 */
	public abstract String getDescription();
	
}
