import com.sun.jna.*;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.Wincon;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

//kernel32
public interface Kernel32 extends Library, WinNT, Wincon {
    Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class, W32APIOptions.DEFAULT_OPTIONS);

    HANDLE CreateFile(String lpFileName, int dwDesiredAccess,
                      int dwShareMode, Structure lpSecurityAttributes,
                      int dwCreationDisposition, int dwFlagsAndAttributes,
                      PointerType hTemplateFile);

    boolean ReadFile(PointerType hFile, byte[] lpBuffer, int nNumberOfBytesToRead,
                     IntByReference lpNumberOfBytesRead, Structure lpOverlapped);

    boolean CloseHandle(PointerType hFile);

    HANDLE GetProcessHandleFromHwnd(HWND hWnd);
}
