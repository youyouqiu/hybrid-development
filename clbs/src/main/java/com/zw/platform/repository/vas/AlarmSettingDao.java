package com.zw.platform.repository.vas;

import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.vas.alram.AlarmLinkageParam;
import com.zw.platform.domain.vas.alram.AlarmParameter;
import com.zw.platform.domain.vas.alram.AlarmParameterSetting;
import com.zw.platform.domain.vas.alram.AlarmSetting;
import com.zw.platform.domain.vas.alram.AlarmType;
import com.zw.platform.domain.vas.alram.MsgParamDTO;
import com.zw.platform.domain.vas.alram.Msgparam;
import com.zw.platform.domain.vas.alram.OutputControl;
import com.zw.platform.domain.vas.alram.OutputControlDTO;
import com.zw.platform.domain.vas.alram.PhotoDTO;
import com.zw.platform.domain.vas.alram.PhotoParam;
import com.zw.platform.domain.vas.alram.SpecialAlarmDO;
import com.zw.platform.domain.vas.alram.form.AlarmParameterSettingForm;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 报警参数设置Dao
 * @author wangying
 * @since 2016年12月6日 下午5:07:05
 */
public interface AlarmSettingDao {

    /**
     * 查询报警参数设置了的车辆
     */
    List<AlarmSetting> findVehicleAlarmSetting(@Param("userId") String userId,
        @Param("groupList") List<String> groupList);

    /**
     * 根据车辆id查询
     */
    List<AlarmSetting> findByVehicleId(@Param("vehicleId") String vehicleId);

    /**
     * 查询报警参数设置
     * @param moIds 监控对象id
     * @return List<AlarmSetting>
     */
    List<AlarmSetting> findByMoIds(@Param("moIds") Collection<String> moIds);

    /**
     * 根据对象id查询
     */
    List<AlarmSetting> findById(@Param("vehicleId") String vehicleId);

    /**
     * 选择指定车辆中有指定报警参数的id(仅用于报警参数设置)
     */
    Set<String> findByVehicleIds(List<String> vehicleIds);

    /**
     * 根据车辆id集合查询报警参数id和监控对象类型(仅用于报警参数设置)
     */
    List<AlarmSetting> findAlarmSettingByBatch(@Param("vehicleIds") List<String> vehicleIds);

    /**
     * 根据对象id查询报警设置
     */
    @MapKey("alarmParameterId")
    Map<String, AlarmSetting> findMapById(@Param("vehicleId") String vehicleId);

    /**
     * 查询所有参数设置
     */
    List<AlarmSetting> findAllAlarmParameter();

    /**
     * 根据车辆查询参数设置
     */
    List<AlarmParameterSettingForm> findSettingByVid(String vehicleId);

    /**
     * 根据车辆id集合查询报警参数设置集合
     */
    List<AlarmParameterSetting> findSettingsByVehicleIds(Map<String, Object> params);

    /**
     * 批量删除
     */
    boolean deleteAlarmSettingByBatch(List<String> ids);

    /**
     * 根据车辆id批量删除
     */
    boolean deleteByVehicleIds(List<String> vehicleIds);

    /**
     * 批量新增报警参数设置
     */
    boolean addAlarmSettingByBatch(List<AlarmParameterSetting> list);

    /**
     * 查询相同报警类型的参数设置id
     */
    List<String> findSameAlarmTypeSetting(String id);

    /**
     * 根据车辆id查询参考车牌报警参数
     */
    List<AlarmParameterSettingForm> findParameterByVehicleId(String vehicleId);

    /**
     * 根据车辆id查询io报警参数
     * @param vehicleId 车辆id
     * @return List<AlarmParameterSettingForm>
     */
    List<AlarmParameterSettingForm> findIoParameterByVehicleId(String vehicleId);

    /**
     * 根据车辆id查询其打开开关的报警类型
     */
    List<AlarmType> findAlarmTypeByVid(String vehicleId);

    /**
     * 根据车辆id删除车辆与报警参数设置的绑定关系
     */
    boolean deleteAlarmSettingByVehicleId(String vehicleId);

    List<AlarmSetting> findAlarmByVidAndType(@Param("vehicleId") String vehicleId,
        @Param("alarmTypeId") List<String> alarmTypeId);

    List<AlarmSetting> saveAlarmParamter(String vehicleId);

    boolean updateAlarmSettings(AlarmParameterSettingForm alarmForm);

    boolean addAlarmSetting(AlarmParameterSettingForm alarmForm);

    VehicleInfo findPeopleById(String id);

    VehicleInfo findPeopleOrVehicleOrThingById(String id);

    List<String> findAlarmSettingByVid(@Param("vehicleList") List<String> vehicleList);

    PhotoParam findPhotoSetting(String id);

    List<AlarmLinkageParam> findLinkageSettingList(String vehicleId);

