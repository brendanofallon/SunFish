package plugins.treePlugin.treeDisplay.treeStatistics;

import plugins.treePlugin.tree.Node;
import plugins.treePlugin.tree.Tree;

public class NodeTotal extends TreeStatistic {

	public NodeTotal(Tree tree) {
		super(tree);
	}


	public String getDescription() {
		return "Total number of nodes";
	}


	public String getName() {
		return "Node total";
	}


	public double getStandardDeviation() {
		return 0;
	}


	public double getValue() {
		return tree.getAllNodes().size();
	}


	public boolean hasStandardDeviation() {
		return false;
	}

}
