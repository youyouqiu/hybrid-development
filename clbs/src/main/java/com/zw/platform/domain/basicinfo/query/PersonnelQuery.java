package com.zw.platform.domain.basicinfo.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Tdz on 2016/7/21.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PersonnelQuery extends BaseQueryBean implements Serializable {
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
    /**
     * 备注
     */
    private String remark;
    
    private Short flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;
    
    private String peopleNumber; // 人员编号
    
    private String groupId; // 所属企业
    
    private String assignId; // 所属分组
}
