package displayPane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.Icon;

import topLevelGUI.SunFishFrame;


import display.Display;
import display.ProgressPanel;
import displayPane.SaveCancelledException;
import errorHandling.ErrorWindow;
import fileTree.TreeTransferHandler;

/**
 * The component used to display all of the various displays. This does not house or know 
 * of displays that have been 'popped' into new windows
 * 

 * @author brendan
 *
 */
public class DisplayPane extends JTabbedPane implements ChangeListener {

	SunFishFrame parent;
	Logger logger;
	String iconPath = "./icons/";
	int previouslyFocussedDisplay = 0;
	
	public DisplayPane() {
		super(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		this.parent = SunFishFrame.getSunFishFrame();
		this.logger = SunFishFrame.getSunFishFrame().getLogger();
		iconPath = parent.getIconPath();
		addChangeListener(this);
		setOpaque(false);

		setTransferHandler( new DisplayPaneTransferHandler(parent) );
	}
	
	public List<Display> getOpenDisplays() {
		ArrayList<Display> displays = new ArrayList<Display>();
		for(int i=0; i<this.getTabCount(); i++) {
			Object obj = this.getComponentAt(i);
			if (obj instanceof Display) {
				displays.add((Display)obj);
			}
		}
		return displays;
	}
	
	/**
	 * Return the currently selected (focused) display. This may return null if the selected component is
	 * not a display.  
	 * @return
	 */
	public Display getCurrentDisplay() {
		int index = this.getSelectedIndex();
		if (index>-1) {
			Object obj = this.getComponentAt(index);
			if (obj instanceof Display) {
				return (Display)obj;
			}
		}
		return null;
	}
	
	/**
	 * Open a new pseudo-display that just shows a progress bar, and return the progress bar
	 * @param title
	 * @return
	 */
	public ProgressPanel openProgressDisplay(String title) {
		ProgressPanel progPanel = new ProgressPanel();
		addTab(title, progPanel);
		int index = indexOfComponent(progPanel);
		setTabComponentAt(index, new TabPaneTab(title, parent, null));
		previouslyFocussedDisplay = getSelectedIndex();
		setSelectedComponent(progPanel);
		repaint();
		return progPanel;
	}
	
	/**
	 * Replaces the given ProgressPanel with the given Display.
	 * @param panel
	 * @param display
	 */
	public void openDisplayFromProgressPanel(ProgressPanel panel, Display display, String title) {
		int index = indexOfComponent(panel);
		if (index < 0) {
			addDisplay(display, display.getTitle(), null);
			return;
		}
		setComponentAt(index , display);
		previouslyFocussedDisplay = getSelectedIndex();
		setSelectedComponent(display);
		repaint();
	}
	
	/**
	 * Add a new display to be presented in a new tab. 
	 * @param data
	 * @param display
	 * @param title
	 * @return
	 */
	public void addDisplay(Display display, String title, ImageIcon icon) {
		addTab(title, display);
		setTabComponentAt(indexOfComponent(display) , new TabPaneTab(title, parent, icon));
		previouslyFocussedDisplay = getSelectedIndex();
		setSelectedComponent(display);
		repaint();
	}
	
	public String getTitleForComponent(Component comp) {
		int index = this.indexOfComponent(comp);
		if (index<0) {
			return null;
		}
		Component tab = this.getTabComponentAt(index);
		if (tab instanceof TabPaneTab) {
			return ((TabPaneTab)tab).getTitle();
		}
		return tab.toString();
	}
	
	/**
	 * Set the title (the string displayed in the tab) associated with the given component. 
	 * @param comp
	 * @param newTitle
	 */
	public void setTitleForDisplay(Component comp, String newTitle) {
		int index = this.indexOfComponent(comp);
		if (index<0) {
			return;
		}
		Component tab = this.getTabComponentAt(index);
		if (tab instanceof TabPaneTab) {
			((TabPaneTab)tab).setTitle(newTitle);
		}
	}
	
	/**
	 * Close all tabs and, if they have unsaved changes, prompt the user to save the file.
	 * 
	 *  @return True if the operation was cancelled
	 */
	public boolean closeAndPromptToSave() {
		boolean cancelled = false;
		for(int i = this.getTabCount()-1; i>=0 && !cancelled; i--) {
			Component comp = this.getComponentAt(i);
			if (comp instanceof Display) {
				cancelled = closeDisplay(i);
			}
		}
		return cancelled;
	}
	
	//Return a list of all currently open displays of class displayType 
	public List<Display> getDisplaysOfClass(Class displayType) {
		ArrayList<Display> matchingDisplays = new ArrayList<Display>();
		for(int i=0; i<this.getComponentCount(); i++) {
			if (this.getComponent(i).getClass() == displayType) {
				try {

					matchingDisplays.add( (Display)this.getComponent(i));
				}
				catch (ClassCastException cce) {
					System.out.println("  Caught class cast exception");
				}
			}

		}
		
		return matchingDisplays;
	}
	
	public void closeSelectedDisplay() {
		int index = getSelectedIndex();
		closeDisplay(index);
	}	
	

    /**
     * Called when the focused tab in the defaultDisplayPane changes
     * This is needed so we can tell displays if they've lost focus 
     */
	public void stateChanged(ChangeEvent ce) {
		if (ce.getSource() instanceof JTabbedPane) {
			if (previouslyFocussedDisplay > -1 && previouslyFocussedDisplay < getComponentCount() ) {
				Component comp = getComponentAt(previouslyFocussedDisplay);
				if (comp instanceof Display) {
					((Display)comp).lostFocus();
				}
			}

			Component comp = getSelectedComponent();
			if (comp instanceof Display)
				((Display)comp).gainedFocus();
			previouslyFocussedDisplay = getSelectedIndex();
		}
	}

	/**
	 * Close the given display. This appropriately handles aborted save events, lostFocus, and
	 * previouslyFocussedIndex updating...
	 * @param display
	 */
	public boolean closeDisplay(Component display) {
		int index = indexOfComponent(display);
		return closeDisplay(index);
	}
	
	/**
	 * Close the display with the given index. This should be the only method used to close
	 * displays. It calls display.closed(), and if no exception is thrown, removeDisplay(..)
	 * which updates the previouslyFocussedDisplay index and calls lostFocus()... 
	 * @param index Number of the display to remove
	 * @returns  True if the operation was canceled, false otherwise
	 */
	public boolean closeDisplay(int index) {
		if (index>-1 && index < getComponentCount()) {
			Component comp =  this.getComponentAt(index);
			if (comp instanceof Display) {
				
				try {
					((Display)comp).closed();
					removeDisplay(comp);
					return false;
				} catch (SaveCancelledException e) {
					//User has canceled the close operation
					return true;
				}
			}
			else {
				removeDisplay(comp); //Must come AFTER the updating of prevFocussedDisplay index! 
			}

		}
		return false; //Bad index, we didn't do anything.
	}
	
	/**
	 * Removes the given component from the displayPane without calling .close() on the given display. 
	 * This happens when the display being moved into a different container, for instance, when
	 * it is popped. 
	 * @param comp
	 */
	public void removeDisplay(Component comp) {
		int index = indexOfComponent(comp);
		
		if (index>-1 && index < getComponentCount()) {
			if (previouslyFocussedDisplay==index) {
				previouslyFocussedDisplay = -1;
			}
			else {
				if (previouslyFocussedDisplay > index)
					previouslyFocussedDisplay--;
			}

			if (comp instanceof Display) {
				((Display)comp).lostFocus();
			}
			remove(index);
		}
	}

	/**
	 * Returns the TabPaneTab associated with the given Display. If the Display is not in this tabbed pane, returns null.
	 * @param display
	 * @return
	 */
	public TabPaneTab getTabForComponent(Display display) {
		int index = this.indexOfComponent(display);
		if (index >=0 ) {
			Component tab = this.getTabComponentAt(index);
			if (tab instanceof TabPaneTab) {
				return (TabPaneTab)tab;
			}
		
		}
		return null;
	}

	
}
