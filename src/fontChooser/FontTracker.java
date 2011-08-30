package fontChooser;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;


class FontTracker implements ActionListener, Serializable {
    FontChooser chooser;
    Font color;

    public FontTracker(FontChooser c) {
        chooser = c;
    }

    public void actionPerformed(ActionEvent e) {
        color = chooser.getFont();
    }

    public Font getFont() {
        return color;
    }
}
