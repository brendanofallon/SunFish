package plugins.treePlugin.treeDisplay;

import java.util.ArrayList;
import java.util.List;

import plugins.treePlugin.tree.DrawableTree;

import undoRedo.UndoableAction;
import undoRedo.UndoableActionSource;

/**
 * A generic class to represent undoable events that happen to trees
 * @author brendan
 *
 */
public class TreeUndoableAction extends UndoableAction {

	List<DrawableTree> treesBefore;
	String description;
	
	public TreeUndoableAction(UndoableActionSource source, DrawableTree treeBefore, String description) {
		super(source);
		treesBefore = new ArrayList<DrawableTree>(2);
		treesBefore.add(treeBefore);
		this.description = description;
	}
	
	public TreeUndoableAction(UndoableActionSource source, List<DrawableTree> treesBefore, String description) {
		super(source);
		this.treesBefore = treesBefore;
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}

}
