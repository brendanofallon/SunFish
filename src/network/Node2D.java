package network;

import java.util.List;

/**
 * An interface that describes network nodes that can be arranged in space. These have a position and
 * a size. 
 * @author brendan
 *
 */
public interface Node2D extends NetworkNode {

	public List<Node2D> get2DNeighbors();
	
	public double getX();
	
	public double getY();
	
	public void setX(double newX);
	
	public void setY(double newY);
	
	public double getWidth();
	
	public double getHeight();
	
	
}
