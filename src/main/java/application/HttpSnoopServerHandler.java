package application;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.SystemPropertyUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

public class HttpSnoopServerHandler extends SimpleChannelInboundHandler<Object> {
    public static final Logger logger = LogManager.getLogger(HttpSnoopServerHandler.class);

    private HttpRequest request;
    /**
     * Buffer that stores the response content
     */
    private final StringBuilder buf = new StringBuilder();
    byte[] jsonBytes;

    LastHttpContent trailer;

    FileOutputStream os = null;

    private boolean readingChunks = false; // 分块读取开关

    private HashMap<String, String> headers;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        //请求处理
        logger.info("received request");

        //解析header
        HttpHeaders headers = request.headers();
        if (!headers.isEmpty()) {
            for (Map.Entry<String, String> h : headers) {
                String key = h.getKey();
                String value = h.getValue();
                buf.append("HEADER: ").append(key).append(" = ").append(value).append("\r\n");
            }
            buf.append("\r\n");
        }

        //解析请求参数
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> params = queryStringDecoder.parameters();
        if (!params.isEmpty()) {
            for (Entry<String, List<String>> p : params.entrySet()) {
                String key = p.getKey();
                List<String> vals = p.getValue();
                for (String val : vals) {
                    buf.append("PARAM: ").append(key).append(" = ").append(val).append("\r\n");
                }
            }
            buf.append("\r\n");
        }

        appendDecoderResult(buf, request);

        //处理请求
        Object response = DispatcherServlet.dispatch(request.uri(), buf);
        jsonBytes = JSON.toJSONBytes(response);


        //回复请求
        if (!writeResponse(trailer, ctx)) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }


        ctx.flush();
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws IOException {
        //*******
        logger.info("received request0");


        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;

            if (HttpUtil.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }


            buf.setLength(0);
            /*buf.append("WELCOME TO THE WILD WILD WEB SERVER\r\n");
            buf.append("===================================\r\n");

            buf.append("VERSION: ").append(request.protocolVersion()).append("\r\n");
            buf.append("HOSTNAME: ").append(HttpHeaders.getHost(request, "unknown")).append("\r\n");
            buf.append("REQUEST_URI: ").append(request.uri()).append("\r\n\r\n");*/

            buf.append("<!DOCTYPE html>\r\n")
                    .append("<html><head><meta charset='utf-8' /><title>")
                    .append("Listing of: ")
                    .append("</title></head><body>\r\n")
                    .append("<h3>Listing of: ")
                    .append("</h3>\r\n")
                    .append("<ul>")
                    .append("<li><a href=\"../\">..</a></li>\r\n");

            headers = parseHeaders(request);

            if ("image/png".equals(headers.get("Content-Type"))) {
                logger.info("初始化传输文件");
                if (null == os) {
                    String local = SystemPropertyUtil.get("user.home") + File.separator + "sss.png";
                    System.out.println(local);
                    File file = new File(local);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    os = new FileOutputStream(file);
                }
            }
        }


        //*******
        if (msg instanceof HttpContent) {

            HttpContent httpContent = (HttpContent) msg;

            readingChunks = true;

            ByteBuf content = httpContent.content();
            if (content.isReadable()) {
                buf.append("CONTENT: ");
                buf.append(content.toString(CharsetUtil.UTF_8));
                buf.append("\r\n");
                appendDecoderResult(buf, request);
            }

            if (msg instanceof LastHttpContent) {
                buf.append("END OF CONTENT\r\n");

                //LastHttpContent trailer = (LastHttpContent) msg;
                trailer = (LastHttpContent) msg;
                if (!trailer.trailingHeaders().isEmpty()) {
                    buf.append("\r\n");
                    for (String name : trailer.trailingHeaders().names()) {
                        for (String value : trailer.trailingHeaders().getAll(name)) {
                            buf.append("TRAILING HEADER: ");
                            buf.append(name).append(" = ").append(value).append("\r\n");
                        }
                    }
                    buf.append("\r\n");
                }

                readingChunks = false;
            }

            //文件传输
            if ("image/png".equals(headers.get("Content-Type"))) {
                byte[] bytes = new byte[content.readableBytes()];

                while (content.isReadable()) {
                    content.readBytes(bytes);
                    os.write(bytes);
                }
                if (null != os) {
                    os.flush();
                }

                if (!readingChunks) {
                    if (null != os) {
                        System.out.println("Download done->");
                        os.flush();
                        os.close();
                        //file = null;
                        os = null;
                    }
                    ctx.channel().close();
                }
            }
        }
    }


    private static void appendDecoderResult(StringBuilder buf, HttpObject o) {
        DecoderResult result = o.decoderResult();
        if (result.isSuccess()) {
            return;
        }

        buf.append(".. WITH DECODER FAILURE: ");
        buf.append(result.cause());
        buf.append("\r\n");
    }

    private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
        // Decide whether to close the connection or not.
        boolean keepAlive = HttpHeaders.isKeepAlive(request);
        // Build the response object.

        //text/plain
        /*FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, currentObj.decoderResult().isSuccess() ? HttpResponseStatus.OK : HttpResponseStatus.BAD_REQUEST,
                Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));*/

        //response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        //text/html
        //response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");

        //application/json
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, currentObj.decoderResult().isSuccess() ? HttpResponseStatus.OK : HttpResponseStatus.BAD_REQUEST,
                Unpooled.wrappedBuffer(jsonBytes));

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            // Add keep alive header as per:
            // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        // Encode the cookie.
        String cookieString = request.headers().get(HttpHeaderNames.COOKIE);
        if (cookieString != null) {
            Set<io.netty.handler.codec.http.cookie.Cookie> cookies = ServerCookieDecoder.LAX.decode(cookieString);
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

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        ctx.write(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


    public HashMap<String, String> parseHeaders(HttpRequest request) {
        //解析header
        HashMap<String, String> map = new HashMap<String, String>();
        HttpHeaders headers = request.headers();
        if (!headers.isEmpty()) {
            for (Map.Entry<String, String> h : headers) {
                map.put(h.getKey(), h.getValue());
            }
            buf.append("\r\n");
        }
        return map;
    }


    public HashMap<String, String> parseParams(HttpRequest request) {
        //解析请求参数
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> params = queryStringDecoder.parameters();
        if (!params.isEmpty()) {
            for (Entry<String, List<String>> p : params.entrySet()) {
                String key = p.getKey();
                List<String> vals = p.getValue();
                for (String val : vals) {
                    buf.append("PARAM: ").append(key).append(" = ").append(val).append("\r\n");
                }
            }
            buf.append("\r\n");
        }

        return null;
    }

    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

    private static String sanitizeUri(String uri) {
        // Decode the path.
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }

        if (uri.isEmpty() || uri.charAt(0) != '/') {
            return null;
        }

        // Convert file separators.
        uri = uri.replace('/', File.separatorChar);

        // Simplistic dumb security check.
        // You will have to do something serious in the production environment.
        if (uri.contains(File.separator + '.') ||
                uri.contains('.' + File.separator) ||
                uri.charAt(0) == '.' || uri.charAt(uri.length() - 1) == '.' ||
                INSECURE_URI.matcher(uri).matches()) {
            return null;
        }

        // Convert to absolute path.
        return SystemPropertyUtil.get("user.dir") + File.separator + uri;
    }
}