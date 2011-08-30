package display;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import displayPane.SaveCancelledException;

import topLevelGUI.SunFishFrame;


/**
 * A frame that contains Displays that have been 'popped'. Currently doesn't do much but notify
 * the Display contained when the frame is closing. 
 * @author brendan
 *
 */
public class DisplayFrame extends JFrame {
	
	final Display display;
	
	private boolean isReattaching = false;
	
	public DisplayFrame(final Display display) {
		super(display.getTitle());
		this.display = display;
		add(display);
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setPreferredSize(new Dimension(810, 400));
		setLocationRelativeTo(null);
		pack();
		
		display.setFrame(this);
		
		this.addWindowListener(new ClosingListener(this));
	}

	/**
	 * Called when this displayFrame is closing. If there are unsaved changes and we are not reattaching
	 * we prompt the user to save the file. If this save is cancelled, we throw an exception to 
	 * indicate that we don't actually want to close this frame after all. 
	 * @throws SaveCancelledException 
	 */
	protected void windowIsClosing() throws SaveCancelledException {
		if (! isReattaching) {
			display.closed();
		}
	}

	/**
	 * After calling this, when the window is closed we will not notify the display of closure
	 */
	public void setReattaching() {
		isReattaching = true;
	}
	
	class ClosingListener implements WindowListener  {
		
		DisplayFrame parent = null;
		
		public ClosingListener(DisplayFrame parent) {
			this.parent = parent;
		}
		
		public void windowClosed(WindowEvent e) {	}

		@Override
		public void windowClosing(WindowEvent e) {
			try {
				windowIsClosing();
				parent.setVisible(false);
				parent.dispose();
			} catch (SaveCancelledException e1) {
				//OK, we're not actually closing
			}
		}

		@Override
		public void windowOpened(WindowEvent e) {	}
		
		@Override
		public void windowIconified(WindowEvent e) {  }

		@Override
		public void windowDeiconified(WindowEvent e) {  }

		@Override
		public void windowActivated(WindowEvent e) { }

		@Override
		public void windowDeactivated(WindowEvent e) {	}
	}
}
