package com.zw.platform.domain.leaderboard;

import lombok.Data;

import java.io.Serializable;


@Data
public class OrgRiskType implements Serializable {

    private int tired; //疲劳

    private int crash;//碰撞

    private int exception;//异常

    private int distraction;//分心

    private int cluster;//集速

    private int intenseDriving;//激烈驾驶风险

    private int total;//风险总数

    private int time; //查询的时间

}
