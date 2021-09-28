package com.zw.platform.service.systems;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.Page;
import com.zw.platform.domain.systems.DeviceUpgrade;
import com.zw.platform.domain.systems.query.DeviceUpgradeQuery;

public interface DeviceUpgradeService {

    String addDeviceUpgradeFile(MultipartFile file, DeviceUpgrade deviceUpgrade)throws Exception;

    void updateDeviceUpgradeFile(DeviceUpgrade deviceUpgrade);

    void addDeviceUpgradeByBatch(List<DeviceUpgrade> list);

    void deleteDeviceUpgradeById(List<String> ids);

    void deleteDeviceUpgradeById(String id);

    Page<DeviceUpgrade> queryList(DeviceUpgradeQuery baseQueryBean);
}
