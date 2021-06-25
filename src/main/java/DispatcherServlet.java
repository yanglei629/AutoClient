import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

public class DispatcherServlet {
    public static final Logger logger = LogManager.getLogger(DispatcherServlet.class);

    public static void dispatch(String uri, StringBuilder buf) {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
        Map<String, List<String>> params = queryStringDecoder.parameters();
        if (!params.isEmpty()) {
            for (Map.Entry<String, List<String>> p : params.entrySet()) {
                /*String key = p.getKey();
                List<String> vals = p.getValue();
                for (String val : vals) {
                }*/
                if (p.getKey().toUpperCase().equals("UPDATE")) {
                    Machine.update();
                    buf.append("RESPONSE: " + "更新成功\r\n");
                }
            }
        }
    }
}
