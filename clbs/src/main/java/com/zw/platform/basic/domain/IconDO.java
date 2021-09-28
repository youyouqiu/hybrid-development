package com.zw.platform.basic.domain;

import com.zw.platform.commons.SystemHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * zw_c_ico_config
 * @author zhangjuan
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class IconDO extends BaseDO {

    /**
     * 图标名称
     */
    private String icoName;

    /**
     * 图标地址
     */
    private String icoUrl;

    /**
     * 车辆图标系统默认为0，用户上传为1
     */
    private Integer defultState;

    /**
     * 监控对象类型
     */
    private String monitorType;

    public IconDO(String fileName) {
        super();
        this.icoName = fileName;
        this.monitorType = "0";
        this.defultState = 1;
        this.setCreateDataUsername(SystemHelper.getCurrentUsername());
        this.setUpdateDataUsername(SystemHelper.getCurrentUsername());
    }

}
