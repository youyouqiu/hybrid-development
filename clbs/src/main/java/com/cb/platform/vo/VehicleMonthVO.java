package com.cb.platform.vo;

import com.cb.platform.domain.VehicleOnlineMonth;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zhangsq
 * @date 2018/5/3 16:00
 */
@Data
public class VehicleMonthVO {

    @ExcelField(title = "车牌号")
    private String vehicleBrandNumber;
    @ExcelField(title = "车牌颜色")
    private String vehicleBrandColor;
    @ExcelField(title = "车辆类型")
    private String vehicleType;
    @ExcelField(title = "所属道路运输企业")
    private String enterpriseName;
    @ExcelField(title = "合计")
    private String monthReport;
    private String[] days;

    public VehicleMonthVO() {

    }

    public VehicleMonthVO(VehicleOnlineMonth vehicleOnlineMonth, int lastDay) {
        int minuteMonth = 0;
        int minute = 24 * 60;
        days = new String[lastDay];
        if (vehicleOnlineMonth.getDay1() != null && vehicleOnlineMonth.getDay1() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay1();
            String day1 =
                new BigDecimal(vehicleOnlineMonth.getDay1()).divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[0] = day1;
        }
        if (vehicleOnlineMonth.getDay2() != null && vehicleOnlineMonth.getDay2() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay2();
            String day2 =
                new BigDecimal(vehicleOnlineMonth.getDay2()).divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[1] = day2;
        }
        if (vehicleOnlineMonth.getDay3() != null && vehicleOnlineMonth.getDay3() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay3();
            String day3 =
                new BigDecimal(vehicleOnlineMonth.getDay3()).divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[2] = day3;
        }
        if (vehicleOnlineMonth.getDay4() != null && vehicleOnlineMonth.getDay4() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay4();
            String day4 =
                new BigDecimal(vehicleOnlineMonth.getDay4()).divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[3] = day4;
        }
        if (vehicleOnlineMonth.getDay5() != null && vehicleOnlineMonth.getDay5() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay5();
            String day5 =
                new BigDecimal(vehicleOnlineMonth.getDay5()).divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[4] = day5;
        }
        if (vehicleOnlineMonth.getDay6() != null && vehicleOnlineMonth.getDay6() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay6();
            String day6 =
                new BigDecimal(vehicleOnlineMonth.getDay6()).divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[5] = day6;
        }
        if (vehicleOnlineMonth.getDay7() != null && vehicleOnlineMonth.getDay7() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay7();
            String day7 =
                new BigDecimal(vehicleOnlineMonth.getDay7()).divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[6] = day7;
        }
        if (vehicleOnlineMonth.getDay8() != null && vehicleOnlineMonth.getDay8() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay8();
            String day8 =
                new BigDecimal(vehicleOnlineMonth.getDay8()).divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[7] = day8;
        }
        if (vehicleOnlineMonth.getDay9() != null && vehicleOnlineMonth.getDay9() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay9();
            String day9 =
                new BigDecimal(vehicleOnlineMonth.getDay9()).divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[8] = day9;
        }
        if (vehicleOnlineMonth.getDay10() != null && vehicleOnlineMonth.getDay10() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay10();
            String day10 = new BigDecimal(vehicleOnlineMonth.getDay10())
                .divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[9] = day10;
        }
        if (vehicleOnlineMonth.getDay11() != null && vehicleOnlineMonth.getDay11() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay11();
            String day11 = new BigDecimal(vehicleOnlineMonth.getDay11())
                .divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[10] = day11;
        }
        if (vehicleOnlineMonth.getDay12() != null && vehicleOnlineMonth.getDay12() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay12();
            String day12 = new BigDecimal(vehicleOnlineMonth.getDay12())
                .divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[11] = day12;
        }
        if (vehicleOnlineMonth.getDay13() != null && vehicleOnlineMonth.getDay13() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay13();
            String day13 = new BigDecimal(vehicleOnlineMonth.getDay13())
                .divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[12] = day13;
        }
        if (vehicleOnlineMonth.getDay14() != null && vehicleOnlineMonth.getDay14() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay14();
            String day14 = new BigDecimal(vehicleOnlineMonth.getDay14())
                .divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[13] = day14;
        }
        if (vehicleOnlineMonth.getDay15() != null && vehicleOnlineMonth.getDay15() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay15();
            String day15 = new BigDecimal(vehicleOnlineMonth.getDay15())
                .divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[14] = day15;
        }
        if (vehicleOnlineMonth.getDay16() != null && vehicleOnlineMonth.getDay16() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay16();
            String day16 = new BigDecimal(vehicleOnlineMonth.getDay16())
                .divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[15] = day16;
        }
        if (vehicleOnlineMonth.getDay17() != null && vehicleOnlineMonth.getDay17() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay17();
            String day17 = new BigDecimal(vehicleOnlineMonth.getDay17())
                .divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[16] = day17;
        }
        if (vehicleOnlineMonth.getDay18() != null && vehicleOnlineMonth.getDay18() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay18();
            String day18 = new BigDecimal(vehicleOnlineMonth.getDay18())
                .divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[17] = day18;
        }
        if (vehicleOnlineMonth.getDay19() != null && vehicleOnlineMonth.getDay19() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay19();
            String day19 = new BigDecimal(vehicleOnlineMonth.getDay19())
                .divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[18] = day19;
        }
        if (vehicleOnlineMonth.getDay20() != null && vehicleOnlineMonth.getDay20() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay20();
            String day20 = new BigDecimal(vehicleOnlineMonth.getDay20())
                .divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[19] = day20;
        }
        if (vehicleOnlineMonth.getDay21() != null && vehicleOnlineMonth.getDay21() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay21();
            String day21 = new BigDecimal(vehicleOnlineMonth.getDay21())
                .divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[20] = day21;
        }
        if (vehicleOnlineMonth.getDay22() != null && vehicleOnlineMonth.getDay22() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay22();
            String day22 = new BigDecimal(vehicleOnlineMonth.getDay22())
                .divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[21] = day22;
        }
        if (vehicleOnlineMonth.getDay23() != null && vehicleOnlineMonth.getDay23() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay23();
            String day23 = new BigDecimal(vehicleOnlineMonth.getDay23())
                .divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[22] = day23;
        }
        if (vehicleOnlineMonth.getDay24() != null && vehicleOnlineMonth.getDay24() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay24();
            String day24 = new BigDecimal(vehicleOnlineMonth.getDay24())
                .divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[23] = day24;
        }
        if (vehicleOnlineMonth.getDay25() != null && vehicleOnlineMonth.getDay25() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay25();
            String day25 = new BigDecimal(vehicleOnlineMonth.getDay25())
                .divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[24] = day25;
        }
        if (vehicleOnlineMonth.getDay26() != null && vehicleOnlineMonth.getDay26() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay26();
            String day26 = new BigDecimal(vehicleOnlineMonth.getDay26())
                .divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[25] = day26;
        }
        if (vehicleOnlineMonth.getDay27() != null && vehicleOnlineMonth.getDay27() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay27();
            String day27 = new BigDecimal(vehicleOnlineMonth.getDay27())
                .divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[26] = day27;
        }
        if (vehicleOnlineMonth.getDay28() != null && vehicleOnlineMonth.getDay28() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay28();
            String day28 = new BigDecimal(vehicleOnlineMonth.getDay28())
                .divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            days[27] = day28;
        }
        if (vehicleOnlineMonth.getDay29() != null && vehicleOnlineMonth.getDay29() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay29();
            String day29 = new BigDecimal(vehicleOnlineMonth.getDay29())
                .divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            if (lastDay > 28) {
                days[28] = day29;
            }

        }
        if (vehicleOnlineMonth.getDay30() != null && vehicleOnlineMonth.getDay30() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay30();
            String day30 = new BigDecimal(vehicleOnlineMonth.getDay30())
                .divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            if (lastDay > 29) {
                days[29] = day30;
            }
        }
        if (vehicleOnlineMonth.getDay31() != null && vehicleOnlineMonth.getDay31() > 0) {
            minuteMonth += vehicleOnlineMonth.getDay31();
            String day31 = new BigDecimal(vehicleOnlineMonth.getDay31())
                .divide(new BigDecimal(minute), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
            if (lastDay > 30) {
                days[30] = day31;
            }
        }
        if (minuteMonth > 0) {
            int allMinute = lastDay * minute;
            this.monthReport =
                new BigDecimal(minuteMonth).divide(new BigDecimal(allMinute), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
        }

    }
}
