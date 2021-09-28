package com.zw.platform.domain.basicinfo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 人员信息
 *
 * @author  Tdz
 * @create 2017-05-05 15:49
 **/
@Data
public class PersonnelInfo  implements Serializable {
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
    @DateTimeFormat(pattern="yyyy-MM-dd")
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

    private int softwareFance;

}
