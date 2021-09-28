package com.cb.platform.vo;

import com.cb.platform.domain.GroupOnlineMonth;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * @author zhangsq
 * @date 2018/5/3 16:00
 */
@Data
public class EnterpriseVehicleMonthVO {

    @ExcelField(title = "道路运输企业")
    private String enterpriseName;
    private String monthReport = "0.00%";

    private String[] days;

    public EnterpriseVehicleMonthVO() {

    }

    public EnterpriseVehicleMonthVO(GroupOnlineMonth vehicleOnlineMonth, int lastDay, int vehicleCount) {
        days = new String[lastDay];
        days[0] = vehicleOnlineMonth.getDay1();
        days[1] = vehicleOnlineMonth.getDay2();
        days[2] = vehicleOnlineMonth.getDay3();
        days[3] = vehicleOnlineMonth.getDay4();
        days[4] = vehicleOnlineMonth.getDay5();
        days[5] = vehicleOnlineMonth.getDay6();
        days[6] = vehicleOnlineMonth.getDay7();
        days[7] = vehicleOnlineMonth.getDay8();
        days[8] = vehicleOnlineMonth.getDay9();
        days[9] = vehicleOnlineMonth.getDay10();
        days[10] = vehicleOnlineMonth.getDay11();
        days[11] = vehicleOnlineMonth.getDay12();
        days[12] = vehicleOnlineMonth.getDay13();
        days[13] = vehicleOnlineMonth.getDay14();
        days[14] = vehicleOnlineMonth.getDay15();
        days[15] = vehicleOnlineMonth.getDay16();
        days[16] = vehicleOnlineMonth.getDay17();
        days[17] = vehicleOnlineMonth.getDay18();
        days[18] = vehicleOnlineMonth.getDay19();
        days[19] = vehicleOnlineMonth.getDay20();
        days[20] = vehicleOnlineMonth.getDay21();
        days[21] = vehicleOnlineMonth.getDay22();
        days[22] = vehicleOnlineMonth.getDay23();
        days[23] = vehicleOnlineMonth.getDay24();
        days[24] = vehicleOnlineMonth.getDay25();
        days[25] = vehicleOnlineMonth.getDay26();
        days[26] = vehicleOnlineMonth.getDay27();
        days[27] = vehicleOnlineMonth.getDay28();
        if (lastDay > 28) {
            days[28] = vehicleOnlineMonth.getDay29();
        }
        if (lastDay > 29) {
            days[29] = vehicleOnlineMonth.getDay30();
        }
        if (lastDay > 30) {
            days[30] = vehicleOnlineMonth.getDay31();
        }
        double monthReportD = 0;
        for (String day : days) {
            if (StringUtils.isNotEmpty(day) && day.contains("%")) {
                monthReportD += Double.parseDouble(day.substring(0, day.length() - 1));
            }
        }
        if (monthReportD > 0) {
            this.monthReport =
                BigDecimal.valueOf(monthReportD).divide(new BigDecimal(lastDay), 2, BigDecimal.ROUND_HALF_UP) + "%";
        }


    }

}
