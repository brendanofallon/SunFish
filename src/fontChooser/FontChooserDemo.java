package fontChooser;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;


import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * This entire class was lifted directly and with little modification from :
 * http://www.java2s.com/Code/Java/Swing-Components/JFontChooser.htm, for which
 * no licensing information is available.
 * @author Adrian BER (beradrian@yahoo.com)
 */
public class FontChooserDemo extends JPanel {

    private static final Insets INSETS = new Insets(5, 5, 5, 5);

    private FontChooser fontChooser;
    private JCheckBox defaultPreviewCheckBox;
    private JTextField previewTextField;
    private JLabel previewLabel;
    private JTextArea codeTextArea;

    public FontChooserDemo() {
        init();
    }

    private void init() {
        setLayout(new GridBagLayout());

        defaultPreviewCheckBox = new JCheckBox("Use font name as the preview text");
        defaultPreviewCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                boolean selected = defaultPreviewCheckBox.isSelected();
                fontChooser.setPreviewText(selected ? null : previewTextField.getText());
                previewLabel.setEnabled(!selected);
                previewTextField.setEnabled(!selected);
                updateCode();
            }
        });
        add(defaultPreviewCheckBox, new GridBagConstraints(0, 0, 2, 1, 0, 0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, INSETS, 0, 0));

        previewLabel = new JLabel("Preview text:");
        add(previewLabel, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST,
                GridBagConstraints.NONE, INSETS, 0, 0));

        previewTextField = new JTextField();
        previewTextField.getDocument().addDocumentListener(new DocumentListener() {
            private void changePreviewText() {
                fontChooser.setPreviewText(previewTextField.getText());
                updateCode();
            }

            public void insertUpdate(DocumentEvent e) {
                changePreviewText();
            }

            public void removeUpdate(DocumentEvent e) {
                changePreviewText();
            }

            public void changedUpdate(DocumentEvent e) {
                changePreviewText();
            }
        });
        add(previewTextField, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, INSETS, 0, 0));

        JButton testButton = new JButton("Test");
        testButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Font font = fontChooser.showDialog(FontChooserDemo.this, "Choose a font");
                JOptionPane.showMessageDialog(FontChooserDemo.this, font == null ? "You canceled the dialog."
                        : "You have selected " + font.getName() + ", " + font.getSize()
                        + (font.isBold() ? ", Bold" : "") + (font.isItalic() ? ", Italic" : ""));
            }
        });
        add(testButton, new GridBagConstraints(0, 2, 2, 1, 1, 0, GridBagConstraints.NORTHEAST,
                GridBagConstraints.NONE, INSETS, 0, 0));

        codeTextArea = new JTextArea(5, 30);
        codeTextArea.setOpaque(false);
        codeTextArea.setEditable(false);
        codeTextArea.setBorder(BorderFactory.createTitledBorder("Code"));
        add(codeTextArea, new GridBagConstraints(0, 3, 2, 1, 1, 1, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, INSETS, 0, 0));

        setFontChooser(new FontChooser());
    }

    private void setFontChooser(FontChooser fontChooser) {
        this.fontChooser = fontChooser;
        String previewText = fontChooser.getPreviewText();
        defaultPreviewCheckBox.setSelected(previewText == null);
        previewTextField.setText(previewText);
        updateCode();
    }

    private void updateCode() {
        codeTextArea.setText("JFontChooser fontChooser = new JFontChooser();\n"
                + (defaultPreviewCheckBox.isSelected() ? "" : "fontChooser.setPreviewText(\""
                    + previewTextField.getText() + "\");\n")
                + "Font font = fontChooser.showDialog(invokerComponent, \"Choose a font\");\n"
                + "System.out.println(font == null ? \"You have canceled the dialog.\" : \"You have selected \" + font);");
    }

    public void updateUI() {
        super.updateUI();
        if (fontChooser != null)
            SwingUtilities.updateComponentTreeUI(fontChooser);
    }
}
