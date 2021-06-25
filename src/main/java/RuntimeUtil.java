import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RuntimeUtil {
    public static boolean killProcess(String processName) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        runtime.exec("taskkill /F /IM " + processName);
        return true;
    }

    public static boolean startProgram(String path) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(path);
        Process process = processBuilder.start();
        //process.waitFor();
        //Thread.sleep(3000);
        //process.destroy();
        return true;
    }

    public static void executeCmdCommand(String command) throws IOException {
        String command1 = "cd \"E:\\\" && dir";
        if (null != command && !"".equals(command)) {
            command1 = command;
        }
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", command1);
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
        p.destroy();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        killProcess("javaw.exe");
        //startProgram("C:\\Users\\allen\\Desktop\\EAP Client\\EAP Client.exe");
        //executeCmdCommand("cd \"E:\\\" && dir");
        System.out.println("hello");
    }
}