    /**
     * 批量查拍照和录像设置
     * @param ids ids
     * @return list
     */
    List<PhotoDTO> listPhotoSetting(@Param("ids") Collection<String> ids);

    List<VehicleInfo> findAlarmLinkageReferenceVehicles(@Param("userId") String uuid,
        @Param("vehicleIds") List<String> vehicleIds, @Param("deviceTypes") List<Integer> deviceTypes);

    boolean addPhotoSettingByBatch(List<PhotoParam> list);

    boolean deleteLinkageSettingByBatch(List<String> list);

    boolean deletePhotoSettingByBatch(List<String> list);

    boolean deleteOutputControlSettingByBatch(List<String> list);

    /**
     * 根据车辆ID查询报警参数设置
     */
    List<AlarmSetting> findAlarmParameterByIoMonitorId(@Param("ioMonitorId") String ioMonitorId);

    /**
     * 查找短信下发参数
     */
    Msgparam findMsgSetting(String id);

    /**
     * 短信参数批量增加
     */
    boolean addMsgSettingByBatch(List<Msgparam> linkageMsg);

    /**
     * 输出控制参数批量增加
     */
    boolean addOutputControlSettingByBatch(List<OutputControl> outputControls);

    /**
     * 查询输出控制参数
     */
    OutputControl findOutputControlSetting(String id);

    /**
     * 输出控制参数批量增加
     */
    boolean addOutputControlSetting(OutputControl outputControl);

    /**
     * 获取车辆当前输出控制
     */
    List<OutputControl> getVehicleOutputControlSetting(@Param("vehicleId") String vehicleId);

    /**
     * 修改车辆输出控制标记
     */
    boolean updateVehicleOutputControlSetting(@Param("vehicleId") String vehicleId);

    /**
     * 根据监控对象id查询io报警参数设置
     */
    List<Map<String, Object>> findIoAlarmValueByVehicleId(String vehicleId);

    /**
     * 获取限速值
     */
    Integer getSpeedLimitByVehicleId(String vehicleId);

    /**
     * 批量获取车辆获取限速值
     */
    List<Map<String, Object>> getSpeedLimitByVehicleIds(@Param("monitorIds") Collection<String> monitorIds);

    /**
     * 根据协议类型查询绑定数据（除开高精度）
     */
    List<AlarmSetting> findAllParameterByProtocolType(String deviceType);

    /**
     * 根据协议类型查询绑定数据
     */
    List<AlarmSetting> findAllAlarmParameterByProtocolType(String deviceType);

    /**
     * 根据协议类型查询绑定数据（只包含高精度）
     */
    List<AlarmSetting> findHighPrecisionAlarmByProtocolType(String protocolType);

    List<AlarmLinkageParam> findLinkageSettingListByMonitorIds(@Param("monitorIds") Set<String> monitorIds);

    /**
     * 批量删除报警参数设置
     */
    boolean deleteBatchAlarmSettingByVehicleId(@Param("monitorIds") List<String> monitorIds);

    boolean deleteRoadAlarmSpeedLimit();

    /**
     * 通过车辆id查询报警类型id、
     * @param vehicleId
     * @return
     */
    Set<String> findAlarmTypeIdByVehicleId(String vehicleId);

    boolean deleteMsgSettingByBatch(@Param("msgIds") Collection<String> msgIds);

    /**
     * 批量新增联动策略
     * @param specialAlarmList specialAlarmList
     * @return boolean
     */
    boolean addSpecialAlarmDOByBatch(List<SpecialAlarmDO> specialAlarmList);

    /**
     * 查询联动策略-下发短信设置
     * @param ids ids
     * @return list
     */
    List<MsgParamDTO> listMsgSetting(@Param("ids") Collection<String> ids);

    /**
     * 报警联动控制输出
     * @param ids ids
     * @return list
     */
    List<OutputControlDTO> listOutputControlSetting(@Param("ids") Collection<String> ids);

    /**
     * 获取报警参数
     * @param types 类型
     * @return List<AlarmParameter>
     */
    List<AlarmParameter> getAlarmTypeParameterByTypes(@Param("types") Collection<String> types);

    /**
     * 获取Io报警参数
     * @param moId 监控对象id
     * @return List<AlarmParameter>
     */
    List<AlarmParameter> getIoAlarmTypeParameterByMoId(String moId);

    /**
     * 根据协议类型和报警所属类型查询默认参数
     * @param deviceType 协议类型
     * @param types      报警所属类型
     * @return List<AlarmParameterSettingForm>
     */
    List<AlarmParameterSettingForm> getDefaultAlarmParameterSetting(@Param("deviceType") String deviceType,
        @Param("types") Collection<String> types);

    /**
     * 根据id获取报警参数
     * @param alarmParameterIds alarmParameterIds
     * @return List<AlarmParameter>
     */
    List<AlarmParameter> getAlarmParameterByAlarmParameterIds(
        @Param("alarmParameterIds") Collection<String> alarmParameterIds);
}
