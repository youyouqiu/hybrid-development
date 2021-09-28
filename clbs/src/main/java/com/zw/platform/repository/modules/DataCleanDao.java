package com.zw.platform.repository.modules;

import com.zw.platform.domain.systems.form.DataCleanSettingForm;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author denghuabing
 * @version V1.0
 * @description: TODO
 * @date 2020/10/27
 **/
public interface DataCleanDao {

    DataCleanSettingForm get();

    void saveSetting(@Param("type") Integer type, @Param("value") Integer value);

    void saveTime(@Param("time") String time, @Param("cleanType") String cleanType);

    List<String> getSpotCheckIds(String overTime);

    void deleteSpotCheck(@Param("ids") List<String> ids);

    List<Map<String, String>> getMedia(String overTime);

    void deleteMedia(@Param("ids") List<String> ids);
}
