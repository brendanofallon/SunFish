package guiWidgets;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class FancyPanel extends JPanel {
	
	public void paintComponent(Graphics g) {
		g.setColor( getBackground() );
		g.fillRect(0, 0, getWidth()-1, getHeight()-1);
		
		for(int i=getHeight(); i>getHeight()/2; i--) {
			int  cVal = 254+((int)Math.round(i)-getHeight())/3;
			Color c = new Color(cVal, cVal, cVal);
			g.setColor(c);
			g.drawLine(1, i, getWidth()-2, i);
		}
		
		//super.paint(g);
	}
}
