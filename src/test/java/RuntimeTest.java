import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RuntimeTest {

    @Test
    public void runtime() throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("notepad.exe");
        Thread.sleep(3000);
        process.destroy();
    }

    @Test
    public void killProcess() throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        runtime.exec("taskkill /F /IM javaw.exe");
        Thread.sleep(3000);
        ProcessBuilder processBuilder = new ProcessBuilder("C:\\Users\\allen\\Desktop\\EAP Client\\EAP Client.exe");
        processBuilder.start();
    }


    @Test
    public void executeCmdCommand() throws IOException {
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", "cd \"E:\\\" && dir");
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), "GB2312"));
        String line;
        while (true) {
            line = r.readLine();
            if (line == null) {
                break;
            }
            System.out.println(line);
        }
    }
}
