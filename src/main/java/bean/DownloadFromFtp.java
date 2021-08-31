package bean;

import dto.Status;

public class DownloadFromFtp implements Action {
    public String path;

    public DownloadFromFtp(String path) {
        this.path = path;
    }

    @Override
    public Status doAction() {
        System.out.println("下载文件:" + path);
        return null;
    }
}
