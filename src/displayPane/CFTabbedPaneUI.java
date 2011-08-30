package displayPane;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public class CFTabbedPaneUI extends BasicTabbedPaneUI {
	
	static Color darkLineColor = Color.red;//new Color(200, 200, 200); 
	static Color lightLineColor = new Color(240, 240, 240);
	static Color reallyLightColor = new Color(240, 240, 240);
	
	protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
		System.out.println("painting content border");
		int tabCount = tabPane.getTabCount();
		if (tabCount ==0) 
			return;
		
		int tabHeight = calculateMaxTabHeight(tabPlacement)+5;
		
		
		g.setColor(darkLineColor);
		g.drawLine(1, tabHeight-1, rects[selectedIndex].x+2, tabHeight-1);

		g.drawLine(rects[selectedIndex].x+rects[selectedIndex].width, tabHeight-1, g.getClipBounds().width-3, tabHeight-1);

		g.drawLine(1, tabHeight, 1, g.getClipBounds().height-5);
		g.drawLine(g.getClipBounds().width-3, tabHeight, g.getClipBounds().width-3, g.getClipBounds().height-5);
		
		g.setColor(Color.GREEN);
		g.drawLine(0, tabHeight-1, g.getClipBounds().width, tabHeight-1);
		
	}
	
	protected void paintTabBorder(Graphics g,
            int tabPlacement,
            int tabIndex,
            int x,
            int y,
            int w,
            int h,
            boolean isSelected) {
		System.out.println("painting tab border " + tabIndex);
		int selectedIndex = tabPane.getSelectedIndex();
		int leftEdge = 0;
		
		for(int i=0; i<rects.length; i++) {
			if (i!=selectedIndex) {
				if (i==0)
					leftEdge=1;
				
				int x1 = rects[i].x+1;
				int y1 = rects[i].y;
				int x2 = rects[i].x+rects[i].width-2;
				int y2 = rects[i].y+rects[i].height;
				
				g.setColor(lightLineColor);
				g.drawLine(x1+1, y1+1, x2+1, y1+1);
				g.setColor(darkLineColor);
				g.drawLine(x1, y1, x1, y2); //left side
				g.drawLine(x1, y1, x2, y1); //top side
				g.drawLine(x2, y1, x2, y2); //right side
				
				//g.drawRect(rects[i].x+1, rects[i].y+0, rects[i].width-2, rects[i].height+2);
			}

		}

		if (selectedIndex==0)
			leftEdge=1;
		//Draw selected one last
		int x1 = rects[selectedIndex].x;
		int y1 = rects[selectedIndex].y;
		int x2 = rects[selectedIndex].x+rects[selectedIndex].width;
		int y2 = rects[selectedIndex].y+rects[selectedIndex].height;

		g.setColor(reallyLightColor);
		g.drawLine(x1+1+leftEdge, y1+1, x2-1, y1+1); //top
		g.drawLine(x1+1+leftEdge, y1+1, x1+1+leftEdge, y2); //left
		//g.setColor(new Color(0.9f, 0.9f, 0.9f, 0.5f));
		g.drawLine(x2, y1+1, x2, y2); //right

		
		//g.drawRect(rects[selectedIndex].x+1, rects[selectedIndex].y+1, rects[selectedIndex].width, rects[selectedIndex].height);
		g.setColor(darkLineColor);
		g.drawLine(x1+1+leftEdge, y1, x2-2, y1); //top
		g.drawLine(x1+leftEdge, y1+1, x1+leftEdge, y2); //left
		g.drawLine(x2-1, y1+1, x2-1, y2); //right
		
		//Light-colored corner points
/*		g.setColor(reallyLightColor);
		g.drawLine(x1+leftEdge, y1, x1+leftEdge, y1);
		g.drawLine(x2-1, y1, x2-1, y1+1);*/
		
	}
	
	protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
		System.out.println("painting tab background " + tabIndex);
		int selectedIndex = tabPane.getSelectedIndex();
		Rectangle clip = g.getClipBounds();
		clip.height +=2;
		g.setClip(clip);
		for(int i=0; i<rects.length; i++) {
			if (i!=selectedIndex) {
				g.setColor(reallyLightColor);
				g.fillRect(rects[i].x+1, rects[i].y, rects[i].width-2, rects[i].height+2);
				for(int j=4; j<=rects[i].height+2; j++) {
					int val = (int)Math.round(250- (double)(rects[i].height-j)/1.5);
					g.setColor(new Color(val, val, val));
					g.drawLine(rects[i].x+1, j, rects[i].width+rects[i].x-1, j);
				}				
								
			}			
		}

		g.setColor(Color.white);	
		g.fillRect(rects[selectedIndex].x+1, rects[selectedIndex].y+1, rects[selectedIndex].width-2, rects[selectedIndex].height+2);
		for(int i=2; i<=(rects[selectedIndex].height+1); i++) {
			int val = (int)Math.round(254- (double)(rects[selectedIndex].height-i)/2);
			g.setColor(new Color(val, val, val));
			g.drawLine(rects[selectedIndex].x+1, i, rects[selectedIndex].width+rects[selectedIndex].x, i);
		}
		//g.fillRect(rects[selectedIndex].x+1, rects[selectedIndex].y+1, rects[selectedIndex].width-2, rects[selectedIndex].height);
		//g.setColor(new Color(255, 255, 255));
		//g.fillRect(rects[selectedIndex].x+1, rects[selectedIndex].height/2+2, rects[selectedIndex].width-2, rects[selectedIndex].height);
	}
	
}
