package com.zw.platform.domain.oilsubsidy.forwardvehiclemanage;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;

import java.util.List;

/**
 * @Author: lijie
 * @Description:
 * @Date: create in 2020/9/30 9:35
 */
@Data
public class OilForwardVehicleQuery extends BaseQueryBean {
    /**
     *  搜索内容
     */
    private String searchParam;

    /**
     * 油补对接码组织
     */
    private String dockingCodeOrgId;

    /**
     * 油补对接码组织ids
     */
    private List<String> dockingCodeOrgIds;

    /**
     * 搜索的油补对接码组织ids
     */
    private List<String> searchDockingCodeOrgIds;

    /**
     * 匹配状态 0：匹配失败 1：匹配成功
     */
    private Integer matchStatus;

}
