package com.zw.platform.basic.imports.validator;

import com.google.common.collect.Sets;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.MonitorBaseDTO;
import com.zw.platform.basic.dto.imports.ConfigImportDTO;
import com.zw.platform.basic.imports.ConfigImportHolder;
import com.zw.platform.basic.service.impl.MonitorFactory;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RegexUtils;
import com.zw.platform.util.excel.validator.ImportValidator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 信息配置配置参数校验类
 * @author zhangjuan
 */
public class ConfigImportValidator extends ImportValidator<ConfigImportDTO> {
    private static final Pattern DEVICE_CHECKER = Pattern.compile("^[0-9a-zA-Z]{7,30}$");
    private static final int SIM_CARD_NUM_MAX_LENGTH = 20;
    private static final Set<String> DEVICE_FUNCTIONAL_TYPE =
        Sets.newHashSet("简易型车机", "行车记录仪", "对讲设备", "超长待机设备", "手咪设备", "定位终端");
    private final MonitorFactory monitorFactory;

    private final ConfigImportHolder importHolder;
    /**
     * 平台已经存在，但用户无权限或已经绑定的车辆
     */
    private Set<String> othersVehNames;

    /**
     * 平台已经存在，但用户无权限或已经绑定的人员
     */
    private Set<String> othersPeopleNames;

    /**
     * 平台已经存在，但用户无权限或已经绑定的物品
     */
    private Set<String> othersThingNames;


    public ConfigImportValidator(MonitorFactory monitorFactory, ConfigImportHolder importHolder) {
        this.monitorFactory = monitorFactory;
        this.importHolder = importHolder;
    }

    @Override
    public JsonResultBean validate(List<ConfigImportDTO> configList, boolean isCheckGroupName,
        List<OrganizationLdap> orgList) {
        //获取获取监控对象
        prepareNoAuthMonitor(configList, orgList);

        //获取企业名称与id的映射关系
        Map<String, String> orgMap = importHolder.getOrgMap();
        for (int i = 0, n = configList.size(); i < n; i++) {
            if (getRequiredOrRepeatable(i + 1)) {
                continue;
            }

            ConfigImportDTO config = configList.get(i);
            if (StringUtils.isNotBlank(config.getErrorMsg())) {
                continue;
            }

            //校验企业名称
            if (checkOrgName(orgMap, config)) {
                continue;
            }

            //校验监控对象
            if (checkMonitor(config)) {
                continue;
            }

            //校验终端号
            if (!DEVICE_CHECKER.matcher(config.getDeviceNumber()).matches()) {
                config.setErrorMsg("监控对象终端号只能输入7-30位数字字母");
                continue;
            }

            //校验终端手机号
            if (checkSimCard(config)) {
                continue;
            }

            // 校验终端厂商和终端型号
            if (checkTerminal(config)) {
                continue;
            }

            // 校验服务周期
            checkBillingAndDurDate(config);
        }
        Optional<ConfigImportDTO> option =
            configList.stream().filter(o -> StringUtils.isNotBlank(o.getErrorMsg())).findFirst();
        return new JsonResultBean(!option.isPresent());
    }

    private void prepareNoAuthMonitor(List<ConfigImportDTO> configList, List<OrganizationLdap> orgList) {
        this.othersVehNames = getNoAuthMonitor(configList, orgList, MonitorTypeEnum.VEHICLE.getType());
        this.othersPeopleNames = getNoAuthMonitor(configList, orgList, MonitorTypeEnum.PEOPLE.getType());
        this.othersThingNames = getNoAuthMonitor(configList, orgList, MonitorTypeEnum.THING.getType());
    }

