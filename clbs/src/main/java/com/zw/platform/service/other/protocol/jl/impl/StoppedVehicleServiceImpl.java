package com.zw.platform.service.other.protocol.jl.impl;

import com.alibaba.fastjson.JSONObject;
import com.cb.platform.util.page.PassCloudResultBean;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Sets;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.connectionparamsset_809.PlantParam;
import com.zw.platform.domain.other.protocol.jl.dto.StoppedVehicleDto;
import com.zw.platform.domain.other.protocol.jl.dto.StoppedVehicleQuery;
import com.zw.platform.domain.other.protocol.jl.dto.StoppedVehicleRecordDto;
import com.zw.platform.domain.other.protocol.jl.dto.StoppedVehicleRecordQuery;
import com.zw.platform.domain.other.protocol.jl.query.AddVehicleStopInfo;
import com.zw.platform.domain.other.protocol.jl.xml.EtBaseElement;
import com.zw.platform.domain.other.protocol.jl.xml.ResponseRootElement;
import com.zw.platform.domain.other.protocol.jl.xml.SuccessContentElement2;
import com.zw.platform.repository.modules.ConnectionParamsConfigDao;
import com.zw.platform.repository.modules.ConnectionParamsSetDao;
import com.zw.platform.repository.other.protocol.jl.StoppedVehicleDao;
import com.zw.platform.service.other.protocol.jl.StoppedVehicleService;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MapUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.jl.JiLinConstant;
import com.zw.platform.util.jl.JiLinOrgExamineInterfaceBaseParamEnum;
import com.zw.platform.util.jl.JiLinOrgExamineUtil;
import com.zw.protocol.util.ProtocolTypeUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.zw.platform.util.jl.JiLinConstant.MSG;
import static com.zw.platform.util.jl.JiLinConstant.RESULT;
import static com.zw.platform.util.jl.JiLinConstant.RESULT_FAULT;
import static com.zw.platform.util.report.PaasCloudUrlEnum.STOPPED_VEHICLE_URL;
import static com.zw.protocol.util.ProtocolTypeUtil.T809_JI_LIN_PROTOCOL_809_2013;

/**
 * 停运车辆
 * @author create by zhouzongbo
 */
@Service
public class StoppedVehicleServiceImpl implements StoppedVehicleService {

    @Autowired
    private StoppedVehicleDao stoppedVehicleDao;

    @Autowired
    private JiLinOrgExamineUtil jiLinOrgExamineUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private ConnectionParamsSetDao connectionParamsSetDao;

    @Autowired
    private ConnectionParamsConfigDao connectionParamsConfigDao;

    @Override
    public PassCloudResultBean page(StoppedVehicleQuery query) {
        Map<String, String> param = new HashMap<>();
        param.put("userName", SystemHelper.getCurrentUsername());
        param.put("date", query.getDate());
        param.put("plateId", query.getPlatformId());
        param.put("type", query.getType());
        param.put("pageSize", String.valueOf(query.getLength()));
        param.put("page", String.valueOf(query.getPage()));
        param.put("fuzzyQueryParam", query.getSimpleQueryParam());
        String queryResult = HttpClientUtil.send(STOPPED_VEHICLE_URL, param);
        return PassCloudResultBean
            .getPageInstance(queryResult, data -> JSONObject.parseArray(data, StoppedVehicleDto.class));
    }

    @Override
    public JsonResultBean upload(List<StoppedVehicleRecordDto> list) throws Exception {
        JSONObject result = new JSONObject();
        Set<String> monitorIds = list.stream().map(StoppedVehicleRecordDto::getMonitorId).collect(Collectors.toSet());
        List<RedisKey> monitorKeys = RedisKeyEnum.MONITOR_INFO.ofs(monitorIds);
        List<Map<String, String>> monitorData = RedisHelper.batchGetHashMap(monitorKeys);
        Map<String, BindDTO> configMap = monitorData.stream().map(o -> MapUtil.mapToObj(o, BindDTO.class))
            .collect(Collectors.toMap(BindDTO::getId, Function.identity()));
        List<AddVehicleStopInfo> uploadList = new ArrayList<>();
        list.forEach(dto -> {
            String monitorId = dto.getMonitorId();
            BindDTO config = configMap.get(monitorId);
            dto.setPlateColor(config.getPlateColor());
            dto.setGroupName(config.getOrgName());
            dto.setMonitorName(config.getName());

            AddVehicleStopInfo info = new AddVehicleStopInfo();
            info.setVehicleNo(dto.getMonitorName());
            info.setPlateColorId(JiLinConstant.CLBS_COLOR_JILIN_TRANSLATOR.b2p(dto.getPlateColor()));
            info.setStopCauseCode(dto.getStopCauseCode());
            info.setStartDate(DateUtil.formatDate(dto.getStartDate(), DateUtil.DATE_YMD_FORMAT));
            info.setEndDate(DateUtil.formatDate(dto.getEndDate(), DateUtil.DATE_YMD_FORMAT));
            uploadList.add(info);
        });
        ResponseRootElement<SuccessContentElement2> response = jiLinOrgExamineUtil
            .sendExamineRequest(uploadList, JiLinOrgExamineInterfaceBaseParamEnum.ADD_VEHICLE_STOP_INFO,
                SuccessContentElement2.class);
        EtBaseElement etBase = response.getData().getEtBase();
        String currentUsername = SystemHelper.getCurrentUsername();
        if (Objects.nonNull(etBase)) {
            result.put(RESULT, RESULT_FAULT);
            result.put(MSG, etBase.getMsg());
            list.forEach(dto -> {
                dto.setUploadState(StoppedVehicleRecordDto.ERROR_CODE);
                dto.setErrorMsg(etBase.getMsg());
                dto.setUploadTime(new Date());
                dto.setOperator(currentUsername);
            });
        } else {
            List<SuccessContentElement2> content = response.getData().getContent();
            if (CollectionUtils.isNotEmpty(content)) {
                SuccessContentElement2 successContentElement = content.get(0);
                list.forEach(dto -> {
                    dto.setUploadState(successContentElement.getResult());
                    dto.setUploadTime(new Date());
                    dto.setOperator(currentUsername);
                });
                result.put(RESULT, successContentElement.getResult());
            }
        }
        stoppedVehicleDao.addRecode(list);
        return new JsonResultBean(result);
    }

