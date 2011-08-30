package plugin;

import java.util.List;

import topLevelGUI.FileParser;
import topLevelGUI.analyzer.Analyzable;

import display.Display;

/**
 * The base class of all plugins. Plugins typically consist of one or more Displays, zero or more
 * parsers, and zero or more Analyzers. At startup, the main application class looks in a few places
 * for these and loads them.  
 * @author brendan
 *
 */
public abstract class Plugin {

	public abstract List<Display> getDisplays();
	
	public abstract List<FileParser> getParsers();
	
	public abstract List<Analyzable> getAnalyzers();
	
}
