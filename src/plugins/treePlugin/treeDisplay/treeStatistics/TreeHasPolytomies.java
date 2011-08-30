package plugins.treePlugin.treeDisplay.treeStatistics;
import plugins.treePlugin.tree.Node;
import plugins.treePlugin.tree.Tree;

public class TreeHasPolytomies extends TreeStatistic {

	public TreeHasPolytomies(Tree tree) {
		super(tree);
	}


	public String getDescription() {
		return "Tree has nodes with more than two offspring";
	}


	public String getName() {
		return "Has polytomies";
	}


	public double getValue() {
		return 0;
	}


	public boolean getBooleanValue() {
		return ! tree.isBinary();
	}
	
	public boolean hasBooleanValue() {
		return true;
	}

}
