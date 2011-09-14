package topLevelGUI;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class DefaultDisplayPaneBackground extends JPanel {

	int topPixels = 50;
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		
		GradientPaint gp = new GradientPaint(1f, 1f, new Color(0.9f, 0.9f, 0.9f), 1f, getHeight(), Color.white);
		g2d.setPaint(gp);
		g2d.fillRect(0, 0, getWidth(), getHeight());
	}
	
}
