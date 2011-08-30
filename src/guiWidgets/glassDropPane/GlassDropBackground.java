package guiWidgets.glassDropPane;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class GlassDropBackground extends javax.swing.JPanel {

	GlassDropPane parent;
	
	public GlassDropBackground(GlassDropPane parent) {
		super();
		setOpaque(true);
		this.parent = parent;
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;

		drawBackground(g2d, 0, 0, getWidth(), getHeight());
		g2d.setColor(new Color(0.8f, 0.8f, 0.8f));
		g2d.drawLine(0, 0, 0, getHeight()-1);	//left edge
		g2d.drawLine(getWidth()-1, 0, getWidth()-1, getHeight()-1); //right edge
		if (! parent.isOpen()) {
			g2d.drawLine(0, 21, getWidth()-1, 21); //bottom
		}		
	}
	
	public static void drawBackground(Graphics2D g2d, int x, int y, int width, int height) {
		for(int i=1; i<height; i++) {
			float val = 1.0f-0.1f*(float)i/(float)height;
			g2d.setColor(new Color(val, val, val));
			g2d.drawLine(x, i+y, x+width, i+y);
			
		}
		
		g2d.setColor(new Color(0.8f, 0.8f, 0.8f));
		g2d.drawLine(x, y, x+width-1, y);	//top edge
		

	}
}
