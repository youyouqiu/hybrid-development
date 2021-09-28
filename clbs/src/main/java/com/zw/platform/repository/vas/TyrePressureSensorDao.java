package com.zw.platform.repository.vas;

import com.zw.platform.domain.basicinfo.form.TyrePressureSensorForm;
import com.zw.platform.domain.basicinfo.query.TyrePressureSensorQuery;

import java.util.List;

public interface TyrePressureSensorDao {

    List<TyrePressureSensorForm> getList(TyrePressureSensorQuery query);

    boolean saveSensor(TyrePressureSensorForm form);

    TyrePressureSensorForm findSensorById(String id);

    boolean updateSensor(TyrePressureSensorForm form);

    boolean deleteSensor(String id);

    TyrePressureSensorForm findSensorByName(String name);

    boolean addSensorByBatch(List<TyrePressureSensorForm> forms);

    List<TyrePressureSensorForm> findAllSensor();
}
