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
 * 终端service
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
        log.info("开始进行终端管理的redis初始化~");
        List<String> sortList = deviceNewDao.getSortList();
        DeviceRedisCache.clearCache();
        if (CollectionUtils.isEmpty(sortList)) {
            log.info("结束终端管理redis初始化缓存，终端数据为空");
            return;
        }

        List<DeviceDTO> deviceList = deviceNewDao.getByNumbers(null);
        DeviceRedisCache.initCache(sortList, deviceList);
        log.info("结束终端管理redis初始化缓存!");
    }

    @Override
    public boolean add(DeviceDTO deviceDTO) throws BusinessException {
        isRepeatDeviceNumber(deviceDTO.getDeviceNumber(), null);
        DeviceDO deviceDO = DeviceDO.getAddInstance(deviceDTO, SystemHelper.getCurrentUsername());
        //查插入数据库
        deviceNewDao.addDevice(deviceDO);
        //维护缓存
        DeviceRedisCache.addDeviceCache(deviceDTO);
        //添加日志
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
        // 消息
        String message = String.format("新增终端：%s( @%s )", deviceDTO.getDeviceNumber(), groupName);
        // 记录新增终端日志
        logSearchService.addLog(getIpAddress(), message, "3", "", "-", "");
    }

    @Override
    public boolean updateNumber(DeviceDTO deviceDTO) throws BusinessException {
        String deviceId = deviceDTO.getId();
        isRepeatDeviceNumber(deviceDTO.getDeviceNumber(), deviceId);

        //更新数据库
        DeviceDO updateBean = DeviceDO.getUpdateInstance(deviceDTO, SystemHelper.getCurrentUsername());
        // 修改前的设备信息
        DeviceInfoDo beforeDevice = deviceNewDao.findDeviceById(deviceDTO.getId());
        deviceNewDao.updateDevice(updateBean);
        //查找绑定的终端监控对象id
        String bindMonitorId = deviceNewDao.getBindMonitorId(deviceDTO.getId());
        TerminalTypeInfo terminalTypeInfo = deviceNewDao.getTerminalTypeInfo(deviceDTO.getTerminalTypeId());
        if (Objects.nonNull(terminalTypeInfo)) {
            deviceDTO.setIsVideo(terminalTypeInfo.getSupportVideoFlag());
            deviceDTO.setTerminalType(terminalTypeInfo.getTerminalType());
            deviceDTO.setTerminalManufacturer(terminalTypeInfo.getTerminalManufacturer());
        }
        //更新终端以及监控对象相关的缓存信息
        DeviceRedisCache.updateDeviceCache(deviceDTO, bindMonitorId, beforeDevice);
        //进行相关指令的下发
        sendMsg(deviceDTO, bindMonitorId);
        //添加日志
        addUpdateLog(deviceDTO);
        return true;
    }

    private void sendMsg(DeviceDTO deviceDTO, String bindMonitorId) {
        String deviceId = deviceDTO.getId();

        if (bindMonitorId == null) {
            return;
        }
        //推送F3
        configService.sendBindToF3(bindMonitorId);
        if (Objects.equals(deviceDTO.getDeviceType(), ProtocolTypeUtil.HEI_PROTOCOL_808_2019)) {
            webClientHandleCom.send1210ByUpdateDeviceByHljProtocol(deviceId);
        }
        webClientHandleCom.send1240ByUpdateDeviceByZwProtocol(deviceId);
    }

    private void addUpdateLog(DeviceDTO deviceDTO) {
        String orgName = organizationService.getOrgNameByUuid(deviceDTO.getOrgId());
        String updateLog = String.format("修改终端 : %s ( @%s )", deviceDTO.getDeviceNumber(), orgName);
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

        //参数初始化
        deviceQuery.paramInit();
        //查询用户当前组织及其下级组织
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

        //判断是否要进行高级查询
        if (deviceQuery.containsAdvanceQuery()) {
            finalDeviceId.retainAll(deviceNewDao.advancedQueryGetDeviceId(deviceQuery));
        }
        //模糊搜索终端号和监控对象
        if (StringUtils.isNotBlank(deviceQuery.getSimpleQueryParam())) {
            finalDeviceId.retainAll(FuzzySearchUtil.getFuzzySearchDeviceId(deviceQuery.getSimpleQueryParam()));
        }
        List<DeviceListDTO> deviceListDTOList = getDeviceList(deviceQuery, new ArrayList<>(finalDeviceId));
        result = RedisQueryUtil.getListToPage(deviceListDTOList, deviceQuery, finalDeviceId.size());
        return result;
    }

    private List<DeviceListDTO> getDeviceList(DeviceQuery deviceQuery, List<String> finalDeviceId) {
        Map<String, String> userOrgNameMap = userService.getCurrentUserOrgIdOrgNameMap();
        //所有满足条件的终端ID，存入Redis用于导出
        RedisKey exportKey = RedisKeyEnum.USER_DEVICE_EXPORT.of(userService.getCurrentUserInfo().getUsername());
        RedisHelper.delete(exportKey);
        RedisHelper.addToList(exportKey, finalDeviceId);

        //组装返回的数据列表
        List<String> deviceIds = RedisQueryUtil.getPageListIds(finalDeviceId, deviceQuery);
        if (CollectionUtils.isEmpty(deviceIds)) {
            return new ArrayList<>();
        }
        List<DeviceListDO> deviceListDOS =
            DbUtils.partitionSortQuery(deviceIds, deviceNewDao::getDeviceList, DeviceListDO::getId);
        Map<String, BaseKvDo<String, String>> monitorIdNameMap = getMonitorIdNameMap(deviceListDOS);

        //对查询出的结果镜像重新排序
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
            //若为对象设备，默认类型为3（对讲设备）
            deviceDTO.setFunctionalType("3");
            deviceDTO.setDeviceType(ProtocolEnum.T808_2013.getDeviceType());
        } else {
            if (Objects.equals(MonitorTypeEnum.VEHICLE.getType(), bindDTO.getMonitorType())) {
                // 车默认类型为1（简易型车机）
                deviceDTO.setFunctionalType("1");
            } else {
                // 人和物默认类型为6（定位终端）
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
    @MethodLog(name = "批量导入", description = "批量导入")
    @ImportLock(value = ImportModule.DEVICE)
    public JsonResultBean importData(MultipartFile multipartFile) throws Exception {
        // 导入的文件
        ExcelImportHelper excelImportHelper = new DeviceImportHelper(deviceNewDao, new ImportExcel(multipartFile));
        //初始化 excel 转换成 list
        excelImportHelper.init(DeviceImportDTO.class);
        List<DeviceImportDTO> list = excelImportHelper.getExcelData();
        //进行校验
        excelImportHelper.validate(userService.getCurrentUserOrgNameOrgIdMap());
        // 导入逻辑
        final DeviceImportHandler handler = new DeviceImportHandler(deviceNewDao, excelImportHelper);
        try (ImportCache ignored = new ImportCache(ImportModule.DEVICE, SystemHelper.getCurrentUsername(), handler)) {
            final JsonResultBean jsonResultBean = handler.execute();
            if (!jsonResultBean.isSuccess()) {
                ImportErrorUtil.putDataToRedis(list, ImportModule.DEVICE);
                return jsonResultBean;
            }
        }
        addImportLog(list);
        return new JsonResultBean(true, String.format("导入结果： 成功导入%d条数据<br/>", list.size()));

    }

    private void addImportLog(List<DeviceImportDTO> list) {
        StringBuilder message = new StringBuilder();
        String logTemplate = "导入终端 : %s ( @%s ) <br/>";
        for (DeviceImportDTO device : list) {
            message.append(String.format(logTemplate, device.getDeviceNumber(), device.getOrgName()));
        }

        // 记录日志
        if (!message.toString().isEmpty()) {
            logSearchService.addLog(getIpAddress(), message.toString(), "3", "batch", "导入终端信息");
        }
    }

    @Override
    public boolean checkIsBind(String deviceNumber) {
        return deviceNewDao.getMonitorIdByDeviceNumber(deviceNumber) != null;
    }

    @MethodLog(name = "导出", description = "导出")
    @Override
    public void exportDevice() throws Exception {
        Map<String, String> userOrgMap = userService.getCurrentUserOrgIdOrgNameMap();
        RedisKey exportKey = RedisKeyEnum.USER_DEVICE_EXPORT.of(userService.getCurrentUserInfo().getUsername());
        List<String> deviceIds = RedisHelper.getList(exportKey);
        List<DeviceListDO> deviceListDOS =
                DbUtils.partitionSortQuery(deviceIds, deviceNewDao::getDeviceList, DeviceListDO::getId);
        Map<String, BaseKvDo<String, String>> monitorIdNameMap = getMonitorIdNameMap(deviceListDOS);
        List<DeviceExportDTO> exportDTOS = new ArrayList<>();
        //对从进行最终导出数据的组装
        for (DeviceListDO deviceListDO : deviceListDOS) {
            exportDTOS.add(DeviceExportDTO.build(deviceListDO, userOrgMap, monitorIdNameMap));
        }
        HttpServletResponse response = getResponse();
        try (OutputStream out = response.getOutputStream()) {
            ExportExcel export = new ExportExcel(null, DeviceExportDTO.class, 1);
            export.setDataList(exportDTOS);
            // 将文档对象写入文件输出流
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
        //获取用户权限下符合条件的终端ID集合
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
        // 获得当前用户所属企业及其下级企业名称
        List<String> groupNames = userService.getCurrentUserOrgNames();
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // 表头
        headList.add("终端号");
        headList.add("所属企业");
        headList.add("通讯类型");
        headList.add("终端厂商");
        headList.add("终端型号");
        headList.add("功能类型");
        headList.add("终端名称");
        headList.add("制造商ID");
        headList.add("终端型号（注册）");
        headList.add("MAC地址");
        headList.add("制造商");
        headList.add("条码");
        headList.add("启停状态");
        // headList.add("监控对象");
        headList.add("安装时间");
        headList.add("采购时间");
        // headList.add("创建日期");
        // headList.add("修改日期");
        headList.add("安装单位");
        headList.add("联系人");
        headList.add("联系电话");
        headList.add("是否符合要求");
        headList.add("备注");
        // 必填字段
        requiredList.add("终端号");
        requiredList.add("所属企业");
        requiredList.add("通讯类型");
        requiredList.add("终端厂商");
        requiredList.add("终端型号");
        requiredList.add("功能类型");
        // 默认设置一条数据
        exportList.add("TH00300");
        //所属企业
        if (CollectionUtils.isNotEmpty(groupNames)) {
            exportList.add(groupNames.get(0));
        } else {
            exportList.add("");
        }
        exportList.add("交通部JT/T808-2013");
        exportList.add("[f]F3");
        exportList.add("F3-default");
        exportList.add("简易型车机");
        exportList.add("测试11");
        exportList.add("中位");
        exportList.add("型号741");
        exportList.add("");
        exportList.add("中位科技");
        exportList.add("1123572044597");
        exportList.add("启用");
        // exportList.add("");
        exportList.add("2016-12-01");
        exportList.add("2016-12-03");
        // exportList.add("2016-12-01");
        // exportList.add("2016-12-03");
        exportList.add("中位科技");
        exportList.add("");
        exportList.add("");
        exportList.add("是");
        exportList.add("终端信息");
        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<>();
        //所属企业
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(groupNames)) {
            String[] groupNameArr = new String[groupNames.size()];
            groupNames.toArray(groupNameArr);
            selectMap.put("所属企业", groupNameArr);
        }
        // 启停状态
        String[] startStop = { "启用", "停用" };
        selectMap.put("启停状态", startStop);
        // 是否符合需求
        String[] isRequirements = { "是", "否" };
        selectMap.put("是否符合要求", isRequirements);
        // 通讯类型
        Set<String> deviceTypeNameList = ProtocolEnum.DEVICE_TYPE_NAMES;
        String[] deviceType = new String[deviceTypeNameList.size()];
        deviceTypeNameList.toArray(deviceType);
        selectMap.put("通讯类型", deviceType);
        // 功能类型
        String[] functionalType = { "简易型车机", "行车记录仪", "对讲设备", "手咪设备", "超长待机设备", "定位终端" };
        selectMap.put("功能类型", functionalType);
        //终端厂商
        List<String> terminalManufacturer = terminalTypeDao.getTerminalManufacturer();
        String[] manuFacturer = new String[terminalManufacturer.size()];
        terminalManufacturer.toArray(manuFacturer);
        selectMap.put("终端厂商", manuFacturer);
        //终端型号
        List<TerminalTypeInfo> allTerminalType = terminalTypeDao.getAllTerminalType();
        String[] terminalType = new String[allTerminalType.size()];
        for (int i = 0; i < allTerminalType.size(); i++) {
            terminalType[i] = allTerminalType.get(i).getTerminalType();
        }
        selectMap.put("终端型号", terminalType);

        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // 输出导文件
        ResponseUtil.writeFile(response, export);

        return true;
    }

    @Override
    public void updateDeviceManufacturer(DeviceDTO deviceDTO) {
        deviceNewDao.updateDeviceManufacturer(deviceDTO);
    }

    private void addDeleteMoreLog(Map<String, String> orgMap, List<DeviceListDO> deviceList) {
        StringBuilder message = new StringBuilder();
        String logTemplate = "删除终端 : %s ( @%s ) <br/>";
        for (DeviceListDO info : deviceList) {
            message.append(String.format(logTemplate, info.getDeviceNumber(), orgMap.get(info.getOrgId())));
        }
        logSearchService.addLog(getIpAddress(), message.toString(), "3", "batch", "批量删除终端");
    }

    private void addDeleteLog(DeviceInfoDo device) {
        String orgName = organizationService.getOrgNameByUuid(device.getOrgId());
        String updateLog = String.format("删除终端 : %s ( @%s )", device.getDeviceNumber(), orgName);
        logSearchService.addLog(getIpAddress(), updateLog, "3", "", "-", "");
    }
}
