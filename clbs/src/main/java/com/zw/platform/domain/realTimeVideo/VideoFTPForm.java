package com.zw.platform.domain.realTimeVideo;

import java.util.Date;

import com.zw.platform.util.common.BaseFormBean;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

/**
 * ftp文件记录实体
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class VideoFTPForm extends BaseFormBean {

    private String vehicleId; // 监控对象Id

    private String name; // 文件名

    private String url; // 文件路径

    private String tempName; // 原始文件名

    private String tempUrl; // 原始文件路径

    private Date startTime; // 开始时间

    private Date endTime; // 结束时间

    private Date uploadTime;//上传时间

    private Long alarmType; // 文件上传路径

    private String fileSize; // 文件上传路径

    private Integer channelNumber; // 通道号

    private Integer type; // 存储类型  0：文件上传；1：视频服务器存储

    public boolean check() {
        return StringUtils.isNotBlank(vehicleId) && channelNumber != null
            && startTime != null && endTime != null;
    }
    
}
