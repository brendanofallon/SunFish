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
	
	private int leftInset = 1; 
	private int rightInset = 1;
	private int topInset = 1;
	private int bottomInset = 1;
	
	public InsetShadowPanel(int topInset, int leftInset, int bottomInset, int rightInset) {
		this.leftInset = leftInset;
		this.topInset = topInset;
		this.rightInset = rightInset;
		this.bottomInset = bottomInset;
		this.setBorder(BorderFactory.createEmptyBorder(topInset+1,leftInset+1,bottomInset,rightInset));
		
	}
	
	public InsetShadowPanel() {
		this.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
	}
	
	public void setHighlight(boolean highlight) {
		highlightBorder = highlight;
		repaint();
	}
	
	public void paint(Graphics g) {
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setColor(bgColor);
		g.fillRoundRect(leftInset, topInset, getWidth()-(leftInset+rightInset), getHeight()-(topInset+bottomInset), 10, 10);
		
		super.paint(g);

		//Shadow down from top
		g.setColor(color1);
		g.drawLine(leftInset+4, topInset, getWidth()-4-rightInset, topInset);
		g.setColor(color2);
		g.drawLine(leftInset+2, topInset+1, getWidth()-2-rightInset, topInset+1);
		g.setColor(color3);
		g.drawLine(leftInset+2, topInset+2, getWidth()-2-rightInset, topInset+2);

		g.setColor(color2);
		g.drawLine(leftInset+1, topInset+2, leftInset+1, getHeight()-2-bottomInset);
		g.setColor(color3);
		g.drawLine(leftInset+2, topInset+2, leftInset+2, topInset+4);
		g.drawLine(leftInset+3, topInset+3,leftInset+4, topInset+3);
		
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
		g.drawRoundRect(leftInset, topInset, getWidth()+1-(leftInset+rightInset), getHeight()+1-(topInset+bottomInset), 10, 10);
		
		if (highlightBorder) {
			g.setColor(highlightColor);
			((Graphics2D) g).setStroke(highlightStroke);
			g.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
		}
		
	}
	
}
