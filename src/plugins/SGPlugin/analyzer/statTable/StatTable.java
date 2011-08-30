package plugins.SGPlugin.analyzer.statTable;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import plugins.SGPlugin.sgStatistics.*;

import sun.swing.table.DefaultTableCellHeaderRenderer;
import topLevelGUI.SunFishFrame;

import element.sequence.*;
import guiWidgets.StringUtilities;

/**
 * A table that houses seq stats as rows and sequencegroups or partitions as columns. StatTables are currently
 * only used in the SequenceGroupSummary to display various sg. statistics associated with one or more sequence groups.
 * @author brendan
 *
 */
public class StatTable extends JPanel {
	
	JScrollPane scrollPane;
	JTable table;
	StatTableModel tableModel;
	
	List<SequenceGroup> currentSGs = new ArrayList<SequenceGroup>(10);
	
	List<StatTableColumn> statColumns = new ArrayList<StatTableColumn>();
	List<SGCalculator> currentStats = new ArrayList<SGCalculator>(10);

	static Font rowHeaderFont = new Font("Sans", Font.PLAIN, 11);
	static Font colHeaderFont = new Font("Sans", Font.PLAIN, 11);
	static Font valFont = new Font("Sans", Font.PLAIN, 11);
	
	static Color textShadowColor = new Color(0.9f, 0.9f, 0.9f, 0.3f);
	static Color stripeColor = new Color(56, 56, 56, 20);
	
	StatTableRowHeader rowHeader;
	HeaderRenderer headerRenderer = new HeaderRenderer();
	CellRenderer cellRenderer = new CellRenderer();
			
	JPopupMenu popup;
	Point popupPosition;
	
