/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fileTree;
import java.io.File;
import java.util.List;

import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;
/**
 * This is the "user object" stored with the JTree nodes in a DirectoryTreeBlock. Each one
 * of these is associated with a file, and remembers some information about the file, such
 * as whether it's a directory and what the associated FileParser is. These attributes
 * can be re-loaded via a call to loadFileAttributes, which should occur whenever the file
 * info changes. 
 *  
 * @author brendan
 */
public class TreeFile {

    File file = null;
    boolean attributesLoaded = false;
    boolean attributesDirty = true;
    
    boolean isDir = false;
    FileParser parser = null;
    
    public TreeFile(File f){
        this.file = f;
    }

    
    public boolean isDir() {
    	if (!attributesLoaded)
    		loadFileAttributes();
    	return isDir;
    }
    
    /**
     * Return the primary file parser associated with this file. Right now this
     * just follows SunFishFrame.getParserList(file).get(0)
     * @return
     */
    public FileParser getParser() {
    	if (!attributesLoaded)
    		loadFileAttributes();
    	return parser;
    }
    
    /**
     * Re-scan the file associated with this object and set the isDir and Parser fields accordingly
     */
    public void loadFileAttributes() {
    	isDir = (file != null) && (file.isDirectory());
    	
    	List<FileParser> parsers = SunFishFrame.getSunFishFrame().getParserList(file);
    	if (parsers.size() > 0)
    		parser = parsers.get(0);
//    	else
//    		System.out.println("Could not find any parsers for file : " + file);
    	
//    	System.out.println("Loading attributes for file : " + file.getName());
//    	System.out.println("Directory : " + isDir);
//    	if (parser != null)
//    		System.out.println("Parser : " + parser.getName());
//    	else 
//    		System.out.println("Parser : NULL ");
    	
    	attributesLoaded = true;
    	attributesDirty = false;
    }
    
    public File getFile() {
        return file;
    }
    
    public String getFilePath() {
    	String path = file.getAbsolutePath();
		String text = path.substring(0, path.lastIndexOf("/")+1);
		return text;    	
    }
    
    public String toString() {
        return file.getName();
    }

}
