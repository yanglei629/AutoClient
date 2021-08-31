package application;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import utils.CommonUtil;
import utils.RuntimeUtil;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;


public class Main {
    public static final Logger logger = LogManager.getLogger(Main.class);
    public static String IP;
    public static Integer PORT = 9000;
    public static String desktopName;
    public static String separator;
    public static String programPath = System.getProperty("user.home") + File.separator + "AutoClient";
    public static String tempPath = "temp";

    static final JFrame mainFrame = new JFrame();
    static HttpSnoopServer httpSnoopServer = new HttpSnoopServer();

    static {
        try {
            File directory = new File(Main.programPath);

            if (!directory.exists()) {
                directory.mkdirs();
            }

            //读取配置文件
            separator = System.getProperty("file.separator");
            String configFile = System.getProperty("user.home") + separator + "AutoClientConfig.properties";
            File file = new File(configFile);
            if (!file.exists()) {
                logger.warn("新建配置文件");
                file.createNewFile();
            }
            InputStream input = new FileInputStream(file);
            Properties prop = new Properties();
            prop.load(input);

            if (null != prop.get("IP") && !("").equals(prop.get("IP")))
                Main.IP = prop.getProperty("IP");
            input.close();


            //获取计算机名
            String computerName = RuntimeUtil.getComputerName();
            desktopName = computerName;
            //获取本机ip
            String ip = CommonUtil.getIP();
            logger.info("IP:" + ip);
            IP = ip;
        } catch (Throwable e) {
            if (e instanceof FileNotFoundException) {
                logger.error("file not exists");
            }
            logger.warn(e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        logger.info("程序启动");

        Panel panel = new Panel();
        mainFrame.setTitle("AutoClient");
        mainFrame.setVisible(true);
        //设置关闭操作
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.getContentPane().add(panel, BorderLayout.CENTER);
        mainFrame.setIconImage((new ImageIcon(Main.class.getClassLoader().getResource("logo.png"))).getImage());
        mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(mainFrame,
                        "是否退出?", "Exit",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    //System.exit(0);
                }
            }
        });
        mainFrame.setMinimumSize(new Dimension(1392, 900));
        logger.info("UI初始化完成");

        //启动服务器
        logger.info("启动服务器");
        //new application.DiscardServer(port).run();
        startSever();
    }


    public static void startSever() throws Exception {
        httpSnoopServer.run();
    }

    public static void stopSever() {
        httpSnoopServer.shutdown();
    }

    public static void restartServer() {
        httpSnoopServer.shutdown();
        try {
            httpSnoopServer.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
