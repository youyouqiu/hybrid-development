package com.zw.platform.domain.leaderboard;

import com.zw.platform.util.common.ComputingUtils;
import lombok.Data;

import java.io.Serializable;


@Data
public class OrgVehOnline implements Serializable {
    private int online;

    private int total;

    private String rate;

    private int time;

    public void calOnlineRate() {

        rate = ComputingUtils.calProportion(online, total);
    }
}
