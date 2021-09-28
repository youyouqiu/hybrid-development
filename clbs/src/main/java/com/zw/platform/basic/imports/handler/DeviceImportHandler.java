package com.zw.platform.basic.imports.handler;

import com.zw.platform.basic.domain.DeviceDO;
import com.zw.platform.basic.rediscache.DeviceRedisCache;
import com.zw.platform.basic.repository.DeviceNewDao;
import com.zw.platform.util.excel.annotation.ExcelImportHelper;
import com.zw.platform.util.imports.lock.BaseImportHandler;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.platform.util.imports.lock.ImportTable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * @author denghuabing
 * @version V1.0
 * @date 2020/9/7
 **/
@Slf4j
public class DeviceImportHandler extends BaseImportHandler {

    private DeviceNewDao deviceDao;
    private List<DeviceDO> list;
    private boolean verified;

    public DeviceImportHandler(DeviceNewDao deviceDao, ExcelImportHelper deviceValidator) {
        this.deviceDao = deviceDao;
        this.verified = deviceValidator.getValidateResult();
        this.list = deviceValidator.getFinalData();
    }

    @Override
    public ImportModule module() {
        return ImportModule.DEVICE;
    }

    @Override
    public ImportTable[] tables() {
        return new ImportTable[] { ImportTable.ZW_M_DEVICE_INFO };
    }

    @Override
    public boolean uniqueValid() {

        if (CollectionUtils.isNotEmpty(list)) {
            progressBar.setTotalProgress(list.size());
        }

        return verified;
    }

    @Override
    public boolean addMysql() {
        partition(list, deviceDao::addDeviceByBatch);
        return true;
    }

    @Override
    public void addOrUpdateRedis() {
        DeviceRedisCache.addImportCache(list);
    }
}
