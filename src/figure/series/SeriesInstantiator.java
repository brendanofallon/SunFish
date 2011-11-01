package figure.series;

/**
 * Interface for classes that can create an XYSeriesElement
 * @author brendan
 *
 */
public interface SeriesInstantiator {

	/**
	 * Return a brief description of the type of element drawn by the series element (Line, Box, etc.)
	 * @return
	 */
	public String getSeriesTypeName();
	
	/**
	 * Obtain a new new instance of an XYSeriesElement showing the series provided 
	 * @param series
	 * @param parent
	 * @return
	 */
	public XYSeriesElement getInstance(XYSeries series, XYSeriesFigure parent);
	
}
