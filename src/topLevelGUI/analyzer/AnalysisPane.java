package topLevelGUI.analyzer;

import guiWidgets.InsetShadowPanel;
import guiWidgets.RoundedBezelPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import topLevelGUI.SunFishFrame;



/**
 * The panel that houses all analysis panes. This may grow to be more complex sometime, but right
 * now all it does it 'show' new analyzers. Analyzers can optionally veto being removed by returning
 * false from canAndWillClose(), which is always called before the currently displayed analyzer is
 * removed. Currently no analyzers do this, but in theory one could prompt a user to save an image
 * or some data prior to being disappeared, if need be.  
 * 
 * @author brendan
 *
 */
public class AnalysisPane extends InsetShadowPanel {

	Analyzable currentAnalyzer = null;
		
	public AnalysisPane(SunFishFrame sfFrame) {
		super(0, 7, 1, 7); //These numbers make the insets line up well on a mac (running OS X 10.7), but should be tested on other platforms
		setTransferHandler( new AnalysisPaneTransferHandler(sfFrame) );
		setPreferredSize(new Dimension(200, 500));
	}
	
	
	public Analyzable getCurrentAnalyzer() {
		return currentAnalyzer;
	}
	
	
	
	public void showAnalyzer(Analyzable newAnalyzer) {
		boolean canClose = true;
		if (currentAnalyzer!=null) {
			canClose = currentAnalyzer.canAndWillClose();
			
			if (canClose) {
				this.remove(currentAnalyzer);
				currentAnalyzer.closed();
			}
		}
		
		if (canClose) {
			newAnalyzer.setOpaque(false);
			add(newAnalyzer);
			currentAnalyzer = newAnalyzer;
			revalidate();
			repaint();
		}
	}
}
