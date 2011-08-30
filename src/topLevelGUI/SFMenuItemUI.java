package topLevelGUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.plaf.basic.BasicMenuUI;
import javax.swing.plaf.basic.BasicPopupMenuUI;

public class SFMenuItemUI extends BasicPopupMenuUI {

	public void paint(Graphics g, JComponent c) {
		super.paint(g, c);
		System.out.println("Yep it's getting called");
	}
	
}
