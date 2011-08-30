package plugins.treePlugin.parsers;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import display.DisplayData;

import plugins.treePlugin.parsers.NexusParser.TreeReaderType;
import plugins.treePlugin.tree.DrawableNode;
import plugins.treePlugin.tree.DrawableTree;
import plugins.treePlugin.tree.Node;
import plugins.treePlugin.tree.SquareTree;
import plugins.treePlugin.tree.Tree;
import plugins.treePlugin.tree.reading.TreeFileForwardReader;
import plugins.treePlugin.tree.reading.TreeFileReader;
import plugins.treePlugin.tree.reading.TreeReader;
import plugins.treePlugin.treeDisplay.MultiTreeDisplay;



import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;
import errorHandling.ErrorWindow;
import errorHandling.FileParseException;

public class TreeLogParser extends FileParser implements PropertyChangeListener {

	String[] suffices = {"trees"};
	
	final String labelSep = ","; 	// Just for debugging! Use this later :"%#&"; //Something unlikely to ever appear in a real label
	int numInputTrees = 0;
	double targetFraction = 0.50; 	// Should be user-adjustable, this specifies the fraction of input trees
								  	//in which a clade must be to appear in the majority tree. Must be at least 0.50
	int subsampleRate = 1;    		// 1/fraction of trees to sample, if this is two, only every other tree will be sampled...
	int burninTrees = 0;
	
	DrawableTree majorityTree;
	ProgressMonitor progressMonitor;  //Displays a progress window on the screen
	MajorityTreeBuilder treeBuilder; //SwingWorker which actually handles building the tree
	TreeLogSetupFrame setupFrame;
	boolean cancel = false;
	
	StringBuilder merge; //working object for clade counting
	String emptyString = "";
	
	int totalClades = 0;
	int maxTrees = 50000;	//A hard cap on the total number of trees to include. Currently not user-adjustable (but could easily be so)
	int totalTreeEst = maxTrees;
	
	private boolean DEBUG = false;
	
	public TreeLogParser(SunFishFrame parent) {
		super(parent, parent.getLogger());
		merge = new StringBuilder();
		this.logger = parent.getLogger();
		
		if (parent.getProperty("trees.color")!=null) {
			Color color = parent.parseColor(parent.getProperty("trees.color"));
			setLabelColor(color);
		}
		if (parent.getProperty("trees.icon")!=null) {
			ImageIcon icon = new ImageIcon(parent.getIconPath() + parent.getProperty("trees.icon"));
			setIcon(icon);
		}
	}
	
	@Override
	public String getName() {
		return "Tree log parser";
	}

	@Override
	public String getDescription() {
		return "Reads multiple trees from .trees files";
	}

	@Override
	public double getVersionNumber() {
		return 1.0;
	}
	
	
	public Class getDataClass() {
		return DrawableTree.class;
	}
	
	public String[] getMatchingSuffices() {
		return suffices;
	}

