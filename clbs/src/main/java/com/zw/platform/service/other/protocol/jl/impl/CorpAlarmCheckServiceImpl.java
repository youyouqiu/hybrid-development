package com.zw.platform.service.other.protocol.jl.impl;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.other.protocol.jl.query.CorpCheckReq;
import com.zw.platform.domain.other.protocol.jl.query.QueryCorpAlarmCheckInfo;
import com.zw.platform.domain.other.protocol.jl.resp.CorpAlarmCheckInfoResp;
import com.zw.platform.domain.other.protocol.jl.xml.EtBaseElement;
import com.zw.platform.domain.other.protocol.jl.xml.ResponseRootElement;
import com.zw.platform.service.other.protocol.jl.CorpAlarmCheckService;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.jl.JiLinConstant;
import com.zw.platform.util.jl.JiLinOrgExamineInterfaceBaseParamEnum;
import com.zw.platform.util.jl.JiLinOrgExamineUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 企业车辆违规考核
 * @author xiaoyun
 */
@Service
public class CorpAlarmCheckServiceImpl implements CorpAlarmCheckService {

    @Autowired
    private JiLinOrgExamineUtil jiLinOrgExamineUtil;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserService userService;

    @Override
    public JsonResultBean corpAlarmCheckDataReleased(CorpCheckReq info) throws Exception {

        OrganizationLdap org = organizationService.getOrgByEntryDn(info.getOrgId());
        if (org == null) {
            throw new NullPointerException("企业不存在.");
        }
        final String license = org.getLicense();
        if (StringUtils.isBlank(license)) {
            throw new IllegalArgumentException("经营许可证号不能为空.");
        }
        QueryCorpAlarmCheckInfo queryCorpAlarmCheckInfo = new QueryCorpAlarmCheckInfo();
        BeanUtils.copyProperties(info, queryCorpAlarmCheckInfo);
        queryCorpAlarmCheckInfo.setLicenceCard(license);
        JSONObject result = new JSONObject();
        ResponseRootElement<CorpAlarmCheckInfoResp> response = jiLinOrgExamineUtil
            .sendExamineRequest(Collections.singletonList(queryCorpAlarmCheckInfo),
                JiLinOrgExamineInterfaceBaseParamEnum.QUERY_CORP_ALARM_CHECK_INFO, CorpAlarmCheckInfoResp.class);

        EtBaseElement etBase = response.getData().getEtBase();
        if (Objects.nonNull(etBase)) {
            result.put(JiLinConstant.RESULT, JiLinConstant.RESULT_FAULT);
            result.put(JiLinConstant.MSG, etBase.getMsg());
            return new JsonResultBean(result);
        }

        result.put(JiLinConstant.RESULT, JiLinConstant.RESULT_SUCCESS);
        String userId = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_CORP_ALARM_CHECK_INFO.of(userId);
        RedisHelper.delete(redisKey);
        List<CorpAlarmCheckInfoResp> content = response.getData().getContent();
        if (content.size() > 0) {
            CorpAlarmCheckInfoResp corpAlarmCheckInfo = content.get(0);
            corpAlarmCheckInfo.setSendTime(DateUtil.getDateToString(new Date(), null));
            RedisHelper.setString(redisKey, JSONObject.toJSONString(corpAlarmCheckInfo));
            RedisHelper.expireKey(redisKey, 86400);
            result.put("info", corpAlarmCheckInfo);
        }
        return new JsonResultBean(result);

    }

    @Override
    public CorpAlarmCheckInfoResp exportCorpAlarmCheckInfo() {
        String userId = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_CORP_ALARM_CHECK_INFO.of(userId);
        String value = RedisHelper.getString(redisKey);
        if (StringUtils.isNotBlank(value)) {
            return JSONObject.parseObject(value, CorpAlarmCheckInfoResp.class);
        }
        return null;
    }
}
