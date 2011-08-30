package undoRedo;

public class RedoException extends Exception {
	
	UndoableActionSource source;
	
	public RedoException(UndoableActionSource source) {
		this.source = source;
	}
}
