package com.zw.platform.service.generalCargoReport;

import com.zw.platform.domain.generalCargoReport.CargoRecordForm;

import java.util.List;
import java.util.Set;

public interface CargoCommonService {

    /**
     * 获取组织一段时间内存在过的国货车的id
     * @param groupId
     * @return
     */
    Set<String> getCargoIds(String groupId, String startTime, String endTime);

    /**
     * 根据组织id获取一段时间内存在过的普货车的变动记录(增，删， 改)
     */
    List<CargoRecordForm> getCargoRecordByGroupId(String groupId, String startTime, String endTime);

}
