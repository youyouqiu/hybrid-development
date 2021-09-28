package com.zw.adas.domain.driverScore.show;

import lombok.Data;

/***
 @Author zhengjc
 @Date 2019/10/15 10:07
 @Description 综合评分列表
 @version 1.0
 **/
@Data
public class AdasDriverGroupGeneralScoreListShow {
    /**
     * (企业Id)
     */
    private String groupId;

    /**
     * (企业名称)
     */
    private String groupName;

    /**
     * (201909)
     */
    private String time;

    /**
     * (司机名称和从业资格账号拼接字符串从业资格证号_姓名)
     */
    private String driverNameCardNumber;

    /**
     * (行驶里程)
     */
    private Double driverMile;
    /**
     * (行驶次数)
     */
    private int driverTimes;

    /**
     * （平均行驶时长）天
     */
    private String averageDriverTime;
    /**
     * （平均速度）
     */
    private Double averageSpeed;
    /**
     * （报警数）
     */
    private int alarm;

    /**
     * (综合得分)
     */
    private Double score;
    /**
     * 驾驶员企业id
     */
    private String driverGroupId;
    /**
     * 查询使用的字段
     */
    private String driverNameCardNumberVal;

    public static AdasDriverGroupGeneralScoreListShow getInstanceShow() {
        AdasDriverGroupGeneralScoreListShow show = new AdasDriverGroupGeneralScoreListShow();
        show.groupId = "60c1bcfd-d3e0-4a45-a301-00519a21b977";
        show.time = "201909";
        show.driverNameCardNumber = "84003529463400352905";
        show.driverMile = 100.20;
        show.driverTimes = 10;
        show.averageDriverTime = "3小时50分52秒";
        show.averageSpeed = 70.0;
        show.alarm = 20;
        show.score = 60.2;
        return show;
    }

}
