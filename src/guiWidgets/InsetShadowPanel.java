package guiWidgets;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class InsetShadowPanel extends JPanel {

	boolean highlightBorder = false;
	final static Color bgColor = new Color(248, 248, 248);
	final static Color highlightColor = new Color(100, 100, 100, 100);
	final static Stroke highlightStroke = new BasicStroke(1.5f);
	final static Color color1 = new Color(150, 150, 150, 150);
	final static Color color2 = new Color(150, 150, 150, 100);
	final static Color color3 = new Color(150, 150, 150, 50);

	final static Color color4 = new Color(255, 255, 255, 50);
	final static Color color5 = new Color(255, 255, 255, 100);
	final static Color color6 = new Color(255, 255, 255, 200);
	
	
	public InsetShadowPanel() {
		this.setBorder(BorderFactory.createEmptyBorder(5, 2, 2, 2));
	}
	
	public void setHighlight(boolean highlight) {
		highlightBorder = highlight;
		repaint();
	}
	
	public void paintComponent(Graphics g) {
		
		g.setColor(bgColor);
		g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
	
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setColor(color1);
		g.drawLine(4, 0, getWidth()-4, 0);
		g.setColor(color2);
		g.drawLine(2, 1, getWidth()-2, 1);
		g.setColor(color3);
		g.drawLine(2, 2, getWidth()-2, 2);

		g.setColor(color2);
		g.drawLine(1, 2, 1, getHeight()-2);
		g.setColor(color3);
		g.drawLine(2, 2, 2, 4);
		g.drawLine(3,3,4,3);
		
		g.setColor(color4);
		g.drawLine(2, getHeight()-3, getWidth()-2, getHeight()-3);
		g.drawLine(getWidth()-2, 3, getWidth()-2, getHeight()-2);
		g.drawLine(getWidth()-3, getHeight()-3, getWidth()-3, getHeight()-2);
		g.setColor(color5);
		g.drawLine(3, getHeight()-2, getWidth()-3, getHeight()-2);
		g.drawLine(getWidth()-1, 4, getWidth()-1, getHeight()-2);
		g.setColor(color6);
		g.drawLine(4, getHeight()-1, getWidth()-4, getHeight()-1);
		
		
		g.setColor(color2);
		g.drawRoundRect(0, 0, getWidth()+1, getHeight()+1, 10, 10);
		
		if (highlightBorder) {
			g.setColor(highlightColor);
			((Graphics2D) g).setStroke(highlightStroke);
			g.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
		}
		
	}
	
}
