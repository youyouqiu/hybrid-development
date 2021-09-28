package com.zw.protocol.msg.t809.body;

import com.alibaba.fastjson.JSONObject;
import com.zw.adas.domain.driverStatistics.bean.AdasFaceCheckAuto;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.protocol.msg.t809.T809MsgBody;
import lombok.Data;

/***
 @Author zhengjc
 @Date 2019/10/1 19:11
 @Description 人脸对比上报结果实体
 @version 1.0
 **/
@Data
public class AdasFaceCheckAutoInfo implements T809MsgBody {
    private String vehicleNo;
    private Integer vehicleColor;
    private Integer dataType;
    private Integer dataLength;
    private JSONObject data;

    public static AdasFaceCheckAutoInfo getInstance(BindDTO vehicleInfo, AdasFaceCheckAuto adasFaceCheckAuto) {
        //初始化长度
        adasFaceCheckAuto.initPhotoLength();
        // 消息体
        AdasFaceCheckAutoInfo adasFaceCheckAutoInfo = new AdasFaceCheckAutoInfo();
        adasFaceCheckAutoInfo.dataType = ConstantUtil.T809_UP_WARN_MSG_FACE_CHECK_AUTO;
        // 后续数据长度(协议端自己计算)
        adasFaceCheckAutoInfo.dataLength = 0;
        adasFaceCheckAutoInfo.vehicleNo = vehicleInfo.getName();
        adasFaceCheckAutoInfo.vehicleColor = vehicleInfo.getPlateColor();
        adasFaceCheckAutoInfo.data = MsgUtil.objToJson(adasFaceCheckAuto);
        return adasFaceCheckAutoInfo;
    }
}
