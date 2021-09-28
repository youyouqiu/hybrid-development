package com.zw.adas.domain.riskManagement;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zjc on 2017/8/16.
 */
@Data
public class AdasRiskEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    //6401
    private Integer functionId;

    private Short flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    private String address;

    private Date eventTime;
    //风险事件名称
    private String riskEvent;
    //风险类型名称
    private String riskType;

    private String description;

    //协议类型 12:川标,1:黑标,13:冀表
    private Integer protocolType;
    //间隔时间
    private  int intervalTime;
    //1:疲劳,2:分心,3:异常,4:碰撞,6:激烈驾驶,
    private  int riskTypeNum;
    //报警事件统称
    private String eventCommonName;
    //一级等级初始值30km/h≤一级<50km/h
    private int oneLevel;
    //二级等级初始值50km/h≤二级
    private int twoLevel;
    //速度等级系数30km/h≤1级<50km/h
    private float oneSpeedFactor;
    //速度等级系数50km/h≤2级＜70km/h
    private float twoSpeedFactor;
    //速度等级系数3级≥70km/h
    private float threeSpeedFactor;
    //一级速度等级速度30km/h
    private float oneSpeed;
    //二级速度等级速度50km/h
    private float twoSpeed;
    //三级速度等级速度70km/h
    private float threeSpeed;
    //08报警类型码,主动安全转发809的时候不能为空
    private String t808Pos;




}
