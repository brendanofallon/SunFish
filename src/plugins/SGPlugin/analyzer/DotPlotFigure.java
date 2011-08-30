package plugins.SGPlugin.analyzer;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import element.sequence.Sequence;
import figure.Figure;
import figure.TextElement;
import figure.VerticalTextElement;

public class DotPlotFigure extends Figure implements ActionListener {

	DotPlotElement dotPlot;
	
	TextElement xLabel;
	VerticalTextElement yLabel;
	
	Font labelFont = new Font("Sans", Font.PLAIN, 11);
	
	//Some variables related to the zoom animation effect
	javax.swing.Timer timer;
	boolean isZooming = false;
	int oldBlockWidth;
	int zoomSteps = 15;
	int zoomStep = 0;
	double xMinDiff = 0;
	double xMaxDiff = 0;
	double yMinDiff = 0;
	double yMaxDiff = 0;
	
	public DotPlotFigure() {
		dotPlot = new DotPlotElement(this);
		dotPlot.setMobile(false);
		dotPlot.setCanConfigure(false);
		dotPlot.setBounds(0.1, 0.05, 0.8, 0.8);
		addElement(dotPlot);
		xLabel = new TextElement("x axis", this);
		yLabel = new VerticalTextElement("x axis", this);
		xLabel.setFont(labelFont);
		yLabel.setFont(labelFont);
		xLabel.setBounds(0.5, 0.9, 0.1, 0.1);
		yLabel.setBounds(0.02, 0.4, 0.1, 0.1);
		xLabel.setMobile(true);
		yLabel.setMobile(true);
		addElement(xLabel);
		addElement(yLabel);
		
		int delay = 60; //milliseconds
		timer = new javax.swing.Timer(delay, this);
		
		setRectangleSelection(true);
		setPreserveSelectionRect(true);
	}
	
	public void setSequences(Sequence seq1, Sequence seq2) {
		dotPlot.setSequences(seq1, seq2);
		xLabel.setText(seq1.getName());
		yLabel.setText(seq2.getName());
		repaint();
	}
	
	
	public void selectionRectUpdated(Rectangle selectRect2) {
		dotPlot.yMarkerMin = Math.min(selectRect2.y, selectRect2.y+selectRect2.height);
		dotPlot.yMarkerMax = Math.max(selectRect2.y, selectRect2.y+selectRect2.height);
		
		dotPlot.xMarkerMin = Math.min(selectRect2.x, selectRect2.x+selectRect2.width);
		dotPlot.xMarkerMax = Math.max(selectRect2.x, selectRect2.x+selectRect2.width);
	}
	
	public int getBlockWidth() {
		return dotPlot.getBlockWidth();
	}

	public void setBlockWidth(int blockWidth) {
		dotPlot.setBlockWidth(blockWidth);
	}

	/**
	 * Set the range to include all of both sequences
	 */
	public void restoreDefaultBounds() {
		dotPlot.setSequenceBounds(0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
		dotPlot.unsetZoom();
		repaint();
	}

	public void zoomToSelection() {
		if (isZooming)
			return;
		
		//Will need to be changed at some point...
		int xFactor = this.getWidth();
		int yFactor = this.getHeight();
		
		if ( hasSelectionRect() ) {
			isZooming = true;
			zoomStep = 0;
				
			int xMinStart = (int)Math.round((double)xFactor*dotPlot.getX());
			int xMinEnd = selectRect.x;
			
			int xMaxStart = (int)Math.round((double)xFactor*(dotPlot.getX()+dotPlot.getWidth()));
			int xMaxEnd = selectRect.x+selectRect.width;

			int yMinStart = (int)Math.round(yFactor*dotPlot.getY());
			int yMinEnd = selectRect.y;
			
			int yMaxStart = (int)Math.round(yFactor*(dotPlot.getY()+dotPlot.getHeight()));
			int yMaxEnd = selectRect.y+selectRect.height;
			
			
			xMinDiff = (xMinEnd-xMinStart) / (double)zoomSteps;
			xMaxDiff = (xMaxEnd-xMaxStart) / (double)zoomSteps;
			yMinDiff = (yMinEnd-yMinStart) / (double)zoomSteps;
			yMaxDiff = (yMaxEnd-yMaxStart) / (double)zoomSteps;
			
			//System.out.println("Zooming from " + xMaxStart + " to " + xMaxEnd);
			//System.out.println("xmax : " + xMax + " xMaxDiff : " + xMaxDiff);
			timer.start();
			repaint();
		}
		
	}

	/**
	 * Called when we're zooming in or out 
	 * @param e
	 */
	public void actionPerformed(ActionEvent e) {
		zoomStep++;
		//System.out.println("Timer's gettin called, step: xd: " + zoomStep*xMaxDiff + " yd: " + zoomStep*yMinDiff);
		
		System.out.println("new xmax: " + (int)Math.round(zoomStep+xMaxDiff));
		//System.out.println("Drawing " + zoomStep*xMinDiff + ", " + zoomStep*yMinDiff + " to " + zoomStep*xMaxDiff + ", " + zoomStep*yMaxDiff + "");
		dotPlot.setZoom( (int)Math.round(zoomStep*xMinDiff), (int)Math.round(zoomStep*yMinDiff),  (int)Math.round((double)getWidth()*dotPlot.getWidth() + zoomStep*xMaxDiff), (int)Math.round((double)getHeight()*dotPlot.getHeight() + zoomStep*yMaxDiff));
		//dotPlot.setSequenceBounds(dotPlot.sequenceOneMin+xMinDiff, dotPlot.sequenceOneMax+xMaxDiff, dotPlot.sequenceTwoMin+yMinDiff, dotPlot.sequenceTwoMax+yMaxDiff);
		repaint();
		if (zoomStep == zoomSteps) {
			isZooming = false;
			zoomStep = 0;
			timer.stop();
			//dotPlot.setBlockWidth(oldBlockWidth);
			repaint();
		}
	}
	
}
