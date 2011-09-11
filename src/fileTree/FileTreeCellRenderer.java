/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fileTree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.io.File;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;


import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;



/**
 *
 * @author brendan
 */
public class FileTreeCellRenderer extends DefaultTreeCellRenderer {

	int row;
	Rectangle parentBounds;
	JTree tree;
	SunFishFrame parentFrame;
	Font parsableFileFont;
	Font unknownFileFont;
	Font topLevelFont;
	int defaultFontSize = 10;
	
	ImageIcon imageIcon = null;
	ImageIcon unknownIcon;
	String unknownIconPath;
	boolean isDir;
	boolean isUnknown;

	boolean topLevel;
	Color directoryColor = Color.DARK_GRAY;
	
	static ImageIcon openArrowIcon;
	static ImageIcon closedArrowIcon;
	static ImageIcon openHighlightIcon;
	static ImageIcon closedHighlightIcon;
	ImageIcon topLevelIcon;
	static boolean iconsInitialized = false;
	
	TopLevelTreeBlock parentBlock;
	boolean mouseOver = false;
	
	Dimension dim;
	
	
	boolean selected;
	public static Color selectionColor = javax.swing.UIManager.getColor( "Tree.selectionBackground" );
	public static final Color white0 = new Color(255, 255, 255, 150);
	public static final Color white1 = new Color(255, 255, 255, 80);
	public static final Color white3 = new Color(255, 255, 255, 25);

	boolean isMac;
	
	private static final long serialVersionUID = 1L;

	public FileTreeCellRenderer(TopLevelTreeBlock parentBlock, SunFishFrame parentFrame) {
		this.parentFrame = parentFrame;
		this.parentBlock = parentBlock;
		String fontSizeStr = parentFrame.getProperty("filetree.fontsize");
		int fontSize = defaultFontSize;
		
		dim = new Dimension(250, 18); //Seems fine, if not important, to set this to something really big
		setPreferredSize(dim);
		setOpaque(false);
		
        isMac = SunFishFrame.getSunFishFrame().onAMac();
        
		if ( selectionColor == null )
			selectionColor = java.awt.SystemColor.textHighlight;
		
		if (parentFrame.getProperty("unknown.icon")!=null) {
			unknownIconPath = parentFrame.getIconPath() + parentFrame.getProperty("unknown.icon");
			unknownIcon = new ImageIcon(unknownIconPath);
		}

		
		if (fontSizeStr != null) {
			try {
				int tryFont = Integer.parseInt(fontSizeStr); 
				fontSize = tryFont;
			}
			catch (NumberFormatException nfe) {
				//forget it.. just use the default font size
			}
		}
		
		parsableFileFont = new Font("Sans", Font.BOLD, fontSize);
		unknownFileFont = new Font("Sans", Font.PLAIN, fontSize);
		topLevelFont = new Font("Sans", Font.BOLD, fontSize+1);
	
		setBackground(new Color(1, 1, 1, 0));
		
		if (!iconsInitialized && !isMac) {
			initializeIcons();
		}
		
		//Load this icon even if we are on a macintosh
		topLevelIcon = parentBlock.getTopLevelIcon(); 
	}
	
	private void initializeIcons() {
		openArrowIcon = new ImageIcon("./icons/triangle_6.png");
		openHighlightIcon = new ImageIcon("./icons/triangle_highlight6.png");
		closedArrowIcon = new ImageIcon("./icons/triangle_1.png");
		closedHighlightIcon = new ImageIcon("./icons/triangle_highlight.png");		
			
		iconsInitialized = true;
		
		super.openIcon = openArrowIcon;
		super.closedIcon = closedArrowIcon;
	}
	
	
	
	public Component getTreeCellRendererComponent(JTree tree,
                                       Object value,
                                       boolean selected,
                                       boolean expanded,
                                       boolean leaf,
                                       int row,
                                       boolean hasFocus) {
		
		topLevel = row==0;

		this.selected = selected;
		
		if (leaf)
			setIcon(getLeafIcon());
		else if (expanded)
			setIcon(getOpenIcon());
		else
			setIcon(getClosedIcon());

		//super.selected = false;
		this.hasFocus = false;
		setHorizontalAlignment(LEFT);
		setOpaque(false);
		setVerticalAlignment(CENTER);
		setEnabled(true);
		super.setFont(topLevelFont);

		String text = value.toString();
		
		mouseOver = row==parentBlock.getMouseOverRow();
		
		Object userObj = ((DefaultMutableTreeNode)value).getUserObject();
		
		if (userObj instanceof TreeFile) {
			TreeFile wrappedFile = (TreeFile)userObj;
			text = wrappedFile.getFile().getName();
			File file = wrappedFile.getFile();
			isDir = !((DefaultMutableTreeNode)value).isLeaf(); //file.isDirectory();
			setOpaque(false);
			
			setForeground(Color.black);	
			setIcon(null);

			FileParser parser = wrappedFile.getParser();
			
			if (parser!=null) {
				setForeground(parser.getLabelColor());
				setFont(parsableFileFont);
				if (parser.getIcon()!=null) {
					imageIcon = parser.getIcon();
					setIcon(parser.getIcon());
				}

				isDir = false;
				isUnknown = false;
			}
			else { //Parser is null
				setForeground(Color.GRAY);
				setFont(unknownFileFont);
				setText(text);
				isUnknown = true;
				if (unknownIconPath!=null) {
					unknownIcon = new ImageIcon(unknownIconPath);
					imageIcon = unknownIcon;
					setIcon(unknownIcon);
				}
			}

			if (!leaf) {
				isDir = true; //Flag to not override if we're also top level
				setForeground(directoryColor);
				setFont(parsableFileFont);

				if (expanded) {
					if (mouseOver)
						setIcon(openHighlightIcon);
					else
						setIcon(openArrowIcon);
				}
				else {
					if (mouseOver)
						setIcon(closedHighlightIcon);
					else
						setIcon(closedArrowIcon);
				}

			} //user object was a TreeFile
		
		}
		else {
			if (userObj == null)
				text = "?";
			
		}
		
		
		setText(text);
		
		//This is a top-level item, we use different formatting for it
		if (topLevel) {
			setFont(topLevelFont);
			setForeground(Color.DARK_GRAY);
			setIcon(topLevelIcon); 
		}
		
		return this;
	}
	
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		//System.out.println("Painting tree cell for row: " + row + " which is : " + getText() + " width is: " + getWidth());
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		
		if (selected && !topLevel) {
			g.setColor(selectionColor);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(white3);
			g.fillRoundRect(getWidth()/2, -4, getWidth(), 100, 100,100);
		}
		
		int textOffset = 1;
		if (getIcon()!=null ) {
			int height = getIcon().getIconHeight();
			int imageOffset = 0;
			if (isMac && topLevel)
				imageOffset = 5;
			g.drawImage( ((ImageIcon)getIcon()).getImage(), imageOffset, getHeight()/2-height/2, null);
			
			textOffset += getIcon().getIconWidth()+1+imageOffset;
		}
		
		g.setFont(getFont());
		g.setColor(white1);
		g.drawString(getText(), textOffset+2, getHeight()-4);
		g.setColor(getForeground());
		g.drawString(getText(), textOffset+1, getHeight()-5);

		
	}

}
