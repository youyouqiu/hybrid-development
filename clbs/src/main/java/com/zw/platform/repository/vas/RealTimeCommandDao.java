package com.zw.platform.repository.vas;

import com.zw.platform.domain.param.CameraParam;
import com.zw.platform.domain.param.CommunicationParam;
import com.zw.platform.domain.param.DeviceConnectServerParam;
import com.zw.platform.domain.param.DeviceParam;
import com.zw.platform.domain.param.EventSetParam;
import com.zw.platform.domain.param.GNSSParam;
import com.zw.platform.domain.param.InformationParam;
import com.zw.platform.domain.param.PhoneBookParam;
import com.zw.platform.domain.param.PhoneParam;
import com.zw.platform.domain.param.PositionParam;
import com.zw.platform.domain.param.SerialPortParam;
import com.zw.platform.domain.param.StationParam;
import com.zw.platform.domain.param.WirelessUpdateParam;
import com.zw.platform.domain.vas.monitoring.MonitorCommandBindForm;
import com.zw.platform.domain.vas.monitoring.query.RealTimeCommandQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


public interface RealTimeCommandDao {
    boolean addCommunicationParam(CommunicationParam commParam);

    boolean addCameraParam(CameraParam cameraParam);

    boolean addDeviceParam(DeviceParam deviceParam);

    boolean addGNSSParam(GNSSParam gnssParam);

    boolean addPhoneParam(PhoneParam phoneParam);

    boolean addPositionParam(PositionParam positionParam);

    boolean addWirelessUpdateParam(WirelessUpdateParam wirelessParam);

    boolean addDeviceConnectServerParam(DeviceConnectServerParam connectParam);

    boolean addInformationParam(InformationParam infoParam);

    boolean addEventSetParam(EventSetParam eventParam);

    boolean addPhoneBookParam(PhoneBookParam phoneBookParam);

    boolean addCommandBind(List<MonitorCommandBindForm> commandBind);

    /**
     * 新增 F3超待设备基站参数设置
     */
    boolean addStationParam(StationParam stationParam);

    /**
     * 获取 F3超待设备基站参数
     */
    StationParam getStationParam(@Param("vehicleId") String vehicleId, @Param("commandType") String commandType);

    List<SerialPortParam> getSerialPortParam(@Param("id") List<String> id);

    StationParam findStationParam(@Param("id") String id);

    List<Map<String, Object>> findRealTimeCommand(RealTimeCommandQuery query,
        @Param("vehicleList") List<String> vehicleList);

    boolean deleteParamSetting(@Param("vehicleId") String vehicleId, @Param("commandType") int commandType);

    CommunicationParam findCommunicationParam(@Param("id") String id);

    CommunicationParam findCommunicationByParamId(@Param("id") String id);

    DeviceParam findDeviceParam(@Param("id") String id);

    GNSSParam findGNSSParam(@Param("id") String id);

    PhoneParam findPhoneParam(@Param("id") String id);

    PositionParam findPositionParam(@Param("id") String id);

    CameraParam findCameraParam(@Param("id") String id);

    WirelessUpdateParam findWirelessUpdateParam(@Param("id") String id);

    DeviceConnectServerParam findDeviceConnectServerParam(@Param("id") String id);

    List<EventSetParam> findEventParam(@Param("paramId") List<String> id);

    List<InformationParam> findInformationParam(@Param("paramId") List<String> id);

    List<PhoneBookParam> findPhoneBookParam(@Param("paramId") List<String> id);

    boolean deleteCommandBind(MonitorCommandBindForm commandBind);

    boolean deleteCommandBindByBatch(String[] item);

    CommunicationParam getCommunicationParam(@Param("id") String id, @Param("commandType") String commandType);

    DeviceParam getDeviceParam(@Param("id") String id, @Param("commandType") String commandType);

    GNSSParam getGNSSParam(@Param("id") String id, @Param("commandType") String commandType);

    PhoneParam getPhoneParam(@Param("id") String id, @Param("commandType") String commandType);

    PositionParam getPositionParam(@Param("id") String id, @Param("commandType") String commandType);

    CameraParam getCameraParam(@Param("id") String id, @Param("commandType") String commandType);

    WirelessUpdateParam getWirelessUpdateParam(@Param("id") String id, @Param("commandType") String commandType);

    DeviceConnectServerParam getDeviceConnectServerParam(@Param("id") String id,
        @Param("commandType") String commandType);

    MonitorCommandBindForm findBind(@Param("id") String id, @Param("commandType") String commandType);

    List<MonitorCommandBindForm> findReferVehicle(@Param("commandType") String commandType,
        @Param("deviceType") Integer deviceType, @Param("vid") String vid);

    List<MonitorCommandBindForm> findReferVehicleExcept(@Param("commandType") String commandType,
        @Param("vid") String vid);

    /**
     * 获得车辆的指令类型对应的参数id
     */
    List<MonitorCommandBindForm> getMonitorParamId(@Param("monitorIdList") List<String> monitorIdList,
        @Param("commandType") Integer commandType, @Param("upgradeType") String upgradeType);

    /**
     * 获得实时指令绑定信息
     */
    MonitorCommandBindForm getCommandBindInfoById(@Param("id") String id);

