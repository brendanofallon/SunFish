package plugins.xmlDisplay;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;

import errorHandling.ErrorWindow;
import errorHandling.FileParseException;

public class XMLParser extends FileParser {

	static final double VERSION = 1.0;
	
	String[] suffices = {"xml"};
	
	DocumentBuilderFactory factory;
	DocumentBuilder builder; 
	
	public XMLParser(SunFishFrame parent) {
		super(parent, parent.getLogger());
		
		if (parent.getProperty("xml.color")!=null) {
			Color color = parent.parseColor(parent.getProperty("xml.color"));
			setLabelColor(color);
		}
		if (parent.getProperty("xml.icon")!=null) {
			ImageIcon icon = new ImageIcon(parent.getIconPath() + parent.getProperty("xml.icon"));
			setIcon(icon);
		}
		
		factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			ErrorWindow.showErrorWindow(e);
		}
	}
	
	@Override
	public String getName() {
		return "XML parser";
	}

	@Override
	public String getDescription() {
		return "Reads XML-formatted files";
	}
	
	public double getVersionNumber() {
		return VERSION;
	}
	
	public Class getDataClass() {
		return org.w3c.dom.Document.class;
	}

	public String[] getMatchingSuffices() {
		return suffices;
	}
	
	public Object readFile(File file) throws IOException,
			FileParseException {
		org.w3c.dom.Document data = null;
		try {
			data = builder.parse(file);
		} 
		catch (SAXException se) {
			ErrorWindow.showErrorWindow(se, logger);
			logger.warning("Caught SAX exception while trying to load xml file " + infile.getName() + " : " + se);
			throw new FileParseException(se.toString());
		}
		
		//String toplevelname = data.getDocumentElement().getNodeName();
		//System.out.println("Read in a document with top level element name : " + toplevelname);
		data.getDocumentElement().normalize();
		return data;
	}

	
	protected boolean fileFormatMatches(File file) throws Exception {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.parse(file);
			return true;
		} 
		catch (Exception ex) {
			return false;	
		}
	}
	
	public Class getPreferredDisplayClass() {
		return XMLDisplay.class;
	}

	protected Object readFile(BufferedReader buf) throws IOException,
			FileParseException {
		//This doesn't need to do anything since we've overridden readFile(file)
		return null;
	}
	
	public void writeData(File file, Object data) throws IOException {
		throw new IllegalArgumentException("writeData not implemented yet for XML files");
	}
	
	

}
