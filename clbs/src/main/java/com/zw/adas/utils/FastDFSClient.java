package com.zw.adas.utils;

import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.domain.upload.FastImageFile;
import com.github.tobato.fastdfs.exception.FdfsServerException;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.StringUtil;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 @Author zhengjc
 @Date 2019/6/13 20:54
 @Description fastdfs工具类
 @version 1.0
 */

@Component
public class FastDFSClient {

    private final Logger logger = LoggerFactory.getLogger(FastDFSClient.class);

    @Autowired
    @Qualifier("defaultFastFileStorageClient")
    private FastFileStorageClient storageClient;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Value("${system.ssl.enable:false}")
    private boolean sslEnabled;

    /**
     * 上传文件
     * @param file 文件对象
     * @return 文件访问地址
     * @throws IOException
     */
    public String uploadFile(MultipartFile file) throws IOException {
        StorePath storePath = storageClient
            .uploadFile(file.getInputStream(), file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()),
                null);
        return getResAccessUrl(storePath, true);
    }

    public String uploadFile(InputStream stream, long fileSize, String originalFilename) {
        StorePath storePath =
            storageClient.uploadFile(stream, fileSize, FilenameUtils.getExtension(originalFilename), null);
        return getResAccessUrl(storePath, false);
    }

    /**
     * 上传文件
     * @param file 文件对象
     * @return 文件访问地址
     * @throws IOException
     */
    public String uploadFile(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        StorePath storePath =
            storageClient.uploadFile(inputStream, file.length(), FilenameUtils.getExtension(file.getName()), null);
        return getResAccessUrl(storePath, true);
    }

    /**
     * 将一段字符串生成一个文件上传
     * @param content       文件内容
     * @param fileExtension
     * @return
     */
    public String uploadFile(String content, String fileExtension) {
        byte[] buff = content.getBytes(Charset.forName("UTF-8"));
        ByteArrayInputStream stream = new ByteArrayInputStream(buff);
        StorePath storePath = storageClient.uploadFile(stream, buff.length, fileExtension, null);
        return getResAccessUrl(storePath, true);
    }

    /**
     * 封装图片完整URL地址
     * @param storePath
     * @return
     */
    private String getResAccessUrl(StorePath storePath, boolean isNeedServerUrl) {
        String fileUrl;
        if (isNeedServerUrl) {
            fileUrl = fdfsWebServer.getWebServerUrl() + storePath.getFullPath();
        } else {
            fileUrl = storePath.getFullPath();
        }
        return fileUrl;
    }

    /**
     * 下载文件
     * @param fileUrl 文件URL
     * @return 文件字节
     */
    public byte[] downloadFile(String fileUrl) {
        String group = getGroupName(fileUrl);
        String path = getPath(fileUrl);
        DownloadByteArray downloadByteArray = new DownloadByteArray();
        return storageClient.downloadFile(group, path, downloadByteArray);
    }

    /**
     * 判断文件是否存在
     * @param fileUrl
     * @return
     */
    public boolean existFile(String fileUrl) {
        boolean result;
        String group = getGroupName(fileUrl);
        String path = getPath(fileUrl);
        try {
            FileInfo fileInfo = storageClient.queryFileInfo(group, path);
            result = fileInfo != null;
        } catch (FdfsServerException e) {
            logger.error("找不到节点或文件", e);
            result = false;
        }
        return result;
    }

    private String getGroupName(String fileUrl) {
        return fileUrl.substring(0, fileUrl.indexOf("/"));
    }

    private String getPath(String fileUrl) {
        return fileUrl.substring(fileUrl.indexOf("/") + 1);
    }

    /**
     * 删除文件
     * @param fileUrl
     */
    public void deleteFile(String fileUrl) {
        try {
            if (existFile(fileUrl)) {
                storageClient.deleteFile(fileUrl);
            }
        } catch (Exception e) {
            logger.error("从fastDfs服务器删除图片失败,路径为: " + fileUrl);
        }
    }

    public FastImageFile uploadThumbImage(InputStream inputStream, File file) {
        Set<MetaData> metaData = createMetaData();
        String fileExtName = FilenameUtils.getExtension(file.getName());
        return new FastImageFile.Builder().withThumbImage(200, 200).withFile(inputStream, file.length(), fileExtName)
            .withMetaData(metaData).build();
    }

    /**
     * 按照默认方式生成缩率图
     * @param stream           stream
     * @param fileSize         fileSize
     * @param originalFilename originalFilename
     * @return
     */
    public String uploadThumbImage(InputStream stream, long fileSize, String originalFilename) {
        Set<MetaData> metaData = createMetaData();
        StorePath storePath = storageClient
            .uploadImageAndCrtThumbImage(stream, fileSize, FilenameUtils.getExtension(originalFilename), metaData);
        return getResAccessUrl(storePath, false);
    }

    /**
     * 生成元数据
     * @return 元数据集合
     */
    private Set<MetaData> createMetaData() {
        Set<MetaData> metaDataSet = new HashSet<MetaData>();
        metaDataSet.add(new MetaData("Author", "zwkj"));
        metaDataSet.add(new MetaData("CreateDate", LocalDateUtils.dateFormate(new Date())));
        return metaDataSet;
    }

    public String getWebAccessUrl(String filePath) {
        return fdfsWebServer.getWebServerUrl() + filePath;
    }

    /**
     * 获取绝对的url地址
     * group1/M00/14/31/wKgYkF_TRnKAKhnzAA-HUgfrlhM179.MP4-> /group1/M00/14/31/wKgYkF_TRnKAKhnzAA-HUgfrlhM179.MP4 --—>
     * group1/M00/14/31/wKgYkF_TRnKAKhnzAA-HUgfrlhM179.MP4-> http://192.168.24.144:8798/group1/M00/14/31/wKgYkF_TRnKAKhnzAA-HUgfrlhM179.MP4
     * @return
     */
    public String getAccessUrl(String filePath) {
        if (StringUtil.isNullOrBlank(filePath)) {
            return "";
        }
        if (sslEnabled) {
            return "/" + filePath;
        } else {
            return fdfsWebServer.getWebServerUrl() + filePath;
        }
    }

    /**
     * 获取fastdfs相对地址，把group前面的一段截取掉，只保留后面一段
     * /group1/M00/14/31/wKgYkF_TRnKAKhnzAA-HUgfrlhM179.MP4 --—>group1/M00/14/31/wKgYkF_TRnKAKhnzAA-HUgfrlhM179.MP4
     * http://192.168.24.144:8798/group1/M00/14/31/wKgYkF_TRnKAKhnzAA-HUgfrlhM179.MP4 --—>group1/M00/14/31/wKgYkF_TRnKAKhnzAA-HUgfrlhM179.MP4
     * @param filePath
     * @return
     */
    public String getGroupUrl(String filePath) {
        if (StringUtil.isNullOrBlank(filePath)) {
            return "";
        }
        //如果就是正确的group1/M00/14/31/wKgYkF_TRnKAKhnzAA-HUgfrlhM179.MP4直接返回
        if (!filePath.startsWith("/")) {
            return filePath;
        }
        int length = filePath.length();
        if (sslEnabled) {
            return filePath.substring(1, length);
        } else {
            return filePath.substring(fdfsWebServer.getWebServerUrl().length(), length);
        }
    }
}
