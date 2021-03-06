package com.zw.platform.basic.service.impl;

import com.github.pagehelper.Page;
import com.google.common.collect.ImmutableMap;
import com.zw.platform.basic.constant.ErrorMsg;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.BaseKvDo;
import com.zw.platform.basic.domain.DeviceDO;
import com.zw.platform.basic.domain.DeviceInfoDo;
import com.zw.platform.basic.domain.DeviceListDO;
import com.zw.platform.basic.dto.ConfigDTO;
import com.zw.platform.basic.dto.DeviceDTO;
import com.zw.platform.basic.dto.DeviceListDTO;
import com.zw.platform.basic.dto.export.DeviceExportDTO;
import com.zw.platform.basic.dto.imports.DeviceImportDTO;
import com.zw.platform.basic.dto.query.DeviceQuery;
import com.zw.platform.basic.helper.DeviceImportHelper;
import com.zw.platform.basic.imports.handler.DeviceImportHandler;
import com.zw.platform.basic.rediscache.DeviceRedisCache;
import com.zw.platform.basic.repository.DeviceNewDao;
import com.zw.platform.basic.service.CacheService;
import com.zw.platform.basic.service.DeviceService;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.basic.service.MonitorService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.util.DbUtils;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.TerminalTypeInfo;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.push.common.WebClientHandleCom;
import com.zw.platform.repository.modules.TerminalTypeDao;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.FuzzySearchUtil;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportErrorUtil;
import com.zw.platform.util.excel.ImportExcel;
import com.zw.platform.util.excel.annotation.ExcelImportHelper;
import com.zw.platform.util.imports.ImportCache;
import com.zw.platform.util.imports.lock.ImportLock;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.platform.util.response.ResponseUtil;
import com.zw.protocol.util.ProtocolTypeUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ??????service
 */
@Service("deviceNewServiceImpl")
@Order(5)
public class DeviceServiceImpl implements CacheService, DeviceService, IpAddressService {
    private static final Logger log = LogManager.getLogger(ConfigServiceImpl.class);

    @Autowired
    private DeviceNewDao deviceNewDao;

    @Autowired
    private TerminalTypeDao terminalTypeDao;

    @Autowired
    private UserService userService;
    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private LogSearchService logSearchService;

    @Value("${device.number.bound}")
    private String deviceNumberBound;

    @Autowired
    private WebClientHandleCom webClientHandleCom;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private ConfigService configService;

    @Override
    public void initCache() {
        log.info("???????????????????????????redis?????????~");
        List<String> sortList = deviceNewDao.getSortList();
        DeviceRedisCache.clearCache();
        if (CollectionUtils.isEmpty(sortList)) {
            log.info("??????????????????redis????????????????????????????????????");
            return;
        }

        List<DeviceDTO> deviceList = deviceNewDao.getByNumbers(null);
        DeviceRedisCache.initCache(sortList, deviceList);
        log.info("??????????????????redis???????????????!");
    }

    @Override
    public boolean add(DeviceDTO deviceDTO) throws BusinessException {
        isRepeatDeviceNumber(deviceDTO.getDeviceNumber(), null);
        DeviceDO deviceDO = DeviceDO.getAddInstance(deviceDTO, SystemHelper.getCurrentUsername());
        //??????????????????
        deviceNewDao.addDevice(deviceDO);
        //????????????
        DeviceRedisCache.addDeviceCache(deviceDTO);
        //????????????
        addDeviceLog(deviceDTO);
        return true;

    }

    private void isRepeatDeviceNumber(String deviceNumber, String id) throws BusinessException {
        if (deviceNewDao.getNoRepeatDeviceNumber(deviceNumber, id) != null) {
            throw new BusinessException(ErrorMsg.DEVICE_EXIST.getMsg());
        }
    }

    private void addDeviceLog(DeviceDTO deviceDTO) {
        String groupName = organizationService.getOrgNameByUuid(deviceDTO.getOrgId());
        // ??????
        String message = String.format("???????????????%s( @%s )", deviceDTO.getDeviceNumber(), groupName);
        // ????????????????????????
        logSearchService.addLog(getIpAddress(), message, "3", "", "-", "");
    }

