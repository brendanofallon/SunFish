package figure.series;

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
	
	private final XYSeriesFigure parentFig;
	
	public SeriesManager(XYSeriesFigure parentFig) {
		this.parentFig = parentFig;
		SeriesInstantiator lineInstantiator = LineSeriesElement.getStaticInstantiator();
		elementNames.put(lineInstantiator.getSeriesTypeName(), lineInstantiator);
		
		SeriesInstantiator boxInstantiator = BoxSeriesElement.getStaticInstantiator();
		elementNames.put(boxInstantiator.getSeriesTypeName(), boxInstantiator);
		
		SeriesInstantiator markerInstantiator = MarkerLineElement.getStaticInstantiator();
		elementNames.put(markerInstantiator.getSeriesTypeName(), markerInstantiator);
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
	public XYSeriesElement getSeries(String typeName, XYSeries series) {
		SeriesInstantiator instantiator = elementNames.get(typeName);
		if (instantiator == null) {
			throw new IllegalArgumentException("Could not find series of type '" + typeName + "'");
		}
		else {
			return instantiator.getInstance(series, parentFig);
		}
	}
	
	/**
	 * Returns a newly created XYSeriesElement of the requested type, using the XYSeries in source as the underlying data
	 * @param source
	 * @param destType
	 * @return
	 */
	public XYSeriesElement convert(XYSeriesElement source, String destType) {
		SeriesInstantiator instantiator = elementNames.get(destType);
		if (instantiator == null) {
			throw new IllegalArgumentException("Could not find series of type '" + destType + "'");
		}
		else {
			return instantiator.getInstance(source.getSeries(), parentFig);
		}
	}
	
}
