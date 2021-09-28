package com.zw.platform.service.generalCargoReport.impl;

import com.zw.platform.domain.generalCargoReport.CargoRecordForm;
import com.zw.platform.repository.modules.CargoDao;
import com.zw.platform.service.generalCargoReport.CargoCommonService;
import com.zw.platform.util.common.CargoCommonUtils;
import com.zw.platform.util.common.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class CargoCommonServiceImpl implements CargoCommonService {

    @Autowired
    private CargoDao cargoDao;

    /**
     * 获取组织一段时间内存在过的国货车的id
     *
     * @param groupId
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public Set<String> getCargoIds(String groupId, String startTime, String endTime) {
        Set<String> nowVid = CargoCommonUtils.getGroupCargoVids(groupId);
        String nowTime = DateUtil.getDateToString(new Date(), null);
        Set<String> startAddVids = cargoDao.getCargoRecordVids(groupId, startTime, nowTime, 1);
        Set<String> startDelVids = cargoDao.getCargoRecordVids(groupId, startTime, nowTime, 3);
        if (startAddVids != null && startAddVids.size() > 0) {
            nowVid.removeAll(startAddVids);
        }
        if (startDelVids != null && startDelVids.size() > 0) {
            nowVid.addAll(startDelVids);
        }
        Set<String> addVids = cargoDao.getCargoRecordVids(groupId, startTime, endTime, 1);
        Set<String> delVids = cargoDao.getCargoRecordVids(groupId, startTime, endTime, 3);
        if (addVids != null && addVids.size() > 0) {
            nowVid.addAll(addVids);
        }
        if (delVids != null && delVids.size() > 0) {
            nowVid.addAll(delVids);
        }
        return nowVid;
    }

    @Override
    public List<CargoRecordForm> getCargoRecordByGroupId(String groupId, String startTime,
        String endTime) {
        if (StringUtils.isBlank(groupId) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            return null;
        }
        List<String> groupIds = new ArrayList<>(new HashSet<>(Arrays.asList(groupId.split(","))));
        return cargoDao.getCargoRecordByGroupId(groupIds, startTime, endTime);
    }
}
