package application;

import dto.Status;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class DispatcherServlet {
    public static final Logger logger = LogManager.getLogger(DispatcherServlet.class);

    public static Object dispatch(String uri, StringBuilder buf) {
        //controller

        //状态
        if (uri.equals("/client/status")) {
            logger.info("query status");
            String status = Client.queryStatus();
            if (status.equals("ONLINE")) {
                return new Status("ONLINE", -1);
            }
            return new Status("OFFLINE", -1);
        }


        if (uri.equals("/client/close")) {
            logger.info("close program");
            String result = Client.close();
            if (result.equals("Success")) {
                return new Status(-1, "关闭成功");
            }
            return new Status(0, "关闭失败");
        }

        if (uri.equals("/client/start")) {
            logger.info("start program");
            String result = Client.start();
            if (result.equals("Success")) {
                return new Status(-1, "开启成功");
            }
            return new Status(0, "开启失败");
        }


        //传输文件
        if (uri.equals("/client/upload")) {
            logger.info("upload file");

        }

        //更新
        if (uri.equals("/client/update")) {
            logger.info("update program");
            boolean update = Client.update();
            if (update) {
                buf.append("RESPONSE: " + "更新成功\r\n");
            } else {
                buf.append("RESPONSE: " + "更新失败\r\n");
            }
        }

        return null;
    }
}
