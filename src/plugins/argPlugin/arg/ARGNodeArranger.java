package plugins.argPlugin.arg;

import java.util.List;

import network.Network2D;
import network.NetworkNode;
import network.Node2D;
import network.NodeArranger;

/**
 * A class that positions ARG nodes by using a focal site to identify a marginal tree, then laying out nodes along that marginal tree
 * as if the marginal tree was a right-facing SquareTree
 * @author brendan
 *
 */
public class ARGNodeArranger implements NodeArranger {

	int focalSite = 0;
	
	public void setFocalSite(int site) {
		this.focalSite = site;
	}
	
	@Override
	public void arrangeNodes(Network2D network) {
		if (! (network instanceof ARG)) {
			throw new IllegalArgumentException("Cannot arrange non-args with the ARG node arranger");
		}
		ARG arg = (ARG)network;
		

		//clearNodePositions(arg);
		calculateNodePositionsRight(arg);
		//arrangeMissedNodes(arg);
		
		//Shift and rescale so everything fits in the unit square
		arg.shiftToOrigin();
		double maxX = arg.getMaxX();
		double maxY = arg.getMaxY();
		arg.scaleX(1.0/maxX);
		arg.scaleY(1.0/maxY);
	}
	
	/**
	 * Calculate node positions just traverses the focal tree and therefore misses some nodes. These missed nodes are 
	 * identifiable because their positions will be NaN. Here we scan for such nodes and try to set a sensible position
	 * for them
	 * @param arg
	 */
	private void arrangeMissedNodes(ARG arg) {
		List<NetworkNode> nodes = arg.getAllNodes();
		Double maxHeight = arg.getMaxHeight();
		for(NetworkNode node : nodes) {
			ARGNode aNode = (ARGNode)node;
			aNode.setX( 1.0-aNode.getNodeHeight()/maxHeight);
			
			ARGNode parent = findSensibleParent(aNode); //Identify a parent with a known Y
			if (parent != null)
				aNode.setY( parent.getY());
		}
		
	}

	
	
	private ARGNode findSensibleParent(ARGNode aNode) {
		ARGNode ref = aNode.getParentForSite(focalSite);
		while(ref != null && Double.isNaN(ref.getY())) {
			ref = ref.getParentForSite(focalSite);
		}
		return ref;
	}

	private void clearNodePositions(ARG arg) {
		List<NetworkNode> nodes = arg.getAllNodes();
		for(NetworkNode node : nodes) {
			Node2D n2d = (Node2D)node;
//			n2d.setX(Double.NaN);
//			n2d.setY(Double.NaN);
			n2d.setX(0);
			n2d.setY(0);
		}
		
	}

	private void calculateNodePositionsRight(ARG arg) {
		ARGNode root = arg.getRootForSite(focalSite);
		root.setX(0);
		root.setY(0.5);

		//System.out.println("Laying out nodes with focal site " + focalSite);
		
		int totTips = arg.getTips().size();
		
		double maxTreeHeight = arg.getMaxHeight();
		
		double yDist = 0.0; //What should this be?
		for(ARGNode kid : root.getOffspring()) {
			if (kid.getParentForSite(focalSite)==root) {
				int kidTips = ARG.getNumTipsForSite(kid, focalSite);

				double frac = (double)kidTips/(double)totTips;
				calculateNodePositionsRight(kid, yDist+frac/2.0, maxTreeHeight, kidTips, totTips);
				yDist += frac;
			}
		}


	}
	
	private void calculateNodePositionsRight(ARGNode n, double yDist, double maxTreeHeight, int myTips, int totTips) { 
		double myFrac = (double)myTips/(double)totTips;
		
		//n.setX( ((ARGNode)n.getParentForSite(focalSite)).getX() + distToParent(n, focalSite)/maxTreeHeight );
		n.setX( 1.0-n.getNodeHeight()/maxTreeHeight);
		n.setY(yDist);

		double dist = -myFrac/2.0;;
		for(ARGNode kid : n.getOffspring()) {
			if (kid.getParentForSite(focalSite)==n) {
				int kidTips = ARG.getNumTipsForSite(kid, focalSite);

				double frac = (double)kidTips/(double)totTips;

				calculateNodePositionsRight(kid, yDist+dist+frac/2.0, maxTreeHeight, kidTips, totTips);
				dist+=frac;	
			}
		}
 
			
		
		  
	}
	
	private double distToParent(ARGNode node, int site) {
		return (node.getParentForSite(site).getNodeHeight() - node.getNodeHeight());
	}
}
