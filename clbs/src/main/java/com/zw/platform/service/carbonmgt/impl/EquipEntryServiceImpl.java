package com.zw.platform.service.carbonmgt.impl;

import com.github.pagehelper.PageHelper;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.vas.carbonmgt.form.EquipForm;
import com.zw.platform.domain.vas.carbonmgt.query.EquipQuery;
import com.zw.platform.repository.vas.EquipDao;
import com.zw.platform.service.carbonmgt.EquipEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
@Service
public class EquipEntryServiceImpl implements EquipEntryService {
    @Autowired
    private EquipDao equipDao;

    @Override
    public List<EquipForm> findBenchmark(List<String> groupId, EquipQuery query, boolean doPage) {
        return doPage
                ? PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                        .doSelectPage(() -> equipDao.findBenchmark(groupId, query))
                : equipDao.findBenchmark(groupId, query);
    }

    @Override
    public boolean addBenchmark(EquipForm form) {
        return equipDao.addBenchmark(form);
    }

    @Override
    public boolean deleteBenchmark(EquipForm form) {
        form.setFlag(0);
        return equipDao.updateBenchmark(form);
    }

    @Override
    public boolean updateBenchmark(EquipForm form) {
        return equipDao.updateBenchmark(form);
    }

    @Override
    public List<VehicleInfo> findVehicleByUser(List<String> groupId, boolean flag) {
        if (flag) {
            return equipDao.findVehicleByAssign(groupId);
        }
        return equipDao.findVehicleByUser(groupId);
    }

    @Override
    public EquipForm findBenchmarkById(String id) {
        return equipDao.findBenchmarkById(id);
    }

    @Override
    public EquipForm findBenchmarkByVehicleId(String vehicleId) {
        return equipDao.findBenchmarkByVehicleId(vehicleId);
    }


}
