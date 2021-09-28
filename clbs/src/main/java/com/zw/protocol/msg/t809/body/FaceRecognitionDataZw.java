package com.zw.protocol.msg.t809.body;

import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.util.ConstantUtil;
import com.zw.protocol.msg.MsgDesc;
import com.zw.protocol.msg.t809.T809MsgBody;
import lombok.Data;

/**
 * 功能描述:中位标准1241主动上报驾驶员人脸识别
 * @author zhengjc
 * @date 2020/5/7
 * @time 16:22
 */
@Data
public class FaceRecognitionDataZw implements T809MsgBody {
    /**
     * 车牌号
     */
    private String vehicleNo;
    /**
     * 车辆颜色
     */
    private Integer vehicleColor;

    /**
     * 子业务类型id即指令1241（这里固定是1241）
     */
    private Integer dataType;
    /**
     * 后续长度
     */
    private Integer dataLength;
    /**
     * 驾驶员姓名
     */
    private String driverName;
    /**
     * 驾驶员驾驶证号
     */
    private String driverId;
    /**
     * 驾驶员从业资格证号
     */
    private String licence;

    /**
     * 图片格式
     */
    private Integer photoType;
    /**
     * 图片长度
     */
    private Integer photoLength;
    /**
     * 图片长度
     */
    private byte[] photo;

    /**
     * 对应驾驶员人脸识别请求消息源子业务类型标识
     * 固定为0x9242
     */
    private Integer sourceDataType;
    /**
     * 对应驾驶员人脸识别请求消息源报文序列号对应message的
     * msgSNAck
     */
    private Integer sourceMsgSn;

    public static FaceRecognitionDataZw getInstance(byte[] photo, String driverName, VehicleDTO vehicleDTO,
        String drivingLicenseNo, String licence) {
        FaceRecognitionDataZw data = new FaceRecognitionDataZw();
        data.vehicleNo = vehicleDTO.getName();
        data.vehicleColor = vehicleDTO.getPlateColor();
        data.dataType = ConstantUtil.T809_UP_EXG_MSG_FACE_PHOTO_AUTO;
        //协议端自己计算
        data.dataLength = 0;
        data.driverName = driverName;
        data.driverId = drivingLicenseNo;
        data.licence = licence;
        //写死jpg
        data.photoType = 1;
        data.photoLength = photo.length;
        data.photo = photo;
        return data;
    }

    public FaceRecognitionDataZw assembleSourceInfo(MsgDesc desc) {
        this.sourceDataType = desc.getMsgID();
        this.sourceMsgSn = Integer.parseInt(desc.getMsgSNAck());
        return this;
    }
}
