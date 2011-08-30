package plugins.xmlDisplay;

import java.util.ArrayList;
import java.util.List;

import display.Display;
import plugin.Plugin;
import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;
import topLevelGUI.analyzer.Analyzable;

public class XMLPlugin extends Plugin {

	@Override
	public List<Display> getDisplays() {
		List<Display> displays = new ArrayList<Display>();
		displays.add(new XMLDisplay(SunFishFrame.getSunFishFrame(), SunFishFrame.getSunFishFrame().getLogger()));
		return displays;
	}

	@Override
	public List<FileParser> getParsers() {
		List<FileParser> parsers = new ArrayList<FileParser>();
		parsers.add(new XMLParser(SunFishFrame.getSunFishFrame()));
		return parsers;
	}

	@Override
	public List<Analyzable> getAnalyzers() {
		List<Analyzable> analyzers = new ArrayList<Analyzable>();
		return analyzers;
	}

}
