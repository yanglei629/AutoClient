import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class ExtractTest {
    public static void main(String[] args) {
        String source = "E:\\七剑\\help.zip";
        String destination = "E:\\七剑\\";
        String password = "password";

        try {
            ZipFile zipFile = new ZipFile(source);
            if (zipFile.isEncrypted()) {
                zipFile.setPassword(password);
            }
            zipFile.extractAll(destination);
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }
}
