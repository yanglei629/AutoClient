import java.net.*;
import java.util.Enumeration;

public class IPTest {
    public static void main(String[] args) throws SocketException, UnknownHostException {
        //获取本机ip
        final DatagramSocket socket = new DatagramSocket();
        socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
        String ip = socket.getLocalAddress().getHostAddress();
        System.out.println("IP:" + ip);

        //遍历接口
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            // filters out 127.0.0.1 and inactive interfaces
            if (iface.isLoopback() || !iface.isUp() || iface.getDisplayName().contains("Virtual") || iface.getDisplayName().contains("VPN"))
                continue;

            //遍历接口地址
            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                ip = addr.getHostAddress();
                if (!ip.equals("0.0.0.0") && ip.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
                    System.out.println(iface.getDisplayName() + " " + ip);
                }
            }
        }
    }
}
