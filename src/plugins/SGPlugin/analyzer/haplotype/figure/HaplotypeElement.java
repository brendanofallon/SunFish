package plugins.SGPlugin.analyzer.haplotype.figure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import plugins.SGPlugin.analyzer.haplotype.Haplotype;

import network.NetworkEdge;
import network.Node2D;


import figure.Figure;
import figure.FigureElement;

/**
 * A visual representation of a haplotype. Not a lot of functionality here - in order to maintain
 * branch lengths proportional to distances we have to keep everything square (ie xFactor=yFactor),
 * and make sure no elements overlap, etc, and so most everything is handled in the parent
 * HaplotypeNetworkFigure
 * 
 * @author brendan
 *
 */
public class HaplotypeElement extends FigureElement implements Node2D {

	static int hapCount = 0;
	int myNumber;
	
	Haplotype hap;
	Color fillColor = new Color(5, 50, 250);
	static Stroke normalStroke = new BasicStroke(1.25f);
	static Stroke selectedStroke = new BasicStroke(5.0f);
	
	final static Color highlightColor = new Color(10, 225, 225);
	
	double baseRadius; // stores initially set radius value



	List<NetworkEdge> edges = new ArrayList<NetworkEdge>();
	List<Node2D> neighbors = new ArrayList<Node2D>();
	
	
	//A few debugging items...
	public String label = "";
//	
//	public HaplotypeElement(String label) {
//		super(null);
//		this.label = label;
//	}
	//
//	public String toString() {
//		return label;
//	}
	
	public HaplotypeElement(HaplotypeNetworkFigure parent) {
		super(parent);
		hap = null;
		bounds.width = 0.0;
		bounds.height = 0.0;
		myNumber = hapCount;
		hapCount++;
	}


	public HaplotypeElement(Figure parent, Haplotype hap) {
		super(parent);
		this.hap = hap;
		bounds.width = 0.0;
		bounds.height = 0.0;
		myNumber = hapCount;
		hapCount++;
		//System.out.println("Setting height to : " + bounds.height + " in constructor");
	}
	
	public int getNumber() {
		return myNumber;
	}
	
	public boolean contains(double x, double y) {
		double bx = getCenterX();
		double by = getCenterY();
		double min = Math.min(parent.getWidth(), parent.getHeight());
		x*=parent.getWidth()/min;
		y*=parent.getHeight()/min;
		
		double dist = Math.sqrt( (x-bx)*(x-bx)+(y-by)*(y-by) );
		//System.out.println("Center: " + bx + ", " + by + " click: " + x + ", " + y + " dist: " + dist + " radius: " + getRadius());
		return  dist<=getRadius();
	}
	
	public void setHaplotype(Haplotype hap) {
		this.hap = hap;
		bounds.width = 0.0; //These must start at 0,0 for the drawing algorithm to work
		bounds.height = 0.0;
	}
	
	
	public Haplotype getHap() {
		return hap;
	}
	
	public Color getFillColor() {
		return fillColor;
	}
	
	public void setFillColor(Color c) {
		this.fillColor = c;
	}
	
	public void setLabel(String lab) {
		this.label = lab;
	}
	
	/**
	 * Show the configuration tool. Overrides FigureElement.popupConfigureTool
	 */
	public void popupConfigureTool(java.awt.Point pos) {
		((HaplotypeNetworkFigure)parent).showConfigTool();
	}
	
	/**
	 * Moves this element so that it's center lies at point p
	 * @param p
	 */
	public void setCenterPosition(double x, double y) {
		bounds.x = x-bounds.width/2.0;
		bounds.y = y-bounds.height/2.0;
	}

	public double getBaseRadius() {
		return baseRadius;
	}


	public void setBaseRadius(double baseRadius) {
		this.baseRadius = baseRadius;
	}
	
