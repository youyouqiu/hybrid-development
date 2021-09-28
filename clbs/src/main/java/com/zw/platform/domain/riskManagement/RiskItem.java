package com.zw.platform.domain.riskManagement;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

/**
 * @author Chen Feng
 * @version 1.0 2018/5/10
 */
@Data
public class RiskItem {
    private String id;

    private byte[] riskId;

    private String vehicleId;// 车辆id

    private String brand;// 车牌号

    private String riskNumber;// 风险编号

    private String riskLevel;// 风险等级

    private String riskType;// 风险类型

    private String status;// 风险状态

    private String riskStatus;

    private String vuuid = UUID.randomUUID().toString(); // 回访UUID

    private Date warningTime; // 开始时间

    private Date visitTime;

    private Integer videoFlag;

    private Integer picFlag;

    private String weather;

    private String picHtml;

    private String videoHtml;

    private String address;

    public String getPicHtml() {
        if (Integer.valueOf(1).equals(getPicFlag())) {
            return "<span class=\"risk_img\" />";
        }
        return picHtml;
    }

    public String getVideoHtml() {
        if (Integer.valueOf(1).equals(getVideoFlag())) {
            return "<span class=\"risk_video\" />";
        }
        return videoHtml;
    }
}
