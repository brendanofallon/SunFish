package fontChooser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;


/*
 * This entire class was lifted directly and with little modification from :
 * http://www.java2s.com/Code/Java/Swing-Components/JFontChooser.htm, for which
 * no licensing information is available.
 * 
 * Class which builds a font chooser dialog consisting of
 * a JFontChooser with "Ok", "Cancel", and "Reset" buttons.
 *
 * Note: This needs to be fixed to deal with localization!
 * Additional note: Screw localization
 */
public class FontChooserDialog extends JDialog {
    
	private static final long serialVersionUID = 1L;
	private Font initialFont;
    private FontChooser chooserPane;

    public FontChooserDialog(Component c, String title, boolean modal,
              FontChooser chooserPane,
              ActionListener okListener, ActionListener cancelListener) {
        super(JOptionPane.getFrameForComponent(c), title, modal);
        //setResizable(false);
        setPreferredSize(new Dimension(350, 400));

        String okString = UIManager.getString("ColorChooser.okText");
        String cancelString = UIManager.getString("ColorChooser.cancelText");
        String resetString = UIManager.getString("ColorChooser.resetText");

        /*
         * Create Lower button panel
         */
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton(okString);
        getRootPane().setDefaultButton(okButton);
        okButton.setActionCommand("OK");
        if (okListener != null) {
            okButton.addActionListener(okListener);
        }
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        buttonPane.add(okButton);

        JButton cancelButton = new JButton(cancelString);

        // The following few lines are used to register esc to close the dialog
        Action cancelKeyAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // todo make it in 1.3
//                ActionListener[] listeners
//                        = ((AbstractButton) e.getSource()).getActionListeners();
//                for (int i = 0; i < listeners.length; i++) {
//                    listeners[i].actionPerformed(e);
//                }
            }
        };
        KeyStroke cancelKeyStroke = KeyStroke.getKeyStroke((char) KeyEvent.VK_ESCAPE);
        InputMap inputMap = cancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = cancelButton.getActionMap();
        if (inputMap != null && actionMap != null) {
            inputMap.put(cancelKeyStroke, "cancel");
            actionMap.put("cancel", cancelKeyAction);
        }
        // end esc handling

        cancelButton.setActionCommand("cancel");
        if (cancelListener != null) {
            cancelButton.addActionListener(cancelListener);
        }
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        buttonPane.add(cancelButton);

        JButton resetButton = new JButton(resetString);
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reset();
            }
        });
        int mnemonic = UIManager.getInt("ColorChooser.resetMnemonic");
        if (mnemonic != -1) {
            resetButton.setMnemonic(mnemonic);
        }
        buttonPane.add(resetButton);


        // initialiase the content pane
        this.chooserPane = chooserPane;

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(chooserPane, BorderLayout.CENTER);

        contentPane.add(buttonPane, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(c);
    }

    public void setVisible(boolean visible) {
        if (visible)
            initialFont = chooserPane.getFont();
        super.setVisible(visible);
    }

    public void reset() {
        chooserPane.setFont(initialFont);
    }

    static class Closer extends WindowAdapter implements Serializable {
        public void windowClosing(WindowEvent e) {
            Window w = e.getWindow();
            w.setVisible(false);
        }
    }

    static class DisposeOnClose extends ComponentAdapter implements Serializable {
        public void componentHidden(ComponentEvent e) {
            Window w = (Window) e.getComponent();
            w.dispose();
        }
    }

}

