package com.zw.platform.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.DateFormatKey;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.connectionparamsset_809.T809ForwardConfig;
import com.zw.platform.domain.netty.ServerParamList;
import com.zw.platform.domain.oilsubsidy.mileagereport.MileageDetailDTO;
import com.zw.platform.repository.vas.ForwardVehicleManageDao;
import com.zw.platform.service.oilsubsidy.OilSubsidyVehicleMileageReportService;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.OilSubsidyCommand;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t809.T809Message;
import com.zw.protocol.msg.t809.body.MainVehicleInfo;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 河南油补--定时自动上报车辆运营公里
 * @author zhangjuan
 */
@Component
public class VehicleOilSubsidyMileReportTask {
    private static final Logger log = LogManager.getLogger(VehicleOilSubsidyMileReportTask.class);
    private static final String YMD_FORMAT = "yyyyMMdd";

    private static final String YMD_HMS_FORMAT = "yyyyMMddHHmmss";
    private static final int SUCCESS = 10000;
    /**
     * 单次处理条数
     */
    private static final int DEAL_SIZE = 100;

    @Autowired
    private ForwardVehicleManageDao forwardVehicleManageDao;

    @Autowired
    private OilSubsidyVehicleMileageReportService mileageReportService;

    @Autowired
    private ServerParamList serverParamList;

    @Scheduled(cron = "${oil.subsidy.vehicle.mile.report.job}")
    public void execute() {
        log.info("河南油补定时上报车辆运营里程任务开始了");
        //获取所有绑定“河南油补809-2011”协议的车辆
        List<T809ForwardConfig> vehicleList = forwardVehicleManageDao.getAllBindVehicle();
        if (vehicleList.isEmpty()) {
            return;
        }

        //获取昨天的时间
        String yesterday = DateUtil.getYesterdayStartTime(DateFormatKey.YYYYMMDD);

        //分批次进行监控对象里程获取和运营里程上报
        List<List<String>> cutMonitorIds = new ArrayList<>();
        int initialCapacity = vehicleList.size() < 16 ? 16 : (int) (vehicleList.size() / 0.75) + 1;
        Map<String, T809ForwardConfig> vehicleMap = new HashMap<>(initialCapacity);
        for (int i = 0; i < vehicleList.size(); i++) {
            T809ForwardConfig config = vehicleList.get(i);
            vehicleMap.put(config.getId(), config);

            int size = cutMonitorIds.size();
            List<String> monitorIds;
            if (i % DEAL_SIZE == 0) {
                monitorIds = new ArrayList<>();
            } else {
                monitorIds = cutMonitorIds.get(size - 1);
            }
            monitorIds.add(config.getId());
            if (Objects.equals(monitorIds.size(), 1)) {
                cutMonitorIds.add(monitorIds);
            }
        }

        //分批次处理监控对象的
        for (List<String> monitorIds : cutMonitorIds) {
            List<MileageDetailDTO> mileageDetailList = getMonitorMileDetail(monitorIds, yesterday);
            if (CollectionUtils.isEmpty(mileageDetailList)) {
                log.info("{}日监控对象【{}】不存在里程数据", yesterday, StringUtils.join(monitorIds, ","));
                return;
            }
            for (MileageDetailDTO mileageDetail : mileageDetailList) {
                T809ForwardConfig config = vehicleMap.get(mileageDetail.getMonitorId());
                if (Objects.isNull(mileageDetail.getStartLat()) || Objects.isNull(mileageDetail.getEndLon())) {
                    log.info("监控对象{}起始经纬度为空，不进行上报", config.getBrand());
                }
                sendMsg(config, mileageDetail);
            }
        }

    }

    private void sendMsg(T809ForwardConfig config, MileageDetailDTO mileageDetail) {
        MainVehicleInfo vehicleInfo = buildVehicleInfo(config, mileageDetail);
        //封装下发的的809消息实体
        Integer msgId = OilSubsidyCommand.T809_UP_EXG_MSG;
        T809Message t809Message =
            MsgUtil.getT809Message(msgId, config.getPlantFormIp(), config.getPlatFormCenterId(), vehicleInfo);
        Message resultMessage = MsgUtil.getMsg(msgId, t809Message).assembleDesc809(config.getPlantFormId());
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(resultMessage);
        log.info("上报里程数据信息:{}", JSON.toJSONString(resultMessage));
    }

    private MainVehicleInfo buildVehicleInfo(T809ForwardConfig config, MileageDetailDTO mileageDetail) {
        MainVehicleInfo mainVehicleInfo = new MainVehicleInfo();
        mainVehicleInfo.setVehicleNo(config.getBrand());
        mainVehicleInfo.setVehicleColor(config.getVehicleColor());
        mainVehicleInfo.setExternalVehicleId(config.getVehicleCode());

        //开始时间和结束时间转换成UTC时间的时间戳
        Long startTime = DateUtil.localToUTCTime(mileageDetail.getStartTime(), YMD_HMS_FORMAT);
        Long endTime = DateUtil.localToUTCTime(mileageDetail.getEndTime(), YMD_HMS_FORMAT);

        JSONObject data = new JSONObject();
        data.put("startTime", startTime / 1000);
        data.put("endTime", endTime / 1000);
        data.put("startLon", getOriginalLongLat(mileageDetail.getStartLat()));
        data.put("endLon", getOriginalLongLat(mileageDetail.getEndLon()));
        data.put("startLat", getOriginalLongLat(mileageDetail.getStartLat()));
        data.put("endLat", getOriginalLongLat(mileageDetail.getEndLat()));
        Double mileDouble = mileageReportService
            .getMile(mileageDetail.getSensorFlag(), mileageDetail.getMileage(), mileageDetail.getGpsMile());
        int mile = Objects.isNull(mileDouble) ? 0 : (int) (mileDouble * 1000);
        data.put("oreratemil", mile);

        mainVehicleInfo.setData(data);
        mainVehicleInfo.setDataType(OilSubsidyCommand.UP_CXG_MSG_TAKE_OPERATEMILE_ACK);
        return mainVehicleInfo;
    }

    private Integer getOriginalLongLat(Double longLat) {
        if (Objects.isNull(longLat)) {
            return null;
        }
        return (int) (longLat * Math.pow(10, 6));
    }

    /**
     * 获取监控对象行驶数据
     * @param monitorIds monitorIds
     * @param yesterday  yesterday
     * @return 监控对象行驶数据
     */
    private List<MileageDetailDTO> getMonitorMileDetail(List<String> monitorIds, String yesterday) {
        //封装查询参数
        String monitorIdStr = StringUtils.join(monitorIds, ",");
        Map<String, String> queryParam = new HashMap<>(16);
        queryParam.put("date", yesterday);
        queryParam.put("monitorIds", monitorIdStr);

        String queryResult = HttpClientUtil.send(PaasCloudUrlEnum.DAILY_MILEAGE_DETAIL_URL, queryParam);
        if (StringUtils.isBlank(queryResult)) {
            log.error("获取{}天监控对象[{}]行驶明细异常", yesterday, monitorIdStr);
            return null;
        }
        JSONObject queryResultJsonObj = JSON.parseObject(queryResult);
        if (queryResultJsonObj == null || queryResultJsonObj.getInteger("code") != SUCCESS) {
            log.error("获取监控对象[{}]行驶明细异常", monitorIdStr);
            return null;
        }

        return JSONObject.parseArray(queryResultJsonObj.getString("data"), MileageDetailDTO.class);
    }

}
