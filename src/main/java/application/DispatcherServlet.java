package application;

import bean.Action;
import bean.Flow;
import com.alibaba.fastjson.JSON;
import dto.Status;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.util.CharsetUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class DispatcherServlet {
    public static final Logger logger = LogManager.getLogger(DispatcherServlet.class);
    private static final StringBuilder buf = new StringBuilder();
    private static HashMap<String, String> _headers = new HashMap<String, String>();
    private static Map<String, List<String>> _params = new HashMap<String, List<String>>();

    public static boolean dispatch(HttpRequest request, ChannelHandlerContext ctx,
                                   LastHttpContent trailer, HashMap<String, String> headers,
                                   Map<String, List<String>> params, byte[] bodyContent) {
        _headers = headers;
        _params = params;

        //处理请求
        Object response = doDispatch(request, bodyContent);

        //回复请求
        if (!writeResponse(request, trailer, ctx, response)) {
            logger.info("写回复失败");
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }

        return true;
    }

    public static Object doDispatch(HttpRequest request, byte[] bodyContent) {
        String uri = request.uri();

        //controller

        //***
        if (uri.equals("/")) {
            buf.setLength(0);

            buf.append("WELCOME TO THE WILD WILD WEB SERVER\r\n");
            buf.append("===================================\r\n");

            buf.append("VERSION: ").append(request.protocolVersion()).append("\r\n");
            buf.append("HOSTNAME: ").append(HttpHeaders.getHost(request, "unknown")).append("\r\n");
            buf.append("REQUEST_URI: ").append(request.uri()).append("\r\n\r\n");

            return "/";
        }

        //html
        if (uri.equals("/info")) {
            buf.setLength(0);

            buf.append("<!DOCTYPE html>\n" +
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
            String path = "";
            path = _params.get("path").get(0);

            if (!path.matches("^[a-zA-Z]:.*")) {
                path = Main.programPath + File.separator + path;
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
            storagePath = Main.programPath + File.separator + fileName;
            //}

            logger.info(storagePath);
            try {
                File file = new File(storagePath);
                if (!file.exists()) {
                    file.createNewFile();
                } else {
                    return new Status(0, "文件已经存在");
                }
                os = new FileOutputStream(file);
                os.write(bodyContent);
                os.flush();
            } catch (IOException exception) {
                exception.printStackTrace();
            } finally {
                try {
                    os.close();
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
                buf.append("RESPONSE: " + "更新成功\r\n");
            } else {
                buf.append("RESPONSE: " + "更新失败\r\n");
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


    private static boolean writeResponse(HttpRequest request, HttpObject currentObj, ChannelHandlerContext ctx, Object res) {
        // Decide whether to close the connection or not.
        boolean keepAlive = HttpHeaders.isKeepAlive(request);
        // Build the response object.
        //FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, currentObj.decoderResult().isSuccess() ? HttpResponseStatus.OK : HttpResponseStatus.BAD_REQUEST);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

        //text/plain
        if ("/".equals(res)) {
            response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, currentObj.decoderResult().isSuccess() ? HttpResponseStatus.OK : HttpResponseStatus.BAD_REQUEST,
                    Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));

            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        }

        //text/html
        if ("info".equals(res)) {
            response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, currentObj.decoderResult().isSuccess() ? HttpResponseStatus.OK : HttpResponseStatus.BAD_REQUEST,
                    Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));

            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        }

        //application/json
        if (res instanceof Status) {
            response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, currentObj.decoderResult().isSuccess() ? HttpResponseStatus.OK : HttpResponseStatus.BAD_REQUEST,
                    Unpooled.wrappedBuffer(JSON.toJSONBytes(res)));

            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        }


        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            // Add keep alive header as per:
            // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        //设置cookie
        // Encode the cookie.
        String cookieString = request.headers().get(HttpHeaderNames.COOKIE);
        if (cookieString != null) {
            Set<Cookie> cookies = ServerCookieDecoder.LAX.decode(cookieString);
            if (!cookies.isEmpty()) {
                // Reset the cookies if necessary.
                for (io.netty.handler.codec.http.cookie.Cookie cookie : cookies) {
                    response.headers().add(HttpHeaderNames.SET_COOKIE, io.netty.handler.codec.http.cookie.ServerCookieEncoder.LAX.encode(cookie));
                }
            }
        } else {
            // Browser sent no cookie.  Add some.
            response.headers().add(HttpHeaderNames.SET_COOKIE, io.netty.handler.codec.http.cookie.ServerCookieEncoder.LAX.encode("key1", "value1"));
            response.headers().add(HttpHeaderNames.SET_COOKIE, io.netty.handler.codec.http.cookie.ServerCookieEncoder.LAX.encode("key2", "value2"));
        }

        // Write the response.
        ctx.write(response);

        return keepAlive;
    }
}
