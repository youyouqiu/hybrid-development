package com.zw.platform.util;

import jodd.util.Base64;
import org.apache.commons.lang3.RandomStringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Chen Feng
 * @version 1.0 2018/11/7
 */
public class DecryptionUtil {
    private static final String ALGORITHMSTR = "AES/ECB/PKCS5Padding";

    public static String generateKey(String id) {
        return RandomStringUtils.randomAlphabetic(16);
    }

    private static byte[] base64Decode(String base64Code) {
        return Base64.decode(base64Code);
    }

    private static String aesDecryptByBytes(byte[] encryptBytes, String decryptKey) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decryptKey.getBytes(), "AES"));
        byte[] decryptBytes = cipher.doFinal(encryptBytes);
        return new String(decryptBytes);
    }

    public static String aesDecrypt(String encryptStr, String key) throws Exception {
        return aesDecryptByBytes(base64Decode(encryptStr), key);
    }
}
