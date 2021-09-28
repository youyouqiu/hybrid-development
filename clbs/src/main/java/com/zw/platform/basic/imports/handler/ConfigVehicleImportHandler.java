package com.zw.platform.basic.imports.handler;

import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.domain.VehicleDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.MonitorBaseDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.dto.VehiclePurposeDTO;
import com.zw.platform.basic.imports.ConfigImportHolder;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.enums.VehicleColor;
import com.zw.platform.dto.constant.VehicleConstant;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.imports.lock.BaseImportHandler;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.platform.util.imports.lock.ImportTable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 信息配置车辆导入
 * @author zhangjuan
 */
@Slf4j
public class ConfigVehicleImportHandler extends BaseImportHandler {
    private final ConfigImportHolder importHolder;
    private final VehicleService vehicleService;
    private final Set<String> delVehicleIds;
    private final List<VehicleDTO> vehicleList;
    /**
     * excel中是否存在需要新增的车辆
     */
    private int vehicleCount;

    public ConfigVehicleImportHandler(ConfigImportHolder importHolder, VehicleService vehicleService) {
        this.importHolder = importHolder;
        this.vehicleService = vehicleService;
        this.delVehicleIds = new HashSet<>();
        this.vehicleList = new ArrayList<>();
        this.vehicleCount = 0;
    }

    @Override
    public ImportModule module() {
        return ImportModule.CONFIG;
    }

    @Override
    public int stage() {
        return 0;
    }

    @Override
    public ImportTable[] tables() {
        return new ImportTable[] { ImportTable.ZW_M_VEHICLE_INFO };
    }

    @Override
    public boolean uniqueValid() {
        if (importHolder.getImportVehicleNum() == 0) {
            progressBar.setTotalProgress(1 + vehicleList.size() * 5 / 2);
            return true;
        }
        Map<String, MonitorBaseDTO> vehicleMap =
            AssembleUtil.collectionToMap(importHolder.getExistVehicleList(), MonitorBaseDTO::getName);
        Map<String, String> orgMap = importHolder.getOrgMap();

        int errorCount = 0;
        for (BindDTO bindDTO : importHolder.getImportList()) {
            if (!Objects.equals(MonitorTypeEnum.VEHICLE.getType(), bindDTO.getMonitorType())) {
                continue;
            }
            bindDTO.setVehiclePassword("000000");
            //唯一性校验
            MonitorBaseDTO baseDTO = vehicleMap.get(bindDTO.getName());
            bindDTO.setOrgId(orgMap.get(bindDTO.getOrgName()));
            //监控对象在数据库不存在，为新增
            if (Objects.isNull(baseDTO)) {
                vehicleCount++;
                continue;
            }

            //校验监控对象是否已经绑定 对讲信息导入时即当对讲绑定字段不为空时，可以是绑定状态
            if (Objects.isNull(bindDTO.getIntercomBindType()) && Objects
                .equals(Vehicle.BindType.HAS_BIND, baseDTO.getBindType())) {
                bindDTO.setErrorMsg("【车辆: " + bindDTO.getName() + "】已绑定");
                errorCount++;
                continue;
            }

            //车辆已经存在，未绑定，但用户没有权限操作
            if (StringUtils.isBlank(importHolder.getOrgIdNameMap().get(baseDTO.getOrgId()))) {
                errorCount++;
                bindDTO.setErrorMsg("车辆已存在，不能重复导入");
                continue;
            }
            //校验db里的监控对象企业和导入的企业是否一致,若一致，赋值监控对象ID和监控对象的组织ID
            if (Objects.equals(bindDTO.getOrgId(), baseDTO.getOrgId())) {
                bindDTO.setId(baseDTO.getId());
                continue;
            }

            //若db里的监控对象企业和导入的企业不一致，则删除原来的车辆，新增导入的车辆
            delVehicleIds.add(baseDTO.getId());
            vehicleCount++;
        }

        progressBar.setTotalProgress(1 + vehicleCount * 3 / 2);
        //释放缓存
        importHolder.setExistVehicleList(null);

        return errorCount == 0;
    }

    private VehicleDTO build(BindDTO bindDTO) {
        VehicleDTO vehicle = new VehicleDTO();
        vehicle.setName(bindDTO.getName());
        vehicle.setPlateColor(bindDTO.getPlateColor());
        vehicle.setVehicleType(VehicleConstant.VEHICLE_TYPE_DEFAULT);
        vehicle.setCodeNum(VehicleConstant.CODE_NUM_DEFAULT);
        vehicle.setVehicleTypeName(VehicleConstant.SUB_TYPE_DEFAULT);
        vehicle.setStandard(Vehicle.Standard.COMMON);
        vehicle.setVehicleCategoryId(VehicleConstant.VEHICLE_CATEGORY_DEFAULT);
        vehicle.setVehicleCategoryName(VehicleConstant.VEHICLE_CATEGORY_NAME_DEFAULT);
        vehicle.setOperatingState(VehicleConstant.OPERATING_STATE_WORK);
        vehicle.setVehicleColor(VehicleColor.BLACK.getCodeVal());
        vehicle.setOrgId(bindDTO.getOrgId());
        vehicle.setOrgName(bindDTO.getOrgName());
        vehicle.setBindType(Vehicle.BindType.UNBIND);
        vehicle.setMonitorType(MonitorTypeEnum.VEHICLE.getType());
        vehicle.setFuelType(VehicleConstant.DEFAULT_FUEL_TYPE);
        return vehicle;
    }

    @SneakyThrows
    private void deleteOldVehicle(Set<String> removeVehicleIds) {
        if (CollectionUtils.isNotEmpty(removeVehicleIds)) {
            vehicleService.batchDel(removeVehicleIds);
        }
    }

    @Override
    public boolean addMysql() {
        deleteOldVehicle(delVehicleIds);

        if (vehicleCount == 0) {
            return true;
        }

        //获取车辆默认运营类别
        Iterator<VehiclePurposeDTO> iterator = TypeCacheManger.getInstance().getVehiclePurposes().iterator();
        String purposeId = null;
        if (iterator.hasNext()) {
            purposeId = iterator.next().getId();
        }
        //1. 封装车辆信息对象
        final String username = SystemHelper.getCurrentUsername();
        final Date createDate = new Date();
        List<VehicleDO> vehicleDOList = new ArrayList<>();
        for (BindDTO bindDTO : importHolder.getImportList()) {
            if (!Objects.equals(MonitorTypeEnum.VEHICLE.getType(), bindDTO.getMonitorType())) {
                continue;
            }

            if (StringUtils.isNotBlank(bindDTO.getId())) {
                continue;
            }
            VehicleDTO vehicleDTO = build(bindDTO);
            vehicleDTO.setVehiclePurpose(purposeId);
            VehicleDO vehicleDO = VehicleDO.build(vehicleDTO, true);
            vehicleDO.setCreateDataUsername(username);
            vehicleDO.setCreateDataTime(createDate);
            bindDTO.setId(vehicleDO.getId());
            this.vehicleList.add(vehicleDTO);
            vehicleDOList.add(vehicleDO);
        }

        //2.存储数据到mysql
        partition(vehicleDOList, vehicleService::addBatch);
        return true;
    }

    @Override
    public void addOrUpdateRedis() {
        vehicleService.addOrUpdateRedis(vehicleList, null);
    }
}
