package guiWidgets;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.border.Border;


/**
 * A panel with a rounded-rectangle, inset-looking border. Currently used to house
 * the output analyses. 
 * @author brendan
 *
 */
public class RoundedBezelPanel extends javax.swing.JPanel {
	
	int arcVal = 20;
	final static int baseVal = 230;
	final static Color color1 = new Color(baseVal, baseVal, baseVal, 255);
	final static Color color2 = new Color(baseVal-5, baseVal-5, baseVal-10, 255);
	final static Color color3 = new Color(baseVal-20, baseVal-20, baseVal-20, 255);

	int leftInset = 1;
	int topInset = 2;
	int rightInset = 1;
	int bottomInset = 2;
	
	Shape rRect;
	Color interiorColor = new Color(255, 255, 255);
	
	boolean highlightBorder = false; //If we should highlight the border 
	//boolean mouseOver = false; //True if the mouse is over the panel
	Color highlightColor = Color.LIGHT_GRAY;
	
	private boolean shadowAtTop = true;
	
	Rectangle innerBounds;
	
	Point mousePos = new Point(0, 0);
	
	public RoundedBezelPanel() {
		super.setBorder(BorderFactory.createEmptyBorder(topInset+2, leftInset+4, bottomInset+2, rightInset));
		setLayout(new BorderLayout());
		setOpaque(true);

		innerBounds = new Rectangle(0, 0, 0, 0);
	}
	
	public void setArcWidth(int width) {
		arcVal = width;
	}
	
	public void setHighlight(boolean highlight) {
		highlightBorder = highlight;
		repaint();
	}
	
	/**
	 * We don't let anything set the border for these panels - the whole point is that 
	 * it already has a decent border.
	 */
	public void setBorder(Border b) {
		return;
	}
	
	public void setShadowAtTop(boolean onTop) {
		shadowAtTop = onTop;
	}
	
	public void paint(Graphics g) {
		
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		innerBounds.x = leftInset;
		innerBounds.y = topInset;
		innerBounds.width = getWidth()-1-leftInset-rightInset;
		innerBounds.height = getHeight() - 1 - topInset - bottomInset;
		
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(interiorColor);
		g.fillRoundRect(leftInset, topInset, getWidth()-1-leftInset-rightInset, getHeight()-1-topInset-bottomInset, arcVal, arcVal);

		super.paint(g);
		
		if (highlightBorder) {
			g.setColor(highlightColor);
			((Graphics2D) g).setStroke(new BasicStroke(1.3f));
			g.drawRoundRect(leftInset, topInset, getWidth()-1-leftInset-rightInset, getHeight()-1-topInset-bottomInset, arcVal, arcVal);
			g.drawRoundRect(leftInset+1, topInset+1, getWidth()-2-leftInset-rightInset, getHeight()-2-topInset-bottomInset, arcVal, arcVal);
			g.drawRoundRect(leftInset+1, topInset+1, getWidth()-2-leftInset-rightInset, getHeight()-2-topInset-bottomInset, arcVal/2, arcVal/2);
			g.drawRoundRect(leftInset+1, topInset+2, getWidth()-3-leftInset-rightInset, getHeight()-4-topInset-bottomInset, arcVal, arcVal);
			((Graphics2D) g).setStroke(new BasicStroke(1.0f));
		}
		
		if (shadowAtTop) {
			g.setColor(color3);
			g.drawRoundRect(leftInset, topInset+2, getWidth()-2-leftInset-rightInset, getHeight()-2-topInset-bottomInset, arcVal, arcVal);


			g.setColor(color2);
			g.drawRoundRect(leftInset, topInset+1, getWidth()-1-leftInset-rightInset, getHeight()-2-topInset-bottomInset, arcVal, arcVal);

			g.setColor(color1);
			g.drawRoundRect(leftInset, topInset+0, getWidth()-1-leftInset-rightInset, getHeight()-1-topInset-bottomInset, arcVal, arcVal);
		}
		else {
			g.setColor(color3);
			g.drawRoundRect(leftInset+2, topInset+2, getWidth()-2-leftInset-rightInset, getHeight()-2-topInset-bottomInset, arcVal, arcVal);

			g.setColor(color2);
			g.drawRoundRect(leftInset+1, topInset+1, getWidth()-1-leftInset-rightInset, getHeight()-2-topInset-bottomInset, arcVal, arcVal);

			g.setColor(color1);
			g.drawRoundRect(leftInset, topInset+0, getWidth()-1-leftInset-rightInset, getHeight()-1-topInset-bottomInset, arcVal, arcVal);
			
			g.setColor(color2);
			g.drawRoundRect(leftInset, topInset, getWidth()-1-leftInset-rightInset, getHeight()-1-topInset-bottomInset, arcVal, arcVal);

			
		}
		
		
	}

	
}
