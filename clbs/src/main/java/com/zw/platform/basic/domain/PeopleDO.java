package com.zw.platform.basic.domain;

import com.zw.platform.basic.dto.PeopleDTO;
import com.zw.platform.commons.SystemHelper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * zw_m_people_info
 *
 * @author zhangjuan 2020-10-20
 */
@Data
@NoArgsConstructor
public class PeopleDO {

    /**
     * 组件
     */
    private String id;
    /**
     * 姓名
     */
    private String name;
    /**
     * 出生年月
     */
    private String birthday;
    /**
     * 性别 1 男 2女
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
     * flag
     */
    private Integer flag;
    /**
     * create_data_time
     */
    private Date createDataTime;
    /**
     * create_data_username
     */
    private String createDataUsername;
    /**
     * update_data_time
     */
    private Date updateDataTime;
    /**
     * update_data_username
     */
    private String updateDataUsername;
    /**
     * 人员编号
     */
    private String peopleNumber;
    /**
     * 备注信息
     */
    private String remark;
    /**
     * 人员图标id
     */
    private String peopleIcon;
    /**
     * 身份证照片
     */
    private String identityCardPhoto;
    /**
     * nation_id
     */
    private String nationId;
    /**
     * 是否在职 0:离职； 2:在职 1:显示空白
     */
    private Integer isIncumbency;
    /**
     * 职位id
     */
    private String jobId;
    /**
     * 血型id
     */
    private String bloodTypeId;
    /**
     * 资格证id
     */
    private String qualificationId;
    /**
     * org_id
     */
    private String orgId;

    public PeopleDO(PeopleDTO people) {
        if (Objects.isNull(people.getId())) {
            this.id = UUID.randomUUID().toString();
            this.createDataTime = new Date();
            this.createDataUsername = SystemHelper.getCurrentUsername();
            people.setId(this.id);
        } else {
            this.id = people.getId();
            this.updateDataTime = new Date();
            this.updateDataUsername = SystemHelper.getCurrentUsername();
        }
        this.name = people.getAlias();
        this.peopleNumber = people.getName();
        this.birthday = people.getBirthday();
        this.gender = people.getGender();
        this.identity = people.getIdentity();
        this.phone = people.getPhone();
        this.address = people.getAddress();
        this.email = people.getEmail();
        this.flag = 1;
        this.remark = people.getRemark();
        this.identityCardPhoto = people.getIdentityCardPhoto();
        this.nationId = people.getNationId();
        this.isIncumbency = Optional.ofNullable(people.getIsIncumbency()).orElse(1);
        this.jobId = Optional.ofNullable(people.getJobId()).orElse("default");
        this.bloodTypeId = people.getBloodTypeId();
        this.qualificationId = people.getQualificationId();
        this.orgId = people.getOrgId();
    }
}
