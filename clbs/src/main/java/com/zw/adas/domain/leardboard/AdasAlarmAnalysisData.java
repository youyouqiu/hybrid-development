package com.zw.adas.domain.leardboard;

import lombok.Data;

import java.util.List;

@Data
public class AdasAlarmAnalysisData {

    private long total = 0;//风险事件总数

    // private long overseeNotDeal = 0;//被督办未处理
    //
    // private long overseeAndDeal = 0;//被督办已处理
    //
    // private long notDealNotOversee = 0;//未督办未处理
    //
    // private long dealNotOversee = 0;//未督办已处理

    /**
     * 已处理
     */
    private long dealed = 0;

    /**
     * 未处理
     */
    private long undeal = 0;

    private List<AdasRiskData> riskDataList;

}
