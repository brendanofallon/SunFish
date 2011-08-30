package guiWidgets;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 *  A convienience class for creating buttons with a consistent look, feel and behavior 
 * @author brendan
 *
 */
public class CFButton extends JButton {

	public CFButton(String text) {
		super(text);
		this.addBellsAndWhistles();
	}
	
	public CFButton(String text, ImageIcon icon) {
		super(text);
		this.setIcon(icon);
		this.addBellsAndWhistles();
	}
	
	public CFButton(ImageIcon icon) {
		super(icon);
		this.addBellsAndWhistles();
	}
	
	public CFButton(ImageIcon icon, String toolTipText) {
		super(icon);
		this.setToolTipText(toolTipText);
		this.setBorderPainted(false);
		this.addBellsAndWhistles();
	}
	
	private void addBellsAndWhistles() {
		setFocusable(false);
		this.setBorderPainted(false);
		MouseListener rollHandler = new RollOverHandler(this); 
		this.addMouseListener(rollHandler);
	}
	
	private class RollOverHandler implements MouseListener {
		
		private JButton buttonParent;
		
		public RollOverHandler(JButton button) {
			super();
			buttonParent = button;
		}

		public void mouseEntered(MouseEvent e) {
			buttonParent.setBorderPainted(true);
			buttonParent.repaint();
		}

		public void mouseExited(MouseEvent e) {
			buttonParent.setBorderPainted(false);
			buttonParent.repaint();						
		}

		public void mouseClicked(MouseEvent e) {}
		
		public void mousePressed(MouseEvent e) {}
		
		public void mouseReleased(MouseEvent e) {
			buttonParent.setBorderPainted(false);
			buttonParent.repaint();			
		}
		
	}
}
