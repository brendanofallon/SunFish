package guiWidgets;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class IconButton extends JPanel {

	protected ImageIcon image;
	protected boolean mouseIsOver = false;
	protected boolean mousePressed = false;
	
	List<ActionListener> actionListeners;
	
	public IconButton(ImageIcon image, String tooltipText) {
		this.image = image;
		this.setToolTipText(tooltipText);
		
		this.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				for(ActionListener al : actionListeners)
					al.actionPerformed(new ActionEvent(this, 0, ""));
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				mouseIsOver = true;
				paintImmediately(0, 0, getWidth(), getHeight());
			}

			@Override
			public void mouseExited(MouseEvent e) {
				mouseIsOver = false;
				paintImmediately(0, 0, getWidth(), getHeight());
			}

			@Override
			public void mousePressed(MouseEvent e) {
				mousePressed = true;
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				mousePressed = false;
				repaint();
			}
		});
	}
	
	public void setIcon(ImageIcon icon) {
		image = icon;
	}
	
	public void addActionListener(ActionListener al) {
		if (actionListeners==null) {
			actionListeners = new ArrayList<ActionListener>();
		}
		actionListeners.add(al);
	}
	
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(getBackground());
		g2d.fillRect(0, 0, getWidth(), getHeight());
		
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (mouseIsOver) {
			g2d.setColor(new Color(250, 250, 250,250));
			g2d.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
			
			float min = getHeight()/2.0f;
			for(int i=0; i<min; i++) {
				float val = 0.4f*(1.0f-(float)i/min);
				g2d.setColor(new Color(0.5f, 0.5f, 0.5f, val));
				int dif = Math.min(3, i);
				g2d.drawLine(4-dif, getHeight()-i, getWidth()-2*(4-dif), getHeight()-i);
			}
			
			g2d.setColor(new Color(150, 150, 150));
			g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
		}
		
		if (mousePressed) {
			g2d.setColor(new Color(200, 200, 200, 150));
			g2d.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
			
			g2d.setColor(Color.gray);
			g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
		}
		
		image.paintIcon(this, g2d, getWidth()/2-image.getIconWidth()/2, getHeight()/2 - image.getIconHeight()/2);
		
	}
	
}
