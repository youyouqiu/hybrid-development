package com.zw.app.domain.alarm;

import lombok.Data;


@Data
public class PercentageOfRank {

    private Integer tired = 0;//疲劳1

    private Integer distraction = 0;//分心2

    private Integer exception = 0;//异常3

    private Integer crash = 0;//碰撞4

    private Integer cluster = 0;//组合风险5

    private Integer intenseDriving = 0;//激烈驾驶6

    public PercentageOfRank() {

    }

    // public PercentageOfRank(Integer tired, Integer distraction, Integer exception, Integer crash, Integer cluster) {
    //     this.tired = tired;
    //     this.distraction = distraction;
    //     this.exception = exception;
    //     this.crash = crash;
    //     this.cluster = cluster;
    // }
}
