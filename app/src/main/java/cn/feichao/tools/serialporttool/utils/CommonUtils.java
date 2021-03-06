package cn.feichao.tools.serialporttool.utils;

/**
 * Created by feichao on 2/13/17.
 */

public final class CommonUtils {

    private CommonUtils() {}

    /**
     * 将字节数组转换成ascii
     * @param bytes
     * @param len
     * @return
     */
    public static String asciiStrFrom(byte[] bytes, int len) {
        StringBuilder builder = new StringBuilder("");
        for(int i = 0; i < len; i++) {
            builder.append(bytes[i]).append(" ");
        }
        return builder.toString();
    }

    /**
     * 将字节数组转换成十六进制内容的字符串
     * @param bytes
     * @param len
     * @return
     */
    public static String hexStrFrom(byte[] bytes, int len) {
        StringBuilder builder = new StringBuilder("");
        for(int i = 0; i < len; i++) {
            int tmp = bytes[i];
            String hexStr = Integer.toHexString(tmp);
            int l = hexStr.length();
            if(l == 1) {
                hexStr = "0" + hexStr;
            }else if(l > 2){
                hexStr = hexStr.substring(l - 2, l);
            }
            builder.append(hexStr).append(" ");
        }

        return builder.toString();
    }

    /**
     * 将十六进制的字符串内容转换字节数组
     * @param hex
     * @return
     */
    public static byte[] hexToBytes(String hex) {
        String[] strArr = hex.trim().split(" ");
        int len = strArr.length;
        byte[] bytes = new byte[len];
        try {
            for (int i = 0; i < strArr.length; i++) {
                int b = Integer.valueOf(strArr[i], 16); // 使用Byte.valueOf 会超出byte的范围
                bytes[i] = (byte) b;
            }
        }catch (Exception e) {
            return null;
        }
        return bytes;
    }

    /**
     * 将十六进制的字符串内容转换成ascii码的字符串内容
     * @param hex
     * @return
     */
    public static String hexToAscii(String hex) {
        byte[] bytes = hexToBytes(hex);
        if(bytes == null)
            return null;
        return asciiStrFrom(bytes, bytes.length);
    }

    /**
     * 将ascii码的字符串内容转换成字节数组
     * @param ascii
     * @return
     */
    public static byte[] asciiToBytes(String ascii) {
        String[] strArr = ascii.trim().split(" ");
        int len = strArr.length;
        byte[] bytes = new byte[len];
        try {
            for (int i = 0; i < len; i++) {
                bytes[i] = Byte.parseByte(strArr[i]);
            }
        }catch (Exception e) {
            return null;
        }
        return bytes;
    }

    /**
     * 将ascii码的字符串内容转换成十六进制的字符从内容
     * @param ascii
     * @return
     */
    public static String asciiToHex(String ascii) {
        byte[] bytes = asciiToBytes(ascii);
        if(bytes == null)
            return null;
        return hexStrFrom(bytes, bytes.length);
    }
}
