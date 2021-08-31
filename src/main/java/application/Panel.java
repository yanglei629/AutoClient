package application;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import utils.GBC;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Panel extends JPanel {
    public static final Logger logger = LogManager.getLogger(Panel.class);
    private JPanel info_pane = new JPanel();
    private JPanel tool_pane = new JPanel();
    private JPanel notify_pane = new JPanel();
    private JPanel log_pane = new JPanel();

    Border border = new LineBorder(Color.black, 1, true);

    private JLabel version = new JLabel("version:8.2.2");
    private JLabel IPLabel = new JLabel("本机ip:" + Main.IP);
    private JLabel desktopNameLabel = new JLabel("计算机名:" + Main.desktopName);

    private JButton update_eap = new JButton("更新EAP");
    private JButton close_eap = new JButton("关闭EAP");
    private JButton set_ip = new JButton("set ip");
    private JButton exit = new JButton("关闭程序");


    public Panel() {
        setLayout(new GridBagLayout());

        update_eap.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Client.update();
            }
        });
        close_eap.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String res = Client.close();
                if ("Success".equals(res)) {
                    Dialog.showDialog("关闭成功");
                }
            }
        });

        set_ip.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.PORT = 9001;
                Main.restartServer();
            }
        });

        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String pass = JOptionPane.showInputDialog(Main.mainFrame, "请输入密码", "");
                if (pass.equals("12345")) {
                    System.exit(0);
                } else {
                    JOptionPane.showMessageDialog(Main.mainFrame, "密码错误!");
                }
            }
        });

        //info
        info_pane.setPreferredSize(new Dimension(200, 200));
        info_pane.setBorder(border);
        info_pane.add(desktopNameLabel);
        info_pane.add(IPLabel);
        info_pane.add(version);


        //tool
        tool_pane.setPreferredSize(new Dimension(200, 200));
        tool_pane.setBorder(border);
        tool_pane.add(update_eap);
        tool_pane.add(close_eap);
        tool_pane.add(set_ip);
        tool_pane.add(exit);

        //notify
        notify_pane.setPreferredSize(new Dimension(200, 200));
        notify_pane.setBorder(border);

        //log
        log_pane.setPreferredSize(new Dimension(200, 200));
        log_pane.setBorder(border);

        add(info_pane, new GBC(0, 0, 1, 1).setFill(GBC.BOTH).setWeight(1, 1).setInsets(5, 0, 0, 0).setAnchor(GBC.NORTHWEST));
        add(tool_pane, new GBC(1, 0, 1, 1).setFill(GBC.BOTH).setWeight(1, 1).setInsets(5, 0, 0, 0).setAnchor(GBC.NORTHWEST));
        add(notify_pane, new GBC(2, 0, 1, 1).setFill(GBC.BOTH).setWeight(1, 1).setInsets(5, 0, 0, 0).setAnchor(GBC.NORTHWEST));
        add(log_pane, new GBC(0, 1, 3, 1).setFill(GBC.BOTH).setWeight(1, 1).setInsets(5, 0, 0, 0).setAnchor(GBC.NORTHWEST));
    }
}
