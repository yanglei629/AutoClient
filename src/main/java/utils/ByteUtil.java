package utils;

public class ByteUtil {
    public static byte[] append(byte[] src, byte[] target) {
        byte[] result = new byte[src.length + target.length];
        System.arraycopy(src, 0, result, 0, src.length);
        System.arraycopy(target, 0, result, src.length, target.length);
        return result;
    }
}
