package plugins.SGPlugin.analyzer;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;


import element.DoubleRectangle;
import element.sequence.Sequence;
import figure.Figure;
import figure.FigureElement;
import guiWidgets.StringUtilities;

/**
 * This handles the actual drawing of the dot plot, including the axes
 * @author brendan
 *
 */
public class DotPlotElement extends FigureElement {

	Sequence sequenceOne;
	Sequence sequenceTwo;
	
	Rectangle2D rect = new Rectangle2D.Double(); //Buffer for drawing rects
	
	Color cellColor = Color.gray;
	
	BufferedImage dotPlotImage = null;
	Graphics2D dpg = null; //Graphics used to draw image
	boolean redrawImage = true; //Whether we need to redraw the image
	
	boolean imageIsBeingRedrawn = false;  //This gets set while we're actually drawing
		
	int blockWidth = 1; //The number of identical bases two sequences need in order to induce a drawn cell

	//A couple of buffers we use to store substrings of the sequences
	List<String> substrs1 = new ArrayList<String>();
	List<String> substrs2 = new ArrayList<String>();
	
	Map<String, Integer> substrMap = new HashMap<String, Integer>();
	
	int xAxisTicks = 5;
	int yAxisTicks = 5;
	
	ProgressMonitor progressMonitor;
	
	//We store some of the info that was used to draw the image so we can 
	//see if it changes (and if so, we redraw the image);
	DoubleRectangle boundsUsedForImage = new DoubleRectangle();
	double xFactorUsedForImage = 0;
	double yFactorUsedForImage = 0;
	
	Font labelFont = new Font("Sans", Font.PLAIN, 11);
	
	//When the selection rect is being drawn, we draw little marks on the axes at these points
	int yMarkerMin = 0;
	int yMarkerMax = 0;
	int xMarkerMin = 0;
	int xMarkerMax = 0;
	
	DotPlotFigure dotPlotParent;
	
	int sequenceOneMin;
	
	int sequenceOneMax;
	int sequenceTwoMin;
	int sequenceTwoMax;
	
	NumberFormat formatter = new DecimalFormat("0");
	
	//The following variables are set when we're zooming to a selection rectangle
	//the sx... s define the corners of the image that are stretched into the drawing region
	boolean useZoom = false;
	int sx1 = 0;
	int sy1 = 0;
	int sx2 = 0;
	int sy2 = 0;
	
	public DotPlotElement(DotPlotFigure parent) {
		super(parent);
		dotPlotParent = parent;	
	}

	public void setSequences(Sequence seq1, Sequence seq2) {
		sequenceOne = seq1;
		sequenceTwo = seq2;
		
		int minLength = Math.min(seq1.length(), seq2.length());
		sequenceOneMin = 0;
		sequenceTwoMin = 0;
		
		sequenceOneMax = minLength;
		sequenceTwoMax = minLength;
		
		recalcSubstrs();
		
		redrawImage = true;
	}

	/**
	 * Set the region of sequence space to draw
	 * @param oneMin
	 * @param oneMax
	 * @param twoMin
	 * @param twoMax
	 */
	public void setSequenceBounds(int oneMin, int oneMax, int twoMin, int twoMax) {
		sequenceOneMin = Math.max(oneMin, 0);
		sequenceTwoMin = Math.max(twoMin, 0);
		sequenceOneMax = Math.min(oneMax, sequenceOne.length());
		sequenceTwoMax = Math.min(twoMax, sequenceTwo.length());
		redrawImage = true;
	}
	
	/**
	 * Recompute the substring buffers, this needs to happen anytime the sequences are changed or the
	 * block width changes...and results in a tiny speedup
	 */
	private void recalcSubstrs() {
		substrs1.clear();
		substrs2.clear();
		for(int i=0; i<Math.min(sequenceOne.length(), sequenceTwo.length()); i++) {
			substrs1.add( sequenceOne.toString().substring(i, Math.min(i+blockWidth, sequenceOne.length())) );
			substrs2.add( sequenceTwo.toString().substring(i, Math.min(i+blockWidth, sequenceTwo.length())));
		}
	}
	
	/**
	 * Get the number of bases which must be equal to draw a cell rectangle
	 * @return
	 */
	public int getBlockWidth() {
		return blockWidth;
	}

	/**
	 * Set the number of sites which must be equal to draw a cell
	 * @param blockWidth
	 */
	public void setBlockWidth(int blockWidth) {
		if (this.blockWidth != blockWidth) {
			this.blockWidth = blockWidth;
			recalcSubstrs();
			redrawImage = true;
		}
	}
	
	
	
	/**
	 * Notifies this element that it should redraw it's image, probably because the width or height of the element
	 * has changed
	 */
	public void flagRedrawImage() {
		redrawImage = true;
	}
	
//	private void drawCell(Graphics2D g, Rectangle2D rect) {
//		g.fill(rect);
//	}
	
