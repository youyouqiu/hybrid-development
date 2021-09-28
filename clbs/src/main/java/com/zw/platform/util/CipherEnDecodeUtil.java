package com.zw.platform.util;

import com.zw.platform.util.common.BusinessException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.net.URLDecoder;

/**
 * @Author: zjc
 * @Description:加密解密
 * @Date: create in 2020/10/15 11:35
 */
public class CipherEnDecodeUtil {
    static final String SECRET_KEY = "rybtdata";
    static final String ENCODING = "UTF-8";
    static final String ALGORITHM_KEY = "DES";
    static final String ALGORITHM_CIPHER = "DES/CBC/PKCS5Padding";

    /**
     * 加密并将加密后的byte数组转换成16进制
     * @param data
     * @return
     * @throws Exception
     */
    public static String encodeToHexStr(String data) throws BusinessException {
        byte[] encode = encode(data);
        return bytesToHex(encode);

    }

    /**
     * 将返回的16进制数据转换成byte数组，并进行解密
     * @param data
     * @return
     * @throws Exception
     */
    public static String decodeHexToStr(String data) throws BusinessException {
        byte[] decode = hexToByteArray(data);
        return deCode(decode);

    }

    private static byte[] encode(String message) throws BusinessException {
        //加密
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_CIPHER);
            DESKeySpec desKeySpec = new DESKeySpec(SECRET_KEY.getBytes(ENCODING));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM_KEY);
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec(SECRET_KEY.getBytes(ENCODING));
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            return cipher.doFinal(message.getBytes(ENCODING));
        } catch (Exception e) {
            throw new BusinessException("加密异常", e);
        }
    }

    private static String deCode(byte[] message) throws BusinessException {
        //加密
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_CIPHER);
            DESKeySpec desKeySpec = new DESKeySpec(SECRET_KEY.getBytes(ENCODING));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM_KEY);
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec(SECRET_KEY.getBytes(ENCODING));
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            String result = new String(cipher.doFinal(message), ENCODING);
            //地址重新编码
            return URLDecoder.decode(result, "UTF-8");
        } catch (Exception e) {
            throw new BusinessException("解密异常", e);
        }
    }

    /**
     * 字节数组转16进制
     * @param bytes 需要转换的byte数组
     * @return 转换后的Hex字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * hex字符串转byte数组
     * @param inHex 待转换的Hex字符串
     * @return 转换后的byte数组结果
     */
    private static byte[] hexToByteArray(String inHex) {
        int hexlen = inHex.length();
        byte[] result;
        if (hexlen % 2 == 1) {
            //奇数
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            //偶数
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = hexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }

    /**
     * Hex字符串转byte
     * @param inHex 待转换的Hex字符串
     * @return 转换后的byte
     */
    private static byte hexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);

    }

    public static void main(String[] args) throws Exception {

        String data = "晚上发顺丰";

        byte[] encodeByte = CipherEnDecodeUtil.encode(data);

        System.out.println("加密后后数据:" + encodeByte);
        String decodeData = CipherEnDecodeUtil.deCode(encodeByte);

        System.out.println("解密后数据:" + decodeData);
    }
}
