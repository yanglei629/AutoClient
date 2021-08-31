package application;

import javax.swing.*;
import java.awt.*;

public class Dialog {
    private static Frame panel = Main.mainFrame;

    public static void showDialog(String message) {
        JOptionPane.showMessageDialog(panel, message);
    }
}
