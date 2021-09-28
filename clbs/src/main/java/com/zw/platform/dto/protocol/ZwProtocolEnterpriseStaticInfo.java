package com.zw.platform.dto.protocol;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/9/27 11:58
 */
@Data
public class ZwProtocolEnterpriseStaticInfo {
    /**
     * 企业编号/业户编号
     */
    private String owersId;
    /**
     * 企业名称/业户名称
     */
    private String owersName;
    /**
     * 行业类别
     */
    private String transType;
    /**
     * 道路运输经营许可证字别
     */
    private String businessWord;
    /**
     * 道路运输经营许可证号
     */
    private String businessNumber;
    /**
     * 许可证有效期起 格式“yyyymmdd”
     */
    private String validityBegin;
    /**
     * 许可证有效期止 格式“yyyymmdd”
     */
    private String validityEnd;
    /**
     * 组织机构代码
     */
    private String orgCa;
    /**
     * 上级组织机构代码
     */
    private String upCa;
    /**
     * 管理组织机构代码
     */
    private String manageCa;
    /**
     * 籍贯地
     */
    private String zone;
    /**
     * 地址
     */
    private String address;
    /**
     * 发证机关
     */
    private String issueOrg;
    /**
     * 经营状态
     */
    private String status;
    /**
     * 经营范围 按照 JT/T415-2006 中 5.2.4 的规定，多项间用半角“,”隔开，如“01101,01104”
     */
    private String businessScope;
    /**
     * 企业法人
     */
    private String corporation;
    /**
     * 法人联系电话
     */
    private String corporationTel;
    /**
     * 企业联系人
     */
    private String linkMan;
    /**
     * 企业联系电话
     */
    private String linkTel;

    @Override
    public String toString() {
        return "OWERS_ID:=" + valueToString(owersId)
            + ";OWERS_NAME:= " + valueToString(owersName)
            + ";TRANS_TYPE:=" + valueToString(transType)
            + ";BUSINESS_WORD:=" + valueToString(businessWord)
            + ";BUSINESS_NUMBER:=" + valueToString(businessNumber)
            + ";VALIDITY_BEGIN:=" + valueToString(validityBegin)
            + ";VALIDITY_END:=" + valueToString(validityEnd)
            + ";ORG_CA:=" + valueToString(orgCa)
            + ";UP_CA:=" + valueToString(upCa)
            + ";MANAGE_CA:=" + valueToString(manageCa)
            + ";ZONE:=" + valueToString(zone)
            + ";ADDRESS:=" + valueToString(address)
            + ";ISSUE_ORG:=" + valueToString(issueOrg)
            + ";STATUS:=" + valueToString(status)
            + ";BUSINESS_SCOPE:=" + valueToString(businessScope)
            + ";CORPORATION:=" + valueToString(corporation)
            + ";CORPORATION_TEL:=" + valueToString(corporationTel)
            + ";LINKMAN:=" + valueToString(linkMan)
            + ";LINKTEL:=" + valueToString(linkTel);
    }

    private String valueToString(String value) {
        return StringUtils.isBlank(value) ? "" : value;
    }
}
