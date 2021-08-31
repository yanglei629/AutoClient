package utils;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class FileUtil extends FileUtils {
    public static final Logger logger = LogManager.getLogger(FileUtil.class);


    public static boolean deleteFileOrDirectory(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            try {
                deleteDirectory(file);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                return false;
            }
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