    @Override
    public Page<StoppedVehicleRecordDto> recordPage(StoppedVehicleRecordQuery query) {
        if (StringUtils.isNotBlank(query.getSimpleQueryParam())) {
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam()));
        }
        if (CollectionUtils.isEmpty(query.getVehicleIds())) {
            // 没传 权限下所有车
            Set<String> monitorIds = userService.getCurrentUserMonitorIds();
            // 过滤改平台的车
            Set<String> forwardVehicleIdList = connectionParamsConfigDao
                .findForwardVehiclesByProtocolType(Integer.valueOf(T809_JI_LIN_PROTOCOL_809_2013)).stream()
                .map(VehicleInfo::getId).collect(Collectors.toSet());
            Set<String> intersection = Sets.intersection(forwardVehicleIdList, monitorIds);
            List<String> userMonitorIds = new ArrayList<>(intersection);
            query.setVehicleIds(userMonitorIds);
        }
        Page<StoppedVehicleRecordDto> result =
            PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                .doSelectPage(() -> stoppedVehicleDao.recordPage(query));
        if (CollectionUtils.isNotEmpty(result)) {
            result.forEach(this::transformData);
        }
        return result;
    }

    @Override
    public void exportStoppedRecord(StoppedVehicleRecordQuery query, HttpServletResponse response) throws IOException {
        ExportExcel export = new ExportExcel(null, StoppedVehicleRecordDto.class, 1, null);
        if (StringUtils.isBlank(query.getIds())) {
            // 没传 权限下所有车
            Set<String> monitorIds = userService.getCurrentUserMonitorIds();
            // 过滤改平台的车
            Set<String> forwardVehicleIdList = connectionParamsConfigDao
                .findForwardVehiclesByProtocolType(Integer.valueOf(T809_JI_LIN_PROTOCOL_809_2013)).stream()
                .map(VehicleInfo::getId).collect(Collectors.toSet());
            Set<String> intersection = Sets.intersection(forwardVehicleIdList, monitorIds);
            List<String> userMonitorIds = new ArrayList<>(intersection);
            query.setVehicleIds(userMonitorIds);
        } else {
            List<String> vehicleIds = Arrays.asList(query.getIds().split(","));
            query.setVehicleIds(vehicleIds);
        }
        List<StoppedVehicleRecordDto> stoppedVehicleRecordDtos = stoppedVehicleDao.recordPage(query);
        if (CollectionUtils.isNotEmpty(stoppedVehicleRecordDtos)) {
            stoppedVehicleRecordDtos.forEach(dto -> {
                transformData(dto);
                if (Objects.equals(dto.getUploadState(), StoppedVehicleRecordDto.ERROR_CODE)) {
                    dto.setUploadStateStr("上传失败(" + dto.getErrorMsg() + ")");
                }
            });

        }
        export.setDataList(stoppedVehicleRecordDtos);
        // 输出导文件
        OutputStream out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
    }

    @Override
    public List<JSONObject> getStoppedPlatformInfo() {
        List<JSONObject> result = new ArrayList<>();
        List<PlantParam> byProtocolType = connectionParamsSetDao
            .get809ByProtocolType(Integer.parseInt(ProtocolTypeUtil.T809_JI_LIN_PROTOCOL_809_2013));
        if (CollectionUtils.isNotEmpty(byProtocolType)) {
            byProtocolType.forEach(plantParam -> {
                JSONObject object = new JSONObject();
                object.put("id", plantParam.getId());
                object.put("name", plantParam.getPlatformName());
                result.add(object);
            });
        }
        return result;
    }

    private void transformData(StoppedVehicleRecordDto recordDto) {
        recordDto.setStartDateStr(DateUtil.getDayStr(recordDto.getStartDate()));
        recordDto.setEndDateStr(DateUtil.getDayStr(recordDto.getEndDate()));
        recordDto.setPlateColorStr(PlateColor.getNameOrBlankByCode(recordDto.getPlateColor()));
        recordDto.setStopCauseCodeStr(getStopCause(recordDto.getStopCauseCode()));
        recordDto.setUploadStateStr(getUploadState(recordDto));
        recordDto.setUploadTimeStr(DateUtil.getDateToString(recordDto.getUploadTime(), null));
    }

    private String getStopCause(Integer code) {
        //报停原因: 1:天气; 2:车辆故障; 3: 路阻; 4: 终端报修; 9: 其他(默认)
        switch (code) {
            case 1:
                return "天气";
            case 2:
                return "车辆故障";
            case 3:
                return "路阻";
            case 4:
                return "终端报修";
            case 9:
                return "其他(默认)";
            default:
                return "";
        }
    }

    private String getUploadState(StoppedVehicleRecordDto recordDto) {
        String state = "上传成功";
        Integer uploadState = recordDto.getUploadState();
        if (Objects.equals(uploadState, StoppedVehicleRecordDto.ERROR_CODE)) {
            String errorMsg = recordDto.getErrorMsg();
            state = "上传失败（" + errorMsg + "）";
        }
        return state;
    }
}
