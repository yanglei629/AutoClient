import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FTPClientUtil {
    private static final Logger logger = LogManager.getLogger(FTPClientUtil.class);
    private static final String HOSTNAME = "10.65.1.168";
    private static final Integer PORT = 21;
    private static final String USERNAME = "ftp01";
    private static final String PASSWORD = "abcd12#$";
    private static final String Local_DIRECTORY = "C:\\EAP Client";
    private static final String Remote_DIRECTORY = "/sevenswords/EAP Client";

    private static FTPClient ftpClient;

    static {
        ftpClient = new FTPClient();
        ftpClient.setControlEncoding("utf-8");
        try {
            ftpClient.connect(HOSTNAME, PORT);
            //ftpClient.connect(HOSTNAME);
            ftpClient.login(USERNAME, PASSWORD);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            //ftpClient.enterLocalPassiveMode();
            //ftpClient.setControlEncoding("GBK");
            int replyCode = ftpClient.getReplyCode();
            logger.debug("replyCode:" + replyCode);
            if (FTPReply.isPositiveCompletion(replyCode) == true) {
                // 登陆成功
                logger.info("FTP connect to " + HOSTNAME + ":" + PORT + " succeed!");
            } else {
                logger.warn("FTP connect to " + HOSTNAME + ":" + PORT + " failed!");
                ftpClient = null;
            }
            ftpClient.changeWorkingDirectory(Remote_DIRECTORY);
            File Local_DIRECTORY = new File(FTPClientUtil.Local_DIRECTORY);
            if (!Local_DIRECTORY.exists()) {
                Local_DIRECTORY.mkdirs();
            }
        } catch (IOException e) {
            logger.warn(e.getMessage());
            //e.printStackTrace();
        } catch (Exception ex) {
            logger.warn(ex.getMessage());
        }
    }

    private FTPClientUtil() {

    }

    public void downloadOneDayFiles(String dayString) {
        if (this.ftpClient == null) {
            System.out.println("[ERROR] because ftp client is NULL!");
            return;
        }
        try {
            ftpClient.changeWorkingDirectory("");
            // 进入系统文件夹
            for (FTPFile sysFile : this.ftpClient.listDirectories()) {
                String sysName = sysFile.getName();
//				System.out.println("sysName = " + sysName);
                boolean changeSysFlag = ftpClient.changeWorkingDirectory(sysName);
                if (changeSysFlag == true) {
                    // 进入类型文件夹 ADD或者ALL
                    for (FTPFile typeFile : this.ftpClient.listDirectories()) {
                        String typeName = typeFile.getName();
                        boolean changeTypeFlag = ftpClient.changeWorkingDirectory(typeName);
                        if (changeTypeFlag == true) {
                            // 进入当前目录，该目录下应该只有一个文件
                            boolean changeDayFlag = ftpClient.changeWorkingDirectory(dayString);
                            if (changeDayFlag == true) {
                                for (FTPFile needFile : ftpClient.listFiles()) {
                                    System.out.println(needFile.getName());
                                    //download(dayString, needFile.getName());
                                }
                                ftpClient.changeToParentDirectory();
                            }
                            ftpClient.changeToParentDirectory();
                        }
                    }
                    ftpClient.changeToParentDirectory();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean download(String filename) {
        if (ftpClient == null) {
            return false;
        }
        try {
            OutputStream os;
            File localFile = new File(Local_DIRECTORY + File.separator + File.separator + filename);
            os = new FileOutputStream(localFile);
            ftpClient.retrieveFile(filename, os);
            os.close();
            logger.info("download file " + filename + " finished!");
            return true;
        } catch (Exception e) {
            logger.warn(e.getMessage());
            logger.warn("download file " + filename + " failed!");
            //e.printStackTrace();
            return false;
        }
    }
}
