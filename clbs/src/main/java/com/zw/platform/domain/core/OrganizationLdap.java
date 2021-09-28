package com.zw.platform.domain.core;

import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import org.apache.bval.constraints.NotEmpty;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
import javax.validation.constraints.Size;
import java.util.Objects;


/**
 * <p>
 * Title: 组织架构实体
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 *
 * @version 1.0
 * @author: wangying
 * @date 2016年8月3日下午6:08:40
 */
@Entry(objectClasses = {"organizationalUnit", "top"}, base = "ou=organization")
public final class OrganizationLdap {

    private static final long serialVersionUID = 1L;

    @Id
    private Name id;

    @Attribute(name = "entryUUID")
    private String uuid;

    @Attribute(name = "ou")
    private String ou;

    @Size(max = 20, message = "【组织机构代码】不能超过10个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Attribute(name = "postalCode")
    private String organizationCode;

    @Size(max = 20, message = "【经营许可证号】不能超过13个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Attribute(name = "p0")
    private String license;

    @Attribute(name = "physicalDeliveryOfficeName")
    private String registerDate;

    /**
     * 行业类别
     */
    @Attribute(name = "businessCategory")
    private String operation;

    @Size(max = 50, message = "【地址】不能超过50个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Attribute(name = "registeredAddress")
    private String address;

    @Attribute(name = "description")
    private String description;

    @Size(max = 20, message = "【负责人】不能超过20个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Attribute(name = "st")
    private String principal;

    @Attribute(name = "telephoneNumber")
    private String phone;

    @NotEmpty(message = "【组织名称】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Size(max = 25, message = "【组织名称】不能超过25个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Attribute(name = "l")
    private String name;


    /**
     * 经营范围
     */
    @Attribute(name = "scopeOfOperation")
    private String scopeOfOperation;

    /**
     * 经营范围Id
     */
    private String scopeOfOperationIds;

    /**
     * 经营范围代码
     */
    private String scopeOfOperationCodes;


    /**
     * 发证机构
     */
    @Attribute(name = "issuingOrgan")
    private String issuingOrgan;


    /**
     * 经营状态
     */
    @Attribute(name = "operatingState")
    private String operatingState;

    /**
     * 省
     */
    @Attribute(name = "provinceName")
    private String provinceName;

    /**
     * 县
     */
    @Attribute(name = "countyName")
    private String countyName;

    /**
     * 市
     */
    @Attribute(name = "street")
    private String cityName;
    /**
     * 行政区划代码
     */
    @Attribute(name = "areaNumber")
    private String areaNumber;

    @Attribute(name = "isarea")
    private String isArea;

    @Attribute(name = "mycic")
    private String cityCode;

    @Attribute(name = "mycoc")
    private String countyCode;

    /**
     * 许可证有效期起
     */
    @Attribute(name = "p7")
    private String licenseValidityStartDate;

    /**
     * 许可证有效期止
     */
    @Attribute(name = "p8")
    private String licenseValidityEndDate;

    /**
     *  联系人
     */
    @Attribute(name = "p9")
    private String contactName;

    /**
     * 许可证有效期止
     */
    @Attribute(name = "upOrganizationCode")
    private String upOrganizationCode;

    /**
     * 管理者组织机构代码
     */
    @Attribute(name = "k0")
    private String managerOrganizationCode;

    /**
     * 经营许可证字别
     */
    @Attribute(name = "k8")
    private String businessLicenseType;
    /**
     * 创建时间
     */
    @Attribute(name = "createTimestamp")
    private String createTimestamp;

    /**
     * 法人电话
     */
    @Attribute(name = "destinationIndicator")
    private String principalPhone;

    public String getBusinessLicenseType() {
        return businessLicenseType;
    }

    public void setBusinessLicenseType(String businessLicenseType) {
        this.businessLicenseType = businessLicenseType;
    }

    private String cid;

    private String pid;

    public String getManagerOrganizationCode() {
        return managerOrganizationCode;
    }

    public void setManagerOrganizationCode(String managerOrganizationCode) {
        this.managerOrganizationCode = managerOrganizationCode;
    }

    private String entryDN;

    public String getEntryDN() {
        return entryDN;
    }

    public void setEntryDN(String entryDN) {
        this.entryDN = entryDN;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Name getId() {
        return id;
    }

    public void setId(Name id) {
        this.id = id;
    }

    public String getOu() {
        return ou;
    }

    public void setOu(String ou) {
        this.ou = ou;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getIsArea() {
        return isArea;
    }

    public void setIsArea(String isArea) {
        this.isArea = isArea;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCountyCode() {
        return countyCode;
    }

    public void setCountyCode(String countyCode) {
        this.countyCode = countyCode;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(String registerDate) {
        this.registerDate = registerDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setScopeOfOperation(String scopeOfOperation) {
        this.scopeOfOperation = scopeOfOperation;
    }

    public String getScopeOfOperation() {
        return scopeOfOperation;
    }

    public String getScopeOfOperationIds() {
        return scopeOfOperationIds;
    }

    public void setScopeOfOperationIds(String scopeOfOperationIds) {
        this.scopeOfOperationIds = scopeOfOperationIds;
    }

    public String getScopeOfOperationCodes() {
        return scopeOfOperationCodes;
    }

    public void setScopeOfOperationCodes(String scopeOfOperationCodes) {
        this.scopeOfOperationCodes = scopeOfOperationCodes;
    }

    public void setIssuingOrgan(String issuingOrgan) {
        this.issuingOrgan = issuingOrgan;
    }

    public String getIssuingOrgan() {
        return issuingOrgan;
    }

    public void setOperatingState(String operatingState) {
        this.operatingState = operatingState;
    }

    public String getOperatingState() {
        return operatingState;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setAreaNumber(String areaNumber) {
        this.areaNumber = areaNumber;
    }

    public String getAreaNumber() {
        return areaNumber;
    }

    public String getLicenseValidityStartDate() {
        return licenseValidityStartDate;
    }

    public void setLicenseValidityStartDate(String licenseValidityStartDate) {
        this.licenseValidityStartDate = licenseValidityStartDate;
    }

    public String getLicenseValidityEndDate() {
        return licenseValidityEndDate;
    }

    public void setLicenseValidityEndDate(String licenseValidityEndDate) {
        this.licenseValidityEndDate = licenseValidityEndDate;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getUpOrganizationCode() {
        return upOrganizationCode;
    }

    public void setUpOrganizationCode(String upOrganizationCode) {
        this.upOrganizationCode = upOrganizationCode;
    }

    public String getAreaName() {
        StringBuffer sb = new StringBuffer();
        sb.append(StringUtil.getBlankIfNull(provinceName)).append(" ").append(
            StringUtil.getBlankIfNull(cityName)).append(" ").append(StringUtil.getBlankIfNull(countyName));
        return sb.toString();
    }

    public String getPrincipalPhone() {
        return principalPhone;
    }

    public void setPrincipalPhone(String principalPhone) {
        this.principalPhone = principalPhone;
    }

    public String getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(String createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrganizationLdap that = (OrganizationLdap) o;
        return Objects.equals(id, that.id) && Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uuid);
    }
}
