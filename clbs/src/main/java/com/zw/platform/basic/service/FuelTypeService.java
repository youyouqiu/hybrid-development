package com.zw.platform.basic.service;

import com.zw.platform.basic.dto.FuelTypeDTO;

import java.util.List;

/**
 * 燃料管理接口
 * @author zhangjuan
 */
public interface FuelTypeService {

    /**
     * 根据燃料类型关键字获取燃料列表
     * @param keyword 关键字
     * @return 燃料类型列表
     */
    List<FuelTypeDTO> getByKeyword(String keyword);

}
