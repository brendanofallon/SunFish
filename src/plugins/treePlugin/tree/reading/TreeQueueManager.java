package plugins.treePlugin.tree.reading;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import plugins.treePlugin.tree.DrawableTree;

/**
 * A wrapper for a TreeReader that allows access to a queue of trees of variable size.
 * THIS MAY BE BROKEN! It appears to not work as expected sometimes, but its unknown
 * exactly what the issue is or what causes the bug. 
 * 
 * @author brendan
 *
 */
public class TreeQueueManager {

	TreeReader reader;
	Deque<DrawableTree> queue;
	int leftIndex = -1;	//Index of tree on left edge of queue
	int rightIndex = -1; //Index of tree on right edge
	int currentIndex = -1; //index of last tree read in 
	
	public TreeQueueManager(TreeReader reader) {
		this.reader = reader;
		queue = new ArrayDeque<DrawableTree>();
	}
	
	public int size() {
		return queue.size();
	}
	
	/**
	 * Extends the queue by howmany trees in the right direction. 
	 * @param howmany
	 * @return the number of trees actually added
	 */
	public int extendRight(int howmany) {
		DrawableTree tree;
		while(currentIndex<rightIndex) {
			tree = reader.getNextTree();
			currentIndex++;
		}
		if (leftIndex == -1)
			leftIndex = 0;
		
		int treesRead = 0;
		while(treesRead<howmany) {
			tree = reader.getNextTree();
			if (tree==null)
				break;
			else {
				currentIndex++;
				treesRead++;
				queue.addLast(tree);
				rightIndex++;
			}
		}

		//System.out.println("Extending right by " + howmany + " trees. Trees read: " + treesRead + " current index : " + currentIndex + " left index: " + leftIndex + " right index: " + rightIndex);
		return treesRead;
	}
	
	/**
	 * Extends the queue by howmany trees in the left direction
	 * @param howmany
	 */
	public int extendLeft(int howmany) {
		DrawableTree tree;
		while(currentIndex>leftIndex) {
			tree = reader.getPreviousTree();
			currentIndex--;
		}
		if (rightIndex == -1)
			rightIndex = 0;
		
		int treesRead = 0;
		while(treesRead<howmany) {
			tree = reader.getPreviousTree();
			if (tree==null) {
				break;
			}
			else {
				currentIndex--;
				treesRead++;
				queue.addFirst(tree);
				leftIndex--;
			}
		}

		if (currentIndex < 0)
			currentIndex = 0;
		//System.out.println("Extending left by " + howmany + " trees. Trees read: " + treesRead + " current index : " + currentIndex + " left index: " + leftIndex + " right index: " + rightIndex);
		return treesRead;
	}
	
	/**
	 * Shrinks the queue by howmany trees from the left
	 * @param howmany
	 */
	public void shrinkLeft(int howmany) {
		if (howmany>queue.size())
			queue.clear();
		else {
			int i = 0;
			while(i<howmany) {
				queue.removeFirst();
				leftIndex++;
				i++;
			}
		}
	}
	
	/**
	 * Shrinks the queue by howmany trees from the right
	 * @param howmany
	 */
	public void shrinkRight(int howmany) {
		if (howmany>queue.size())
			queue.clear();
		else {
			int i = 0;
			while(i<howmany) {
				queue.removeLast();
				rightIndex--;
				i++;
			}
		}
	}
	
	/**
	 * Shifts the entire queue by howmany trees to the right, preserves size of queue
	 * @param howmany
	 */
	public void shiftRight(int howmany) {
		int shifted = extendRight(howmany);
		shrinkLeft(shifted);
	}

	/**
	 * Shifts entire queue howmany positions to the left
	 * @param howmany
	 */
	public void shiftLeft(int howmany) {
		int shifted = extendLeft(howmany);
		shrinkRight(shifted);
	}
	
	public Iterator<DrawableTree> iterator() {
		return queue.iterator();
	}
	
	/**
	 * Retrieves the whichth tree from the left side of the queue 
	 * @param which
	 */
	public DrawableTree get(int which) {
		if (which < 0 || which>=queue.size()) {
			return null;
		}
		
		int count = 0;
		Iterator<DrawableTree> it = queue.iterator();
		DrawableTree tree = it.next();
		while(count<which && it.hasNext()) {
			tree = it.next();
			count++;
		}
		
		return tree;
	}
}
