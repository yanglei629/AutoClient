package application;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import utils.User32;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Panel extends JPanel {
    public static final Logger logger = LogManager.getLogger(Panel.class);

    private JButton button = new JButton("更新程序");
    private JButton button2 = new JButton("中止程序");
    private JLabel label = new JLabel("version:7.13.1");

    public Panel() {
        //setLayout(new BorderLayout());
        setLayout(new FlowLayout());

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Client.update();
            }
        });
        add(button);
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WinDef.HWND byWare = User32.INSTANCE.FindWindow(null, "EAP application.Client");
                User32.INSTANCE.ShowWindow(byWare, WinUser.SW_RESTORE);
                User32.INSTANCE.SetForegroundWindow(byWare);
                User32.INSTANCE.CloseWindow(byWare);
                User32.INSTANCE.EnumWindows(new WinUser.WNDENUMPROC() {
                    int count;

                    @Override
                    public boolean callback(WinDef.HWND hwnd, Pointer pointer) {
                        byte[] windowText = new byte[512];
                        User32.INSTANCE.GetWindowTextA(hwnd, windowText, 512);
                        String wText = Native.toString(windowText);
                        wText = (wText.isEmpty()) ? "" : "; text: " + wText;
                        System.out.println("Found window " + hwnd + ", total " + ++count + wText);
                        return true;
                    }
                }, null);
            }
        });
        add(button2);

        add(label);
    }
}
