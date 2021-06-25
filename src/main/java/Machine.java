import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class Machine {
    public static final Logger logger = LogManager.getLogger(Machine.class);

    public static boolean update() {
        logger.info("开始更新程序");

        try {
            //结束进程
            if (User32.INSTANCE.FindWindow(null, "EAP Client") != null) {
                //RuntimeUtil.killProcess("javaw.exe");
            }
            logger.info("结束程序完成");
            //删除旧程序文件
            String path = "c:\\" + File.separator + "EAP Client" + File.separator + "EAP Client.exe";
            //FileUtil.deleteFileOrDirectory(path);
            logger.info("清除旧文件完成");
            //下载新文件
            //FTPClientUtil.download("EAP Client.exe");
            logger.info("下载更新完成");
            RuntimeUtil.startProgram("C:\\Users\\allen\\Desktop\\EAP Client\\EAP Client.exe");
            logger.info("启动程序完成");
        } catch (IOException exception) {
            logger.error(exception.getMessage());
            exception.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return true;
    }
}
