package com.zw.platform.basic.imports.handler;

import com.zw.platform.basic.domain.DeviceDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.DeviceDTO;
import com.zw.platform.basic.imports.ConfigImportHolder;
import com.zw.platform.basic.rediscache.DeviceRedisCache;
import com.zw.platform.basic.repository.DeviceNewDao;
import com.zw.platform.basic.service.DeviceService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.TerminalTypeInfo;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.imports.lock.BaseImportHandler;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.platform.util.imports.lock.ImportTable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 信息配置终端的导入
 * @author zhangjuan
 */
@Slf4j
public class ConfigDeviceImportHandler extends BaseImportHandler {
    private final ConfigImportHolder holder;
    private final DeviceService deviceService;
    private final List<String> delDeviceIds;
    private final DeviceNewDao deviceNewDao;
    private final List<DeviceDO> addList;

    public ConfigDeviceImportHandler(ConfigImportHolder importHolder, DeviceService deviceService,
        DeviceNewDao deviceNewDao) {
        this.holder = importHolder;
        this.deviceService = deviceService;
        this.delDeviceIds = new ArrayList<>();
        this.deviceNewDao = deviceNewDao;
        this.addList = new ArrayList<>();
    }

    @Override
    public ImportModule module() {
        return ImportModule.CONFIG;
    }

    @Override
    public int stage() {
        return 1;
    }

    @Override
    public ImportTable[] tables() {
        return new ImportTable[] { ImportTable.ZW_M_DEVICE_INFO };
    }

    @Override
    public boolean uniqueValid() {
        Map<String, String> orgNameIdMap = holder.getOrgMap();
        Map<String, String> orgIdNameMap = holder.getOrgIdNameMap();
        Map<String, DeviceDTO> existDeviceMap = getExistDevice();
        int errorCount = 0;
        int newDeviceCount = 0;
        Map<String, TerminalTypeInfo> terminalTypeMap =
            AssembleUtil.collectionToMap(holder.getTerminalTypeInfoList(), TerminalTypeInfo::getId);
        for (BindDTO bindDTO : holder.getImportList()) {
            final String deviceNumber = bindDTO.getDeviceNumber();
            final DeviceDTO existDevice = existDeviceMap.get(deviceNumber);
            bindDTO.setDeviceOrgId(orgNameIdMap.get(bindDTO.getOrgName()));
            String terminalTypeId =
                holder.getTerminalTypeMap().get(bindDTO.getTerminalManufacturer() + "_" + bindDTO.getTerminalType());
            bindDTO.setTerminalTypeId(terminalTypeId);
            TerminalTypeInfo terminalType = terminalTypeMap.get(terminalTypeId);
            if (Objects.nonNull(terminalType)) {
                bindDTO.setIsVideo(terminalType.getSupportVideoFlag());
            }
            //终端在平台不存在
            if (existDevice == null) {
                newDeviceCount++;
                continue;
            }

            bindDTO.setIsVideo(existDevice.getIsVideo());
            bindDTO.setManufacturerId(existDevice.getManufacturerId());
            bindDTO.setFunctionalType(existDevice.getFunctionalType());
            //终端已经被绑定了 对讲信息导入时即当对讲绑定字段不为空时，可以是绑定状态
            if (Objects.isNull(bindDTO.getIntercomBindType()) && StringUtils.isNotBlank(existDevice.getBindId())) {
                bindDTO.setErrorMsg("【终端编号: " + deviceNumber + "】已绑定");
                errorCount++;
                continue;
            }

            //终端已存在，未绑定，但用户没有操作权限（用户对终端所属企业不可见）
            if (StringUtils.isBlank(orgIdNameMap.get(existDevice.getOrgId()))) {
                bindDTO.setErrorMsg("终端已存在，不能重复导入");
                errorCount++;
                continue;
            }
            if (ObjectUtils.equals(existDevice.getOrgId(), bindDTO.getOrgId())) {
                if (!Objects.equals(existDevice.getDeviceType(), bindDTO.getDeviceType())) {
                    delDeviceIds.add(existDevice.getId());
                } else {
                    bindDTO.setDeviceId(existDevice.getId());
                }
                continue;
            }

            delDeviceIds.add(existDevice.getId());
            newDeviceCount++;
        }
        holder.setExistDeviceMap(null);
        progressBar.setTotalProgress(1 + newDeviceCount * 3);
        return errorCount == 0;
    }

