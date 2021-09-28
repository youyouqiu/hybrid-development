package com.zw.platform.service.sensor;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.domain.basicinfo.form.TyrePressureSettingForm;
import com.zw.platform.domain.basicinfo.query.TyrePressureSettingQuery;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;

public interface TyrePressureSettingService extends IpAddressService {

    Page<TyrePressureSettingForm> getList(TyrePressureSettingQuery query);

    TyrePressureSettingForm refreshSendStatus(String vid);

    List<TyrePressureSettingForm> findExistByVid(String vid);

    JsonResultBean addTyrePressureSetting(TyrePressureSettingForm form) throws Exception;

    TyrePressureSettingForm findTyrePressureSettingById(String id);

    JsonResultBean updateTyrePressureSetting(TyrePressureSettingForm form) throws Exception;

    JsonResultBean deleteTyrePressureSetting(String id) throws Exception;

    JsonResultBean deleteMore(String ids) throws Exception;

    void sendSettingParam(List<JSONObject> list) throws Exception;

    TyrePressureSettingForm findTyrePressureSettingByVid(String vid);

    JsonResultBean updateRoutineSetting(TyrePressureSettingForm form, String dealType)
        throws Exception;
}
