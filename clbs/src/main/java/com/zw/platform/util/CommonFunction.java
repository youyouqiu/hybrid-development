package com.zw.platform.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Created by LiaoYuecai on 2017/9/19.
 */
public class CommonFunction {
    private static final String DES = "DES";
    private static final String Key = "20020818";

    /**
     * Description 根据键值进行加密
     * @param data 加密数据
     */
    public static String encrypt(String data) throws Exception {
        byte[] bt = encrypt(data.getBytes(), Key.getBytes());
        return new String(Base64.getEncoder().encode(bt));
    }

    /**
     * Description 根据键值进行解密
     * @param data 解密数据
     */
    public static String decrypt(String data) throws Exception {
        if (data == null) {
            return null;
        }
        byte[] buf = Base64.getDecoder().decode(data);
        byte[] bt = decrypt(buf, Key.getBytes());
        return new String(bt);
    }

    /**
     * Description 根据键值进行加密
     * @param data 加密值byte数组
     * @param key 加密键byte数组
     */
    private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        SecureRandom sr = new SecureRandom();
        DESKeySpec dks = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);
        Cipher cipher = Cipher.getInstance(DES);
        cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);
        return cipher.doFinal(data);
    }

    /**
     * Description 根据键值进行解密
     * @param data 解密值byte数组
     * @param key 解密键byte数组
     */
    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        SecureRandom sr = new SecureRandom();
        DESKeySpec dks = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);
        Cipher cipher = Cipher.getInstance(DES);
        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
        return cipher.doFinal(data);
    }


}
