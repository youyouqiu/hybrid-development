package com.zw.adas.domain.core;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import org.apache.bval.constraints.NotEmpty;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
import javax.validation.constraints.Size;

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
 * @version 1.0
 * @author: wangying
 * @date 2016年8月3日下午6:08:40
 */
@Entry(objectClasses = { "organizationalUnit", "top" }, base = "ou=organization")
public final class OrganizationLdapAdas {

    private static final long serialVersionUID = 1L;

    @Id
    private Name id;

    @Attribute(name = "entryUUID")
    private String uuid;

    @Attribute(name = "ou")
    private String ou;

    @ExcelField(title = "企业名称")
    @NotEmpty(message = "【组织名称】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Size(max = 25, message = "【组织名称】不能超过25个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Attribute(name = "l")
    private String name;

    @ExcelField(title = "组织机构代码")
    @Size(max = 20, message = "【组织机构代码】不能超过10个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Attribute(name = "postalCode")
    private String organizationCode;//组织机构代码

    @ExcelField(title = "行业类别")
    @Attribute(name = "businessCategory")
    private String operation;//行业类别

    @ExcelField(title = "经营范围")
    @Attribute(name = "scopeOfOperation")
    private String scopeOfOperation;//经营范围

    @ExcelField(title = "经营许可证号")
    @Size(max = 20, message = "【经营许可证号】不能超过13个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Attribute(name = "p0")
    private String license;//企业营业执照代码

    @ExcelField(title = "发证机关")
    @Attribute(name = "issuingOrgan")
    private String issuingOrgan;//发证机构

    @ExcelField(title = "经营状态")
    @Attribute(name = "operatingState")
    private String operatingState;//经营状态

    @ExcelField(title = "省/直辖市")
    @Attribute(name = "provinceName")
    private String provinceName;//省

    @ExcelField(title = "市/区")
    @Attribute(name = "street")
    private String cityName;//市

    @ExcelField(title = "县")
    @Attribute(name = "countyName")
    private String countyName;//县

    @ExcelField(title = "行政区划代码")
    @Attribute(name = "areaNumber")
    private String areaNumber;//行政区划代码

    @ExcelField(title = "注册日期")
    @Attribute(name = "physicalDeliveryOfficeName")
    private String registerDate;//注册成立日期

    @ExcelField(title = "企业法人")
    @Size(max = 20, message = "【负责人】不能超过20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Attribute(name = "st")
    private String principal;//负责人

    @ExcelField(title = "电话号码")
    @Attribute(name = "telephoneNumber")
    private String phone;

    @ExcelField(title = "地址")
    @Size(max = 50, message = "【地址】不能超过50个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Attribute(name = "registeredAddress")
    private String address;//地址

    @ExcelField(title = "备注")
    @Attribute(name = "description")
    private String description;//描述

    @Attribute(name = "area")
    private String ldapAddress;

    @NotEmpty(message = "省份不能为空", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String province;

    @NotEmpty(message = "市不能为空", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String city;

    @NotEmpty(message = "区县不能为空", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String county;

    @Attribute(name = "myproc")
    private String provinceCode;

    @Attribute(name = "isarea")
    private String isArea;

    @Attribute(name = "mycic")
    private String cityCode;

    @Attribute(name = "mycoc")
    private String countyCode;

    @Attribute(name = "areaType")
    private String areaType;

    @Attribute(name = "isOperator")
    private String isOperator;

    @Attribute(name = "operatingArea")
    private String operatingArea;

    private String entryDN;

    public String getEntryDN() {
        return entryDN;
    }

    public void setEntryDN(String entryDN) {
        this.entryDN = entryDN;
    }

    public String getIsOperator() {
        return isOperator;
    }

    public void setIsOperator(String isOperator) {
        this.isOperator = isOperator;
    }

    public String getOperatingArea() {
        return operatingArea;
    }

    public void setOperatingArea(String operatingArea) {
        this.operatingArea = operatingArea;
    }

    public String getLdapAddress() {
        return ldapAddress;
    }

    public void setLdapAddress(String ldapAddress) {
        this.ldapAddress = ldapAddress;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    private String cid;

    private String pid;

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

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getAreaType() {
        return areaType;
    }

    public void setAreaType(String areaType) {
        this.areaType = areaType;
    }
}
