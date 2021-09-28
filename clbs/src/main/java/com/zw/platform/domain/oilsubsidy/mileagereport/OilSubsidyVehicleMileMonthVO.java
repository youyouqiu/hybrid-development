package com.zw.platform.domain.oilsubsidy.mileagereport;

import com.cb.platform.domain.VehicleMileMonth;
import lombok.Data;

/**
 * @author zhangsq
 * @date 2018/5/14 10:42
 */
@Data
public class OilSubsidyVehicleMileMonthVO {

    private String vehicleBrandNumber;
    private String enterpriseName;
    private Double monthReport;
    private Double[] days;


    public OilSubsidyVehicleMileMonthVO() {

    }

    public OilSubsidyVehicleMileMonthVO(VehicleMileMonth mileMonth, Integer lastDay) {
        days = new Double[lastDay];
        days[0] = mileMonth.getMonthEveryDayMile().get(0);
        days[1] = mileMonth.getMonthEveryDayMile().get(1);
        days[2] = mileMonth.getMonthEveryDayMile().get(2);
        days[3] = mileMonth.getMonthEveryDayMile().get(3);
        days[4] = mileMonth.getMonthEveryDayMile().get(4);
        days[5] = mileMonth.getMonthEveryDayMile().get(5);
        days[6] = mileMonth.getMonthEveryDayMile().get(6);
        days[7] = mileMonth.getMonthEveryDayMile().get(7);
        days[8] = mileMonth.getMonthEveryDayMile().get(8);
        days[9] = mileMonth.getMonthEveryDayMile().get(9);
        days[10] = mileMonth.getMonthEveryDayMile().get(10);
        days[11] = mileMonth.getMonthEveryDayMile().get(11);
        days[12] = mileMonth.getMonthEveryDayMile().get(12);
        days[13] = mileMonth.getMonthEveryDayMile().get(13);
        days[14] = mileMonth.getMonthEveryDayMile().get(14);
        days[15] = mileMonth.getMonthEveryDayMile().get(15);
        days[16] = mileMonth.getMonthEveryDayMile().get(16);
        days[17] = mileMonth.getMonthEveryDayMile().get(17);
        days[18] = mileMonth.getMonthEveryDayMile().get(18);
        days[19] = mileMonth.getMonthEveryDayMile().get(19);
        days[20] = mileMonth.getMonthEveryDayMile().get(20);
        days[21] = mileMonth.getMonthEveryDayMile().get(21);
        days[22] = mileMonth.getMonthEveryDayMile().get(22);
        days[23] = mileMonth.getMonthEveryDayMile().get(23);
        days[24] = mileMonth.getMonthEveryDayMile().get(24);
        days[25] = mileMonth.getMonthEveryDayMile().get(25);
        days[26] = mileMonth.getMonthEveryDayMile().get(26);
        days[27] = mileMonth.getMonthEveryDayMile().get(27);
        if (lastDay > 28) {
            days[28] = mileMonth.getMonthEveryDayMile().get(28);
        }
        if (lastDay > 29) {
            days[29] = mileMonth.getMonthEveryDayMile().get(29);
        }
        if (lastDay > 30) {
            days[30] = mileMonth.getMonthEveryDayMile().get(30);
        }
        this.monthReport = mileMonth.getMileCount();
        this.enterpriseName = mileMonth.getGroupName();
        this.vehicleBrandNumber = mileMonth.getMonitorName();
    }

}
