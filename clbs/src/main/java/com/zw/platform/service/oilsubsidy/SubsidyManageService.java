package com.zw.platform.service.oilsubsidy;

import com.zw.platform.domain.oilsubsidy.subsidyManage.SubsidyManageResp;

import java.util.List;

/**
 * 补传管理service
 */
public interface SubsidyManageService {

    /**
     * 根据企业获取企业下车辆信息
     * @return List<SubsidyManageResp>
     */
    List<SubsidyManageResp> getDetail(String orgIds);
}
