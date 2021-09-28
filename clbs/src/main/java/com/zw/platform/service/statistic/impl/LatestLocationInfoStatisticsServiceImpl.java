package com.zw.platform.service.statistic.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.statistic.info.LatestLocationInfo;
import com.zw.platform.domain.statistic.info.LatestLocationInfoData;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.service.statistic.LatestLocationInfoStatisticsService;
import com.zw.platform.util.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

/**
 * @author penghj
 * @version 1.0
 */
@Service
public class LatestLocationInfoStatisticsServiceImpl implements LatestLocationInfoStatisticsService {

    @Resource
    private PositionalService positionalService;

    @Resource
    private UserService userService;

    @Override
    public JsonResultBean getLatestLocationInfoByF3Pass(String monitorIdStr, String queryTime) {
        List<LatestLocationInfo> result = new ArrayList<>();
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_LATEST_LOCATION_INFO_LIST.of(userUuid);
        if (RedisHelper.isContainsKey(redisKey)) {
            RedisHelper.delete(redisKey);
        }
        if (StringUtils.isBlank(monitorIdStr) || StringUtils.isBlank(queryTime)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数错误");
        }
        LocalDateTime queryDateTime = DateUtil.YMD_HMS.ofDateTime(queryTime).orElse(null);
        if (queryDateTime == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数错误");
        }
        Set<String> monitorIds = new HashSet<>(Arrays.asList(monitorIdStr.trim().split(",")));
        // 两天前
        LocalDateTime twoDaysAgoDateTime = queryDateTime.plusDays(-2);
        Map<String, String> queryParam = new HashMap<>(10);
        queryParam.put("monitorIds", StringUtils.join(monitorIds, ","));
        queryParam.put("startTime", DateUtil.YMD_HMS_SHORT.format(twoDaysAgoDateTime).orElse(""));
        queryParam.put("endTime", DateUtil.YMD_HMS_SHORT.format(queryDateTime).orElse(""));
        String queryResult = HttpClientUtil.send(PaasCloudUrlEnum.LATEST_LOCATION_INFO_REPORT_URL, queryParam);
        JSONObject queryResultJsonObj = JSON.parseObject(queryResult);
        if (Objects.isNull(queryResultJsonObj) || queryResultJsonObj.getInteger("code") != 10000) {
            return new JsonResultBean(false, "数据查询异常");
        }
        List<LatestLocationInfoData> data =
            JSONObject.parseArray(queryResultJsonObj.getString("data"), LatestLocationInfoData.class);
        Map<String, LatestLocationInfoData> map = new HashMap<>(16);
        if (CollectionUtils.isNotEmpty(data)) {
            map = data.stream()
                .collect(Collectors.toMap(LatestLocationInfoData::getMonitorId, Function.identity(), (v1, v2) -> v1));
        }
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(monitorIds);
        for (String monitorId : monitorIds) {
            LatestLocationInfo info = new LatestLocationInfo();
            if (map.containsKey(monitorId)) {
                LatestLocationInfoData lastData = map.get(monitorId);
                String time = DateUtil.YMD_HMS.format(DateUtil.fromTimestamp(lastData.getTime() * 1000)).orElse("");
                info.setLocationTime(time);
                info.setVtime(lastData.getTime());
                info.setLatitude(lastData.getLatitude());
                info.setLongtitude(lastData.getLongitude());
                if (Objects.equals(lastData.getIsOnline(), 1)) {
                    info.setIsOnline(1);
                    info.setIsOnlineStr("在线");
                } else {
                    info.setIsOnline(2);
                    info.setIsOnlineStr("不在线");
                }
                info.setSpeed(lastData.getSpeed());
                info.setAccStatus(lastData.getAccStatus() == 0 ? "关" : "开");
            } else {
                info.setLocation("近2日内无定位信息");
            }
            BindDTO bindDTO = bindInfoMap.get(monitorId);
            if (bindDTO != null) {
                String deviceType = bindDTO.getDeviceType();
                info.setDeviceTypeStr(ProtocolEnum.getDeviceNameByDeviceType(deviceType));
                info.setPlateNumber(bindDTO.getName());
                info.setGroupName(bindDTO.getOrgName());
                String monitorType = bindDTO.getMonitorType();
                info.setMonitorTypeStr(monitorType != null ? ("0".equals(monitorType) ? "车" :
                    ("1".equals(monitorType) ? "人" : ("2".equals(monitorType) ? "物" : null))) : null);
            }
            result.add(info);
        }
        result.sort(comparing(LatestLocationInfo::getVtime).reversed());
        RedisHelper.addToList(redisKey, result);
        RedisHelper.expireKey(redisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        return new JsonResultBean(result);
    }

    @Override
    public void exportLatestLocationInfo(HttpServletResponse response, String simpleQueryParam) throws Exception {
        ExportExcel export = new ExportExcel(null, LatestLocationInfo.class, 1);
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_LATEST_LOCATION_INFO_LIST.of(userUuid);
        List<LatestLocationInfo> allExportList = RedisHelper.getList(redisKey, LatestLocationInfo.class);
        List<LatestLocationInfo> exportList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(allExportList)) {
            exportList.addAll(allExportList);
            //模糊搜索需要过滤 如果没有搜索则全部导出
            if (StringUtils.isNotBlank(simpleQueryParam)) {
                exportList.clear();
                List<LatestLocationInfo> filterExportList = allExportList.stream()
                    .filter(info -> StringUtils.isNotBlank(info.getPlateNumber())
                        && info.getPlateNumber().contains(simpleQueryParam))
                    .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(filterExportList)) {
                    exportList.addAll(filterExportList);
                }
            }
            for (LatestLocationInfo info : allExportList) {
                if (!"近2日内无定位信息".equals(info.getLocation())) {
                    String address = positionalService.getAddress(info.getLongtitude(), info.getLatitude());
                    info.setLocation(StringUtils.isBlank(address) ? "未定位" : address);
                }
            }
        }
        export.setDataList(exportList);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
    }
}
