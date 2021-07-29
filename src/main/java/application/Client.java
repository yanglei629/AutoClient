package application;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import utils.FileUtil;
import utils.RuntimeUtil;
import utils.User32;

import java.io.File;

public class Client {
    public static final Logger logger = LogManager.getLogger(Client.class);

    //状态
    public static String queryStatus() {
        if (User32.INSTANCE.FindWindow(null, "EAP Client") != null) {
            return "ONLINE";
        }
        return "OFFLINE";
    }

    //关闭
    public static String close() {
        if (User32.INSTANCE.FindWindow(null, "EAP Client") != null) {
            boolean kill = RuntimeUtil.killProcess("\"EAP Client\"");
            if (!kill)
                return "Failed";
        }
        return "Success";
    }

    //启动
    public static String start() {
        boolean start = RuntimeUtil.executeScript("C:\\EAPClient\\start.bat");
        if (!start) {
            return "Failed";
        }
        if (User32.INSTANCE.FindWindow(null, "EAP application.Client") != null) {
            logger.info("Boot Program Success");
            return "Success";
        }
        return "Failed";
    }

    //更新
    public static boolean update() {
        FtpClient ftpClient = null;
        logger.info("Start Update Program");

        try {
            //结束进程
            logger.info("Kill Process");
            if (User32.INSTANCE.FindWindow(null, "EAP application.Client") != null) {
                boolean kill = RuntimeUtil.killProcess("\"EAP Client\"");
                if (!kill) {
                    return false;
                }
            }
            logger.info("Kill Process Success");
            //删除旧程序文件
            logger.info("Clean Old Files");
            String path = "c:\\" + File.separator + "EAPClient" + File.separator + "EAPClient.exe";
            FileUtil.deleteFileOrDirectory(path);
            logger.info("Clean Old Files Success");
            //下载新文件
            logger.info("Download Files");
            ftpClient = new FtpClient("10.65.206.199", 21, "ftp01", "abcd12#$");
            boolean download = ftpClient.download("EAPClient.exe");
            if (!download) {
                return false;
            }
            logger.info("Download Files Success");
            boolean start = RuntimeUtil.executeScript("C:\\EAPClient\\start.bat");
            if (!start) {
                return false;
            }
            if (User32.INSTANCE.FindWindow(null, "EAP application.Client") != null) {
                logger.info("Boot Program Success");
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            ftpClient.close();
        }
        return true;
    }
}
