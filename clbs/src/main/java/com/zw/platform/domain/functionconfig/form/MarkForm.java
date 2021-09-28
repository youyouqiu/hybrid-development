package com.zw.platform.domain.functionconfig.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 终端管理Form
 * @author wangying
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MarkForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 新增或者修改标识：0-新增；1-修改
     */
    @NotEmpty(message = "【新增或修改线路标识】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Pattern(message = "【新增或修改线路标识】填值错误！", regexp = "^[0-1]{1}$", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    private String addOrUpdateMarkerFlag = "0";

    /**
     * 被修改标记的id
     */
    private String markerId = "";

    /**
     * 名称
     */
    @NotEmpty(message = "【标注名称】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Size(max = 50, message = "【标注名称】长度不能超过50个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String name;

    /**
     * 经度
     */
    @NotEmpty(message = "【经度】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Double longitude;

    /**
     * 纬度
     */
    @NotEmpty(message = "【纬度】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Double latitude;

    private Integer radius;

    /**
     * 类型
     */
    private String type;

    private String groupId; // 组织id

    /**
     * 描述
     */
    @Size(max = 255, message = "【描述】长度不能超过255个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String description;

    /**
     * 标注类型图标:0:水滴,1:圆圈
     */
    private Integer markIcon = 0;

    /**
     * 围栏种类
     */
    private String typeId;
}