	/**
	 * Establishes the scale of the element (by telling it how big the containing figure is), this
	 * is guaranteed to get called before paint
	 * 
	 * @param xFactor Width of the figure in pixels
	 * @param yFactor Height of the figure in pixels
	 */
	public void setScale(double xFactor, double yFactor, Graphics g) {
		double min = Math.min(xFactor, yFactor);
		this.xFactor = min;
		this.yFactor = min;
		//bounds.height = bounds.width*xFactor/yFactor;
	}
	
	/**
	 * Overrides figureElement.move so we can account for the fact that xFactor != getWidth and
	 * yFactor != getHeight
	 */
	public void move(double dx, double dy) {
		double width = parent.getWidth();
		double height = parent.getHeight();
		double min = Math.min(width, height);
		
		bounds.x += dx*width/min;
		bounds.y += dy*height/min;
		if( bounds.x < 0)
			bounds.x = 0;
		if (bounds.y < 0)
			bounds.y = 0;
	}
	
	public void paint(Graphics2D g) {
		Stroke origStroke = g.getStroke();
	
		if (isSelected()) { 
			g.setStroke(selectedStroke);
			g.setColor(highlightColor);
			g.drawOval(round(bounds.x*xFactor), round(bounds.y*yFactor), round(bounds.width*xFactor), round(bounds.height*yFactor));
		}
		
		g.setColor(fillColor);
		g.fillOval(round(bounds.x*xFactor), round(bounds.y*yFactor), round(bounds.width*xFactor), round(bounds.height*yFactor));	
		
		g.setStroke(normalStroke);
		g.setColor(foregroundColor);
		g.drawOval(round(bounds.x*xFactor), round(bounds.y*yFactor), round(bounds.width*xFactor), round(bounds.height*yFactor));

		g.setColor(Color.CYAN);
		g.setFont(new Font("Sans", Font.BOLD, 12));
		g.drawString(label, round((bounds.x+bounds.width/2.0)*xFactor), round(bounds.y*yFactor)+10);
		
		g.setStroke(origStroke);
	}
	
	public double getRadius() {
		return bounds.width/2.0;
	}

	public void setRadius(double radius) {
		bounds.width = 2.0*radius;
		bounds.height = bounds.width;
	}


	@Override
	public List<NetworkEdge> getEdges() {
		return edges;
	}
	
	public List<NetworkEdge> getHapEdges() {
		return hap.getEdges();
	}


	@Override
	public void setX(double newX) {
		setCenterPosition(newX, getCenterY());
	}


	@Override
	public void setY(double newY) {
		setCenterPosition(getCenterX(), newY);
	}


	@Override
	public void addEdge(NetworkEdge e) {
		if (e.getNodeA()!=null && !(e.getNodeA() instanceof Node2D)) {
			throw new IllegalArgumentException("Node2D's cannot be joined to non-node2D's");
		}
		if (e.getNodeB()!=null && !(e.getNodeB() instanceof Node2D)) {
			throw new IllegalArgumentException("Node2D's cannot be joined to non-node2D's");
		}
		HapEdgeElement edge = (HapEdgeElement)e;
		edges.add(edge);
		if (edge.getNodeA() != this)
			neighbors.add((Node2D)edge.getNodeA());
		else
			neighbors.add((Node2D)edge.getNodeB());
	}


	@Override
	/**
	 * Returns a list of all nodes that are adjacent to (joined by one edge to) this node
	 */
	public List<Node2D> get2DNeighbors() {
		return neighbors;
	}
	
	
	
	/**
	 * Multiplies figure width by the supplied factor. Does not affect position. 
	 * 
	 * @param factor
	 */
//	public void setScalingFactor(double factor) {
//		//We hold the minimum value at a certain level so nodes can't disappear completely
//		double newVal = Math.max(0.01, getRadius()*factor);
//		
//		setRadius(newVal);
//		System.out.println("Setting radius to " + getRadius());
//	}
	
//	public double getScalingFactor() {
//		return scaleFactor;
//	}

}
