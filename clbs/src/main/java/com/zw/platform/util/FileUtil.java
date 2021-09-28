package com.zw.platform.util;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

/**
 * 获取文件的的真实类型
 *
 * @version 1.0
 * @author lijie
 * @since 2018年12月19日下午4:44:08
 */
@Log4j2
public class FileUtil {
    private static final Logger logger = LogManager.getLogger(FileUtil.class);

    //h264的文件的文件头
    private static final byte[] h264 = new byte[] { 0, 0, 0, 1, 103 };

    private FileUtil() {
    }

    /**
     * 获取文件类型
     * ps:流会关闭
     */
    public static String getFileType(InputStream inputStream) {
        final String fileHeader = getFileHeader(inputStream);
        return FileType.match(fileHeader);
    }

    private static String getFileHeader(InputStream inputStream) {
        String value = null;
        try {
            byte[] b = new byte[16];
            int len = inputStream.read(b);
            if (len > 0) {
                value = ConvertUtil.toHexString(b);
            }
        } catch (Exception e) {
            log.error("Failed to read file.", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error(e);
            }
        }
        return value;
    }

    public static void save(String filePath, byte[] data) {
        Path path;
        try {
            path = Paths.get(filePath);
        } catch (Exception e) {
            log.error("获取文件路径失败", e);
            return;
        }
        if (!path.getParent().toFile().exists()) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                log.error("创建文件夹失败", e);
                return;
            }
        }
        try (ByteChannel channel = Files.newByteChannel(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            channel.write(ByteBuffer.wrap(data));
        } catch (IOException e) {
            log.error("保存文件失败", e);
        }
    }


    /**
     * 读取文件内容，作为字符串返回
     */
    public static String readFileAsString(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException(filePath);
        }

        if (file.length() > 1024 * 1024 * 1024) {
            throw new IOException("File is too large");
        }

        StringBuilder sb = new StringBuilder((int) (file.length()));
        // 创建字节输入流
        try (FileInputStream fis = new FileInputStream(filePath)) {
            // 创建一个长度为10240的Buffer
            byte[] buf = new byte[10240];
            // 用于保存实际读取的字节数
            int hasRead;
            while ((hasRead = fis.read(buf)) > 0) {
                sb.append(new String(buf, 0, hasRead));
            }
        }
        return sb.toString();
    }

    /**
     * 根据文件路径读取byte[] 数组
     */
    public static byte[] readFileByBytes(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException(filePath);
        } else {

            try (ByteArrayOutputStream bos = new ByteArrayOutputStream((int) file.length());
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
                short bufSize = 1024;
                byte[] buffer = new byte[bufSize];
                int len1;
                while (-1 != (len1 = in.read(buffer, 0, bufSize))) {
                    bos.write(buffer, 0, len1);
                }

                return bos.toByteArray();
            }
        }
    }

    public static void h264ReadAndWriteFile(RandomAccessFile read, String path) {
        try (RandomAccessFile write = new RandomAccessFile(new File(path), "rw")) {
            byte[] buffer = new byte[h264.length];
            int start = 0;

            while ((read.read(buffer)) != -1) {
                if (Arrays.equals(buffer, h264)) {
                    do {
                        write.write(buffer);
                    } while (read.read(buffer) != -1);
                    break;
                } else {
                    start++;
                    read.seek(start);
                }
            }
        } catch (Exception e) {
            logger.error("将视频文件截取h264时异常", e);
        } finally {
            IOUtils.closeQuietly(read);
        }
    }

    /**
     * 检查图片后缀是否是图片
     * @param suffix
     * @return
     */
    public static boolean checkPicSuffix(String suffix) {
        if (StringUtils.isBlank(suffix)) {
            return false;
        }
        return (suffix.endsWith("png") || suffix.endsWith("jpg") || suffix.endsWith("gif") || suffix.endsWith("jpeg"));
    }

}
