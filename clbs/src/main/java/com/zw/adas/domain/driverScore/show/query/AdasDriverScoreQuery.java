package com.zw.adas.domain.driverScore.show.query;

import com.alibaba.fastjson.JSONArray;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;

import java.util.List;

/***
 @Author zhengjc
 @Date 2019/10/15 11:03
 @Description 司机评分查询对象
 @version 1.0
 **/
@Data
public class AdasDriverScoreQuery extends BaseQueryBean {
    /**
     * 查询企业id
     */
    private String groupId;
    /**
     * 查询的从业资格证号
     */
    private String cardNumber;
    /**
     * 查询的司机名称
     */
    private String driverName;
    /**
     * 查询的时间范围（月）
     */
    private long time;

    private List<String> groupIds;

    private String cardNumberName;

    List<AdasDriverScoreQueryParam> queryParams;

    private String queryParamsStr;



    /**
     * es查询下一页标示
     */
    private Object[] searchAfter;


    public void initParam() {
        cardNumberName = cardNumber + "_" + driverName;
        if (StrUtil.isNotBlank(queryParamsStr)) {
            queryParams = JSONArray.parseArray(queryParamsStr, AdasDriverScoreQueryParam.class);
            queryParams.forEach(queryParam ->
                    queryParam.setCardNumberName(queryParam.getCardNumber() + "_" + queryParam.getDriverName()));
        }
    }

}
