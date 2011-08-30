package fileTree;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * A class to paint the fancy border around top level tree blocks
 * @author brendan
 *
 */
public class TLBPanel extends JPanel {

	static Color bgColor = new Color(253, 253, 253);
	
	static Color gray1 = Color.white;
	static Color gray2 = new Color(250, 250, 250, 100);
	static float topDark = 0.935f;
	static Color dark1 = new Color(topDark, topDark, topDark);
	static Color dark2 = new Color(220, 220, 220, 100);
	static Color shadowColor = new Color(0f, 0f, 0f, 0.1f);
	static Color lineColor = new Color(200, 200, 200);
	
	static Stroke shadowStroke = new BasicStroke(1.6f);
	static Stroke normalStroke = new BasicStroke(1.0f);
	
	public TLBPanel() {
		setBackground(bgColor);
		setBorder(BorderFactory.createEmptyBorder(5, 3, 0, 0));
	}
	
	public void paintComponent(Graphics g) {
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setColor(shadowColor);
		((Graphics2D)g).setStroke(shadowStroke);
		g.drawRoundRect(4, 4, getWidth()-5, getHeight()-5, 8, 8);
		
		g.setColor(bgColor);
		((Graphics2D)g).setStroke(normalStroke);
		g.fillRoundRect(1, 1, getWidth()-3, getHeight()-2, 5, 5);
	
		//A gradient
		float gradMax = Math.min(200, Math.max( getHeight()/2f, 20));
		g.setColor(gray2);
		g.drawLine(3, 2, getWidth()-4, 2);
		g.setColor(dark1);
		g.drawLine(3, 3, getWidth()-4, 3);
		g.drawLine(2, 4, getWidth()-2, 4);
		for(float i=5; i<gradMax; i++) {
			float newVal = topDark + (0.99f-topDark)*(1-(gradMax-i)/gradMax );
			g.setColor( new Color(newVal, newVal, newVal));
			g.drawLine(1, (int)i, getWidth()-2, (int)i);
		}
		
		g.setColor(lineColor);
		g.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 5, 5);
	}
	
}
