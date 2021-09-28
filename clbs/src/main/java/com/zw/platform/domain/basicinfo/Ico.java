package com.zw.platform.domain.basicinfo;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class Ico extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String icoName;//图标名
    private String icoUrl;//图标地址
    private String defultState;//默认状态
    private String monitorType;//图标类型  0是车，1是人
    private String vehicleIcon;//车辆表info,关联ico表的ID
    private List<String> ids;
    private String ico;//车辆类别表里的图标字段

    /**
     * 车牌颜色（0蓝、1黄、2白、3黑）
     */
    private String plateColor;
    private String phone;
    private String vehicleTypeIcon;//车辆图标
    private String categoryIcon;//车辆类型图标
}
