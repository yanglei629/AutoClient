package application;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import utils.ByteUtil;

import javax.sound.midi.Soundbank;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HttpSnoopServerHandler extends SimpleChannelInboundHandler<Object> {
    public static final Logger logger = LogManager.getLogger(HttpSnoopServerHandler.class);

    private HttpRequest request;
    /**
     * Buffer that stores the response content
     */
    private final StringBuilder buf = new StringBuilder();

    byte[] bodyContent;

    LastHttpContent trailer;

    FileOutputStream os = null;

    boolean file_mode = false;

    private boolean readingChunks = false; // 分块读取开关

    //headers
    private HashMap<String, String> headers;
    private Map<String, List<String>> params;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        //请求处理
        logger.info("received request");

        appendDecoderResult(buf, request);

        //处理请求
        DispatcherServlet.dispatch(request, ctx, trailer, headers, params, bodyContent);

        //回复请求
        /*if (!writeResponse(trailer, ctx)) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }*/

        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws IOException {

        logger.info("received request0");

        //*******
        if (msg instanceof HttpRequest) {
            logger.info("request");

            HttpRequest request = this.request = (HttpRequest) msg;
            bodyContent = new byte[0];

            if (HttpUtil.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }


            buf.setLength(0);

            //获取header和params
            headers = parseHeaders(request);
            params = parseParams(request);

            //检查Content-Type
            String contentType = headers.get("Content-Type");

            if (null != contentType) {
                logger.info(contentType);
                if (contentType.startsWith("image/") || contentType.startsWith("application/x-")) {
                    logger.info("初始化传输文件");
                    file_mode = true;
                    if (null == os) {

                    }

                    //创建文件
                    String storagePath;
                    String fileName;


                    /*if (null != params && !params.isEmpty()) {
                        fileName = params.get("filename").get(0);
                    } else {
                        fileName = new Date().toString().split(" ")[3].replaceAll(":", "_");
                    }

                    if (fileName.matches("^[a-zA-Z]:.*")) {
                        storagePath = fileName;
                    } else {
                        storagePath = Main.programPath + File.separator + fileName;
                    }

                    logger.info(storagePath);
                    if (null == os) {
                        File file = new File(storagePath);
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        os = new FileOutputStream(file);
                    }*/
                } else {
                    file_mode = false;
                }
            }
        }


        //*******
        if (msg instanceof HttpContent) {
            logger.info("request body");

            HttpContent httpContent = (HttpContent) msg;

            readingChunks = true;

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

                readingChunks = false;
            }

            //文件传输
            if (file_mode) {
                byte[] bytes = new byte[content.readableBytes()];

                /*while (content.isReadable()) {
                    System.out.println("1");
                    content.readBytes(bytes);
                    os.write(bytes);
                }*/
                /*content.readBytes(bytes);
                os.write(bytes);
                if (null != os) {
                    os.flush();
                }*/
                if (!readingChunks) {
                   /* if (null != os) {
                        System.out.println("Download done->");
                        os.flush();
                        os.close();
                        os = null;
                    }*/
                    //ctx.channel().close();
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
}