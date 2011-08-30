package plugins.treePlugin.treeDisplay.treeStatistics;

import java.util.ArrayList;
import java.util.List;

import plugins.treePlugin.tree.Node;
import plugins.treePlugin.tree.Tree;

public class SackinsIndex extends TreeStatistic {

	
	public SackinsIndex(Tree tree) {
		super(tree);
	}

	public String getDescription() {
		return "Sackins index of tree imbalance";
	}


	public String getName() {
		return "Sackin's index";
	}


	public double getStandardDeviation() {
		double mean = getValue();
		List<Node> tips = tree.getAllTips();
		double dev = 0;
		for(Node tip : tips) {
			int depth = tree.getNodeDepth(tip);
			dev += (mean-depth)*(mean-depth);
		}
		return dev / (double)tips.size();
	}


	public double getValue() {
		List<Node> tips = tree.getAllTips();
		double sum = 0;
		for(Node tip : tips) {
			sum += tree.getNodeDepth(tip);
		}
		return sum/(double)tips.size();
	}


	public boolean hasStandardDeviation() {
		return true;
	}

}
