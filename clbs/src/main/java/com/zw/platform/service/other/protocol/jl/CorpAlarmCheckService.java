package com.zw.platform.service.other.protocol.jl;

import com.zw.platform.domain.other.protocol.jl.query.CorpCheckReq;
import com.zw.platform.domain.other.protocol.jl.resp.CorpAlarmCheckInfoResp;
import com.zw.platform.util.common.JsonResultBean;

public interface CorpAlarmCheckService {

    /**
     * 获取企业车辆违规考核数据
     * @param info 查询条件
     * @return 企业考核数据
     */
    JsonResultBean corpAlarmCheckDataReleased(CorpCheckReq info) throws Exception;

    /**
     * 获取企业车辆违规考核数据
     * @return 考核数据
     */
    CorpAlarmCheckInfoResp exportCorpAlarmCheckInfo();

}
