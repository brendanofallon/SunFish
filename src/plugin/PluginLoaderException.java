package plugin;

/**
 * These are thrown when there is an error loading plugins. 
 * @author brendan
 *
 */
public class PluginLoaderException  extends Exception {

	public PluginLoaderException(String message) {
		super(message);
	}
	
}
