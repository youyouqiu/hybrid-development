package com.zw.platform.repository.vas;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.form.TyrePressureSettingForm;
import com.zw.platform.domain.basicinfo.query.TyrePressureSettingQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TyrePressureSettingDao {

    List<TyrePressureSettingForm> findTyrePressureSettingByIds(@Param("list") List<String> ids);

    Page<TyrePressureSettingForm> findTyrePressureSetting(@Param("query") TyrePressureSettingQuery query,
        @Param("userId") String userId, @Param("list") List<String> userOrgListId);

    List<TyrePressureSettingForm> findExistByVid(String vid);

    boolean addTyrePressureSetting(TyrePressureSettingForm form);

    TyrePressureSettingForm findTyrePressureSettingById(String id);

    boolean updateTyrePressureSetting(TyrePressureSettingForm form);

    boolean deleteTyrePressureSetting(String id);

    boolean deleteMore(@Param("ids") String[] ids);

    TyrePressureSettingForm findTyrePressureSettingByVid(String vid);

}
