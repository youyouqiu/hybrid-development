package com.zw.platform.service.other.protocol.jl.impl;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.other.protocol.jl.query.QueryAloneVehicleInfo;
import com.zw.platform.domain.other.protocol.jl.resp.AloneVehicleInfoResp;
import com.zw.platform.domain.other.protocol.jl.xml.EtBaseElement;
import com.zw.platform.domain.other.protocol.jl.xml.ResponseRootElement;
import com.zw.platform.service.other.protocol.jl.QueryVehicleInfoService;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.jl.JiLinConstant;
import com.zw.platform.util.jl.JiLinOrgExamineInterfaceBaseParamEnum;
import com.zw.platform.util.jl.JiLinOrgExamineUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author denghuabing
 * @version V1.0
 * @description: 请求车辆信息数据
 * @date 2020/6/15
 **/
@Service
public class QueryVehicleInfoServiceImpl implements QueryVehicleInfoService {

    @Autowired
    private JiLinOrgExamineUtil jiLinOrgExamineUtil;

    @Override
    public JsonResultBean query(QueryAloneVehicleInfo info) throws Exception {
        JSONObject result = new JSONObject();
        Map<String, String> configMap =
            RedisHelper.hgetAll(RedisKeyEnum.MONITOR_INFO.of(info.getVehicleId()));
        Integer jilinColor =
            JiLinConstant.CLBS_COLOR_JILIN_TRANSLATOR.b2p(Integer.parseInt(configMap.get("plateColor")));
        info.setPlateColorId(jilinColor);
        ResponseRootElement<AloneVehicleInfoResp> response = jiLinOrgExamineUtil
            .sendExamineRequest(Collections.singletonList(info),
                JiLinOrgExamineInterfaceBaseParamEnum.QUERY_ALONE_VEHICLE_INFO, AloneVehicleInfoResp.class);
        EtBaseElement etBase = response.getData().getEtBase();
        if (Objects.nonNull(etBase)) {
            result.put(JiLinConstant.RESULT, JiLinConstant.RESULT_FAULT);
            result.put(JiLinConstant.MSG, etBase.getMsg());
            return new JsonResultBean(result);
        }
        result.put(JiLinConstant.RESULT, JiLinConstant.RESULT_SUCCESS);
        List<AloneVehicleInfoResp> content = response.getData().getContent();
        if (CollectionUtils.isNotEmpty(content)) {
            AloneVehicleInfoResp aloneVehicleInfoResp = content.get(0);
            Integer color = JiLinConstant.CLBS_COLOR_JILIN_TRANSLATOR.p2b(aloneVehicleInfoResp.getPlateColorId());
            aloneVehicleInfoResp.setPlateColorId(color);
            aloneVehicleInfoResp.setPlateColorStr(PlateColor.getNameOrBlankByCode(color));
            aloneVehicleInfoResp.setRunStatusStr(getRunStatus(aloneVehicleInfoResp.getRunStatus()));
            aloneVehicleInfoResp.setVclTradeStr(getTrade(aloneVehicleInfoResp.getVclTrade()));
            aloneVehicleInfoResp.setSendTime(DateUtil.getDateToString(new Date(), null));
            result.put("info", aloneVehicleInfoResp);
        }
        return new JsonResultBean(result);
    }

    /**
     * 运营状态编码(10:运营、21:停运、22:挂失、31:迁出(过户)、32:迁出(转籍)、33:报废、34:歇业、80:注销、90:其 他)
     **/
    private String getRunStatus(String code) {
        switch (code) {
            case "10":
                return "运营";
            case "21":
                return "停运";
            case "22":
                return "挂失";
            case "31":
                return "迁出(过户)";
            case "32":
                return "迁出(转籍)";
            case "33":
                return "报废";
            case "34":
                return "歇业";
            case "80":
                return "注销";
            case "90":
                return "其他";
            default:
                return "";
        }
    }

    /**
     * 所属行业(011:班车客运、 012:包车可与、030:危货运输、900:其他)
     */
    private String getTrade(String code) {
        switch (code) {
            case "011":
                return "班车客运";
            case "012":
                return "包车可与";
            case "030":
                return "危货运输";
            case "900":
                return "其他";
            default:
                return "";
        }
    }
}