    @Override
    public boolean updateNumber(DeviceDTO deviceDTO) throws BusinessException {
        String deviceId = deviceDTO.getId();
        isRepeatDeviceNumber(deviceDTO.getDeviceNumber(), deviceId);

        //???????????????
        DeviceDO updateBean = DeviceDO.getUpdateInstance(deviceDTO, SystemHelper.getCurrentUsername());
        // ????????????????????????
        DeviceInfoDo beforeDevice = deviceNewDao.findDeviceById(deviceDTO.getId());
        deviceNewDao.updateDevice(updateBean);
        //?????????????????????????????????id
        String bindMonitorId = deviceNewDao.getBindMonitorId(deviceDTO.getId());
        TerminalTypeInfo terminalTypeInfo = deviceNewDao.getTerminalTypeInfo(deviceDTO.getTerminalTypeId());
        if (Objects.nonNull(terminalTypeInfo)) {
            deviceDTO.setIsVideo(terminalTypeInfo.getSupportVideoFlag());
            deviceDTO.setTerminalType(terminalTypeInfo.getTerminalType());
            deviceDTO.setTerminalManufacturer(terminalTypeInfo.getTerminalManufacturer());
        }
        //???????????????????????????????????????????????????
        DeviceRedisCache.updateDeviceCache(deviceDTO, bindMonitorId, beforeDevice);
        //???????????????????????????
        sendMsg(deviceDTO, bindMonitorId);
        //????????????
        addUpdateLog(deviceDTO);
        return true;
    }

    private void sendMsg(DeviceDTO deviceDTO, String bindMonitorId) {
        String deviceId = deviceDTO.getId();

        if (bindMonitorId == null) {
            return;
        }
        //??????F3
        configService.sendBindToF3(bindMonitorId);
        if (Objects.equals(deviceDTO.getDeviceType(), ProtocolTypeUtil.HEI_PROTOCOL_808_2019)) {
            webClientHandleCom.send1210ByUpdateDeviceByHljProtocol(deviceId);
        }
        webClientHandleCom.send1240ByUpdateDeviceByZwProtocol(deviceId);
    }

    private void addUpdateLog(DeviceDTO deviceDTO) {
        String orgName = organizationService.getOrgNameByUuid(deviceDTO.getOrgId());
        String updateLog = String.format("???????????? : %s ( @%s )", deviceDTO.getDeviceNumber(), orgName);
        logSearchService.addLog(getIpAddress(), updateLog, "3", "", "-", "");
    }

    @Override
    public boolean delete(String id) throws BusinessException {

        if (deviceNewDao.getBindMonitorId(id) != null) {
            throw new BusinessException(deviceNumberBound);
        }
        DeviceInfoDo device = deviceNewDao.findDeviceById(id);
        deviceNewDao.deleteDeviceById(id);
        DeviceRedisCache.deleteCache(id, device);
        addDeleteLog(device);
        return true;
    }

    @Override
    public Page<DeviceListDTO> getListByKeyWord(DeviceQuery deviceQuery) {
        Page<DeviceListDTO> result;
        Set<String> finalDeviceId = new LinkedHashSet<>(RedisHelper.getList(RedisKeyEnum.DEVICE_SORT_LIST.of()));

        //???????????????
        deviceQuery.paramInit();
        //??????????????????????????????????????????
        List<String> userOrgListId = userService.getCurrentUserOrgIds();
        if (StrUtil.isNotBlank(deviceQuery.getOrgId())) {
            userOrgListId.retainAll(Collections.singleton(deviceQuery.getOrgId()));

        }
        List<RedisKey> orgDeviceKeys = new ArrayList<>(userOrgListId.size() * 500);
        for (String orgId : userOrgListId) {
            orgDeviceKeys.add(RedisKeyEnum.ORG_DEVICE.of(orgId));
            orgDeviceKeys.add(RedisKeyEnum.ORG_UNBIND_DEVICE.of(orgId));
        }
        finalDeviceId.retainAll(RedisHelper.batchGetSet(orgDeviceKeys));

        //?????????????????????????????????
        if (deviceQuery.containsAdvanceQuery()) {
            finalDeviceId.retainAll(deviceNewDao.advancedQueryGetDeviceId(deviceQuery));
        }
        //????????????????????????????????????
        if (StringUtils.isNotBlank(deviceQuery.getSimpleQueryParam())) {
            finalDeviceId.retainAll(FuzzySearchUtil.getFuzzySearchDeviceId(deviceQuery.getSimpleQueryParam()));
        }
        List<DeviceListDTO> deviceListDTOList = getDeviceList(deviceQuery, new ArrayList<>(finalDeviceId));
        result = RedisQueryUtil.getListToPage(deviceListDTOList, deviceQuery, finalDeviceId.size());
        return result;
    }

