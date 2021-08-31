package bean;

import dto.Status;

public class DeleteFile implements Action {
    public String path;

    public DeleteFile(String path) {
        this.path = path;
    }

    @Override
    public Status doAction() {
        System.out.println("删除文件:" + path);
        //Client.delete(this.path);
        return null;
    }
}
