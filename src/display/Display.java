package display;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

import topLevelGUI.SunFishFrame;
import undoRedo.CCPListener;
import undoRedo.RedoException;
import undoRedo.UndoException;
import undoRedo.UndoRedoManager;
import undoRedo.UndoableAction;
import undoRedo.UndoableActionSource;

import displayPane.DisplayPane;
import displayPane.SaveCancelledException;
import displayPane.TabPaneTab;

import errorHandling.ErrorWindow;


/**
 * A component that is used to display some sort of data. These usually live in a DisplayPane. This class
 * provied lots of utility methods for most displays. For instance methods related to saving, tracking
 * whether or not there are unsaved changes, dropping of new data, and clipboard related stuff are in here. 
 * @author brendan
 *
 */
public abstract class Display extends JPanel implements UndoableActionSource, CCPListener, ClipboardOwner {
	
	private static int displayCount = 0; //Total number of displays instantiated
	public final int myNumber = displayCount; //Unique number for this display

	protected String filename = ""; //The name of the file being displayed
	protected String title = "";
	protected JFrame myFrame;
	protected SunFishFrame sunfishParent;
	protected ImageIcon icon;
	protected File sourceFile;
	
	protected boolean isPopped = false;
	
	private boolean hasUnsavedChanges = false;
	
	protected UndoRedoManager undoManager = new UndoRedoManager();
	
	protected ProgressPanel progressPanel = null;
	
	
	//A fileChooser common to all displays, this is so it can remember past locations
	//so users don't have to constantly navigate to the same folder
	protected JFileChooser fileChooser = null;
	
