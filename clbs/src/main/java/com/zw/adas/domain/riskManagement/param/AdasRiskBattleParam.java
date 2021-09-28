package com.zw.adas.domain.riskManagement.param;

import lombok.Data;

/***
 @Author zhengjc
 @Date 2019/3/12 11:27
 @Description 风控作战参数
 @version 1.0
 **/
@Data
public class AdasRiskBattleParam {
    //页面数量
    private int pageNum;

    //页面显示的大小
    private int pageSize;
    //已经存在的风险id
    private String riskIds;

    //企业或者监管的适配标志
    boolean enterpriseFlag;

}
