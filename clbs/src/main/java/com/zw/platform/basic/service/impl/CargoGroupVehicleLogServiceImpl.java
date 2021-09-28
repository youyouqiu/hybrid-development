package com.zw.platform.basic.service.impl;

import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.domain.CargoGroupVehicleDO;
import com.zw.platform.basic.dto.VehicleCategoryDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.service.CargoGroupVehicleLogService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 山东货运数据报表记录车辆更新记录
 */
@Service
public class CargoGroupVehicleLogServiceImpl implements CargoGroupVehicleLogService {
    @Autowired
    private NewVehicleDao vehicleDao;
    private TypeCacheManger cacheManger = TypeCacheManger.getInstance();

    @Override
    public void add(VehicleDTO vehicleDTO) {
        VehicleCategoryDTO category = cacheManger.getVehicleCategory(vehicleDTO.getVehicleCategoryId());
        if (Objects.isNull(category) || !Objects.equals(category.getStandard(), Vehicle.Standard.FREIGHT_TRANSPORT)) {
            return;
        }
        CargoGroupVehicleDO cargoGroupVehicleDO = new CargoGroupVehicleDO(vehicleDTO, "add");
        vehicleDao.addCargoGroupVehicle(Collections.singletonList(cargoGroupVehicleDO));
        RedisHelper.addToSet(HistoryRedisKeyEnum.ORG_CARGO_VEHICLE.of(vehicleDTO.getOrgId()), vehicleDTO.getId());
    }

    @Override
    public void delete(List<VehicleDTO> vehicleList) {
        List<CargoGroupVehicleDO> list = new ArrayList<>();
        Map<String, List<String>> orgIdMap = new HashMap<>(16);
        for (VehicleDTO vehicle : vehicleList) {
            if (!Objects.equals(vehicle.getStandard(), Vehicle.Standard.FREIGHT_TRANSPORT)) {
                continue;
            }

            List<String> ids = orgIdMap.get(vehicle.getOrgId());
            if (ids == null) {
                ids = new ArrayList<>();
            }
            ids.add(vehicle.getId());
            orgIdMap.put(vehicle.getOrgId(), ids);

            list.add(new CargoGroupVehicleDO(vehicle, "delete"));
        }

        if (list.isEmpty()) {
            return;
        }

        vehicleDao.addCargoGroupVehicle(list);
        for (Map.Entry<String, List<String>> entry : orgIdMap.entrySet()) {
            RedisHelper.delSetItem(HistoryRedisKeyEnum.ORG_CARGO_VEHICLE.of(entry.getKey()), entry.getValue());
        }
    }

    @Override
    public void update(VehicleDTO curVehicle, VehicleDTO oldVehicle) {
        List<CargoGroupVehicleDO> list = updateRedis(curVehicle, oldVehicle);
        if (CollectionUtils.isNotEmpty(list)) {
            vehicleDao.addCargoGroupVehicle(list);
        }
    }

    private List<CargoGroupVehicleDO> updateRedis(VehicleDTO curVehicle, VehicleDTO oldVehicle) {
        //判断车辆当前及之前是否是货运车辆,都不是不做变更
        boolean beforeIsFreight = Objects.equals(oldVehicle.getStandard(), Vehicle.Standard.FREIGHT_TRANSPORT);
        boolean nowIssFreight = Objects.equals(curVehicle.getStandard(), Vehicle.Standard.FREIGHT_TRANSPORT);
        if (!(beforeIsFreight || nowIssFreight)) {
            return null;
        }
        //车辆类别是否发生改变
        boolean categoryIsChange =
            !Objects.equals(curVehicle.getVehicleCategoryId(), oldVehicle.getVehicleCategoryId());
        //组织是否发生改变
        boolean orgIsChange = !Objects.equals(curVehicle.getOrgId(), oldVehicle.getOrgId());

        //车辆状态是否发生改变
        boolean statusIsChange = !Objects.equals(curVehicle.getIsStart(), oldVehicle.getIsStart());

        //车辆类别、企业和状态未发生改变，不用记录变更
        if (categoryIsChange && orgIsChange && statusIsChange) {
            return null;
        }

        //车辆从非货运变成货运车辆
        if (!beforeIsFreight) {
            add(curVehicle);
            return null;
        }

        //从货运变成非货运
        if (!nowIssFreight) {
            CargoGroupVehicleDO cargoGroupVehicleDO = new CargoGroupVehicleDO(oldVehicle, "delete");
            RedisHelper.delSetItem(HistoryRedisKeyEnum.ORG_CARGO_VEHICLE.of(oldVehicle.getOrgId()), oldVehicle.getId());
            return Collections.singletonList(cargoGroupVehicleDO);
        }

        //现在和之前都是货运车辆，修改了车辆状态或组织
        List<CargoGroupVehicleDO> list = new ArrayList<>();
        if (orgIsChange) {
            list.add(new CargoGroupVehicleDO(oldVehicle, "delete"));
            list.add(new CargoGroupVehicleDO(curVehicle, "add"));
            RedisHelper.delSetItem(HistoryRedisKeyEnum.ORG_CARGO_VEHICLE.of(oldVehicle.getOrgId()), oldVehicle.getId());
            RedisHelper.addToSet(HistoryRedisKeyEnum.ORG_CARGO_VEHICLE.of(curVehicle.getOrgId()), curVehicle.getId());
        } else if (statusIsChange) {
            list.add(new CargoGroupVehicleDO(curVehicle, "update"));
        }

        return list;
    }

    @Override
    public void updateBatch(List<VehicleDTO> curVehicleList, Map<String, VehicleDTO> oldVehicleMap) {
        List<CargoGroupVehicleDO> list = new ArrayList<>();
        List<CargoGroupVehicleDO> tempList;
        for (VehicleDTO curVehicle : curVehicleList) {
            tempList = updateRedis(curVehicle, oldVehicleMap.get(curVehicle.getId()));
            if (tempList != null) {
                list.addAll(tempList);
            }
        }

        if (CollectionUtils.isNotEmpty(list)) {
            vehicleDao.addCargoGroupVehicle(list);
        }
    }
}
