package com.zw.adas.domain.driverStatistics.bean;

import com.zw.protocol.msg.t809.T809MsgBody;
import lombok.Data;

/***
 @Author zhengjc
 @Date 2019/10/1 19:11
 @Description 人脸对比上报结果实体
 @version 1.0
 **/
@Data
public class AdasFaceCheckAuto implements T809MsgBody {

    /**
     * 0 匹配成功 1 匹配失败 2 超时 3 没有启用该功能 4 连接异常 5 无人脸识别图片
     */
    private Integer result;

    /**
     * 比对相似阀值
     */
    private Integer threshold = 80;

    /**
     * 比对相似度
     */
    private Integer similarity = 0;

    /**
     * 比对类型 0 插卡比对 1 巡检比对 2 点火比对 3 离开返回时比对
     */
    private Integer compareType = 0;

    /**
     * 人脸识别比对时位置信息
     */
    private byte[] position =
        { 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00,
            00 };

    /**
     * 图片类型
     */
    private int photoType = 0;

    /**
     * 图片数据包
     */
    private byte[] photo;

    /**
     * 沪标定义的字段
     */
    private Integer photoLen = 0;
    /**
     * zw标准定义的字段
     */
    private Integer photoLength = 0;

    public static AdasFaceCheckAuto getInstance() {
        AdasFaceCheckAuto adasFaceCheckAuto = new AdasFaceCheckAuto();
        adasFaceCheckAuto.result = 5;
        adasFaceCheckAuto.compareType = 0;
        adasFaceCheckAuto.photoType = 0;
        adasFaceCheckAuto.threshold = 80;
        return adasFaceCheckAuto;

    }

    public void initPhotoLength() {
        if (photo != null) {
            photoLen = photo.length;
            photoLength = photoLen;
        }
    }

}
