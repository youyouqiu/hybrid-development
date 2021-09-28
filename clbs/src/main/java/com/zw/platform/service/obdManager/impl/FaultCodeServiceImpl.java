package com.zw.platform.service.obdManager.impl;

import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.statistic.FaultCodeQuery;
import com.zw.platform.domain.statistic.info.FaultCodeInfo;
import com.zw.platform.repository.modules.OBDVehicleTypeDao;
import com.zw.platform.service.obdManager.FaultCodeService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 故障码
 * @author zhouzongbo on 2018/12/28 16:36
 */
@Service
public class FaultCodeServiceImpl implements FaultCodeService {

    @Autowired
    private OBDVehicleTypeDao obdVehicleTypeDao;

    @Autowired
    private UserService userService;

    @Override
    public PageGridBean getFaultCodeList(FaultCodeQuery query) {
        setQuery(query);
        String simpleQueryParam = query.getSimpleQueryParam();
        List<String> moIds = query.getMonitorList();
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(moIds);
        if (StringUtils.isNotEmpty(simpleQueryParam)) {
            Set<String> filterMoIds = bindInfoMap.values()
                .stream()
                .filter(obj -> obj.getName().contains(simpleQueryParam))
                .map(BindDTO::getId)
                .collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(filterMoIds)) {
                return new PageGridBean(query, new Page<>(), true);
            }
            query.setMonitorList(new ArrayList<>(filterMoIds));
        }

        Page<FaultCodeInfo> faultCodeInfoList =
            PageHelperUtil.doSelect(query, () -> obdVehicleTypeDao.findFaultCodeInfoList(query));
        if (CollectionUtils.isEmpty(faultCodeInfoList)) {
            return new PageGridBean(query, new Page<>(), true);
        }
        List<GroupDTO> currentUserGroupList = userService.getCurrentUserGroupList();
        Map<String, String> userGroupIdAndNameMap =
            currentUserGroupList.stream().collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
        for (FaultCodeInfo faultCodeInfo : faultCodeInfoList) {
            BindDTO bindDTO = bindInfoMap.get(faultCodeInfo.getMonitorId());
            if (bindDTO == null) {
                continue;
            }
            String groupIds = bindDTO.getGroupId();
            String groupNames = Arrays.stream(groupIds.split(","))
                .map(userGroupIdAndNameMap::get)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(","));
            faultCodeInfo.setAssignmentName(groupNames);
            faultCodeInfo.setMonitorNumber(bindDTO.getName());
        }
        return new PageGridBean(query, faultCodeInfoList, true);
    }

    private void setQuery(FaultCodeQuery query) {
        String monitorIds = query.getMonitorIds();
        query.setMonitorList(Arrays.asList(monitorIds.split(",")));
    }

    @Override
    public JsonResultBean findExportFaultCode(FaultCodeQuery query) {
        setQuery(query);
        String simpleQueryParam = query.getSimpleQueryParam();
        List<String> moIds = query.getMonitorList();
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(moIds);
        if (StringUtils.isNotEmpty(simpleQueryParam)) {
            Set<String> filterMoIds = bindInfoMap.values()
                .stream()
                .filter(obj -> obj.getName().contains(simpleQueryParam))
                .map(BindDTO::getId)
                .collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(filterMoIds)) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            query.setMonitorList(new ArrayList<>(filterMoIds));
        }
        Page<FaultCodeInfo> exportFaultCodeInfoList = obdVehicleTypeDao.findFaultCodeInfoList(query);
        if (CollectionUtils.isEmpty(exportFaultCodeInfoList)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        List<GroupDTO> currentUserGroupList = userService.getCurrentUserGroupList();
        Map<String, String> userGroupIdAndNameMap = currentUserGroupList
            .stream()
            .collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
        for (FaultCodeInfo faultCodeInfo : exportFaultCodeInfoList) {
            BindDTO bindDTO = bindInfoMap.get(faultCodeInfo.getMonitorId());
            if (bindDTO == null) {
                continue;
            }
            String groupIds = bindDTO.getGroupId();
            String groupNames = Arrays.stream(groupIds.split(","))
                .map(userGroupIdAndNameMap::get)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(","));
            faultCodeInfo.setAssignmentName(groupNames);
            faultCodeInfo.setMonitorNumber(bindDTO.getName());
        }
        String username = SystemHelper.getCurrentUsername();
        RedisKey obdExportFaultCodeListRedisKey = HistoryRedisKeyEnum.OBD_EXPORT_FAULT_CODE_LIST.of(username);
        if (RedisHelper.isContainsKey(obdExportFaultCodeListRedisKey)) {
            RedisHelper.delete(obdExportFaultCodeListRedisKey);
        }
        RedisHelper.addToList(obdExportFaultCodeListRedisKey, exportFaultCodeInfoList);
        RedisHelper.expireKey(obdExportFaultCodeListRedisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public void getExportFaultCode(HttpServletResponse response) throws Exception {
        String username = SystemHelper.getCurrentUsername();
        RedisKey obdExportFaultCodeListRedisKey = HistoryRedisKeyEnum.OBD_EXPORT_FAULT_CODE_LIST.of(username);
        List<FaultCodeInfo> exportFaultCodeInfoList =
            RedisHelper.getList(obdExportFaultCodeListRedisKey, FaultCodeInfo.class);
        ExportExcelUtil.setResponseHead(response, "故障码");
        ExportExcelUtil.export(new ExportExcelParam("", 1, exportFaultCodeInfoList, FaultCodeInfo.class, null,
            response.getOutputStream()));
    }

}
