package com.zw.app.domain.activeSecurity;

import lombok.Data;


@Data
public class DayRiskNum {
    private String day;

    private int num;

    public DayRiskNum(String day, int num) {
        this.day = day;
        this.num = num;
    }
}
