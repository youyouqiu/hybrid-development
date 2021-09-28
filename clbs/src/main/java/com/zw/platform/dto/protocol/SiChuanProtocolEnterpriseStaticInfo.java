package com.zw.platform.dto.protocol;

import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/9/28 17:05
 */
@Data
public class SiChuanProtocolEnterpriseStaticInfo {

    /**
     * 所属运输企业
     */
    private String orgCa;

    /**
     * 企业名称
     */
    private String name;
    /**
     * 运输行业编码
     */
    private String transType;

    /**
     * 上级组织机构代 码
     */
    private String upCa;

    /**
     * 管理织机构代码
     */
    private String manageCa;
    /**
     * 籍贯地
     */
    private String zone;
    /**
     * 经营范围
     */
    private String businessScope;

    /**
     * 经营许可证号
     */
    private String businessNumber;

    /**
     * 许可证有效期起
     */
    private String validityBegin;

    /**
     * 许可证有效期止
     */
    private String validityEnd;
    /**
     * 地址
     */
    private String address;
    /**
     * 法人代表
     */
    private String corporation;
    /**
     * 联系人
     */
    private String linkman;
    /**
     * 联系电话
     */
    private String telphone;

    @Override
    public String toString() {
        return "ORG_CA:=" + (orgCa == null ? "" : orgCa)
            + ";NAME:=" + (name == null ? "" : name)
            + ";TRANS_TYPE:=" + (transType == null ? "" : transType)
            + ";UP_CA:=" + (upCa == null ? "" : upCa)
            + ";MANAGE_CA:=" + (manageCa == null ? "" : manageCa)
            + ";ZONE:=" + (zone == null ? "" : zone)
            + ";BUSINESS_NUMBER:=" + (businessNumber == null ? "" : businessNumber)
            + ";BUSINESS_SCOPE:=" + (businessScope == null ? "" : businessScope)
            + ";VALIDITY_BEGIN:=" + (validityBegin == null ? "" : validityBegin)
            + ";VALIDITY_END:=" + (validityEnd == null ? "" : validityEnd)
            + ";ADDRESS:=" + (address == null ? "" : address)
            + ";CORPORATION:=" + (corporation == null ? "" : corporation)
            + ";LINKMAN:=" + (linkman == null ? "" : linkman)
            + ";TELPHONE:=" + (telphone == null ? "" : telphone);
    }

}
