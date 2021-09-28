package com.zw.platform.service.other.protocol.jl.impl;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.other.protocol.jl.query.CorpCheckReq;
import com.zw.platform.domain.other.protocol.jl.query.QueryCorpCheckInfo;
import com.zw.platform.domain.other.protocol.jl.resp.CorpCheckInfoResp;
import com.zw.platform.domain.other.protocol.jl.xml.EtBaseElement;
import com.zw.platform.domain.other.protocol.jl.xml.ResponseRootElement;
import com.zw.platform.service.other.protocol.jl.CorpCheckService;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
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

import static com.zw.platform.util.jl.JiLinConstant.MSG;
import static com.zw.platform.util.jl.JiLinConstant.RESULT;
import static com.zw.platform.util.jl.JiLinConstant.RESULT_FAULT;
import static com.zw.platform.util.jl.JiLinConstant.RESULT_SUCCESS;

/**
 * 企业考核数据
 * @author xiaoyun
 */
@Service
public class CorpCheckServiceImpl implements CorpCheckService {

    @Autowired
    private JiLinOrgExamineUtil jiLinOrgExamineUtil;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserService userService;

    @Override
    public JsonResultBean corpCheckDataReleased(CorpCheckReq info) throws Exception {
        OrganizationLdap org = organizationService.getOrgByEntryDn(info.getOrgId());
        if (org == null) {
            throw new NullPointerException("企业不存在.");
        }
        final String license = org.getLicense();
        if (StringUtils.isBlank(license)) {
            throw new IllegalArgumentException("经营许可证号不能为空.");
        }
        QueryCorpCheckInfo queryCorpCheckInfo = new QueryCorpCheckInfo();
        BeanUtils.copyProperties(info, queryCorpCheckInfo);
        queryCorpCheckInfo.setLicenceCard(license);
        JSONObject result = new JSONObject();
        ResponseRootElement<CorpCheckInfoResp> response = jiLinOrgExamineUtil
            .sendExamineRequest(Collections.singletonList(queryCorpCheckInfo),
                JiLinOrgExamineInterfaceBaseParamEnum.QUERY_CORP_CHECK_INFO, CorpCheckInfoResp.class);

        EtBaseElement etBase = response.getData().getEtBase();
        if (Objects.nonNull(etBase)) {
            result.put(RESULT, RESULT_FAULT);
            result.put(MSG, etBase.getMsg());
            return new JsonResultBean(result);
        }
        result.put(RESULT, RESULT_SUCCESS);
        String userId = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_CORP_CHECK_INFO.of(userId);
        RedisHelper.delete(redisKey);
        List<CorpCheckInfoResp> content = response.getData().getContent();
        if (content.size() > 0) {
            CorpCheckInfoResp corpCheckInfo = content.get(0);
            corpCheckInfo.setSendTime(DateUtil.getDateToString(new Date(), null));
            RedisHelper.setString(redisKey, JSONObject.toJSONString(corpCheckInfo));
            RedisHelper.expireKey(redisKey, 86400);
            result.put("info", corpCheckInfo);
        }
        return new JsonResultBean(result);
    }

    @Override
    public CorpCheckInfoResp exportCorpCheckInfo() {
        String userId = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_CORP_CHECK_INFO.of(userId);
        String value = RedisHelper.getString(redisKey);
        if (StringUtils.isNotBlank(value)) {
            return JSONObject.parseObject(value, CorpCheckInfoResp.class);
        }
        return null;
    }
}
