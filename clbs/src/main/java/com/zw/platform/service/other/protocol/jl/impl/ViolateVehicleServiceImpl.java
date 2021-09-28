package com.zw.platform.service.other.protocol.jl.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.other.protocol.jl.dto.ViolateVehicleDO;
import com.zw.platform.domain.other.protocol.jl.dto.ViolateVehicleDTO;
import com.zw.platform.domain.other.protocol.jl.dto.ViolateVehicleExportDTO;
import com.zw.platform.domain.other.protocol.jl.query.AddViolationVehicles;
import com.zw.platform.domain.other.protocol.jl.query.QueryAloneCorpInfo;
import com.zw.platform.domain.other.protocol.jl.query.SingleViolateVehicleReq;
import com.zw.platform.domain.other.protocol.jl.query.ViolateVehicleExportReq;
import com.zw.platform.domain.other.protocol.jl.query.ViolateVehiclePageReq;
import com.zw.platform.domain.other.protocol.jl.query.ViolateVehicleReq;
import com.zw.platform.domain.other.protocol.jl.resp.AloneCorpInfoResp;
import com.zw.platform.domain.other.protocol.jl.xml.EtBaseElement;
import com.zw.platform.domain.other.protocol.jl.xml.ResponseRootElement;
import com.zw.platform.domain.other.protocol.jl.xml.SuccessContentElement;
import com.zw.platform.repository.other.protocol.jl.ViolateVehicleDao;
import com.zw.platform.service.other.protocol.jl.ViolateVehicleService;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MapUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.jl.JiLinConstant;
import com.zw.platform.util.jl.JiLinOrgExamineInterfaceBaseParamEnum;
import com.zw.platform.util.jl.JiLinOrgExamineUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.zw.platform.util.LocalDateUtils.dateTimeFormatterFileName;
import static com.zw.platform.util.jl.JiLinConstant.MSG;
import static com.zw.platform.util.jl.JiLinConstant.RESULT;
import static com.zw.platform.util.jl.JiLinConstant.RESULT_TS;

/**
 * 违规车辆
 * @author create by zhouzongbo on 2020/6/12.
 */
@Service
public class ViolateVehicleServiceImpl implements ViolateVehicleService {

    @Autowired
    private ViolateVehicleDao violateVehicleDao;

    @Autowired
    private JiLinOrgExamineUtil jiLinOrgExamineUtil;

    @Autowired
    private OrganizationService organizationService;

    @Override
    public JsonResultBean insertViolateUpload(ViolateVehicleReq vehicleReq) throws Exception {
        final Set<String> monitorIds = vehicleReq.getMonitorIds();
        // 查询车辆信息
        List<RedisKey> monitorKeys = RedisKeyEnum.MONITOR_INFO.ofs(monitorIds);
        List<Map<String, String>> monitorData = RedisHelper.batchGetHashMap(monitorKeys);
        List<BindDTO> configList = monitorData.stream().map(o -> MapUtil.mapToObj(o, BindDTO.class))
            .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(configList)) {
            return new JsonResultBean(JsonResultBean.FAULT, "车辆信息不存在!");
        }

        // 1.组装上传上级平台数据
        final List<AddViolationVehicles> requestXmlList = configList.stream().map(
            v -> new AddViolationVehicles(v.getName(),
                JiLinConstant.CLBS_COLOR_JILIN_TRANSLATOR.b2p(v.getPlateColor()), vehicleReq.getType(),
                dateTimeFormatterFileName(vehicleReq.getViolateTime()))).collect(Collectors.toList());

        // 2.数据上传
        ResponseRootElement<SuccessContentElement> response = jiLinOrgExamineUtil
            .sendExamineRequest(requestXmlList, JiLinOrgExamineInterfaceBaseParamEnum.ADD_VIOLATION_VEHICLES,
                SuccessContentElement.class);
        JSONObject resultJson = JiLinOrgExamineUtil.upLoadResolverResult(response);

