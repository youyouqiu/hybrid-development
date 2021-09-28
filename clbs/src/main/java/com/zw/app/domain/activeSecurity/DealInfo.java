package com.zw.app.domain.activeSecurity;

import lombok.Data;


@Data
public class DealInfo {

    private int untreated;

    private int treated;

    private int total;

    public DealInfo(int untreated, int treated) {
        this.untreated = untreated;
        this.treated = treated;
        this.total = untreated + treated;
    }
}
