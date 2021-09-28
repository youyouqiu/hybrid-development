package com.zw.adas.service.report.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.adas.domain.report.deliveryLine.LineRecordDo;
import com.zw.adas.domain.report.deliveryLine.LineRecordDto;
import com.zw.adas.domain.report.query.DeliveryLineQuery;
import com.zw.adas.repository.mysql.riskdisposerecord.LineRecordDao;
import com.zw.adas.service.report.DeliveryLineService;
import com.zw.platform.basic.domain.VehicleDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.connectionparamsset_809.PlantParam;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.repository.modules.ConnectionParamsConfigDao;
import com.zw.platform.repository.modules.LineDao;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.service.functionconfig.FenceConfigService;
import com.zw.platform.service.functionconfig.ManageFenceService;
import com.zw.platform.service.functionconfig.impl.FenceConfigServiceImpl;
import com.zw.platform.util.Translator;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t809.T809Message;
import com.zw.protocol.util.ProtocolTypeUtil;
import com.zw.ws.entity.t808.location.defence.T8080x8606;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/***
 @Author lijie
 @Date 2021/1/8 16:18
 @Description 线路下发
 @version 1.0
 **/
@Service
public class DeliveryLineServiceImpl implements DeliveryLineService {

    public static final Translator<String, Integer> SEND_STATUS = Translator.of("指令已生效", 0,
        "指令未生效", 1, "参数消息有误", 2, "参数不支持", 3,
            "参数下发中", 4, "终端离线，未下发", 5, Translator.Pair.of("终端处理中", 7),
            Translator.Pair.of("终端接收失败", 8));

    @Autowired
    LineRecordDao lineRecordDao;

    @Autowired
    LineDao lineDao;

    @Autowired
    VehicleService vehicleService;

    @Autowired
    ParameterDao parameterDao;

    @Autowired
    private NewVehicleDao newVehicleDao;

    @Autowired
    private ManageFenceService manageFenceService;

    @Autowired
    FenceConfigService fenceConfigService;

    @Autowired
    ConnectionParamsConfigDao connectionParamsConfigDao;

    @Override
    public Page<LineRecordDto> pageList(DeliveryLineQuery query) {
        Page<LineRecordDto> lineRecordDtos = (Page<LineRecordDto>) getList(query);
        return lineRecordDtos;
    }

    private List<LineRecordDto> getList(DeliveryLineQuery query) {
        List<LineRecordDto> lineRecordDtos = lineRecordDao.pageList(query);
        Map<String, BindDTO> vehicleIdOrgIdMap = MonitorUtils.getBindDTOMap(query.getMonitorIds());
        lineRecordDtos.stream().forEach(o -> {
            List<Directive> paramlist = parameterDao.findParameterStatus(o.getVehicleId(), o.getFenceConfigId(), "1",
                o.getSwiftNumber() == null ? "0" : o.getSwiftNumber().toString());
            BindDTO bindDTO = vehicleIdOrgIdMap.get(o.getVehicleId());
            o.setOrgId(bindDTO.getOrgId());
            o.setOrgName(bindDTO.getOrgName());
            o.setVehicleColorStr(PlateColor.getNameOrBlankByCode(o.getVehicleColor().toString()));
            o.setReceiveTimeStr(DateUtil.getDateToString(o.getReceiveTime(), DateUtil.DATE_FORMAT_SHORT));
            if (paramlist != null && !paramlist.isEmpty()) {
                o.setDirStatus(paramlist.get(0).getStatus());
                o.setDirStatusStr(SEND_STATUS.p2b(o.getDirStatus()));
                o.setSendTime(DateUtil.getDateToString(paramlist.get(0).getDownTime(), DateUtil.DATE_FORMAT_SHORT));
            }
        });
        return lineRecordDtos;
    }

    @Override
    public boolean export(DeliveryLineQuery query, HttpServletResponse response) throws IOException {
        List<LineRecordDto> list = getList(query);
        return ExportExcelUtil
            .export(new ExportExcelParam(null, 1, list, LineRecordDto.class, null, response.getOutputStream()));
    }

    /**
     * 上级平台下发线路信息
     * @param message
     */
    @Override
    public void addDrvLineInfo(Message message) {
        //如果是window平台就直接过滤
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return;
        }
        T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
        JSONObject msgBody = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody()));
        String brand = msgBody.getString("vehicleNo");
        Integer vehicleColor = msgBody.getInteger("vehicleColor");
        JSONObject line =
            Optional.ofNullable(msgBody.getJSONObject("data")).map(o -> o.getJSONObject("line")).orElse(null);
        VehicleDO vehicleDO = newVehicleDao.findByBrandAndPlateColor(brand, vehicleColor);
        if (vehicleDO == null || line == null) {
            return;
        }
        String vehicleId = vehicleDO.getId();

        T8080x8606 fence = JSON.parseObject(line.toJSONString(), T8080x8606.class);

        VehicleInfo vehicleInfo = FenceConfigServiceImpl.getVehicleInfo(MonitorUtils.getBindDTO(vehicleId));

        List<PlantParam> plantParams = connectionParamsConfigDao.getConnectionInfoByVehicleId(vehicleId);
        for (PlantParam plantParam : plantParams) {
            if (Objects.equals(plantParam.getProtocolType() + "", ProtocolTypeUtil.T809_HEIPROTOCOL_809_2019) && Objects
                .equals(t809Message.getMsgHead().getMsgGNSSCenterId(), plantParam.getCenterId())) {
                Map<String, Object> fenceConfig = fenceConfigService.findFenceInfo(vehicleId, fence.getLineID());
                String lineUuid;
                String fenceConfigId;
                if (fenceConfig == null) {
                    lineUuid = lineDao.findLineByName(fence.getName());
                    if (lineUuid == null) {
                        lineUuid = UUID.randomUUID().toString();
                        manageFenceService.add809Line(fence, vehicleInfo, lineUuid);
                        fenceConfigId = manageFenceService.add809FenceLineInfo(fence, lineUuid, vehicleId);
                    } else {
                        manageFenceService.update809Line(fence, lineUuid);
                        fenceConfigId = manageFenceService.update809FenceLineInfo(fence, lineUuid, vehicleId);
                    }

                } else {
                    fenceConfigId = fenceConfig.get("fence_id").toString();
                    lineUuid = manageFenceService
                        .update809FenceLineInfo(fence, vehicleInfo, fenceConfig.get("fence_id").toString());
                }
                Integer msgSn =
                    manageFenceService.sendLineToVehicle(fence, vehicleInfo, plantParam.getId(), fenceConfigId);
                LineRecordDo lineRecordDo = new LineRecordDo();
                lineRecordDo.setVehicleId(vehicleId);
                lineRecordDo.setSwiftNumber(Optional.ofNullable(msgSn).orElse(0));
                lineRecordDo.setBrand(brand);
                lineRecordDo.setVehicleColor(vehicleColor);
                lineRecordDo.setReceiveTime(new Date());
                lineRecordDo.setFenceConfigId(fenceConfigId);
                lineRecordDo.setLineId(fence.getLineID().toString());
                lineRecordDo.setLineUuid(lineUuid);
                lineRecordDo.setFlag(1);
                lineRecordDao.insert(lineRecordDo);
            }
        }

    }
}
