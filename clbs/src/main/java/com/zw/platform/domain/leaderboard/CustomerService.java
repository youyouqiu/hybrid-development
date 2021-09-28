package com.zw.platform.domain.leaderboard;

import lombok.Data;

import java.io.Serializable;


@Data
public class CustomerService implements Serializable {
    private int online;

    private int total;

    private String rate;

    private long time;
}
