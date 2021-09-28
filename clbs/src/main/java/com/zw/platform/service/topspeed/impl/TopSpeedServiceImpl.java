package com.zw.platform.service.topspeed.impl;

import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.topspeed_entering.DeviceRegister;
import com.zw.platform.repository.modules.DeviceRegisterDao;
import com.zw.platform.service.topspeed.TopSpeedService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by LiaoYuecai on 2017/3/1.
 */
@Service
public class TopSpeedServiceImpl implements TopSpeedService {
    @Autowired
    private DeviceRegisterDao deviceRegisterDao;
    @Autowired
    private UserService userService;

    private static final String FORMATE_DATE = "yyyy-MM-dd HH:mm:ss";

    @Override
    public List<DeviceRegister> findDeviceData() {
        List<String> userOrgListId = userService.getCurrentUserOrgIds();
        List<DeviceRegister> list = deviceRegisterDao.findListInUser(userOrgListId);
        list.addAll(deviceRegisterDao.findListOutUser(userOrgListId));
        //过滤异常数据（唯一标识为空的）
        for (int i = 0; i < list.size(); i++) {
            DeviceRegister dr = list.get(i);
            if (StringUtils.isBlank(dr.getUniqueNumber())) {
                list.remove(list.get(i));
                i--;
            } else {
                if (dr.getUpdateDataTime() != null) {
                    StringBuilder value = new StringBuilder();
                    value.append(dr.getUniqueNumber()).append("（")
                        .append(DateFormatUtils.format(dr.getUpdateDataTime(), FORMATE_DATE)).append("）");
                    dr.setUniqueNumber(value.toString());
                }
            }
        }
        return list;

    }

    @Override
    public void deleteByDeviceId(String deviceId) {
        deviceRegisterDao.deleteByDeviceId(deviceId);
    }
}
