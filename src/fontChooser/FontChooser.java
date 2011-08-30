package fontChooser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import fontChooser.FontChooserDialog.Closer;
import fontChooser.FontChooserDialog.DisposeOnClose;


/**
 * This entire class was lifted directly and with little modification from :
 * http://www.java2s.com/Code/Java/Swing-Components/JFontChooser.htm, for which
 * no licensing information is available.
 * 
 * <code>JFontChooser</code> provides a pane of controls designed to allow
 * a user to manipulate and select a font.
 *
 * This class provides three levels of API:
 * <ol>
 * <li>A static convenience method which shows a modal font-chooser
 * dialog and returns the font selected by the user.
 * <li>A static convenience method for creating a font-chooser dialog
 * where <code>ActionListeners</code> can be specified to be invoked when
 * the user presses one of the dialog buttons.
 * <li>The ability to create instances of <code>JFontChooser</code> panes
 * directly (within any container). <code>PropertyChange</code> listeners
 * can be added to detect when the current "font" property changes.
 * </ol>
 * <p>
 *
 * @author Adrian BER
 */
public class FontChooser extends JComponent {

    /** The list of possible font sizes. */
    private static final Integer[] SIZES =
            {5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 16, 18, 20, 24, 26, 28, 32, 36, 40, 48, 56, 64, 72};

    /** The list of possible fonts. */
    private static final String[] FONTS = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getAvailableFontFamilyNames();


    private FontSelectionModel selectionModel;

    private JList fontList;

    private JList sizeList;

    private JCheckBox boldCheckBox;

    private JCheckBox italicCheckBox;

    private JLabel previewLabel;

    /** The preview text, if null the font name will be the preview text. */
    private String previewText = "The quick brown fox jumped over the lazy dog";

    /** Listener used to update the font of the selection model. */
    private SelectionUpdater selectionUpdater = new SelectionUpdater();

    /** Listener used to update the font in the components. This should be registered
     * with the selection model. */
    private LabelUpdater labelUpdater = new LabelUpdater();

    /** True if the components are being updated and no event should be generated. */
    private boolean updatingComponents = false;

