package com.zw.platform.domain.basicinfo;


import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


/**
 * 人员信息 @author  Tdz
 * @create 2017-05-05 15:58
 **/
@Data
public class PeopleInfo {

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

    private String deviceId; // 终端id

    private String deviceType; // 终端类型

    /**
     * 分组名称
     */
    private String assignmentName;

    /** 下发字段 */
    private String assignmentId; //分组id

    private String mobile;// sim卡号

    private String professionalsName;// 从业人员名字

    private String cardNumber;// 从业人员电话


}
