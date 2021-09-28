package com.zw.talkback.util.excel.validator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.RedisKeys;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RegexUtils;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.validator.ImportValidator;
import com.zw.talkback.domain.basicinfo.form.InConfigImportForm;
import com.zw.talkback.domain.basicinfo.form.InConfigInfoForm;
import com.zw.talkback.domain.intercom.info.OriginalModelInfo;
import com.zw.talkback.repository.mysql.ClusterDao;
import com.zw.talkback.repository.mysql.OriginalModelDao;
import com.zw.talkback.util.IntercomRedisKeys;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class IntercomObjectImportValidator extends ImportValidator<InConfigImportForm> {

    Map<String, List<OrganizationLdap>> groupNames;
    private Jedis nineJedis;
    private Jedis tenJedis;

    private static final int MAX_COUNT = 10000;

    private static final Pattern searchSplitter = Pattern.compile("&");
    private static final Pattern vehicleDeviceChecker = Pattern.compile("^[0-9A-Z]{7}$");
    private static final Pattern assignmentSplitter = Pattern.compile(",");
    private static final Pattern assignmentNameSplitter = Pattern.compile("@");
    private static final Pattern passwordSplitter = Pattern.compile("^[0-9a-zA-Z]{8}$");
    private static final Pattern nameChecker = Pattern.compile("^[0-9a-zA-Z\\u4e00-\\u9fa5_-]{2,20}$");
    private final Set<String> boundConfigMonitors = new HashSet<>();
    private final Set<String> boundConfigDevices = new HashSet<>();
    private final Set<String> boundConfigSimCards = new HashSet<>();

    private final Set<String> boundMonitors = new HashSet<>();
    private final Set<String> boundDevices = new HashSet<>();
    private final Set<String> boundSimCards = new HashSet<>();
    private final Map<String, String> nameMonitorIdMap = new HashMap<>();
    private final Map<String, OriginalModelInfo> modelMap = new HashMap<>();

    private final Map<String, Integer> monitorNameMap = new HashMap<>();
    private final Map<String, Integer> simsMap = new HashMap<>();
    private final Map<String, Integer> devicesNameMap = new HashMap<>();

    private Map<String, String> groupIdList = new HashMap<>();

    // 记录缓存中每个分组下监控对象的数量
    private Map<String, Integer> groupCount;

    private OriginalModelDao originalModelDao;

    private ClusterDao clusterDao;

    private UserService userService;

    /**
     * 一个分组下面最大的车辆数
     */
    private final Integer maxNumberAssignmentMonitor;

    public IntercomObjectImportValidator(Map<String, List<OrganizationLdap>> groupNames,
        OriginalModelDao originalModelDao, ClusterDao clusterDao, UserService userService,
        Integer maxNumberAssignmentMonitor) {
        super();
        this.groupNames = groupNames;
        this.originalModelDao = originalModelDao;
        this.clusterDao = clusterDao;
        this.userService = userService;
        this.maxNumberAssignmentMonitor = maxNumberAssignmentMonitor;
    }

    @Override
    public JsonResultBean validate(List<InConfigImportForm> list, boolean isCheckGroupName,
        List<OrganizationLdap> organizations) {
        List<InConfigInfoForm> unBindList = new ArrayList<>();
        List<InConfigInfoForm> bindConfList = new ArrayList<>();
        List<InConfigImportForm> unbindImportList = new ArrayList<>();
        groupCount = new HashMap<>();
        try {
            // 对讲在4.4.0版本不可用，后续版本恢复兼容
            // nineJedis = RedisHelper.getJedis(PublicVariable.REDIS_NINE_DATABASE);
            // tenJedis = RedisHelper.getJedis(PublicVariable.REDIS_TEN_DATABASE);
            prepareCheckInfo();
            InConfigInfoForm inConfigInfoForm;
            for (int i = 0, n = list.size(); i < n; i++) {

                inConfigInfoForm = new InConfigInfoForm();
                inConfigInfoForm.setDeviceType("1");

                if (getRequiredOrRepeatable(i + 1)) {
                    continue;
                }
                InConfigImportForm config = list.get(i);
                //数据检查
                checkData(i, config, inConfigInfoForm);
                config.setGroupId(inConfigInfoForm.getGroupid());
                //检查是否绑定对讲对象，若未绑定，判断是否绑定了定位关系
                if (!checkIntercomBind(i, config)) {
                    if (checkConfigBind(i, config, inConfigInfoForm)) {
                        bindConfList.add(inConfigInfoForm);
                    } else {
                        unBindList.add(inConfigInfoForm);
                        unbindImportList.add(config);
                    }
                }

            }
            //检查数量
            checkGroupCount();
        } finally {
            nineJedis.close();
            tenJedis.close();
        }
        String invalidMessage = getInvalidInfo();
        invalidMessage = invalidMessage.isEmpty() ? "" : "导入文件有以下错误，请修复后重新导入：<br/>" + invalidMessage;

        JSONObject msg = new JSONObject();
        msg.put("invalidMessage", invalidMessage);
        msg.put("unBindList", unBindList);
        msg.put("bindConfList", bindConfList);
        msg.put("unbindImportList", unbindImportList);
        return new JsonResultBean(msg);
    }

    private void checkData(int index, InConfigImportForm importData, InConfigInfoForm inConfigInfoForm) {

        //检验组织名称
        if (checkGroupName(index, importData, inConfigInfoForm)) {
            return;
        }

        //校验设备号
        inConfigInfoForm.setDevices(importData.getDeviceNumber());
        if (checkDeviceData(index, importData)) {
            return;
        }

        //校验SIM卡号
        inConfigInfoForm.setSims(importData.getSimcardNumber());
        if (checkSimCardData(index, importData)) {
            return;
        }

        //校验原始机型
        inConfigInfoForm.setIntercomDeviceId(importData.getModelId() + importData.getDeviceNumber());
        if (checkModelId(index, importData, inConfigInfoForm)) {
            return;
        }

        //校验设备密码
        inConfigInfoForm.setDevicePassword(importData.getDevicePassword());
        if (checkDevicePasswordData(index, importData)) {
            return;
        }

        //校验监控类型
        if (checkMonitorType(index, importData, inConfigInfoForm)) {
            return;
        }
        //校验监控对象
        inConfigInfoForm.setBrands(importData.getMonitorName());
        if (checkMonitor(index, importData)) {
            return;
        }
        checkGroupData(index, importData, inConfigInfoForm);

        Integer priority =
            importData.getPriority() == null || importData.getPriority() > 5 || importData.getPriority() < 1 ? 1 :
                importData.getPriority();
        inConfigInfoForm.setPriority(priority);
        inConfigInfoForm.setTextEnable(1);
        inConfigInfoForm.setImageEnable(1);
        inConfigInfoForm.setAudioEnable(1);
    }

    private void checkGroupData(int index, InConfigImportForm importData, InConfigInfoForm inConfigInfoForm) {
        if (StringUtils.isBlank(importData.getAssignments())) {
            recordInvalidInfo(String.format("第%d条数据【分组：%s】不能为空<br/>", index + 1, importData.getAssignments()));
            return;
        }
        String[] groups = assignmentSplitter.split(importData.getAssignments());
        Set<String> groupSet = new HashSet<>(Arrays.asList(groups));
        if (groups.length > 8) {
            recordInvalidInfo(String.format("第%d条数据【分组：%s】大于8个<br/>", index + 1, importData.getAssignments()));
            return;
        }

        String[] names;
        int num = 0;

        StringBuilder assignNames = null;
        StringBuilder assignIds = null;
        for (String group : groupSet) {
            num++;
            names = assignmentNameSplitter.split(group);
            if (names.length != 2) {
                recordInvalidInfo(String.format("第%d条数据【分组：%s】数据不规范<br/>", index + 1, group));
                continue;
            }
            if (names[0].isEmpty() || names[1].isEmpty()) {
                recordInvalidInfo(String.format("第%d条数据【分组：%s】不存在<br/>", index + 1, group));
                continue;
            }
            if (num == 1) {
                assignNames = new StringBuilder(names[0]);
                assignIds = new StringBuilder(groupIdList.getOrDefault(group, ""));
            } else {
                assignNames.append(",").append(names[0]);
                assignIds.append(";").append(groupIdList.getOrDefault(group, ""));
            }
            Integer count = groupCount.getOrDefault(group, 0);
            groupCount.put(group, count + 1);
        }
        if (assignIds != null) {
            inConfigInfoForm.setAssignmentName(assignNames.toString());
            inConfigInfoForm.setCitySelID(assignIds.toString());
        }
    }

    private void checkGroupCount() {
        JSONArray array;
        for (Map.Entry<String, Integer> entry : groupCount.entrySet()) {
            String groupId = groupIdList.getOrDefault(entry.getKey(), "");
            if (groupId.isEmpty()) {
                recordInvalidInfo(String.format("【分组：%s】不存在或没有权限<br/>", entry.getKey()));
                continue;
            }
            array = JSON.parseArray(tenJedis.get(groupId + "_assignment_monitor_list"));
            array = Optional.ofNullable(array).orElse(new JSONArray());
            if (entry.getValue() + array.size() > maxNumberAssignmentMonitor) {
                recordInvalidInfo(
                    String.format("导入后【分组：%s】下的监控对象的数量将超过上限%d<br/>", entry.getKey(), maxNumberAssignmentMonitor));
            }
        }
    }

    private Map<String, String> getGroupNameIdList() {
        UserLdap user = SystemHelper.getCurrentUser();
        String userId = userService.getUserUuidById(user.getId().toString());

        List<JSONObject> userGroupList =
            JSON.parseArray(tenJedis.get(user.getUsername() + "_zw_list"), JSONObject.class);
        userGroupList = Optional.ofNullable(userGroupList).orElse(new ArrayList<>());

        //获取当前用户拥有权限的对讲群组ID
        List<String> userIntercomAss = clusterDao.getUserAssignIds(userId);
        for (JSONObject jsonObject : userGroupList) {
            String assignId = jsonObject.get("id").toString();
            if (userIntercomAss.contains(assignId)) {
                String groupKey = jsonObject.get("name") + "@" + jsonObject.get("groupName");
                groupIdList.put(groupKey, jsonObject.get("id").toString());
            }
        }
        return groupIdList;
    }

    public boolean checkMonitor(int index, InConfigImportForm config) {
        String monitorName = config.getMonitorName();
        if (!nameChecker.matcher(Converter.toBlank(monitorName)).matches()) {
            recordInvalidInfo(String.format("第%d条数据【监控对象：%s】必须为2-20位的中文、数字、字母或短横杠<br/>", index + 1, monitorName));
            return true;
        }
        if (monitorNameMap.containsKey(monitorName)) {
            recordInvalidInfo(String
                .format("第%d条数据【监控对象：%s】与第%d条数据重复<br/>", index + 1, monitorName, monitorNameMap.get(monitorName)));
            return true;
        }
        monitorNameMap.put(monitorName, index + 1);
        return false;
    }

    /**
     * 校验 监控对象类型
     * @param index  index
     * @param config config
     * @return boolean
     */
    public boolean checkMonitorType(int index, InConfigImportForm config, InConfigInfoForm inConfigInfoForm) {
        boolean hasError = false;
        String monitorType = config.getMonitorType();
        if ("人".equals(monitorType)) {
            inConfigInfoForm.setMonitorType("1");
        } else if ("车".equals(monitorType)) {
            inConfigInfoForm.setMonitorType("0");
        } else if ("物".equals(monitorType)) {
            inConfigInfoForm.setMonitorType("2");
        } else {
            recordInvalidInfo(String.format("第%d条数据【监控对象类型：%s】类型错误<br/>", index + 1, monitorType));
            hasError = true;
        }
        return hasError;
    }

    private boolean checkDevicePasswordData(int index, InConfigImportForm config) {
        String password = config.getDevicePassword();
        if (!passwordSplitter.matcher(Converter.toBlank(password)).matches()) {
            recordInvalidInfo(String.format("第%d条数据【设备密码：%s】必须为8位的数字和字母<br/>", index + 1, password));
            return true;
        }
        return false;
    }

    private boolean checkModelId(int index, InConfigImportForm config, InConfigInfoForm inConfigInfoForm) {
        String modelId = config.getModelId();
        if (StringUtils.isBlank(modelId) || modelId.length() != 5) {
            recordInvalidInfo(String.format("第%d条数据【原始机型：%s】长度必须为5<br/>", index + 1, modelId));
            return true;
        }

        inConfigInfoForm.setModelId(modelId);
        OriginalModelInfo modelInfo = modelMap.get(modelId);
        if (modelInfo == null) {
            recordInvalidInfo(String.format("第%d条数据【原始机型：%s】不存在<br/>", index + 1, modelId));
            return true;
        }
        inConfigInfoForm.setOriginalModelInfo(modelInfo);
        inConfigInfoForm.setOriginalModelId(modelInfo.getIndex());
        return false;
    }

    private boolean checkSimCardData(int index, InConfigImportForm config) {
        String simNum = config.getSimcardNumber();

        if (!RegexUtils.checkSIM(Converter.toBlank(simNum))) {
            recordInvalidInfo(String.format("第%d条数据【终端手机号：%s】数据不规范<br/>", index + 1, simNum));
            return true;
        }
        if (simNum.startsWith("0")) {
            recordInvalidInfo(String.format("第%d条数据【终端手机号：%s】不支持以0开头的SIM卡号<br/>", index + 1, simNum));
            return true;
        }
        if (simsMap.containsKey(simNum)) {
            recordInvalidInfo(String.format("第%d条数据【终端手机号：%s】与第%d条重复<br/>", index + 1, simNum, simsMap.get(simNum)));
            return true;
        }
        simsMap.put(simNum, index + 1);
        return false;
    }

    private boolean checkDeviceData(int index, InConfigImportForm config) {
        if (!vehicleDeviceChecker.matcher(Converter.toBlank(config.getDeviceNumber())).matches()) {
            recordInvalidInfo(String.format("第%d条数据的设备标识只能输入7位数字和大写字母<br/>", index + 1));
            return true;
        }
        if (devicesNameMap.containsKey(config.getDeviceNumber())) {
            recordInvalidInfo(
                String.format("第%d条数据设备标识与第%d条设备标识数据重复<br/>", index + 1, devicesNameMap.get(config.getDeviceNumber())));
            return true;
        }
        devicesNameMap.put(config.getDeviceNumber(), index + 1);
        return false;
    }

    private boolean checkGroupName(int index, InConfigImportForm config, InConfigInfoForm inConfigInfoForm) {
        boolean hasError = false;
        String companyName = config.getGroupName();
        if (StringUtils.isEmpty(companyName)) {
            recordInvalidInfo(String.format("第%d条数据【企业：%s】不能为空<br/>", index + 1, companyName));
            return true;
        }
        List<OrganizationLdap> organizationLdapList = groupNames.get(companyName);
        if (CollectionUtils.isNotEmpty(organizationLdapList)) {
            // 如果存在多个企业, 默认取第一个
            OrganizationLdap organizationLdap = organizationLdapList.get(0);
            inConfigInfoForm.setGroupid(organizationLdap.getUuid());
            inConfigInfoForm.setGroupName(organizationLdap.getName());
        } else {
            recordInvalidInfo(String.format("第%d条数据【企业：%s】不存在<br/>", index + 1, companyName));
            hasError = true;
        }
        return hasError;
    }

    /**
     * @param index  index
     * @param config index
     * @return 是否绑定
     */
    private boolean checkIntercomBind(int index, InConfigImportForm config) {
        boolean isBind = false;
        if (boundMonitors.contains(config.getMonitorName())) {
            isBind = true;
            recordInvalidInfo(String.format("第%d条数据【监控对象：%s】已经存在对讲绑定关系<br/>", index + 1, config.getMonitorName()));
        }
        if (boundDevices.contains(config.getDeviceNumber())) {
            isBind = true;
            recordInvalidInfo(String.format("第%d条数据【设备标识：%s】已经存在对讲绑定关系<br/>", index + 1, config.getDeviceNumber()));
        }

        if (boundSimCards.contains(config.getSimcardNumber())) {
            isBind = true;
            recordInvalidInfo(String.format("第%d条数据【终端手机号：%s】已经存在对讲绑定关系<br/>", index + 1, config.getSimcardNumber()));
        }

        return isBind;
    }

    /**
     * 是否绑定定位对象
     * @param index  index
     * @param config config
     * @return 是否绑定
     */
    private boolean checkConfigBind(int index, InConfigImportForm config, InConfigInfoForm inConfigInfoForm) {
        String simNum = config.getSimcardNumber();
        String deviceNum = config.getDeviceNumber();
        String monitorName = config.getMonitorName();
        if (!boundConfigMonitors.contains(monitorName) && !boundConfigDevices.contains(deviceNum)
            && !boundConfigSimCards.contains(simNum)) {
            return false;
        }
        String monitorId = nameMonitorIdMap.get(monitorName);
        if (StringUtils.isBlank(monitorId)) {
            recordInvalidInfo(
                String.format("第%d条数据已经存在定位绑定关系，SIM卡或设备标识已经绑定过，但绑定的监控对象不是【监控对象：%s】<br/>", index + 1, monitorName));
            return true;
        }

        final BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(monitorId);
        if (null != bindInfo) {
            if (!simNum.equals(bindInfo.getSimCardNumber())) {
                recordInvalidInfo(String.format("第%d条数据【监控对象：%s】已经存在定位绑定关系，但绑定的是【终端手机号：%s】而不是【%s】<br/>",
                        index + 1, monitorName, bindInfo.getSimCardNumber(), simNum));
                return true;
            }
            if (!deviceNum.equals(bindInfo.getDeviceNumber())) {
                inConfigInfoForm.setDevices(bindInfo.getDeviceNumber());
            }

            inConfigInfoForm.setSimID(bindInfo.getSimCardId());
            inConfigInfoForm.setId(bindInfo.getConfigId());
            inConfigInfoForm.setBrandID(bindInfo.getId());
            inConfigInfoForm.setDeviceID(bindInfo.getDeviceId());
        }
        return true;
    }

    private List<Map.Entry<String, String>> searchConfigInfo() {
        ScanParams params = new ScanParams().match("*&*").count(MAX_COUNT);
        String key = RedisKeys.VEHICLE_DEVICE_SIMCARD_FUZZY_SEARCH;
        ScanResult<Map.Entry<String, String>> scanResult;
        List<Map.Entry<String, String>> results = new ArrayList<>();
        String cursor = ScanParams.SCAN_POINTER_START;
        do {
            scanResult = tenJedis.hscan(key, cursor, params);
            results.addAll(scanResult.getResult());
            cursor = scanResult.getCursor();
        } while (!cursor.equals(ScanParams.SCAN_POINTER_START));
        return results;
    }

    private List<Map.Entry<String, String>> searchBindInfo() {
        ScanParams params = new ScanParams().match("*&*").count(MAX_COUNT);
        String key = IntercomRedisKeys.INTERCOM_INFO_FUZZY_SEARCH;
        ScanResult<Map.Entry<String, String>> scanResult;
        List<Map.Entry<String, String>> results = new ArrayList<>();
        String cursor = ScanParams.SCAN_POINTER_START;
        do {
            scanResult = nineJedis.hscan(key, cursor, params);
            results.addAll(scanResult.getResult());
            cursor = scanResult.getCursor();
        } while (!cursor.equals(ScanParams.SCAN_POINTER_START));
        return results;
    }

    private void prepareConfigInfo(String key, String value) {
        // 缓存中key的顺序为监控对象，设备，SIM卡
        String[] keys = searchSplitter.split(key);
        String[] values = searchSplitter.split(value);
        if (keys.length == 3) {
            boundConfigMonitors.add(keys[0]);
            boundConfigDevices.add(keys[1]);
            boundConfigSimCards.add(keys[2]);
            nameMonitorIdMap.put(keys[0], values[1]);
        }

    }

    private void prepareBindInfo(String key) {
        // 缓存中key的顺序为监控对象，设备，SIM卡
        String[] keys = searchSplitter.split(key);
        if (keys.length == 3) {
            boundMonitors.add(keys[0]);
            boundDevices.add(keys[1].substring(keys[1].length() - 7, keys[1].length()));
            boundSimCards.add(keys[2]);
        }
    }

    private void prepareCheckInfo() {
        List<Map.Entry<String, String>> bindInfo = searchBindInfo();
        for (Map.Entry<String, String> entry : bindInfo) {
            prepareBindInfo(entry.getKey());
        }

        List<Map.Entry<String, String>> configInfo = searchConfigInfo();
        for (Map.Entry<String, String> entry : configInfo) {
            prepareConfigInfo(entry.getKey(), entry.getValue());
        }
        prepareOriginModel();

        groupIdList = getGroupNameIdList();
    }

    private void prepareOriginModel() {
        List<OriginalModelInfo> originalModels = originalModelDao.getOriginalModelList();
        for (OriginalModelInfo modelInfo : originalModels) {
            modelMap.put(modelInfo.getModelId(), modelInfo);
        }
    }

}
