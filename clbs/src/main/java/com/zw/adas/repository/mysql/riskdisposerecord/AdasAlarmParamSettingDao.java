package com.zw.adas.repository.mysql.riskdisposerecord;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.AdasPlatformParamSetting;
import com.zw.platform.domain.systems.form.DirectiveForm;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/***
 @Author gfw
 @Date 2019/6/10 10:01
 @Description 报警参数设置表
 @version 1.0
 **/
public interface AdasAlarmParamSettingDao {

    /**
     * 新增报警参数设置表 批量/单个
     * @param record
     * @return
     */
    boolean insertAlarmParamBatch(List<AdasAlarmParamSetting> record);

    /**
     * 查询车id对应的所有报警参数
     * @param vehicleId
     * @param paramType
     * @return
     */
    List<AdasAlarmParamSetting> selectByVehicleId(String vehicleId, Integer paramType);

    /**
     * 修改
     * @param adasAlarmParamSetting
     * @param vid
     */
    void updateAlarmParamById(@Param("adasParam") AdasAlarmParamSetting adasAlarmParamSetting,
        @Param("vid") String vid);

    /**
     * 批量删除
     * @param vehicleIds
     */
    void deleteCommonByBatch(@Param("vehicleIds") List<String> vehicleIds);

    void updateDirectiveStatus(@Param("directiveForms") List<DirectiveForm> directiveForms);

    List<AdasPlatformParamSetting> findPlatformSetting(String vehicleId);

    void deletePlatformParamByVehicleId(@Param("vehicleIds") List<String> vehicleIds);

    boolean insertPlatformParams(List<AdasPlatformParamSetting> platformParamSettings);

    List<AdasPlatformParamSetting> findAllPlatformSetting();

    List<Map<String, String>> findAllTireModel();

    String findTireModelById(String id);

    void updateDirectiveStatusByIdSet(@Param("directiveIdSet") Set<String> directiveIdSet);

    /**
     * 批量删除参数指定页签
     * @param vehicleIds
     */
    void deleteAdasByParamType(@Param("vehicleIds") List<String> vehicleIds,
        @Param("paramTypeSet") Set<Integer> paramTypeSet);

    List<Map<String, String>> findLogicChannelsByVehicleIds(@Param("vehicleIds") List<String> vehicleIds);
}
