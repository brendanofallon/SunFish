package plugins.SGPlugin;

import java.util.ArrayList;
import java.util.List;

import display.Display;
import plugin.Plugin;
import plugins.SGPlugin.display.SGContentPanelDisplay;
import plugins.SGPlugin.parsers.FastaParser;
import plugins.SGPlugin.parsers.IMAParser;
import plugins.SGPlugin.parsers.PhyParser;
import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;
import topLevelGUI.analyzer.Analyzable;

public class SGPlugin extends Plugin {

	@Override
	public List<Display> getDisplays() {
		List<Display> displays = new ArrayList<Display>();
		displays.add(new SGContentPanelDisplay(SunFishFrame.getSunFishFrame()));
		return displays;
	}

	@Override
	public List<FileParser> getParsers() {
		List<FileParser> parsers = new ArrayList<FileParser>();
		parsers.add(new FastaParser(SunFishFrame.getSunFishFrame()));
		parsers.add(new PhyParser(SunFishFrame.getSunFishFrame()));
		parsers.add(new IMAParser(SunFishFrame.getSunFishFrame()));
		return parsers;
	}

	@Override
	public List<Analyzable> getAnalyzers() {
		// TODO Auto-generated method stub
		return null;
	}

}
