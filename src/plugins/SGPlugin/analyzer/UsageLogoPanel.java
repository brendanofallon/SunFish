package plugins.SGPlugin.analyzer;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

/**
 * Draws a base frequency 'usage logo' for a sequence group. The Y-scale of
 * the drawn bases are adjusted so that they match the frequencies given
 * by the 'freqs' array. 
 * 
 * @author brendan
 *
 */
public class UsageLogoPanel extends JPanel {
	
	double[] freqs; //Sets the y-scaling of the drawn base characters
	Color backgroundColor = Color.white;
	Font font = new Font("Sans", Font.BOLD, 48); //The font to use for drawing. The initial size
												   //size is probably arbitrary
	AffineTransform transform = new AffineTransform();
	
	boolean showPosition = true;	//Set to true if we should display the site indication marker at the bottom
	int positionLabelHeight = 20;	//How high the position indicating marker should be from the bottom
	Font posFont = new Font("Sans", Font.PLAIN, 12);
	String positionLabel = "";
	
	boolean frequenciesSet = false;
	
	Color aColor = Color.black;
	Color cColor = Color.black;
	Color gColor = Color.black;
	Color tColor = Color.black;
	
	public UsageLogoPanel() {
		freqs = new double[4];
	}
	
	
	/**
	 * Turns on drawing of the position label and connecting horizontal line
	 * @param pos
	 */
	public void setShowPosition(boolean show, int pos) {
		showPosition = show;
		this.positionLabel = String.valueOf(pos);
	}
	
	public void setFrequencies(double[] freqs) {
		this.freqs = freqs;
		double sum = 0;
		for(int i=0; i<4; i++) {
			sum += freqs[i];
		}
		
		for(int i=0; i<4; i++) {
			freqs[i] /= sum;
		}
		
		frequenciesSet = true;
	}
	
	public void setAColor(Color c) {
		aColor = c;
	}
	
	public void setCColor(Color c) {
		cColor = c;
	}
	
	public void setGColor(Color c) {
		gColor = c;
	}
	
	public void setTColor(Color c) {
		tColor = c;
	}
	
	
	private char charForInt(int i) {
		if (i==0) return 'A';
		if (i==1) return 'C';
		if (i==2) return 'T';
		if (i==3) return 'G';
		
		return '?';
	}
	
	private Color colorForInt(int i) {
		if (i==0) return aColor;
		if (i==1) return cColor;
		if (i==2) return tColor;
		if (i==3) return gColor;
		
		return Color.black;
	}
	
	public void paintComponent(Graphics g) {
		g.setColor(backgroundColor);
		g.drawRect(0, 0, getWidth(), getHeight());
		
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setColor(Color.black);
	
		if (!frequenciesSet) {
			return;
		}
		
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics();
		double actualHeight = fm.getAscent()*0.75;
		
		
		int targetHeight = getHeight();	//The final height of all all the stacked bases
		if (showPosition)
			targetHeight -= positionLabelHeight+3;
		
		int gap = 1;	//Y-gap separating the drawn bases
		double scale = (targetHeight-6*gap) / actualHeight; //Scaling coefficient
		
		int prevHeight = 0;
		for(int i=0; i<4; i++) {
			transform.setToIdentity();
			String base = String.valueOf(charForInt(i));
			double mod = 1.0;
			if (base.equals("C") || base.equals("G"))
				mod = 0.98;
			else
				mod = 1.02;
			transform.scale(1.0, mod*scale*freqs[i]);
			g.setColor(colorForInt(i));
			Font derivedFont = font.deriveFont(transform);
			g.setFont(derivedFont);
			fm = g.getFontMetrics();
			
			double realHeight = fm.getAscent()*0.75;
			if (base.equals("C") || base.equals("G"))
				prevHeight += realHeight+gap;
			else
				prevHeight += realHeight+2*gap;
			g.drawString(base, 1, prevHeight);
		}
		
		if (showPosition) {
			g.setColor(Color.gray);
			g.drawLine(0, getHeight()-positionLabelHeight, getWidth(), getHeight()-positionLabelHeight);
			g2d.setFont(posFont);
			fm = g2d.getFontMetrics();
			int posWidth = fm.stringWidth(positionLabel);
			g2d.drawString(positionLabel, getWidth()/2-posWidth/2, getHeight()-1);
		}
		g.setColor(backgroundColor);
	}

}
