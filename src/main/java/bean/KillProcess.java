package bean;

import dto.Status;

public class KillProcess implements Action {
    public String processName;

    public KillProcess(String processName) {
        this.processName = processName;
    }

    @Override
    public Status doAction() {
        System.out.println("结束进程:" + processName);
        return null;
    }
}
