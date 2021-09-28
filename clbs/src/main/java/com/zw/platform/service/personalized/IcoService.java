package com.zw.platform.service.personalized;

import java.util.List;

@Deprecated
public interface IcoService {

    /**
     * 根据子类型id获取所有使用该子类型车辆id
     * @param subTypeId
     * @return
     */
    List<String> getVidsBySubTypeId(String subTypeId);

    /**
     * 根据类别id获取所有使用该类别车辆id
     * @param categoryId
     * @return
     */
    List<String> getVidsByCategoryId(String categoryId);
}
