package plugins.argPlugin.arg.argStats;

import java.util.ArrayList;

import plugins.argPlugin.arg.ARG;
import plugins.argPlugin.arg.Range;

import element.Point;
import figure.series.XYSeries;


public class MaxHeight extends ARGStat {

	@Override
	public XYSeries computeSeries(ARG arg) {
		ArrayList<Point> points = new ArrayList<Point>();
		
		int steps = arg.getMaxSite() / stepSize;
		if (steps < 50)
			stepSize = 1;
		
		//Algorithm below only computes a new mrca height when the index site corresponds to a new range
		Range range = arg.getRangeForSite(arg.getMinSite());
		double mrcaHeight = arg.getMRCAForSite(range.getMin()).getNodeHeight();
		
		
		for(int site=arg.getMinSite(); site<arg.getMaxSite(); site+=stepSize) {
			Range siteRange = arg.getRangeForSite(site);
			if (range == siteRange) {
				points.add(new Point(site, mrcaHeight));	
			}
			else {
				range = siteRange;
				mrcaHeight = arg.getMRCAForSite(range.getMin()).getNodeHeight();
			}
		}
		return new XYSeries(points, "Marginal tree height");
	}

}