    private List<DeviceListDTO> getDeviceList(DeviceQuery deviceQuery, List<String> finalDeviceId) {
        Map<String, String> userOrgNameMap = userService.getCurrentUserOrgIdOrgNameMap();
        //???????????????????????????ID?????????Redis????????????
        RedisKey exportKey = RedisKeyEnum.USER_DEVICE_EXPORT.of(userService.getCurrentUserInfo().getUsername());
        RedisHelper.delete(exportKey);
        RedisHelper.addToList(exportKey, finalDeviceId);

        //???????????????????????????
        List<String> deviceIds = RedisQueryUtil.getPageListIds(finalDeviceId, deviceQuery);
        if (CollectionUtils.isEmpty(deviceIds)) {
            return new ArrayList<>();
        }
        List<DeviceListDO> deviceListDOS =
            DbUtils.partitionSortQuery(deviceIds, deviceNewDao::getDeviceList, DeviceListDO::getId);
        Map<String, BaseKvDo<String, String>> monitorIdNameMap = getMonitorIdNameMap(deviceListDOS);

        //???????????????????????????????????????
        List<DeviceListDTO> result = new ArrayList<>();
        for (DeviceListDO deviceListDO : deviceListDOS) {
            result.add(DeviceListDTO.buildList(deviceListDO, monitorIdNameMap, userOrgNameMap));
        }
        return result;
    }

    private Map<String, BaseKvDo<String, String>> getMonitorIdNameMap(List<DeviceListDO> deviceListDOS) {
        Set<String> monitorIds =
            deviceListDOS.stream().filter(e -> e.getMonitorId() != null).map(DeviceListDO::getMonitorId)
                .collect(Collectors.toSet());
        Map<String, BaseKvDo<String, String>> monitorIdNameMap = new HashMap<>();
        if (CollectionUtils.isEmpty(monitorIds)) {
            return monitorIdNameMap;
        }
        monitorIdNameMap = monitorService.getMonitorIdNameMap(monitorIds, null);
        return monitorIdNameMap;
    }

    @Override
    public DeviceDTO getDefaultInfo(ConfigDTO bindDTO) {
        DeviceDTO deviceDTO = new DeviceDTO();
        deviceDTO.setIsVideo(1);
        deviceDTO.setFlag(1);
        deviceDTO.setComplianceRequirements(1);
        if (StringUtils.isNotBlank(bindDTO.getIntercomDeviceNumber())) {
            //????????????????????????????????????3??????????????????
            deviceDTO.setFunctionalType("3");
            deviceDTO.setDeviceType(ProtocolEnum.T808_2013.getDeviceType());
        } else {
            if (Objects.equals(MonitorTypeEnum.VEHICLE.getType(), bindDTO.getMonitorType())) {
                // ??????????????????1?????????????????????
                deviceDTO.setFunctionalType("1");
            } else {
                // ????????????????????????6??????????????????
                deviceDTO.setFunctionalType("6");
            }
        }
        if (StringUtils.isNotBlank(bindDTO.getFunctionalType())) {
            deviceDTO.setFunctionalType(bindDTO.getFunctionalType());
        }
        deviceDTO.setDeviceType(bindDTO.getDeviceType());
        deviceDTO.setCreateDataUsername(SystemHelper.getCurrentUsername());
        if (StringUtils.isBlank(bindDTO.getDeviceOrgId())) {
            OrganizationLdap currentUserOrg = userService.getCurrentUserOrg();
            deviceDTO.setOrgId(currentUserOrg.getUuid());
        } else {
            deviceDTO.setOrgId(bindDTO.getDeviceOrgId());
        }
        if (StringUtils.isBlank(bindDTO.getTerminalTypeId())) {
            deviceDTO.setTerminalTypeId("default");
            deviceDTO.setTerminalManufacturer("[f]F3");
            deviceDTO.setTerminalType("F3-default");
        } else {
            TerminalTypeInfo terminalType = deviceNewDao.getTerminalTypeInfo(bindDTO.getTerminalTypeId());
            if (Objects.nonNull(terminalType)) {
                deviceDTO.setTerminalManufacturer(terminalType.getTerminalManufacturer());
                deviceDTO.setTerminalType(terminalType.getTerminalType());
            }
            deviceDTO.setTerminalTypeId(bindDTO.getTerminalTypeId());
        }
        deviceDTO.setDeviceNumber(bindDTO.getDeviceNumber());
        deviceDTO.setId(bindDTO.getDeviceId());
        if (Objects.isNull(bindDTO.getDeviceIsStart())) {
            deviceDTO.setIsStart(1);
        } else {
            deviceDTO.setIsStart(bindDTO.getDeviceIsStart());
        }
        deviceDTO.setInstallTimeStr(bindDTO.getInstallTimeStr());
        return deviceDTO;
    }

