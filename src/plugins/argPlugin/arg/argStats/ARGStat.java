package plugins.argPlugin.arg.argStats;

import plugins.argPlugin.arg.ARG;
import figure.series.XYSeries;


/**
 * Base class of all things that can take an ARG and compute a series of values along its sequence length. 
 * 
 * @author brendan
 *
 */
public abstract class ARGStat {

	//The size of the interval in between sites at which we calculate the value. We can't calculate the value for every site,
	//so instead we just do it at periodic intervals, given by the following field
	int stepSize = 5;
	
	public int getStepSize() {
		return stepSize;
	}
	
	/**
	 * Sets the interval between sites at which we calculate the value
	 * @param newStep
	 */
	public void setStepSize(int newStep) {
		this.stepSize = newStep;
	}
	
	public abstract XYSeries computeSeries(ARG arg);
	
}
