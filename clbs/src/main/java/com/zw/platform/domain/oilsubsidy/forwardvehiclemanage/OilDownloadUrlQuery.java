package com.zw.platform.domain.oilsubsidy.forwardvehiclemanage;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2020/9/30 9:35
 */
@Data
public class OilDownloadUrlQuery extends BaseQueryBean {
    /**
     * 对接码组织名称
     */
    private String dockingCodeOrgName;

    /**
     * 车牌号
     */
    private String brand;

    /**
     * 匹配状态 0：匹配失败 1：匹配成功
     */
    private Integer matchStatus;

}
