package com.zw.platform.service.reportManagement.impl;

import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BusinessScopeDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.basic.repository.NewVehicleTypeDao;
import com.zw.platform.basic.service.BusinessScopeService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.reportManagement.form.VehGeneralInfo;
import com.zw.platform.domain.reportManagement.query.VehGeneralInfoQuery;
import com.zw.platform.service.reportManagement.VehGeneralInfoService;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/***
 @Author zhengjc
 @Date 2019/4/30 17:00
 @Description 车辆综合信息报表
 @version 1.0
 **/
@Service
public class VehGeneralInfoServiceImpl implements VehGeneralInfoService {
    private static Logger log = LogManager.getLogger(VehGeneralInfoServiceImpl.class);

    @Autowired
    private NewVehicleTypeDao newVehicleTypeDao;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private BusinessScopeService businessScopeService;

    @Override
    public Page<VehGeneralInfo> listVehGeneralInfo(VehGeneralInfoQuery query) {
        List<VehGeneralInfo> vehGeneralInfoList;
        Page<VehGeneralInfo> pageResult = new Page<>(query.getPage().intValue(), query.getLimit().intValue());
        // 权限下排序车id
        List<String> sortVehicle = userService.getValidVehicleId(null, null, null, null, true);
        Set<String> vids;
        if (StringUtils.isNotBlank(query.getVehicleIds())) {
            List<String> ids = Arrays.asList(query.getVehicleIds().split(","));
            Set<String> finalVids = new HashSet<>();
            sortVehicle.forEach(id -> {
                if (ids.contains(id)) {
                    finalVids.add(id);
                }
            });
            vids = new HashSet<>(finalVids);
        } else {
            vids = new HashSet<>(sortVehicle);
        }
        vehGeneralInfoList = fuzzySearch(query, pageResult, vids);

        return RedisQueryUtil.getListToPage(vehGeneralInfoList, query, Math.toIntExact(pageResult.getTotal()));
    }

    @Override
    public List<VehicleTypeDTO> getVehTypes() {
        return newVehicleTypeDao.getByKeyword(null);
    }

    /**
     * 导出车辆综合信息报表
     * @param response
     * @throws Exception
     */
    @Override
    @SuppressWarnings("unchecked")
    public void exportVehicleGeneralInfo(HttpServletResponse response) throws Exception {
        ExportExcelUtil.setResponseHead(response, "车辆综合信息报表");
        // 获取导出数据
        RedisKey redisKey = HistoryRedisKeyEnum.VEHICLE_COMPREHENSIVE_INFO.of(userService.getCurrentUserUuid());
        List<VehGeneralInfo> vehGeneralInfoList = RedisHelper.getList(redisKey, VehGeneralInfo.class);
        ExportExcelUtil.export(
            new ExportExcelParam("", 1, vehGeneralInfoList, VehGeneralInfo.class, null, response.getOutputStream()));
    }

    private List<VehGeneralInfo> fuzzySearch(VehGeneralInfoQuery query, Page<VehGeneralInfo> result, Set<String> vids) {
        // 筛选符合 条件的Id
        List<Map<String, String>> bindInfos = RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(vids));
        if (StringUtils.isNotBlank(query.getSimpleQueryParam())) {
            bindInfos = bindInfos.stream().filter(o -> o.get("name").contains(query.getSimpleQueryParam()))
                .collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(query.getDeviceNumber())) {
            bindInfos = bindInfos.stream().filter(o -> o.get("deviceNumber").contains(query.getDeviceNumber()))
                .collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(query.getSimCard())) {
            bindInfos = bindInfos.stream().filter(o -> o.get("simCardNumber").contains(query.getSimCard()))
                .collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(query.getVehType())) {
            bindInfos = bindInfos.stream().filter(o -> o.get("vehicleType").contains(query.getVehType()))
                .collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(query.getProfessional())) {
            bindInfos = bindInfos.stream().filter(o -> StringUtils.isNotBlank(o.get("professionalNames")))
                .filter(o -> o.get("professionalNames").contains(query.getProfessional())).collect(Collectors.toList());
        }
        RedisKey redisKey = HistoryRedisKeyEnum.VEHICLE_COMPREHENSIVE_INFO.of(userService.getCurrentUserUuid());
        RedisHelper.delete(redisKey);
        if (CollectionUtils.isEmpty(bindInfos)) {
            return new ArrayList<>();
        }
        List<VehGeneralInfo> vgiList = getVehGeneralInfoList(bindInfos);
        RedisHelper.addToList(redisKey, vgiList);
        RedisHelper.expireKey(redisKey, 60 * 60);
        result.setTotal(vgiList.size());
        int end = Math.min(result.getPageSize(), vgiList.size());
        return vgiList.stream().skip(result.getStartRow()).limit(end).collect(Collectors.toList());
    }

    private Set<String> initCheckedVidSet(String checkedVids, List<String> sortAssignVehicle) {
        //当前端没有传递车辆id，就默认全部权限的车辆
        Set<String> checkedVehIdSet = new HashSet<>(sortAssignVehicle);
        if (!StringUtil.isNullOrBlank(checkedVids)) {
            checkedVehIdSet = new HashSet<>(Arrays.asList(checkedVids.split(",")));
        }
        return checkedVehIdSet;
    }

    private List<VehGeneralInfo> getVehGeneralInfoList(List<Map<String, String>> bindInfos) {

        List<VehGeneralInfo> vehGeneralInfoList = new ArrayList<>();
        for (Map<String, String> bindInfo : bindInfos) {
            VehGeneralInfo vehGeneralInfo = new VehGeneralInfo();
            vehGeneralInfoList.add(vehGeneralInfo.init(bindInfo));
        }
        initGroupInfo(vehGeneralInfoList);
        return vehGeneralInfoList;
    }

    private void initGroupInfo(List<VehGeneralInfo> vehGeneralInfoList) {
        Set<String> orgIds = vehGeneralInfoList.stream().map(VehGeneralInfo::getGroupId).collect(Collectors.toSet());
        List<BusinessScopeDTO> businessScope = businessScopeService.getBusinessScopeByIds(orgIds);
        Map<String, List<String>> businessScopeMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(businessScope)) {
            businessScopeMap = businessScope.stream().collect(Collectors.groupingBy(BusinessScopeDTO::getId,
                Collectors.mapping(BusinessScopeDTO::getBusinessScope, Collectors.toList())));
        }
        Map<String, OrganizationLdap> orgMap = organizationService.getOrgByUuids(orgIds);
        for (VehGeneralInfo vgi : vehGeneralInfoList) {
            OrganizationLdap orgInfo = orgMap.get(vgi.getGroupId());
            if (orgInfo != null) {
                vgi.setGroupName(orgInfo.getName());
                vgi.setGroupArea(orgInfo.getAreaName());
                if (businessScopeMap.containsKey(vgi.getGroupId())) {
                    String businessScopes =
                        businessScopeMap.get(vgi.getGroupId()).stream().collect(Collectors.joining(","));
                    vgi.setBusinessScope(businessScopes);
                }
                vgi.setIssuingOrgan(orgInfo.getIssuingOrgan());
                vgi.setPrincipal(orgInfo.getPrincipal());
                vgi.setPhone(orgInfo.getPhone());
                vgi.setOrgAddress(orgInfo.getAddress());
                vgi.setLicense(orgInfo.getLicense());
            }
        }
    }
}