	public Display(SunFishFrame sunfishParent) {
		displayCount++;
		this.sunfishParent = sunfishParent;
		if (fileChooser == null) {
			String userDir = System.getProperty("user.dir");
			fileChooser = new JFileChooser(userDir);
		}
		
		setFocusable(true);
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				requestFocusInWindow();
			}
		});
		
		setDefaultActionMap();	
	}
	
	/**
	 * Return a short, descriptive name for this type of Display, e.g. "Phylogenetic tree display"
	 */
	public abstract String getName();
	
	/**
	 * Return a brief description of this Display, e.g. "Display and editing phylogenetic trees"
	 */
	public abstract String getDescription();
	
	/**
	 * Return the version number of this display. 
	 * @return
	 */
	public abstract double getVersionNumber();

	/**
	 * This important method describes the types of objects that this class can display. 
	 * @return
	 */
	public abstract Class[] getDisplayableClasses();
	
	protected void setDefaultActionMap() {
		ActionMap map = this.getActionMap();
		map.put(TransferHandler.getCutAction().getValue(Action.NAME),
                TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME),
                TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME),
                TransferHandler.getPasteAction());
      
	}

	/**
	 * Remove all components from this Display and display a progress bar instead, initialized at zero. 
	 * Call setProgress to increment the progress bar. 
	 */
	public void initializeProgressBar() {
		this.removeAll();
		progressPanel = new ProgressPanel();
		this.setLayout(new BorderLayout());
		this.add(progressPanel, BorderLayout.CENTER);
	}
	
	/**
	 * Remove the progressPanel, if it has been initialized 
	 */
	public void removeProgressPanel() {
		if (progressPanel != null)
		this.remove(progressPanel);
		progressPanel = null;
	}
	/**
	 * Set the value of the progress bar to the given amount (in 0, 1). This has no effect
	 * if sequences have already been loaded and the progress bar is no longer displayed. 
	 * @param prog
	 */
	public void setProgress(double prog) {
		if (progressPanel == null) {
			throw new IllegalStateException("ProgressPanel has not been initialized");
		}
		
		progressPanel.setProgress(prog);
	}
	
	/**
	 * This should return true if the user has modified the data in this display in any way and has not saved
	 * TODO: If the user makes changes and then undoes them, this should be restored
	 * @return
	 */
	public boolean hasUnsavedChanges() {
		return hasUnsavedChanges;
	}
	
	protected void setHasUnsavedChanges(boolean hasEm) {
		hasUnsavedChanges = hasEm;
		TabPaneTab myTab = sunfishParent.getDisplayPane().getTabForComponent(this);
		if (myTab != null)
			myTab.repaint();
	}
	
	
	/**
	 * This method is called by SunFishFrame to associate the given data with this presumably new 
	 * display. Typically, this display is then added to the DisplayPane  
	 * 
	 * @param parent
	 * @param data
	 * @param title
	 * @return
	 */
    public boolean presentDisplay(SunFishFrame parent, DisplayData data, String title) {
    	this.sunfishParent = parent;
    	this.title = title;
    	this.icon = data.getIcon();
    	sourceFile = data.getFile();
    	if (sourceFile == null) {
    		hasUnsavedChanges = true;
    	}
    	boolean isOK = update(parent, data);
    	if (isOK) {
    		requestFocusInWindow();
    	}
    	return isOK;
    }
	
    /**
     * Return the sourceFile associated with this display, if there is one. May return null
     * if this display is not associated with a source file.
     * @return
     */
    public File getSourceFile() {
    	return sourceFile;
    }
    
    /**
     * This should be called whenever this display is closed, it removes this display from the list
     * of open displays in sunfishframe. Currently it's called when the associated TabPaneTab button
     * is closed and, if this thing is popped, when the containing DisplayFrame is closed.
     */
    public void closed() throws SaveCancelledException {
    	System.out.println("Calling close for display: " + this + " unsaved changes is : " + hasUnsavedChanges());
    	if (hasUnsavedChanges()) {
    		Object[] options = {"Cancel", "Don't save",	"Save"};
    		int n = JOptionPane.showOptionDialog(SunFishFrame.getSunFishFrame(),
    				"The display " + getTitle() + " has unsaved changes. Save before closing?",
    				"Save changes",
    				JOptionPane.YES_NO_CANCEL_OPTION,
    				JOptionPane.QUESTION_MESSAGE,
    				null,
    				options,
    				options[2]);
    		if (n==0)
    			throw new SaveCancelledException();
    		if (n==2)
    			save();
    	}
    	
    	//Must tell the various undo and ccp focus listeners that we no longer exist
    	UndoRedoManager.releaseUndoFocus(this);
    	SunFishFrame.getSunFishFrame().getCCPListener().releaseFocus(this);
    	SunFishFrame.getSunFishFrame().displayClosed(this);
    }
    
    /**
     * Called before any data is given, gives this class a chance to instantiate some objects at the beginning
     * so classes can be displayed quickly. In general, this should be called prior to update. 
     */
    public void construct() {   }
    
	/**
	 * Called when the frame containing this component changes. Displays with GlassDropPanes
	 * should override this to notify the GDPs of the change to their frame. 
	 * @param newFrame The new frame containing this Display
	 */
	public void setFrame(JFrame newFrame) { }
    
	/**
	 * Returns true if this display can accept drops (as in Drag and Drop) of the specified
	 * file. Returning false (the default) means that this display will ignore the file
	 * 
	 * @param file
	 * @return True if we can handle having this file dropped on us.
	 */
	public boolean acceptDrop(File file) {
		return false;
	}
	
	/**
	 * This is called if a file has been dropped on us and we returned 'true' to acceptDrop
	 * @param file
	 */
	public void fileDropped(File file) {
		
	}
	
	/**
	 * Attempts to save this file to sourceFile, if sourceFile exists
	 */
	public void save() throws SaveCancelledException {
		if (sourceFile == null)
			sourceFile = saveNew();
		else {
			Object[] options = {"Cancel" , "Overwrite"};
			int n = JOptionPane.showOptionDialog(SunFishFrame.getSunFishFrame(),
					"Over write existing data? (Cannot be undone)",
					"File exists",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[0]);
			if (n==1) {
				saveToFile(sourceFile);
				setHasUnsavedChanges(false);
			}
		}
	}
	
	/**
	 * Save this display's information to the given file. The basic idea is we already have the file, 
	 * and we've cleared the various user hurdles (operation was not cancelled, user has OK'd over-
	 * writing existing file, etc.).. so we're definitely going to save at this point. Unless an
	 * IO error occurs.. 
	 * @param file
	 */
	public abstract void saveToFile(File file); 
	
	/**
	 * Save the data to a new file chosen by the user. This just prompts the user to select a file and 
	 * then calls saveToFile with the file. 
	 */
	public File saveNew() throws SaveCancelledException {
		int retVal = fileChooser.showSaveDialog(sunfishParent);
		File chosenFile = null;
		if (retVal == JFileChooser.APPROVE_OPTION) {
			chosenFile = fileChooser.getSelectedFile();
			saveToFile(chosenFile);
			setHasUnsavedChanges(false);
		}
		else {
			throw new SaveCancelledException();
		}
		
		return chosenFile;
	}
	
	/**
	 * Returns true if this Display is in the 'popped' state, in which it exists in it's own JFrame
	 * @return
	 */
	public boolean isPopped() {
		return isPopped;
	}
    /**
     * Pop this window into its own frame
     */
    public void pop() {
    	displayIsPopping();
    	sunfishParent.getDisplayPane().removeDisplay(this);
		myFrame = new DisplayFrame(this);
		isPopped = true;
		displayPopped();
		myFrame.setVisible(true);
    }
    
    
    /**
     * A hook so that subclasses can do something before the display is popped into a new frame. 
     * The default is to do nothing.
     */
    public void displayIsPopping() {
    	//Do nothing.
    }
    
    /**
     * A 'hook' so that subclasses can do something when the display is popped into a new frame. 
     * This is called after the new frame has been constructed and packed, but before it is made
     * visible. 
     */
    protected void displayPopped() {
    	//Default is do nothing
    }
    
    /**
     * Returns the JFrame that currently contains this displays
     * @return
     */
    public JFrame getParentFrame() {
    	return myFrame;
    }
    
    /**
     * Reattach this window to the displayPane
     */
    public void reattach() {
    	myFrame.setVisible(false);
    	//For components that display things in scrollPanes, resizing to something small 
    	//means that the component will grow to exactly fit in the scrollpane on reattachment..
    	myFrame.setSize(new Dimension(20, 20));
    	if (myFrame instanceof DisplayFrame) {
    		((DisplayFrame)myFrame).setReattaching();
    	}
		this.revalidate();
    	myFrame.remove(this);
    	myFrame.dispose();
    	displayPane.DisplayPane disPane = sunfishParent.getDisplayPane(); 
    	disPane.addTab(title, this);
		disPane.setTabComponentAt(disPane.indexOfComponent(this) , new TabPaneTab(title, sunfishParent, icon));
		myFrame = sunfishParent;
		setFrame(myFrame);
		isPopped = false;
    }

    
    /**
     * Called to supply this display with some data. Upon calling the display should somehow 'display' the data
     * The return value determines if this component should actually be displayed -- If there's an error while
     * trying to display the 'DisplayData', there may be nothing to show. Rather than display an empty pane, we
     * return false to notify the DefaultDisplayPane that there's nothing to show, and it doesn't open a new tab.
     * Under normal operation, this should return true.
     * 
     * @param parent The CowFishFrame that owns this display (perhaps also attainable via getRootPane?)
     * @param data The data to display
     * @param filename Title of the data
     */
    protected abstract boolean update(SunFishFrame parent, DisplayData data);

    public String getDisplayName() {
    	return "Generic Display";
    }

	public abstract Display getNew();

	public String getFileName() {
		if (sourceFile!=null) {
			return sourceFile.getName();
		}
		if (filename != null) {
			return filename;
		}
		return "(unknown)";
	}
	
	public String getTitle() {
		return title;
	}
	
	/**
	 * Called when this display has lost focus. Most derived classes don't care. 
	 */
	public void lostFocus() {
		UndoRedoManager.releaseUndoFocus(this);
		SunFishFrame.getSunFishFrame().getCCPListener().releaseFocus(this);
	}
	
	/**
	 * Called when this display has gained focus
	 */
	public void gainedFocus() {
		requestFocusInWindow();
		UndoRedoManager.requestUndoFocus(this);
	}
	
	//No-op UndoableActionSource implementation
	public UndoableAction undoAction(UndoableAction action) {
		return null;
	}
	
	public void redoAction(UndoableAction action) {
		
	}
	
	/**
	 * Obtain the undo manager associated with this display
	 */
	public UndoRedoManager getManager() {
		return undoManager;
	}
	
	public String toString() {
		return "Display of class : " + this.getClass() + " displaying file: " + getSourceFile();
	}
	
	
	/************** Clipboard related stuff ********************************************/
	
	public void copy() {
		Clipboard clipboard = SunFishFrame.getSunFishFrame().getClipboard();
		Transferable data = copyData();
		if (data != null)
			clipboard.setContents(data, this);
	}
	
	public void cut() {
		Clipboard clipboard = SunFishFrame.getSunFishFrame().getClipboard();
		Transferable data = cutData();
		if (data != null)
			clipboard.setContents(data, this);		
	}
	
	public void paste() {
		Clipboard clipboard = SunFishFrame.getSunFishFrame().getClipboard();
		Transferable data = clipboard.getContents(null);
		if (data != null)
			pasteData(data);
	}
	
	/**
	 * This is called when the user chooses copy from the edit menu and this display is the current
	 * focusOwner (as defined in the TransferActionListener). The default here is to return null. 
	 * Displays which support data copying should return an object representing the copied data here. 
	 * @return The data copied from this display
	 */
	public Transferable copyData() {
		return null;
	}
	
	/**
	 * Called when the user chooses cut from the edit menu and this Display is the current focusOwner
	 * (as defined in TransferActionListener). Displays which support data cutting should return the cut
	 * data here. 
	 * @return The data cut from this display
	 */
	public Transferable cutData() {
		return null;
	}
	
	/**
	 * Called when the user pastes data into this display.
	 * @param data
	 */
	public void pasteData(Transferable data) {	}
	
	/**
	 * Called when we lose ownership of the clipboard. In general we don't care about this. Should we? 
	 */
	public void lostOwnership(Clipboard clipboard, Transferable contents) {

	}
}
