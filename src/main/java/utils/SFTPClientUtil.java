package utils;

import com.jcraft.jsch.*;
import org.apache.log4j.LogManager;

public class SFTPClientUtil {
    public static final org.apache.log4j.Logger logger = LogManager.getLogger(SFTPClientUtil.class);
    public static final String HOST = "10.65.1.168";
    public static final Integer PORT = 21;
    public static final String USERNAME = "ftp01";
    public static final String PASSWORD = "abcd12#$";
    public static final String Download_DIRECTORY = "D:\\";
    ChannelSftp sftpChannel;
    Session session;

    public SFTPClientUtil() {
        JSch jsch = new JSch();
        try {
            logger.info("Connect File Server:" + " " + "host:" + HOST + " " + "port:" + PORT);
            session = jsch.getSession(USERNAME, HOST, PORT);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(PASSWORD);
            session.connect();
            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;
        } catch (JSchException e) {
            logger.warn(e.getMessage());
            logger.warn("连接失败");
        }
    }

    public void download(String filename) {
        try {
            sftpChannel.get(filename, filename);
        } catch (SftpException e) {
            logger.warn(e.getMessage());
            logger.warn("下载失败");
            //e.printStackTrace();
        }
    }

    public void close() {
        sftpChannel.exit();
        session.disconnect();
    }
}