	private boolean getVariablesHaveChanged() {
		if (boundsUsedForImage.x != bounds.x)
			return true;
		if (boundsUsedForImage.y != bounds.y)
			return true;
		if (boundsUsedForImage.width != bounds.width)
			return true;
		if (boundsUsedForImage.height != bounds.height)
			return true;
		if (xFactorUsedForImage != xFactor)
			return true;
		if (yFactorUsedForImage != yFactor) {
			return true;
		}
		
		return false;	
	}
	
	/**
	 * Force the image to be stretched into the rectangle defined by the four given points; this
	 * happens when we zoom to a selected area
	 * @param sx1
	 * @param sy1
	 * @param sx2
	 * @param sy2
	 */
	public void setZoom(int sx1, int sy1, int sx2, int sy2) {
		this.sx1 = sx1;
		this.sy1 = sy1;
		this.sx2 = sx2;
		this.sy2 = sy2;
		useZoom = true;
	}
	
	/**
	 * Unset forcing of image stretching
	 */
	public void unsetZoom() {
		useZoom = false;
	}
	
	public void paint(Graphics2D g) {
		if (sequenceOne==null || sequenceTwo==null) {
			System.out.println("Not drawing plot since one of the sequences is null");
			return;
		}
		
		redrawImage = redrawImage || getVariablesHaveChanged();
		
		if (redrawImage) 
			drawImage();
		
		int imageX = (int)Math.round(bounds.x*xFactor);
		int imageY = (int)Math.round(bounds.y*yFactor);
		
		if (imageIsBeingRedrawn) {
			g.drawString("Please wait", 20, 20);
		}
		else {
			if (useZoom) {
				//g.drawImage(dotPlotImage, imageX, imageY, dotPlotImage.getWidth(), dotPlotImage.getHeight(), null);
				g.drawImage(dotPlotImage, imageX, imageY, imageX+dotPlotImage.getWidth(), imageY+dotPlotImage.getHeight(), sx1,sy1, sx2, sy2, null);
//				g.setColor(Color.RED);
//				g.drawRect(imageX+ sx1, imageY + sy1, sx2-sx1, sy2-sy1);
//				g.setColor(Color.black);
			}
			else
				g.drawImage(dotPlotImage, imageX, imageY, dotPlotImage.getWidth(), dotPlotImage.getHeight(), null);

			//Draw x-axis
			g.setColor(Color.black);
			int xAxisY = imageY+dotPlotImage.getHeight()+1; //y-value in pixels of x-axis
			g.drawLine(imageX, xAxisY, imageX+dotPlotImage.getWidth(), xAxisY);
			g.setFont(labelFont);
			FontMetrics fm = g.getFontMetrics();
			double tickStep = dotPlotImage.getWidth() / ((double)xAxisTicks-1);
			for(int i=0; i<xAxisTicks; i++) {
				int tickX = (int)Math.round(imageX+i*tickStep);
				g.drawLine(tickX, xAxisY, tickX, xAxisY+4);	
				String label = formatter.format(dataXForPixelX(tickX));
				g.drawString(label, tickX-fm.stringWidth(label)/2, xAxisY+5+fm.getHeight());
			}

			//Draw the y-axis
			int yAxisX = (int)Math.round(bounds.x*xFactor);
			g.drawLine(yAxisX, imageY, yAxisX, imageY+dotPlotImage.getHeight());
			tickStep = dotPlotImage.getHeight() / ((double)yAxisTicks-1);
			for(int i=0; i<yAxisTicks; i++) {
				int tickY = (int)Math.round(imageY+i*tickStep);
				g.drawLine(yAxisX-4, tickY, yAxisX, tickY);	
				String label = formatter.format(dataYForPixelY(tickY));
				g.drawString(label, yAxisX-fm.stringWidth(label)-6, tickY+5);
			}
			
			if (yMarkerMax>0 && (dotPlotParent.isCurrentlyRectSelecting() || dotPlotParent.isSelectionRectIsPreserved()) ) {
				g.setColor(Color.GRAY);
				g.drawLine(yAxisX-6, yMarkerMax, yAxisX-1, yMarkerMax);
				g.drawLine(yAxisX-6, yMarkerMin, yAxisX-1, yMarkerMin);
				g.drawLine(xMarkerMin, xAxisY+1, xMarkerMin, xAxisY+6);
				g.drawLine(xMarkerMax, xAxisY+1, xMarkerMax, xAxisY+6);
				g.drawString(formatter.format(dataXForPixelX(xMarkerMin)), xMarkerMin-6, xAxisY+17);
				g.drawString(formatter.format(dataXForPixelX(xMarkerMax)), xMarkerMax-6, xAxisY+17);
				
				String lab = formatter.format(dataYForPixelY(yMarkerMin));
				g.drawString(lab, yAxisX-fm.stringWidth(lab)-6, yMarkerMin+4);
				
				lab = formatter.format(dataYForPixelY(yMarkerMax));
				g.drawString(lab, yAxisX-fm.stringWidth(lab)-6, yMarkerMax+4);
			}
			
		}	
	}

