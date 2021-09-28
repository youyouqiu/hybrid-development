package com.cb.platform.vo;

import com.cb.platform.domain.VehicleMileMonth;
import lombok.Data;

/**
 * @author zhangsq
 * @date 2018/5/14 10:42
 */
@Data
public class VehicleMileMonthVO {

    private String vehicleBrandNumber;
    private String vehicleBrandColor;
    private String vehicleType;
    private String enterpriseName;
    private Double monthReport;
    private Double[] days;


    public VehicleMileMonthVO() {

    }

    public VehicleMileMonthVO(VehicleMileMonth mileMonth, Integer lastDay) {
        days = new Double[lastDay];
        days[0] = mileMonth.getDay1();
        days[1] = mileMonth.getDay2();
        days[2] = mileMonth.getDay3();
        days[3] = mileMonth.getDay4();
        days[4] = mileMonth.getDay5();
        days[5] = mileMonth.getDay6();
        days[6] = mileMonth.getDay7();
        days[7] = mileMonth.getDay8();
        days[8] = mileMonth.getDay9();
        days[9] = mileMonth.getDay10();
        days[10] = mileMonth.getDay11();
        days[11] = mileMonth.getDay12();
        days[12] = mileMonth.getDay13();
        days[13] = mileMonth.getDay14();
        days[14] = mileMonth.getDay15();
        days[15] = mileMonth.getDay16();
        days[16] = mileMonth.getDay17();
        days[17] = mileMonth.getDay18();
        days[18] = mileMonth.getDay19();
        days[19] = mileMonth.getDay20();
        days[20] = mileMonth.getDay21();
        days[21] = mileMonth.getDay22();
        days[22] = mileMonth.getDay23();
        days[23] = mileMonth.getDay24();
        days[24] = mileMonth.getDay25();
        days[25] = mileMonth.getDay26();
        days[26] = mileMonth.getDay27();
        days[27] = mileMonth.getDay28();
        if (lastDay > 28) {
            days[28] = mileMonth.getDay29();
        }
        if (lastDay > 29) {
            days[29] = mileMonth.getDay30();
        }
        if (lastDay > 30) {
            days[30] = mileMonth.getDay31();
        }
        double sum = 0;
        for (double monthReport : days) {
            sum += monthReport;
        }
        this.monthReport = sum;
    }

}
