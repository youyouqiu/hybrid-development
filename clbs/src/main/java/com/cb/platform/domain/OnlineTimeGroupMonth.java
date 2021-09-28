package com.cb.platform.domain;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;


@Data
public class OnlineTimeGroupMonth {
    @ExcelField(title = "道路运输企业")
    private String groupName;

    private Double[] days;

    private double sumNUmber; // 合计

    public OnlineTimeGroupMonth(MonthGroupOnlineTime monthGroupOnlineTime, int lastDay) {
        days = new Double[lastDay];
        if (monthGroupOnlineTime != null) {
            groupName = monthGroupOnlineTime.getGroupName();
            sumNUmber = monthGroupOnlineTime.getSumNUmber(); // 合计

            days[0] = monthGroupOnlineTime.getDayOne();
            days[1] = monthGroupOnlineTime.getDayTwo();
            days[2] = monthGroupOnlineTime.getDayThree();
            days[3] = monthGroupOnlineTime.getDayFour();
            days[4] = monthGroupOnlineTime.getDayFive();
            days[5] = monthGroupOnlineTime.getDaySix();
            days[6] = monthGroupOnlineTime.getDaySeven();
            days[7] = monthGroupOnlineTime.getDayEight();
            days[8] = monthGroupOnlineTime.getDayNine();
            days[9] = monthGroupOnlineTime.getDayTen();
            days[10] = monthGroupOnlineTime.getDayEleven();
            days[11] = monthGroupOnlineTime.getDayTwelve();
            days[12] = monthGroupOnlineTime.getDayThirteen();
            days[13] = monthGroupOnlineTime.getDayFourteen();
            days[14] = monthGroupOnlineTime.getDayFifteen();
            days[15] = monthGroupOnlineTime.getDaySixteen();
            days[16] = monthGroupOnlineTime.getDaySeventeen();
            days[17] = monthGroupOnlineTime.getDayEnghteen();
            days[18] = monthGroupOnlineTime.getDayNineteen();
            days[19] = monthGroupOnlineTime.getDayTwenty();
            days[20] = monthGroupOnlineTime.getDayTwentyOne();
            days[21] = monthGroupOnlineTime.getDayTwentyTwo();
            days[22] = monthGroupOnlineTime.getDayTwentyThree();
            days[23] = monthGroupOnlineTime.getDayTwentyFour();
            days[24] = monthGroupOnlineTime.getDayTwentyFive();
            days[25] = monthGroupOnlineTime.getDayTwentySix();
            days[26] = monthGroupOnlineTime.getDayTwentySeven();
            days[27] = monthGroupOnlineTime.getDayTwentyEnght();
            if (lastDay > 28) {
                days[28] = monthGroupOnlineTime.getDayTwentyNine();
            }
            if (lastDay > 29) {
                days[29] = monthGroupOnlineTime.getDayThirty();
            }
            if (lastDay > 30) {
                days[30] = monthGroupOnlineTime.getDayThirtyOne();
            }
        }
    }
}
