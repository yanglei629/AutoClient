package utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;


public class RuntimeUtil {
    private static final Logger logger = LogManager.getLogger(RuntimeUtil.class);

    //获取计算机名称
    public static String getComputerName() {
        String desktopName = "";
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "echo %computername%");
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), "utf-8"));
            desktopName = r.readLine();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return desktopName;
    }

    public static String getComputerName2() {
        String desktopName = "";
        try {
            Process process = Runtime.getRuntime().exec("cmd /c echo %computername%");
            BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
            desktopName = r.readLine();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return desktopName;
    }

    public static String getComputerName3() {
        String desktopName = "";
        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            System.out.println(addr.getHostName());
            System.out.println(addr.getHostAddress());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return desktopName;
    }

    //中止进程
    public static boolean killProcess(String processName) {
        Runtime runtime = Runtime.getRuntime();
        try {
            //解析进程id
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "tasklist /v /fo csv | findstr /c:" + processName);
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), "utf-8"));
            String line = r.readLine();
            String[] split = line.split(",");
            Integer pid = Integer.parseInt(split[1].replaceAll("\"", ""));
            p.destroy();
            //中止进程
            Process process = runtime.exec("taskkill /f /pid " + pid);
            process.waitFor();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }


    //启动程序
    public static boolean startProgram(String path) {
        ProcessBuilder processBuilder = new ProcessBuilder(path);
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = processBuilder.start();
            process.waitFor();
            Thread.sleep(5000);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }


    //执行命令
    public static void executeCmdCommand(String command) throws IOException {

        if (null == command || "".equals(command)) {
            return;
        }
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", command);
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


    //执行脚本
    public static boolean executeScript(final String path) {
        try {
            Process process = Runtime.getRuntime().exec("cmd /c start " + path);
            process.waitFor();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }


    public static void main(String[] args) throws InterruptedException, IOException {
        //killProcess("javaw.exe");
        //killProcess("EAP application.Client");
        //startProgram("C:\\Users\\allen\\Desktop\\EAPClient\\EAPClient.exe");
        //killProcess("javaw.exe");
        //executeCmdCommand("cd \"E:\\\" && dir");
        //executeCmdCommand("c:\\EAPClient\\EAPClient.exe");
        //executeBatScript("C:\\Users\\allen\\Desktop\\start.bat");
        //executeBatScript("C:\\EAPClient\\start.bat");
        //getComputerName3();
    }
}
