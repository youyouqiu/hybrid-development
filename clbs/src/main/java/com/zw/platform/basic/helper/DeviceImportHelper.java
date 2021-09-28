package com.zw.platform.basic.helper;

import com.zw.platform.basic.domain.DeviceDO;
import com.zw.platform.basic.dto.imports.DeviceImportDTO;
import com.zw.platform.basic.repository.DeviceNewDao;
import com.zw.platform.domain.basicinfo.TerminalTypeInfo;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.excel.ImportExcel;
import com.zw.platform.util.excel.annotation.ExcelImportHelper;
import com.zw.ws.common.PublicVariable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Chen Feng
 * @version 1.0 2017/11/30
 */
public class DeviceImportHelper implements ExcelImportHelper<DeviceDO, DeviceImportDTO> {
    /**
     * 用来存放最终校验的结果
     */
    private boolean verified = false;

    private final Set<String> macAddressList = new HashSet<>();

    private Map<String, Map<String, TerminalTypeInfo>> terminalTypeMap = new HashMap<>();
    private Set<String> terminalManufacturer = new HashSet<>();

    private Set<String> allDeviceNumber;

    private ImportExcel importExcel;
    /**
     * 组织名称对应的第一条数据
     */
    private Map<String, String> firstOrgMap = new HashMap<>();
    /**
     * 导入的数据信息
     */
    private List<DeviceImportDTO> importDataList;
    private DeviceNewDao deviceDao;

    private static final Pattern DEVICE_NUMBER_CHECKER = Pattern.compile("^[0-9a-zA-Z]{7,30}$");
    private static final Pattern DEVICE_MAC_CHECKER = Pattern.compile("^([0-9a-fA-F]{2})(([/\\s-][0-9a-fA-F]{2}){5})$");

    public DeviceImportHelper(DeviceNewDao deviceDao, ImportExcel importExcel) {
        this.deviceDao = deviceDao;
        this.importExcel = importExcel;
    }

    @Override
    public void validate(Map<String, String> orgNameIdMap) throws BusinessException {
        validateDataSize(importDataList);
        init(orgNameIdMap, importDataList);
        for (DeviceImportDTO device : importDataList) {

            if (device != null) {
                if (StringUtils.isNotBlank(device.getErrorMsg())) {
                    continue;
                }
                if (!checkExist(device)) {
                    continue;
                }
                checkData(device);
            }
        }
        List<String> errors = importDataList.stream().map(DeviceImportDTO::getErrorMsg).filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
        verified = errors.isEmpty();

    }

    @Override
    public List<DeviceImportDTO> getExcelData() {
        return importDataList;
    }

    @Override
    public void init(Class<DeviceImportDTO> cls) throws InstantiationException, IllegalAccessException {
        importDataList = importExcel.getDataListNew(cls);
    }

    @Override
    public boolean getValidateResult() {
        return verified;
    }

    private boolean checkExist(DeviceImportDTO device) {
        if (!terminalManufacturer.contains(device.getTerminalManufacturer())) {
            device.setErrorMsg("终端厂商：不存在");
            return false;
        } else {
            if (!terminalTypeMap.containsKey(device.getTerminalManufacturer())) {
                device.setErrorMsg("终端厂商：不存在");
                return false;
            } else {
                Map<String, TerminalTypeInfo> terminalTypes = terminalTypeMap.get(device.getTerminalManufacturer());
                if (!terminalTypes.containsKey(device.getTerminalType())) {
                    device.setErrorMsg("终端型号与终端厂商不匹配");
                    return false;
                }
            }
        }

        if (PublicVariable.getDeviceTypeId(device.getDeviceType()).isEmpty()) {
            device.setErrorMsg("通讯类型：不存在");
            return false;
        }

        if (PublicVariable.getFunctionTypeId(device.getFunctionalType()).isEmpty()) {
            device.setErrorMsg("功能类型：不存在");
            return false;
        }
        if (StringUtils.isNotBlank(device.getMacAddress())) {
            if (macAddressList.contains(device.getMacAddress())) {
                device.setErrorMsg("Mac地址：已存在");
                return false;
            }
        }
        return true;
    }

