import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class FileUtil extends FileUtils {
    public static boolean deleteFileOrDirectory(String path) throws IOException {
        File file = new File(path);
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            if (file.delete()) {
                return true;
            }
        }
        return false;
    }

    public static boolean deleteFileOrDirectory(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            if (file.delete()) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        boolean res = deleteFileOrDirectory("d:\\dll.log");
        System.out.println(res);
    }
}
