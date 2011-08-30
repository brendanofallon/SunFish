package guiWidgets;

import java.awt.Dimension;

import javax.swing.JSeparator;
import javax.swing.UIManager;

/**
 * Unfortunately the GTK look and feel does not properly draw JSeparators in popup menus
 * (see debian bug #613134, http://bugs.debian.org/cgi-bin/bugreport.cgi?bug=613134). This
 * is a temporary workaround that fixes the issue on GTK systems. For now, we should use
 * makeSeparator() which will return a fixed separator if we're using the GTK look and feel
 * @author brendan
 *
 */
public class GTKFixSeparator extends JSeparator {

	/**
	 * Bug causes preferred height to be zero. Here we force it to be at least 4.
	 */
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		d.height = Math.max(4, d.height);
		return d;
	}
	
	public static JSeparator makeSeparator() {
		if (UIManager.getSystemLookAndFeelClassName().contains("GTK")) {
			return new GTKFixSeparator();
		}
		else
			return new JSeparator();
	}
}
