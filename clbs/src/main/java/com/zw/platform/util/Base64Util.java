package com.zw.platform.util;

import jodd.util.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Random;


/**
 * Base64工具类
 */
public class Base64Util {
    private static final Logger log = LogManager.getLogger(Base64Util.class);

    private static final char last2byte = (char) Integer.parseInt("00000011", 2);
    private static final char last4byte = (char) Integer.parseInt("00001111", 2);
    private static final char last6byte = (char) Integer.parseInt("00111111", 2);
    private static final char lead6byte = (char) Integer.parseInt("11111100", 2);
    private static final char lead4byte = (char) Integer.parseInt("11110000", 2);
    private static final char lead2byte = (char) Integer.parseInt("11000000", 2);
    private static final char[] encodeTable = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
        'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
        'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
        '4', '5', '6', '7', '8', '9', '+', '/'};


    /**
     * 根据文件路径进行Base64编码
     *
     * @param picPath 文件路径
     * @return 编码结果
     */
    public static String getImageStr(String picPath) {
        byte[] data;
        try (InputStream in = new FileInputStream(picPath)) {
            data = new byte[in.available()];
            int read = in.read(data);
            if (read <= 0) {
                return "";
            }
            //这里需要注意的是，区分linux和windows平台的换行符的区别，这里做了一个替换
            return Base64.encodeToString(data).replace("\n", "\r\n");
        } catch (Exception e) {
            log.error("编码失败！", e.getMessage());
        }
        return "";

    }

    public static String getImageStr(InputStream in, String contextPath) {
        String tempPicPath = null;
        try {
            String tempPicDirPath = contextPath + "picTemp";
            tempPicPath = tempPicDirPath + File.separator + new Random().nextInt() * 1000 + ".png";
            copyFileFromFtp(tempPicDirPath, tempPicPath, in);
            return getImageStr(tempPicPath);
        } catch (Exception e) {
            log.error("编码失败！", e.getMessage());
            return "";
        } finally {
            if (tempPicPath != null) {
                FileUtils.deleteQuietly(new File(tempPicPath));
            }
        }

    }

    private static void copyFileFromFtp(String tempPicDirPath, String tempPicPath, InputStream in) {
        FileOutputStream fsout = null;
        File tempPicDir = new File(tempPicDirPath);
        try {
            File parentFile = tempPicDir.getParentFile();
            Files.createDirectories(parentFile.toPath());
            fsout = new FileOutputStream(tempPicPath);
            IOUtils.copy(in, fsout);
        } catch (IOException e) {
            log.error("从ftp上面拷贝到clbs文件失败", e);
        } finally {
            IOUtils.closeQuietly(fsout);
            IOUtils.closeQuietly(in);
        }
    }

    public static String encode(byte[] from) {
        StringBuilder to = new StringBuilder((int) ((double) from.length * 1.34D) + 3);
        int num = 0;
        char currentByte = 0;

        int i;
        for (i = 0; i < from.length; ++i) {
            for (num %= 8; num < 8; num += 6) {
                switch (num) {
                    case 0:
                        currentByte = (char) (from[i] & lead6byte);
                        currentByte = (char) (currentByte >>> 2);
                        break;
                    case 1:
                        break;
                    case 3:
                        break;
                    case 5:
                        break;
                    default:
                        break;
                    case 2:
                        currentByte = (char) (from[i] & last6byte);
                        break;
                    case 4:
                        currentByte = (char) (from[i] & last4byte);
                        currentByte = (char) (currentByte << 2);
                        if (i + 1 < from.length) {
                            currentByte = (char) (currentByte | (from[i + 1] & lead2byte) >>> 6);
                        }
                        break;
                    case 6:
                        currentByte = (char) (from[i] & last2byte);
                        currentByte = (char) (currentByte << 4);
                        if (i + 1 < from.length) {
                            currentByte = (char) (currentByte | (from[i + 1] & lead4byte) >>> 4);
                        }
                }

                to.append(encodeTable[currentByte]);
            }
        }

        if (to.length() % 4 != 0) {
            for (i = 4 - to.length() % 4; i > 0; --i) {
                to.append("=");
            }
        }

        return to.toString();
    }
}
