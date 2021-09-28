package com.zw.app.entity.appOCR;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * APP上传监控对象-人 身份证照片和身份证信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PersonnelIdentityInfoUploadEntity extends BaseEntity {
    /**
     * 监控对象-人id
     */
    @NotNull(message = "监控对象ID不能为空")
    private String monitorId;

    /**
     * 名字
     */
    private String name;

    /**
     * 身份证号
     */
    private String identity;

    /**
     * 性别(1:男 2:女)
     */
    private String gender;

    /**
     * 身份证照片(在服务器上存储路径)
     */
    @NotNull(message = "身份证照片不能为空")
    private String identityCardPhoto;

    /**
     * 民族
     */
    private String nation;

    /**
     * 出生年月日
     */
    private String birthday;

    /**
     * 住址
     */
    private String address;

    /**
     * 旧的身份证照片存储路径
     */
    private String oldIdentityCardPhoto;

    /**
     * 数据修改时间
     */
    private Date updateDataTime = new Date();

    /**
     * 修改者username
     */
    private String updateDataUsername;

    public Object[] getArgs() {
        List<Object> args = new ArrayList<>();
        args.add(this);
        return args.toArray();
    }

    public Class<?>[] getArgClasses() {
        Class<?>[] argClass = new Class<?>[1];
        argClass[0] = PersonnelIdentityInfoUploadEntity.class;
        return argClass;
    }

    public String getExceptionInfo() {
        return "APP上传人员身份证信息异常";
    }
}
