package plugins.treePlugin.treeDisplay.treeStatistics;

import java.util.List;

import plugins.treePlugin.tree.Node;
import plugins.treePlugin.tree.Tree;


public class TreeHeight extends TreeStatistic {

	boolean hasBranchLengths;
	
	public TreeHeight(Tree tree) {
		super(tree);
		hasBranchLengths = tree.hasBranchLengths();
	}

	public String getDescription() {
		return "Height of tree";
	}

	public String getName() {
		return "Tree height";
	}


	public double getStandardDeviation() {
		if (!hasBranchLengths)
			return Double.NaN;

		List<Node> tips = tree.getAllTips();
		double mean = 0;
		for(Node tip : tips) {
			mean += Tree.getDistToRoot(tip);
		}
		mean /= tips.size();
		
		double stdev = 0;
		for(Node tip : tips) {
			stdev += (mean-Tree.getDistToRoot(tip))*(mean-Tree.getDistToRoot(tip));
		}
		stdev /= tips.size();
		return stdev;
	}


	public double getValue() {
		if (hasBranchLengths)
			return tree.getHeight();
		else 
			return Double.NaN;
	}

	public boolean hasStandardDeviation() {
		return true;
	}

}
