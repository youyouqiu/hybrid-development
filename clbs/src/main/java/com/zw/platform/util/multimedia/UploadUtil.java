package com.zw.platform.util.multimedia;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * 多媒体上传工具 @author  Tdz
 * @create 2017-08-31 9:21
 **/
public class UploadUtil {
    public static final Logger logger = LogManager.getLogger(UploadUtil.class);

    public static boolean uploadFile(String f, String host, int port) {
        try {
            new UploadFileHandler(f, host, port).run();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 读取某个文件夹下的所有文件
     */
    public static List<File> readfile(String filepath) throws FileNotFoundException, IOException {
        List<File> flist = new ArrayList<>();
        try {

            File file = new File(filepath);
            if (file.isDirectory()) {
                String[] filelist = file.list();
                for (int i = 0; i < filelist.length; i++) {
                    File readfile = new File(filepath + "\\" + filelist[i]);
                    if (!readfile.isDirectory()) {
                        flist.add(readfile);
                    } else if (readfile.isDirectory()) {
                        readfile(filepath + "\\" + filelist[i]);
                    }
                }
            }

        } catch (FileNotFoundException e) {
            logger.info("readfile()   Exception:" + e.getMessage());
        }
        return flist;
    }

    /**
     * 删除文件，可以是文件或文件夹
     * @param fileName 要删除的文件名
     * @return 删除成功返回true，否则返回false
     */
    public static boolean delete(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            logger.error("删除文件失败:" + fileName + "不存在！");
            return false;
        } else {
            if (file.isFile()) {
                return deleteFile(fileName);
            } else {
                return deleteDirectory(fileName);
            }
        }
    }

    /**
     * 删除单个文件
     * @param fileName 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            try {
                if (file.delete()) {
                    logger.info("删除单个文件" + fileName + "成功！");
                    return true;
                } else {
                    logger.error("删除单个文件" + fileName + "失败！");
                    return false;
                }
            } catch (Exception e) {
                logger.error("删除单个文件异常", e);
            }
        } else {
            logger.error("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
        return false;
    }

    /**
     * 删除目录及目录下的文件
     * @param dir 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String dir) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dir.endsWith(File.separator)) {
            dir = dir + File.separator;
        }
        File dirFile = new File(dir);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            logger.error("删除目录失败：" + dir + "不存在！");
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            } else if (files[i].isDirectory()) {
                // 删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            }
        }
        if (!flag) {
            logger.error("删除目录失败！");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            logger.error("删除目录" + dir + "成功！");
            return true;
        } else {
            return false;
        }
    }

    private static class UploadFileHandler extends Thread {

        // 客户端Socket
        private Socket client;

        private String filePath;

        public UploadFileHandler(String filePath, String host, int port) {
            try {
                // 需要上传的本地文件
                this.filePath = filePath;
                // 连接到服务器端
                this.client = new Socket(host, port);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            byte[] buf = new byte[1024];
            int len = -1;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                File file = new File(filePath);
                if (file.exists()) {
                    // 建立文件输入流，读取需要上传的数据
                    bis = new BufferedInputStream(new FileInputStream(file));
                    // 建立输出流用于将文件数据上传到服务器端
                    bos = new BufferedOutputStream(client.getOutputStream());

                    /**
                     * 发送操作标志：服务器需要判断入站操作时上传文件还是下载文件 将操作标志发给服务端解析处理 TextServer.CLIENT_FILE_UPLOAD : 上传操作
                     * TextServer.CLIENT_FILE_DOWNLOAD : 下载操作
                     */
                    String uploadsring = filePath;
                    byte[] uploadbyte = uploadsring.getBytes();
                    bos.write(uploadbyte, 0, uploadbyte.length);
                    bos.flush();

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // 当发送完操作标识符以后，开始读文件数据，并且往服务端上传数据
                    while ((len = bis.read(buf)) > 0) {
                        bos.write(buf, 0, len);
                        bos.flush();
                    }
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if (client != null) {
                    try {
                        client.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }

    }

}
