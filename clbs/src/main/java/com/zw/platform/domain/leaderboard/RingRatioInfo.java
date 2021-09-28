package com.zw.platform.domain.leaderboard;

import lombok.Data;


@Data
public class RingRatioInfo {
    private String riskRingRatio;//风险数环比
    private String accuracyRate;//准确率
    private boolean accuracyUp;//准备率环比（true 上升）

}
