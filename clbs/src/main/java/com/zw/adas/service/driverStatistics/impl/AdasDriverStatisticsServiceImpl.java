package com.zw.adas.service.driverStatistics.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.util.page.PassCloudResultUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.zw.adas.domain.driverStatistics.VehicleIcHistoryDO;
import com.zw.adas.domain.driverStatistics.form.AdasDriverStatisticsExport;
import com.zw.adas.domain.driverStatistics.query.AdasDriverQuery;
import com.zw.adas.domain.driverStatistics.show.AdasDriverInfoShow;
import com.zw.adas.domain.driverStatistics.show.AdasDriverStatisticsDetailShow;
import com.zw.adas.domain.driverStatistics.show.AdasDriverStatisticsShow;
import com.zw.adas.domain.driverStatistics.show.AdasProfessionalShow;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasVehicleCardNumDao;
import com.zw.adas.service.driverStatistics.AdasDriverStatisticsService;
import com.zw.adas.service.realTimeMonitoring.AdasRealTimeMonitoringService;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.ConfigHelper;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.service.GroupService;
import com.zw.platform.basic.service.ProfessionalService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.query.IcCardDriverQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.util.JsonUtil;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.TreeUtils;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.excel.TemplateExportExcel;
import com.zw.platform.util.report.PaasCloudAdasUrlEnum;
import com.zw.ws.common.PublicVariable;
import com.zw.ws.entity.vehicle.ClientVehicleInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/***
 @Author zhengjc
 @Date 2019/7/10 10:40
 @Description ????????????service
 @version 1.0
 **/
@Service
public class AdasDriverStatisticsServiceImpl implements AdasDriverStatisticsService {

    private static Logger logger = LogManager.getLogger(AdasDriverStatisticsServiceImpl.class);

    @Autowired
    private ConfigHelper configHelper;

    @Autowired
    private GroupService groupService;

    @Autowired
    private AdasVehicleCardNumDao adasVehicleCardNumDao;

    @Autowired
    private UserService userService;

    @Autowired
    private ProfessionalService professionalService;

    @Autowired
    AdasRealTimeMonitoringService adasRealTimeMonitoringService;

    @Autowired
    private TemplateExportExcel templateExportExcel;

    @Override
    public List<AdasDriverStatisticsShow> getDriverInfo(AdasDriverQuery adasDriverQuery) {
        List<AdasDriverStatisticsShow> results;
        if (StrUtil.isNotBlank(adasDriverQuery.getCardNumbers())) {
            results = getDriverInfoByCardNumbers(adasDriverQuery);
        } else {
            results = getDriverInfoByVehIds(adasDriverQuery);
        }
        return results;
    }

    private List<AdasDriverStatisticsShow> getDriverInfoByVehIds(AdasDriverQuery adasDriverQuery) {
        Set<String> userVidSet = new HashSet<>(Arrays.asList(adasDriverQuery.getVehicleIds().split(",")));
        adasDriverQuery.initParam(adasVehicleCardNumDao.listUniqueIdentificationNumber(userVidSet));
        return queryDriverInfos(adasDriverQuery, userVidSet);
    }

    private List<AdasDriverStatisticsShow> getDriverInfoByCardNumbers(AdasDriverQuery adasDriverQuery) {
        Set<String> userVidSet = userService.getCurrentUserMonitorIds();
        adasDriverQuery.initParam();
        return queryDriverInfos(adasDriverQuery, userVidSet);
    }

