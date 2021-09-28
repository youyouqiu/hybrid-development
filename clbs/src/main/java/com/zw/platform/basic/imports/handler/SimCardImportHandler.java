package com.zw.platform.basic.imports.handler;

import com.zw.platform.basic.domain.SimCardDO;
import com.zw.platform.basic.rediscache.SimCardRedisCache;
import com.zw.platform.basic.repository.SimCardNewDao;
import com.zw.platform.util.excel.annotation.ExcelImportHelper;
import com.zw.platform.util.imports.lock.BaseImportHandler;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.platform.util.imports.lock.ImportTable;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2020/11/9 11:50
 */
public class SimCardImportHandler extends BaseImportHandler {
    private SimCardNewDao simCardNewDao;
    private List<SimCardDO> list;
    private boolean verified;

    public SimCardImportHandler(SimCardNewDao simCardNewDao, ExcelImportHelper deviceValidator) {
        this.simCardNewDao = simCardNewDao;
        this.verified = deviceValidator.getValidateResult();
        this.list = deviceValidator.getFinalData();
    }

    @Override
    public ImportModule module() {
        return ImportModule.SIM_CARD;
    }

    @Override
    public ImportTable[] tables() {
        return new ImportTable[] { ImportTable.ZW_M_SIM_CARD_INFO };
    }

    @Override
    public boolean addMysql() {
        partition(list, simCardNewDao::addByBatch);
        return true;
    }

    @Override
    public boolean uniqueValid() {


        if (CollectionUtils.isNotEmpty(list)) {
            progressBar.setTotalProgress(list.size());
        }

        return verified;
    }

    @Override
    public void addOrUpdateRedis() {
        SimCardRedisCache.addImportCache(list);
    }
}
