package com.zw.platform.basic.service.impl;

import com.zw.platform.basic.domain.FuelTypeDO;
import com.zw.platform.basic.dto.FuelTypeDTO;
import com.zw.platform.basic.repository.FuelTypeDao;
import com.zw.platform.basic.service.FuelTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 燃料管理
 */
@Service
public class FuelTypeServiceImpl implements FuelTypeService {

    @Autowired
    private FuelTypeDao fuelTypeDao;

    @Override
    public List<FuelTypeDTO> getByKeyword(String keyword) {
        List<FuelTypeDO> fuelTypeList = fuelTypeDao.getByKeyword(keyword);
        List<FuelTypeDTO> result = new ArrayList<>();
        for (FuelTypeDO fuelType : fuelTypeList) {
            result.add(new FuelTypeDTO(fuelType));
        }
        return result;
    }
}
