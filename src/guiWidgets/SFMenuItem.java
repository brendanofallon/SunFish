package guiWidgets;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.plaf.basic.BasicMenuItemUI;

import topLevelGUI.SFMenuItemUI;



/**
 * the GTK look and feel doesn't always seem to render the backgrounds or borders of JMenuItems
 * properly. So instead of using the default JMenuItem, all menu items used in SunFish are derived
 * from this class, which ensures that menu borders are properly drawn (FYI, apparently in GTK
 * all Menus are popup menus, so when paintBorder is called here, we attempt to set the border 
 * painted property of our parent. This is a little inefficient since it requires a call to
 * instanceof every time we try to paint if our parent is not a popup menu, but it's probably not
 * too big of a performance hit). 
 * @author brendan
 *
 */
public class SFMenuItem extends JMenuItem {

	
	public SFMenuItem() { };
	JPopupMenu parentMenu = null; //The parent is actually a popup in GTK (and others?)
	
	public SFMenuItem(String title) {
		super(title);

	}
	
//	public void paintBorder(Graphics g) {
//		g.setColor(Color.GRAY);
//		
//		if (parentMenu==null) {
//			Container p = getParent();
//			if (p instanceof JPopupMenu) {
//				parentMenu = (JPopupMenu)p;
//				parentMenu.setBorderPainted(true);
//				parentMenu.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
//			}
//
//		}
//	}
}
