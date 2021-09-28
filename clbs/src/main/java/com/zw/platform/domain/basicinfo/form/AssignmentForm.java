package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * Title: 分组管理Form
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
 * @author: wangying
 * @date 2016年10月9日下午6:11:38
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AssignmentForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 分组名称
     */
    @NotEmpty(message = "【分组名称】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Size(max = 30, message = "【分组名称】不能超过30个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "分组名称")
    private String name;

    /**
     * 监控对象类型
     */
    private String type;

    @ExcelField(title = "所属企业")
    private String groupName;

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

    private Integer orderNum = 0;
    /**
     * 分组下的监控对象数量
     */
    private Integer assignmentNumber;
    /**
     * 组织Id
     */
    private String groupId;

    public GroupDTO translate() {
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setId(this.getId());
        groupDTO.setName(this.getName());
        groupDTO.setOrgName(this.getGroupName());
        groupDTO.setContacts(this.getContacts());
        groupDTO.setTelephone(this.getTelephone());
        groupDTO.setDescription(this.getDescription());
        return groupDTO;
    }
}
