package com.zw.platform.domain.basicinfo;

import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.dto.PeopleDTO;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.DateUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 监控人员
 * Created by Tdz on 2016/7/20.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
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

    /**
     * 民族
     */
    private String nation;

    /**
     * 身份证照片(存储路径)
     */
    private String identityCardPhoto;

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

    public Personnel(PeopleDTO peopleDTO) throws Exception {
        this.id = peopleDTO.getId();
        this.name = peopleDTO.getAlias();
        if (StringUtils.isNotBlank(peopleDTO.getBirthday())) {
            this.birthday = DateUtil.parseDate(peopleDTO.getBirthday());
        }
        this.gender = peopleDTO.getGender();
        this.identity = peopleDTO.getIdentity();
        this.address = peopleDTO.getAddress();
        this.phone = peopleDTO.getPhone();
        this.email = peopleDTO.getEmail();
        this.peopleNumber = peopleDTO.getName();
        this.groupId = peopleDTO.getOrgId();
        this.groupName = peopleDTO.getOrgName();
        this.assign = peopleDTO.getGroupName();
        this.deviceNumber = peopleDTO.getDeviceNumber();
        this.simcardNumber = peopleDTO.getSimCardNumber();
        this.monitorType = MonitorTypeEnum.PEOPLE.getType();
        this.bindId = peopleDTO.getConfigId();

    }
}
