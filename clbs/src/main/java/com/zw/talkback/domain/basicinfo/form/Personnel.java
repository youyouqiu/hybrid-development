package com.zw.talkback.domain.basicinfo.form;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 监控人员
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Personnel extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 人
     */
    private String id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 出生年月
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /**
     * 性别
     */
    private String gender;

    /**
     * 身份证号
     */
    private String identity;

    /**
     * 电话
     */
    private String phone;

    /**
     * 地址
     */
    private String address;

    /**
     * 邮箱
     */
    private String email;
    /**
     * 备注
     */
    private String remark;

    private String jobId;

    private String jobName;

    private String jobIconName;

    /**
     * 是否在职 0:离职； 2:在职  1:显示空白
     */
    private Integer isIncumbency = 1;

    private Short flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    private String peopleNumber; // 人员编号

    private String groupId; // 所属分组

    private String groupName;// 分组名称

    private String assign; // 所属分组

    private String deviceNumber; // 终端

    private String simcardNumber; // SIM卡

    private String monitorType; // 监控对象类型 0：车；1：人

    private String bindId;//绑定id

    /**
     * 驾照类别
     */
    private String driverTypeIds;

    private String driverTypeNames;

    /**
     * 资格证
     */
    private String qualificationId;

    private String qualificationName;

    /**
     * 血型
     */
    private String bloodTypeId;

    private String bloodTypeName;

    /**
     * 民族
     */
    private String nationId;

    private String nationName;

    /**
     * 技能
     */
    private String skillIds;

    private String skillNames;
}
