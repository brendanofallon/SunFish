package plugins.SGPlugin.sgStatistics;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import element.sequence.*;
import figure.series.XYSeries;

/**
 * This type of XYSeries always has a BaseCounter associated with it. BaseCounters store intermediate
 * values, so preserving them allows for rapid re-computation of values (for instance, when we change
 * the window size or step we don't have to recompute the frequencies array all over again). 
 * 
 * @author brendan
 *
 */
public class BaseCounterSeries extends XYSeries {
	
	BaseCounter calculator;
	int partitionIndex = -1; //The partition to which we're assigned, if any. -1 indicates none.

	public BaseCounterSeries(ArrayList<Point2D> points, BaseCounter calc) {
		super(points);
		this.calculator = calc;
	}
	
	public BaseCounterSeries(ArrayList<Point2D> points, BaseCounter calc, int pIndex) {
		super(points);
		this.calculator = calc;
		this.partitionIndex = pIndex;
	}
	
	public BaseCounterSeries(ArrayList<Point2D> points, String name, BaseCounter calc) {
		super(points, name);
		this.calculator = calc;
	}
	
	public SequenceGroup getSequenceGroup() {
		return calculator.getSequenceGroup();
	}
	
	/**
	 * Sets the current partition index
	 * @param index
	 */
	public void setPartitionIndex(int index) {
		partitionIndex = index;
	}
	
	/**
	 * Get the partition index associated with this base counter.
	 * @return
	 */
	public int getPartitionIndex() {
		return partitionIndex;
	}
	
	public BaseCounter getCalculator() {
		return calculator;
	}

	public void replaceSeries(ArrayList<Point2D> points) {
		constructPointsFromList(points);
	}
	
	/**
	 * Kind of a kludge here.. in SeriesFigures, it really useful to have series' and their 
	 * calculators closely linked so if the parameters (say, window size) is changed, we can
	 * just ask the series to recalculate themselves
	 * @param calculator
	 */
	public void setCalculator(BaseCounter calculator) {
		this.calculator = calculator;
	}

}
