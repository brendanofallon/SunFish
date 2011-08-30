package fileTree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import topLevelGUI.DirectoryFileFilter;
import topLevelGUI.SunFishFrame;

import display.DisplayData;
import guiWidgets.CFButton;
import guiWidgets.FancyPanel;
import guiWidgets.IconButton;


/**
 * The panel which manages the list of files. Currently, the scheme is that a number of
 * 'top level blocks' exist, each of which is a jpanel and presents its own sub-display.
 * Subclasses of TopLevelTreeBlock implement different kinds of things to display, for 
 * instance, a directory in the file system, or the list of recent files (or a remote 
 * server? or cloud storage?)
 * @author brendan
 *
 */
public class FileTreePanel extends JPanel {

	SunFishFrame sunfishParent;
	Logger logger;
	String iconPath = "./icons/";
	
	ArrayList<TopLevelTreeBlock> topLevelBlocks;
	RecentItemsBlock recentItemsBlock;
	
	JFileChooser fileChooser;
	
	private boolean firstFilterClick = true;
	
	Component glueBox;
	
	int rightBlockPadding = 10; //Amount of space between right edge of blocks and edge of scroll pane
	int topBlockPadding = 3; //Amount of space above blocks
	
	public FileTreePanel(SunFishFrame parent) {
		sunfishParent = parent;
		
		iconPath = sunfishParent.getIconPath();
		
		topLevelBlocks = new ArrayList<TopLevelTreeBlock>();
		
		setOpaque(false);
		
		glueBox = Box.createVerticalGlue();
		glueBox.setMinimumSize(new Dimension(1, 25));
		initializeComponents();
		
        initializeBlocks();
       
        HighlightListener hl = new HighlightListener();
        treeScrollPane.addMouseMotionListener(hl);
        treeScrollPane.addMouseListener(hl);
	}
	
	/**
	 * Ensures that the panel holding all of the top level blocks is tall enough to contain them all, this is called
	 * after tree nodes have been expanded or contracted. 
	 */
	public void recalculateBlockHeights() {
		int totHeight = 0;
		int maxWidth = 100;
		for(TopLevelTreeBlock tlBlock : topLevelBlocks) {
			if (tlBlock.getPreferredSize()!=null)
				totHeight += tlBlock.getPreferredSize().height;
			else
				totHeight += tlBlock.getHeight();
			
			if (tlBlock.getWidth()>maxWidth)
				maxWidth = tlBlock.getWidth();
		}
		
		//System.out.println("Recalculating panel height, total is: " + totHeight);
		topLevelPanel.setMinimumSize(new Dimension(1, totHeight));
		topLevelPanel.setPreferredSize(new Dimension(maxWidth, totHeight));
		topLevelPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, totHeight));
		topLevelPanel.revalidate();
	}


	/**
	 * Creates all top level blocks 
	 */
	protected void initializeBlocks() {
		recentItemsBlock = new RecentItemsBlock(this);
		recentItemsBlock.setMaximumSize(new Dimension(topLevelPanel.getWidth(), 34));

		topLevelPanel.add(recentItemsBlock);
		topLevelBlocks.add(recentItemsBlock);
		
		for(int i=0; i<50; i++) {
			String path = sunfishParent.getProperty("fileTree.block." + Integer.valueOf(i));
			if (path!=null) {
				File file = new File(path);
				addDirectoryBlock(file, false);
			}
			
			
			path = sunfishParent.getProperty("fileTree.recentItem." + Integer.valueOf(i));
			if (path!=null) {
				File file = new File(path);
				recentItemsBlock.addItem(file);
			}
		}
		
		topLevelPanel.setAlignmentX(LEFT_ALIGNMENT);
		topLevelPanel.add(glueBox);
	}
	

	/**
	 * Add a new directory to be represented as a block in the file tree, the first row of the
	 * tree will be opened. If the file is not a directory, the parent directory of the file is added. 
	 * @param dir
	 */
	public void addDirectoryBlock(File dir) {
		addDirectoryBlock(dir, true);
	}
	
	/**
	 * Add a new directory to be represented as a block in the file tree. If the file is not a
	 * directory, the parent directory of the file is added.
	 * @param dir
	 */
	public void addDirectoryBlock(File dir, boolean expandFirstRow) {
		topLevelPanel.remove(glueBox);
		
		if (! dir.exists()) {
			JOptionPane.showMessageDialog(sunfishParent, "Could not find the directory for file " + dir.getName());
			topLevelPanel.add(glueBox);
			return;
		}
		
		if (! dir.isDirectory()) {
			dir = dir.getParentFile();
			if (!dir.isDirectory()) {
				JOptionPane.showMessageDialog(sunfishParent, "Could not find the directory for file " + dir.getName());
				topLevelPanel.add(glueBox);
				return;
			}
		}
	
		TopLevelTreeBlock newBlock = new DirectoryTreeBlock(this, dir);
		newBlock.setPreferredSize(new Dimension(treeScrollPane.getWidth()-rightBlockPadding, 35));
		newBlock.setMaximumSize(new Dimension(treeScrollPane.getWidth()-rightBlockPadding, Integer.MAX_VALUE));
		if (expandFirstRow)
			newBlock.getTree().expandRow(0);
		else {
			newBlock.getTree().collapseRow(0);
		}
		topLevelPanel.add(Box.createVerticalStrut(topBlockPadding));
		topLevelPanel.add(newBlock);
		
		topLevelBlocks.add(newBlock);
		
		topLevelPanel.add(glueBox);
		
		topLevelPanel.revalidate();
		repaint();
	}
	
	/**
	 * Unhighlights all blocks except the given block... no longer used since blocks aren't highlighted
	 * @param tlBlock
	 */
