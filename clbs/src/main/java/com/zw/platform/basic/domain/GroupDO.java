package com.zw.platform.basic.domain;

import java.util.Date;

import com.zw.platform.basic.dto.GroupDTO;
import lombok.Data;

/**
 * @author wanxing
 * @Title: 分组表实体一一对应关系
 * @date 2020/10/2616:16
 */
@Data
public class GroupDO {

    private String id;
    /**
     * 分组名称
     */
    private String name;
    /**
     * 监控对象类型
     */
    private String type;
    /**
     * 描述
     */
    private String description;
    /**
     * 标记
     */
    private int flag;
    /**
     * 创建数据时间
     */
    private Date createDataTime;
    /**
     * 创建用户
     */
    private String createDataUsername;
    /**
     * 联系人
     */
    private String contacts;
    /**
     *联系电话
     */
    private String telephone;
    /**
     *是否录音， 1：录音 0：不
     */
    private String soundRecording;
    /**
     *对讲群组id
     */
    private String intercomGroupId;
    /**
     *组呼号码
     */
    private String groupCallNumber;
    /**
     *分组类型（分组0，群组1）
     */
    private String types;

    /**
     * 组织ID
     */
    private String orgId;

    public GroupDTO copyDO2DTO() {
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setName(this.getName());
        groupDTO.setId(this.getId());
        groupDTO.setTelephone(this.getTelephone());
        groupDTO.setContacts(this.getContacts());
        groupDTO.setOrgId(this.getOrgId());
        groupDTO.setDescription(this.getDescription());
        return groupDTO;
    }
}

