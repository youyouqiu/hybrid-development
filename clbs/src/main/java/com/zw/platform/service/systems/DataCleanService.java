package com.zw.platform.service.systems;

import com.zw.platform.util.common.JsonResultBean;

public interface DataCleanService {

    JsonResultBean getListData();

    boolean saveSetting(Integer type, Integer value);

    boolean saveDefault(Integer type);

    boolean saveTime(String time, String cleanType);
}
