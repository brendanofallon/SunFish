/*
 * SunFishView.java
 */

package topLevelGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultEditorKit;

import sunfish.SunFishApp;
import topLevelGUI.analyzer.AnalysisPane;
import topLevelGUI.analyzer.Analyzable;
import undoRedo.TransferActionListener;
import undoRedo.UndoRedoManager;
import undoRedo.UndoableActionSource;
import display.Display;
import display.DisplayData;
import display.DisplayRegistry;
import display.ProgressPanel;
import displayPane.DisplayPane;
import displayPane.SaveCancelledException;
import displayPane.TabPaneTab;

import errorHandling.ErrorWindow;
import fileTree.FileTreePanel;
import guiWidgets.FancyPanel;
import guiWidgets.GTKFixSeparator;
import guiWidgets.SFMenu;
import guiWidgets.SFMenuItem;

/**
 *  
 * Enhancements :
 *  Support for DNA / RNA / protein sequences (have to guess type at parsing - utility class in Sequence)
 *  Editable XMLs
 *  Save text info
 *  Consistent interface for display names (can't always be file name...)
 *  
 * New Displays :
 *  Single sequence display w/attributes (also translate / rev comp / add attribute)
 *  Display registered parsers?
 *  Display registered displays?
 *  
 * New Parsers :
 *   Trace files? Can we do anything better than Tracer?  
 *   GBK, parses to single attributed sequence 
 * 
 * Other Features / wishlist :
 *   NCBI / BLAST interface (at least get gene by acc num would be nice)
 *   Online storage of/access to files
 * 	 Alignment / tree building web services
 */
public class SunFishFrame extends JFrame {

	private Logger logger;
	private Properties props;
		
	DisplayRegistry displayRegistry;
	ParserRegistry parserRegistry;
	
	Hashtable<String,Display> currentDisplays = null;

	String iconPath;
	
	List<Display> openDisplays; // A list of all currently open displays
		
	boolean onAMac = false; //True if we're on a mac system
	
	Clipboard clipboard;
	TransferActionListener ccpListener = new TransferActionListener();
	
	static SunFishFrame sunfish;
	
