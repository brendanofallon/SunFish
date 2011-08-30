package topLevelGUI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;


public class SunFishBackgroundPanel extends JPanel {


	int topPixels = 50;
	int bottomPixels = 100;
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		
		g2d.setColor(Color.white);
		g2d.fillRect(0,0,getWidth(), getHeight());
		
		for(int i=0; i<topPixels; i++) {
			float val = 0.9f+0.1f*(float)i/(float)topPixels;
			g2d.setColor(new Color(val, val, val));
			g2d.drawLine(0, i, getWidth(), i);
		}
		
		
		for(int i=0; i<bottomPixels; i++) {
			float val = 1.0f-0.1f*(float)(i)/(float)bottomPixels;
			g2d.setColor(new Color(val, val, val));
			g2d.drawLine(0, i+(getHeight()-bottomPixels), getWidth(), i+(getHeight()-bottomPixels));
		}
		
	}
}