    private List<AdasDriverStatisticsShow> queryDriverInfos(AdasDriverQuery adasDriverQuery, Set<String> userVidSet) {
        List<AdasDriverStatisticsShow> results = new ArrayList<>();
        if (CollectionUtils.isEmpty(adasDriverQuery.getCardNumberSet())) {
            return results;
        }

        List<AdasDriverStatisticsShow> driverInfos = getAdasDriverStatisticsFromPass(adasDriverQuery);

        logger.info("???????????????????????????????????????" + driverInfos.size());
        Set<AdasDriverStatisticsShow> driverInfoSet = new HashSet<>(driverInfos);
        driverInfos = new ArrayList<>(driverInfoSet);
        logger.info("???????????????????????????????????????" + driverInfos.size());
        if (CollectionUtils.isEmpty(driverInfos)) {
            return results;
        }
        //?????????????????????id???????????????????????????????????????
        Set<String> driverVidSet = new HashSet<>();
        //????????????????????????????????????????????????????????????????????????????????????????????????
        Set<String> cardNumberNameSet = new HashSet<>();
        for (AdasDriverStatisticsShow droverInfo : driverInfos) {
            driverVidSet.add(droverInfo.getMonitorId());
            cardNumberNameSet.add(droverInfo.getCardNumber());
        }
        Set<IcCardDriverQuery> icCardDriverQuerySet = new HashSet<>();
        String[] cardNumberArray;
        for (String nameCardNumber : cardNumberNameSet) {

            cardNumberArray = nameCardNumber.split("#");
            cardNumberArray = cardNumberArray[0].split("_");
            icCardDriverQuerySet.add(IcCardDriverQuery.getInstance(cardNumberArray[0], cardNumberArray[1]));

        }

        Map<String, AdasProfessionalShow> driverInfoMaps = AdasProfessionalShow
            .convertProfessionalMaps(professionalService.getProfessionalShowMaps(icCardDriverQuerySet));
        //?????????????????????????????????
        userVidSet.retainAll(driverVidSet);
        Map<String, BindDTO> configInfos = MonitorUtils.getBindDTOMap(userVidSet, "orgName", "plateColor", "name");
        //??????map??????????????????????????????????????????
        Map<String, String> userVehIdMap = getUserVidMap(userVidSet);
        for (AdasDriverStatisticsShow droverInfo : driverInfos) {
            if (userVehIdMap.get(droverInfo.getMonitorId()) != null) {
                droverInfo.assembleData(configInfos, driverInfoMaps);
                results.add(droverInfo);
            }
        }
        //????????????????????????
        results.sort(
            (o1, o2) -> Long.valueOf(o2.getInsertCardTimeVal()).compareTo(Long.valueOf(o1.getInsertCardTimeVal())));
        return results;
    }

    private List<AdasDriverStatisticsShow> getAdasDriverStatisticsFromPass(AdasDriverQuery adasDriverQuery) {
        Map<String, String> param = new HashMap<>();
        param.put("cardNumberSetStr", JSONObject.toJSONString(adasDriverQuery.getCardNumberSet()));
        param.put("startTimeVal", adasDriverQuery.getStartTimeVal() + "");
        param.put("endTimeVal", adasDriverQuery.getEndTimeVal() + "");
        String sendResult = HttpClientUtil.send(PaasCloudAdasUrlEnum.GET_IC_CARD_DRIVER_LIST, param);
        return PassCloudResultUtil.getListResult(sendResult, AdasDriverStatisticsShow.class);
    }

    private Map<String, String> getUserVidMap(Set<String> userVidSet) {
        Map<String, String> userVehIdMap = new HashMap<>();
        for (String vid : userVidSet) {
            userVehIdMap.put(vid, vid);
        }
        return userVehIdMap;
    }

