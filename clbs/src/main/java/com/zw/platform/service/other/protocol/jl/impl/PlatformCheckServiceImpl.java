package com.zw.platform.service.other.protocol.jl.impl;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.other.protocol.jl.query.QueryPlatformCheckInfo;
import com.zw.platform.domain.other.protocol.jl.resp.PlatformCheckInfoResp;
import com.zw.platform.domain.other.protocol.jl.xml.EtBaseElement;
import com.zw.platform.domain.other.protocol.jl.xml.ResponseRootElement;
import com.zw.platform.service.other.protocol.jl.PlatformCheckService;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.jl.JiLinOrgExamineInterfaceBaseParamEnum;
import com.zw.platform.util.jl.JiLinOrgExamineUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.zw.platform.util.jl.JiLinConstant.MSG;
import static com.zw.platform.util.jl.JiLinConstant.RESULT;
import static com.zw.platform.util.jl.JiLinConstant.RESULT_FAULT;
import static com.zw.platform.util.jl.JiLinConstant.RESULT_SUCCESS;

/**
 * 平台考核
 * @author xiaoyun
 */
@Service
public class PlatformCheckServiceImpl implements PlatformCheckService {

    @Autowired
    private JiLinOrgExamineUtil jiLinOrgExamineUtil;

    @Autowired
    private UserService userService;

    @Override
    public JsonResultBean dataReleased(QueryPlatformCheckInfo info) throws Exception {
        JSONObject result = new JSONObject();
        ResponseRootElement<PlatformCheckInfoResp> response = jiLinOrgExamineUtil
            .sendExamineRequest(Collections.singletonList(info),
                JiLinOrgExamineInterfaceBaseParamEnum.QUERY_PLATFORM_CHECK_INFO, PlatformCheckInfoResp.class);

        EtBaseElement etBase = response.getData().getEtBase();
        if (Objects.nonNull(etBase)) {
            result.put(RESULT, RESULT_FAULT);
            result.put(MSG, etBase.getMsg());
            return new JsonResultBean(result);
        }

        result.put(RESULT, RESULT_SUCCESS);
        List<PlatformCheckInfoResp> content = response.getData().getContent();
        String userId = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_PLATFORM_CHECK_LIST.of(userId);
        RedisHelper.delete(redisKey);
        if (content.size() > 0) {
            PlatformCheckInfoResp platformCheckInfoResp = content.get(0);
            platformCheckInfoResp.setSendTime(DateUtil.getDateToString(new Date(), null));
            RedisHelper.setString(redisKey, JSONObject.toJSONString(platformCheckInfoResp));
            RedisHelper.expireKey(redisKey, 86400);
            result.put("info", platformCheckInfoResp);
        }

        return new JsonResultBean(result);
    }

    @Override
    public PlatformCheckInfoResp exportPlatformCheckInfo() {
        String userId = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_PLATFORM_CHECK_LIST.of(userId);
        String value = RedisHelper.getString(redisKey);
        if (StringUtils.isNotBlank(value)) {
            return JSONObject.parseObject(value, PlatformCheckInfoResp.class);
        }
        return null;
    }
}