	public double dataYForPixelY(int pixelY) {
		return sequenceTwoMax-((double)(pixelY-yFactor*bounds.y))/(yFactor*getHeight())*(sequenceTwoMax-sequenceTwoMin);
	}
	
	public double dataXForPixelX(int pixelX) {
		return ((double)(pixelX-xFactor*bounds.x))/(xFactor*getWidth())*(sequenceOneMax-sequenceOneMin) + sequenceOneMin;  
	}
	
	private void drawImage() {
		
		progressMonitor = new ProgressMonitor(parent, "Drawing dot plot", "", 0, 100);
		//System.out.println("Redrawing image with seq one bounds " + sequenceOneMin + " .. " + sequenceOneMax);
		imageIsBeingRedrawn = true;
		ImageDrawer drawer = new ImageDrawer();
		drawer.execute();
				
		progressMonitor.close();
		
		redrawImage = false;
	}

	class ImageDrawer extends SwingWorker {

		@Override
		protected Object doInBackground() throws Exception {
//			if (dotPlotImage==null) {
//				dotPlotImage = parent.getGraphicsConfiguration().createCompatibleImage((int)Math.round(xFactor*getWidth()), (int)Math.round(yFactor*getHeight()), Transparency.TRANSLUCENT );
//				dpg = dotPlotImage.createGraphics();
//			}
//			else {
//				dpg.setColor(Color.white);
//				dpg.fillRect(0, 0, dotPlotImage.getWidth(), dotPlotImage.getHeight());
//			}
			
			dotPlotImage = parent.getGraphicsConfiguration().createCompatibleImage((int)Math.round(xFactor*getWidth()), (int)Math.round(yFactor*getHeight()), Transparency.TRANSLUCENT );
			dpg = dotPlotImage.createGraphics();
			
			boundsUsedForImage.x = bounds.x;
			boundsUsedForImage.y = bounds.y;
			boundsUsedForImage.width = bounds.width;
			boundsUsedForImage.height = bounds.height;
			
			xFactorUsedForImage = xFactor;
			yFactorUsedForImage = yFactor;
			
			dpg.setColor(cellColor);
			double xWidth = (double)dotPlotImage.getWidth()/(sequenceOneMax-sequenceOneMin);
			double yWidth = (double)dotPlotImage.getHeight()/(sequenceTwoMax - sequenceTwoMin);
			
			int cellXWidth = (int)Math.max(Math.round(xWidth), 1);
			int cellYWidth = (int)Math.max(Math.round(yWidth), 1);
			
			double stepSizeOne = (double)(sequenceOneMax-sequenceOneMin) / (double)dotPlotImage.getWidth();
			double stepSizeTwo = (double)(sequenceTwoMax-sequenceTwoMin) / (double)dotPlotImage.getWidth();
			
			int maxCalcs = (sequenceOneMax-sequenceOneMin)*(sequenceTwoMax-sequenceTwoMin);
			for(double i=sequenceOneMin; i<sequenceOneMax && (! progressMonitor.isCanceled()); i+=stepSizeOne) {
				int iIndex = (int)Math.floor(i);
				String str1 = substrs1.get(iIndex); 
				progressMonitor.setProgress((int)Math.round(100*(double)i/(double)maxCalcs));
				double ixWidth = (iIndex-sequenceOneMin)*xWidth;
				for(double j=sequenceTwoMin; j<sequenceTwoMax && (! progressMonitor.isCanceled()); j+=stepSizeTwo) {
					int jIndex = (int)Math.floor(j);
					String str2 = substrs2.get(jIndex); //sequenceTwo.toString().substring(j, Math.min(j+blockWidth, sequenceTwo.length()));

					boolean equal = str1.equals(str2); 
					if (equal) {
						rect.setRect(ixWidth, yWidth*(sequenceTwoMax-jIndex-1), cellXWidth, cellYWidth);
						dpg.fill(rect);				
					}
					
				}
				
			}

//			long endTime = System.currentTimeMillis();
//			System.out.println("Total elapsed time: " + (endTime-startTime));

			imageIsBeingRedrawn = false;
			parent.repaint();
			return null;
		}
		
	}
	
	
	public int getSequenceOneMin() {
		return sequenceOneMin;
	}

	public void setSequenceOneMin(int sequenceOneMin) {
		this.sequenceOneMin = sequenceOneMin;
	}

	public int getSequenceOneMax() {
		return sequenceOneMax;
	}

	public void setSequenceOneMax(int sequenceOneMax) {
		this.sequenceOneMax = sequenceOneMax;
	}

	public int getSequenceTwoMin() {
		return sequenceTwoMin;
	}

	public void setSequenceTwoMin(int sequenceTwoMin) {
		this.sequenceTwoMin = sequenceTwoMin;
	}

	public int getSequenceTwoMax() {
		return sequenceTwoMax;
	}

	public void setSequenceTwoMax(int sequenceTwoMax) {
		this.sequenceTwoMax = sequenceTwoMax;
	}

	
}
