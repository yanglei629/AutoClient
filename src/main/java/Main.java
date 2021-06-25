import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;


public class Main {
    public static final Logger logger = LogManager.getLogger(Panel.class);
    public static String IP;

    static {
        try {
            //读取配置文件
            String separator = System.getProperty("file.separator");
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
        } catch (FileNotFoundException e) {
            logger.warn(e.getMessage());
            e.printStackTrace();
        } catch (IOException exception) {
            logger.warn(exception.getMessage());
            exception.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {

        logger.info("程序启动");
        final JFrame mainFrame = new JFrame();
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
                    System.exit(0);
                }
            }
        });
        mainFrame.setMinimumSize(new Dimension(1392, 900));
        logger.info("UI初始化完成");

        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        //启动服务器
        logger.info("启动服务器");
        //new DiscardServer(port).run();
        new HttpSnoopServer().run();
    }
}
