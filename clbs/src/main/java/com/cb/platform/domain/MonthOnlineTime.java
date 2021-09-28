package com.cb.platform.domain;


import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;


/**
 * 道路运输企业月在线时长统计实体
 */
@Data
public class MonthOnlineTime implements Serializable {

    @ExcelField(title = "用户名")
    private String userName; // 用户名

    @ExcelField(title = "道路运输企业")
    private String groupName; // 企业名称

    @ExcelField(title = "01")
    private double dayOne; // 月份的第1天

    @ExcelField(title = "02")
    private double dayTwo; // 月的第2天

    @ExcelField(title = "03")
    private double dayThree; // 月的第3天

    @ExcelField(title = "04")
    private double dayFour; // 月的第4天

    @ExcelField(title = "05")
    private double dayFive; // 月的第5天

    @ExcelField(title = "06")
    private double daySix; // 月的第6天

    @ExcelField(title = "07")
    private double daySeven; // 月的第7天

    @ExcelField(title = "08")
    private double dayEight; // 月的第8天

    @ExcelField(title = "09")
    private double dayNine; // 月的第9天

    @ExcelField(title = "10")
    private double dayTen; // 月的第10天

    @ExcelField(title = "11")
    private double dayEleven; // 月的第11天

    @ExcelField(title = "12")
    private double dayTwelve; // 月的第12天

    @ExcelField(title = "13")
    private double dayThirteen; // 月的第13天

    @ExcelField(title = "14")
    private double dayFourteen; // 月的第14天

    @ExcelField(title = "15")
    private double dayFifteen; // 月的第15天

    @ExcelField(title = "16")
    private double daySixteen; // 月的第16天

    @ExcelField(title = "17")
    private double daySeventeen; // 月的第17天

    @ExcelField(title = "18")
    private double dayEnghteen; // 月的第18天

    @ExcelField(title = "19")
    private double dayNineteen; // 月的第19天

    @ExcelField(title = "20")
    private double dayTwenty; // 月的第20天

    @ExcelField(title = "21")
    private double dayTwentyOne; // 月的第21天

    @ExcelField(title = "22")
    private double dayTwentyTwo; // 月的第22天

    @ExcelField(title = "23")
    private double dayTwentyThree; // 月的第23天

    @ExcelField(title = "24")
    private double dayTwentyFour; // 月的第24天

    @ExcelField(title = "25")
    private double dayTwentyFive; // 月的第25天

    @ExcelField(title = "26")
    private double dayTwentySix; // 月的第26天

    @ExcelField(title = "27")
    private double dayTwentySeven; // 月的第27天

    @ExcelField(title = "28")
    private double dayTwentyEnght; // 月的第28天

    @ExcelField(title = "29")
    private double dayTwentyNine; // 月的第29天

    @ExcelField(title = "30")
    private double dayThirty; // 月的第30天

    @ExcelField(title = "31")
    private double dayThirtyOne; // 月的第31天

    @ExcelField(title = "合计")
    private double sumNUmber; // 合计

    public void setDayData(Integer onDay,Double onlineTimes) {
        switch (onDay) {
            case 1:
                setDayOne(onlineTimes);
                break;
            case 2:
                setDayTwo(onlineTimes);
                break;
            case 3:
                setDayThree(onlineTimes);
                break;
            case 4:
                setDayFour(onlineTimes);
                break;
            case 5:
                setDayFive(onlineTimes);
                break;
            case 6:
                setDaySix(onlineTimes);
                break;
            case 7:
                setDaySeven(onlineTimes);
                break;
            case 8:
                setDayEight(onlineTimes);
                break;
            case 9:
                setDayNine(onlineTimes);
                break;
            case 10:
                setDayTen(onlineTimes);
                break;
            case 11:
                setDayEleven(onlineTimes);
                break;
            case 12:
                setDayTwelve(onlineTimes);
                break;
            case 13:
                setDayThirteen(onlineTimes);
                break;
            case 14:
                setDayFourteen(onlineTimes);
                break;
            case 15:
                setDayFifteen(onlineTimes);
                break;
            case 16:
                setDaySixteen(onlineTimes);
                break;
            case 17:
                setDaySeventeen(onlineTimes);
                break;
            case 18:
                setDayEnghteen(onlineTimes);
                break;
            case 19:
                setDayNineteen(onlineTimes);
                break;
            case 20:
                setDayTwenty(onlineTimes);
                break;
            case 21:
                setDayTwentyOne(onlineTimes);
                break;
            case 22:
                setDayTwentyTwo(onlineTimes);
                break;
            case 23:
                setDayTwentyThree(onlineTimes);
                break;
            case 24:
                setDayTwentyFour(onlineTimes);
                break;
            case 25:
                setDayTwentyFive(onlineTimes);
                break;
            case 26:
                setDayTwentySix(onlineTimes);
                break;
            case 27:
                setDayTwentySeven(onlineTimes);
                break;
            case 28:
                setDayTwentyEnght(onlineTimes);
                break;
            case 29:
                setDayTwentyNine(onlineTimes);
                break;
            case 30:
                setDayThirty(onlineTimes);
                break;
            case 31:
                setDayThirtyOne(onlineTimes);
                break;
            default:
                break;
        }
    }
}
