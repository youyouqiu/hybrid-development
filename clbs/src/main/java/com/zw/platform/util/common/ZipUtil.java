package com.zw.platform.util.common;

import com.zw.platform.service.oilmassmgt.impl.OilCalibrationServiceImpl;
import jodd.util.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 解压缩字符串工具类
 * @author wangying
 */
public class ZipUtil {
    private static final Logger log = LogManager.getLogger(OilCalibrationServiceImpl.class);

    /**
     * 功能：使用gzip进行压缩，然后再用Base64进行编码
     * @return 返回压缩后字符串
     * @author wangying
     */
    public static String gzip(String primStr) {
        if (primStr == null || primStr.length() == 0) {
            return primStr;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(primStr.getBytes());
            gzip.finish();
            return Base64.encodeToString(out.toByteArray());
        } catch (IOException e) {
            log.error("gzip string error", e);
            return "";
        }
    }

    /**
     * <p>
     * Description:使用gzip进行解压缩
     * 先对压缩数据进行BASE64解码。再进行Gzip解压
     * </p>
     * @param compressedStr 压缩字符串
     * @return 返回解压字符串
     */
    public static String gunzip(String compressedStr) {
        if (compressedStr == null) {
            return null;
        }
        byte[] compressed = Base64.decode(compressedStr);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(compressed);
            GZIPInputStream ginzip = new GZIPInputStream(in)) {
            byte[] buffer = new byte[1024];
            int offset;
            while ((offset = ginzip.read(buffer)) != -1) {
                out.write(buffer, 0, offset);
            }
            return out.toString();
        } catch (IOException e) {
            log.error("gunzip string error", e);
            return "";
        }
    }

    /**
     * @param str：正常的字符串
     * @return 压缩字符串 类型为：  ³)°K,NIc i£_`Çe#  c¦%ÂXHòjyIÅÖ`
     */
    public static String compress(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(str.getBytes(StandardCharsets.UTF_8));
            gzip.finish();
            return out.toString("ISO-8859-1");
        } catch (IOException e) {
            log.error("compress string error", e);
            return null;
        }
    }

    /**
     * @return 解压字符串  生成正常字符串。
     */
    public static String uncompress(byte[] bytes, String charset) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream)) {
            //使用org.apache.commons.io.IOUtils 简化流的操作
            IOUtils.copy(gzipInputStream, byteArrayOutputStream);
            return byteArrayOutputStream.toString(charset);
        } catch (IOException e) {
            log.error("uncompress string error", e);
            return null;
        }
    }
}