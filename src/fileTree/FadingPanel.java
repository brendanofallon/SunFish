package fileTree;

import java.awt.Color;
import java.awt.Graphics;



public class FadingPanel extends javax.swing.JPanel {

	Color[] colArray;
	
	public FadingPanel() {
		int arrLength = 6;
		
		colArray = new Color[arrLength];
		
		setBackground(Color.white);
		
		
		
		for(int i=0; i<arrLength; i++) {
			int val = (int)Math.floor(240.0*(1.0-(double)i/arrLength));
			System.out.println(i + "\t" + val);
			colArray[i] = new Color(240, 240, 240, val);
		}
	}
	
	public void paintComponent(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		
		for(int i=0; i<colArray.length; i++) {
			g.setColor(colArray[i]);
			g.drawLine(0, i, getWidth(), i);
		}
		
		for(int i=0; i<colArray.length; i++) {
			g.setColor(colArray[i]);
			g.drawLine(0, getHeight()-i, getWidth(), getHeight()-i);
		}
	}
}