    @Override
    public DeviceDTO getByNumber(String number) {
        List<DeviceDTO> deviceList = deviceNewDao.getByNumbers(Collections.singletonList(number));
        DeviceDTO deviceDTO = deviceList.isEmpty() ? null : deviceList.get(0);
        if (Objects.isNull(deviceDTO)) {
            return null;
        }
        deviceDTO.setOrgName(organizationService.getOrgNameByUuid(deviceDTO.getOrgId()));
        return deviceDTO;
    }

    @Override
    public boolean updateNumber(String id, String deviceNumber) {
        return deviceNewDao.updateNumber(id, deviceNumber);
    }

    @Override
    public boolean checkIsExist(String deviceNumber, String id) {
        return deviceNewDao.getNoRepeatDeviceNumber(deviceNumber, id) != null;
    }

    @Override
    public DeviceDTO findById(String id) {
        DeviceInfoDo device = deviceNewDao.findDeviceById(id);
        String orgName = organizationService.getOrgNameByUuid(device.getOrgId());
        return DeviceDTO.getInfo(device, orgName);
    }

    @Override
    @MethodLog(name = "????????????", description = "????????????")
    @ImportLock(value = ImportModule.DEVICE)
    public JsonResultBean importData(MultipartFile multipartFile) throws Exception {
        // ???????????????
        ExcelImportHelper excelImportHelper = new DeviceImportHelper(deviceNewDao, new ImportExcel(multipartFile));
        //????????? excel ????????? list
        excelImportHelper.init(DeviceImportDTO.class);
        List<DeviceImportDTO> list = excelImportHelper.getExcelData();
        //????????????
        excelImportHelper.validate(userService.getCurrentUserOrgNameOrgIdMap());
        // ????????????
        final DeviceImportHandler handler = new DeviceImportHandler(deviceNewDao, excelImportHelper);
        try (ImportCache ignored = new ImportCache(ImportModule.DEVICE, SystemHelper.getCurrentUsername(), handler)) {
            final JsonResultBean jsonResultBean = handler.execute();
            if (!jsonResultBean.isSuccess()) {
                ImportErrorUtil.putDataToRedis(list, ImportModule.DEVICE);
                return jsonResultBean;
            }
        }
        addImportLog(list);
        return new JsonResultBean(true, String.format("??????????????? ????????????%d?????????<br/>", list.size()));

    }

    private void addImportLog(List<DeviceImportDTO> list) {
        StringBuilder message = new StringBuilder();
        String logTemplate = "???????????? : %s ( @%s ) <br/>";
        for (DeviceImportDTO device : list) {
            message.append(String.format(logTemplate, device.getDeviceNumber(), device.getOrgName()));
        }

        // ????????????
        if (!message.toString().isEmpty()) {
            logSearchService.addLog(getIpAddress(), message.toString(), "3", "batch", "??????????????????");
        }
    }

    @Override
    public boolean checkIsBind(String deviceNumber) {
        return deviceNewDao.getMonitorIdByDeviceNumber(deviceNumber) != null;
    }