    /**
     * 查询通讯参数
     */
    List<CommunicationParam> findCommunicationParamByParamIds(@Param("paramIdList") List<String> paramIdList);

    /**
     * 查询终端参数
     */
    List<DeviceParam> findDeviceParamByParamIds(@Param("paramIdList") List<String> paramIdList);

    /**
     * 查询控制终端连接指定服务器参数
     */
    List<WirelessUpdateParam> findWirelessUpgradeParamByParamIds(@Param("paramIdList") List<String> paramIdList);

    /**
     * 查询控制终端连接指定服务器参数
     */
    List<DeviceConnectServerParam> findDeviceConnectServerParamByParamIds(
        @Param("paramIdList") List<String> paramIdList);

    /**
     * 查询位置汇报参数
     */
    List<PositionParam> findPositionParamByParamIds(@Param("paramIdList") List<String> paramIdList);

    /**
     * 查询电话参数
     */
    List<PhoneParam> findPhoneParamByParamIds(@Param("paramIdList") List<String> paramIdList);

    /**
     * 查询视频拍照参数
     */
    List<CameraParam> findCameraParamByParamIds(@Param("paramIdList") List<String> paramIdList);

    /**
     * 查询GNSS参数
     */
    List<GNSSParam> findGNSSParamByParamIds(@Param("paramIdList") List<String> paramIdList);

    /**
     * 查询事件设置参数
     */
    List<EventSetParam> findEventParamByParamIds(@Param("paramIdList") List<String> paramIdList);

    /**
     * 查询电话本设置参数
     */
    List<PhoneBookParam> findPhoneBookParamByParamIds(@Param("paramIdList") List<String> paramIdList);

    /**
     * 查询信息点播菜单参数
     */
    List<InformationParam> findInformationParamByParamIds(@Param("paramIdList") List<String> paramIdList);

    /**
     * 查询基站参数设置
     */
    List<StationParam> findStationParamByParamIds(@Param("paramIdList") List<String> paramIdList);

    /**
     * 查询串口参数
     */
    List<SerialPortParam> findSerialPortParamByParamIds(@Param("paramIdList") List<String> paramIdList);

    /**
     * 批量新增通讯参数
     */
    boolean addCommunicationParamByBatch(@Param("list") List<CommunicationParam> commParamList);

    /**
     * 批量删除通讯参数
     */
    boolean delCommunicationParamByIds(@Param("list") List<String> ids);

    /**
     * 批量新增终端参数
     */
    boolean addDeviceParamByBatch(@Param("list") List<DeviceParam> deviceParamList);

    /**
     * 批量删除终端参数
     */
    boolean delDeviceParamByIds(@Param("list") List<String> ids);

    boolean addWirelessUpdateParamByBatch(@Param("list") List<WirelessUpdateParam> wirelessUpdateParamList);

    boolean delWirelessUpdateParamByIds(@Param("list") List<String> ids);

    boolean addDeviceConnectServerParamByBatch(
        @Param("list") List<DeviceConnectServerParam> deviceConnectServerParamList);

    boolean delDeviceConnectServerParamByIds(@Param("list") List<String> ids);

    boolean addPositionParamByBatch(@Param("list") List<PositionParam> positionParamList);

    boolean delPositionParamByIds(@Param("list") List<String> ids);

    boolean addPhoneParamByBatch(@Param("list") List<PhoneParam> phoneParamList);

    boolean delPhoneParamByIds(@Param("list") List<String> ids);

    boolean addCameraParamByBatch(@Param("list") List<CameraParam> cameraParamList);

    boolean delCameraParamByIds(@Param("list") List<String> ids);

    boolean addGNSSParamByBatch(@Param("list") List<GNSSParam> gnssParamList);

    boolean delGNSSParamByIds(@Param("list") List<String> ids);

    boolean addEventSetParamByBatch(@Param("list") List<EventSetParam> eventSetParamList);

    boolean delEventSetParamByIds(@Param("list") List<String> ids);

    boolean addPhoneBookParamByBatch(@Param("list") List<PhoneBookParam> phoneBookParamList);

    boolean delPhoneBookParamByIds(@Param("list") List<String> ids);

    boolean addInformationParamByBatch(@Param("list") List<InformationParam> informationParamList);

    boolean delInformationParamByIds(@Param("list") List<String> ids);

    boolean addSerialPortParamByBatch(@Param("list") List<SerialPortParam> serialPortParamList);

    boolean delSerialPortParamByIds(@Param("list") List<String> ids);

    boolean addStationParamByBatch(@Param("list") List<StationParam> stationParamList);

    boolean delStationParamByIds(@Param("list") List<String> ids);

    /**
     * 批量解除绑定关系
     */
    boolean delCommandBindByIds(@Param("list") List<String> ids);

    /**
     * 根据车辆iD和类型获取数据
     */
    MonitorCommandBindForm getRealTimeCommand(@Param("vehicleId") String vehicleId,
        @Param("commandType") Integer commandType);

    /**
     * 获取信息点播数据
     */
    InformationParam getInformationParamsByParamId(@Param("paramIds") List<String> paramIds,
        @Param("infoType") Integer infoType);

    List findDeviceUpgradeDaoByIds(@Param("paramIdList")List<String> paramIdList);
}
