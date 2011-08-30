package plugins.treePlugin.treeDisplay.treeStatistics;

import plugins.treePlugin.tree.Tree;


public abstract class TreeStatistic {

	Tree tree;
	
	public TreeStatistic(Tree tree) {
		this.tree = tree;
	}
	
	public abstract String getName();
	
	public abstract String getDescription();
	
	public abstract double getValue();
	
	public boolean hasStandardDeviation() {
		return false;
	}
	
	public double getStandardDeviation() {
		return 0;
	}
	
	public boolean hasDoubleValue() {
		return true;
	}
	
	public boolean hasBooleanValue() {
		return false;
	}
	
	public boolean getBooleanValue() {
		return false;
	}
	
	
}
