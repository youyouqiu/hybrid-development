package com.zw.platform.util.common;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FtpClientUtil {
    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(FtpClientUtil.class);

    public static final String FTP_NAME = "FTP服务器";

    public static String getRealPath() {
        return realPath;
    }

    public static void setRealPath(String realPath) {
        FtpClientUtil.realPath = realPath;
    }

    public static String realPath;

    public static boolean uploadFile(String url, int port, String username, String password, String directory,
        String filename, InputStream input) throws Exception {
        boolean success;
        FTPClient ftp = null;
        try {
            ftp = getFTPClient(username, password, url, port, directory);
            File file = new File(directory);
            if (!file.exists()) {
                FtpClientUtil.createDir(directory, ftp);
            }
            success = ftp.storeFile(filename, input);
            ftp.logout();
        } finally {
            closeFtpConnect(ftp);
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return success;
    }

    public static FTPClient getFTPClient(String username, String password, String url, int port, String directory) {
        FTPClient ftp = new FTPClient();
        try {
            int reply;
            // 设置ftp部署在linux上
            ftp.configure(new FTPClientConfig(FTPClientConfig.SYST_UNIX));
            //设置连接和控制通道超时时间为10秒
            ftp.setDefaultTimeout(10000);
            ftp.connect(url, port);// 连接FTP服务器
            // 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftp.login(username, password);// 登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                log.error("FTP不是被动连接");
                throw new FTPException("FTP连接失败，请检查");
            }
            // 设置缓冲区
            ftp.setBufferSize(1024);
            // 跳转目录
            ftp.changeWorkingDirectory(directory);
            // 二进制文件
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            // 开启被动模式
            ftp.enterLocalPassiveMode();
            //设置数据通道超时时间为10秒
            ftp.setDataTimeout(10000);
            return ftp;
        } catch (SocketException e) {
            log.error("FTPClient获取失败", e);
            throw new FTPException("FTP连接失败，请检查");
        } catch (IOException e) {
            log.error("FTPClient获取失败", e);
            throw new FTPException("FTP操作失败，请检查");
        }
    }

    public static void closeFtpConnect(FTPClient ftp) {
        try {
            if (ftp != null && ftp.isConnected()) {
                ftp.disconnect();
            }
        } catch (IOException e) {
            throw new RuntimeException("关闭FTP连接发生异常！", e);
        }
    }

    /**
     * FTP单个文件下载,并自定义名称
     *
     * @param ftpUrl       ftp地址
     * @param username     ftp的用户名
     * @param password     ftp的密码
     * @param directory    跳转目录
     * @param destFileName 要下载的文件名
     * @param downloadPath 存储下载的文件名全路径
     */
    public static boolean download(String ftpUrl, String username, int port, String password, String directory,
        String downloadPath, String destFileName, String fileName) throws Exception {
        FTPClient ftp = null;
        boolean result = false;
        byte[] buff;
        try {
            // 快速失败
            if (StringUtils.isBlank(downloadPath)) {
                throw new Exception("存储下载文件的路径不能为空!");
            }
            ftp = getFTPClient(username, password, ftpUrl, port, directory);
            Files.createDirectories(Paths.get(downloadPath));
            try (InputStream inputStream = ftp.retrieveFileStream(new String(destFileName.getBytes(), "iso8859-1"));
                 OutputStream out = new FileOutputStream(downloadPath + File.separator + fileName)) {
                if (inputStream != null) {
                    buff = new byte[1024];
                    int rc;
                    while ((rc = inputStream.read(buff)) > 0) {
                        out.write(buff, 0, rc);
                    }
                    result = true;
                } else {
                    log.info(destFileName + " 文件在" + ftpUrl + "中的" + directory + "目录中不存在!");
                }
            }
            return result;
        } catch (Exception e) {
            log.info(">=======ftpClient下载文件:" + destFileName + " 错误============<", e);
            throw e;
        } finally {
            try {
                if (ftp != null) {
                    ftp.disconnect();
                }
            } catch (IOException e) {
                log.error("关闭FTP连接发生异常！", e);
            }
        }
    }

    /**
     * 获取ftp上面文件的流
     */
    public static InputStream getFileInputStream(String ftpUserName, String ftpPassword, String ftpHost, int ftpPort,
        String ftpPath, String filePath) {
        FTPClient ftpClient = null;
        InputStream inputStream = null;
        try {
            ftpClient = getFTPClient(ftpUserName, ftpPassword, ftpHost, ftpPort, ftpPath);
            inputStream = ftpClient.retrieveFileStream(filePath);
        } catch (Exception e) {
            log.error("FTP获取文件流失败", e);
        } finally {
            closeFtpConnect(ftpClient);
        }
        return inputStream;
    }

    public static InputStream getFileInputStream(String ftpUserName, String ftpPassword, String ftpHost, int ftpPort,
        String ftpFilePath) {
        //应用于直接文件存的就是ftp的绝对路径，不是ftp根目录的子目录
        String ftpRootPath = "";
        return getFileInputStream(ftpUserName, ftpPassword, ftpHost, ftpPort, ftpRootPath, ftpFilePath);
    }

    /**
     * 创建目录(有则切换目录，没有则创建目录)
     */
    public static boolean createDir(String dir, FTPClient ftp) throws Exception {
        if (StringUtils.isBlank(dir)) {
            return true;
        }
        // 目录编码，解决中文路径问题
        String path = new String(dir.getBytes("GBK"), StandardCharsets.ISO_8859_1);
        // 尝试切入目录
        if (ftp.changeWorkingDirectory(path)) {
            return true;
        }
        final String rootPath = "/";
        if (path.startsWith(rootPath)) {
            ftp.changeWorkingDirectory(rootPath);
        }
        final String pathSeparator = "/";
        String[] directories = path.split(pathSeparator);
        // 循环生成子目录
        for (String directory : directories) {
            // 如果/出现在path的首尾或连续出现，directory就是空的，这种情况可以直接忽略
            if (StringUtils.isEmpty(directory)) {
                continue;
            }
            // 尝试切入目录
            if (ftp.changeWorkingDirectory(directory)) {
                continue;
            }
            if (!ftp.makeDirectory(directory)) {
                log.info("[失败]ftp创建目录：{}, ", directory);
                return false;
            }
            ftp.changeWorkingDirectory(directory);
            log.info("[成功]创建ftp目录：" + directory);
        }
        // 将目录切换至指定路径
        return ftp.changeWorkingDirectory(path);
    }

    public static String getFileName(String path, FTPClient ftp) throws Exception {
        if (ftp == null || StringUtils.isBlank(path)) {
            return "";
        }
        // 获得指定目录下所有文件名
        FTPFile[] ftpFiles = ftp.listFiles(path);
        if (ftpFiles.length == 0) {
            return "";
        }
        return new String(ftpFiles[0].getName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    /**
     * 判断Ftp目录是否存在
     */
    public static boolean existDirectory(FTPClient ftpClient, String dir) {
        try {
            return ftpClient.changeWorkingDirectory(dir);
        } catch (IOException e1) {
            return false;
        }
    }

    public static boolean deletePath(FTPClient ftpClient, String dir, String filePath) {
        try {
            if (ftpClient != null) {
                if (StringUtils.isNotBlank(dir)) { // 文件路径不为空
                    String fileName = getFileName(dir, ftpClient);
                    // 删除文件
                    if (StringUtils.isNotBlank(fileName)) {
                        ftpClient.changeWorkingDirectory(dir);
                        ftpClient.deleteFile(new String((dir + "/" + filePath).getBytes("iso8859-1")));
                    }
                    ftpClient.removeDirectory(new String(dir.getBytes("iso8859-1")));
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 复制文件.
     */
    public static void copyFile(String sourceFileName, String targetFileName, String sourceDir, String targetDir,
        FTPClient ftpClient) throws Exception {
        if (ftpClient != null && StringUtils.isNotBlank(sourceFileName) && StringUtils.isNotBlank(targetFileName)
            && StringUtils.isNotBlank(sourceDir) && StringUtils.isNotBlank(targetDir)) {
            if (!existDirectory(ftpClient, targetDir)) {
                createDir(targetDir, ftpClient);
            }
            ftpClient.setBufferSize(1024 * 2);
            // 变更工作路径
            ftpClient.changeWorkingDirectory(sourceDir);
            // 设置以二进制流的方式传输
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            final String sourceName = new String(sourceFileName.getBytes("GBK"), StandardCharsets.ISO_8859_1);
            final String targetName = new String(targetFileName.getBytes("GBK"), StandardCharsets.ISO_8859_1);
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                ftpClient.retrieveFile(sourceName, bos);
                try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray())) {
                    // 将文件读到内存中
                    ftpClient.changeWorkingDirectory(targetDir);
                    ftpClient.storeFile(targetName, bis);
                }
            }
        }
    }

}