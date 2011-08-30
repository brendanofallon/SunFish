package plugins.SGPlugin.parsers;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import display.DisplayData;
import element.sequence.SequenceGroup;
import errorHandling.FileParseException;

import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;

/**
 * The superclass of things that can return SequenceGroups from a parsed file. 
 * This currently just exists so that we can properly assign the name of the sequence
 * group from the input file. 
 * @author brendan
 *
 */
public abstract class SequenceFileParser extends FileParser {

	public SequenceFileParser(SunFishFrame sunfishParent, Logger logger) {
		super(sunfishParent, logger);
	}

	/**
	 * Overrides FileParser.parse so that we can set the name is this sequence group to the
	 * file name. 
	 */
	public DisplayData parse(File file) throws IOException, FileParseException {
		Object info = readFile(file);
		DisplayData data = new DisplayData(file, info, getPreferredDisplayClass());
		if (info instanceof SequenceGroup)
			((SequenceGroup)info).setName(file.getName());
		data.setIcon(icon);
		return data;
	}
}
