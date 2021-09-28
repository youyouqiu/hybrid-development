package com.zw.api2.swaggerEntity;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.ApiParam;
import lombok.Data;
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
 *
 * @version 1.0
 * @author: wangying
 * @date 2016年8月3日下午6:08:40
 */
@Data
@Entry(objectClasses = {"organizationalUnit", "top"}, base = "ou=organization")
public final class SwaggerOrganizationLdap {

    private static final long serialVersionUID = 1L;

    @ApiParam(value = "组织id", required = true)
    private String pid;

    @NotEmpty(message = "【组织名称】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Size(max = 25, message = "【组织名称】不能超过25个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Attribute(name = "l")
    @ApiParam(value = "组织名称", required = true)
    private String name;

    @Size(max = 20, message = "【组织机构代码】不能超过10个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Attribute(name = "postalCode")
    @ApiParam(value = "组织机构代码,不能超过10个字符")
    private String organizationCode;//组织机构代码

    @Attribute(name = "businessCategory")
    @ApiParam(value = "行业类别")
    private String operation;//行业类别

    @Attribute(name = "scopeOfOperation")
    @ApiParam(value = "经营范围")
    private String scopeOfOperation;//经营范围

    @Size(max = 20, message = "【经营许可证号】不能超过13个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Attribute(name = "p0")
    @ApiParam(value = "经营范围,不能超过13个字符")
    private String license;//企业营业执照代码

    @Attribute(name = "issuingOrgan")
    @ApiParam(value = "发证机构")
    private String issuingOrgan;//发证机构

    @Attribute(name = "operatingState")
    @ApiParam(value = "经营状态，1营业，2停业，3整改，4停业整顿，5歇业，6注销，7其他")
    private String operatingState;//经营状态

    @Attribute(name = "provinceName")
    @ApiParam(value = "省")
    private String provinceName;//省

    @Attribute(name = "street")
    @ApiParam(value = "市")
    private String cityName;//市

    @Attribute(name = "countyName")
    @ApiParam(value = "县")
    private String countyName;//县

    @Attribute(name = "areaNumber")
    @ApiParam(value = "行政区划代码")
    private String areaNumber;//行政区划代码

    @Attribute(name = "physicalDeliveryOfficeName")
    @ApiParam(value = "注册成立日期")
    private String registerDate;//注册成立日期

    @Size(max = 20, message = "【负责人】不能超过20个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Attribute(name = "st")
    @ApiParam(value = "负责人")
    private String principal;//负责人

    @Size(max = 50, message = "【地址】不能超过50个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Attribute(name = "registeredAddress")
    @ApiParam(value = "地址")
    private String address;//地址

    @Attribute(name = "description")
    @ApiParam(value = "备注")
    private String description;//描述

    @Attribute(name = "telephoneNumber")
    @ApiParam(value = "电话号码")
    private String phone;
}