    @Override
    public List<AdasDriverStatisticsShow> getDriverInfoByCardNumber(List<AdasDriverStatisticsShow> datas,
        String cardNumber) {
        if (datas != null) {
            return datas.stream().filter(driver -> driver.getCardNumber().contains(cardNumber))
                .collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public void export(AdasDriverQuery adasDriverQuery, HttpServletResponse response, HttpServletRequest request)
        throws IOException {
        List<AdasDriverStatisticsExport> exports = getAdasDriverStatisticsExports(adasDriverQuery);
        ExportExcelUtil.export(
            new ExportExcelParam(null, 1, exports, AdasDriverStatisticsExport.class, null, response.getOutputStream()));
    }

    @Override
    public AdasDriverInfoShow getIcCardDriverInfo(String vehicleId, String cardNumber) {
        ClientVehicleInfo clientVehicleInfo = getLastPositionalInfo(vehicleId);
        AdasDriverInfoShow adasDriverInfoShow = null;
        boolean onLine = clientVehicleInfo != null;
        //????????????????????????????????????5
        if (onLine && Double.parseDouble(clientVehicleInfo.getSpeed()) > 5) {
            String insertCardInfo = RedisHelper.getString(HistoryRedisKeyEnum.CARD_NUM_PREFIX.of(vehicleId));
            //??????????????????
            if (StrUtil.isNotBlank(insertCardInfo)) {
                adasDriverInfoShow = getAdasDriverInfoShow(vehicleId, cardNumber, insertCardInfo.split(",")[1]);
            }
        } else {
            //????????????????????????????????????????????????
            String lastInsertCardTimeInfo = RedisHelper.getString(HistoryRedisKeyEnum.LAST_DRIVER.of(vehicleId));
            if (StrUtil.isNotBlank(lastInsertCardTimeInfo)) {
                String lastInsertCardTime = lastInsertCardTimeInfo.split(",")[1].split("_")[1];
                adasDriverInfoShow = getAdasDriverInfoShow(vehicleId, cardNumber, lastInsertCardTime);
            }
        }
        return adasDriverInfoShow;
    }

    private AdasDriverInfoShow getAdasDriverInfoShow(String vehicleId, String cardNumber, String lastInsertCardTime) {
        AdasDriverInfoShow adasDriverInfoShow;
        LocalDateTime lastInsertCardDateTime = Date8Utils.fromLongTime(lastInsertCardTime);
        AdasDriverQuery adasDriverQuery = AdasDriverQuery.getAdasDriverQuery(cardNumber, lastInsertCardDateTime);
        adasDriverInfoShow = queryAndAssembleAdasDriverInfo(vehicleId, adasDriverQuery);
        return adasDriverInfoShow;
    }

    private AdasDriverInfoShow queryAndAssembleAdasDriverInfo(String vehicleId, AdasDriverQuery adasDriverQuery) {
        AdasDriverInfoShow adasDriverInfoShow = null;
        List<AdasDriverStatisticsShow> driverStaticsData = getSingleDriverInfoFromPass(adasDriverQuery);
        logger.info("??????????????????????????????" + JSONObject.toJSONString(adasDriverQuery));
        logger.info("??????????????????????????????" + JSONObject.toJSON(driverStaticsData));
        if (CollectionUtils.isNotEmpty(driverStaticsData)) {
            driverStaticsData.forEach(driver -> driver.assembleData(null, null));
            driverStaticsData.sort(
                (o1, o2) -> Long.compare(o2.getInsertCardTimeVal(), o1.getInsertCardTimeVal()));
            adasDriverInfoShow = AdasDriverInfoShow.assembleData(driverStaticsData, vehicleId);
        }
        return adasDriverInfoShow;
    }

    private List<AdasDriverStatisticsShow> getSingleDriverInfoFromPass(AdasDriverQuery adasDriverQuery) {
        Map<String, String> param = new HashMap<>();
        param.put("cardNumber", adasDriverQuery.getCardNumbers());
        param.put("startTimeVal", adasDriverQuery.getStartTimeVal() + "");
        param.put("endTimeVal", adasDriverQuery.getEndTimeVal() + "");
        String sendResult = HttpClientUtil.send(PaasCloudAdasUrlEnum.GET_IC_CARD_DRIVER_STATICS_LIST, param);
        return PassCloudResultUtil.getListResult(sendResult, AdasDriverStatisticsShow.class);
    }

    private ClientVehicleInfo getLastPositionalInfo(String vid) {
        String csInfo = RedisHelper.getString(HistoryRedisKeyEnum.MONITOR_STATUS.of(vid));
        return StrUtil.isBlank(csInfo) ? null : JSON.parseObject(csInfo, ClientVehicleInfo.class);
    }

    private List<AdasDriverStatisticsExport> getAdasDriverStatisticsExports(AdasDriverQuery adasDriverQuery) {
        List<AdasDriverStatisticsShow> driverInfos = getDriverInfo(adasDriverQuery);
        List<AdasDriverStatisticsExport> exports = new ArrayList<>();
        AdasDriverStatisticsExport adasDriverStatisticsExport;
        for (AdasDriverStatisticsShow driverInfo : driverInfos) {
            adasDriverStatisticsExport = new AdasDriverStatisticsExport();
            BeanUtils.copyProperties(driverInfo, adasDriverStatisticsExport);
            exports.add(adasDriverStatisticsExport);
        }
        return exports;
    }

    @Override
    public JSONObject bindIcCardTree() {
        JSONObject obj = new JSONObject();
        JSONArray result = new JSONArray();
        //??????????????????????????????
        List<OrganizationLdap> organizationLdapList = userService.getCurrentUseOrgList();
        //?????????????????????icCard???????????????
        Set<String> vehicleIdSet = adasVehicleCardNumDao.findAllBindIcCardVehicleId();
        //??????????????????????????????
        List<GroupDTO> groupDTOList = userService.getCurrentUserGroupList();
        Set<String> allMonitorIds = new HashSet<>();

        Map<String, GroupDTO> groupDTOMap = groupDTOList.stream()
                .collect(Collectors.toMap(GroupDTO::getId, Function.identity()));
        Set<RedisKey> groupKeys = groupDTOList.stream()
                .map(e -> RedisKeyEnum.GROUP_MONITOR.of(e.getId()))
                .collect(Collectors.toSet());
        Map<String, Set<String>> groupMonitorMap = RedisHelper.getMapSet(groupKeys, null);

        for (Map.Entry<String, Set<String>> entry : groupMonitorMap.entrySet()) {
            if (entry.getValue() == null) {
                groupDTOMap.get(entry.getKey()).setAssignmentNumber(0);
                continue;
            }
            Set<String> monitorIds = Sets.intersection(vehicleIdSet, new HashSet<>(entry.getValue()));
            groupDTOMap.get(entry.getKey()).setAssignmentNumber(monitorIds.size());
            allMonitorIds.addAll(monitorIds);
        }
        // ?????????????????????
        result.addAll(JsonUtil.getOrgTree(organizationLdapList, null));
        //???????????????
        // ??????5000?????????????????????
        if (allMonitorIds.size() > PublicVariable.MONITOR_COUNT) {
            putAssignmentTree(result, groupDTOList, true);
        } else {
            putAssignmentTree(result, groupDTOList, false);
            //?????????????????????
            putMonitorTree(result, allMonitorIds, groupDTOMap);
        }
        obj.put("size", allMonitorIds.size());
        obj.put("tree", result);
        return obj;
    }

    private void putMonitorTree(JSONArray result, Set<String> allMonitorIds, Map<String, GroupDTO> groupDTOMap) {
        Map<String, BindDTO> bindDTOMap =
            MonitorUtils.getBindDTOMap(allMonitorIds, "id", "name", "monitorType", "groupId", "groupName");
        for (Map.Entry<String, BindDTO> entry : bindDTOMap.entrySet()) {

            BindDTO bindDTO = entry.getValue();
            if (StrUtil.isBlank(bindDTO.getGroupId()) || StrUtil.isBlank(bindDTO.getGroupName())) {
                continue;
            }
            String[] assignmentIds = bindDTO.getGroupId().split(",");
            for (int i = 0; i < assignmentIds.length; i++) {
                GroupDTO groupDTO = groupDTOMap.get(assignmentIds[i]);
                if (groupDTO == null) {
                    continue;
                }
                JSONObject vehicleObj = new JSONObject();
                vehicleObj.put("id", bindDTO.getId());
                String monitorType = bindDTO.getMonitorType();
                if (!"0".equals(monitorType)) {
                    continue;
                }
                vehicleObj.put("type", "vehicle");
                vehicleObj.put("iconSkin", "vehicleSkin");
                vehicleObj.put("pId", assignmentIds[i]);
                vehicleObj.put("name", bindDTO.getName());
                vehicleObj.put("assignName", groupDTO.getName());
                result.add(vehicleObj);
            }
        }
    }

    @Override
    public JSONArray bindIcCardTreeByAssign(String assignmentId, boolean isChecked) {
        JSONArray result = new JSONArray();
        //?????????????????????icCard???????????????
        Set<String> vehicleIdSet = adasVehicleCardNumDao.findAllBindIcCardVehicleId();

        List<String> monitorIdList = new ArrayList<>(RedisHelper.getSet(RedisKeyEnum.GROUP_MONITOR.of(assignmentId)));
        monitorIdList.retainAll(vehicleIdSet);
        if (monitorIdList.size() == 0) {
            return result;
        }
        Map<String, BindDTO> bindDTOMap =
                MonitorUtils.getBindDTOMap(monitorIdList, "id", "name", "groupId", "groupName", "monitorType");

        for (Map.Entry<String, BindDTO> entry : bindDTOMap.entrySet()) {
            BindDTO bindDTO = entry.getValue();
            String[] assignmentIds = bindDTO.getGroupId().split(",");
            String[] assignmentNames = bindDTO.getGroupName().split(",");
            for (int i = 0; i < assignmentIds.length; i++) {
                if (assignmentId.equals(assignmentIds[i])) {
                    JSONObject vehicleObj = new JSONObject();
                    vehicleObj.put("id", bindDTO.getId());
                    String monitorType = bindDTO.getMonitorType();
                    if (!"0".equals(monitorType)) {
                        continue;
                    }
                    vehicleObj.put("type", "vehicle");
                    vehicleObj.put("iconSkin", "vehicleSkin");
                    vehicleObj.put("pId", assignmentIds[i]);
                    vehicleObj.put("name", bindDTO.getName());
                    vehicleObj.put("assignName", assignmentNames[i]);
                    if (isChecked) {
                        vehicleObj.put("checked", isChecked);
                    }
                    result.add(vehicleObj);
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public Map<String, JSONArray> bindIcCardTreeByGroup(String groupId, boolean isChecked) {
        Map<String, JSONArray> map = new HashMap<>();
        List<GroupDTO> assignmentList = groupService.getGroupsByOrgId(groupId);
        if (assignmentList.size() == 0) {
            return map;
        }
        return processData(assignmentList, isChecked);
    }

    @Override
    public JSONArray bindIcCardTreeSearch(String param) {
        JSONArray result = new JSONArray();
        if (StringUtils.isEmpty(param)) {
            return result;
        }
        //?????????????????????icCard???????????????
        Set<String> vehicleIdSet = adasVehicleCardNumDao.findAllBindIcCardVehicleId();
        //??????????????????????????????
        List<GroupDTO> userGroupList = userService.getCurrentUserGroupList();
        //????????????????????????????????????????????????
        List<GroupDTO> bindIcCardAssign = new ArrayList<>();
        //?????????????????????????????????????????????icCard???????????????ids
        Set<String> allBindIcCardVidSet = findAllBindIcCardVidSet(vehicleIdSet, userGroupList, bindIcCardAssign, param);
        //???????????????
        putGroupTree(result, bindIcCardAssign);
        //???????????????
        putAssignmentTree(result, bindIcCardAssign, false);
        Map<String, GroupDTO> groupDTOMap =
            bindIcCardAssign.stream().collect(Collectors.toMap(GroupDTO::getId, Function.identity()));
        //?????????????????????
        putMonitorTree(result, allBindIcCardVidSet, groupDTOMap);
        return result;
    }

    private void putGroupTree(JSONArray jsonArray, List<GroupDTO> bindIcCardAssign) {

        List<OrganizationLdap> orgs = userService.getCurrentUseOrgList();
        Set<String> proOrgIds = bindIcCardAssign.stream().map(GroupDTO::getOrgId).collect(Collectors.toSet());
        List<OrganizationLdap> filterList =
            orgs.stream().filter(org -> proOrgIds.contains(org.getUuid())).collect(Collectors.toList());
        orgs = TreeUtils.getFilterWholeOrgList(orgs, filterList);
        // ?????????????????????
        jsonArray.addAll(JsonUtil.getOrgTree(orgs, null));
    }

    private Set<String> findAllBindIcCardVidSet(Set<String> vehicleIdSet, List<GroupDTO> groupDTOList,
        List<GroupDTO> assignmentList, String param) {
        Set<String> searchVehicleIds = Sets.intersection(MonitorUtils.fuzzySearchBindMonitorIds(param), vehicleIdSet);
        Set<String> allMonitorIds = new HashSet<>();
        Map<String, GroupDTO> groupDTOMap = groupDTOList.stream()
            .collect(Collectors.toMap(GroupDTO::getId, Function.identity()));
        Set<RedisKey> groupKeys = groupDTOMap.keySet().stream()
            .map(RedisKeyEnum.GROUP_MONITOR::of)
            .collect(Collectors.toSet());
        Map<String, Set<String>> groupMonitorMap = RedisHelper.getMapSet(groupKeys, null);

        for (Map.Entry<String, Set<String>> entry : groupMonitorMap.entrySet()) {

            Set<String> set = Sets.intersection(new HashSet<>(entry.getValue()), searchVehicleIds);
            if (set.size() > 0) {
                assignmentList.add(groupDTOMap.get(entry.getKey()));
                allMonitorIds.addAll(set);
            }
        }

        return allMonitorIds;
    }

    private Map<String, JSONArray> processData(List<GroupDTO> groupDTOList, boolean isChecked) {
        Map<String, JSONArray> map = new HashMap<>();
        Map<String, String> groupNameMap = groupDTOList.stream()
            .collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
        Map<String, List<BindDTO>> bindIcCardSet = getBindIcCardSet(groupDTOList);
        if (bindIcCardSet.size() == 0) {
            return map;
        }
        for (Map.Entry<String, List<BindDTO>> entry : bindIcCardSet.entrySet()) {
            JSONArray jsonArray = new JSONArray();
            for (BindDTO bindDTO : entry.getValue()) {

                JSONObject vehicleObj = new JSONObject();
                vehicleObj.put("id", bindDTO.getId());
                String monitorType = bindDTO.getMonitorType();
                if (!"0".equals(monitorType)) {
                    continue;
                }
                vehicleObj.put("type", "vehicle");
                vehicleObj.put("iconSkin", "vehicleSkin");
                vehicleObj.put("pId", entry.getKey());
                vehicleObj.put("name", bindDTO.getName());
                vehicleObj.put("assignName", groupNameMap.get(entry.getKey()));
                if (isChecked) {
                    vehicleObj.put("checked", isChecked);
                }
                jsonArray.add(vehicleObj);
            }
            if (jsonArray.size() > 0) {
                map.put(entry.getKey(), jsonArray);
            }
        }
        return map;
    }

    private Map<String, List<BindDTO>> getBindIcCardSet(List<GroupDTO> groupDTOList) {
        Map<String, List<BindDTO>> resultMap = new HashMap<>();

        Set<String> bindIcCardSet = adasVehicleCardNumDao.findAllBindIcCardVehicleId();
        Set<RedisKey> groupKeys = groupDTOList.stream().map(RedisKeyEnum.GROUP_MONITOR::of).collect(Collectors.toSet());
        Map<String, Set<String>> groupMonitorMap = RedisHelper.getMapSet(groupKeys, null);
        Set<String> monitorIds = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : groupMonitorMap.entrySet()) {
            entry.getValue().retainAll(bindIcCardSet);
            monitorIds.addAll(entry.getValue());
        }

        Map<String, BindDTO> bindDTOMap = MonitorUtils.getBindDTOMap(monitorIds, "id", "name", "monitorType");

        for (Map.Entry<String, Set<String>> entry : groupMonitorMap.entrySet()) {
            List<BindDTO> list = new ArrayList<>();
            for (String monitorId : entry.getValue()) {
                list.add(bindDTOMap.get(monitorId));
            }
            resultMap.put(entry.getKey(), list);
        }
        return resultMap;
    }

    private void putAssignmentTree(JSONArray result, List<GroupDTO> assignmentInfoList, boolean isBigData) {
        for (GroupDTO info : assignmentInfoList) {
            // ???????????????
            JSONObject assignmentObj = JSONObject.parseObject(JsonUtil.object2Json(info));
            assignmentObj.put("pName", info.getOrgName());
            assignmentObj.put("pId", info.getOrgDn());
            assignmentObj.put("type", "assignment");
            assignmentObj.put("iconSkin", "assignmentSkin");
            assignmentObj.put("count", info.getAssignmentNumber());
            if (isBigData) {
                assignmentObj.put("isParent", true); // ????????????
            }
            result.add(assignmentObj);
        }
    }

    @Override
    public AdasProfessionalShow getAdasProfessionalDetail(String identity, String name) {
        return adasRealTimeMonitoringService.getAdasProfessionalByIdentityAndName(identity, name);
    }

    @Override
    public void exportDetail(AdasDriverQuery adasDriverQuery, HttpServletResponse response) throws Exception {
        AdasDriverStatisticsShow show = getAdasDriverStatisticsById(adasDriverQuery);
        int number = 0;
        for (AdasDriverStatisticsDetailShow detail : show.getDetails()) {
            detail.setNumber(++number);
        }
        Map<String, Object> exportData = new HashMap<>();

        if (show.getProfessionalShow() != null) {
            String photo = show.getProfessionalShow().getPhotograph();
            if (StrUtil.isNotBlank(photo)) {
                exportData.put("img", configHelper.getFileFromFtp(photo));
            }
        }

        exportData.put("data", show);

        templateExportExcel.templateExportExcel("/file/cargoReport/insertCardDriverDetail.xls", response, exportData,
            "?????????" + show.getDriverName() + "(" + show.getCardNumber() + ")" + "????????????");
    }

    /**
     * ??????????????????????????????????????????
     * @param adasDriverQuery
     * @param response
     */
    @Override
    public void exportDetails(AdasDriverQuery adasDriverQuery, HttpServletResponse response) throws Exception {
        List<AdasDriverStatisticsShow> shows = getDriverInfo(adasDriverQuery);
        List<Map<String, Object>> prarm = new ArrayList<>();
        for (AdasDriverStatisticsShow show : shows) {
            Map<String, Object> exportData = new HashMap<>();
            String templateSingleFileName = "?????????" + show.getDriverName() + "(" + show.getCardNumber() + ")" + DateUtil
                .getLongToDateStr(show.getInsertCardTimeVal(), "yyyyMMddHHmmss") + "????????????";
            exportData.put("templateSingleFileName", templateSingleFileName);
            int number = 0;
            for (AdasDriverStatisticsDetailShow detail : show.getDetails()) {
                detail.setNumber(++number);
            }
            if (show.getProfessionalShow() != null) {
                String photo = show.getProfessionalShow().getPhotograph();
                if (StrUtil.isNotBlank(photo)) {
                    exportData.put("img", configHelper.getFileFromFtp(photo));
                }
            }
            exportData.put("data", show);
            prarm.add(exportData);
        }
        templateExportExcel
            .templateExportExcels("/file/cargoReport/insertCardDriverDetail.xls", response, prarm, "?????????????????????");

    }

    private AdasDriverStatisticsShow getAdasDriverStatisticsById(AdasDriverQuery adasDriverQuery) throws Exception {
        AdasDriverStatisticsShow result = new AdasDriverStatisticsShow();
        RedisKey key = getRedisDataKey(adasDriverQuery.getSimpleQueryParam());
        List<AdasDriverStatisticsShow> allData = RedisHelper.getListObj(key, 0, -1);
        for (AdasDriverStatisticsShow data : allData) {
            if (data.getId().equals(adasDriverQuery.getId())) {
                return data;
            }
        }
        return result;
    }

    private RedisKey getRedisDataKey(String simpleQueryParam) {
        String keyVal = SystemHelper.getCurrentUserId();
        if (simpleQueryParam != null && !"".equals(simpleQueryParam.trim())) {

            keyVal = keyVal + "_" + simpleQueryParam;

        }
        return HistoryRedisKeyEnum.DRIVER_STATISTICS_INFO_LIST.of(keyVal);
    }

    @Override
    public String migrate443() {
        final int readBatchSize = 100;
        final int writeBatchSize = 1000;
        final long begin = System.currentTimeMillis();
        String lastId = null;
        List<Map<String, String>> oldData;
        int offset = 0;
        int added = 0;
        do {
            oldData = adasVehicleCardNumDao.listOldData(lastId, readBatchSize);
            if (oldData.isEmpty()) {
                break;
            }
            lastId = oldData.get(oldData.size() - 1).get("vid");
            logger.info("??????????????????-???????????????{}?????????{}???", offset + 1, offset + oldData.size());
            offset += oldData.size();

            final List<VehicleIcHistoryDO> newData = oldData.stream().flatMap(e -> {
                final String vehicleId = e.get("vid");
                final String pair1 = e.get("card_number");
                final String pair2 = e.get("identify_number");
                final Set<VehicleIcHistoryDO> set1 = convertToDO(vehicleId, pair1);
                final Set<VehicleIcHistoryDO> set2 = convertToDO(vehicleId, pair2);
                if (set1.size() > set2.size()) {
                    set1.addAll(set2);
                    return set1.stream();
                } else {
                    set2.addAll(set1);
                    return set2.stream();
                }
            }).collect(Collectors.toList());
            for (List<VehicleIcHistoryDO> list : Lists.partition(newData, writeBatchSize)) {
                added += adasVehicleCardNumDao.batchInsert(list);
                logger.info("??????????????????-????????????{}???", added);
            }
        } while (oldData.size() == readBatchSize);
        final long end = System.currentTimeMillis();
        final String result = String.format("??????????????????????????????%d????????????%d????????????%.2fs",
                offset, added, (end - begin) / 1000f);
        logger.info(result);
        return result;
    }

    private static Set<VehicleIcHistoryDO> convertToDO(@NonNull String vehicleId, String idNamePairs) {
        if (null == idNamePairs) {
            return new HashSet<>();
        }
        return Arrays.stream(idNamePairs.split(","))
                .filter(s -> !s.contains("?"))
                .map(o -> o.split("_"))
                .filter(arr -> 2 == arr.length)
                .map(arr -> new VehicleIcHistoryDO(vehicleId, arr[1], arr[0]))
                .collect(Collectors.toSet());
    }
}