    private Set<String> getNoAuthMonitor(List<ConfigImportDTO> configs, List<OrganizationLdap> orgList, String moType) {
        Set<String> monitorNames = new HashSet<>();
        for (ConfigImportDTO config : configs) {
            if (Objects.equals(moType, MonitorTypeEnum.getTypeByName(config.getMonitorType()))) {
                monitorNames.add(config.getMonitorName());
            }
        }
        List<MonitorBaseDTO> monitorList;
        int monitorCount = monitorNames.size();
        if (monitorNames.isEmpty()) {
            monitorList = new ArrayList<>();
        } else {
            //监控对象数量在1000以内按监控对象名称查询监控对象，若1000以外，则查询全部的
            monitorNames = monitorCount <= 1000 ? monitorNames : null;
            monitorList = monitorFactory.create(moType).getByNames(monitorNames);
        }

        switch (moType) {
            case "0":
                importHolder.setExistVehicleList(monitorList);
                importHolder.setImportVehicleNum(monitorCount);
                break;
            case "1":
                importHolder.setExistPeopleList(monitorList);
                importHolder.setImportPeopleNum(monitorCount);
                break;
            case "2":
                importHolder.setExistThingList(monitorList);
                importHolder.setImportThingNum(monitorCount);
                break;
            default:
                break;
        }

        if (monitorList.isEmpty()) {
            return new HashSet<>();
        }

        // todo 目前保留了原有逻辑 后续可以优化成过滤掉绑定的监控对象
        // 获取未绑定的监控对象
        Map<String, String> idNameMap =
            AssembleUtil.collectionToMap(monitorList, MonitorBaseDTO::getId, MonitorBaseDTO::getName);

        //获取用户权限下未绑定的监控对象
        List<RedisKey> unbindRedisKeys = new ArrayList<>();
        orgList.forEach(o -> unbindRedisKeys.add(monitorFactory.getOrgUnbindKey(moType, o.getUuid())));
        Set<String> unBindMonitors = RedisHelper.hkeys(unbindRedisKeys);
        if (CollectionUtils.isNotEmpty(unBindMonitors)) {
            unBindMonitors.forEach(idNameMap::remove);
        }
        return new HashSet<>(idNameMap.values());
    }

    private boolean checkOrgName(Map<String, String> orgMap, ConfigImportDTO config) {
        String orgName = config.getOrgName();
        if (StringUtils.isBlank(orgName)) {
            config.setErrorMsg("【企业：" + orgName + "】不能为空");
            return true;
        }
        if (!orgMap.containsKey(orgName)) {
            config.setErrorMsg("【企业：" + orgName + "】不存在");
            return true;
        }
        return false;
    }

    private boolean checkMonitor(ConfigImportDTO config) {
        StringBuilder errorBuilder = new StringBuilder();
        boolean hasError = false;
        if (!ProtocolEnum.DEVICE_TYPE_NAMES.contains(config.getDeviceTypeName())) {
            addErrorMsg(errorBuilder, "监控对象与通讯类型不匹配");
            hasError = true;
        }

        //前面导入对象转换成BindDTO时，已经把功能类型的文字版转换成数字的枚举类型，若为空就是在支持的范围外的
        if (!DEVICE_FUNCTIONAL_TYPE.contains(config.getFunctionalType())) {
            addErrorMsg(errorBuilder, "监控对象与功能类型不匹配");
            hasError = true;
        }
        String monitorName = config.getMonitorName();
        if (!RegexUtils.checkPlateNumber(monitorName)) {
            addErrorMsg(errorBuilder, "【监控对象：" + monitorName + "】监控对象编号错误");
            hasError = true;
        }

        if (StringUtils.isBlank(config.getMonitorType())) {
            hasError = true;
            addErrorMsg(errorBuilder, "【监控对象类型：" + config.getMonitorType() + "】类型错误");
        }
        boolean hasRight = true;
        switch (config.getMonitorType()) {
            case "车":
                hasRight = !othersVehNames.contains(monitorName);
                break;
            case "人":
                hasRight = !othersPeopleNames.contains(monitorName);
                break;
            case "物":
                hasRight = !othersThingNames.contains(monitorName);
                break;
            default:
                break;
        }
        if (!hasRight) {
            hasError = true;
            addErrorMsg(errorBuilder, "【监控对象：" + monitorName + "】监控对象没有权限或已绑定");
        }

        if (Objects.equals("车", config.getMonitorType()) && StringUtils.isBlank(config.getPlateColorStr())) {
            addErrorMsg(errorBuilder, "【车牌颜色】不能为空");
            hasError = true;
        }

        if (hasError) {
            config.setErrorMsg(errorBuilder.toString());
        }
        return hasError;
    }

