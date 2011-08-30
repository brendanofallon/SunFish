package plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import topLevelGUI.SunFishFrame;

import display.Display;

/**
 * A class that maintains a list of locations to examine, and provides facilities to identify and instantiate
 * objects of type Plugin from class and .jar files. Paths to search are added via calls to addPluginPath(), 
 * and typical usage involves something like :
 * 
 * PluginLoader loader = new PluginLoader("/path/to/plugins");
 * loader.loadAllPlugins();
 * List<Plugin> pluginsFound = loader.getPlugins();
 * @author brendan
 *
 */
public class PluginLoader {
	
	List<File> pluginPaths = new ArrayList<File>();
	List<Plugin> pluginsFound = new ArrayList<Plugin>();
	
	public PluginLoader() {
		
	}
	
	public PluginLoader(String path) {
		addPluginPath(path);
	}
	
	/**
	 * Add the given file to the list of places to look for classes. If a directory,
	 * we add all files in the directory that end in .class or .jar to the list of places to
	 * look for classes.
	 * 
	 * @param file
	 */
	public void addPluginPath(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] files = file.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						if (name.endsWith("class") || name.endsWith("jar"))
							return true;
						else
							return false;
					}
				});
				
				for(int i=0; i<files.length; i++) {
					//System.out.println("Added file from directory : " + files[i]);
					pluginPaths.add(files[i]);
				}
			}
			else {
				//System.out.println("Added file : " + file);
				pluginPaths.add(file);
			}
		}
		else {
			System.out.println("File does not exist, cannot add file: " + file.getPath());
		}
	}
	
	/**
	 * Assumes path indicates either a directory or a file containing classes, then adds
	 * it to the list exactly as in addPluginPath(file)  
	 * @param path
	 */
	public void addPluginPath(String path) {
		File file = new File(path);
		addPluginPath(file);		
	}
	
	public Class getClassFromFile(File file, String name) throws Exception {
		URLClassLoader clazzLoader;
		Class clazz;
		String filePath = file.getAbsolutePath();
		if (filePath.endsWith("jar"))
			filePath = "jar:file://" + filePath + "!/";
		else
			filePath = "file://" + filePath + "!/";
		URL url = new URL(filePath);
		clazzLoader = new URLClassLoader(new URL[]{url});
		clazz = clazzLoader.loadClass(name);
		return clazz;
	}
	
	public List<Plugin> getPlugins() {
		return pluginsFound;
	}
	
	/**
	 * Search through all pluginPaths that have been so far specified and see if any classes are instances
	 * of Plugin. If so, attempt to instantiate  them and add them to the list of pluginsFound. 
	 * @throws PluginLoaderException 
	 */
	public void loadAllPlugins() throws PluginLoaderException {
		for(File file : pluginPaths) {
			try {
				JarInputStream jis =  new JarInputStream(new FileInputStream(file));
			
				JarEntry entry = jis.getNextJarEntry();
				while(entry != null) {
					//System.out.println("Found : " + entry.getName() );
					if (entry.getName().endsWith("class")) {
						String className = convertEntryToClassName(entry.getName());
						//System.out.println("Attempting to load class : " + className);
						Class clazz = getClassFromFile(file, className);
						if (Plugin.class.isAssignableFrom( clazz)) {
							//System.out.println("Attempting to instantiate plugin : " + className);
							
							Plugin plugin = (Plugin)clazz.newInstance();
							SunFishFrame.getSunFishFrame().getLogger().info("Found plugin of class : " + plugin.getClass() + " from file: " + file.getName());
							pluginsFound.add(plugin);
						}
					}
					entry = jis.getNextJarEntry();
				}
			
		} catch (FileNotFoundException e) {
			//Don't care about these too much
		}  catch (Exception e) {
			throw new PluginLoaderException("Error encountered during component loading : " + e.getMessage());
		}
		
	}
	
	}
	
	
	private String convertEntryToClassName(String name) {
		String str = name.replaceAll("/", ".");
		str = str.substring(0, str.lastIndexOf("."));
		return str;
	}

}
