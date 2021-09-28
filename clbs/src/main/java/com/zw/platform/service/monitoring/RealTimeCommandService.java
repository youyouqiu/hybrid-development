package com.zw.platform.service.monitoring;


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


/**
 * 实时指令 <p>Title: RealTimeVideoService.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p> <p>team:
 * ZhongWeiTeam</p>
 * @author:FanLu
 * @date 2017年4月12日下午14:54:30
 * @version 1.0
 */
public interface RealTimeCommandService {
    boolean addCommunicationParam(CommunicationParam commParam) throws Exception;

    boolean addCameraParam(CameraParam cameraParam) throws Exception;

    boolean addDeviceParam(DeviceParam deviceParam) throws Exception;

    boolean addGNSSParam(GNSSParam gnssParam) throws Exception;

    boolean addPhoneParam(PhoneParam phoneParam) throws Exception;

    boolean addPositionParam(PositionParam positionParam) throws Exception;

    boolean addCommandBind(List<MonitorCommandBindForm> commandBind) throws Exception;

    boolean addWirelessUpdateParam(WirelessUpdateParam wirelessParam) throws Exception;

    boolean addDeviceConnectServerParam(DeviceConnectServerParam connectParam) throws Exception;

    boolean addInformationParam(InformationParam infoParam) throws Exception;

    boolean addEventSetParam(EventSetParam eventParam) throws Exception;

    boolean addPhoneBookParam(PhoneBookParam phoneBookParam) throws Exception;

    /**
     * 新增 F3超待设备基站参数设置
     * @param stationParam
     * @return
     */
    boolean addStationParam(StationParam stationParam) throws Exception;

    /**
     * 获取 F3超待设备基站参数
     * @param vehicleId
     * @param commandType
     * @return
     */
    StationParam getStationParam(String vehicleId, String commandType) throws Exception;

    List<SerialPortParam> getSerialPortParam(List<String> id) throws Exception;

    StationParam findStationParam(String id) throws Exception;

    List<Map<String, Object>> findRealTimeCommand(RealTimeCommandQuery query, List<String> vehicleList, boolean doPage)
        throws Exception;

    boolean deleteParamSetting(String vehicleId, int commandType) throws Exception;

    CommunicationParam findCommunicationParam(String id) throws Exception;

    CommunicationParam findCommunicationByParamId(@Param("id") String id) throws Exception;

    DeviceParam findDeviceParam(String id) throws Exception;

    GNSSParam findGNSSParam(String id) throws Exception;

    PhoneParam findPhoneParam(String id) throws Exception;

    PositionParam findPositionParam(String id) throws Exception;

    CameraParam findCameraParam(String id) throws Exception;

    WirelessUpdateParam findWirelessUpdateParam(String id) throws Exception;

    DeviceConnectServerParam findDeviceConnectServerParam(String id) throws Exception;

    List<EventSetParam> findEventParam(List<String> id) throws Exception;

    List<InformationParam> findInformationParam(List<String> id) throws Exception;

    List<PhoneBookParam> findPhoneBookParam(List<String> id) throws Exception;

    boolean deleteCommandBind(MonitorCommandBindForm commandBind) throws Exception;

    boolean deleteCommandBindByBatch(String[] item) throws Exception;

    CommunicationParam getCommunicationParam(String id, String commandType) throws Exception;

    DeviceParam getDeviceParam(String id, String commandType) throws Exception;

    GNSSParam getGNSSParam(String id, String commandType) throws Exception;

    PhoneParam getPhoneParam(String id, String commandType) throws Exception;

    CameraParam getCameraParam(String id, String commandType) throws Exception;

    PositionParam getPositionParam(String id, String commandType) throws Exception;

    WirelessUpdateParam getWirelessUpdateParam(String id, String commandType) throws Exception;

    DeviceConnectServerParam getDeviceConnectServerParam(String id, String commandType) throws Exception;

    MonitorCommandBindForm findBind(String id, String commandType) throws Exception;

    List<MonitorCommandBindForm> findReferVehicle(String commandType) throws Exception;

    List<MonitorCommandBindForm> findReferVehicleExcept(String commandType, String vid) throws Exception;
}
