package com.zw.adas.domain.riskManagement.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.UuidUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;


/**
 * <p>
 * Title: 多媒体Form
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 *
 * @version 1.0
 * @author: wangying
 * @date 2017年4月13日下午6:11:38
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AdasMediaForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer type;//多媒体类型  （0：图像；1：音频；2：视频）

    private Integer formatCode;//多媒体格式编码 （0：JPEG 1：TIF 2：MP3 3：WAV 4：WMV）

    private Integer eventCode;//事件项编码 （0：平台下发指令；1：定时动作；2：抢劫报警触发；3：碰撞侧翻报警触发）

    private Integer wayId;//通道ID

    private String vehicleId;//车辆ID

    private String mediaName; // 名称

    private String mediaUrl; // 路径

    private Long mediaId; //多媒体id

    private String riskNum;

    private String visitId;

    private String description;

    /**
     * 风险id
     */
    private String riskId;

    /**
     * 多媒体对应的来源（0：风险事件；1：风险）
     */
    private Short source;

    /**
     * 风险事件Id
     */
    private String riskEventId;

    /**
     * 新的media_url
     */
    private String mediaUrlNew;

    //处理人
    private String dealer;

    private String driverName;

    //风险时间类型
    private Integer eventId;

    //报警地址
    private String address;

    private String riskNumber;

    private String eventNumber;

    private Long riskTime;

    //插入hbase的字节数组id
    private byte[] idStr;

    private Long createTime = System.currentTimeMillis();

    //风险类型
    private String riskType;

    private Date createDataTime = new Date();

    private Integer riskLevel;

    private String createDataUsername;

    private String brand;

    //回访次数
    private Integer visitTimes;

    private Integer riskResult;

    //协议类型（1黑标，12川标，13冀标）
    private Integer protocolType;

    //
    private Long fileSize;

    private Integer status;

    private Integer result;

    private Double direction;

    public static AdasMediaForm of(int status, String dealer, Integer result, String id) {
        final AdasMediaForm form = new AdasMediaForm();
        form.setIdStr(UuidUtils.getBytesFromStr(id));
        form.setStatus(status);
        form.setDealer(dealer);
        form.setResult(result);
        return form;
    }
}
