package plugins.textPlugin;

import java.util.ArrayList;
import java.util.List;

import display.Display;
import plugin.Plugin;
import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;
import topLevelGUI.analyzer.Analyzable;

public class TextPlugin extends Plugin {

	@Override
	public List<Display> getDisplays() {
		List<Display> d = new ArrayList<Display>();
		d.add(new TextDisplay(SunFishFrame.getSunFishFrame()));
		return d;
	}

	@Override
	public List<FileParser> getParsers() {
		List<FileParser> p = new ArrayList<FileParser>();
		p.add(new TextParser(SunFishFrame.getSunFishFrame()));
		return p;
	}

	@Override
	public List<Analyzable> getAnalyzers() {
		// TODO Auto-generated method stub
		return null;
	}

}
