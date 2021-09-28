package com.zw.platform.service.carbonmgt;

import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.vas.carbonmgt.form.EquipForm;
import com.zw.platform.domain.vas.carbonmgt.query.EquipQuery;

import java.util.List;


/**
 * <p>
 * Title: 设备录入Service
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 *
 * @version 1.0
 * @author: fanlu
 * @date 2016年9月18日下午3:11
 */
public interface EquipEntryService {

    List<EquipForm> findBenchmark(List<String> groupId, EquipQuery query, boolean doPage);

    boolean addBenchmark(EquipForm form);

    boolean deleteBenchmark(EquipForm form);

    boolean updateBenchmark(EquipForm form);

    List<VehicleInfo> findVehicleByUser(List<String> groupId, boolean flag);

    EquipForm findBenchmarkById(String id);

    EquipForm findBenchmarkByVehicleId(String vehicleId);
}