    /**
     * 获取已经存在的终端，若导入数量小于1000按终端号进行查询，若大于1000则查询全部的终端
     * @return 终端编号和终端的映射关系
     */
    private Map<String, DeviceDTO> getExistDevice() {
        //对讲信息列表导入时特有逻辑，在对讲导入校验时会进行查询，这里是避免二次查询
        if (holder.getExistDeviceMap() != null) {
            return holder.getExistDeviceMap();
        }
        List<BindDTO> configs = holder.getImportList();
        List<String> deviceNumbers =
            configs.size() > 1000 ? null : configs.stream().map(BindDTO::getDeviceNumber).collect(Collectors.toList());
        List<DeviceDTO> deviceList = deviceService.getByDeviceNumbers(deviceNumbers);
        return AssembleUtil.collectionToMap(deviceList, DeviceDTO::getDeviceNumber);
    }

    @Override
    public boolean addMysql() {
        removeOldDevices(delDeviceIds);
        final String username = SystemHelper.getCurrentUsername();
        final Date createDate = new Date();

        List<DeviceDO> updateList = new ArrayList<>();
        for (BindDTO bindDTO : holder.getImportList()) {
            //终端在数据库中存在进行信息更新
            DeviceDO deviceDO;
            if (StringUtils.isNotBlank(bindDTO.getDeviceId())) {
                deviceDO = buildUpdateDO(username, createDate, bindDTO);
                updateList.add(deviceDO);
                continue;
            }

            //终端在数据库中不存在
            deviceDO = buildAddDO(username, createDate, bindDTO);
            bindDTO.setDeviceId(deviceDO.getId());
            this.addList.add(deviceDO);
        }
        partition(this.addList, deviceNewDao::addDeviceByBatch);
        partition(updateList, deviceNewDao::updateDeviceByBatch);
        return true;
    }

    @Override
    public void addOrUpdateRedis() {
        if (CollectionUtils.isNotEmpty(this.addList)) {
            DeviceRedisCache.addImportCache(this.addList);
        }
    }

    private DeviceDO buildUpdateDO(String username, Date createDate, BindDTO bindDTO) {
        DeviceDO device = new DeviceDO();
        device.setId(bindDTO.getDeviceId());
        device.setDeviceType(bindDTO.getDeviceType());
        device.setFunctionalType(bindDTO.getFunctionalType());
        device.setTerminalTypeId(bindDTO.getTerminalTypeId());
        device.setUpdateDataTime(createDate);
        device.setUpdateDataUsername(username);
        return device;
    }

    private DeviceDO buildAddDO(String username, Date createDate, BindDTO bindDTO) {
        DeviceDO device = new DeviceDO();
        device.setDeviceNumber(bindDTO.getDeviceNumber());
        device.setIsStart(1);
        device.setIsVideo(1);
        device.setDeviceType(bindDTO.getDeviceType());
        device.setFunctionalType(bindDTO.getFunctionalType());
        device.setOrgId(bindDTO.getOrgId());
        device.setTerminalTypeId(bindDTO.getTerminalTypeId());
        device.setCreateDataUsername(username);
        device.setCreateDataTime(createDate);
        return device;
    }

    @SneakyThrows
    private void removeOldDevices(List<String> removingDeviceIds) {
        if (CollectionUtils.isNotEmpty(removingDeviceIds)) {
            deviceService.deleteBatch(removingDeviceIds);
        }
    }
}
