package com.zw.platform.basic.imports.validator;

import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.DeviceDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.GroupMonitorDTO;
import com.zw.platform.basic.dto.MonitorBaseDTO;
import com.zw.platform.basic.dto.SimCardDTO;
import com.zw.platform.basic.dto.imports.IntercomImportDTO;
import com.zw.platform.basic.imports.ConfigImportHolder;
import com.zw.platform.basic.repository.DeviceNewDao;
import com.zw.platform.basic.repository.IntercomDao;
import com.zw.platform.basic.repository.SimCardNewDao;
import com.zw.platform.basic.service.GroupMonitorService;
import com.zw.platform.basic.service.impl.MonitorFactory;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RegexUtils;
import com.zw.platform.util.excel.validator.ImportValidator;
import com.zw.talkback.domain.intercom.info.OriginalModelInfo;
import com.zw.talkback.repository.mysql.OriginalModelDao;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class IntercomImportValidator extends ImportValidator<IntercomImportDTO> {
    private static final Pattern vehicleDeviceChecker = Pattern.compile("^[0-9A-Z]{7}$");
    private static final Pattern assignmentSplitter = Pattern.compile(",");
    private static final Pattern assignmentNameSplitter = Pattern.compile("@");
    private static final Pattern passwordSplitter = Pattern.compile("^[0-9a-zA-Z]{8}$");
    private static final Pattern nameChecker = Pattern.compile("^[0-9a-zA-Z\\u4e00-\\u9fa5_-]{2,20}$");
    private final MonitorFactory monitorFactory;
    private final ConfigImportHolder importHolder;
    private final DeviceNewDao deviceNewDao;
    private final SimCardNewDao simCardNewDao;
    private final IntercomDao intercomDao;
    private final OriginalModelDao originalModelDao;
    private final GroupMonitorService groupMonitorService;
    /**
     * 已存在监控对象map 监控对象类型-监控对象名称-监控对象基本信息
     */
    private Map<String, Map<String, MonitorBaseDTO>> monitorMap;
    private Map<String, SimCardDTO> existSimMap;
    private Map<String, DeviceDTO> existDeviceMap;
    private Set<String> existIntercomDeviceNumSet;
    private Set<String> originalModels;
    /**
     * 分组@组织-分组
     */
    private Map<String, GroupDTO> groupMap;
    private Set<String> importGroupIdSet;

    public IntercomImportValidator(ConfigImportHolder importHolder, MonitorFactory monitorFactory,
        DeviceNewDao deviceNewDao, SimCardNewDao simCardNewDao, IntercomDao intercomDao,
        OriginalModelDao originalModelDao, GroupMonitorService groupMonitorService, List<GroupDTO> groupList) {
        this.monitorFactory = monitorFactory;
        this.importHolder = importHolder;
        this.deviceNewDao = deviceNewDao;
        this.simCardNewDao = simCardNewDao;
        this.intercomDao = intercomDao;
        this.originalModelDao = originalModelDao;
        this.groupMonitorService = groupMonitorService;
        this.importGroupIdSet = new HashSet<>();
        this.groupMap = new HashMap<>(CommonUtil.ofMapCapacity(groupList.size()));
        for (GroupDTO groupDTO : groupList) {
            this.groupMap.put(groupDTO.getName() + "@" + groupDTO.getOrgName(), groupDTO);
        }
    }

    @Override
    public JsonResultBean validate(List<IntercomImportDTO> list, boolean isCheckGroupName,
        List<OrganizationLdap> organizations) {
        //数据准备
        prepareData(list);
        //进行参数校验
        List<BindDTO> bindList = new ArrayList<>();

        for (int index = 1; index <= list.size(); index++) {
            IntercomImportDTO importDTO = list.get(index - 1);
            BindDTO bindDTO = new BindDTO();
            if (StringUtils.isNotBlank(importDTO.getErrorMsg())) {
                this.addInvalidMsg(index, importDTO.getErrorMsg(), importDTO);
                continue;
            }
            //1、校验企业
            if (!checkOrg(index, importDTO, bindDTO)) {
                continue;
            }
            //2、校验监控对象类型
            if (!checkMoType(index, importDTO, bindDTO)) {
                continue;
            }

            //3、校验监控对象
            if (!checkMoName(index, importDTO, bindDTO)) {
                continue;
            }

            //4、校验终端手机号
            if (!checkSim(index, importDTO, bindDTO)) {
                continue;
            }

            //5、校验终端
            if (!checkDevice(index, importDTO, bindDTO)) {
                continue;
            }
            //6、校验设备标识
            if (!checkIntercomDevice(index, importDTO, bindDTO)) {
                continue;
            }

            //7、校验密码
            if (!checkDevicePassword(index, importDTO)) {
                continue;
            }
            //8、校验分组
            if (!checkGroup(index, importDTO, bindDTO)) {
                continue;
            }
            bindList.add(bindDTO);
        }

        //检查分组下监控对象对象的数量
        checkGroupMonitorNum(list);
        String invalidMessage = getInvalidInfo();
        if (StringUtils.isNotBlank(invalidMessage)) {
            return new JsonResultBean(false, "导入文件有以下错误，请修复后重新导入：<br/>" + invalidMessage);
        }
        importHolder.setImportList(bindList);
        return new JsonResultBean(true);
    }

    private void addInvalidMsg(int index, String errorMsg, IntercomImportDTO importDTO) {
        importDTO.setErrorMsg(errorMsg);
        recordInvalidInfo(String.format("第%d条数据%s<br/>", index, errorMsg));
    }

    private boolean checkOrg(int index, IntercomImportDTO importDTO, BindDTO bindDTO) {
        String orgName = importDTO.getOrgName();
        if (importHolder.getOrgMap().containsKey(orgName)) {
            bindDTO.setOrgId(importHolder.getOrgMap().get(orgName));
            bindDTO.setOrgName(orgName);
            return true;
        }
        String errorMsg = "【企业：" + orgName + "】不存在";
        addInvalidMsg(index, errorMsg, importDTO);
        return false;
    }

    private boolean checkMoType(int index, IntercomImportDTO importDTO, BindDTO bindDTO) {
        String moTypeName = importDTO.getMonitorTypeName();
        String moType = MonitorTypeEnum.getTypeByName(moTypeName);
        if (StringUtils.isNotBlank(moType)) {
            bindDTO.setMonitorType(moType);
            return true;
        }
        addInvalidMsg(index, "【监控对象类型：" + moTypeName + "】类型错误", importDTO);
        return false;
    }

    private boolean checkMoName(int index, IntercomImportDTO importDTO, BindDTO bindDTO) {
        String moName = importDTO.getMonitorName().trim();
        String errMsg = "【监控对象：" + moName + "】";
        if (!nameChecker.matcher(moName).matches()) {
            errMsg = errMsg + "必须为2-20位的中文、数字、字母或短横杠";
            this.addInvalidMsg(index, errMsg, importDTO);
            return false;
        }
        MonitorBaseDTO monitor = monitorMap.getOrDefault(importDTO.getMonitorTypeName(), new HashMap<>(16)).get(moName);
        if (Objects.nonNull(monitor)) {
            if (Objects.equals(Vehicle.BindType.HAS_BIND, monitor.getIntercomBindType())) {
                errMsg = errMsg + "已经存在对讲绑定关系";
                addInvalidMsg(index, errMsg, importDTO);
                return false;
            }
            if (!importHolder.getOrgIdNameMap().containsKey(monitor.getOrgId())) {
                errMsg = errMsg + "没有权限";
                addInvalidMsg(index, errMsg, importDTO);
                return false;
            }
            bindDTO.setBindType(monitor.getBindType());
            bindDTO.setId(monitor.getId());
            bindDTO.setIntercomBindType(Vehicle.BindType.HAS_BIND);
            if (Objects.equals(Vehicle.BindType.HAS_BIND, monitor.getBindType())) {
                bindDTO.setOrgId(monitor.getOrgId());
                bindDTO.setOrgName(importHolder.getOrgIdNameMap().get(monitor.getOrgId()));
            }
        } else {
            bindDTO.setBindType(Vehicle.BindType.UNBIND);
        }
        bindDTO.setName(moName);
        return true;

    }

    private boolean checkSim(int index, IntercomImportDTO importDTO, BindDTO bindDTO) {
        String simNum = importDTO.getSimCardNumber().trim();
        String errMsg = "【终端手机号：" + simNum + "】";
        if (!RegexUtils.checkSIM(Converter.toBlank(simNum))) {
            errMsg = errMsg + "数据不规范";
            addInvalidMsg(index, errMsg, importDTO);
            return false;
        }
        if (simNum.startsWith("0")) {
            errMsg = errMsg + "不支持以0开头的SIM卡号";
            addInvalidMsg(index, errMsg, importDTO);
            return false;
        }
        SimCardDTO simCardDTO = existSimMap.get(simNum);
        bindDTO.setSimCardNumber(simNum);
        String bindMonitor = null;
        if (Objects.nonNull(simCardDTO)) {
            if (!importHolder.getOrgIdNameMap().containsKey(simCardDTO.getOrgId())) {
                errMsg = errMsg + "没有操作权限";
                addInvalidMsg(index, errMsg, importDTO);
                return false;
            }
            bindDTO.setSimCardId(simCardDTO.getId());
            bindDTO.setConfigId(simCardDTO.getConfigId());
            bindDTO.setRealSimCardNumber(simCardDTO.getRealId());
            bindMonitor = simCardDTO.getVehicleId();
        }
        if (Objects.isNull(bindMonitor) && Objects.equals(Vehicle.BindType.UNBIND, bindDTO.getBindType())) {
            return true;
        }
        if (Objects.equals(bindMonitor, bindDTO.getId())) {
            return true;
        }
        errMsg = Objects.isNull(bindMonitor) ? "【监控对象" + bindDTO.getName() + "】已经绑定了其他终端手机号" : errMsg + "已经绑定了其他监控对象";
        addInvalidMsg(index, errMsg, importDTO);
        return false;
    }

    private boolean checkDevice(int index, IntercomImportDTO importDTO, BindDTO bindDTO) {
        String deviceNum = importDTO.getDeviceNumber().trim();
        String errorMsg = "【设备号" + deviceNum + "】";
        if (!vehicleDeviceChecker.matcher(deviceNum).matches()) {
            errorMsg = errorMsg + "只能输入7位数字和大写字母";
            addInvalidMsg(index, errorMsg, importDTO);
            return false;
        }
        DeviceDTO deviceDTO = existDeviceMap.get(deviceNum);
        String configId = null;
        if (Objects.nonNull(deviceDTO)) {
            if (!importHolder.getOrgIdNameMap().containsKey(deviceDTO.getOrgId())) {
                errorMsg = errorMsg + "已经存在，不能重复";
                addInvalidMsg(index, errorMsg, importDTO);
                return false;
            }
            configId = deviceDTO.getBindId();
            bindDTO.setFunctionalType(deviceDTO.getFunctionalType());
            bindDTO.setTerminalType(deviceDTO.getTerminalType());
            bindDTO.setTerminalManufacturer(deviceDTO.getTerminalManufacturer());
            bindDTO.setDeviceType(deviceDTO.getDeviceType());
            bindDTO.setDeviceId(deviceDTO.getId());
        } else {
            bindDTO.setDeviceType(ProtocolEnum.T808_2013.getDeviceType());
            bindDTO.setTerminalType("F3-default");
            bindDTO.setTerminalManufacturer("[f]F3");
            bindDTO.setFunctionalType("3");
        }
        bindDTO.setDeviceNumber(deviceNum);

        if (Objects.equals(configId, bindDTO.getConfigId())) {
            return true;
        }
        errorMsg = Objects.isNull(configId) ? "【监控对象" + bindDTO.getName() + "】已经绑定了其他设备" : errorMsg + "已经绑定了其他监控对象";
        addInvalidMsg(index, errorMsg, importDTO);
        return false;
    }

    private boolean checkIntercomDevice(int index, IntercomImportDTO importDTO, BindDTO bindDTO) {
        //校验原始机型是否存在
        String errorMsg;
        if (!this.originalModels.contains(importDTO.getOriginalModel())) {
            errorMsg = "【原始机型" + importDTO.getOriginalModel() + "】不存在";
            addInvalidMsg(index, errorMsg, importDTO);
            return false;
        }
        String deviceNum = importDTO.getOriginalModel() + importDTO.getDeviceNumber();
        if (this.existIntercomDeviceNumSet.contains(deviceNum)) {
            errorMsg = "【对讲设备标识" + deviceNum + "】已经存在对讲绑定关系";
            addInvalidMsg(index, errorMsg, importDTO);
            return false;
        }
        return true;
    }

    private boolean checkDevicePassword(int index, IntercomImportDTO importDTO) {
        String password = importDTO.getDevicePassword().trim();
        if (!passwordSplitter.matcher(password).matches()) {
            recordInvalidInfo(String.format("第%d条数据【设备密码：%s】必须为8位的数字和字母<br/>", index + 1, password));
            String errorMsg = "【设备密码：" + password + "】必须为8位的数字和字母";
            addInvalidMsg(index, errorMsg, importDTO);
            return false;
        }
        return true;
    }

    private boolean checkGroup(int index, IntercomImportDTO importDTO, BindDTO bindDTO) {
        String[] groups = assignmentSplitter.split(importDTO.getAssignments());
        if (groups.length > 8) {
            addInvalidMsg(index, "【分组：" + importDTO.getAssignments() + "】大于8个", importDTO);
            return false;
        }

        Set<String> groupSet = new HashSet<>(Arrays.asList(groups));
        List<String> groupIds = new ArrayList<>();
        List<String> groupNames = new ArrayList<>();
        String[] names;
        for (String group : groupSet) {
            names = assignmentNameSplitter.split(group);
            if (names.length != 2) {
                addInvalidMsg(index, "【分组：" + group + "】数据不规范", importDTO);
                return false;
            }
            if (!groupMap.containsKey(group)) {
                addInvalidMsg(index, "【分组：" + group + "】不存在", importDTO);
                return false;
            }
            GroupDTO groupDTO = groupMap.get(group);
            groupIds.add(groupDTO.getId());
            groupNames.add(groupDTO.getName());
        }
        this.importGroupIdSet.addAll(groupIds);
        bindDTO.setGroupName(StringUtils.join(groupNames, ","));
        bindDTO.setGroupId(StringUtils.join(groupIds, ","));
        return true;
    }

    private void checkGroupMonitorNum(List<IntercomImportDTO> list) {
        //进行分组数校验
        Map<String, Integer> groupCountMap = groupMonitorService.getByGroupIds(importGroupIdSet).stream()
            .collect(Collectors.groupingBy(GroupMonitorDTO::getGroupId, Collectors.summingInt(x -> 1)));
        int maxMonitorNum = importHolder.getGroupMaxMonitorNum();
        for (int index = 1; index <= list.size(); index++) {
            IntercomImportDTO importDTO = list.get(index - 1);
            if (StringUtils.isNotBlank(importDTO.getErrorMsg())) {
                continue;
            }
            String[] groups = assignmentSplitter.split(importDTO.getAssignments());
            List<String> groupNames = new ArrayList<>();
            for (String group : groups) {
                GroupDTO groupDTO = groupMap.get(group);
                if (Objects.isNull(groupDTO)) {
                    continue;
                }
                Integer count = groupCountMap.getOrDefault(groupDTO.getId(), 0) + 1;
                if (count > maxMonitorNum) {
                    groupNames.add(groupDTO.getName());
                }
                groupCountMap.put(groupDTO.getId(), count);
            }
            if (!groupNames.isEmpty()) {
                String errorMsg = "【分组" + StringUtils.join(groupNames, ",") + "】下的监控对象的数量超过上限" + maxMonitorNum;
                addInvalidMsg(index, errorMsg, importDTO);
            }
        }
    }

    private void prepareData(List<IntercomImportDTO> importList) {
        //查询所有数据库已经存在的人车物
        this.monitorMap = new HashMap<>();
        for (MonitorTypeEnum typeEnum : MonitorTypeEnum.values()) {
            String type = typeEnum.getTypeName();
            this.monitorMap.put(type, getMonitorData(importList, type));
        }

        //获取存在的sim卡
        Set<String> simCardNumList = new HashSet<>();
        Set<String> deviceNumList = new HashSet<>();
        Set<String> intercomDeviceNumSet = new HashSet<>();
        if (importList.size() <= 1000) {
            importList.forEach(importDTO -> {
                simCardNumList.add(importDTO.getSimCardNumber());
                deviceNumList.add(importDTO.getDeviceNumber());
                intercomDeviceNumSet.add(importDTO.getOriginalModel() + importDTO.getDeviceNumber());
            });
        }
        List<SimCardDTO> simCardList = simCardNewDao.getByNumbers(simCardNumList);
        this.existSimMap = new HashMap<>(CommonUtil.ofMapCapacity(simCardList.size()));
        for (SimCardDTO simCardDTO : simCardList) {
            this.existSimMap.put(simCardDTO.getSimcardNumber(), simCardDTO);
        }

        List<DeviceDTO> deviceList = deviceNewDao.getByNumbers(deviceNumList);
        this.existDeviceMap = new HashMap<>(CommonUtil.ofMapCapacity(deviceList.size()));
        for (DeviceDTO deviceDTO : deviceList) {
            this.existDeviceMap.put(deviceDTO.getDeviceNumber(), deviceDTO);
        }

        //获取已经存在的终端设备标识
        this.existIntercomDeviceNumSet = intercomDao.getIntercomDeviceNum(intercomDeviceNumSet);
        this.originalModels = originalModelDao.getOriginalModelList().stream().map(OriginalModelInfo::getModelId)
            .collect(Collectors.toSet());
        this.importHolder.setExistDeviceMap(existDeviceMap);
        this.importHolder.setExistSimMap(existSimMap);
    }

    private Map<String, MonitorBaseDTO> getMonitorData(List<IntercomImportDTO> importList, String moTypeName) {
        Set<String> monitorNames = new HashSet<>();
        for (IntercomImportDTO importDTO : importList) {
            if (Objects.equals(moTypeName, importDTO.getMonitorTypeName())) {
                monitorNames.add(importDTO.getMonitorName());
            }
        }
        List<MonitorBaseDTO> monitorList;
        int monitorCount = monitorNames.size();
        if (monitorNames.isEmpty()) {
            monitorList = new ArrayList<>();
        } else {
            //监控对象数量在1000以内按监控对象名称查询监控对象，若1000以外，则查询全部的
            monitorNames = monitorCount <= 1000 ? monitorNames : null;
            monitorList = monitorFactory.create(MonitorTypeEnum.getTypeByName(moTypeName)).getByNames(monitorNames);
        }
        switch (MonitorTypeEnum.getTypeByName(moTypeName)) {
            case "0":
                importHolder.setImportVehicleNum(monitorCount);
                importHolder.setExistVehicleList(monitorList);
                break;
            case "1":
                importHolder.setImportPeopleNum(monitorCount);
                importHolder.setExistPeopleList(monitorList);
                break;
            case "2":
                importHolder.setImportThingNum(monitorCount);
                importHolder.setExistThingList(monitorList);
                break;
            default:
                break;
        }
        Map<String, MonitorBaseDTO> monitorMap = new HashMap<>(CommonUtil.ofMapCapacity(monitorList.size()));
        for (MonitorBaseDTO monitor : monitorList) {
            monitorMap.put(monitor.getName(), monitor);
        }
        return monitorMap;
    }
}
