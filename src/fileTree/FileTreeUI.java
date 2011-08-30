package fileTree;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.plaf.metal.MetalTreeUI;
import javax.swing.tree.TreePath;

/**
 * Implements a couple of custom look and feel items for the file tree. This is mostly
 * because the GTK UI doesn't properly handle the icon drawing for non-leaf tree nodes, so
 * we install this version instead to override the GTK UI.
 * @author brendan
 *
 */
public class FileTreeUI extends BasicTreeUI {

	protected void paintHorizontalPartOfLeg(Graphics g, 
								Rectangle clipBounds, Insets insets, Rectangle bounds, 
								TreePath path, 
								int row, 
								boolean isExpanded, 
								boolean hasBeenExpanded, 
								boolean isLeaf) { };
								
	protected void 	paintVerticalPartOfLeg(Graphics g, 
								Rectangle clipBounds, 
								Insets insets, 
								TreePath path) { };
}