    /** Listener class used to update the font in the components. This should be registered
      * with the selection model. */
    private class LabelUpdater implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            updateComponents();
        }

    }

    /** Listener class used to update the font of the preview label. */
    private class SelectionUpdater implements ChangeListener, ListSelectionListener {

        public void stateChanged(ChangeEvent e) {
        	System.out.println("state changed..");
            if (!updatingComponents) {
            	System.out.println("not updating components, so setting font...");
                setFont(buildFont());
            }
            else
            	System.out.println("were updating comps, so aborting change.");
        }

        public void valueChanged(ListSelectionEvent e) {
        	System.out.println("Value changed..");
            if (!updatingComponents) {
            	System.out.println("state changed..");
                setFont(buildFont());
            }
            else
            	System.out.println("were updating comps, so aborting change.");
        }
    }

    /**
     * Shows a modal font-chooser dialog and blocks until the
     * dialog is hidden.  If the user presses the "OK" button, then
     * this method hides/disposes the dialog and returns the selected color.
     * If the user presses the "Cancel" button or closes the dialog without
     * pressing "OK", then this method hides/disposes the dialog and returns
     * <code>null</code>.
     *
     * @param component    the parent <code>Component</code> for the dialog
     * @param title        the String containing the dialog's title
     * @return the selected font or <code>null</code> if the user opted out
     * @exception HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public Font showDialog(Component component, String title) {

        FontTracker ok = new FontTracker(this);
        JDialog dialog = createDialog(component, title, true, ok, null);
        dialog.addWindowListener(new FontChooserDialog.Closer());
        dialog.addComponentListener(new FontChooserDialog.DisposeOnClose());

        dialog.setVisible(true); // blocks until user brings dialog down...

        return ok.getFont();
    }


    /**
     * Creates and returns a new dialog containing the specified
     * <code>ColorChooser</code> pane along with "OK", "Cancel", and "Reset"
     * buttons. If the "OK" or "Cancel" buttons are pressed, the dialog is
     * automatically hidden (but not disposed).  If the "Reset"
     * button is pressed, the color-chooser's color will be reset to the
     * font which was set the last time <code>show</code> was invoked on the
     * dialog and the dialog will remain showing.
     *
     * @param c              the parent component for the dialog
     * @param title          the title for the dialog
     * @param modal          a boolean. When true, the remainder of the program
     *                       is inactive until the dialog is closed.
     * @param okListener     the ActionListener invoked when "OK" is pressed
     * @param cancelListener the ActionListener invoked when "Cancel" is pressed
     * @return a new dialog containing the font-chooser pane
     * @exception HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public JDialog createDialog(Component c, String title, boolean modal,
        ActionListener okListener, ActionListener cancelListener) {

        return new FontChooserDialog(c, title, modal, this,
                okListener, cancelListener);
    }

    /**
     * Creates a color chooser pane with an initial font which is the same font
     * as the default font for labels.
     */
    public FontChooser() {
        this(new DefaultFontSelectionModel());
    }

    /**
     * Creates a font chooser pane with the specified initial font.
     *
     * @param initialFont the initial font set in the chooser
     */
    public FontChooser(Font initialFont) {
        this(new DefaultFontSelectionModel(initialFont));
    }

    /**
     * Creates a font chooser pane with the specified
     * <code>FontSelectionModel</code>.
     *
     * @param model the font selection model used by this component
     */
    public FontChooser(FontSelectionModel model) {
        selectionModel = model;
        init(model.getSelectedFont());
        selectionModel.addChangeListener(labelUpdater);
    }

    private void init(Font font) {
        setLayout(new GridBagLayout());

        Insets ins = new Insets(2, 2, 2, 2);

        fontList = new JList(FONTS);
        fontList.setVisibleRowCount(10);
        fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(fontList), new GridBagConstraints(0, 0, 1, 1, 2, 2,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                ins, 0, 0));
        fontList.setCellRenderer(new FontListCellRenderer(FONTS));

        sizeList = new JList(SIZES);
        ((JLabel)sizeList.getCellRenderer()).setHorizontalAlignment(JLabel.RIGHT);
        sizeList.setVisibleRowCount(10);
        sizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(sizeList), new GridBagConstraints(1, 0, 1, 1, 1, 2,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                ins, 0, 0));

        boldCheckBox = new JCheckBox("Bold");
        add(boldCheckBox, new GridBagConstraints(0, 1, 2, 1, 1, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE,
                ins, 0, 0));

        italicCheckBox = new JCheckBox("Italic");
        add(italicCheckBox, new GridBagConstraints(0, 2, 2, 1, 1, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE,
                ins, 0, 0));

        previewLabel = new JLabel("");
        previewLabel.setHorizontalAlignment(JLabel.CENTER);
        previewLabel.setVerticalAlignment(JLabel.CENTER);
        add(new JScrollPane(previewLabel), new GridBagConstraints(0, 3, 2, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                ins, 0, 0));

        setFont(font == null ? previewLabel.getFont() : font);

        fontList.addListSelectionListener(selectionUpdater);
        sizeList.addListSelectionListener(selectionUpdater);
        boldCheckBox.addChangeListener(selectionUpdater);
        italicCheckBox.addChangeListener(selectionUpdater);
    }

    private Font buildFont() {
//        Font labelFont = previewLabel.getFont();

        String fontName = (String)fontList.getSelectedValue();
        if (fontName == null) {
            return null;
//            fontName = labelFont.getName();
        }
        Integer sizeInt = (Integer)sizeList.getSelectedValue();
        if (sizeInt == null) {
//            size = labelFont.getSize();
            return null;
        }

        // create the font
//        // first create the font attributes
//        HashMap map = new HashMap();
//        map.put(TextAttribute.BACKGROUND, Color.white);
//        map.put(TextAttribute.FAMILY, fontName);
//        map.put(TextAttribute.FOREGROUND, Color.black);
//        map.put(TextAttribute.SIZE , new Float(size));
//        map.put(TextAttribute.UNDERLINE, italicCheckBox.isSelected() ? TextAttribute.UNDERLINE_LOW_ONE_PIXEL : TextAttribute.UNDERLINE_LOW_TWO_PIXEL);
//        map.put(TextAttribute.STRIKETHROUGH, italicCheckBox.isSelected() ? TextAttribute.STRIKETHROUGH_ON : Boolean.FALSE);
//        map.put(TextAttribute.WEIGHT, boldCheckBox.isSelected() ? TextAttribute.WEIGHT_BOLD : TextAttribute.WEIGHT_REGULAR);
//        map.put(TextAttribute.POSTURE,
//                italicCheckBox.isSelected() ? TextAttribute.POSTURE_OBLIQUE : TextAttribute.POSTURE_REGULAR);
//
//        return new Font(map);

        return new Font(fontName,
                (italicCheckBox.isSelected() ? Font.ITALIC : Font.PLAIN)
                | (boldCheckBox.isSelected() ? Font.BOLD : Font.PLAIN),
                sizeInt);
    }

    /** Updates the font in the preview component according to the selected values. */
    private void updateComponents() {
        updatingComponents = true;

        Font font = getFont();

        fontList.setSelectedValue(font.getName(), true);
        sizeList.setSelectedValue(font.getSize(), true);
        boldCheckBox.setSelected(font.isBold());
        italicCheckBox.setSelected(font.isItalic());

        if (previewText == null) {
            previewLabel.setText(font.getName());
        }
        else 
        	previewLabel.setText(previewText);
        System.out.println("updating components, preview text is : " + previewLabel.getText());

        // set the font and fire a property change
        Font oldValue = previewLabel.getFont();
        previewLabel.setFont(font);
        firePropertyChange("font", oldValue, font);

        updatingComponents = false;
    }

    /**
     * Returns the data model that handles font selections.
     *
     * @return a FontSelectionModel object
     */
    public FontSelectionModel getSelectionModel() {
        return selectionModel;
    }


    /**
     * Set the model containing the selected font.
     *
     * @param newModel   the new FontSelectionModel object
     */
    public void setSelectionModel(FontSelectionModel newModel ) {
        FontSelectionModel oldModel = selectionModel;
        selectionModel = newModel;
        oldModel.removeChangeListener(labelUpdater);
        newModel.addChangeListener(labelUpdater);
        firePropertyChange("selectionModel", oldModel, newModel);
    }

    /**
     * Gets the current font value from the font chooser.
     *
     * @return the current font value of the font chooser
     */
    public Font getFont() {
        return selectionModel.getSelectedFont();
    }

    /**
     * Sets the current font of the font chooser to the specified font.
     * The <code>ColorSelectionModel</code> will fire a <code>ChangeEvent</code>
     * @param font the font to be set in the font chooser
     * @see JComponent#addPropertyChangeListener
     */
    public void setFont(Font font) {
        selectionModel.setSelectedFont(font);
    }

    /** Returns the preview text displayed in the preview component.
     * @return the preview text, if null the font name will be displayed
     */
    public String getPreviewText() {
        return previewText;
    }

    /** Sets the preview text displayed in the preview component.
     * @param previewText the preview text, if null the font name will be displayed
     */
    public void setPreviewText(String previewText) {
        this.previewText = previewText;
        previewLabel.setText("");
        updateComponents();
    }

}