	protected void parseFile() {
		NexusParser nexParser = null; //Used if this file is actually a nexus file
		cancel = false;
		targetFraction = setupFrame.getTargetFrequency();
		subsampleRate = setupFrame.getSubsampleFrequency();
		burninTrees = setupFrame.getBurninTrees();

        try {
        	BufferedReader buf = new BufferedReader(new FileReader(infile));
        	estimateTotalTrees(buf);
        	
        	TreeReader treeReader;
        	Map<String, String> translationTable = null;
        	buf = new BufferedReader(new FileReader(infile));
        	boolean isNexus = guessNexus(buf);
        	
        	if (isNexus) {
        		//System.out.println("File is NEXUS in disguise, using NexusParser as treeReader");
        		nexParser = new NexusParser(sunfishParent);
        		buf = new BufferedReader(new FileReader(infile));
        		nexParser.setPreferredReaderType(TreeReaderType.FORWARD_READER); //Use low-memory forward-only reader. This must come before .identifyBlocks!
        		nexParser.identifyBlocks(infile);
        		treeReader = nexParser;
        		
        		//System.out.println("done reading file, tree reader is now the nexus parser");
        	}
        	else {
        		treeReader = new TreeFileForwardReader(infile);
        	}
        	
        	translationTable = treeReader.getTranslationTable();
        	treeReader.setTranslationTable(null); //Prevents translating tip labels for every tree we read = performance optimization
        	
        	progressMonitor = new ProgressMonitor(sunfishParent, "Constructing majority tree", "Initializing", 0, 100);
        	//buildTreeNotInBackground(treeReader);
        	
        	progressMonitor.setMillisToDecideToPopup(100);
        	progressMonitor.setMillisToPopup(100);
        	treeBuilder = new MajorityTreeBuilder(treeReader, translationTable);
        	treeBuilder.addPropertyChangeListener(this);

        	treeBuilder.execute();
        }
        catch (IOException e) {
        	ErrorWindow.showErrorWindow(e, logger);
            infile = null;
        }
        catch (FileParseException fpe) {  		
      		ErrorWindow.showErrorWindow(fpe, logger);
        }
	
	}

	
	/**
	 * Detect is this .trees file is actually a NEXUS file in disguise - BEAST style
	 * @param buf
	 * @return True if the first non-blank line starts with #NEXUS 
	 * @throws IOException
	 */
	private boolean guessNexus(BufferedReader buf) throws IOException {
		String line = buf.readLine();
		while(line != null && line.trim().length()==0) {
			line = buf.readLine();
		}
		
		if (line==null) {
			return false;
		}
		
		if (line.trim().startsWith("#NEXUS")) {
			return true;
		}
		else 
			return false;
	}

	/**
	 * Entry point for file parsing, the first thing we do is see if the user wants to build a majority tree or view the
	 * trees one at a time.
	 */
	public void parseAndDisplay(File inputFile) {
		infile = inputFile;
		Object[] options = {"Cancel",
                "Show multi tree",
                "Build majority tree"};
		int n = JOptionPane.showOptionDialog(sunfishParent,
				"This file contains multiple trees ",
				"Choose display method",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[0]);
		
		if (n==0)
			return;
		if (n==1) {
			try {
				BufferedReader buf = new BufferedReader(new FileReader(infile));
				boolean isNexus = guessNexus(buf);
				TreeReader reader;
				if (isNexus) {
					NexusParser nexReader = new NexusParser(sunfishParent);
					nexReader.identifyBlocks(infile);
					reader = nexReader;
				}
				else {
					reader = new TreeFileReader(infile);
				}
				DisplayData data = new DisplayData(infile, reader, MultiTreeDisplay.class);
				sunfishParent.displayData(data, infile.getName());
			}
			catch (IOException ioex) {
				ErrorWindow.showErrorWindow(ioex, logger);
			}
			catch (FileParseException fpe) {
				ErrorWindow.showErrorWindow(fpe, logger);
			}
		}
		if (n==2) {
			setupFrame = new TreeLogSetupFrame(this);
			setupFrame.setVisible(true);        
		}
	}

	
	/**
	 * We have to implement this from the FileParser abstract superclass, but we don't ever use it
	 */
	protected Object readFile(BufferedReader buf) throws IOException,
			FileParseException {

		return null;
	}
	
	/**
	 * Called when the tree is done being constructed, tells the sunfishParent to display the tree
	 * @param tree
	 */
	public void displayTree(DrawableTree tree) {
		DisplayData data = new DisplayData(infile, tree, getPreferredDisplayClass());
		data.setIcon(icon);
		sunfishParent.displayData(data, data.getFileName());
	}
	
