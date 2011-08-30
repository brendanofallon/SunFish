package plugins.SGPlugin.analyzer.statTable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;


import element.sequence.SequenceGroup;
import element.sequence.Sequence;

public class StatTableRowHeader extends JTable implements ChangeListener, 
						PropertyChangeListener {

	private JTable main; //The primary statTable
	
	List<String> statNames = new ArrayList<String>();
	RowHeaderRenderer renderer;
	
	public StatTableRowHeader(JTable table, int width)
	{
		main = table;
		main.addPropertyChangeListener( this );

		setFocusable( false );
		setAutoCreateColumnsFromModel( false );
		
		TableColumn column = new TableColumn();
		column.setResizable(true);
		
		addColumn( column );

		renderer = new RowHeaderRenderer();
		renderer.setFont(getFont());
		column.setCellRenderer( renderer );
		getTableHeader().setResizingAllowed(true); 
		
		setFillsViewportHeight(true);
		getColumnModel().getColumn(0).setPreferredWidth(width);

		setBackground(Color.white);
		
		setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		setPreferredScrollableViewportSize(getPreferredSize());
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setShowHorizontalLines(false);
		this.setShowVerticalLines(false);
	}

	public Object getValueAt(int row, int col ) {
		if (row < statNames.size())
			return statNames.get(row);
		else 
			return null;
	}
	
	public void addName(String name) {
		statNames.add(name);
		DefaultTableModel mod = (DefaultTableModel)this.getModel();
		Object[] row = new String[1];
		row[0] = name;
		mod.addRow(row);	
	}
	
	@Override
	public void addNotify()	{
		super.addNotify();
		Component c = getParent();

		//  Keep scrolling of the row table in sync with the main table.
		if (c instanceof JViewport)
		{
			JViewport viewport = (JViewport)c;
			viewport.addChangeListener( this );
		}
	}

	/**
	 *  Delegate method to main table
	 */
	public int getRowCount() {
		return main.getRowCount();
	}

	public int getRowHeight(int row) {
		return main.getRowHeight(row);
	}
	
	public void setFont(Font font) {
		super.setFont(font);
		if (renderer!=null)
			renderer.setFont(font);
	}

	
	public void stateChanged(ChangeEvent e)	{
		//  Keep the scrolling of the row table in sync with main table
		
		JViewport viewport = (JViewport) e.getSource();
		JScrollPane scrollPane = (JScrollPane)viewport.getParent();
		scrollPane.getVerticalScrollBar().setValue(viewport.getViewPosition().y);

		//System.out.println("State has changed");
	}

	public void propertyChange(PropertyChangeEvent e)	{
		
		//Mandatory for avoiding some painting bugs
		if (e.getPropertyName().equals("rowHeight")) {
			this.setRowHeight((Integer)e.getNewValue());
		}

//		if ("model".equals(e.getPropertyName()))
//		{
//			setModel( e.getModel() );
//		}
	}
	
	class RowHeaderRenderer extends DefaultTableCellRenderer {

		int row;
		
		public RowHeaderRenderer() {
			this.setOpaque(false);
			this.setHorizontalAlignment(JLabel.RIGHT);
			this.setHorizontalTextPosition(RIGHT);
			
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
		
			this.row = row;
			if (value == null) {
				System.err.println("Row header value for row: " + row + " col: " + column + " is null!");
				setText("NULL");
			}
			else
				setText(value.toString());
			return this;
		}
		
		public void paintComponent(Graphics g) {
			if (row%2==0)
				g.setColor(getBackground());
			else
				g.setColor(StatTable.stripeColor);
			g.fillRect(0, 0, getWidth(), getHeight());
			
			g.setFont(getFont());
			int strWidth = g.getFontMetrics().stringWidth(getText());
			int height = g.getFontMetrics().getHeight();
			
			g.setColor(Color.black);
			g.drawString(getText(), getWidth()-strWidth-5, getHeight()/2+height/2-2);
			g.setColor(StatTable.textShadowColor);
			g.drawString(getText(), getWidth()-strWidth-5, getHeight()/2+height/2-1);
		}
		
	}

}
