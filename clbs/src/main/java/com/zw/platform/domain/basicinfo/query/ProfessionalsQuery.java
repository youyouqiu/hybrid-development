package com.zw.platform.domain.basicinfo.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * Title: 从业人员Query
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
 * @author: penghujie
 * @date 2018年4月16日下午4:15:13
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ProfessionalsQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 从业人员信息
     */
    private String id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 所属分组
     */
    private String groupName;

    /**
     * 岗位类型
     */
    private String positionType;

    /**
     * 身份证号
     */
    private String identity;

    /**
     * 工号
     */
    private String jobNumber;

    /**
     * 从业资格证号
     */
    private String cardNumber;

    /**
     * 性别
     */
    private String gender;

    /**
     * 出生年月
     */
    private Date birthday;

    /**
     * 照片
     */
    private String photograph;

    /**
     * 电话
     */
    private String phone;
    /**
     * 紧急联系人
     */
    private String emergencyContact;
    /**
     * 紧急联系人电话
     */
    private String emergencyContactPhone;
    /**
     * 邮箱
     */
    private String email;

    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;
    private String hiredate;
    private String state;
    /**
     * 手机2
     */
    private String phoneTwo;
    /**
     * 手机3
     */
    private String phoneThree;
    /**
     * 座机
     */
    private String landline;
    /**
     * 驾驶证号
     */
    private String drivingLicenseNo;
    /**
     * 驾驶证发证机关
     */
    private String drivingAgencies;
    /**
     * 操作证号
     */
    private String operationNumber;
    /**
     * 操作证发证机关
     */
    private String operationAgencies;

    // * (0:A1(大型客车);1:A2(牵引车);2:A3(城市公交车);
    // * 3:B1(中型客车);4:B2(大型货车);5:C1(小型汽车);
    // * 6:C2(小型自动挡汽车);7:C3(低速载货汽车);8:C4(三轮汽车);
    // * 9:D(普通三轮摩托车);10:E(普通二轮摩托车);11:F(轻便摩托车);
    // * 12:M(轮式自行机械车);13:N(无轨电车);14:P(有轨电车))
    /**
     * @author tianzhangxu
     * @date 2019.6.28 10.37
     * 准驾车型
     */
    private String drivingType;
    // /**
    //  * 准驾车型(用于导入导出)
    //  */
    // private String drivingTypeForExport;
    /**
     * 准驾有效期起
     */
    private Date drivingStartDate;
    /**
     * 准驾有效期至
     */
    private Date drivingEndDate;
    /**
     * 提前提醒天数
     */
    private Integer remindDays;
}