	public StatTable() {
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBackground(Color.white);
		
		table = new JTable();
		table.setShowHorizontalLines(false);
		table.setShowVerticalLines(false);
		tableModel = new StatTableModel();
		table.setModel(tableModel);
		table.setOpaque(false);
		table.setFont(valFont);
		
		table.setDefaultRenderer(Object.class, cellRenderer);
		table.setDefaultRenderer(String.class, cellRenderer);
		table.setRowHeight(20);
		
		scrollPane = new JScrollPane(table);
		scrollPane.setBackground(Color.white);
		scrollPane.getViewport().setBackground(Color.white);
		
		JPanel leftPanel = new JPanel();
		leftPanel.setOpaque(false);
		leftPanel.setMaximumSize(new Dimension(10, Integer.MAX_VALUE));
		
		this.add(leftPanel);
		this.add(scrollPane);
		scrollPane.setPreferredSize(new Dimension(200, 200));
		this.add(Box.createGlue());

		rowHeader = new StatTableRowHeader(table, 150);
		
		rowHeader.setShowHorizontalLines(false);
		rowHeader.setFont(rowHeaderFont);
		
		scrollPane.setRowHeaderView(rowHeader);
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		table.setBorder(BorderFactory.createEmptyBorder());
		
		//Construct popup
		popup = new JPopupMenu();
		popup.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY) );
		popup.setBackground(new Color(100,100,100) );
		
		JMenuItem popupRemove = new JMenuItem("Remove column");
		popupRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	removeColumnFromPopup();
            }
        });
		popup.add(popupRemove);
		
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.isPopupTrigger() || (SunFishFrame.getSunFishFrame().onAMac() && evt.isControlDown()) || (evt.getButton()==MouseEvent.BUTTON3)) {
					popupPosition = evt.getPoint();
					popup.show(table, evt.getX(), evt.getY());
				}
			}
			
		});
	}
	
	/**
	 * Returns the column index underneath the given point 
	 * @param p
	 * @return
	 */
	private int getColumnForPoint(Point p) {
		//System.out.println("Point x: " + p.x + " column 0 width: " + table.getColumn( sgColumnNames.get(0)).getWidth());
		int x = p.x;
		if (x < table.getColumn( statColumns.get(0).getName()).getWidth())
			return 0;
		
		for(int i=1; i<table.getColumnCount(); i++) {
			//System.out.println("Column " + i + " width: " + table.getColumn( sgColumnNames.get(i)).getWidth());
			x -= table.getColumn( statColumns.get(i-1).getName()).getWidth();
			if (x < table.getColumn(statColumns.get(i).getName()).getWidth())
				return i;
			
		}
		System.out.println("Could not find column for point: " + p.x);
		return -1;
	}
	
	
	/**
	 * Removes a column from the table based on the position of the popup. 
	 */
	protected void removeColumnFromPopup() {
		int column = getColumnForPoint(popupPosition);
		if (column>-1) {
			//System.out.println("Point: " + popupPosition.getX() + " column index: " + column + "  name: " + sgColumnNames.get(column));
			removeColumn(statColumns.get(column).getName());
		}
	}

	private void emitAll() {
		System.out.println("Table columns " + table.getColumnCount() + " stat columns: " + statColumns.size() );
		for(int i=0; i<statColumns.size(); i++) {
			System.out.println(i + " : " + statColumns.get(i).getName() );
		}
		System.out.println();
	}

	/**
	 * Return the index of the StatColumn with the given name
	 * @param sg
	 * @return
	 */
	private int columnForName(String columnName) {
		for(int i=0; i<statColumns.size(); i++) {
			if (statColumns.get(i).getName().equals(columnName)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Return the index of the StatColumn that refers to the given SequenceGroup
	 * @param sg
	 * @return
	 */
//	private int columnForSG(SequenceGroup sg) {
//		for(int i=0; i<statColumns.size(); i++) {
//			if (statColumns.get(i).getSG()==sg) {
//				return i;
//			}
//		}
//		return -1;
//	}
	
	/**
	 * Remove the column with the given name.
	 * @param columnName
	 */
	protected void removeColumn(String columnName) {
		int index = columnForName(columnName);
		if (index == -1) {
			SunFishFrame.getSunFishFrame().getLogger().info("Could not remove StatTableColumn with name: " + columnName + ", no column with that name");
			return;
		}
		
		statColumns.get(index).clearListeners();
		statColumns.remove(index);		
		layoutColumns();
		
//		System.out.println("State after removal, column count: " + tableModel.getColumnCount() );
//		emitAll();
		table.repaint();
	}
	
//	public List<SequenceGroup> getSequenceGroups() {
//		return currentSGs;
//	}
	
	
	
	/**
	 * Add a new sequence group to be displayed as a column of this table. The header of the column is 
	 * set to whatever 'name' is, and the width of the column is based on the width of the string
	 * @param sg
	 * @param name
	 */
	public void addSequenceGroup(SequenceGroup sg, String name) {
		currentSGs.add(sg);
		StatTableColumn newCol = new StatTableColumn(this, sg, name, currentStats);
		statColumns.add(newCol);
		//System.out.println(" \n Adding group : " + name + " column count before add : " + tableModel.getColumnCount());
		
		layoutColumns();
		//emitAll();
	}
	
	/**
	 * Add a new stat column with the same sequence group as col, but tracking the given partition index
	 * @param col
	 * @param newPartitionNumber
	 */
	public void handlePartitionAdd(StatTableColumn col, int newPartitionNumber) {
		//First see if there's already a column with the same sg and partition number
		for(StatTableColumn tCol : statColumns) {
			if (tCol.getSG()==col.getSG() && tCol.getPartitionIndex()==newPartitionNumber) {
				return;
			}
		}
		
		//There's not a column tracking the new partition, so add one
		SequenceGroup sg = col.getSG();
		StatTableColumn newCol = new StatTableColumn(this, sg, col.getName() + " Par. " + partitionLetters[newPartitionNumber], currentStats, newPartitionNumber);
		statColumns.add(newCol);				

		layoutColumns();
	}

	public void handlePartitionRemoved(StatTableColumn col) {
		//See if there are any columns whose partition index does not match a partition in
		//the sequence group
		SequenceGroup sg = col.getSG();
		
		List<StatTableColumn> colsToRemove = new ArrayList<StatTableColumn>();
		for(StatTableColumn tCol : statColumns) {
			if (tCol.getSG()==sg) {
				if (sg.getPartitionSiteCount(tCol.getPartitionIndex() )==0) {
					colsToRemove.add(tCol);
				}
			}
			
		}
		
		for(int i=0; i<colsToRemove.size(); i++)
			statColumns.remove(colsToRemove.get(i));
						

		layoutColumns();
	}
	
	
	/**
	 * This is called after any change to the stat columns. We refresh the model we new data to reflect any
	 * changes that may have occurred. 
	 */
	private void layoutColumns() {
		int totWidth = rowHeader.getWidth();
		tableModel = new StatTableModel();
		//System.out.println("Regenerating all columns...");
		//Add a new column onto the end of each row
		for(int j=0; j<statColumns.size(); j++) {
			Object[] values = statColumns.get(j).getValues();
			tableModel.addColumn(statColumns.get(j).getName(), values);
			System.out.println("Added column " + j + " with name: " + statColumns.get(j).getName());
		}
		
		table.setModel( tableModel);
		
		
		FontMetrics fm = table.getFontMetrics(colHeaderFont);
		for(int i=0; i<tableModel.getColumnCount(); i++) {
			String colName = statColumns.get(i).getName();
			table.getColumnModel().getColumn( i ).setHeaderRenderer(new HeaderRenderer());
			table.getColumnModel().getColumn( i ).setCellRenderer(new CellRenderer());			
			int colWidth = fm.stringWidth( colName )+10;
			colWidth = Math.min(200, colWidth);
			table.getColumnModel().getColumn( i ).setMinWidth(colWidth);
			table.getColumnModel().getColumn( i ).setPreferredWidth(colWidth);
			table.getColumnModel().getColumn( i ).setMaxWidth(Integer.MAX_VALUE);
			table.getColumnModel().getColumn( i ).setResizable(true);
			totWidth += colWidth;
			//System.out.println("Setting header width for col " + colName + " to : " + colWidth);
		}
		
		
		scrollPane.setPreferredSize(new Dimension(totWidth, scrollPane.getHeight() ));
		scrollPane.getViewport().setPreferredSize(new Dimension(totWidth, scrollPane.getHeight() ));
		
		table.doLayout();
		this.revalidate();
		if (table.getWidth()>scrollPane.getWidth())
			scrollPane.getHorizontalScrollBar().setVisible(true);
		else
			scrollPane.getHorizontalScrollBar().setVisible(false);
		
		table.repaint();
		System.out.println("Added " + statColumns.size() + " groups, table column count: " + table.getColumnModel().getColumnCount());
	}
	
	/**
	 * Adds a new statistic as a row
	 * @param statwww
	 */
	public void addStatistic(SGCalculator stat) {
		currentStats.add(stat);
		for(StatTableColumn statCol : statColumns)
			statCol.addStatistic(stat);
		
		rowHeader.addName(stat.getName());
		
		layoutColumns();
	}
	
	/**
	 * Returns a list of the SG calculators currently used by this stat table
	 * @return
	 */
	public List<SGCalculator> getCalculators() {
		return currentStats;
	}
	
	
	/**
	 * Removes this sequence group from the table. At least one column is removed from the
	 * table when this happens.
	 * @param sg
	 */
	public void removeSequenceGroup(SequenceGroup sg) {
		int index = -1;
		for(int i=0; i<statColumns.size(); i++) {
			if (statColumns.get(i).getSG() == sg) {
				index = i;
				break;
			}
		}
		
		statColumns.get(index).clearListeners();
		statColumns.remove(index);
		layoutColumns();
		repaint();
	}
	
	/**
	 * Removes this statistic from the table. One row is removed from the table when
	 * this happens. 
	 * @param stat
	 */
//	public void removeStatistic(SeqStatistic stat) {
//		
//	}
	
	/**
	 * Remove all listeners from all the sequence groups
	 */
	public void clearAllListeners() {
		for(StatTableColumn col : statColumns)
			col.clearListeners();
	}
	
	
	

	/**
	 * A custom.. or not, table model. We may customize this someday.
	 * @author brendan
	 *
	 */
	class StatTableModel extends DefaultTableModel {
		
		public boolean isCellEditable(int row, int col) {
			return false;
		}
		
	}
	
	/**
	 * Handles the drawing of the column headers
	 * @author brendan
	 *
	 */
	class HeaderRenderer extends JLabel implements TableCellRenderer {

		int drawCloseBoxForColumn = -1;
		int column = 0;
		
		public HeaderRenderer() {
			setBackground(Color.white);
			this.setHorizontalTextPosition(JLabel.CENTER);
			setFont(colHeaderFont);
		}
		
		public Component getTableCellRendererComponent(JTable table,
													Object value, 
													boolean isSelected, 
													boolean hasFocus, 
													int row,
													int column) {

			if (statColumns.size()<2)
				setText("");
			else
				setText(value.toString());
			this.column = column;
			return this;
		}
		
		public void paintComponent(Graphics g) {
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                RenderingHints.VALUE_ANTIALIAS_ON);
			
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());

			g.setColor(Color.black);
			g.setFont(colHeaderFont);

			int textWidth = g.getFontMetrics().stringWidth(getText());
			g.drawString(getText(), 3, getFont().getSize());
			
			if (drawCloseBoxForColumn == column) {
				Graphics2D g2d = (Graphics2D)g;
				g2d.setColor(Color.RED);
				g2d.setStroke(new BasicStroke(2.2f));
				int xPos = 3+g2d.getFontMetrics().stringWidth(getText());
				if (xPos > getWidth()-10)
					xPos = getWidth()-10;
				int rectSize = 8;
				g2d.drawLine(xPos, 1, xPos+rectSize, rectSize);
				g2d.drawLine(xPos, rectSize, xPos+rectSize, 1);
			}
		}
		
	}

	/**
	 * Handles the drawing of individual values in the table
	 * @author brendan
	 *
	 */
	class CellRenderer extends DefaultTableCellRenderer {
		
		int row;
		
		public CellRenderer() {
			setBackground(Color.white);
			this.setHorizontalTextPosition(RIGHT);
			setFont(valFont);
		}
		
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			this.row = row;
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
		
		public void paintComponent(Graphics g) {
			if (row%2==0)
				g.setColor(getBackground());
			else
				g.setColor(stripeColor);
			
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                RenderingHints.VALUE_ANTIALIAS_ON);
			
			g.fillRect(0, 0, getWidth(), getHeight());
			
			g.setColor(Color.black);
			g.setFont(getFont());
			int height =  g.getFontMetrics().getHeight();
			int width = g.getFontMetrics().stringWidth(getText());
			
			g.drawString(getText(), Math.max(2, 40-width/2), getHeight()/2+height/2-2);
			g.setColor(textShadowColor);
			g.drawString(getText(), Math.max(2, 40-width/2)+1, getHeight()/2+height/2-1);
		}
		
	}
	
	
	private char[] partitionLetters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'W', 'X', 'Y', 'Z'};
}
