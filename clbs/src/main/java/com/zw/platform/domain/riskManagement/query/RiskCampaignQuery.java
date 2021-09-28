package com.zw.platform.domain.riskManagement.query;


import com.zw.platform.domain.basicinfo.ProfessionalsInfo;
import com.zw.platform.domain.multimedia.Media;
import com.zw.platform.domain.riskManagement.RiskAlarm;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Data
public class RiskCampaignQuery implements Serializable {
    private String id;

    private String vehicleId;// 车辆id

    private String riskNumber;// 风险编号

    private String riskLevel;// 风险等级

    private String riskType;// 风险类型

    private String status;// 风险状态

    private double speed;// 速度

    private String dealId;// 处理人

    private String driverId;// 司机

    private String address;// 位置

    private String job;// 岗位

    private Date fileTime;// 归档时间

    private Date dealTime;// 处理时间

    private int riskResult;// 风控结果

    private String riskEvent;// 事件类型

    private String brand;// 车牌号

    private String deviceNumber;// 设备编号

    private String groupName;// 企业名

    private String groupPhone;// 车队点话

    private String driverName;// 司机名

    private String driverPhone;// 司机点话

    private String vehicleType;// 车辆类型

    private String emergencyContact;// 紧急联系人

    private String emergencyContactPhone;// 紧急联系人点话

    private String photograph;// 照片

    private String configId;

    private String riskLevelNum;// 等级编号

    private String mediaId;// 多媒体id

    private String visitId;// 回放id

    private Integer plateColor = 2;// 车牌颜色

    private String vuuid = UUID.randomUUID().toString();

    private List<ProfessionalsInfo> professionalsInfo;// 从业人员信息

    /**
     * 0不显示、1显示
     */
    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    private long startTime;

    private long endTime;

    private Date warningTime; // 开始时间

    private Date endTimeStr;

    private long tempTime;

    private List<RiskAlarm> riskEventList;// 风险-报警

    private List<RiskEventQuery> riskEventQueries;// 风险-报警

    private Date visitTime;

    private List<Media> mediaList;

    private String riskEventId;

}
