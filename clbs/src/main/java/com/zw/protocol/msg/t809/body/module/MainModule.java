package com.zw.protocol.msg.t809.body.module;


import com.zw.protocol.msg.MsgBean;

/**
 * Created by LiaoYuecai on 2017/2/13.
 */
public class MainModule implements MsgBean {
    protected String vehicleNo;
    protected Integer vehicleColor;
    protected Integer dataType;
    protected Integer dataLength;

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }

    public Integer getVehicleColor() {
        return vehicleColor;
    }

    public void setVehicleColor(Integer vehicleColor) {
        this.vehicleColor = vehicleColor;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public Integer getDataLength() {
        return dataLength;
    }

    public void setDataLength(Integer dataLength) {
        this.dataLength = dataLength;
    }
}
