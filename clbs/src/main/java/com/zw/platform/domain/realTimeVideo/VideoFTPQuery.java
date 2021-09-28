package com.zw.platform.domain.realTimeVideo;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class VideoFTPQuery extends BaseFormBean {
    private String vehicleId; // 监控对象Id

    private String tempName; // 原始文件名

    private String tempUrl; // 原始文件路径

    private Date uploadTime; // 上传时间

    private String name; // 文件名

    private String url; // 文件路径

    private String downUrl;// 下载路径

    private Date startTime; // 开始时间

    private Date endTime; // 结束时间

    private Long alarmType; // 报警类型

    private String fileSize; // 文件大小

    private Integer channelNumber; // 通道号

    private Integer physicsChannel; //物理通道号

    private Integer type; // 存储类型  0：文件上传；1：视频服务器存储

    /**
     * 初始化相关通道号信息
     * @param logicPhysicsChannelMap
     */
    public void initChannelNumber(Map<Integer, Integer> logicPhysicsChannelMap) {
        physicsChannel = logicPhysicsChannelMap.get(channelNumber);
    }
}
