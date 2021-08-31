package bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Flow {
    @JSONField(ordinal = 0)
    public String flowName = "";

    @JSONField(ordinal = 1)
    public String createTime = "";

    @JSONField(ordinal = 2)
    public Map<Integer, Action> actions = new HashMap<Integer, Action>();

    @JSONField(serialize = false)
    public Integer size = 0;

    @JSONField(serialize = false)
    public Integer index = 0;

    public static class FlowIterator implements Iterator {

        public final Flow flow;

        private FlowIterator(Flow flow) {
            this.flow = flow;
        }

        @Override
        public boolean hasNext() {
            if (flow.index < flow.size) {
                return true;
            }
            return false;
        }

        @Override
        public Action next() {
            return flow.actions.get(flow.index++);
        }

        @Override
        public void remove() {
            flow.actions.remove(--flow.size);
        }
    }


    public Iterator<Action> Iterator() {
        return new FlowIterator(this);
    }


    public void add(Action action) {
        this.actions.put(size++, action);
    }

    public static void main(String[] args) {
        Flow update = new Flow();
        update.flowName = "更新EAP";
        update.createTime = new Date().toString();

        update.add(new KillProcess("EAP Client"));
        update.add(new DeleteFile("c:\\EAPClient\\EAPClient.exe"));
        update.add(new DownloadFromFtp("/EAPClient/EAPClient.exe"));
        update.add(new StartProcess("c:\\EAPClient\\EAPClient.exe"));


        String s = JSON.toJSONString(update);

        /*Object o1 = JSONObject.toJSON(update);
        System.out.println(o1);*/


        System.out.println("res:" + s);

        Flow o = JSON.parseObject(s, Flow.class);
        System.out.println(o.flowName);
        Iterator<Action> iterator1 = o.Iterator();
        while (iterator1.hasNext()) {
            iterator1.next().doAction();
        }

        Iterator<Action> iterator = update.Iterator();
        while (iterator.hasNext()) {
            iterator.next().doAction();
        }
    }
}
