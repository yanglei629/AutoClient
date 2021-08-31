package bean;

import dto.Status;

public class StartProcess implements Action {
    public String path;

    public StartProcess(String path) {
        this.path = path;
    }

    @Override
    public Status doAction() {
        System.out.println("启动进程:" + path);
        return null;
    }
}
