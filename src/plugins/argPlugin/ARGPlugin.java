package plugins.argPlugin;

import java.util.ArrayList;
import java.util.List;

import display.Display;
import plugin.Plugin;
import plugins.argPlugin.argDisplay.ARGDisplay;
import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;
import topLevelGUI.analyzer.Analyzable;

public class ARGPlugin extends Plugin {

	@Override
	public List<Display> getDisplays() {
		List<Display> displays = new ArrayList<Display>();
		displays.add(new ARGDisplay(SunFishFrame.getSunFishFrame()));
		return displays;
	}

	@Override
	public List<FileParser> getParsers() {
		List<FileParser> parsers = new ArrayList<FileParser>();
		parsers.add(new GraphMLParser(SunFishFrame.getSunFishFrame()));
		return parsers;
	}

	@Override
	public List<Analyzable> getAnalyzers() {
		// TODO Auto-generated method stub
		return null;
	}

}
