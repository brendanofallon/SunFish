package undoRedo;

import java.util.ArrayList;
import java.util.List;

import topLevelGUI.SunFishFrame;


import errorHandling.ErrorWindow;

/**
 * Handles undo / redo events for all classes. In general any component that wants to manage
 *  undo / redo events gets a separate one of these - it's probably a can of worms if we want
 *  to have one big global one.  
 * 
 * Works by maintaining a list of actions and a pointer to the current position in the list. Prior 
 * to any undo / redos, actions are posted to the list via calls to postNewAction, which adds items
 * to the end. When an undo happens, the nextIndex pointer (actually, an int), is decremented by one
 * and subsequent redo actions increase it again. Hence, undos can only happen if nextIndex>0, and
 * redos can only happen if nextIndex <= actions.size() 
 * 
 * 
 * We also implement a simple, static, focus-like mechanism for deciding which component (actually,
 * which UndoableActionSource) receives the 'Undo' command. The static field undoFocusOwner
 * refers to the most recent component to call requestUndoFocus,  Various components can request focus
 * (which is always granted) and release focus (which sets the field to null). These calls also
 * cause calls back to the SunFishFrame so that the Undo and Redo menu items can be updated appropriately
 * @author brendan
 *
 */
public class UndoRedoManager {
	
	//The total number of actions to remember. This is the 'undo' level - we can't undo more
	//actions than this
	int actionsToRemember = 10;
	
	//The list of actions we are tracking. 0 indicates the oldest action
	List<UndoableAction> actions = new ArrayList<UndoableAction>();
	
	//The index of the next event to be posted. This is equal to actions.size() if we're
	//posting events to the end of the list (for instance, if no undos have occurred), but may
	// be less than actions.size if we've had some undos. In this case, redos are possible
	private int nextIndex = 0; 
	
	//The component to whom undo actions are directed. 
	static UndoableActionSource undoFocusOwner  = null;
	
	
	UndoableAction finalState = null;
	
	public UndoRedoManager() {

	}
	
	/**
	 * Get the current component to whom undo actions are directed. This may be null if
	 * no components have requested it. 
	 * @return
	 */
	public static UndoableActionSource getUndoFocusOwner() {
		return undoFocusOwner;
	}
	
	/**
	 * Request the focus for undo/redo commands. Request is always granted until another component
	 * nabs it. 
	 * @param source
	 */
	public static void requestUndoFocus(UndoableActionSource source) {
		//System.out.println("Component " + source + " has undo focus");
		undoFocusOwner = source;
		if (source!=null) {
			source.getManager().setMenuItemState();
		}
	}
	
	/**
	 * Un-register listening for undo commands - this only releases undo focus if the currently
	 * focussed component is the component supplied in the argument. The idea is that whenever a 
	 * component (say, a display) is closed, it can no longer listen for undo events. In this case, we
	 * should set the undoFocusOwner to null. However, it may be the case that something else has already 
	 * grabbed undo focus, in this case, we shouldn't set the focus owner to null. By checking to make sure
	 * that the source is the current owner and only releasing if it is, then components can call
	 * this method whenever they're closed and not worry who else may have grabbed the focus. 
	 *  
	 * @param source
	 */
	public static void releaseUndoFocus(UndoableActionSource source) {
		//System.out.println("Component " + source + " is releasing undo focus");
		if (undoFocusOwner==source) {
			undoFocusOwner = null;
			SunFishFrame.getSunFishFrame().setUndoState(false, "");
			SunFishFrame.getSunFishFrame().setRedoState(false, "");
		}
	}
	
	/**
	 * Post a new undoable action to the list. If nextIndex is at the end of the list, we just
	 * add the new action to the end. If nextIndex is somewhere in the middle (because we've undone
	 * some previous actions) all subsequent items in the list are removed (items with indices nextIndex..
	 * actions.size()-1). In either case the new action has index nextIndex, and nextIndex is increased
	 * by one. 
	 * @param action
	 */
	public void postNewAction(UndoableAction action) {
		while(actions.size()>nextIndex)
			actions.remove(nextIndex);
		 
		if (actions.size()==actionsToRemember) {
			actions.remove(0);
			nextIndex--;
		}
		
		actions.add(action);
		nextIndex++;
		
		setMenuItemState();
		//System.out.println("Posting new action, action list is now: ");
		//emitActionList();
	}
	
	/**
	 * A debugging function that writes the current state of the list to sys.out
	 */
	public void emitActionList() {
		
		System.out.println("Action list, current size: " + actions.size());
		for(int i=0; i<actions.size(); i++) {
			if (i==nextIndex)
				System.out.print("*");
			System.out.println(i + "\t" + actions.get(i).getDescription() + "\t" + actions.get(i).getSource());
		}
	}
	
	/**
	 * This should be /is called whenever the new items are posted or undo / redo actions are performed;
	 * it sets the state of the menu items in the Edit menu
	 */
	protected void setMenuItemState() {
		if (actions.size()==0) {
			SunFishFrame.getSunFishFrame().setUndoState(false, "");
			SunFishFrame.getSunFishFrame().setRedoState(false, "");
			return;
		}
		
		if (nextIndex>0) {
			SunFishFrame.getSunFishFrame().setUndoState(true, actions.get(nextIndex-1).getDescription());
		}
		else {
			SunFishFrame.getSunFishFrame().setUndoState(false, "");
		}
		
		if (nextIndex<actions.size()) {
			SunFishFrame.getSunFishFrame().setRedoState(true, actions.get(nextIndex).getDescription());
		}
		else {
			SunFishFrame.getSunFishFrame().setRedoState(false, "");
		}
	}
	
	public void undo() {
		if (! canUndo()) {
			throw new IllegalStateException("Cannot undo action.. no more actions to undo.");
		}
		
		nextIndex--;
		UndoableAction action = actions.get(nextIndex);
		UndoableActionSource source = action.getSource();
		try {
			UndoableAction state = source.undoAction(action);
			if (nextIndex==(actions.size()-1)) {
				finalState = state;
			}
		} catch (UndoException e) {
			ErrorWindow.showErrorWindow(e);
		}
		
		setMenuItemState();
		//System.out.println("Undoing action, action list is now: ");
		//emitActionList();
	}
	
	public void redo() {
		if (! canRedo()) {
			throw new IllegalStateException("Cannot redo action, no actions to redo");
		}
		
		nextIndex++;
		UndoableAction action;
		if (nextIndex < actions.size())
			action = actions.get(nextIndex);
		else
			action = finalState;
		
		UndoableActionSource source = action.getSource();
		try {
			source.redoAction(action);
		} catch (RedoException e) {
			ErrorWindow.showErrorWindow(e);
		}
		
		setMenuItemState();
		//System.out.println("Redoing action " + action.getDescription() + " action list is now: ");
		//emitActionList();
	}
	
	public boolean canUndo() {
		return nextIndex > 0;
	}
	
	public boolean canRedo() {
		return nextIndex <= actions.size();
	}
}
