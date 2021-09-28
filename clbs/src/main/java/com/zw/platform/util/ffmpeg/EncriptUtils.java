package com.zw.platform.util.ffmpeg;

import com.zw.platform.util.ConvertUtil;

import java.security.MessageDigest;

/**
 * Created by tonydeng on 15/4/17.
 */
public class EncriptUtils {

    /**
     * 把inputString加密
     */
    public static String md5(String inputStr) {
        return encodeByMD5(inputStr);
    }

    /**
     * 对字符串进行MD5编码
     */
    private static String encodeByMD5(String originString) {
        if (originString != null) {
            try {
                //创建具有指定算法名称的信息摘要
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                //使用指定的字节数组对摘要进行最后更新，然后完成摘要计算
                byte[] results = md5.digest(originString.getBytes());
                //将得到的字节数组变成字符串返回
                return ConvertUtil.toHexString(results);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
