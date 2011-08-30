package undoRedo;

import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JComponent;


/**
 * This class handles maintaining cut/copy/paste focus. Basically, it tracks a focusOwner, and listens
 * for copy / copy / paste / undo / redo actions, and sends the action to the current focusOwner. Focus
 * is generally requested through the requestFocusInWindow() method. 
 * This class was mostly lifted from http://download-llnw.oracle.com/javase/tutorial/uiswing/dnd/listpaste.html
 * 
 * @author brendan
 *
 */
public class TransferActionListener implements ActionListener, PropertyChangeListener {

	private JComponent focusOwner = null;
	
	public TransferActionListener() {
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addPropertyChangeListener("permanentFocusOwner", this);
	}
	
	public JComponent getFocusOwner() {
		return focusOwner;
	}
	
	public void releaseFocus(JComponent comp) {
		if (focusOwner == comp) {
			focusOwner = null;
		}
	}
	
	public void propertyChange(PropertyChangeEvent e) {
		Object o = e.getNewValue();
		if (o instanceof JComponent) {
			focusOwner = (JComponent)o;
		} else {
			focusOwner = null;
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (focusOwner == null)
			return;

		String action = (String)e.getActionCommand();
		Action a = focusOwner.getActionMap().get(action);

		if (a != null) {
			//System.out.println("Firing action " + a + " to focus owner: " + focusOwner);
			if (focusOwner instanceof CCPListener) {
				if (a.getValue(Action.NAME).equals("cut"))
					((CCPListener)focusOwner).cut();
				if (a.getValue(Action.NAME).equals("copy"))
					((CCPListener)focusOwner).copy();
				if (a.getValue(Action.NAME).equals("paste"))
					((CCPListener)focusOwner).paste();
				
			}
			else { 
				a.actionPerformed(new ActionEvent(focusOwner,
						ActionEvent.ACTION_PERFORMED,
						null));
			}
		}
	}
}
