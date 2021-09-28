package com.zw.platform.service.other.protocol.jl.impl;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.other.protocol.jl.query.QueryVehicleServiceInfo;
import com.zw.platform.domain.other.protocol.jl.resp.VehicleServiceInfoResp;
import com.zw.platform.domain.other.protocol.jl.xml.EtBaseElement;
import com.zw.platform.domain.other.protocol.jl.xml.ResponseRootElement;
import com.zw.platform.service.other.protocol.jl.JiLinVehicleOperationStatusService;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.jl.JiLinConstant;
import com.zw.platform.util.jl.JiLinOrgExamineInterfaceBaseParamEnum;
import com.zw.platform.util.jl.JiLinOrgExamineUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.zw.platform.util.jl.JiLinConstant.CLBS_COLOR_JILIN_TRANSLATOR;
import static com.zw.platform.util.jl.JiLinConstant.MSG;
import static com.zw.platform.util.jl.JiLinConstant.RESULT;
import static com.zw.platform.util.jl.JiLinConstant.RUN_STATUS;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/6/15 17:01
 */
@Service
public class JiLinVehicleOperationStatusServiceImpl implements JiLinVehicleOperationStatusService {

    @Autowired
    private JiLinOrgExamineUtil jiLinOrgExamineUtil;

    @Override
    public JsonResultBean listOperationStatus(String vehicleId) throws Exception {
        JSONObject result = new JSONObject();
        Map<String, String> configMap = RedisHelper.hgetAll(RedisKeyEnum.MONITOR_INFO.of(vehicleId));
        if (configMap == null) {
            throw new NullPointerException("车辆不存在!");
        }
        QueryVehicleServiceInfo queryVehicleServiceInfo = new QueryVehicleServiceInfo(configMap.get("name"),
            CLBS_COLOR_JILIN_TRANSLATOR.b2p(Integer.parseInt(configMap.get("plateColor"))));
        ResponseRootElement<VehicleServiceInfoResp> response =
            jiLinOrgExamineUtil.sendExamineRequest(Collections.singletonList(queryVehicleServiceInfo),
                JiLinOrgExamineInterfaceBaseParamEnum.QUERY_VEHICLE_SERVICE_INFO, VehicleServiceInfoResp.class);
        EtBaseElement etBase = response.getData().getEtBase();
        if (etBase != null) {
            result.put(RESULT, JiLinConstant.RESULT_FAULT);
            result.put(MSG, etBase.getMsg());
            return new JsonResultBean(result);
        }
        result.put(RESULT, JiLinConstant.RESULT_SUCCESS);
        List<VehicleServiceInfoResp> operationStatusList = response.getData().getContent();
        if (CollectionUtils.isNotEmpty(operationStatusList)) {
            VehicleServiceInfoResp vehicleServiceInfoResp = operationStatusList.get(0);
            vehicleServiceInfoResp.setPlateColorStr(PlateColor
                .getNameOrBlankByCode(CLBS_COLOR_JILIN_TRANSLATOR.p2b(vehicleServiceInfoResp.getPlateColorId())));
            vehicleServiceInfoResp.setRunStatusStr(RUN_STATUS.b2p(vehicleServiceInfoResp.getRunStatus()));
            vehicleServiceInfoResp.setReturnTimeStr(DateUtil.getDateToString(new Date(), null));
            result.put("info", vehicleServiceInfoResp);
        }
        return new JsonResultBean(result);
    }
}
