package com.zw.platform.util.common;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;


/**
 * ftp工具 @author  Tdz
 * @create 2017-10-18 17:50
 **/
public class FavFTPUtil {

    /**
     * Description: 向FTP服务器上传文件 @Version1.0 Jul 27, 2008 4:31:09 PM by 崔红保（cuihongbao@d-heaven.com）创建
     * @param url
     *            FTP服务器hostname
     * @param port
     *            FTP服务器端口
     * @param username
     *            FTP登录账号
     * @param password
     *            FTP登录密码
     * @param path
     *            FTP服务器保存目录
     * @param filename
     *            上传到FTP服务器上的文件名
     * @param input
     *            输入流
     * @return 成功返回true，否则返回false
     */
    public static boolean uploadFile(String url, int port, String username, String password, String path,
                                     String filename, InputStream input) {
        boolean success = false;
        FTPClient ftp = new FTPClient();
        try {
            int reply;
            ftp.connect(url, port);// 连接FTP服务器
            // 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftp.login(username, password);// 登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return success;
            }
            ftp.changeWorkingDirectory(path);
            ftp.storeFile(filename, input);
            input.close();
            ftp.logout();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        return success;
    }

    /**
     * 下载文件
     * @param hostname
     *            FTP服务器地址
     * @param port
     *            FTP服务器端口号
     * @param username
     *            FTP登录帐号
     * @param password
     *            FTP登录密码
     * @param pathname
     *            FTP服务器文件目录
     * @param filename
     *            文件名称
     * @param localpath
     *            下载后的文件路径
     * @return
     */
    public static boolean downloadFile(String hostname, int port, String username, String password, String pathname,
                                       String filename, String localpath) {
        boolean flag = false;
        FTPClient ftpClient = new FTPClient();
        InputStream in = null;
        try {
            // 连接FTP服务器
            ftpClient.connect(hostname, port);
            // 登录FTP服务器
            ftpClient.login(username, password);
            // 验证FTP服务器是否登录成功
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                return flag;
            }
            String[] pn = pathname.split("/");
            if (pn[2].contains("_")) {
                //之前中文乱码，切了个字符
                pathname = "/ADAS/" + pn[2];
            } else {
                pathname = "/ADAS/" + pn[pn.length - 1];
            }
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            // 切换FTP目录
            ftpClient.changeWorkingDirectory("/ADAS");
            File localFile = new File(localpath);
            // 如果文件夹不存在则创建
            if (!localFile.exists() && !localFile.isDirectory()) {
                localFile.mkdir();
            }
            File file = new File(localpath + "/" + filename);
            OutputStream os = new FileOutputStream(file);
            // ftpClient.retrieveFile(filename, os);
            in = ftpClient.retrieveFileStream(new String(pathname.getBytes("UTF-8"),"ISO8859-1"));
            if (in != null) {
                byte[] buff = new byte[1024];
                int rc = 0;
                while ((rc = in.read(buff)) > 0) {
                    os.write(buff, 0, rc);
                }
            } else {
                file.delete();
            }
            os.close();
            ftpClient.logout();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return flag;
    }
}
