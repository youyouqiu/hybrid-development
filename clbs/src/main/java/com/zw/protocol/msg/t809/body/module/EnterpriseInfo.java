package com.zw.protocol.msg.t809.body.module;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @Description:
 * @Author:nixiangqian
 * @Date:Create in 2018/10/17 14:39
 */
@Data
public class EnterpriseInfo {

    /**
     * 企业名称
     */
    private String name;

    /**
     * 运输行业
     */
    private String transType;

    /**
     * 道路运输经营许可证字别
     */
    private String word;

    /**
     * 道路运输经营许可证号
     */
    private String number;

    /**
     * 城市 行政区划代码，按照GB/T2260的规定
     */
    private String city;

    /**
     * 发证机关
     */
    private String issueOrg;

    /**
     * 经营状态
     */
    private String status;

    /**
     * 经营范围
     */
    private String scope;

    /**
     * 企业法人
     */
    private String person;

    /**
     * 联系电话
     */
    private String tel;

    @Override
    public String toString() {
        return "NAME:=" + valueToString(name)
            + ";TRANS_TYPE:=" + valueToString(transType)
            + ";WORD:=" + valueToString(word)
            + ";NUMBER:=" + valueToString(number)
            + ";CITY:=" + valueToString(city)
            + ";ISSUE_ORG:=" + valueToString(issueOrg)
            + ";STATUS:=" + valueToString(status)
            + ";SCOPE:=" + valueToString(scope)
            + ";PERSON:=" + valueToString(person)
            + ";TEL:=" + valueToString(tel);
    }

    private String valueToString(String value) {
        return StringUtils.isBlank(value) ? "" : value;
    }
}
