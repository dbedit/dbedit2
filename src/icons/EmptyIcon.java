package icons;

import javax.swing.*;
import java.awt.*;

public class EmptyIcon implements Icon {

    private static final Icon ICON = new EmptyIcon();

    public static Icon getInstance() {
        return ICON;
    }

    private EmptyIcon() {
    }

    public int getIconHeight() {
        return 0;
    }

    public int getIconWidth() {
        return 16;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
    }
}
