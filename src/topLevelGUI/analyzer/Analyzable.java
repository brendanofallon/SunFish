/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package topLevelGUI.analyzer;

import java.io.File;

import javax.swing.JComponent;
import javax.swing.JPanel;

import display.Display;

/**
 * Base class of all things 'analyzable' - that is, components that can be shown in the AnalysisPane
 * @author brendan
 */
public abstract class Analyzable extends JPanel {
	
	protected Display source;

	public Analyzable(Display source) {
		super();
		this.source = source;
		setOpaque(false);
	}
	
	public Display getSource() {
		return source;
	}

	/**
	 * Returns true if we can add data of the given type to this analyzer. 
	 * @param data
	 * @return
	 */
	public boolean canAddData(Object data) {
		return false;
	}
	
	/**
	 * Attempt to add the data in the given object to this analyzer. Typically, we will attempt to
	 * cast the data to an appropriate type and then add it in an analyzer-specific manner  
	 * @param data
	 */
	public void addObjectData(Object data) {
		//Intentionally blank, analyzers may implement this on their own if they can recieve data in this
		//manner. 
	}
	
	/**
	 * Called when a file has been dropped on this analyzer
	 * @param file
	 */
	public void fileDropped(File file) {
		//Intentionally blank, analyzers may implement this on their own if they can recieve data in this
		//manner. 
	}
	
	/**
	 * Called when the user has performed an action that will replace this
	 * analyzable with something else. We can block this action by returning false here,
	 * of course blindingly doing so would mean the user could never open a new analyzable.
	 * This also gives us a chance to do something prior to closing, like remove this analyzable
	 * from listener lists so this memory can be freed.  
	 */
	public boolean canAndWillClose() { 
		return true;
	};
	
	/**
	 * Called when we are definitely closing - this always gets called. Resources should
	 * be freed here.  
	 */
	public void closed() { };
}
