package com.zw.ws.entity.t808.device;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 驾驶员识别管理下发指令 人脸识别信息实体
 * @Author Tianzhangxu
 * @Date 2020/9/30 10:24
 */
@Data
public class FaceInfo implements Serializable {
    private static final long serialVersionUID = 4993118918178306638L;

    /**
     * 人脸ID（从业人员ID）
     */
    private String faceId;

    /**
     * 从业人员姓名（鲁标必传）
     */
    private String name;

    /**
     * 从业资格证
     */
    private String certificate;

    /**
     * 人脸图片地址协议
     * 0:FTP
     * 1:HTTP
     */
    private Integer urlType;

    /**
     * 图片地址
     */
    private String pictureUrl;

    /**
     * 图片来源
     * 0:本机拍摄图片
     * 1:第三方图片
     */
    private Integer pictureSource;
}
