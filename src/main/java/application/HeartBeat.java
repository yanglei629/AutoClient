package application;

import com.alibaba.fastjson.JSON;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import utils.HttpClientResult;
import utils.HttpClientUtil;

public class HeartBeat implements Runnable {
    public static final Logger logger = LogManager.getLogger(HeartBeat.class);
    private Client client;

    public HeartBeat() {
    }

    public HeartBeat(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        HttpClientResult httpClientResult = null;

        while (true) {
            try {
                httpClientResult = HttpClientUtil.doPost("http://192.168.0.102:9001/client/heartbeat", null, null, JSON.toJSONString(client));
                logger.info(httpClientResult);
                Thread.sleep(30000);
            } catch (Exception e) {
                if (e instanceof HttpHostConnectException) {
                    logger.warn("cannot connect server");
                } else {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(30000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        }
    }
}
