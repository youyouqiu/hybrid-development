package com.zw.platform.domain.leaderboard;

import lombok.Data;

import java.util.List;


@Data
public class RiskRanking {
    List<Long> riskTotal;//风险总数

    List<Long> distraction;//分心

    List<Long> rException;//异常

    List<Long> crash;//碰撞

    List<Long> tired;//疲劳

    List<Long> cluster;//集束

    List<String> distractionRatio;//分心占比

    List<String> exceptionRatio;//异常占比

    List<String> crashRatio;//碰撞占比

    List<String> tiredRatio;//疲劳占比

    List<String> clusterRatio;//集束占比
}
