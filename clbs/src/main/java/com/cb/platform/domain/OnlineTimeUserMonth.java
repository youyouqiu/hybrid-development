package com.cb.platform.domain;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;


@Data
public class OnlineTimeUserMonth implements Serializable {
    @ExcelField(title = "用户名")
    private String userName;

    @ExcelField(title = "道路运输企业")
    private String groupName;

    private Double[] days;

    private double sumNUmber; // 合计

    public OnlineTimeUserMonth(MonthOnlineTime monthOnlineTime, int lastDay) {
        days = new Double[lastDay];
        if (monthOnlineTime != null) {
            userName = monthOnlineTime.getUserName(); // 用户名
            groupName = monthOnlineTime.getGroupName(); // 所属企业
            sumNUmber = monthOnlineTime.getSumNUmber(); // 合计
            days[0] = monthOnlineTime.getDayOne();
            days[1] = monthOnlineTime.getDayTwo();
            days[2] = monthOnlineTime.getDayThree();
            days[3] = monthOnlineTime.getDayFour();
            days[4] = monthOnlineTime.getDayFive();
            days[5] = monthOnlineTime.getDaySix();
            days[6] = monthOnlineTime.getDaySeven();
            days[7] = monthOnlineTime.getDayEight();
            days[8] = monthOnlineTime.getDayNine();
            days[9] = monthOnlineTime.getDayTen();
            days[10] = monthOnlineTime.getDayEleven();
            days[11] = monthOnlineTime.getDayTwelve();
            days[12] = monthOnlineTime.getDayThirteen();
            days[13] = monthOnlineTime.getDayFourteen();
            days[14] = monthOnlineTime.getDayFifteen();
            days[15] = monthOnlineTime.getDaySixteen();
            days[16] = monthOnlineTime.getDaySeventeen();
            days[17] = monthOnlineTime.getDayEnghteen();
            days[18] = monthOnlineTime.getDayNineteen();
            days[19] = monthOnlineTime.getDayTwenty();
            days[20] = monthOnlineTime.getDayTwentyOne();
            days[21] = monthOnlineTime.getDayTwentyTwo();
            days[22] = monthOnlineTime.getDayTwentyThree();
            days[23] = monthOnlineTime.getDayTwentyFour();
            days[24] = monthOnlineTime.getDayTwentyFive();
            days[25] = monthOnlineTime.getDayTwentySix();
            days[26] = monthOnlineTime.getDayTwentySeven();
            days[27] = monthOnlineTime.getDayTwentyEnght();
            if (lastDay > 28) {
                days[28] = monthOnlineTime.getDayTwentyNine();
            }
            if (lastDay > 29) {
                days[29] = monthOnlineTime.getDayThirty();
            }
            if (lastDay > 30) {
                days[30] = monthOnlineTime.getDayThirtyOne();
            }
        }
    }
}