//	public void unhighlightOtherBlocks(TopLevelTreeBlock tlBlock) {
//		for(TopLevelTreeBlock block : topLevelBlocks) {
//			if (tlBlock!=block) {
//				block.setHighlight(false);
//			}
//		}
//		tlBlock.setHighlight(true);
//	}


	/**
	 * For properties file writing
	 * @return
	 */
	public List<File> getRecentFiles() {
		return recentItemsBlock.getRecentFiles();
	}
	
	
	public SunFishFrame getParentFrame() {
		return sunfishParent;
	}
	
	public void setFilePath(String newPath) {
		
	}
    
    public void setFilter(String newFilter) {
    	for(TopLevelTreeBlock tlBlock : topLevelBlocks) {
    		tlBlock.setCurrentFilter(newFilter);
    	}
    }
    
    /**
     * Add a new file to the recent items list
     * @param file
     */
	public void addRecentFile(File file) {
		recentItemsBlock.addItem(file);
	}
	
//	protected void resetBlockWidth() {
//		int newWidth = Math.max(100, treeScrollPane.getWidth()-20);
//		//System.out.println("Setting new block width to: " + newWidth);
//		int totHeight = 0;
//		for(TopLevelTreeBlock tlBlock : topLevelBlocks) {
//			tlBlock.setPreferredSize(new Dimension(newWidth, tlBlock.getHeight()));
//			tlBlock.setMinimumSize(new Dimension(newWidth, 1));
//			tlBlock.setMaximumSize(new Dimension(newWidth, Integer.MAX_VALUE));
//			tlBlock.resizeBlock();
//			totHeight += tlBlock.getHeight();
//		}
//
//		treeScrollPane.revalidate();
//		repaint();
//		//System.out.println("Sum heights: " + totHeight + " top level panel height is: " + topLevelPanel.getHeight() + " scroll pane max: " + treeScrollPane.getVerticalScrollBar().getMaximum());
//	}
	
	protected void scrollPaneResized() {
		int scrollPaneWidth = treeScrollPane.getWidth();
		int newWidth = Math.max(100, scrollPaneWidth-rightBlockPadding);
		int currentHeight = topLevelPanel.getHeight();
		topLevelPanel.setPreferredSize(new Dimension(newWidth, currentHeight));
		topLevelPanel.setMinimumSize(new Dimension(newWidth, 10));
		for(TopLevelTreeBlock tlb : topLevelBlocks) {
			tlb.resizeBlock(newWidth);
		}
		//System.out.println("Scroll pane width : " + newWidth);
		for(TopLevelTreeBlock tlBlock : topLevelBlocks) {
			tlBlock.getTree().revalidate();
			try {
				Component renderer = (Component)tlBlock.getTree().getCellRenderer();
				renderer.setPreferredSize(new Dimension(newWidth-10, 18));
				//System.out.println("Setting size to " + (newWidth-10) + ", " + renderer.getHeight());
				tlBlock.getTree().setCellRenderer((TreeCellRenderer)renderer);
			}
			catch (Exception ex) {
				System.err.println("Could not set width for tree cell renderer");
			}
		}

		
		repaint();
	}
	
	public void unselectAllTrees() {
		for(TopLevelTreeBlock tlBlock : topLevelBlocks) {
			tlBlock.unselectAll();
		}
	}
    

	public void removeBlock(TopLevelTreeBlock blockToRemove) {
		topLevelBlocks.remove(blockToRemove);
		topLevelPanel.remove(blockToRemove);
		revalidate();
		repaint();
	}
	
	/**
	 * Return an icon associated with the given url. For instance, if the url is icons/folder.png, we look in the
	 * package icons for the image folder.png, and create and return an icon from it. 
	 * @param url
	 * @return
	 */
	public static ImageIcon getIcon(String url) {
		ImageIcon icon = null;
		try {
			java.net.URL imageURL = FileTreePanel.class.getResource(url);
			icon = new ImageIcon(imageURL);
		}
		catch (Exception ex) {
			SunFishFrame.getSunFishFrame().getLogger().warning("Error loadind icon from resouce : " + ex);
		}
		return icon;
	}
	
	public void initializeComponents() {
		topLevelPanel = new JPanel();
		topLevelPanel.setLayout(new BoxLayout(topLevelPanel, BoxLayout.Y_AXIS));
//		topLevelPanel.setBackground(Color.white);
//		topLevelPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
		
		topLevelPanel.add(Box.createVerticalStrut(5));

        addFolderButton = new IconButton(getIcon("icons/addFolder.png"), "Add a new folder" );
		addFolderButton.setPreferredSize(new Dimension(29, 29) );
		addFolderButton.setMaximumSize(new Dimension(29, 29) );
        addFolderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //treeReloadButtonActionPerformed(evt);
            	addFolderAction();
            }
        });
        
        IconButton openFileButton = new IconButton(getIcon("icons/openFile.png"), "Open a file" );
        
        openFileButton.setPreferredSize(new Dimension(27, 27) );
        openFileButton.setMaximumSize(new Dimension(27, 27) );
        openFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	openFileAction();
            }
        });

        treeChangeButton = new IconButton(getIcon("icons/searchFile.png"), "Search for files");
		treeChangeButton.setPreferredSize(new Dimension(27, 27));
		treeChangeButton.setMaximumSize(new Dimension(27, 27));
        treeChangeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	unselectAllTrees();
            	int count = 0;
            	int th = 0;
            	for(TopLevelTreeBlock tlb : topLevelBlocks) {
            		System.out.println("Height of block #" + count + " : " + tlb.getHeight());
            		th += tlb.getHeight();
            		count++;
            	}
            	System.out.println("Total height of tlbs: " + th);
            	System.out.println("Height of topLevelPanel : " + topLevelPanel.getHeight());
            	System.out.println("Scroll bar max: " + treeScrollPane.getVerticalScrollBar().getMaximum());
            }
        });
		
        treeSearchField = new JTextField();
        treeSearchField.setFont(new java.awt.Font("DejaVu Sans", 0, 11));
        treeSearchField.setText("Filter");
        treeSearchField.setMinimumSize(new java.awt.Dimension(10, 24));
        treeSearchField.setPreferredSize(new java.awt.Dimension(125, 24));
        treeSearchField.setMaximumSize(new java.awt.Dimension(155, 24));
        treeSearchField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setFilter(treeSearchField.getText());
            }
        });
        treeSearchField.setForeground(Color.gray);
        treeSearchField.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(java.awt.event.MouseEvent evt) {
        		searchFieldClicked();	
        	}
        });

        JPanel topButtonPanel = new JPanel();
        topButtonPanel.setOpaque(false);
        
        topButtonPanel.setLayout(new BoxLayout(topButtonPanel, BoxLayout.X_AXIS));
        topButtonPanel.add(Box.createHorizontalStrut(4));
        topButtonPanel.add(addFolderButton);
        topButtonPanel.add(Box.createHorizontalStrut(4));
        topButtonPanel.add(openFileButton);
        topButtonPanel.add(Box.createHorizontalStrut(4));
        topButtonPanel.add(treeChangeButton);
        topButtonPanel.add(Box.createGlue());
        topButtonPanel.add(treeSearchField);
        topButtonPanel.add(Box.createHorizontalStrut(10));
        
