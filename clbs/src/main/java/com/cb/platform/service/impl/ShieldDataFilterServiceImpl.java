package com.cb.platform.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.domain.ContinuityAnalysisInfo;
import com.cb.platform.service.ShieldDataFilterService;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zhangsq
 * @date 2018/5/15 15:14
 */
@Service
public class ShieldDataFilterServiceImpl implements ShieldDataFilterService {
    private static final Logger logger = LogManager.getLogger(ShieldDataFilterServiceImpl.class);

    @Autowired
    private UserService userService;

    /**
     * 连续性分析报表
     * @param monitorId   车辆id
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param breakSecond 中断时长(s)
     * @param breakDistance 中断距离
     */
    @Override
    public JsonResultBean getContinuityAnalysisList(String monitorId, String startTime, String endTime,
        Integer breakSecond, Double breakDistance) {
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_CONTINUITY_ANALYSIS_LIST.of(userUuid);
        if (RedisHelper.isContainsKey(redisKey)) {
            RedisHelper.delete(redisKey);
        }
        JSONObject result = new JSONObject();
        List<ContinuityAnalysisInfo> shieldDataFilters = new ArrayList<>();
        JSONArray positional = new JSONArray();
        result.put("positional", positional);
        result.put("shieldDataFilters", shieldDataFilters);
        Map<String, String> queryParam = new HashMap<>(16);
        queryParam.put("breakSecond", String.valueOf(breakSecond));
        if (breakDistance != null) {
            queryParam.put("breakDistance", String.valueOf(breakDistance));
        }
        queryParam.put("monitorId", monitorId);
        queryParam.put("endTime", endTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        queryParam.put("startTime", startTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        JSONObject queryResultJsonObj =
            JSON.parseObject(HttpClientUtil.send(PaasCloudUrlEnum.CONTINUITY_ANALYSIS_LIST_URL, queryParam));
        if (queryResultJsonObj == null || queryResultJsonObj.getInteger("code") != 10000) {
            return new JsonResultBean(JsonResultBean.FAULT, "查询数据异常！");
        }
        JSONObject data = queryResultJsonObj.getJSONObject("data");
        if (data == null) {
            return new JsonResultBean(result);
        }
        Map<String, String> userGroupIdAndNameMap = userService.getCurrentUserGroupList().stream()
            .collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
        List<ContinuityAnalysisInfo> continuityAnalysisInfoList =
            JSONObject.parseArray(data.getString("breakDetail"), ContinuityAnalysisInfo.class);
        if (CollectionUtils.isNotEmpty(continuityAnalysisInfoList)) {
            BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(monitorId);
            String groupNames = null;
            if (bindDTO != null) {
                userService.setObjectTypeName(bindDTO);
                String groupIds = bindDTO.getGroupId();
                groupNames = Arrays.stream(groupIds.split(",")).map(userGroupIdAndNameMap::get).filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
            }

            for (ContinuityAnalysisInfo continuityAnalysisInfo : continuityAnalysisInfoList) {
                String breakStartTime = DateUtil
                    .getStringToString(continuityAnalysisInfo.getStartTime(), DateUtil.DATE_FORMAT,
                        DateUtil.DATE_FORMAT_SHORT);
                continuityAnalysisInfo.setBreakStartTime(breakStartTime);
                String breakEndTime = DateUtil
                    .getStringToString(continuityAnalysisInfo.getEndTime(), DateUtil.DATE_FORMAT,
                        DateUtil.DATE_FORMAT_SHORT);
                continuityAnalysisInfo.setBreakEndTime(breakEndTime);
                if (bindDTO != null) {
                    continuityAnalysisInfo.setGroupName(bindDTO.getOrgName());
                    continuityAnalysisInfo.setObjectType(bindDTO.getObjectTypeName());
                    continuityAnalysisInfo.setSignColor(PlateColor.getNameOrBlankByCode(bindDTO.getPlateColor()));
                    continuityAnalysisInfo.setAssignmentName(groupNames);
                }
                Long duration = continuityAnalysisInfo.getDuration();
                duration = duration != null ? duration : 0L;
                continuityAnalysisInfo.setDurationStr(DateUtil.formatTime(duration * 1000));
            }
            shieldDataFilters.addAll(continuityAnalysisInfoList);
        }
        JSONArray coordinates = data.getJSONArray("coordinates");
        if (CollectionUtils.isNotEmpty(coordinates)) {
            positional.addAll(coordinates);
        }
        RedisHelper.addToList(redisKey, continuityAnalysisInfoList);
        RedisHelper.expireKey(redisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        return new JsonResultBean(result);
    }

    /**
     * 导出连续性分析报表
     * @param response    response
     * @param monitorId   车辆id
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param breakSecond 中断时长(s)
     * @param breakDistance 中断距离
     */
    @Override
    public void exportContinuityAnalysisList(HttpServletResponse response, String monitorId, String startTime,
        String endTime, Integer breakSecond, Double breakDistance) throws IOException {
        List<ContinuityAnalysisInfo> infoList = new ArrayList<>();
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_CONTINUITY_ANALYSIS_LIST.of(userUuid);
        if (RedisHelper.isContainsKey(redisKey)) {
            infoList = RedisHelper.getList(redisKey, ContinuityAnalysisInfo.class);
        } else {
            JsonResultBean getContinuityAnalysisListResult =
                getContinuityAnalysisList(monitorId, startTime, endTime, breakSecond, breakDistance);
            if (getContinuityAnalysisListResult.isSuccess()) {
                infoList = JSON.parseArray(
                    JSON.parseObject(JSON.toJSONString(getContinuityAnalysisListResult.getObj()))
                        .getString("shieldDataFilters"), ContinuityAnalysisInfo.class);
            } else {
                logger.error("导出连续性分析报表查询数据异常！");
            }
        }

        final ArrayList<String> lngLats = new ArrayList<>(infoList.size());
        for (ContinuityAnalysisInfo info : infoList) {
            lngLats.add(info.getLongitude() + "," + info.getLatitude());
        }
        final Map<String, String> addressMap = AddressUtil.batchInverseAddress(new HashSet<>(lngLats));
        for (int i = 0; i < infoList.size(); i++) {
            ContinuityAnalysisInfo info = infoList.get(i);
            info.setAddress(addressMap.get(lngLats.get(i)));
        }

        ExportExcelUtil.setResponseHead(response, "连续性分析报表");
        ExportExcelUtil.export(
            new ExportExcelParam(null, 1, infoList, ContinuityAnalysisInfo.class, null,
                response.getOutputStream()));
    }

}