	public int estimateTotalTrees(BufferedReader buf) throws IOException {
		String line = buf.readLine();
		int total = 0;
		System.out.println("Estimating total tree count..");
		while(line != null && (!cancel)) {
			if (line.trim().length()>0 && ! line.startsWith("#") && line.contains("(") && line.contains(")"))
				total++;
			line = buf.readLine();
		}
		
		System.out.println("..done (got " + total + ")");
		totalTreeEst = total - burninTrees;
		return total;
	}
	
	
	/**
	 * Reads through the input buffer to parse out trees, then calls countClades on each
	 * tree to add all the clades to the hash table
	 * @param buf Input buffer
	 * @param clades HashTable to add clade info to
	 * @return Number of trees counted
	 * @throws IOException
	 */
	private int tabulateTrees(TreeReader reader,
			Map<String, TreeItem> clades) throws IOException {

		int countedTrees = 0;
		int examinedTrees =0;

		SunFishFrame.getSunFishFrame().setInfoLabelText("Parsing trees file");
		
		progressMonitor.setNote("Counting clades");
		progressMonitor.setProgress(0);
		DrawableTree tree = reader.getNextTree(); //This is the slow part and we do it for every single tree, regardless of subSampleRate
										//We should make a DelayedDrawableTree class that just reads in the string but doesn't do any parsing
										//or allocating until necessary.. this would probably be 100x faster
		
		while(tree!=null && countedTrees < maxTrees && (!cancel)) {
			examinedTrees++;
			//System.out.println("Examining tree #" + examinedTrees );
			if (tree!=null && (double)examinedTrees % subsampleRate == 0.0 && examinedTrees > burninTrees) {
				//System.out.println("Counting tree #" + examinedTrees + " val : " + (double)examinedTrees/subsampleRate );
				if (tree.getRoot().numOffspring()<2) {
					System.err.println("Error reading tree, found less than two two tips from root, not tabulating clades...");
				}
				else {
					if (DEBUG)
						System.out.println("Counting tree #" + examinedTrees);
					countClades(tree, (DrawableNode)tree.getRoot(), clades);
					countedTrees++;
				}
				

			}

			
			if (examinedTrees%25==0) {
				progressMonitor.setProgress((int)Math.round(95.0*(double)examinedTrees/(double)totalTreeEst));
				progressMonitor.setNote("Counting tree: " + examinedTrees + "/" + totalTreeEst);
			}
			tree = reader.getNextTree();
		}

		
		//System.out.println("Counted " + countedTrees + " of " + examinedTrees + " trees = " + (double)countedTrees/(double)examinedTrees + "%");
		return countedTrees;
	}
	
	/**
	 * This recursive function is responsible for adding all of the nodes in a tree to the growing 
	 * hash table. 
	 * @param root
	 * @param clades
	 * @return
	 */
	protected ArrayList<String> countClades(Tree tree, DrawableNode root, Map<String, TreeItem> clades) {
		ArrayList<String> tipLabels = new ArrayList<String>();
		if (root.numOffspring()==0) {
			tipLabels.add(root.getLabel());
		}
		else {
			tipLabels = countClades(tree, (DrawableNode)root.getOffspring(0), clades);
			for (int i=1; i<root.numOffspring(); i++) {
				tipLabels.addAll(countClades(tree, (DrawableNode)root.getOffspring(i), clades));
			}
//			for(Node kid : root.getOffspring()) {
//				
//			}
		}
		
//		Stack<DrawableNode> nodeStack = new Stack<DrawableNode>();
//		nodeStack.push(root);
//		while(nodeStack.size()>0) {
//			DrawableNode currentNode = nodeStack.pop();
//			if (currentNode.numOffspring()==0) {
//				tipLabels.add(root.getLabel());
//			}
//			else {
//				
//			}
//			
//		}
					
		Collections.sort(tipLabels);
		merge.replace(0, merge.length(), emptyString);
		
		for(String label : tipLabels) {
			merge.append(label + labelSep);
		}

		root.addAnnotation("tips", merge.toString()); 
		String key = merge.toString();
		TreeItem hashItem = clades.get(key);
		totalClades++;
		if (hashItem==null) {
			TreeItem newItem = new TreeItem();
			newItem.count = 1;
			newItem.distToParent = root.getDistToParent();
			newItem.cardinality = tipLabels.size();
			newItem.height = getNodeHeight(tree, root);
			newItem.M2 = 0;	//Running total of variance in dist to parent
			newItem.clade = key;
			clades.put(key, newItem);
			//System.out.println("Hash item for key " + key + " was null, putting new item with count : " + newItem.count);
		}
		else {
			hashItem.count++;
			double delta = root.getDistToParent()-hashItem.distToParent;
			hashItem.distToParent += delta/(double)hashItem.count;
			
			//hashItem.M2 += delta*(root.getDistToParent()-hashItem.distToParent);
			
			double height = getNodeHeight(tree, root);
			delta = height-hashItem.height;
			hashItem.height += delta/(double)hashItem.count;
			hashItem.M2 += delta*(height-hashItem.height);
			//System.out.println("Found item with key " + key + " increasing count to : " + hashItem.count);
		}
		return tipLabels;
	}