    public SunFishFrame(Logger logger, Properties props) {
        this.logger = logger;
        this.props = props;
        sunfish = this;
    	
        this.iconPath = props.getProperty("iconPath");
        if (iconPath == null)
        	this.iconPath = "icons/";
    	
        try {
        	String plaf = UIManager.getSystemLookAndFeelClassName();
        	LookAndFeelInfo[] installedPLaFs = UIManager.getInstalledLookAndFeels();
        	LookAndFeelInfo gtkLookAndFeel = null;
        	if (installedPLaFs.length > 0) {
        		for(int i=0; i<installedPLaFs.length; i++) {
        			logger.info("Found look and feel: " +installedPLaFs[i].getClassName());
        			if (installedPLaFs[i].getClassName().contains("GTK")) {
        				gtkLookAndFeel  = installedPLaFs[i];
        			}
        		}
        	}
        	else
        		logger.warning("Found 0 installed look and feels");
        	//Attempt to avoid metal look and feel by substituting GTK, if available
        	if (plaf.contains("metal") && gtkLookAndFeel != null) {
        		plaf = gtkLookAndFeel.getClassName();
        		logger.info("System look and feel is metal, switching to : " + plaf);
        	}

        	UIManager.setLookAndFeel( plaf );
        	logger.info("Setting look and feel to : " + plaf);
        }
        catch (Exception e) {
            logger.warning("Caught exception setting look and feel to system : " + e.toString());
        }
        
        String os = System.getProperty("os.name");
        if (os.contains("Mac") || os.contains("mac")) {
        	logger.info("Found a mac system, turning mac mode on");
        	onAMac = true;
        }
        else {
        	logger.info("Did not find mac system, setting mac mode to off");
        	onAMac = false;
        }
        
		currentDisplays = new Hashtable<String, Display>();

		displayRegistry = new DisplayRegistry();
		
		parserRegistry = new ParserRegistry(this);
		
		openDisplays = new ArrayList<Display>();
        
        //A couple of mac-specific changes
        String lcOSName = System.getProperty("os.name").toLowerCase();
        boolean isMac = lcOSName.startsWith("mac os x");
        if (isMac) {
        	logger.info("Setting MAC mode to true");
        	System.setProperty("apple.laf.useScreenMenuBar", "true");
        	System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SunFish"); //Useless?
        }
        
        initComponents(); //Must come after parserRegistry initialization
        createMenus();

		analysisPane.removeAll();
		analysisPane.setLayout(new BorderLayout());
		analysisPane.repaint();
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				SunFishApp.getApplication().shutdown();
			}
		});
		
		Dimension initialFrameSize = new Dimension(1200, 600);
		String widthStr = props.getProperty("frame.width");
		if (widthStr != null) {
			try {
				int width = Integer.parseInt(widthStr);
				initialFrameSize.width = width;
			}
			catch (Exception ex) {
				//Don't care, just accept default
			}
		}
		String heightStr = props.getProperty("frame.height");
		if (heightStr != null) {
			try {
				int height = Integer.parseInt(heightStr);
				initialFrameSize.height = height;
			}
			catch (Exception ex) {
				//Don't care, just accept default
			}
		}
		this.setPreferredSize(initialFrameSize);
		
		clipboard = new Clipboard("sunfish clipboard");
		pack();
		setLocationRelativeTo(null);
    }
    
    /**
     * Retrieve the clipboard used by this sunfish instance
     * @return The clipboard
     */
    public Clipboard getClipboard() {
    	return clipboard;
    }
    
    /**
     * Get a list of all currently open displays
     * @return
     */
    public List<Display> getOpenDisplays() {
    	 return defaultDisplayPane.getOpenDisplays(); 
    }

    /**
     * Returns true if we're on a macintosh system.
     * @return
     */
    public boolean onAMac() {
    	return onAMac;
    }
    
    public String getIconPath() {
    	return iconPath;
    }
    
    public Analyzable getCurrentAnalyzer() {
    	return analysisPane.getCurrentAnalyzer();
    }
    
    /**
     * Set a property to the list of properties maintained by this sunfish instance; these will
     * be written to the properties file and will be re-read in future application runs 
     */
    public void setProperty(String name, String value) {
    	props.setProperty(name, value);
    }
    
    /**
     * Retrieve the property associated with the given key
     * @param propKey
     * @return The property value associated with the given key, or null if the key isn't found
     */
    public String getProperty(String propKey) {
    	return props.getProperty(propKey);
    }

    /**
     * A static getter for this frame, so we don't have to pass it to everything. 
     * @return This sunfishFrame
     */
    static public SunFishFrame getSunFishFrame() {
    	return sunfish;
    }
    
    /**
     * Right now this just looks for file to attempt to re-open
     */
    public void reopenFiles() {
    	ArrayList<File> filesToDisplay = new ArrayList<File>();
    	for(int i=0; i<50; i++) {
    		String key = "display." + Integer.valueOf(i).toString();
    		if (props.containsKey(key)) {
    			String filepath = props.getProperty(key);
    			File file = new File(filepath);
    			if (file.exists()) {
    				filesToDisplay.add(file);
    			}
    		}
    	}
    	
    	if (filesToDisplay.size()>0) {
    		String qStr;
    		if (filesToDisplay.size()==1)
    			qStr = "Reopen one file";
    		else 
    			qStr = "Reopen " + filesToDisplay.size() + " files";
    		Object[] options = {qStr,	"Don't reopen"};
    		int n = JOptionPane.showOptionDialog(this,
    				"Reopen files from last session? ",
    				"Reopen files",
    				JOptionPane.YES_NO_OPTION,
    				JOptionPane.QUESTION_MESSAGE,
    				null,
    				options,
    				options[1]);
    		if (n==0) {
    			for(File file : filesToDisplay) {
    				displayFile(file);
    			}
    		}
    	}
    }
    
    /**
     * Attempts to display the file from the given path, this function is used
     * only to re-open displays on startup that were open at the last shutdown
     * @param filepath
     */
    private void displayFile(String filepath) {
    	File file = new File(filepath);
    	if (file.exists()) {
    		displayFile(file);
    	}
    }
    
    /**
     * Called upon application shutdown to write additional resources 
     */
    public void writeProperties() {
    	
    	List<Display> displays = defaultDisplayPane.getOpenDisplays();
    	
    	//First see if we the location of the user properties file is in
    	//the props file itself
    	String propsPath = props.getProperty("this.path");
    	String fileSep = System.getProperty("file.separator");
    	props.setProperty("iconPath", iconPath);
    	String filename = SunFishApp.userPropsFilename;
    	
    	//Remember window size
    	props.setProperty("frame.width", String.valueOf(this.getWidth()));
    	props.setProperty("frame.height", String.valueOf(this.getHeight()));
    	props.setProperty("displayPane.height", String.valueOf(rightSplitPane.getDividerLocation()));
    	
    	logger.info("Writing properties to file: " + propsPath + filename);
    	
    	int fileTreePanelWidth = outerSplitPane.getDividerLocation();
    	props.setProperty("fileTreePanel.width", new Integer(fileTreePanelWidth).toString());
    	
    	for(int i=0; i<50; i++) {
    		props.remove("display." + Integer.valueOf(i));
    		props.remove("fileTree.block." + Integer.valueOf(i));
    	}
    	
    	//Write properties from file tree, mostly just which top level directories we 
    	//should remember
    	int count = 0;
    	List<String> fileTreeRoots = fileTreePanel.getTopLevelPaths();
    	for(String path : fileTreeRoots) {
    		props.setProperty("fileTree.block." + String.valueOf(count), path);
    		count++;
    	}
    	props.setProperty("fileTree.blocknum", String.valueOf(count));
    	
    	count = 0;
    	List<File> recentFiles = fileTreePanel.getRecentFiles();
    	for(File file : recentFiles) {
    		props.setProperty("fileTree.recentItem." + String.valueOf(count), file.getAbsolutePath());
    		count++;
    	}
    	
    	
    	//Write properties for currently open displays
    	if (displays.size()>0) {
    		count = 0;
    		for(Display d : displays) {
    			File file = d.getSourceFile();
    			if (file!=null) {
    				String filepath = file.getAbsolutePath();
    				props.setProperty("display." + count, filepath);
    				count++;
    			}
    		}
    		props.setProperty("display.number", new Integer(count).toString());
    	}
    	
    	if (propsPath==null) {
    		propsPath = System.getProperty("user.dir");
    		props.setProperty("this.path", propsPath);
    	}
    	String fullPath = propsPath + fileSep + filename;
    	try {
    		FileOutputStream propsStream = new FileOutputStream(fullPath);
    		props.store(propsStream, "--- nothing to report ----" );
    		propsStream.close();
    	}
    	catch (IOException ioe) {
    		logger.warning("Error writing to user properties file, tried path : " + fullPath + "\n" + ioe);
    	}
    }
    
    
	/**
	 * Adds list of all displays to a hashtable keyed by object which the display displays.
	 *  
	 * @issue Should displays be created with the getNew() method?  
	 */
	public void registerDisplays(List<Display> displays) {
		displayRegistry.addDisplay(displays);
	}
	
	/**
	 * Adds the given list of parsers to the parserRegistry
	 * @param parsers
	 */
	public void registerParsers(List<FileParser> parsers) {
		parserRegistry.addParsers(parsers);
	}

	public Logger getLogger() {
		return logger;
	}
	

	
	/**
	 * Takes a string formatted r-g-b and returns a color with those values
	 * used to take a properties string and make a new color from it
	 *  
	 * @param colStr
	 * @return A color corresponding to the colStr
	 */
	public Color parseColor(String colStr) {
		Color color = Color.black;
		String[] rgb = colStr.split("-");
		if (rgb.length!=3) {
			return color;
		}
		else {
			try {
				int val0 = Integer.parseInt(rgb[0]);
				int val1 = Integer.parseInt(rgb[1]);
				int val2 = Integer.parseInt(rgb[2]);
				return new Color(val0, val1, val2);
			} catch (Exception ex) {
				return color;
			}
		}
	}

	public List<FileParser> getParserList(File file) {
		return parserRegistry.getParsers(file);
	}
	
	/**
	 * Returns a list a parse that can (probably) parse the file and return an object of the given class
	 * @param file
	 * @return A parser that can probably parse the file to the given class
	 */
	public FileParser getParserForFileAndClass(File file, Class clazz) {
		List<FileParser> parsers = parserRegistry.getParsers(file);
		for(FileParser parser : parsers) {
			if (parser.getDataClass() == clazz) {
				return parser;
			}
		}
		return null;
	}
	
	public DisplayPane getDisplayPane() {
		return defaultDisplayPane;
	}

	/**
	 * Adds the display to the list of open displays. Right now this is used when displays are reattached
	 * from the popped state, since the window closing event removes the display from the list. 
	 * @param display
	 */
	public void addOpenDisplay(Display display) {
		openDisplays.add(display);
		remakeDisplaysMenu();
	}
	
	/**
	 * Called when a display *has been removed*, this removes the closed display from the list of
	 * open displays and calls remakeDisplaysMenu to update the list of menu items. 
	 * @param display
	 */
	public void displayClosed(Display display) {
		openDisplays.remove(display);		
		remakeDisplaysMenu();
	}
	
	/**
	 * Recreate the items in the Displays menu. This is called when displays are opened or
	 * closed. 
	 */
	private void remakeDisplaysMenu() {
		displaysMenu.removeAll();
		for(final Display display : openDisplays) {
			String name = display.getTitle();
			//System.out.println("Title is : " + name);
			if (name==null || name.length()==0)
				name = display.getFileName();
			
			JMenuItem displayItem = new JMenuItem(name);
			displayItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					focusDisplay(display);
				}
			});
			displaysMenu.add(displayItem);	
		}
		
		displaysMenu.add(GTKFixSeparator.makeSeparator());
		displaysMenu.add(reattachAllDisplays);
		displaysMenu.add(closeAllDisplays);
		displaysMenu.revalidate();
	}

	/**
	 * If the display is popped, this brings the window to the forefront, otherwise the
	 * display becomes the selected display in the display pane.
	 */
	protected void focusDisplay(Display display) {
		if (display.isPopped()) {
			JFrame frame = display.getParentFrame();
			frame.requestFocus();
			frame.toFront();
			
		}
		else {
			try {
				defaultDisplayPane.setSelectedComponent( display );
			}
			catch (IllegalArgumentException iex) {
				ErrorWindow.showErrorWindow(iex, logger);
			}
			
		}
		
	}

	/**
	 * Attempts to display 'data' as a new tab in defaultDisplayPane by finding a Display that knows
	 * how to handle the given data type (or, by querying the PreferredDisplay field in the 
	 * DisplayData object). Once we find a Display object that can display the file, we call
	 * this.display(data, display, title) to show it
	 * This function is usually called by Parsers when they're done parsing a file. 
	 * 
	 * @param data The data to be displayed
	 * @param title Title of the new tab
	 * @returns The Display used to present the data, or null if an error occurred
	 */
	public Display displayData(DisplayData data, String title) {
		if (data==null) {
    		logger.warning("Null data in displayData, not displaying title: " + title);
    		JOptionPane.showMessageDialog(this,
    			    "Unable to display data with title " + title + "\n No data found",
    			    "Error displaying file",
    			    JOptionPane.WARNING_MESSAGE);
    		return null;
		}
		
		if (data.numObjects()==0) {
    		logger.warning("No objects found for file : " + data.getFileName());
    		JOptionPane.showMessageDialog(this,
    			    "Unable to display data with title " + title + "\n No data found",
    			    "Error displaying file",
    			    JOptionPane.WARNING_MESSAGE);
    		return null;
		}
		
		
		if (data.numObjects()>1) {
    		JOptionPane.showMessageDialog(this,
    			    "Found multiple displayable objects for this file (" + data.numObjects() + " total) \n But for now we can only display one.",
    			    "Displaying only one element",
    			    JOptionPane.WARNING_MESSAGE);
		}
		
		Class dataClass = data.getData(0).getClass();
		Class preferredDisplayClass = data.getPreferredDisplay();
		Display display = null;
		
		if (preferredDisplayClass != Object.class) {
			for (Display prefDisplay : displayRegistry.getAllDisplays()) {
				if (preferredDisplayClass == prefDisplay.getClass()) {
					display = prefDisplay;
					//System.out.println("Found preferred display of class : " + display.getClass());
					break;
				}
			}
			
		}
		else {
			display = displayRegistry.pickDisplayForObject(data.getData(0));
		}
		
		logger.info("Displaying class " + dataClass.toString() + " title : " + title);
		
		if (display == null) {
    		JOptionPane.showMessageDialog(this,
    			    "Sorry, could not find an appropriate way to display \n data of type " + data.getData(0).getClass() + "\n Cannot display data",
    			    "Error displaying file",
    			    JOptionPane.WARNING_MESSAGE);
		
			logger.info("No display for object of class : " + dataClass.toString());
			return null;
		}

		display(data, display.getNew(), title);
		return display;
	}
	
	/**
	 * Open a new Display presenting the given data 
	 * @param data
	 * @param display
	 * @param title
	 */
	public void display(DisplayData data, Display display, String title) {
		display(data, display, title, null);
	}
	
	/**
	 * Use the given Display and the DisplayData object to open a new 
	 * Tab in the DisplayPane containing the given Display 
	 * @param data
	 * @param preferredDisplay
	 * @param title
	 */
	public void display(DisplayData data, Display display, String title, ProgressPanel progPanel) {
		try {
			File dFile = data.getFile();
			if (dFile!=null && dFile.exists())
				fileTreePanel.addRecentFile(dFile);
			
			display.construct();
			boolean good = display.presentDisplay(this, data, title);
			
			if (good) {
				if (progPanel != null)
					defaultDisplayPane.openDisplayFromProgressPanel(progPanel, display, title);
				else
					defaultDisplayPane.addDisplay(display, title, data.getIcon());
				addOpenDisplay(display);
			}
			else {
				ErrorWindow.showErrorWindow(new Exception("Error opening display for file: " + title), logger);
			}
			
		}
		catch (Exception ex) {
			ErrorWindow.showErrorWindow(ex, logger);
		}
	}
	
	/**
	 * Attempts to display the contents of datafile as a new tab in defaultDisplayPane by
	 *  finding a parser for the file using getParser (which searches the parser registry),
	 *  and then telling the parser to parse the file. This method is typically called
	 *  by residents of fileTreePanel, from which most new display requests originate.
	 *  
	 *  It is the job of the parser to call SunFishFrame.displayData, 
	 *  which actually handles creation of the new window
	 *  
	 * @param datafile File to be displayed
	 */
	public void displayFile(File file) {
		List<FileParser> workableParsers = parserRegistry.getParsers(file);
		
		if (workableParsers.size()==0) {
    		JOptionPane.showMessageDialog(this,
    			    "Sorry, there is no available file parser for the file " + file.getName(),
    			    "Error parsing file",
    			    JOptionPane.WARNING_MESSAGE);			
			logger.info("No parser for file with name : " + file.getName());
			return;
		}
		
		boolean OK = false;
		Exception ex = null;
		for(FileParser parser : workableParsers) {
			try {
				parser.parseAndDisplay(file);
				OK = true;
			}
			catch (Exception e) {
				ex = e;
			}
			if (OK)
				break;
		}
		
		if (! OK) {
			ErrorWindow.showErrorWindow(ex, logger);
		}

		bottomPanelRightLabel.setText("Displaying " + file.getName());
		bottomPanelRightLabel.repaint();
		logger.info("Displaying file : " + file);
	}

	/**
	 * Displays a new component in the output pane
	 * 
	 * @param comp New component to be displayed in the output pane
	 */
	public void displayOutput(Analyzable analyzer) {
		logger.info("Showing analyzer for " + analyzer.getSource().getFileName());
		analysisPane.showAnalyzer(analyzer);
	}
	
	/**
	 * Obtain the analysis pane
	 * @return
	 */
	public AnalysisPane getAnalysisPane() {
		return analysisPane;
	}
	
	public FileTree getFileTreePanel() {
		return fileTreePanel;
	}
	
	/**
	 * Returns the cut/copy/paste focus listener. 
	 * @return The transfer action listener that knows who has ccp focus
	 */
	public TransferActionListener getCCPListener() {
		return ccpListener;
	}

	
	/**
	 * Set the text of the bottom right label to the new string
	 */
	public void setInfoLabelText(String txt) {
		bottomPanelRightLabel.setText(txt);
		bottomPanelRightLabel.revalidate();
		bottomPanelRightLabel.repaint();
	}

	/**
	 * A quick way to get the key mask for menu accelerators, which are differently defined on macs vs. unix & pcs
	 * @return
	 */
	private int getCommandMask() {
		if (onAMac())
			return ActionEvent.META_MASK;
		else
			return ActionEvent.CTRL_MASK;
	}
	
	/**
	 * Generate the list of menus that appear in the menu bar. 
	 */
	private void createMenus() {
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new SFMenu();
        importMenuItem = new SFMenuItem();
        quitMenuItem = new SFMenuItem();
        editMenu = new javax.swing.JMenu();
        displaysMenu = new JMenu("Displays");
        
        editMenu.getPopupMenu().setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        displaysMenu.getPopupMenu().setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        fileMenu.getPopupMenu().setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        
        //The following two menu items appear at the bottom of the displays menu, and are
        //created here but attached in the remakeDisplaysMenu function
        reattachAllDisplays = new JMenuItem("Reattach all");
        reattachAllDisplays.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reattachAllDisplays();
			}
        });
        
        closeAllDisplays = new SFMenuItem("Close all");
        closeAllDisplays.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeAllDisplays();
			}
        });
        remakeDisplaysMenu();
        
        fileMenu.setText("File");

        JMenuItem newItem = new SFMenuItem();
        newItem.setText("New...");
       	newItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_N, getCommandMask()));
        newItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	System.out.println("New action not implemented yet");
            }
        });
        fileMenu.add(newItem);
        fileMenu.add(importMenuItem);
        
        importMenuItem.setText("Open");
        importMenuItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_O, getCommandMask()));
        importMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileTreePanel.openFileAction();
            }
        });
        fileMenu.add(importMenuItem);
        
        JMenuItem closeItem = new SFMenuItem();
        closeItem.setText("Close");
        closeItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_W, getCommandMask()));
        closeItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeSelectedDisplay();
            }
        });
        fileMenu.add(closeItem);
        
        JMenuItem saveItem = new SFMenuItem("Save");
       	saveItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_S, getCommandMask()));
        saveItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	Display activeDisplay = defaultDisplayPane.getCurrentDisplay();
            	if (activeDisplay!=null) {
            		try {
						activeDisplay.save();
					} catch (SaveCancelledException e) {
						//User cancelled the save, we don't care.
					}
            	}
            }
        });
        fileMenu.add(saveItem);
        
        JMenuItem saveAsItem = new SFMenuItem("Save as...");
        saveAsItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_S, getCommandMask() + ActionEvent.SHIFT_MASK));
        saveAsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	Display activeDisplay = defaultDisplayPane.getCurrentDisplay();
            	if (activeDisplay!=null) {
            		try {
						activeDisplay.saveNew();
					} catch (SaveCancelledException e) {
						//User cancelled the save
					}
            	}
            }
        });
        fileMenu.add(saveAsItem);
        fileMenu.add(GTKFixSeparator.makeSeparator());

        
        JMenuItem showParsers = new SFMenuItem("Show all parsers");
        showParsers.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_P, getCommandMask() ));
        showParsers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	showParserList();
            }
        });
        fileMenu.add(showParsers);

        JMenuItem showDisplays = new SFMenuItem("Show all displays");
        showDisplays.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_D, getCommandMask() ));
        showDisplays.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	showDisplayList();
            }
        });
        fileMenu.add(showDisplays);
        
        fileMenu.add(GTKFixSeparator.makeSeparator());
        
        
        jMenuBar1.add(fileMenu);

        editMenu.setText("Edit");
        undoItem = new SFMenuItem("Undo");
        undoItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_Z, getCommandMask()));
        undoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UndoableActionSource undoOwner = UndoRedoManager.getUndoFocusOwner();
				if (undoOwner != null)
					undoOwner.getManager().undo();
			}
        });
        
        redoItem = new SFMenuItem("Redo");
        redoItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_R, getCommandMask()));
        redoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UndoableActionSource undoOwner = UndoRedoManager.getUndoFocusOwner();
				if (undoOwner != null)
					undoOwner.getManager().redo();
			}
        });
        
        JMenuItem cutItem = new JMenuItem(new DefaultEditorKit.CutAction());
        cutItem.setText("Cut");
        cutItem.setMnemonic(KeyEvent.VK_T);
        cutItem.setActionCommand((String)TransferHandler.getCutAction().
                getValue(Action.NAME));
        cutItem.addActionListener(ccpListener);
       	cutItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_X, getCommandMask()));

        JMenuItem copyItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        copyItem.setText("Copy");
        copyItem.setMnemonic(KeyEvent.VK_C);
        copyItem.setActionCommand((String)TransferHandler.getCopyAction().
                getValue(Action.NAME));
        copyItem.addActionListener(ccpListener);
       	copyItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_C, getCommandMask()));

        JMenuItem pasteItem = new JMenuItem(new DefaultEditorKit.PasteAction());
        pasteItem.setText("Paste");
        pasteItem.setMnemonic(KeyEvent.VK_P);
        pasteItem.setActionCommand((String)TransferHandler.getPasteAction().
                getValue(Action.NAME));
        pasteItem.addActionListener(ccpListener);
       	pasteItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_V, getCommandMask()));
        
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.add(GTKFixSeparator.makeSeparator());
        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        
        setUndoState(false, "");
        
        jMenuBar1.add(editMenu);
        
        if (onAMac)  {
        	//This should only be created on mac systems
        	try {
        		sunfish.MacQuitHandler macHandler = new sunfish.MacQuitHandler(this);
        	}
        	catch (Exception ex) {
        		System.err.println("There was an error creating the Mac OS application exit listener.\n This may affect properties file writing on macintosh systems. ");
        		logger.warning("Error creating the Mac OS application exit listener. " + ex);
        	}
        }
        else {	//On non-macs we add a quit 
            quitMenuItem.setText("Quit");
            quitMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) { 
                    SunFishApp.getApplication().shutdown();
                }
            });
            fileMenu.add(quitMenuItem);
        }

        
        jMenuBar1.add(displaysMenu);        
        this.setJMenuBar(jMenuBar1);
	}
	

	/**
	 * Display a new JFrame with info about all available displays
	 */
	protected void showDisplayList() {
		DisplayListFrame displayList = new DisplayListFrame(displayRegistry);
		displayList.setVisible(true);
	}

	/**
	 * Display a new JFrame with info about all available parsers
	 */
	protected void showParserList() {
		ParserListFrame parserFrame = new ParserListFrame(parserRegistry);
		parserFrame.setVisible(true);
	}

	/**
	 * Set the state of the Undo menu item. The UndoRedoManager calls this as appropriate
	 * @param enabled
	 * @param description
	 */
	public void setUndoState(boolean enabled, String description) {
		undoItem.setEnabled(enabled);
		if (enabled)
			undoItem.setText("Undo " + description);
		else
			undoItem.setText("Can't undo");
	}
	
	/**
	 * Set the state of the Redo menu item. The UndoRedoManager calls this as appropriate
	 * @param enabled
	 * @param description
	 */
	public void setRedoState(boolean enabled, String description) {
		redoItem.setEnabled(enabled);
		if (enabled)
			redoItem.setText("Redo " + description);
		else
			redoItem.setText("Can't redo");
	}
	
	/**
	 * Closes all open displays, but not before prompting the user to confirm the action.
	 */
	protected void closeAllDisplays() {
		Object[] options = {"Close all displays",	"Cancel"};
		int n = JOptionPane.showOptionDialog(this,
				"Close all open displays? ",
				"Close all",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[1]);
		if (n==0) {
			List<Display> currentOpenDisplays = new ArrayList<Display>();
			currentOpenDisplays.addAll(openDisplays);
			for(Display d : currentOpenDisplays) {
				defaultDisplayPane.closeDisplay(d);
			}
		}
	}

	/**
	 * Reattaches all 'popped' displays to the display tabbed pane.
	 */
	protected void reattachAllDisplays() {
		List<Display> currentOpenDisplays = new ArrayList<Display>();
		currentOpenDisplays.addAll(openDisplays);
		for(Display d : currentOpenDisplays) {
			if (d.isPopped()) {
				d.reattach();
			}
		}
	}
	
	/**
	 * Reloads parser information in the file tree. This should be called whenever parsers
	 * are loaded / removed so the file tree info will be up to date
	 */
	public void associateParsers() {
		fileTreePanel.reloadParserInfo();
	}

	/**
	 * Create the various GUI components.. this should be generalized at some point
	 */
	private void initComponents() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

        outerSplitPane = new javax.swing.JSplitPane();
        rightSplitPane = new javax.swing.JSplitPane();
		
		defaultDisplayPane = new DisplayPane();
		
		// Create the outer split pane, which houses the file tree panel at left and the another split
		// pane at right		
		int panelWidth = 200;
        if (props.getProperty("fileTreePanel.width")!=null) {
        	int width = Integer.parseInt(props.getProperty("fileTreePanel.width"));
        	if (width>1 && width < 1000)
        		panelWidth = width;
        }
        
        fileTreePanel = new FileTreePanel(this);
        
        outerSplitPane.setDividerLocation(panelWidth);
        
        try {
        	outerSplitPane.setLeftComponent((Component)fileTreePanel);
        }
        catch (Exception ex) {
        	//Some implementations of fileTree may not be components, which I guess we permit 
        	//for now
        }
        
        outerSplitPane.setRightComponent(rightSplitPane);
        outerSplitPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        
        // Create the display pane, which lives at upper right 
        defaultDisplayPane.setMinimumSize(new java.awt.Dimension(120, 200));
        defaultDisplayPane.setPreferredSize(new java.awt.Dimension(120, 350));
        
        ImageIcon cowfishImage = new ImageIcon(iconPath + "justTheFish.png");
        JLabel defaultLabel = new JLabel(cowfishImage);
        JPanel defaultBackground = new DefaultDisplayPaneBackground();
        defaultBackground.add(defaultLabel);
                
		defaultDisplayPane.addTab("Welcome", defaultBackground);
		defaultDisplayPane.setTabComponentAt(defaultDisplayPane.indexOfComponent(defaultBackground) , new TabPaneTab("Welcome", this, null));
		defaultDisplayPane.setOpaque(false);
		
		rightSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        rightSplitPane.setLeftComponent(defaultDisplayPane);
        rightSplitPane.setOpaque(false);
        if (props.getProperty("displayPane.height")!=null) {
        	int dispHeight = Integer.parseInt(props.getProperty("displayPane.height"));
        	if (dispHeight>1 && dispHeight < 1000)
        		rightSplitPane.setDividerLocation(dispHeight);
        }

		rightSplitPane.setBorder(BorderFactory.createEmptyBorder());
		
		
        //Add the analysis pane to the bottom of the right-hand split pane
        analysisPane = new AnalysisPane(this);
        rightSplitPane.setRightComponent(analysisPane);
		
		// Create the bottom panel and labels ////////////////////////////////
		bottomPanel = new FancyPanel();
		bottomPanelRightLabel = new javax.swing.JLabel();
        bottomPanelLeftLabel = new javax.swing.JLabel();
        
        bottomPanelRightLabel.setText("");
		String fontFace = bottomPanelRightLabel.getFont().getFontName();
		int fontSize = 10;
		bottomPanelRightLabel.setFont(new Font(fontFace, Font.PLAIN, fontSize));

		bottomPanelLeftLabel.setText("");
		fontFace = bottomPanelLeftLabel.getFont().getFontName();
		bottomPanelLeftLabel.setFont(new Font(fontFace, Font.PLAIN, fontSize));

        bottomPanel.setMaximumSize(new java.awt.Dimension(32767, 13));
        bottomPanel.setMinimumSize(new java.awt.Dimension(100, 13));
        bottomPanel.setPreferredSize(new java.awt.Dimension(600, 13));
        bottomPanel.setBorder(BorderFactory.createLineBorder(new Color(185, 185, 185)));
        bottomPanel.setBackground(new Color(253, 253, 253));
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.add(bottomPanelLeftLabel);
        bottomPanel.add(Box.createHorizontalGlue());
        bottomPanel.add(bottomPanelRightLabel);
        bottomPanel.add(Box.createHorizontalStrut(10));
		
        //Add stuff to the main panel
        mainPanel.setBorder(BorderFactory.createEmptyBorder(2,4,1,4));
        mainPanel.add(outerSplitPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		
        this.getContentPane().add(mainPanel);
        
		fileChooser = new JFileChooser();
	}


	public void closeSelectedDisplay() {
		defaultDisplayPane.closeSelectedDisplay();
	}

	/**
	 * Causes the display pane to close the selected display.
	 * @param display
	 */
	public void closeDisplay(Display display) {
		defaultDisplayPane.closeDisplay(display);		
	}


