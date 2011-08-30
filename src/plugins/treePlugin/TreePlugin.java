package plugins.treePlugin;

import java.util.ArrayList;
import java.util.List;

import display.Display;
import plugin.Plugin;
import plugins.treePlugin.parsers.NewickParser;
import plugins.treePlugin.parsers.NexusParser;
import plugins.treePlugin.parsers.PhyloXMLParser;
import plugins.treePlugin.parsers.TreeLogParser;
import plugins.treePlugin.treeDisplay.MultiTreeDisplay;
import plugins.treePlugin.treeDisplay.TreeDisplay;
import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;
import topLevelGUI.analyzer.Analyzable;

public class TreePlugin extends Plugin {

	@Override
	public List<Display> getDisplays() {
		List<Display> displays = new ArrayList<Display>();
		displays.add(new TreeDisplay(SunFishFrame.getSunFishFrame()));
		displays.add(new MultiTreeDisplay(SunFishFrame.getSunFishFrame()));
		return displays;
	}

	@Override
	public List<FileParser> getParsers() {
		List<FileParser> parsers = new ArrayList<FileParser>();
		parsers.add(new NewickParser(SunFishFrame.getSunFishFrame()));
		parsers.add(new PhyloXMLParser(SunFishFrame.getSunFishFrame()));
		parsers.add(new NexusParser(SunFishFrame.getSunFishFrame()));
		parsers.add(new TreeLogParser(SunFishFrame.getSunFishFrame()));
		return parsers;
	}

	@Override
	public List<Analyzable> getAnalyzers() {
		// TODO Auto-generated method stub
		return null;
	}

}