//        IconButton newButton = new IconButton(new ImageIcon(iconPath + "searchFile.png"), "Search for files");
//        newButton.setPreferredSize(new Dimension(24, 24));
//        topPanel.add(newButton);
        
        topButtonPanel.setPreferredSize(new Dimension(150, 30));
        topButtonPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 32));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        treeScrollPane = new JScrollPane(topLevelPanel);
        treeScrollPane.setViewportBorder(null); //BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        treeScrollPane.setBorder(null);
       // treeScrollPane.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        treeScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        treeScrollPane.addComponentListener(new ComponentListener() {
			public void componentResized(ComponentEvent e) {
				scrollPaneResized();
			}

			public void componentShown(ComponentEvent e) {	}     
			public void componentHidden(ComponentEvent e) {	}
			public void componentMoved(ComponentEvent e) {	}
        });
        this.add(Box.createVerticalStrut(15));
        this.add(topButtonPanel);
        this.add(treeScrollPane);			
	}


	/**
	 * Prompt the user to open & display a new file, this is called when the user clicks on the "open file" button or selects
	 * Open from the File menu
	 */
	public void openFileAction() {
		if (fileChooser == null)
			fileChooser = new JFileChooser();
		int n = fileChooser.showOpenDialog(sunfishParent);
		
		if (n==JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			sunfishParent.displayFile(file);
		}	
	}

	/**
	 * Make it so the first click to the tree search field erases the text
	 */
	protected void searchFieldClicked() {
		if (firstFilterClick) {
			treeSearchField.setText("");
			treeSearchField.setForeground(Color.black);
		}
		firstFilterClick = false;
	}


	/**
	 * Called when the user wants to add a new top level block based on a directory
	 */
	protected void addFolderAction() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new DirectoryFileFilter());
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int n = fileChooser.showOpenDialog(this);
		if (n == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			if (file!=null && file.exists() && file.isDirectory()) {
				addDirectoryBlock(file);
			}
			else {
				logger.warning("Invalid path for add top level directory block : " + file.getAbsolutePath());	
			}
		}
	}
	
	/**
	 * Returns the list of absolute file paths from all top level directories; this
	 * is used for file writing
	 * @return
	 */
	public List<String> getTopLevelPaths() {
		List<String> paths = new ArrayList<String>();
		for(TopLevelTreeBlock block : topLevelBlocks) {
			if (block instanceof DirectoryTreeBlock) {
				try {
					File rootFile = ((DirectoryTreeBlock)block).getTreeRoot();
					String path = rootFile.getAbsolutePath();
					paths.add(path);
				}
				catch (Exception e) {
					System.err.println("Error getting a tree file from root node of tree block...");
				}
			}
		}
		return paths;
	}
	
	class HighlightListener extends MouseAdapter {
		
//		public void mouseMoved(MouseEvent me) {
//			for(TopLevelTreeBlock tlBlock : topLevelBlocks) {
//				tlBlock.setHighlight(false);
//				
//			}
//		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			for(TopLevelTreeBlock tlBlock : topLevelBlocks) {
				tlBlock.setHighlight(false);
			}
		}

		
	}
	
	
	JPanel topLevelPanel;
    private javax.swing.JScrollPane treeScrollPane;
    private javax.swing.JTextField treeSearchField;
    private IconButton addFolderButton;
    private IconButton treeChangeButton;
    
}
