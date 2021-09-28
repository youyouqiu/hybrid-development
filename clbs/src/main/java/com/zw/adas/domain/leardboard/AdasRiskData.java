package com.zw.adas.domain.leardboard;

import lombok.Data;

@Data
public class AdasRiskData {
    /**
     * 风险类型
     */
    private String riskType;

    /**
     * 事件报警总数
     */
    private long total;

    // /**
    //  * 被督办未处理
    //  */
    // private long overseeNotDeal;
    //
    // /**
    //  * 被督办已处理
    //  */
    // private long overseeAndDeal;
    //
    // /**
    //  * 未督办未处理
    //  */
    // private long notDealNotOversee;
    //
    // /**
    //  * 未督办已处理
    //  */
    // private long dealNotOversee;

    /**
     * 已处理
     */
    private long dealed;

    /**
     * 未处理
     */
    private long undeal;

    public AdasRiskData(String riskType) {
        this.riskType = riskType;
    }

    public AdasRiskData() {
    }
}