	/**
	 * For debugging purposes...
	 * @param buf
	 * @throws IOException
	 */
	private void buildTreeNotInBackground(TreeReader treeReader) throws IOException, FileParseException {
		DEBUG  = true;
		
		long startTime = System.currentTimeMillis();
		Hashtable<String, TreeItem> clades = new Hashtable<String, TreeItem>();
		
		System.out.println("Reading in trees & building hash table...");
		numInputTrees = tabulateTrees(treeReader, clades);
		
		System.out.println("Building majority clade list");
		ArrayList<TreeItem> majorityClades = buildMajorityCladeList(clades);
		
		
		System.out.println("Merging clades");
		DrawableNode root = mergeClades(majorityClades);
		
		progressMonitor.setNote("Calculating distances");
		setDistFromTips(root);

		System.out.println("Constructing final tree");
		majorityTree = new SquareTree(root);

		
		DEBUG = false;
		displayTree(majorityTree);

	}
	/**
	 * Extracts the majority clades from the hashtable of all clades and, then sorts it by
	 * clade cardinality, then returns it
	 * 
	 * @param clades
	 * @return
	 */
	private ArrayList<TreeItem> buildMajorityCladeList(Map<String, TreeItem> clades) {
		ArrayList<TreeItem> cladeList = new ArrayList<TreeItem>();
		int numKeys = clades.size();
		int currentKey = 0;
		progressMonitor.setNote("Building majority clade list");
		
		for(String key : clades.keySet()) { //; key.hasMoreElements() && (!cancel); ) {
			TreeItem cladeInfo = clades.get(key);
			//System.out.println("Clade : " + clade + " frequency : " + cladeInfo.count/(double)numInputTrees);
			if ((double)cladeInfo.count/(double)numInputTrees > targetFraction) {
				cladeList.add(cladeInfo);
			}

			if (cancel) {
				break;
			}
			currentKey++;
		}
		
		Collections.sort(cladeList);
		return cladeList;
	}


	/**
	 * Reads through a list of clade info items and adds clades successively to tree
	 * Clade info list MUST be sorted by clade cardinality (with biggest cardinality--
	 * the clade corresponding to the root, first)
	 * 
	 * @param majorityClades
	 * @return
	 */
	protected DrawableNode mergeClades(ArrayList<TreeItem> majorityClades) throws FileParseException {
		DrawableNode root = new DrawableNode();
		progressMonitor.setNote("Merging clades");
		if (majorityClades.size()==0) {
			throw new FileParseException("Error building majority tree: Could not identify majority clades");
		}
		
		if (majorityClades.size()<3) {
			System.out.println("Hmmm... majority clades is very small, majority tree builder may have been messed up..");
		}
		
		TreeItem cladeInfo = majorityClades.get(0);
		root.addAnnotation("tips", cladeInfo.clade);
		root.addAnnotation("height", new Double(cladeInfo.height).toString());
		double var = cladeInfo.M2/(double)(cladeInfo.count-1.0);
		//System.out.println("Root node var : " + var);
		if (var>0) {
			System.out.println("Root node error : " + Math.sqrt(var));
			root.addAnnotation("error", new Double(Math.sqrt(var)).toString());
		}
		for(int i=1; i<majorityClades.size() && (!cancel); i++) {
			addClade(root, majorityClades.get(i));
			//System.out.println("Adding clade to tree root node.. : " + majorityClades.get(i).clade);
		}

		//Tree simpleTree = new Tree(root);
		//System.out.println("Newick tree: " + simpleTree.getNewick());
		if (root.numOffspring()<2) {
			System.out.println("Root has only one or two offspring, merge clades must have failed somehow");
		}
		if (root==null) {
			System.out.println("merge clades is returning a null node");
		}
		return root;
	}
	
