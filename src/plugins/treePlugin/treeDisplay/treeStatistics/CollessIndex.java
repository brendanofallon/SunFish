package plugins.treePlugin.treeDisplay.treeStatistics;

import java.util.ArrayList;
import plugins.treePlugin.tree.Node;
import plugins.treePlugin.tree.Tree;


public class CollessIndex extends TreeStatistic {


	public CollessIndex(Tree tree) {
		super(tree);
	}


	public String getDescription() {
		return "Colless' index of tree imbalance";
	}


	public String getName() {
		return "Colless index";
	}



	public double getValue() {
		ArrayList<Node> nodes = tree.getInternalNodes();
		int sum = 0;
		int count = 0;
		for(Node n : nodes) {
			if (n.numOffspring()==2) {
				sum += Math.abs( Tree.getNumTips(n.getOffspring(0)) - Tree.getNumTips(n.getOffspring(1)) );
				count++;
			}
		}
		return (double)sum / (double)count;
	}



}
