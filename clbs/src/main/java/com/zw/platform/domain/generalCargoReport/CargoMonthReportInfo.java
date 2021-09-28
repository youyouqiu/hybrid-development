package com.zw.platform.domain.generalCargoReport;

import com.zw.platform.util.common.PrecisionUtils;
import lombok.Data;

/***
 @Author lijie
 @Date 2019/9/4 17:35
 @Description 普货月报表实体
 @version 1.0
 **/
@Data
public class CargoMonthReportInfo {

    private int serialNumber;
    private String groupId;
    private String groupName;
    private int time;
    private int vehicleTotal;
    private int addNumber;
    private int deleteNumber;
    private String onlineRate;
    private int onlineNumber;
    private int nerverOnlineNumber;
    private int speedNumber;
    private String speedRate;
    private int tiredNumber;
    private String tiredRate;


    public void setOnlineRate(float onlineRate) {
        this.onlineRate = PrecisionUtils.roundByScale(onlineRate * 100, 2) + "%";
    }

    public void setSpeedRate(float speedRate) {
        this.speedRate = PrecisionUtils.roundByScale(speedRate * 100, 2) + "%";
    }

    public void setTiredRate(float tiredRate) {
        this.tiredRate = PrecisionUtils.roundByScale(tiredRate * 100, 2) + "%";
    }
}