    private void checkData(DeviceImportDTO device) {
        if (StringUtils.isBlank(device.getErrorMsg()) && allDeviceNumber.contains(device.getDeviceNumber())) {
            device.setErrorMsg("终端已存在");
            return;
        }
        if (!DEVICE_NUMBER_CHECKER.matcher(device.getDeviceNumber()).matches()) {
            device.setErrorMsg("终端号错误,只能输入7-30位数字字母");
            return;
        }
        if (StringUtils.isNotBlank(device.getMacAddress())) {
            if (!DEVICE_MAC_CHECKER.matcher(device.getMacAddress()).matches()) {
                device.setErrorMsg("Mac地址错误,只能输入数字（0-9）、字母（A-F、a-f）、每两个字符以'-'隔开，长度17位，如：09-af-EA-AE-3C-AF");
                return;
            }
        }

        String orgName = device.getOrgName();
        if (!firstOrgMap.containsKey(orgName)) {
            device.setErrorMsg("所属企业无权限");
            return;
        }

        if (checkLength(device.getDeviceName(), "终端名称", 50, device)) {
            return;
        }
        if (checkLength(device.getManuFacturer(), "制造商", 100, device)) {
            return;
        }
        if (checkLength(device.getBarCode(), "条码", 64, device)) {
            return;
        }
        if (checkLength(device.getManufacturerId(), "制造商ID", 11, device)) {
            return;
        }
        if (checkLength(device.getDeviceModelNumber(), "终端型号（注册）", 30, device)) {
            return;
        }
        if (checkLength(device.getMacAddress(), "Mac地址", 17, device)) {
            return;
        }
        checkLengthValue(device.getMacAddress(), "Mac地址", 17, device);
    }

    private boolean checkLength(String field, String fieldName, int maxLength, DeviceImportDTO device) {
        if (field != null && !field.isEmpty() && field.length() > maxLength) {
            device.setErrorMsg(String.format("%s错误,长度不能超过%d<br/>", fieldName, maxLength));
            return false;
        }
        return true;
    }

    /**
     * 测试长度是否为指定长度
     */
    private void checkLengthValue(String field, String fieldName, int length, DeviceImportDTO device) {
        if (field != null && !field.isEmpty() && field.length() != length) {
            device.setErrorMsg(String.format("%s错误,长度为%d<br/>", fieldName, length));
        }
    }

    /**
     * 初始化校验相关信息
     * @param orgNameIdMap
     */
    private void init(Map<String, String> orgNameIdMap, List<DeviceImportDTO> importList) {
        //获取所有的mac地址
        macAddressList.addAll(deviceDao.getAllMacAddress());
        terminalManufacturer.addAll(deviceDao.getTerminalManufacturer());
        firstOrgMap = orgNameIdMap;
        List<TerminalTypeInfo> allTerminalType = deviceDao.getAllTerminalType();
        if (CollectionUtils.isEmpty(allTerminalType)) {
            return;
        }
        terminalTypeMap = allTerminalType.stream().collect(Collectors
            .groupingBy(TerminalTypeInfo::getTerminalManufacturer,
                Collectors.toMap(TerminalTypeInfo::getTerminalType, Function.identity())));
        importDataList = importList;
        allDeviceNumber = deviceDao.findAllDeviceNumber();
    }

    @Override
    public List<DeviceDO> getFinalData() {
        //组装最终插入数据库的实体
        List<DeviceDO> deviceDOList = new ArrayList<>();
        //验证不通过，则不进行导入数据组装
        if (!verified) {
            return deviceDOList;
        }

        for (DeviceImportDTO data : importDataList) {
            String terminalManufacturer = data.getTerminalManufacturer();
            TerminalTypeInfo terminalTypeInfo = terminalTypeMap.get(terminalManufacturer).get(data.getTerminalType());
            deviceDOList.add(DeviceDO.getImportData(data, terminalTypeInfo, firstOrgMap.get(data.getOrgName())));
        }
        return deviceDOList;
    }

}
