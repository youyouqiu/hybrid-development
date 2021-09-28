package com.zw.platform.basic.domain;

import com.zw.platform.commons.SystemHelper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

/**
 * zw_m_people_basic_info
 *
 * @author zhangjuan 2020-10-20
 */
@Data
@NoArgsConstructor
public class PeopleBasicDO {
    /**
     * 人员基础信息关联表
     */
    private String id;
    /**
     * 人员id
     */
    private String peopleId;
    /**
     * 基础信息id
     */
    private String basicId;
    /**
     * 类型 1：技能，2：驾照类别
     */
    private Integer type;
    private Integer flag;
    private Date createDataTime;
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;

    public PeopleBasicDO(String peopleId, String basicId, Integer type) {
        this.id = UUID.randomUUID().toString();
        this.peopleId = peopleId;
        this.basicId = basicId;
        this.type = type;
        this.createDataTime = new Date();
        this.createDataUsername = SystemHelper.getCurrentUsername();
        this.flag = 1;
    }
}
