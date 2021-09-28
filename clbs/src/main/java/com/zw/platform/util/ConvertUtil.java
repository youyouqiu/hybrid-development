package com.zw.platform.util;

import io.netty.buffer.ByteBuf;

/**
 * @author penghj
 * @version 1.0
 */
public class ConvertUtil {
    private ConvertUtil() {
    }

    /**
     * Long 按位取值，右边低位左边高位
     * todo 返回类型改成 boolean
     */
    public static int binaryLongWithOne(long src, int index) {
        return (int) (src >> index) & 1;
    }

    /**
     * Integer 按位取值，右边低位左边高位
     * todo 返回类型改成 boolean
     */
    public static int binaryIntegerWithOne(int src, int index) {
        return (src >> index) & 1;
    }

    /**
     * @param num:要获取二进制值的数
     * @param index:开始位下标           倒数第一位为0，依次类推
     * @param end:结束位下标，倒数第一位为0依次类推
     */
    public static Integer binaryDigit(Integer num, int index, int end) {
        int mask = (1 << end) - 1;
        if (index > 0) {
            mask ^= (1 << index) - 1;
        }
        return num & mask;
    }

    /**
     * 根据ByteBuf 输出Hex字符串
     */
    public static String getHexBuf(ByteBuf buffer) {
        int offset = buffer.readerIndex();
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        buffer.readerIndex(offset);
        return getHexBytes(bytes);
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     * 根据byte数组 输出HEX字符串
     */
    private static String getHexBytes(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3];
        int m;
        for (int i = 0; i < bytes.length; i++) {
            m = bytes[i] & 0xFF;
            hexChars[i * 3] = HEX_ARRAY[m >>> 4];
            hexChars[i * 3 + 1] = HEX_ARRAY[m & 0x0F];
            hexChars[i * 3 + 2] = 0x20;
        }
        return new String(hexChars).toUpperCase();
    }

    /**
     * 根据byte数组 输出HEX字符串
     */
    public static String toHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int m;
        for (int i = 0; i < bytes.length; i++) {
            m = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[m >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[m & 0x0F];
        }
        return new String(hexChars).toUpperCase();
    }

    public static String toHexString(int value) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) ((value >> 8) & 0xFF);
        bytes[1] = (byte) (value & 0xFF);
        return "0x" + toHexString(bytes);
    }
}
