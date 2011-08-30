package topLevelGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

/**
 * A small, mostly-non interactive frame with info that shows all registered parsers
 * @author brendan
 *
 */
public class ParserListFrame extends JFrame {

	public ParserListFrame(ParserRegistry parserReg) {
		super("Available file parsers");
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setPreferredSize(new Dimension(600, 250));
		
		
				
		String[] columnNames = new String[]{"Name", "Description", "Version"};
		
		List<FileParser> parsers = parserReg.getAllParsers();
		String[][] data = new String[parsers.size()][3];
		for(int i=0; i<parsers.size(); i++) {
			data[i][0] = parsers.get(i).getName();
			data[i][1] = parsers.get(i).getDescription();
			data[i][2] = String.valueOf(parsers.get(i).getVersionNumber());
		}
		
		JTable table = new JTable(data, columnNames);
		table.setGridColor(Color.LIGHT_GRAY);
		table.setBackground(Color.white);
		table.setShowVerticalLines(false);
		table.getColumnModel().getColumn(0).setPreferredWidth(175);
		table.getColumnModel().getColumn(0).setMaxWidth(175);
		table.getColumnModel().getColumn(2).setMaxWidth(50);
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(true);
		//table.getColumnModel().getColumn(1).setPreferredWidth(250);
		table.doLayout();
		JScrollPane scrollPane = new JScrollPane(table);
		
		mainPanel.add(scrollPane);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		
		JButton doneButton = new JButton("Done");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeFrame();
			}
		});
		
		JLabel lab = new JLabel("" + parsers.size() + " registered parsers");
		bottomPanel.add(lab);
		bottomPanel.add(Box.createHorizontalGlue());
		bottomPanel.add(doneButton);
		
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		
		
		this.getContentPane().add(mainPanel);
		this.setPreferredSize(new Dimension(600, 250));
		pack();
		setLocationRelativeTo(null);
	}
	
	protected void closeFrame() {
		this.setVisible(false);
		this.dispose();
	}
}
