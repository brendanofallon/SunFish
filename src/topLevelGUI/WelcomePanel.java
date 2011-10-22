package topLevelGUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

/**
 * This panel is the first one displayed in the DisplayPane when the application opens. Not really sure
 * what should be here.... some info about parsers and displays and stuff? 
 * @author brendano
 *
 */
public class WelcomePanel extends JPanel {

	JScrollPane textScrollPane;
	
	JTextArea textArea;
	Color fontColor = new Color(0.3f, 0.3f, 0.3f, 0.7f);
	Font font = new Font("Sans", Font.PLAIN, 12);
	
	public WelcomePanel() {
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setOpaque(false);
		textArea.setFont(font);
		textArea.setForeground(fontColor);
		

		
		textScrollPane = new JScrollPane(textArea);
		textScrollPane.setPreferredSize(new Dimension(500, 200));
		textScrollPane.setMaximumSize(new Dimension(500, 400));
		textScrollPane.setOpaque(false);
		textScrollPane.getViewport().setOpaque(false);
		textScrollPane.setViewportBorder(null);
		textScrollPane.setBorder(null);
		textScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		textScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		this.setBorder(BorderFactory.createEmptyBorder(200, 10, 10, 50));
		this.add(textScrollPane);
		this.add(Box.createHorizontalGlue());
		this.setOpaque(false);
	}
	
	/**
	 * Append a new line to the text area
	 * @param line
	 */
	public void appendLine(String line) {
		textArea.append(line + "\n");
		textScrollPane.scrollRectToVisible(new Rectangle(10, textArea.getHeight()-2, 10, 10));
	}
	
	public void clearText() {
		textArea.setText("");
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		
		GradientPaint gp = new GradientPaint(1f, 1f, new Color(0.9f, 0.9f, 0.9f), 1f, getHeight(), Color.white);
		g2d.setPaint(gp);
		g2d.fillRect(0, 0, getWidth(), getHeight());
	}
	
		
	
}