    @MethodLog(name = "??????", description = "??????")
    @Override
    public void exportDevice() throws Exception {
        Map<String, String> userOrgMap = userService.getCurrentUserOrgIdOrgNameMap();
        RedisKey exportKey = RedisKeyEnum.USER_DEVICE_EXPORT.of(userService.getCurrentUserInfo().getUsername());
        List<String> deviceIds = RedisHelper.getList(exportKey);
        List<DeviceListDO> deviceListDOS =
                DbUtils.partitionSortQuery(deviceIds, deviceNewDao::getDeviceList, DeviceListDO::getId);
        Map<String, BaseKvDo<String, String>> monitorIdNameMap = getMonitorIdNameMap(deviceListDOS);
        List<DeviceExportDTO> exportDTOS = new ArrayList<>();
        //???????????????????????????????????????
        for (DeviceListDO deviceListDO : deviceListDOS) {
            exportDTOS.add(DeviceExportDTO.build(deviceListDO, userOrgMap, monitorIdNameMap));
        }
        HttpServletResponse response = getResponse();
        try (OutputStream out = response.getOutputStream()) {
            ExportExcel export = new ExportExcel(null, DeviceExportDTO.class, 1);
            export.setDataList(exportDTOS);
            // ????????????????????????????????????
            export.write(out);
        }
    }

    @Override
    public boolean deleteBatch(List<String> deviceIds) throws BusinessException {
        List<String> bindMonitorIds = deviceNewDao.getBindMonitorIds(deviceIds);
        if (CollectionUtils.isNotEmpty(bindMonitorIds)) {
            throw new BusinessException(deviceNumberBound);
        }
        Map<String, String> orgMap = userService.getCurrentUserOrgIdOrgNameMap();
        List<DeviceListDO> deviceList = deviceNewDao.getDeviceList(deviceIds);
        deviceNewDao.deleteByBatch(deviceIds);
        DeviceRedisCache.deleteDevicesCache(deviceList);
        addDeleteMoreLog(orgMap, deviceList);
        return true;
    }

    @Override
    public Set<String> getOrgDeviceIds(String orgId) {
        return deviceNewDao.getOrgDeviceIds(orgId);
    }

    @Override
    public List<DeviceDTO> getByDeviceNumbers(Collection<String> deviceNumbers) {
        return deviceNewDao.getByNumbers(deviceNumbers);
    }

    @Override
    public boolean addByBatch(Collection<DeviceDO> deviceList) {
        return deviceNewDao.addDeviceByBatch(deviceList);
    }

    @Override
    public List<Map<String, String>> getUbBindSelectList(String keyword, Integer deviceType) {
        //??????????????????????????????????????????ID??????
        List<String> sortIds = DeviceRedisCache.getUnbind(userService.getCurrentUserOrgIds(), keyword);
        if (CollectionUtils.isEmpty(sortIds)) {
            return new ArrayList<>();
        }
        int endIndex = Math.min(sortIds.size(), Vehicle.UNBIND_SELECT_SHOW_NUMBER);
        List<String> deviceIds = sortIds.subList(0, endIndex);
        List<DeviceListDO> deviceList = deviceNewDao.getDeviceList(deviceIds);

        Map<String, DeviceListDO> unbindDeviceMap = AssembleUtil.collectionToMap(deviceList, DeviceListDO::getId);
        List<Map<String, String>> unbindList = new ArrayList<>();
        for (String id : deviceIds) {
            DeviceListDO device = unbindDeviceMap.get(id);
            if (Objects.isNull(device)) {
                continue;
            }
            if (Objects.nonNull(deviceType) && !Objects.equals(deviceType, Integer.valueOf(device.getDeviceType()))) {
                continue;
            }
            Map<String, String> deviceMap = ImmutableMap
                .of("deviceNumber", device.getDeviceNumber(), "id", id, "deviceType", device.getDeviceType());
            unbindList.add(deviceMap);
        }
        return unbindList;
    }

    @Override
    public List<DeviceDTO> getDeviceListByIds(Collection<String> deviceIds) {
        if (CollectionUtils.isEmpty(deviceIds)) {
            return new ArrayList<>();
        }
        List<OrganizationLdap> orgList = organizationService.getAllOrganization();
        Map<String, String> orgMap =
            AssembleUtil.collectionToMap(orgList, OrganizationLdap::getUuid, OrganizationLdap::getName);
        List<DeviceDTO> deviceList = deviceNewDao.getDeviceListByIds(deviceIds);
        deviceList.forEach(deviceDTO -> deviceDTO.setOrgName(orgMap.get(deviceDTO.getOrgId())));
        return deviceList;
    }

