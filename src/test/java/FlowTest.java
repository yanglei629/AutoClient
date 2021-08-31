import bean.*;
import com.alibaba.fastjson.JSON;

import java.util.Date;

public class FlowTest {
    public static void main(String[] args) {
        Flow update = new Flow();
        update.flowName = "更新EAP";
        update.createTime = new Date().toString();

        update.add(new KillProcess("EAP Client"));
        update.add(new DeleteFile("c:\\EAPClient\\EAPClient.exe"));
        update.add(new DownloadFromFtp("/EAPClient/EAPClient.exe"));
        update.add(new StartProcess("c:\\EAPClient\\EAPClient.exe"));

        String s = JSON.toJSONString(update);

        System.out.println(s);

        Flow o = JSON.parseObject(s, Flow.class);
        System.out.println(o.flowName);
    }
}
