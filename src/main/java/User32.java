import com.sun.jna.*;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.Wincon;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

//kernel32
public interface User32 extends Library, WinNT, Wincon {
    User32 INSTANCE = Native.load("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);

    void MessageBoxW(int wnd, WString txt, WString caption, long type);

    HWND FindWindow(String lpClassName, String lpWindowName);

    boolean ShowWindow(HWND hWnd, int nCmdShow);

    boolean SetForegroundWindow(HWND hWnd);

    boolean CloseWindow(HWND hWnd);

    int GetWindowTextA(HWND hWnd, byte[] lpString, int nMaxCount);

    boolean EnumWindows(WinUser.WNDENUMPROC lpEnumFunc, Pointer arg);

    HANDLE GetProcessHandleFromHwnd(HWND hWnd);
}
