package undoRedo;

public class UndoException extends Exception {

	UndoableActionSource source;
	
	public UndoException(UndoableActionSource source) {
		this.source = source;
	}
	
}