    private void addErrorMsg(StringBuilder builder, String msg) {
        builder.append(msg).append(";");
    }

    private boolean checkSimCard(ConfigImportDTO config) {
        String simCardNum = config.getSimCardNumber();
        if (StringUtils.isBlank(simCardNum)) {
            config.setErrorMsg("【终端手机号】不能为空");
            return true;
        }
        if (simCardNum.length() > SIM_CARD_NUM_MAX_LENGTH) {
            String msg = String.format("【终端手机号：%s】长度不能超过%d<br/>", simCardNum, SIM_CARD_NUM_MAX_LENGTH);
            config.setErrorMsg(msg);
            return true;
        }
        if (!RegexUtils.checkSIM(simCardNum)) {
            config.setErrorMsg("【终端手机号：" + simCardNum + "】数据不规范");
            return true;
        }

        if (StringUtils.isBlank(config.getRealSimCardNumber())) {
            return false;
        }
        if (!config.getRealSimCardNumber().trim().matches("^[0-9]{7,20}$")) {
            config.setErrorMsg("【真实sim卡号：" + config.getRealSimCardNumber() + "】必须是7-20位整数");
            return true;
        }

        return false;
    }

    /**
     * 校验 终端厂商和终端型号
     * @param config 导入的信息配置
     * @return boolean
     */
    private boolean checkTerminal(ConfigImportDTO config) {
        String terminalManufacturer = config.getTerminalManufacturer();
        String terminalType = config.getTerminalType();
        boolean manufacturerNotEmpty = StringUtils.isNotEmpty(terminalManufacturer);
        boolean terminalTypeNotEmpty = StringUtils.isNotEmpty(terminalType);
        Map<String, String> terminalTypeMap = importHolder.getTerminalTypeMap();
        //终端厂商和终端型号 都不为空时
        if (manufacturerNotEmpty && terminalTypeNotEmpty) {
            String terminalTypeId = terminalTypeMap.get(terminalManufacturer + "_" + terminalType);
            if (Objects.nonNull(terminalTypeId)) {
                return false;
            }
            String msg = String.format("未查询到【终端厂商：%s与终端型号：%s】绑定关系", terminalManufacturer, terminalType);
            config.setErrorMsg(msg);
            return true;
        }

        // 终端型号 为空
        if (manufacturerNotEmpty) {
            config.setErrorMsg("【终端型号】不能为空");
            return true;
        }

        //终端厂商 为空
        if (terminalTypeNotEmpty) {
            config.setErrorMsg("【终端厂商】不能为空");
            return true;
        }

        //终端型号和终端厂商都为空时 默认 F3
        config.setTerminalManufacturer("[f]F3");
        config.setTerminalType("F3-default");

        return false;
    }

    private boolean checkBillingAndDurDate(ConfigImportDTO config) {
        String billingDateStr = config.getBillingDate();
        String durDateStr = config.getExpireDate();
        // 都为空不做校验
        if (StringUtils.isEmpty(billingDateStr) || StringUtils.isEmpty(durDateStr)) {
            return false;
        }

        if (!RegexUtils.checkDate(Converter.toBlank(billingDateStr))) {
            config.setErrorMsg("【计费日期：" + billingDateStr + "】类型错误");
            return true;
        } else if (!RegexUtils.checkDate(Converter.toBlank(durDateStr))) {
            config.setErrorMsg("【到期日期：" + durDateStr + "】类型错误");
            return true;
        } else if (LocalDateUtils.parseDate(durDateStr).before(LocalDateUtils.parseDate(billingDateStr))) {
            config.setErrorMsg("【计费日期【" + billingDateStr + "】不能晚于到期日期【" + durDateStr + "】");
            return true;
        }
        return false;
    }
}
