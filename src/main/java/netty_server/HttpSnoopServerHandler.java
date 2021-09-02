package netty_server;

import com.alibaba.fastjson.JSON;
import dto.Status;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.util.CharsetUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import utils.ByteUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class HttpSnoopServerHandler extends SimpleChannelInboundHandler<Object> {
    public static final Logger logger = LogManager.getLogger(HttpSnoopServerHandler.class);

    private HttpRequest request;
    /**
     * Buffer that stores the response content
     */
    private final StringBuilder buf = new StringBuilder();

    byte[] bodyContent;

    LastHttpContent trailer;

    //headers
    private HashMap<String, String> headers;

    //params
    private Map<String, List<String>> params;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        //请求处理
        logger.info("request complete");

        appendDecoderResult(buf, request);

        //处理请求
        Object response = DispatcherServlet.dispatch(request, headers, params, buf, bodyContent);

        //回复请求
        if (!writeResponse(request, trailer, ctx, response, buf)) {
            logger.info("写回复失败");
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }

        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws IOException {

        logger.info("channel read0");

        //*******
        if (msg instanceof HttpRequest) {
            logger.info("request");

            HttpRequest request = this.request = (HttpRequest) msg;
            bodyContent = new byte[0];

            if (HttpUtil.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }


            buf.setLength(0);

            //解析header和params
            headers = parseHeaders(request);
            params = parseParams(request);
        }


        //*******
        if (msg instanceof HttpContent) {
            logger.info("request body");

            HttpContent httpContent = (HttpContent) msg;


            ByteBuf content = httpContent.content();

            if (content.isReadable()) {
                buf.append("CONTENT: ");
                buf.append(content.toString(CharsetUtil.UTF_8));
                buf.append("\r\n");
                byte[] bytes = new byte[content.readableBytes()];
                content.readBytes(bytes);
                bodyContent = ByteUtil.append(bodyContent, bytes);
                appendDecoderResult(buf, request);
            }

            if (msg instanceof LastHttpContent) {
                logger.info("END OF CONTENT");

                buf.append("END OF CONTENT\r\n");

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


    public Map<String, List<String>> parseParams(HttpRequest request) {
        //解析请求参数
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> params = queryStringDecoder.parameters();
        if (!params.isEmpty()) {
            for (Entry<String, List<String>> p : params.entrySet()) {
                String key = p.getKey();
                List<String> vals = p.getValue();
                for (String val : vals) {
                    buf.append("PARAM: ").append(key).append(" = ").append(val).append("\r\n");
                    logger.info("PARAM: " + key + "=" + val);
                }
            }
            buf.append("\r\n");

            return params;
        }

        return null;
    }


    private static boolean writeResponse(HttpRequest request, HttpObject currentObj, ChannelHandlerContext ctx, Object res, StringBuilder buf) {
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
            /*response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, currentObj.decoderResult().isSuccess() ? HttpResponseStatus.OK : HttpResponseStatus.BAD_REQUEST,
                    Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));*/
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                    Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));

            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        }

        //application/json
        if (res instanceof Status) {
            /*response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, currentObj.decoderResult().isSuccess() ? HttpResponseStatus.OK : HttpResponseStatus.BAD_REQUEST,
                    Unpooled.wrappedBuffer(JSON.toJSONBytes(res)));*/
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
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