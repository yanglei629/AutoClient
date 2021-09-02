package application;

import com.alibaba.fastjson.JSON;
import netty_server.HttpSnoopServer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import utils.*;

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
    public static String upload = "upload";

    static final JFrame mainFrame = new JFrame();
    static HttpSnoopServer httpSnoopServer = new HttpSnoopServer();

    static {
        try {
            //创建程序所需目录
            File directory = new File(Main.programPath);
            File upload_dir = new File(Main.upload);

            if (!directory.exists()) {
                directory.mkdirs();
            }
            if (!upload_dir.exists()) {
                upload_dir.mkdirs();
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
            logger.info("ProcessId:" + Kernel32.INSTANCE.GetCurrentProcessId());
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
                    String pass = JOptionPane.showInputDialog(Main.mainFrame, "请输入密码", "");
                    if (pass.equals("12345")) {
                        System.exit(0);
                    } else {
                        JOptionPane.showMessageDialog(Main.mainFrame, "密码错误!");
                    }
                }
            }
        });
        mainFrame.setMinimumSize(new Dimension(1392, 900));
        logger.info("UI初始化完成");

        //启动服务器
        logger.info("启动服务器");
        Client client = new Client();
        client.name = desktopName;
        client.IP = IP;
        new Thread(new HeartBeat(client)).start();
        startSever();
    }


    public static void startSever() throws Exception {
        httpSnoopServer.run();
    }

    public static void stopServer() {
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