    @Override
    public boolean generateTemplate(HttpServletResponse response) {
        // ??????????????????????????????????????????????????????
        List<String> groupNames = userService.getCurrentUserOrgNames();
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // ??????
        headList.add("?????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("?????????ID");
        headList.add("????????????????????????");
        headList.add("MAC??????");
        headList.add("?????????");
        headList.add("??????");
        headList.add("????????????");
        // headList.add("????????????");
        headList.add("????????????");
        headList.add("????????????");
        // headList.add("????????????");
        // headList.add("????????????");
        headList.add("????????????");
        headList.add("?????????");
        headList.add("????????????");
        headList.add("??????????????????");
        headList.add("??????");
        // ????????????
        requiredList.add("?????????");
        requiredList.add("????????????");
        requiredList.add("????????????");
        requiredList.add("????????????");
        requiredList.add("????????????");
        requiredList.add("????????????");
        // ????????????????????????
        exportList.add("TH00300");
        //????????????
        if (CollectionUtils.isNotEmpty(groupNames)) {
            exportList.add(groupNames.get(0));
        } else {
            exportList.add("");
        }
        exportList.add("?????????JT/T808-2013");
        exportList.add("[f]F3");
        exportList.add("F3-default");
        exportList.add("???????????????");
        exportList.add("??????11");
        exportList.add("??????");
        exportList.add("??????741");
        exportList.add("");
        exportList.add("????????????");
        exportList.add("1123572044597");
        exportList.add("??????");
        // exportList.add("");
        exportList.add("2016-12-01");
        exportList.add("2016-12-03");
        // exportList.add("2016-12-01");
        // exportList.add("2016-12-03");
        exportList.add("????????????");
        exportList.add("");
        exportList.add("");
        exportList.add("???");
        exportList.add("????????????");
        // ?????????????????????map
        Map<String, String[]> selectMap = new HashMap<>();
        //????????????
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(groupNames)) {
            String[] groupNameArr = new String[groupNames.size()];
            groupNames.toArray(groupNameArr);
            selectMap.put("????????????", groupNameArr);
        }
        // ????????????
        String[] startStop = { "??????", "??????" };
        selectMap.put("????????????", startStop);
        // ??????????????????
        String[] isRequirements = { "???", "???" };
        selectMap.put("??????????????????", isRequirements);
        // ????????????
        Set<String> deviceTypeNameList = ProtocolEnum.DEVICE_TYPE_NAMES;
        String[] deviceType = new String[deviceTypeNameList.size()];
        deviceTypeNameList.toArray(deviceType);
        selectMap.put("????????????", deviceType);
        // ????????????
        String[] functionalType = { "???????????????", "???????????????", "????????????", "????????????", "??????????????????", "????????????" };
        selectMap.put("????????????", functionalType);
        //????????????
        List<String> terminalManufacturer = terminalTypeDao.getTerminalManufacturer();
        String[] manuFacturer = new String[terminalManufacturer.size()];
        terminalManufacturer.toArray(manuFacturer);
        selectMap.put("????????????", manuFacturer);
        //????????????
        List<TerminalTypeInfo> allTerminalType = terminalTypeDao.getAllTerminalType();
        String[] terminalType = new String[allTerminalType.size()];
        for (int i = 0; i < allTerminalType.size(); i++) {
            terminalType[i] = allTerminalType.get(i).getTerminalType();
        }
        selectMap.put("????????????", terminalType);

        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // ???????????????
        ResponseUtil.writeFile(response, export);

        return true;
    }

    @Override
    public void updateDeviceManufacturer(DeviceDTO deviceDTO) {
        deviceNewDao.updateDeviceManufacturer(deviceDTO);
    }

    private void addDeleteMoreLog(Map<String, String> orgMap, List<DeviceListDO> deviceList) {
        StringBuilder message = new StringBuilder();
        String logTemplate = "???????????? : %s ( @%s ) <br/>";
        for (DeviceListDO info : deviceList) {
            message.append(String.format(logTemplate, info.getDeviceNumber(), orgMap.get(info.getOrgId())));
        }
        logSearchService.addLog(getIpAddress(), message.toString(), "3", "batch", "??????????????????");
    }

    private void addDeleteLog(DeviceInfoDo device) {
        String orgName = organizationService.getOrgNameByUuid(device.getOrgId());
        String updateLog = String.format("???????????? : %s ( @%s )", device.getDeviceNumber(), orgName);
        logSearchService.addLog(getIpAddress(), updateLog, "3", "", "-", "");
    }
}
