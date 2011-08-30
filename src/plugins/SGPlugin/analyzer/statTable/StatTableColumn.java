package plugins.SGPlugin.analyzer.statTable;

import java.util.ArrayList;
import java.util.List;

import plugins.SGPlugin.sgStatistics.BaseCounter;
import plugins.SGPlugin.sgStatistics.SGCalculator;

import element.sequence.Partition;
import element.sequence.PartitionChangeListener;
import element.sequence.Partitionable;
import element.sequence.SequenceGroup;
import element.sequence.SequenceGroupChangeListener;
import guiWidgets.StringUtilities;

/**
 * A single column in a StatTable that displays a list of statistics for a sequence group, or a
 * partition of a sequence group. We pay attention to sequence change events and partition change
 * events and recompute values as needed, notifying the stat table when our values have changed. 
 * @author brendan
 *
 */
public class StatTableColumn implements SequenceGroupChangeListener, PartitionChangeListener {

	SequenceGroup sg;
	StatTable table;
	String name;
	Integer partition = 0; //Default partition
	String[] values;
	
	public StatTableColumn(StatTable statTable, SequenceGroup sg, String name, List<SGCalculator> calculators) {
		this.table = statTable;
		this.sg = sg;
		this.name = name;
		sg.addSGChangeListener(this);
		sg.addPartitionListener(this);
	}
	
	public StatTableColumn(StatTable statTable, SequenceGroup sg, String name, List<SGCalculator> calculators, int partitionNumber) {
		this(statTable, sg, name, calculators);
		this.partition = partitionNumber;
	}

	public String getName() {
		return name;
	}
	
	/**
	 * Returns a reference to the sequence group this table column is tracking
	 * @return
	 */
	public SequenceGroup getSG() {
		return sg;
	}
	
	/**
	 * Adds a new calculator and immediately computes the new value and adds it to the list of values
	 * tracked by this column. 
	 * @param newCalc
	 */
	public void addStatistic(SGCalculator newCalc) {
		String[] newValues = new String[values.length+1];
		for(int i=0; i<values.length; i++)
			newValues[i] = values[i];
		int index = values.length;
		
		SGCalculator thisCalc = newCalc.getInstance(sg);
		if (thisCalc instanceof BaseCounter) {
			BaseCounter bc = (BaseCounter)thisCalc;
			newValues[index] = StringUtilities.format( bc.getValueRange(0, sg.getMaxSeqLength(), (double)partition), 3);
		}
		else
			newValues[index] = StringUtilities.format( thisCalc.getValue(), 3);
		values = newValues;
		//System.out.println("Adding statistic " + newCalc.getName() + " to " + getName() + " values size is: " + values.length);
	}
	
	/**
	 * Return an array holding the values as calculated by the current list of calculators on the SG associated with this column
	 * @return
	 */
	public Object[] getValues() {
		if (values == null)
			computeValues();
		return values;
	}
	
	/**
	 * Recompute values for all calculators for this SG
	 */
	protected void computeValues() {
		List<SGCalculator> calculators = table.getCalculators();
		values = new String[calculators.size()];
		for(int i=0; i<calculators.size(); i++) {
			SGCalculator calc = calculators.get(i);
			SGCalculator thisCalc = calc.getInstance(sg);
			
			if (thisCalc instanceof BaseCounter) {
				BaseCounter bc = (BaseCounter)thisCalc;
				values[i] = StringUtilities.format( bc.getValueRange(0, sg.getMaxSeqLength(), (double)partition), 3);
			}
			else
				values[i] = StringUtilities.format( thisCalc.getValue(), 3);
			
		}
		//System.out.println("Computing values for " + getName() + " values size is: " + values.length);
	}
	

	@Override
	public void sgChanged(SequenceGroup source, ChangeType type) {
		if (source == sg) {
			computeValues();
		}
	}

	@Override
	public void partitionStateChanged(Partitionable source,
			PartitionChangeType type) {
		if (source != sg)
			return;
		
		if (type == PartitionChangeType.NEW_PARTITION) {
			table.handlePartitionAdd(this, sg.getPartitionCount()-1);
		}
		
		if (type==PartitionChangeType.REMOVED_PARTITION || type==PartitionChangeType.PARTITIONS_CLEARED) {
			table.handlePartitionRemoved(this);
		}
		
		computeValues();
	}
	
	/**
	 * Return the index of the partition this column applies to. 
	 * @return
	 */
	public int getPartitionIndex() {
		return partition;
	}

	/**
	 * Stop this object from listening to sg change events so the sg no longer holds references to this
	 */
	public void clearListeners() {
		sg.removePartitionListener(this);
		sg.removeSGChangeListener(this);
		
	}
}
