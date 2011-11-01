package figure.series;

import java.awt.Container;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This thing knows what types of SeriesElements are available and hence can provide info
 * to configuration frames, etc, about available types
 * @author brendan
 *
 */
public class SeriesManager {

	private Map<String, SeriesInstantiator> elementNames = new HashMap<String, SeriesInstantiator>();
	
	
	public SeriesManager() {
		SeriesInstantiator lineInstantiator = LineSeriesElement.getInstantiator();
		elementNames.put(lineInstantiator.getSeriesTypeName(), lineInstantiator);
		
		SeriesInstantiator boxInstantiator = BoxSeriesElement.getInstantiator();
		elementNames.put(boxInstantiator.getSeriesTypeName(), boxInstantiator);
	}
	
	/**
	 * Return all registered element type names
	 * @return
	 */
	public Set<String> getElementTypeNames() {
		return elementNames.keySet();
	}
	
	/**
	 * Obtain a newly created series element of the type specified in the String argument 
	 * @param typeName
	 * @param series
	 * @param parentFig
	 * @return
	 */
	public XYSeriesElement getSeries(String typeName, XYSeries series, XYSeriesFigure parentFig) {
		SeriesInstantiator instantiator = elementNames.get(typeName);
		if (instantiator == null) {
			throw new IllegalArgumentException("Could not find series of type '" + typeName + "'");
		}
		else {
			return instantiator.getInstance(series, parentFig);
		}
	}
}