	/**
	 * Post-order traverse the tree, calculating the 'distToParent' value for all nodes in the tree. The distToParent is 
	 * calculated from the 'height' annotation, and is just parentHeight - myHeight. This happens as a final step after the
	 * majority tree has been computed. 
	 * 
	 * @param node
	 */
	protected void setDistFromTips(DrawableNode node) {
		progressMonitor.setNote("Calculating for clade : " + node.getAnnotationValue("tips"));
				
		for(Node kid : node.getOffspring()) {
			setDistFromTips((DrawableNode)kid);
		}
	
		//System.out.println("..after recursion..");
		if (node.getParent()==null) { //Can't set dist to parent if there's no parent
			return;
		}
		
		String parentDist = ((DrawableNode)node.getParent()).getAnnotationValue("height");
		String dist = node.getAnnotationValue("height");
		if (parentDist==null) {
			System.out.println("Clade : " + node.getAnnotationValue("tips") + " 's parent has no dist to tip annotation ");
		}
		if (dist==null) {
			System.out.println("Clade : " + node.getAnnotationValue("tips") + " has no dist to tip annotation ");
		}
		try {
			double pDist = Double.parseDouble(parentDist);
			double myDist = Double.parseDouble(dist);
			
			//System.out.println("Clade : " + node.getAnnotationValue("tips") + " from " + node.getDistToParent() + " pDist : " + pDist + " myDist: " + myDist + " to " + (pDist-myDist));
			node.setDistToParent(pDist-myDist);
		} catch (Exception ex) {
			System.err.println("Couldn't find dist to tip annotation in node..not setting parent dist");	
		}
			
	}
	
	/**
	 * Recursive (pre-order traversal) function which identifies the right spot in the tree to which to add a new clade and
	 * then adds it to the root
	 * @param root
	 * @param cladeInfo
	 */
	private void addClade(DrawableNode root, TreeItem cladeInfo) {
		//boolean add = false;
		//We traverse the tree and look for a node that contains this clade, but
		//that does not have any children that contain this clade
		
		if (containsClade(root, cladeInfo.clade)) {
			boolean found = false;
			for(Node kid : root.getOffspring()) {
				if (containsClade((DrawableNode)kid, cladeInfo.clade)) {
					found = true;
					
					addClade((DrawableNode)kid, cladeInfo);
				}
			}
			if (!found) {
				//Here the root contains this clade, but none of root's kids do, so add the clade here
				//System.out.println("Clade " + cladeInfo.clade + " was not found in any kids, but was contained in this node, so adding here");
				DrawableNode newNode = new DrawableNode();
				newNode.addAnnotation("tips", cladeInfo.clade);
				newNode.addAnnotation("support", new Double((double)cladeInfo.count/(double)numInputTrees).toString());
				double height = cladeInfo.height;
				newNode.addAnnotation("height", new Double(height).toString());
				double var = cladeInfo.M2/(double)(cladeInfo.count-1.0);
				//System.out.print("Var: " + var);
				if (var>0) {
					double stdev = Math.sqrt(var);
					//System.out.println(" stdev : " + stdev);
					newNode.addAnnotation("error", new Double(stdev).toString());	
				}
					
				
				newNode.setParent(root);
				root.addOffspring(newNode);
				try {
					double parentHeight = Double.parseDouble( root.getAnnotationValue("height") ) ;
					newNode.setDistToParent( parentHeight- cladeInfo.height);
				}
				catch (Exception nfe) {
					System.err.println("Could not read height value from root node, this means we can't set the node height for a node. Uh oh");
				}
				
				if (cladeInfo.cardinality==1) {
					newNode.setLabel( cladeInfo.clade.replaceAll(labelSep, "") );
				}
			}
		}
	}
	


	private boolean containsClade(DrawableNode root, String clade) {
		String[] rootTips = root.getAnnotationValue("tips").split(labelSep);
		String[] cladeTips = clade.split(labelSep);

		for(int i=0; i<cladeTips.length; i++) {
			String tip = cladeTips[i];
			boolean foundMatch = false;
			for(int j=0; j<rootTips.length; j++) {
				if (tip.equals(rootTips[j])) {
					foundMatch = true;
					break;
				}	
			}
			
			if (!foundMatch) {
				return false;
			}
			
		}
		return true;
	}

	
	