class FontListCellRenderer extends DefaultListCellRenderer {

	String[] fontNames;
	JLabel label;
	
	public FontListCellRenderer(String[] fontNames) {
		this.fontNames = fontNames;
		label = new JLabel();
	}
	
	public Component getListCellRendererComponent(JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
		
		Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		Font newFont = new Font(fontNames[index], Font.PLAIN, 13);
		
		if (newFont == null || ! newFont.canDisplay('e'))
			newFont = new Font("Sans", Font.PLAIN, 13);
			
		comp.setFont( newFont );
		//label.setText(fontNames[index]);
		return comp;
	}
}




/**
 * A generic implementation of <code>{@link FontSelectionModel}</code>.
 *
 * @author Adrian BER
 */
class DefaultFontSelectionModel implements FontSelectionModel {

    /** The default selected font. */
    private static final Font DEFAULT_INITIAL_FONT = new Font("Dialog", Font.PLAIN, 12);
    
    /** The selected font. */
    private Font selectedFont;

    /** The change listeners notified by a change in this model. */
    private EventListenerList listeners = new EventListenerList();

    /**
     * Creates a <code>DefaultFontSelectionModel</code> with the
     * current font set to <code>Dialog, 12</code>.  This is
     * the default constructor.
     */
    public DefaultFontSelectionModel() {
        this(DEFAULT_INITIAL_FONT);
    }

    /**
     * Creates a <code>DefaultFontSelectionModel</code> with the
     * current font set to <code>font</code>, which should be
     * non-<code>null</code>.  Note that setting the font to
     * <code>null</code> is undefined and may have unpredictable
     * results.
     *
     * @param selectedFont the new <code>Font</code>
     */
    public DefaultFontSelectionModel(Font selectedFont) {
        if (selectedFont == null) {
            selectedFont = DEFAULT_INITIAL_FONT;
        }
        this.selectedFont = selectedFont;
    }

    public Font getSelectedFont() {
        return selectedFont;
    }

    public void setSelectedFont(Font selectedFont) {
        if (selectedFont != null) {
            this.selectedFont = selectedFont;
            fireChangeListeners();
        }
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(ChangeListener.class, listener);
    }

    /** Fires the listeners registered with this model. */
    protected void fireChangeListeners() {
        ChangeEvent ev = new ChangeEvent(this);
        Object[] l = listeners.getListeners(ChangeListener.class);
        for (Object listener : l) {
            ((ChangeListener) listener).stateChanged(ev);
        }
    }
}



/**
 * A model that supports selecting a <code>Font</code>.
 *
 * @author Adrian BER
 *
 * @see java.awt.Font
 */
interface FontSelectionModel {
    /**
     * Returns the selected <code>Font</code> which should be
     * non-<code>null</code>.
     *
     * @return  the selected <code>Font</code>
     * @see     #setSelectedFont
     */
    Font getSelectedFont();

    /**
     * Sets the selected font to <code>font</code>.
     * Note that setting the font to <code>null</code>
     * is undefined and may have unpredictable results.
     * This method fires a state changed event if it sets the
     * current font to a new non-<code>null</code> font.
     *
     * @param font the new <code>Font</code>
     * @see   #getSelectedFont
     * @see   #addChangeListener
     */
    void setSelectedFont(Font font);

    /**
     * Adds <code>listener</code> as a listener to changes in the model.
     * @param listener the <code>ChangeListener</code> to be added
     */
    void addChangeListener(ChangeListener listener);

    /**
     * Removes <code>listener</code> as a listener to changes in the model.
     * @param listener the <code>ChangeListener</code> to be removed
     */
    void removeChangeListener(ChangeListener listener);
}
