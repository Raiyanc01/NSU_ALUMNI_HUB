package javaswing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.JTableHeader;

public class ThemeManager {

    private static boolean isDarkMode = false;

    // Dark Theme Palette
    public static final Color DARK_BG = new Color(30, 30, 30);
    public static final Color DARK_CARD_BG = new Color(45, 45, 45);
    public static final Color DARK_TEXT = new Color(220, 220, 220);
    public static final Color DARK_HEADER = new Color(18, 18, 18);
    public static final Color DARK_INPUT_BG = new Color(55, 55, 55);

    // Light Theme Palette
    public static final Color LIGHT_BG = new Color(240, 242, 245);
    public static final Color LIGHT_CARD_BG = Color.WHITE;
    public static final Color LIGHT_TEXT = Color.BLACK;
    public static final Color LIGHT_HEADER = new Color(24, 119, 242);

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    public static void toggleTheme(JFrame frame) {
        isDarkMode = !isDarkMode;
        applyTheme(frame);
    }

    public static void applyTheme(Container container) {
        Color bg = isDarkMode ? DARK_BG : LIGHT_BG;
        Color cardBg = isDarkMode ? DARK_CARD_BG : LIGHT_CARD_BG;
        Color fg = isDarkMode ? DARK_TEXT : LIGHT_TEXT;
        Color inputBg = isDarkMode ? DARK_INPUT_BG : Color.WHITE;

        if (!(container instanceof JFrame)) {
            container.setBackground(cardBg);
        } else {
            container.setBackground(bg);
        }

        for (Component c : container.getComponents()) {
            if (c instanceof JPanel) {
                c.setBackground(cardBg);
                applyTheme((Container) c);
            } else if (c instanceof JLabel) {
                c.setForeground(fg);
            } else if (c instanceof JButton) {
                c.setBackground(isDarkMode ? new Color(60, 60, 60) : new Color(220, 220, 220));
                c.setForeground(fg);
            } else if (c instanceof JTextArea) {
                JTextArea area = (JTextArea) c;
                if (area.isEditable()) {
                    area.setBackground(inputBg);
                } else {
                    area.setBackground(cardBg);
                }
                area.setForeground(fg);
                area.setCaretColor(fg);
            } else if (c instanceof JTextField) {
                JTextField tf = (JTextField) c;
                tf.setBackground(inputBg);
                tf.setForeground(fg);
                tf.setCaretColor(fg);
            } else if (c instanceof JList) {
                JList<?> list = (JList<?>) c;
                list.setBackground(cardBg);
                list.setForeground(fg);
            } else if (c instanceof JTable) {
                JTable table = (JTable) c;
                table.setBackground(cardBg);
                table.setForeground(fg);
                table.setGridColor(isDarkMode ? Color.DARK_GRAY : Color.LIGHT_GRAY);
                JTableHeader header = table.getTableHeader();
                if (header != null) {
                    header.setBackground(isDarkMode ? DARK_HEADER : LIGHT_HEADER);
                    header.setForeground(Color.WHITE);
                }
            } else if (c instanceof JTabbedPane) {
                JTabbedPane tabs = (JTabbedPane) c;
                tabs.setBackground(cardBg);
                tabs.setForeground(fg);
                for (int i = 0; i < tabs.getTabCount(); i++) {
                    Component tabComp = tabs.getComponentAt(i);
                    if (tabComp instanceof Container) {
                        applyTheme((Container) tabComp);
                    }
                }
            } else if (c instanceof JScrollPane) {
                JScrollPane sp = (JScrollPane) c;
                sp.setBackground(cardBg);
                if (sp.getViewport() != null) {
                    sp.getViewport().setBackground(cardBg);
                    applyTheme(sp.getViewport());
                }
            } else if (c instanceof Container) {
                applyTheme((Container) c);
            }
        }
        container.revalidate();
        container.repaint();
    }
}