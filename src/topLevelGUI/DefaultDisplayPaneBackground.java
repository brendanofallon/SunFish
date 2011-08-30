package topLevelGUI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class DefaultDisplayPaneBackground extends JPanel {

	int topPixels = 50;
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		
		g2d.setColor(Color.white);
		g2d.fillRect(0,0,getWidth(), getHeight());
		
		for(int i=0; i<topPixels; i++) {
			float valR = 0.8f+0.2f*(float)i/(float)topPixels;
			float valG = 0.9f+0.1f*(float)i/(float)topPixels;
			float valB = 0.96f+0.04f*(float)i/(float)topPixels;
			g2d.setColor(new Color(valR, valG, valB));
			g2d.drawLine(0, i, getWidth(), i);
		}
	}
	
}
