package display;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

/**
 * This JPanel shows a small progress bar and is meant to be temporarily displayed 
 * whilst a large SequenceGroup or Tree is loading
 * @author brendan
 *
 */
public class ProgressPanel extends JPanel {

	//A value between zero and 1, where 1 indicates the task is complete
	private double progress = 0;
	
	//An optional note to be displayed above the progress indicator
	private String note = null;
	
	private Color textColor = Color.GRAY;
	
	//Dimensions of the progress bar
	private int barWidth = 150;
	private int barHeight = 20;
	
	/**
	 * Set the progress indicator to the given value, value should be between 0 and 1 
	 * @param prog
	 * @return
	 */
	public void setProgress(double prog) {
		this.progress = prog;
		repaint();
	}
	
	//Set an optional note to be displayed above the progress bar. This can be null. 
	public void setNote(String note) {
		this.note = note;
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		
		g2d.setColor(getBackground());
		g2d.fillRect(0, 0, getWidth(), getHeight());
		
		g2d.setColor(textColor);
		
		//Left edge of note and progress bar
		int xLoc = getWidth()/2 - barWidth/2;
		
		//Top of progress bar / bottom of note
		int yLoc = getHeight()/2;
		
		if (note != null)
			g2d.drawString(note, xLoc, yLoc-2);
		
		g2d.drawRect(xLoc, yLoc, barWidth, barHeight);
		g2d.fillRect(xLoc+1, yLoc+1, (int)Math.round((double)barWidth*progress), barHeight-1);
		
	}
	
}
