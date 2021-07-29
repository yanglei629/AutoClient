import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import org.junit.Test;
import utils.Kernel32;
import utils.User32;

import java.io.IOException;

public class User32Test {
    @Test
    public void showMessageBox() {
        User32.INSTANCE.MessageBoxW(0, new WString("内容"), new WString("标题"), 1);
    }

    @Test
    public void findWidow() {

        WinDef.HWND byWare = User32.INSTANCE.FindWindow(null, "EAP ");
        if (byWare == null) {
            System.out.println("此窗口不存在:" + "");
            return;
        }
        System.out.println(byWare);
        User32.INSTANCE.ShowWindow(byWare, WinUser.SW_RESTORE);
        User32.INSTANCE.SetForegroundWindow(byWare);
        //utils.User32.INSTANCE.CloseWindow(byWare);
    }

    @Test
    public void enumWindow() {
        User32.INSTANCE.EnumWindows(new WinUser.WNDENUMPROC() {
            int count;

            @Override
            public boolean callback(WinDef.HWND hwnd, Pointer pointer) {
                byte[] windowText = new byte[512];
                User32.INSTANCE.GetWindowTextA(hwnd, windowText, 512);
                String wText = Native.toString(windowText, "GB2312");
                wText = (wText.isEmpty()) ? "" : "; text: " + wText;
                System.out.println(++count + ":" + "Found window " + hwnd + wText + "\r\n");
                WinNT.HANDLE handle = User32.INSTANCE.GetProcessHandleFromHwnd(hwnd);
                System.out.println("handle:" + handle);
                return true;
            }
        }, null);
    }

    @Test
    public void getProcess() {
        WinDef.HWND byWare = User32.INSTANCE.FindWindow(null, "EAP ");

        WinNT.HANDLE handle = Kernel32.INSTANCE.GetProcessHandleFromHwnd(byWare);
        System.out.println(handle);
    }

    @Test
    public void processBuilder() throws IOException {
        //启动程序
        ProcessBuilder processBuilder = new ProcessBuilder("C:\\Users\\allen\\Desktop\\EAP application.Client\\EAP application.Client.exe");
        processBuilder.start();
    }




}