//	public void showAboutBox() {
//		if (aboutBox == null) {
//			JFrame mainFrame = SunFishApp.getApplication().getMainFrame();
//			aboutBox = new SunFishAboutBox(mainFrame);
//			aboutBox.setLocationRelativeTo(mainFrame);
//		}
//		SunFishApp.getApplication().show(aboutBox);
//	}

	private JMenuItem redoItem;
	private JMenuItem undoItem;
	private JMenu displaysMenu;
	private JMenuItem closeAllDisplays;
	private JMenuItem reattachAllDisplays;
	private javax.swing.JPanel mainPanel;
	private javax.swing.JPanel bottomPanel;
	private javax.swing.JLabel bottomPanelLeftLabel;
	private javax.swing.JLabel bottomPanelRightLabel;
	private DisplayPane defaultDisplayPane;
	private AnalysisPane analysisPane;
	private javax.swing.JMenuItem importMenuItem;
	private javax.swing.JMenu fileMenu;
	private javax.swing.JMenu editMenu;
	private javax.swing.JMenuBar jMenuBar1;
    private FileTree fileTreePanel;
    private javax.swing.JSplitPane outerSplitPane;
    private javax.swing.JMenuItem quitMenuItem;
    private javax.swing.JSplitPane rightSplitPane;
    JFileChooser fileChooser;

    private JDialog aboutBox;


}
