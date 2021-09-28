package com.zw.app.domain.alarm;

import com.zw.platform.domain.leaderboard.GroupRank;
import lombok.Data;

import java.util.Map;
import java.util.Objects;


@Data
public class RiskRankResult {
    private String id;

    private String name;

    private int riskToal;

    private Map<String, Integer> dayToal;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RiskRankResult riskRankResult = (RiskRankResult) o;
        return Objects.equals(id, riskRankResult.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }

}
