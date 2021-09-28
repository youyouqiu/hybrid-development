package com.zw.platform.service.other.protocol.jl;

import com.zw.platform.domain.other.protocol.jl.query.CorpCheckReq;
import com.zw.platform.domain.other.protocol.jl.resp.CorpCheckInfoResp;
import com.zw.platform.util.common.JsonResultBean;

public interface CorpCheckService {

    /**
     * 企业考核数据下发结果
     * @param info 查询条件
     * @return 企业考核数据
     */
    JsonResultBean corpCheckDataReleased(CorpCheckReq info) throws Exception;

    /**
     * 获取企业考核数据
     * @return 考核数据
     */
    CorpCheckInfoResp exportCorpCheckInfo();
}