	protected double getNodeHeight(Tree tree, Node n) {
		//double tot = 0;
		//Node ptr = n;
		double distToRoot = Tree.getDistToRoot(n);
		double treeHeight = tree.getHeight(); 
//		while(ptr.numOffspring()>0) {
//			ptr = ptr.getOffspring(0);
//			tot += ptr.getDistToParent();
//		}
		double height = treeHeight - distToRoot;
		if (height<0) {
			System.err.println("Uh-oh, calculated node height is less than zero for this tree! :  " + tree.getNewick());
		}
		return height;
	}
	
	
	protected void removeAnnotation(Tree tree, String anno) {
		Stack<DrawableNode> nodes = new Stack<DrawableNode>();
		nodes.push( (DrawableNode)tree.getRoot());
		
		while(nodes.size()>0) {
			DrawableNode node = nodes.pop();
			for(Node kid : node.getOffspring()) {
				nodes.push( (DrawableNode)kid);
			}
			node.removeAnnotation(anno);
		}
	}
	
	/**
	 * Attempts to read in a tree from a string. Everything prior to the first ( is
	 * ignored
	 * @param line, the line from which to parse the tree
	 * @return a DrawableTree version of the tree
	 */
	protected DrawableTree readTreeLine(String line) {
		int firstParen = line.indexOf("(");
		int lastParen = line.lastIndexOf(")");
		if (firstParen < 0 || lastParen < firstParen)
			return null;
		
		String treeStr = line.substring(firstParen, lastParen+1);
		
		DrawableTree tree = new SquareTree(treeStr);
		return tree;
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName() ) {
			int progress = (Integer) evt.getNewValue();
			progressMonitor.setProgress(progress);

			if (progressMonitor.isCanceled()) {
				System.out.println("Progress has been canceled");
				treeBuilder.cancel();
				this.cancel = true;
			}
		}

	}
	
	
	
	
	class MajorityTreeBuilder extends SwingWorker<Void, Void> {

		Map<String, String> translationTable = null;
		TreeReader treeReader;
		boolean cancel = false;

		public MajorityTreeBuilder(TreeReader reader, Map<String, String> translationTable) {
			this.translationTable = translationTable;
			this.treeReader = reader;
		}
		
		public void cancel() {
			cancel = true;
		}
		
		protected Void doInBackground() throws FileParseException, Exception {

			Map<String, TreeItem> clades = new HashMap<String, TreeItem>();
			//System.out.println("Reading in trees & building hash table...");
			
			SunFishFrame.getSunFishFrame().setInfoLabelText("Parsing trees file");
			numInputTrees = tabulateTrees(treeReader, clades);
			setProgress(90);
			
			if (cancel) {
				return null;
			}
	 
			SunFishFrame.getSunFishFrame().setInfoLabelText("Building clade list");
			ArrayList<TreeItem> majorityClades = buildMajorityCladeList(clades);
			if (cancel) {
				return null;
			}
			setProgress(95);
			
			SunFishFrame.getSunFishFrame().setInfoLabelText("Merging clades");
			DrawableNode root = mergeClades(majorityClades);
			if (cancel) {
				return null;
			}
			
			progressMonitor.setNote("Calculating distances");
			setProgress(99);
			setDistFromTips(root);
			majorityTree = new SquareTree(root);

			removeAnnotation(majorityTree, "height");
			removeAnnotation(majorityTree, "tips");
			
			SunFishFrame.getSunFishFrame().setInfoLabelText("Displaying tree");
			if (translationTable != null) {
				treeReader.setTranslationTable(translationTable);
				treeReader.translate(majorityTree);
			}
				
			displayTree(majorityTree);
			setProgress(100);
			return null;
		}	
		
	}
	
	
	public void writeData(File file, Object data) throws IOException {
		if (!( data instanceof TreeReader)) {
			throw new IllegalArgumentException("Non-treeReader found in TreeLogParser writeData");
		}
		
		TreeReader reader = (TreeReader)data;
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		Tree tree = reader.getNextTree();
		while(tree!=null) {
			String newick = tree.getNewick();
			writer.write(newick + "\n");
			tree = reader.getNextTree();
		}
		
		writer.close();
		
	}
	
	class TreeItem implements Comparable {
		
		public int count = 0;
		public double distToParent = 0;
		//public double distToTip = 0;
		public double height = 0;
		public int cardinality = 0;
		public double M2 = 0;
		public String clade = null;
		
		public int compareTo(Object c) {
			return ((TreeItem)c).cardinality - cardinality;
		}
		
	}




	



}
