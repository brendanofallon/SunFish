package guiWidgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class PrettyLabel extends javax.swing.JLabel {

	String text;
	int verticalOffset = 0;
	Color highlightColor = new Color(1f, 1f, 1f, 0.5f);
	Color textColor = new Color(0.1f, 0.1f, 0.1f);
	ImageIcon imageIcon;
	
	public PrettyLabel() {
		super();
		setMinimumSize(new Dimension(12, 20));

	}
	
	public PrettyLabel(ImageIcon icon, String label) {
		super(label);
		imageIcon = icon;
		setIcon(icon);
		setMinimumSize(new Dimension(12, 20));
	}
	
	public PrettyLabel(String label) {
		super(label);
		setMinimumSize(new Dimension(12, 20));
	}
	
	public PrettyLabel(String label, int offset) {
		this(label);
		verticalOffset = offset;
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		text = getText();
		int textOffset = 0;

		if (imageIcon != null) {
			int height = imageIcon.getIconHeight();
			textOffset = imageIcon.getIconWidth()+2;
			int y = Math.max(0, (getHeight()-height)/2);
			g2d.drawImage(imageIcon.getImage(), 0, y, null);
		}
		
		FontMetrics fm = g2d.getFontMetrics();
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		
		
		int labelHeight = (int)fm.getStringBounds(text, 0, text.length(), g2d).getHeight();
		//int labelWidth = (int)fm.getStringBounds(text, 0, text.length(), g2d).getWidth();
		g2d.setColor(highlightColor);
		g2d.drawString(text, textOffset+1, labelHeight+verticalOffset);
		//g2d.drawRect(0, 0, getWidth()-3, getHeight()-2);
		g2d.setColor(textColor);
		g2d.drawString(text, textOffset, labelHeight-1+verticalOffset);
		
	}
}
