/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fileTree;
import java.io.File;
/**
 * Just exists so that toString returns the file name, not the full path
 * @author brendan
 */
public class TreeFile {

    File file = null;
    
    public TreeFile(File f){
        this.file = f;
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
