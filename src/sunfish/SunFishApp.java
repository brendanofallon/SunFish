/*
 * SunFishApp.java
 */

package sunfish;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import display.Display;

import plugin.Plugin;
import plugin.PluginLoader;
import plugin.PluginLoaderException;

import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;
import topLevelGUI.analyzer.Analyzable;

import errorHandling.ErrorWindow;


/**
 * The main class of the application. This is responsible for loading properties and creating the main window
 */
public class SunFishApp {

	private static Logger logger = Logger.getLogger(SunFishApp.class.getName());
	public static final String userPropsFilename = "sunfishuser.props";
	public static final String defaultPropsFilename = "sunfishdefault.props";
	
	static SunFishApp sunfishApp;
	SunFishFrame sunfish;
	
	
	protected Properties loadDefaultProperties() {
    	Properties defaultProps = new Properties();
    	String propsFilename = defaultPropsFilename;
    	final String homeDir = System.getProperty("user.home");
    	final String fileSep = System.getProperty("file.separator");
    	final String thisDir = System.getProperty("user.dir");
    	
    	//The following string determines the 
    	String[] propsPaths ={"", thisDir + fileSep, homeDir + fileSep + "SunFish" + fileSep};
    	boolean loadedProps = false;
    	int pathIndex = 0;
    	
    	//Loop through the various directories looking for anything with the name sunfishdefault.props
    	while (!loadedProps && pathIndex < propsPaths.length) {
    		String fullPath = propsPaths[pathIndex]  + propsFilename;
    		pathIndex++;
    		try {
    			FileInputStream in = new FileInputStream(fullPath);
    			defaultProps.load(in);
    			logger.info("Found default props in : " + fullPath);
    			in.close();
    			loadedProps = true;
    		}
    		catch (IOException ioe) {
    			//Shouldn't worry about this..
    		}
    		
    	}
    	
    	if (loadedProps == false) {
    		ErrorWindow.showErrorWindow(new FileNotFoundException("Could not find default properties file"), logger);
    		logger.warning("Could not find an appropriate properties file");
    	}
    	return defaultProps;	
	}
	
	protected Properties loadUserProperties(Properties defaultProps) {
    	Properties userProps;
    	if (defaultProps != null)
    		userProps = new Properties(defaultProps);
    	else
    		userProps = new Properties();
    	String propsFilename = userPropsFilename;
    	final String homeDir = System.getProperty("user.home");
    	final String fileSep = System.getProperty("file.separator");
    	final String thisDir = System.getProperty("user.dir");
    	
    	//The following string determines the 
    	String[] propsPaths ={"", thisDir + fileSep, homeDir + fileSep + "SunFish" + fileSep};
    	boolean loadedProps = false;
    	int pathIndex = 0;
    	
    	//Loop through the various directories looking for anything with the name sunfishdefault.props
    	while (!loadedProps && pathIndex < propsPaths.length) {
    		String fullPath = propsPaths[pathIndex]  + propsFilename;
    		pathIndex++;
    		try {
    			FileInputStream in = new FileInputStream(fullPath);
    			userProps.load(in);
    			logger.info("Found user props in : " + fullPath);
    			in.close();
    			loadedProps = true;
    			userProps.setProperty("this.path", propsPaths[pathIndex]);
    		}
    		catch (IOException ioe) {
    			//Shouldn't worry about this..
    		}
    		
    	}
    	
    	return userProps;	
	}
	
	protected void loadPlugins(SunFishFrame sunfish) {
		PluginLoader loader = new PluginLoader("plugins");
		
		try {
			loader.loadAllPlugins();
		} catch (PluginLoaderException e) {
			ErrorWindow.showErrorWindow(e);
		}

		List<Plugin> plugins = loader.getPlugins();



		for(Plugin plugin : plugins) {
			try {
				List<Display> displays = plugin.getDisplays();
				for(Display display : displays) 
					logger.info("Registering display: " + display.getName() + " version: " + display.getVersionNumber());

				sunfish.registerDisplays(displays);

				List<FileParser> parsers = plugin.getParsers();
				for(FileParser parser : parsers) 
					logger.info("Registering parser: " + parser.getName() + " version: " + parser.getVersionNumber());

				sunfish.registerParsers(parsers);

				List<Analyzable> analyzers = plugin.getAnalyzers();
				//do we ever want to do this?
			}
			catch (java.lang.NoClassDefFoundError err) {
				ErrorWindow.showErrorWindow(new PluginLoaderException("Error loading class: " + err.getMessage()));
			}

		}
	}
	
    /**
     * At startup create and show the main frame of the application.
     */
    protected void startup() {
    	try {
    		//Attempt to load properties ..
    		Properties defaultProps = loadDefaultProperties();
    		Properties userProps = loadUserProperties(defaultProps);
    		sunfishApp = this;
    		sunfish = new SunFishFrame(logger, userProps);
    		loadPlugins(sunfish);
    		sunfish.associateParsers();
    		sunfish.setVisible(true);
    		
    		sunfish.reopenFiles();
    	}
    	catch (Exception ex) {
    		ErrorWindow.showErrorWindow(ex, logger);
    	}
    }

    
    /**
     * Called on application shutdown. We do two things here - close all of the displays so the 
     * user can chose to save any if there are unsaved changed, and then we write all properties to
     * the properties file. 
     */
    public void shutdown() {
    	sunfish.writeProperties();
    	boolean cancelled = sunfish.getDisplayPane().closeAndPromptToSave(); //This should come after writeProperties since prop
    						//writing looks at the display pane to remember which displays are open.
    	if (! cancelled)
    		System.exit(0);
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of SunFishApp
     */
    public static SunFishApp getApplication() {
        return sunfishApp;
    }

    public static void launchApplication() {
    	javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	SunFishApp app = new SunFishApp();
            	app.startup();
            }
        });
    }
    
    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launchApplication();
    }
}
