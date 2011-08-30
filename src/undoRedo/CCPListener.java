package undoRedo;

/**
 * Interface for all classes that listen to cut, copy, and paste events from the transferActionListener.
 * The implementation of these methods should copy and retrieve things from the local clipboard - they
 * don't provide data or expect data to be returned. 
 * @author brendan
 *
 */
public interface CCPListener {

	public void paste();
	
	public void copy();
	
	public void cut();
	
}
