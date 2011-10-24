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
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import fileTree.FileTreePanel;

/**
 * This panel is the first one displayed in the DisplayPane when the application opens. Not really sure
 * what should be here.... some info about parsers and displays and stuff? 
 * @author brendano
 *
 */
public class WelcomePanel extends JPanel {

	JScrollPane textScrollPane;
	
	JTextArea textArea;
	Color fontColor = new Color(0.15f, 0.15f, 0.15f, 0.8f);
	Font font = new Font("Sans", Font.PLAIN, 11);
	int topInset = 150;
	
	static final ImageIcon sunfishname = getIcon("icons/sunfishname.png");
	
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

		this.setBorder(BorderFactory.createEmptyBorder(topInset, 10, 10, 50));
		this.add(textScrollPane);
		this.add(Box.createHorizontalGlue());
		this.setOpaque(false);
	}
	
	public static ImageIcon getIcon(String url) {
		ImageIcon icon = null;
		try {
			java.net.URL imageURL = WelcomePanel.class.getResource(url);
			icon = new ImageIcon(imageURL);
		}
		catch (Exception ex) {
			SunFishFrame.getSunFishFrame().getLogger().warning("Error loadind icon from resouce : " + ex);
		}
		return icon;
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
	
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		
//		GradientPaint gp = new GradientPaint(1f, 1f, new Color(0.9f, 0.9f, 0.9f), 1f, getHeight(), Color.white);
//		g2d.setPaint(gp);
//		g2d.fillRect(0, 0, getWidth(), getHeight());
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		
		super.paint(g);
		
		GradientPaint gp = new GradientPaint(1f, Math.max(50, getHeight()-250), new Color(0.92f, 0.92f, 0.92f), 1f, Math.max(200, getHeight()-100), new Color(1f, 1f, 1f, 0.0f));
		g2d.setPaint(gp);
		g2d.fillRect(0, 0, getWidth(), Math.max(getHeight()-10, 150));
		
		if (sunfishname != null) {
			g2d.drawImage(sunfishname.getImage(), getWidth()- sunfishname.getIconWidth()-50, 50, null);
		}
		
	}
	
		
	
}
