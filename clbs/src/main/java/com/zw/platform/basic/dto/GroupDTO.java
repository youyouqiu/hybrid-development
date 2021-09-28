package com.zw.platform.basic.dto;

import com.zw.platform.basic.domain.GroupDO;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author wanxing
 * @Title: 分组实体
 * @date 2020/9/2511:43
 */
@Data
public class GroupDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 分组id
     */
    private String id = UUID.randomUUID().toString();

    /**
     * 分组名称
     */
    @NotEmpty(message = "【分组名称】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Size(max = 30, message = "【分组名称】不能超过30个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "分组名称")
    private String name;

    @ExcelField(title = "所属企业")
    private String orgName;

    /**
     * 监控对象类型
     */
    private String type;

    /**
     * 联系人
     */
    @Size(max = 20, message = "【联系人】不能超过20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "联系人")
    private String contacts;

    /**
     * 电话号码
     */
    @ExcelField(title = "电话号码")
    private String telephone;

    /**
     * 描述
     */
    @Size(max = 50, message = "【描述】不能超过50个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "描述")
    private String description;

    private Short flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    /**
     * 企业ID
     */
    private String orgId;

    /**
     * 企业Dn
     */
    private String orgDn;

    /**
     * 分组类型（分组0，群组1）
     */
    private String types;

    /**
     * 监控对象数量
     */
    private Integer monitorCount;

    /**
     * 在线监控对象数量
     */
    private Integer onLineMonitorCount;

    /**
     * 监控对象id集合（该分组下的监控对象）
     */
    private List<String> monitorIds;

    /**
     * 监控对象ID 不区分人和车
     */
    private String vehicleId;

    /**
     * 当前分组包含的监控对象数量
     */
    private Integer assignmentNumber;

    private Integer orderNum = 0;

    /**
     * 是否达到分组最大存储个数100
     */
    private Boolean isMaxSize = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroupDTO groupDto = (GroupDTO) o;
        return getId() != null ? getId().equals(groupDto.getId()) : groupDto.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    public Assignment translate() {
        Assignment assignment = new Assignment();
        assignment.setId(this.getId());
        assignment.setName(this.getName());
        assignment.setGroupName(this.getOrgName());
        assignment.setContacts(this.getContacts());
        assignment.setTelephone(this.getTelephone());
        assignment.setDescription(this.getDescription());
        assignment.setGroupId(this.getOrgId());
        return assignment;
    }

    public GroupDO copyDTO2DO() {
        GroupDO groupDO = new GroupDO();
        groupDO.setName(this.getName());
        groupDO.setId(this.getId());
        groupDO.setTelephone(this.getTelephone());
        groupDO.setContacts(this.getContacts());
        groupDO.setDescription(this.getDescription());
        return groupDO;
    }
}

