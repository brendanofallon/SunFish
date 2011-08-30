package undoRedo;

public interface UndoableActionSource {

	public UndoableAction undoAction(UndoableAction action) throws UndoException;
	
	public void redoAction(UndoableAction action) throws RedoException;
	
	public UndoRedoManager getManager();
	
}
