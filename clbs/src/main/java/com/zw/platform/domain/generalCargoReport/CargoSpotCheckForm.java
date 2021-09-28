package com.zw.platform.domain.generalCargoReport;

import lombok.Data;

import java.io.Serializable;

/**
 * 普货抽查表实体
 * @author lijie
 * @date 2018/9/2 15:41
 */
@Data
public class CargoSpotCheckForm  implements Serializable {
    private static final long serialVersionUID = 1L;
    private byte [] vid;
    private String longtitude;
    private String latitude;

    private String vehicleId;
    private String brand;//车牌
    private String groupId;
    private String groupName;//组织名字
    private String time;//抽查时间
    private String address;//地址
    private String onlineFlag;//是否上线
    private String speed;//速度
    private String fatigueFlag;//是否疲劳
    private String speedFlag;//是否超速
    private String otherAlarm;//其他报警
    private String dealMeasure;//处理措施
    private String dealTime;//处理时间
    private String dealResult;//处理结果
    private String dealer;//处理人
    private String feedbackTime;//反馈时间


    //导出excel专用字段
    private String timeMin;
    private String timeSec;
    private String onlineYes = "□";
    private String onlineNo = "√";
    private String speedYes = "□";
    private String speedNo = "√";
    private String fatigueYes = "□";
    private String fatigueNo = "√";
    private String dealMin;
    private String dealSec;
    private String feedbackMin;
    private String feedbackSec;




    public void setOnlineFlag(String onlineFlag) {
        this.onlineFlag = onlineFlag;
        this.onlineYes = "√";
        this.onlineNo = "□";
    }

    public void setFatigueFlag(String fatigueFlag) {
        this.fatigueFlag = fatigueFlag;
        this.fatigueYes = "√";
        this.fatigueNo = "□";
    }

    public void setSpeedFlag(String speedFlag) {
        this.speedFlag = speedFlag;
        this.speedYes = "√";
        this.speedNo = "□";
    }
}
