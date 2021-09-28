package com.zw.platform.service.other.protocol.jl;

import com.zw.platform.domain.other.protocol.jl.query.QueryPlatformCheckInfo;
import com.zw.platform.domain.other.protocol.jl.resp.PlatformCheckInfoResp;
import com.zw.platform.util.common.JsonResultBean;

public interface PlatformCheckService {

    /**
     * 平台考核数据下发结果
     * @param info 查询条件
     * @return 平台考核数据
     */
    JsonResultBean dataReleased(QueryPlatformCheckInfo info) throws Exception;

    /**
     * 获取平台考核数据
     * @return 平台考核数据
     */
    PlatformCheckInfoResp exportPlatformCheckInfo();
}
