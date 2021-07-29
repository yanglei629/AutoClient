package application;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FtpClient {
    private static final Logger logger = LogManager.getLogger(FtpClient.class);
    FTPClient ftpClient;
    private static String Local_Directory = "C:\\EAPClient";
    private static String Remote_Directory = "/EAPClient";

    public FtpClient(String host, int port, String username, String password) {
        try {
            ftpClient = new FTPClient();
            ftpClient.connect(host, port);
            ftpClient.login(username, password);
            ftpClient.setConnectTimeout(5000);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setControlEncoding("UTF-8");
            //ftpClient.enterLocalPassiveMode();
            int replyCode = ftpClient.getReplyCode();
            logger.debug("replyCode:" + replyCode);
            if (FTPReply.isPositiveCompletion(replyCode) == true) {
                // 登陆成功
                logger.info("FTP connect to " + host + ":" + port + " succeed!");
            } else {
                logger.warn("FTP connect to " + host + ":" + port + " failed!");
                ftpClient.disconnect();
            }
            ftpClient.changeWorkingDirectory(Remote_Directory);

            File Local_DIRECTORY = new File(Local_Directory);
            if (!Local_DIRECTORY.exists()) {
                Local_DIRECTORY.mkdirs();
            }
        } catch (IOException e) {
            logger.warn(e.getMessage());
        } catch (Exception ex) {
            logger.warn(ex.getMessage());
        }
    }

    public boolean download(String filename) {
        if (ftpClient == null) {
            return false;
        }
        OutputStream os = null;

        try {
            File localFile = new File(this.Local_Directory + File.separator + filename);
            os = new FileOutputStream(localFile);
            ftpClient.retrieveFile(filename, os);

            logger.info("download remote file:" + Remote_Directory + "/" + filename
                    + "to local:" + Local_Directory + File.separator + filename + " finished!");
            return true;
        } catch (Exception e) {
            logger.info("download remote file:" + Remote_Directory + "/" + filename
                    + "to local:" + Local_Directory + File.separator + filename + " failed!");
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public void close() {
        if (null != ftpClient) {
            try {
                ftpClient.logout();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } finally {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }
}