        // 3.组装存储数据
        final List<ViolateVehicleDO> violateVehicles = getViolateVehicles(configList, resultJson, vehicleReq);
        violateVehicleDao.insertBatchViolateUpload(violateVehicles);
        return new JsonResultBean(resultJson);
    }

    @Override
    public JsonResultBean insertBatchViolateUpload(List<SingleViolateVehicleReq> vehicleReqList) throws Exception {
        final List<String> monitorIds =
            vehicleReqList.stream().map(SingleViolateVehicleReq::getMonitorId).collect(Collectors.toList());
        List<RedisKey> monitorKeys = RedisKeyEnum.MONITOR_INFO.ofs(monitorIds);
        List<Map<String, String>> monitorData = RedisHelper.batchGetHashMap(monitorKeys);
        List<BindDTO> configList =
            monitorData.stream().map(o -> MapUtil.mapToObj(o, BindDTO.class)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(configList)) {
            return new JsonResultBean(JsonResultBean.FAULT, "车辆信息不存在!");
        }

        // 1.组装上传上级平台数据
        final Map<String, BindDTO> vehiclePair =
            configList.stream().collect(Collectors.toMap(BindDTO::getId, Function.identity()));
        final List<AddViolationVehicles> requestXmlList = new ArrayList<>();
        for (SingleViolateVehicleReq req : vehicleReqList) {
            final BindDTO v = vehiclePair.get(req.getMonitorId());
            final AddViolationVehicles addViolationVehicles =
                new AddViolationVehicles(v.getName(), JiLinConstant.CLBS_COLOR_JILIN_TRANSLATOR.b2p(v.getPlateColor()),
                    req.getType(), dateTimeFormatterFileName(req.getViolateTime()));
            requestXmlList.add(addViolationVehicles);
        }

        // 2.数据上传
        ResponseRootElement<SuccessContentElement> response = jiLinOrgExamineUtil
            .sendExamineRequest(requestXmlList, JiLinOrgExamineInterfaceBaseParamEnum.ADD_VIOLATION_VEHICLES,
                SuccessContentElement.class);
        JSONObject resultJson = JiLinOrgExamineUtil.upLoadResolverResult(response);

        // 3.组装存储数据
        final List<ViolateVehicleDO> insertViolateVehicleList =
            getViolateVehicles(vehicleReqList, vehiclePair, resultJson);
        violateVehicleDao.insertBatchViolateUpload(insertViolateVehicleList);
        return new JsonResultBean(resultJson);
    }

    @Override
    public Page<ViolateVehicleDTO> listViolateVehicle(ViolateVehiclePageReq req) {
        buildReq(req);
        final Page<ViolateVehicleDO> vehiclePage =
            PageHelperUtil.doSelect(req, () -> violateVehicleDao.listViolateVehicle(req));

        Page<ViolateVehicleDTO> resultPage = PageHelperUtil.copyPage(vehiclePage);
        for (ViolateVehicleDO v : vehiclePage) {
            final ViolateVehicleDTO violateVehicleDTO =
                new ViolateVehicleDTO(v.getId(), v.getMonitorId(), v.getMonitorName(),
                    LocalDateUtils.dateTimeFormat(v.getViolateTime()), JiLinConstant.VIOLATE_TYPE.b2p(v.getType()),
                    PlateColor.getNameOrBlankByCode(v.getPlateColor()), v.getGroupName(),
                    LocalDateUtils.dateTimeFormat(v.getUploadTime()),
                    RESULT_TS.b2p(v.getUploadState()) + (v.getUploadState() == 1 ? "" : "(" + v.getErrorMsg() + ")"),
                    v.getOperator(), v.getErrorMsg());
            resultPage.add(violateVehicleDTO);
        }
        return resultPage;
    }

    @Override
    public void exportViolateList(HttpServletResponse res, ViolateVehicleExportReq req) throws IOException {
        final List<ViolateVehicleDO> violateVehicles = violateVehicleDao.listViolateVehicle(buildExportReq(req));
        final List<ViolateVehicleExportDTO> exportList = violateVehicles.stream().map(
            v -> new ViolateVehicleExportDTO(v.getMonitorName(), LocalDateUtils.dateTimeFormat(v.getViolateTime()),
                JiLinConstant.VIOLATE_TYPE.b2p(v.getType()), PlateColor.getNameOrBlankByCode(v.getPlateColor()),
                v.getGroupName(), LocalDateUtils.dateTimeFormat(v.getUploadTime()),
                RESULT_TS.b2p(v.getUploadState()) + "(" + v.getErrorMsg() + ")", v.getOperator()))
            .collect(Collectors.toList());

        ExportExcelUtil.export(
            new ExportExcelParam("", 1, exportList, ViolateVehicleExportDTO.class, null, res.getOutputStream()));
    }

    @Override
    public JsonResultBean getAloneCorpInfo(String orgId) throws Exception {
        OrganizationLdap org = organizationService.getOrgByEntryDn(orgId);
        if (org == null) {
            throw new NullPointerException("企业不存在.");
        }
        final String license = org.getLicense();
        if (StringUtils.isBlank(license)) {
            throw new IllegalArgumentException("经营许可证号不能为空.");
        }
        QueryAloneCorpInfo queryAloneCorpInfo = new QueryAloneCorpInfo(org.getName(), license);
        final ResponseRootElement<AloneCorpInfoResp> response = jiLinOrgExamineUtil
            .sendExamineRequest(Collections.singletonList(queryAloneCorpInfo),
                JiLinOrgExamineInterfaceBaseParamEnum.QUERY_ALONE_CORP_INFO, AloneCorpInfoResp.class);
        final EtBaseElement etBase = response.getData().getEtBase();
        JSONObject result = new JSONObject();
        if (Objects.nonNull(etBase)) {
            result.put(RESULT, JiLinConstant.RESULT_FAULT);
            result.put(MSG, etBase.getMsg());
            return new JsonResultBean(result);
        }

        result.put(RESULT, JiLinConstant.RESULT_SUCCESS);
        final List<AloneCorpInfoResp> aloneCorpInfoList = response.getData().getContent();
        if (CollectionUtils.isNotEmpty(aloneCorpInfoList)) {
            final AloneCorpInfoResp corpInfoResp = aloneCorpInfoList.get(0);
            corpInfoResp.setSendTime(LocalDateUtils.dateTimeFormat(LocalDateTime.now()));
            return new JsonResultBean(corpInfoResp);
        }

        return new JsonResultBean(result);
    }

    private List<ViolateVehicleDO> getViolateVehicles(List<BindDTO> configList, JSONObject resultJson,
        ViolateVehicleReq vehicleReq) {
        final Date uploadTime = new Date();
        final String operator = SystemHelper.getCurrentUsername();
        final int finalResult = resultJson.getInteger(RESULT);
        final String finalMsg = resultJson.getString(MSG);
        return configList.stream().map(
            v -> new ViolateVehicleDO(UUID.randomUUID().toString(), v.getId(), v.getName(), vehicleReq.getViolateTime(),
                vehicleReq.getType(), v.getPlateColor(), v.getOrgName(), uploadTime, finalResult, operator, finalMsg))
            .collect(Collectors.toList());
    }

    private List<ViolateVehicleDO> getViolateVehicles(List<SingleViolateVehicleReq> vehicleReqList,
        Map<String, BindDTO> vehiclePair, JSONObject resultJson) {
        final int finalResult = resultJson.getInteger(RESULT);
        final String finalMsg = resultJson.getString(MSG);
        final Date uploadTime = new Date();
        final String operator = SystemHelper.getCurrentUsername();
        final List<ViolateVehicleDO> insertViolateVehicleList = new ArrayList<>(vehicleReqList.size());
        for (SingleViolateVehicleReq req : vehicleReqList) {
            final BindDTO v = vehiclePair.get(req.getMonitorId());
            if (Objects.isNull(v)) {
                continue;
            }

            final ViolateVehicleDO violateVehicleDo =
                new ViolateVehicleDO(UUID.randomUUID().toString(), v.getId(), v.getName(),
                    req.getViolateTime(), req.getType(), v.getPlateColor(), v.getOrgName(), uploadTime, finalResult,
                    operator, finalMsg);
            insertViolateVehicleList.add(violateVehicleDo);
        }
        return insertViolateVehicleList;
    }

    private void buildReq(ViolateVehiclePageReq req) {
        final String violateStartDate = req.getViolateStartDate();
        final String violateEndDate = req.getViolateEndDate();
        if (violateStartDate != null && violateEndDate != null) {
            setDate(req, violateStartDate, violateEndDate);
        }
        req.setSimpleQueryParam(StringUtil.fuzzyKeyword(req.getSimpleQueryParam()));
    }

    private void setDate(ViolateVehiclePageReq req, String violateStartDate, String violateEndDate) {
        final LocalDateTime startTime = LocalDateUtils.localDate(violateStartDate).atTime(LocalTime.of(0, 0, 0));
        final LocalDateTime endTime = LocalDateUtils.localDate(violateEndDate).atTime(23, 59, 59);
        req.setViolateStartTime(LocalDateUtils.localDateTimeToDate(startTime));
        req.setViolateEndTime(LocalDateUtils.localDateTimeToDate(endTime));
    }

    private ViolateVehiclePageReq buildExportReq(ViolateVehicleExportReq req) {
        ViolateVehiclePageReq pageReq = new ViolateVehiclePageReq();
        final String violateStartDate = req.getViolateStartDate();
        final String violateEndDate = req.getViolateEndDate();
        if (StringUtils.isNotBlank(violateStartDate) && StringUtils.isNotBlank(violateEndDate)) {
            setDate(pageReq, violateStartDate, violateEndDate);
        }
        pageReq.setSimpleQueryParam(StringUtil.fuzzyKeyword(req.getSimpleQueryParam()));
        pageReq.setMonitorIds(req.getMonitorIds());
        pageReq.setUploadState(req.getUploadState());
        pageReq.setType(req.getType());
        return pageReq;
    }
}
