package com.zw.api2.swaggerEntity;

import io.swagger.annotations.ApiParam;
import lombok.Data;


/***
 @Author zhengjc
 @Date 2019/2/14 14:02
 @Description 用户修改
 @version 1.0
 **/
@Data
public class SwaggerUserUpdateBean {
    /**
     * 用户id
     */
    @ApiParam(value = "需修改的用户id", required = true)
    private String userId;

    /**
     * 用户名
     */
    @ApiParam(value = "用户名,长度4——25", required = true)
    private String username;

    @ApiParam(value = "密码(不修改则不填)，长度6——25")
    private String password;

    @ApiParam(value = "所属企业id", required = true)
    private String groupId;

    @ApiParam(value = "所属企业名称", required = true)
    private String groupName;

    @ApiParam(value = "用户状态：启用(1)/停用(0)", defaultValue = "1")
    private String state;

    @ApiParam(value = "用于修改用户时标记是否修改过用户名提交")
    private String sign;

    @ApiParam(value = "下发口令")
    private String sendDownCommand;

    @ApiParam(value = "授权截止日期")
    private String authorizationDate;

    @ApiParam(value = "真实姓名,长度2——20")
    private String fullName;

    @ApiParam(value = "性别(1:男；2:女)")
    private String gender;

    @ApiParam(value = "电话号码")
    private String mobile;

    @ApiParam(value = "性别(1:男；2:女)")
    private String mail;
}
