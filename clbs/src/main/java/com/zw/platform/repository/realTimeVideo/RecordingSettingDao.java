package com.zw.platform.repository.realTimeVideo;

import com.zw.platform.domain.realTimeVideo.RecordingSetting;
import com.zw.platform.domain.vas.alram.form.AlarmParameterSettingForm;
import com.zw.platform.vo.realTimeVideo.VideoAlarmParam;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RecordingSettingDao {
    /**
     * 根据车辆id查询音视频报警参数列表
     * @return
     */
    List<Map<String, String>> getVedioAlarmsByVehicleId(@Param("vehicleId") String vehicleId);

    List<AlarmParameterSettingForm> getVedioAlarmsByVid(@Param("vehicleId") String vehicleId);

    /**
     * 获取视频参数设置通过车辆id集合(仅用于报警参数设置)
     * @param vehicleIds 监控对象id
     * @return List<AlarmParameterSettingForm>
     */
    List<AlarmParameterSettingForm> getVideoAlarmsByVehicleIds(@Param("vehicleIds") Collection<String> vehicleIds);

    /**
     * 根据车辆id查询音视频录像参数
     */
    Map<String, Object> getVedioRecordingByVehicleId(@Param("vehicleId") String vehicleId);

    /**
     * 根据车辆id查询音视频录像参数
     */
    RecordingSetting getVedioRecordingSettingByVehicleId(@Param("vehicleId") String vehicleId);

    /**
     * 根据报警类型和报警名称查询报警参数id的集合
     * @param name
     * @return
     */
    List<String> getIdsByAlarmTypeAndName(@Param("name") String name);

    List<AlarmParameterSettingForm> getAlarmTypesByName(@Param("pos") String pos);

    /**
     * 查询报警参数
     * @param posCollection pos
     * @return List<AlarmParameterSettingForm>
     */
    List<AlarmParameterSettingForm> getAlarmParametersByPosList(
        @Param("posCollection") Collection<String> posCollection);

    /**
     * 删除该车辆id下所有的视频录像参数设置
     * @param vehicleId
     */
    void deleteByVehicleId(@Param("vehicleId") String vehicleId);

    void deleteByMonitorIds(@Param("monitorIds") Set<String> monitorIds);

    /**
     * 删除该车辆id下所有的报警参数设置
     * @param vehicleId
     */
    void deleteVedioAlarmParamByVehicleId(@Param("vehicleId") String vehicleId);

    /**
     * 批量添加报警参数设置
     * @param alarmParamSettings
     */
    void addVedioAlarmParamSettings(List<Map<String, Object>> alarmParamSettings);

    /**
     * 批量添加报警参数设置
     * @param alarmParamSettings
     */
    boolean addVedioAlarmParams(List<AlarmParameterSettingForm> alarmParamSettings);

    /**
     * 添加音视频录像参数
     * @param record
     */
    void addVedioRecordingParamSettings(VideoAlarmParam record);

    /**
     * 添加音视频录像参数
     * @param record
     */
    void insertVedioRecordingParamSettings(RecordingSetting recordingSetting);

    /**
     * 根据id修改录像参数设置
     * @param record
     * @return
     */
    void updateVedioRecordingById(RecordingSetting record);

    /**
     * 删除监控对象下所有的报警参数设置
     * @param monitorIds monitorIds
     */
    void deleteVideoAlarmParamByMonitorIds(@Param("monitorIds") Set<String> monitorIds);
}