package netty_server;

import application.Client;
import application.Main;
import bean.Action;
import bean.Flow;
import com.alibaba.fastjson.JSON;
import dto.Status;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class DispatcherServlet {
    public static final Logger logger = LogManager.getLogger(DispatcherServlet.class);
    private static StringBuilder _buf = new StringBuilder();
    private static HashMap<String, String> _headers = new HashMap<String, String>();
    private static Map<String, List<String>> _params = new HashMap<String, List<String>>();

    public static Object dispatch(HttpRequest request, HashMap<String, String> headers,
                                  Map<String, List<String>> params, StringBuilder buf, byte[] bodyContent) {
        _headers = headers;
        _params = params;
        _buf = buf;

        //处理请求
        Object response = doDispatch(request, bodyContent);

        return response;
    }

    public static Object doDispatch(HttpRequest request, byte[] bodyContent) {
        String uri = request.uri();

        //controller

        //***
        if (uri.equals("/")) {
            _buf.setLength(0);

            _buf.append("WELCOME TO THE WILD WILD WEB SERVER\r\n");
            _buf.append("===================================\r\n");

            _buf.append("VERSION: ").append(request.protocolVersion()).append("\r\n");
            _buf.append("HOSTNAME: ").append(HttpHeaders.getHost(request, "unknown")).append("\r\n");
            _buf.append("REQUEST_URI: ").append(request.uri()).append("\r\n\r\n");

            return "/";
        }

        //html
        if (uri.equals("/info")) {
            _buf.setLength(0);

            _buf.append("<!DOCTYPE html>\n" +
                    "<html lang=\"en\" >\n" +
                    "<head>\n" +
                    "  <meta charset=\"UTF-8\">\n" +
                    "  <title>CodePen - welcome</title>\n" +
                    "  \n" +
                    "\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<!-- partial:index.partial.html -->\n" +
                    "<div class=\"content\">\n" +
                    "  <h4>Welcom to Netty Sever</h4>\n" +
                    "</div>\n" +
                    "<style>\n" +
                    "  .content{\n" +
                    "  background:#0c1014;\n" +
                    "  font-size: 17px;\n" +
                    "  font-weight: 500;\n" +
                    "  color: #fff;\n" +
                    "  height:100px;\n" +
                    "}\n" +
                    "  \n" +
                    "  .content h4{\n" +
                    "    text-align: center;\n" +
                    "  }\n" +
                    "</style>\n" +
                    "<!-- partial -->\n" +
                    "  \n" +
                    "</body>\n" +
                    "</html>");

            return "html";
        }


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

        //执行脚本
        if (uri.startsWith("/client/execute")) {
            logger.info("start script");


            String path = "";
            path = _params.get("path").get(0);

            if (!path.matches("^[a-zA-Z]:.*")) {
                path = Main.upload + File.separator + path;
            }

            logger.info("execute script:" + path);

            if (!new File(path).exists()) {
                return new Status(0, "脚本不存在");
            }
            if (Client.executeScript(path)) {
                return new Status(0, "执行脚本成功");
            }
        }


        //传输文件
        if (uri.startsWith("/client/upload")) {
            logger.info("upload file");

            System.out.println(bodyContent.length);
            //创建文件
            String storagePath;
            String fileName;
            FileOutputStream os = null;

            //获取文件名
            if (null != _params && !_params.isEmpty()) {
                fileName = _params.get("filename").get(0);
            } else {
                fileName = new Date().toString().split(" ")[3].replaceAll(":", "_");
            }

            /*if (fileName.matches("^[a-zA-Z]:.*")) {
                storagePath = fileName;
            } else {*/
            storagePath = Main.upload + File.separator + fileName;
            //}

            logger.info(storagePath);
            try {
                File file = new File(storagePath);
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                os = new FileOutputStream(file);
                os.write(bodyContent);
                os.flush();
            } catch (IOException exception) {
                exception.printStackTrace();
            } finally {
                try {
                    if (null != os) {
                        os.close();
                    }
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }

            return new Status(-1, "上传成功");
        }

        //更新
        if (uri.equals("/client/update")) {
            logger.info("update program");
            boolean update = Client.update();
            if (update) {
                _buf.append("RESPONSE: " + "更新成功\r\n");
            } else {
                _buf.append("RESPONSE: " + "更新失败\r\n");
            }
        }

        //更新
        if (uri.equals("/client/flow")) {
            logger.info("flow");
            Flow flow = JSON.parseObject(bodyContent, Flow.class);
            Iterator<Action> actions = flow.Iterator();
            while (actions.hasNext()) {


            }
            return null;
        }

        return null;
    }
}
