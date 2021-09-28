package com.zw.platform.service.realTimeVideo;

import com.alibaba.fastjson.JSONArray;
import com.zw.platform.domain.realTimeVideo.FileUploadControlForm;
import com.zw.platform.domain.realTimeVideo.FileUploadForm;
import com.zw.platform.domain.realTimeVideo.FtpBean;
import com.zw.platform.domain.realTimeVideo.ResourceListBean;
import com.zw.platform.domain.realTimeVideo.ResourceListBeanVO;
import com.zw.platform.domain.realTimeVideo.VideoFTPForm;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.commons.net.ftp.FTPClient;


/**
 * 实时视频资源列表
 */
public interface ResourceListService {

    /**
     * 直接查询FTP获取资源列表
     * @param resourceListBean 资源列表实体
     */
    ResourceListBeanVO getForFtp(ResourceListBean resourceListBean) throws Exception;

    /**
     * 下发9205获取资源列表
     * @param resourceListBean 资源列表实体
     */
    JsonResultBean sendResourceList(ResourceListBean resourceListBean, String ipAddress);

    /**
     * 获取历史轨迹数据
     */
    String getHistory(String vehicleId, String startTime, String endTime) throws Exception;


    /**
     * 获取FTP服务器名
     */
    FtpBean getFtpName();

    /**
     * 文件下载
     * @param ftpName 文件名
     * @param downloadPath 下载路径
     * @param directory 跳转路径
     * @param destFileName  要下载的文件名
     */
    boolean fileDownload(String ftpName, String downloadPath, String directory, String destFileName);

    /**
     * 808车辆树组装
     */
    JSONArray getAlarm808();

    /**
     * 下发文件上传指令(0x9206)
     * @param form
     *            文件上传实体
     * @param ipAddress
     *            客户端的IP地址
     */
    JsonResultBean sendUploadOrder(FileUploadForm form, String ipAddress);

    /**
     * 文件上传控制指令(0x9207)
     */
    JsonResultBean sendControlOrder(FileUploadControlForm form, String ipAddress);

    /**
     * 组装ftp记录存储的文件路径
     */
    String getFTPUrl(String vid, String startTime, Integer channelNum, Long alarmType);

    /**
     * 存储ftp记录
     */
    void insertFTPRecord(VideoFTPForm videoFTPForm);

    /**
     * 创建ftp路径
     * @param ftpName ftp服务器名称
     * @param ftpUrl 创建路径
     */
    boolean createFTPUrl(String ftpName, String ftpUrl);

    /**
     * 修改ftp文件，将临时文件转存至指定文件夹，并重新命名
     * @param ftpClient ftp服务器名称
     * @param tempUrl  临时文件
     * @param newFileName 新文件名称
     * @param newUrl 新文件url
     */
    void motifyFtpFile(FTPClient ftpClient, String tempName, String tempUrl, String newFileName,
        String newUrl) throws Exception;

    /**
     * 获取ftp客户端
     * @param ftpName ftp服务器名称
     * @param path 指定进入路径
     */
    FTPClient getFTPClient(String ftpName, String path);

    /**
     * 下发9205参数获取资源列表
     * @param isSub 是否订阅    809过检时传入flase
     */
    Integer sendMsg(ResourceListBean resourceListBean, boolean isSub);
    
    String checkFtp() throws Exception;

    /**
     * 下发920f获取资源日期
     * @param resourceListBean 资源列表实体
     */
    JsonResultBean sendList(ResourceListBean resourceListBean, String ipAddress);

}
