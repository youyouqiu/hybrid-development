package com.zw.adas.domain.driverScore.show;

import com.zw.platform.util.common.PrecisionUtils;
import lombok.Data;

/***
 @Author zhengjc
 @Date 2019/10/15 10:07
 @Description 综合评分导出实体
 @version 1.0
 **/
@Data
public class AdasDriverGroupGeneralScoreListExport {

    private int number;
    /**
     * (企业名称)
     */
    private String groupName;

    /**
     * 司机名称
     */
    private String cardNumber;

    /**
     * 司机名称
     */
    private String driverName;

    /**
     * (行驶里程)
     */
    private String travelMile;
    /**
     * (行驶次数)
     */
    private int travelTimes;

    /**
     * （平均行驶时长）天
     */
    private String averageDriverTime;
    /**
     * （平均速度）
     */
    private String averageSpeed;
    /**
     * （报警数）
     */
    private int alarm;

    /**
     * (综合得分)
     */
    private String score;

    public static AdasDriverGroupGeneralScoreListExport getInstance(AdasDriverGroupGeneralScoreListShow data,
        int number) {
        AdasDriverGroupGeneralScoreListExport result = new AdasDriverGroupGeneralScoreListExport();

        result.groupName = data.getGroupName();
        if (data.getDriverNameCardNumberVal() != null) {
            String[] driverNameCardNumber = data.getDriverNameCardNumberVal().split("_");

            result.cardNumber = driverNameCardNumber[0];
            result.driverName = driverNameCardNumber[1];
        } else {
            result.cardNumber = "-";
            result.driverName = "-";
        }
        result.travelMile = PrecisionUtils.getNullOrHorizontalLine(data.getDriverMile(), 1);
        result.travelTimes = data.getDriverTimes();
        result.averageDriverTime = data.getAverageDriverTime();
        result.averageSpeed = PrecisionUtils.getNullOrHorizontalLine(data.getAverageSpeed(), 1);
        result.alarm = data.getAlarm();
        result.number = number;
        result.score = PrecisionUtils.getNullOrHorizontalLine(data.getScore(), 0);
        return result;
    }

}
