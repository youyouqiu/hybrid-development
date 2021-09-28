package com.zw.platform.service.systems.impl;

import com.zw.platform.domain.systems.form.DataCleanSettingForm;
import com.zw.platform.repository.modules.DataCleanDao;
import com.zw.platform.service.systems.DataCleanService;
import com.zw.platform.util.DataCleanUtil;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author denghuabing
 * @version V1.0
 * @description: TODO
 * @date 2020/10/27
 **/
@Service
public class DataCleanServiceImpl implements DataCleanService {

    @Autowired
    private DataCleanDao dataCleanDao;

    @Autowired
    private DataCleanUtil dataCleanUtil;

    @Override
    public JsonResultBean getListData() {
        DataCleanSettingForm data = dataCleanDao.get();
        return new JsonResultBean(data);
    }

    @Override
    public boolean saveSetting(Integer type, Integer value) {
        dataCleanDao.saveSetting(type, value);
        return true;
    }

    @Override
    public boolean saveDefault(Integer type) {
        dataCleanDao.saveSetting(type, DataCleanSettingForm.DEFAULT_VALUE);
        return true;
    }

    @Override
    public boolean saveTime(String time, String cleanType) {
        dataCleanDao.saveTime(time, cleanType);
        dataCleanUtil.changeTask(time);
        return true;
    }
}
