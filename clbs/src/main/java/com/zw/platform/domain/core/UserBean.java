package com.zw.platform.domain.core;

import com.alibaba.fastjson.annotation.JSONField;
import com.zw.platform.commons.LdapNameSerializer;
import com.zw.platform.util.ConverterDateUtil;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import lombok.Data;
import org.apache.bval.constraints.NotEmpty;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


/**
 * @author Administrator
 */
@Entry(objectClasses = {"inetOrgPerson", "organizationalPerson", "person", "top"}, base = "ou=organization")
@Data
public final class UserBean implements ConverterDateUtil {

    @JSONField(deserializeUsing = LdapNameSerializer.class, serializeUsing = LdapNameSerializer.class)
    @Id
    private Name id;

    private Name member;

    @Attribute(name = "cn")
    private String firstName;

    @Attribute(name = "sn")
    private String lastName;

    @Size(min = 6, max = 25, message = "【下发口令】长度为2——25！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Attribute(name = "street")
    private String sendDownCommand;

    @Size(max = 60, message = "【邮箱】长度为 60个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Pattern(message = "【邮箱】格式错误！", regexp = "^\\s*$|\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?", groups = {ValidGroupAdd.class,
        ValidGroupUpdate.class})
    @Attribute(name = "mail")
    private String mail;

    @Attribute(name = "mobile")
    private String mobile;

    @NotEmpty(message = "企业信息不能为空")
    private String groupId;

    @NotEmpty(message = "【用户名】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Size(min = 4, max = 25, message = "【用户名】长度为4——25！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Pattern(message = "【用户名】包含非法字符！", regexp = "^[a-zA-Z0-9\u4e00-\u9fa5-_]+$", groups = {ValidGroupAdd.class,
        ValidGroupUpdate.class})
    @Attribute(name = "uid")
    private String username;

    @Attribute(name = "entryuuid")
    private String uuid;
    /**
     * 企业名称
     */
    private String groupName;

    private String roleName;

    @Size(min = 6, max = 25, message = "【密码】长度为2——25！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Attribute(name = "userPassword")
    @NotEmpty(message = "密码不能为空")
    private String password;

    @Attribute(name = "createTimestamp")
    private String createTimestamp;

    /**
     * 用户状态：启用/停用
     */
    @Attribute(name = "st")
    private String state;

    /**
     * 授权截止日期
     */
    @Attribute(name = "carLicense")
    private String authorizationDate;

    /**
     * 1:男 2：女
     */
    @Pattern(message = "【性别】填值错误！", regexp = "^\\s*$|^[1-2]{1}$", groups = {ValidGroupAdd.class,
        ValidGroupUpdate.class})
    @Attribute(name = "employeeType")
    private String gender;

    @Size(min = 2, max = 20, message = "【真实姓名】长度为2——20！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Attribute(name = "givenName")
    private String fullName;

    /**
     * String 型用户名
     */
    private String userId;

    /**
     * 用于修改用户时标记是否修改过用户名提交
     */
    private String sign;

    /**
     * 身份
     */
    @Attribute(name = "employeeNumber")
    private String identity;
    /**
     * 身份证号
     */
    @Attribute(name = "identityNumber")
    private String identityNumber;
    /**
     * 行业
     */
    @Attribute(name = "businessCategory")
    private String industry;

    private String industryName;
    /**
     * 职务
     */
    @Attribute(name = "departmentNumber")
    private String duty;
    /**
     * 科室
     */
    @Attribute(name = "displayName")
    private String administrativeOffice;

    @Attribute(name = "telexNumber")
    private String dispatcherId;

    @Attribute(name = "socialSecurityNumber")
    private String socialSecurityNumber;
}
