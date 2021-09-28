package com.zw.platform.util.ffmpeg;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.List;

/**
 * Created by tonydeng on 15/4/17.
 */
public class FileUtils {
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    private static final String PATH_SRCEENTSHOT = "srceent";

    private static final String PATH_HLS = "hls";

    private static final String EXTEND_JPG = ".jpg";

    private static final String EXTEND_M3U8 = ".m3u8";

    private static final String EXTEND_TS = ".ts";

    private static final String EXTEND_MP4 = ".mp4";

    private static final String EXTEND_MP3 = ".mp3";

    private static final String EXTEND_WAV = ".wav";

    private FileUtils() {
    }

    /**
     * 获得文件大小
     */
    public static long getFineSize(File input) {
        if (input != null && input.exists()) {
            try (InputStream in = new FileInputStream(input)) {
                return in.available();
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("getFineSize error:'{}'", e.getMessage());
                }
                log.error("getFineSize", e);
            }
        }
        return 0;
    }

    /**
     * 根据视频文件获得视频截图文件
     */
    public static File getSrceentshotOutputByInput(File input) {
        if (input != null && input.exists()) {
            File outputPath = new File(input.getParent() + File.separator + PATH_SRCEENTSHOT);
            if (createDirectory(outputPath)) {
                return new File(
                    outputPath.getAbsolutePath() + File.separator + EncriptUtils.md5(input.getAbsolutePath())
                        .substring(0, 6).toLowerCase() + EXTEND_JPG);
            }
        }
        return null;
    }

    /**
     * 根据视频文件获得HLS的m3u8文件
     */
    public static File getM3U8OutputByInput(File input) {
        String videoName = getFileName(input);
        if (StringUtils.isNotEmpty(videoName)) {
            File outputPath = new File(input.getParent() + File.separator + PATH_HLS + File.separator + videoName);
            if (createDirectory(outputPath)) {
                return new File(outputPath.getAbsolutePath() + File.separator + videoName + EXTEND_M3U8);
            }
        }
        return null;
    }

    /**
     * 通过m3u8文件获得该目录下的所有ts文件
     */
    public static List<File> findTS(File m3u8) {
        if (m3u8 != null && m3u8.exists()) {
            final File path = m3u8.getParentFile();
            if (path.isDirectory()) {
                FilenameFilter filter = (dir, name) -> name.endsWith(EXTEND_TS);
                final File[] files = path.listFiles(filter);
                if (files == null) {
                    return Collections.emptyList();
                }
                return Lists.newArrayList(files);
            }
        }
        return Collections.emptyList();
    }

    /**
     * 根据视频文件获得生成的mp4文件地址
     */
    public static File getMp4OutputByInput(File input) {
        String videoName = getFileName(input);
        if (null == videoName) {
            videoName = "";
        }
        return new File(input.getParent() + File.separator + videoName + EXTEND_MP4);
    }

    /**
     * 根据视频文件获得生成的mp3文件地址
     */
    public static File getMp3OutputByInput(File input) {
        String videoName = getFileName(input);
        if (StringUtils.isNotEmpty(videoName) && !"mp3".equals(getFileExtend(input))) {
            return new File(input.getParent() + File.separator + videoName + EXTEND_MP3);
        }
        return null;
    }

    /**
     * 根据视频文件获得生成的mav文件地址
     */
    public static File getWAVOutputByInput(File input) {
        String videoName = getFileName(input);
        if (StringUtils.isNotEmpty(videoName) && !"wav".equals(getFileExtend(input))) {
            return new File(input.getParent() + File.separator + videoName + EXTEND_WAV);
        }
        return null;
    }

    /**
     * 创建目录
     */
    public static boolean createDirectory(String path) {
        if (StringUtils.isNotEmpty(path)) {
            return createDirectory(new File(path));
        }
        return false;
    }

    /**
     * 创建目录
     */
    public static boolean createDirectory(File path) {
        if (path.exists()) {
            return path.isDirectory();
        } else {
            return path.mkdirs();
        }
    }

    /**
     * 获得文件名
     */
    public static String getFileName(File file) {
        if (file != null && file.exists() && file.isFile()) {
            if (file.getName().lastIndexOf(".") > 0) {
                return file.getName().substring(0, file.getName().lastIndexOf(".")).toLowerCase();
            } else {
                return file.getName();
            }

        }
        return null;
    }

    /**
     * 获得文件扩展名
     */
    public static String getFileExtend(File file) {
        if (file != null && file.exists() && file.isFile() && file.getName().lastIndexOf(".") > 0) {
            return file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
        }
        return null;
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful. If a deletion fails, the method stops attempting
     * to delete and returns "false".
     */
    public static boolean deleteDir(File dir) {
        return FileSystemUtils.deleteRecursively(dir);
    }

    /**
     * 获取文件的大小,转换为M
     */
    public static String bytes2Mb(long bytes) {
        BigDecimal filesize = new BigDecimal(bytes);
        BigDecimal megabyte = new BigDecimal(1024 * 1024);
        float returnValue = filesize.divide(megabyte, 2, RoundingMode.UP).floatValue();
        return returnValue + "";
    }

    public static void writeFile(String srcPath, OutputStream destination) {
        byte[] buff = new byte[1024];
        int len;
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcPath));
            BufferedOutputStream bos = new BufferedOutputStream(destination)) {
            while ((len = bis.read(buff)) != -1) {
                bos.write(buff, 0, len);
            }
        } catch (Exception e) {
            log.error("写文件失败！");

        }
    }

    public static void writeFile(InputStream src, OutputStream destination) {
        byte[] buff = new byte[1024];
        int len;
        try (BufferedInputStream bis = new BufferedInputStream(src);
            BufferedOutputStream bos = new BufferedOutputStream(destination)) {
            while ((len = bis.read(buff)) != -1) {
                bos.write(buff, 0, len);
            }
        } catch (Exception e) {
            log.error("写文件失败！");
        }
    }

    public static void writeFile(byte[] data, String outPath) {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outPath))) {
            bos.write(data);
        } catch (Exception e) {
            log.error("写文件失败！");
        }
    }

    /**
     * 通过NiO复制文件到指定的目录中
     * @param newName   新文件名称
     * @param flag      是否再需要加一层文件夹  例如  rename[i] = 测B1234_time  最后会在directory再生成 测B1234文件夹
     */
    public static void copyFileUsingFileChannels(File source, String newName, boolean flag, String directory)
        throws Exception {
        String fileName;
        String temp;
        try (FileChannel in = new FileInputStream(source).getChannel()) {
            File file = new File(directory);
            mkdir(file, true);
            if (!StringUtils.isEmpty(newName)) {
                fileName = newName;
                if (flag) {
                    temp = fileName.split("_")[0];
                    if (!file.getAbsolutePath().contains(temp)) {
                        file = new File(directory + File.separator + temp);
                        mkdir(file, true);
                    }
                }
            } else {
                fileName = source.getName();
            }
            final String targetFile = file.getAbsolutePath() + File.separator + fileName;
            try (FileChannel out = new FileOutputStream(targetFile).getChannel()) {
                out.transferFrom(in, 0, in.size());
            }
        }
    }

    public static void mkdir(final File dir, final boolean createDirectoryIfNotExisting) throws IOException {
        // commons io FileUtils.forceMkdir would be useful here, we just want to omit this dependency
        if (!dir.exists()) {
            if (!createDirectoryIfNotExisting) {
                throw new IOException("The directory " + dir.getAbsolutePath() + " does not exist.");
            }
            if (!dir.mkdirs()) {
                throw new IOException("Could not create directory " + dir.getAbsolutePath());
            }
        }
        if (!dir.isDirectory()) {
            throw new IOException("File " + dir + " exists and is not a directory. Unable to create directory.");
        }
    }

}
