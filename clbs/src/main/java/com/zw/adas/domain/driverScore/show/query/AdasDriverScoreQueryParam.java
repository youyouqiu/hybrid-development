package com.zw.adas.domain.driverScore.show.query;

import lombok.Data;

/***
 @Author zhengjc
 @Date 2019/10/28 11:52
 @Description 查询的参数实体
 @version 1.0
 **/
@Data
public class AdasDriverScoreQueryParam {
    /**
     * 企业id
     */
    private String groupId;
    /**
     * 从业资格证号
     */
    private String cardNumber;

    /**
     * 司机名称
     */
    private String driverName;

    /**
     * 从业资格证_司机名(2个字段拼接)
     */
    private String cardNumberName;
}
