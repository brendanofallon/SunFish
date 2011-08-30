package plugins.treePlugin.treeDisplay.treeStatistics;

import plugins.treePlugin.tree.Node;
import plugins.treePlugin.tree.Tree;

public class NumberOfTips extends TreeStatistic {

	
	public NumberOfTips(Tree tree) {
		super(tree);
	}

	public String getDescription() {
		return "Number of leaves (tips) in tree";
	}

	public String getName() {
		return "Number of tips";
	}

	public double getStandardDeviation() {
		return 0;
	}


	public double getValue() {
		return tree.getNumLeaves();
	}

	public boolean hasStandardDeviation() {
		return false;
	}

}
