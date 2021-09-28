package com.zw.platform.service.sendTxt;

import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.domain.param.CameraParam;
import com.zw.platform.domain.param.CommunicationParam;
import com.zw.platform.domain.param.DeviceParam;
import com.zw.platform.domain.param.EventSetParam;
import com.zw.platform.domain.param.F3SensorParamQuery;
import com.zw.platform.domain.param.GNSSParam;
import com.zw.platform.domain.param.InformationParam;
import com.zw.platform.domain.param.PhoneBookParam;
import com.zw.platform.domain.param.PhoneParam;
import com.zw.platform.domain.param.PositionParam;
import com.zw.platform.domain.param.SerialPortParam;
import com.zw.platform.domain.param.SpeedLimitParam;
import com.zw.platform.domain.param.StationParam;
import com.zw.platform.domain.param.T808Param;
import com.zw.platform.domain.sendTxt.AlarmAck;
import com.zw.platform.domain.sendTxt.DeviceCommand;
import com.zw.platform.domain.sendTxt.F3CommunicationParam;
import com.zw.platform.domain.sendTxt.InformationService;
import com.zw.platform.domain.sendTxt.OBDParam;
import com.zw.platform.domain.sendTxt.OilElectricControl;
import com.zw.platform.domain.sendTxt.OriginalOrder;
import com.zw.platform.domain.sendTxt.RecordCollection;
import com.zw.platform.domain.sendTxt.RecordSend;
import com.zw.platform.domain.sendTxt.SendQuestion;
import com.zw.platform.domain.sendTxt.SendTxt;
import com.zw.platform.domain.sendTxt.SensorParam;
import com.zw.platform.domain.sendTxt.VehicleCommand;
import com.zw.platform.domain.systems.DeviceUpgrade;
import com.zw.platform.push.cache.SendTarget;
import com.zw.ws.entity.device.DeviceWakeUpEntity;
import com.zw.ws.entity.t808.parameter.ParamItem;

import java.util.List;

/**
 * Created by LiaoYuecai on 2017/3/31.
 */
public interface SendTxtService {

    /**
     * 位置信息查询
     * @param deviceId    设备id
     * @param mobile      sim卡号
     * @param msgSN       流水号
     * @param vehicleInfo
     * @throws Exception
     */
    void deviceLocationQuery(String deviceId, String mobile, Integer msgSN, BindDTO vehicleInfo) throws Exception;

    /**
     * 发送文本信息
     * @param deviceId      设备id
     * @param simcardNumber sim卡号
     * @param txt           文本信息
     * @param msgSN         流水号
     * @param deviceType    设备协议类型
     * @throws Exception
     */
    void sendTxt(String deviceId, String simcardNumber, SendTxt txt, Integer msgSN, String deviceType) throws Exception;

    /**
     * 发送文本信息，只发送信息，不订阅
     * @param deviceId      设备id
     * @param simcardNumber sim卡号
     * @param txt           文本信息
     * @param msgSN         流水号
     * @param deviceType    设备协议类型
     * @throws Exception
     */
    void sendTxtOnly(String deviceId, String simcardNumber, SendTxt txt, Integer msgSN, String deviceType)
        throws Exception;

    /**
     * 发送文本信息并订阅应答
     * @param configInfo 信息配置redis缓存对象
     * @param txt
     * @param msgSN      流水号
     * @throws Exception
     */
    void sendTextAndSubscribeAnswer(BindDTO configInfo, SendTxt txt, Integer msgSN);

    /**
     * 下发终端唤醒
     * @param deviceWakeUpEntity deviceWakeUpEntity
     * @param sessionId          sessionId
     */
    void sendDeviceWakeUp(DeviceWakeUpEntity deviceWakeUpEntity, String sessionId);

    /**
     * 提问下发
     * @param
     * @param question
     * @param bindDTO
     */
    void sendQuestion(String deviceId, String simcardNumber, SendQuestion question, Integer msgSN, BindDTO bindDTO)
        throws Exception;

    void vehicleCommand(String deviceId, String simcardNumber, VehicleCommand vehicleCommand, Integer msgSN,
        String deviceType) throws Exception;

    /**
     * 人工报警确认
     * @param deviceId   终端id
     * @param mobile     sim卡号
     * @param alarmAck   参数
     * @param msgSN      流水号
     * @param deviceType 终端类型
     * @throws Exception Exception
     */
    void alarmAck(String deviceId, String mobile, AlarmAck alarmAck, Integer msgSN, String deviceType) throws Exception;

    /**
     * 终端控制
     * @param simCardNumber sim卡号
     * @param deviceCommand 参数
     * @param msgSno        流水号
     * @param deviceId      终端id
     * @param deviceType    终端类型
     * @param sendTarget    sendTarget
     */
    void deviceCommand(String simCardNumber, DeviceCommand deviceCommand, Integer msgSno, String deviceId,
        String deviceType, SendTarget sendTarget);

    /**
     * 终端参数查询（实时指令模块，只查询固定参数）
     * @param deviceId      终端id
     * @param simCardNumber sim卡号
     * @param msgSno        流水号
     * @param deviceType    终端类型
     */
    void devicePropertyQuery(String deviceId, String simCardNumber, Integer msgSno, String deviceType);

    /**
     * 设置通讯参数
     * @param deviceId      终端id
     * @param simCardNumber sim卡号
     * @param param         参数
     * @param msgSno        流水号
     * @param deviceType    终端类型
     * @param sendTarget    sendTarget
     */
    void setCommunicationParam(String deviceId, String simCardNumber, CommunicationParam param, Integer msgSno,
        String deviceType, SendTarget sendTarget);

    /**
     * 设置终端参数
     * @param deviceId      终端id
     * @param simCardNumber sim卡号
     * @param param         参数
     * @param msgSno        流水号
     * @param deviceType    终端类型
     * @param sendTarget    sendTarget
     */
    void setDeviceParam(String deviceId, String simCardNumber, DeviceParam param, Integer msgSno, String deviceType,
        SendTarget sendTarget);

    /**
     * 设置位置汇报参数
     * @param deviceId      终端id
     * @param simCardNumber sim卡号
     * @param param         参数
     * @param msgSno        流水号
     * @param deviceType    终端类型
     * @param sendTarget    sendTarget
     */
    void setPositionParam(String deviceId, String simCardNumber, PositionParam param, Integer msgSno, String deviceType,
        SendTarget sendTarget);

    /**
     * 设置电话参数
     * @param deviceId      终端id
     * @param simCardNumber sim卡号
     * @param param         参数
     * @param msgSno        流水号
     * @param deviceType    终端类型
     * @param sendTarget    sendTarget
     */
    void setPhoneParam(String deviceId, String simCardNumber, PhoneParam param, Integer msgSno, String deviceType,
        SendTarget sendTarget);

    /**
     * 设置通用参数(8103)
     * @param mobile
     * @param params
     */
    public void setT808Param(String mobile, List<T808Param> params, Integer msgSN) throws Exception;

    /**
     * 设置视频拍照参数
     * @param deviceId      终端id
     * @param simCardNumber sim卡号
     * @param param         参数
     * @param msgSno        流水号
     * @param deviceType    终端类型
     * @param sendTarget    sendTarget
     */
    void setCameraParam(String deviceId, String simCardNumber, CameraParam param, Integer msgSno, String deviceType,
        SendTarget sendTarget);

    /**
     * 设置GNSS参数
     * @param deviceId      终端id
     * @param simCardNumber sim卡号
     * @param param         参数
     * @param msgSno        流水号
     * @param deviceType    终端类型
     * @param sendTarget    sendTarget
     */
    void setGNSSParam(String deviceId, String simCardNumber, GNSSParam param, Integer msgSno, String deviceType,
        SendTarget sendTarget);

    void setSpeedMax(String deviceId, String mobile, SpeedLimitParam param, Integer msgSN, String deviceType)
        throws Exception;

    void recordCollection(String deviceId, String simcardNumber, RecordCollection param, Integer msgSN,
        String deviceType) throws Exception;

    /**
     * 行驶记录参数下传
     * @param deviceId
     * @param simcardNumber
     * @param param
     * @param msgSN
     * @param deviceType
     * @throws Exception
     */
    void recordSend(String deviceId, String simcardNumber, RecordSend param, Integer msgSN, String deviceType)
        throws Exception;

    /**
     * 事件设置
     * @param deviceId      终端id
     * @param simCardNumber sim卡号
     * @param param         参数
     * @param msgSno        流水号
     * @param deviceType    终端类型
     * @param sendTarget    sendTarget
     */
    void setEvent(String deviceId, String simCardNumber, List<EventSetParam> param, Integer msgSno, String deviceType,
        SendTarget sendTarget);

    /**
     * 信息点播菜单设置
     * @param deviceId      终端id
     * @param simCardNumber sim卡号
     * @param param         参数
     * @param msgSno        流水号
     * @param deviceType    终端类型
     * @param sendTarget    sendTarget
     */
    void setInformationDemand(String deviceId, String simCardNumber, List<InformationParam> param, Integer msgSno,
        String deviceType, SendTarget sendTarget);

    /**
     * 设置电话本
     * @param deviceId      终端id
     * @param simCardNumber sim卡号
     * @param param         参数
     * @param msgSno        流水号
     * @param deviceType    终端类型
     * @param sendTarget    sendTarget
     */
    void setPhoneBook(String deviceId, String simCardNumber, List<PhoneBookParam> param, Integer msgSno,
        String deviceType, SendTarget sendTarget);

    /**
     * 发送原始命令
     * @param deviceId      终端id
     * @param simCardNumber sim卡号
     * @param originalOrder 参数
     * @param msgSno        流水号
     * @param deviceType    终端类型
     */
    void originalOrder(String deviceId, String simCardNumber, OriginalOrder originalOrder, Integer msgSno,
        String deviceType);

    /**
     * 终端参数
     * @param deviceId      终端id
     * @param simCardNumber sim卡号
     * @param msgSno        流水号
     * @param deviceType    终端类型
     */
    void terminalParameters(String deviceId, String simCardNumber, Integer msgSno, String deviceType);

    /**
     * 传感器轮询设置下发
     * @param mobile       sim卡号
     * @param msgSno       流水号
     * @param sensorParams 参数
     * @param deviceId     终端id
     * @param deviceType   终端类型
     */
    void setSensorPolling(String mobile, Integer msgSno, List<SensorParam> sensorParams, String deviceId,
        String deviceType);

    void informationService(String deviceId, String mobile, InformationService informationService, Integer msgSN,
        String deviceType) throws Exception;

    /**
     * F3传感器参数查询扩展
     * @param mobile     sim卡号
     * @param queries    参数
     * @param msgSno     流水号
     * @param deviceType 终端类型
     */
    void queryF3SensorParam(String mobile, List<F3SensorParamQuery> queries, Integer msgSno, String deviceId,
        String deviceType) throws Exception;

    /**
     * 设置F3通讯参数
     * @param mobile
     * @param param
     * @param msgSN
     */
    void setF3CommunicationParam(String mobile, F3CommunicationParam param, Integer msgSN, String deviceId)
        throws Exception;

    /**
     * F3传感器私有参数查询
     * @param mobile
     * @param sensorID
     * @param commandStr
     * @param msgSN
     * @param monitorConfig
     */
    void getF3SensorPrivateParam(String mobile, Integer sensorID, String commandStr, Integer msgSN, String deviceId,
        BindDTO monitorConfig) throws Exception;

    /**
     * 下发8103   车人物
     * @param vehicleId     车id
     * @param parameterName 设置id
     * @param paramType     下发参数类型
     * @param isOvertime    是否进行超时 true 超时 false 不超过
     * @Description: 参数设置下发
     */
    String setF3SetParamByVehicleAndPeopleAndThing(String vehicleId, String parameterName, List<ParamItem> params,
        String paramType, boolean isOvertime, Integer flag) throws Exception;

    /**
     * 下发8500
     * @param control    断油电必填参数
     * @param msgSN
     * @param deviceType
     * @return
     */
    void oilElectric(String deviceId, String mobile, OilElectricControl control, Integer msgSN, String deviceType);

    void sendOBD(String mobile, Integer msgSN, OBDParam obdParam, String deviceId, String deviceType);

    /**
     * 设置基站参数设置
     * @param deviceId      终端id
     * @param simCardNumber sim卡号
     * @param param         参数
     * @param msgSno        流水号
     * @param deviceType    终端类型
     * @param sendTarget    sendTarget
     */
    void setStationParam(String deviceId, String simCardNumber, StationParam param, Integer msgSno, String deviceType,
        SendTarget sendTarget);

    /**
     * 设置串口参数设置
     * @param deviceId        终端id
     * @param simCardNumber   sim卡号
     * @param serialPortParam 参数
     * @param msgSno          流水号
     * @param id              外设ID 0xF901(RS232串口参数)   F902(RS485串口参数)  F903(CAN总线参数)
     * @param deviceType      终端类型
     * @param sendTarget      sendTarget
     */
    void setSerialPortParam(String deviceId, String simCardNumber, List<SerialPortParam> serialPortParam,
        Integer msgSno, Integer id, String deviceType, SendTarget sendTarget);

    /**
     * 终端升级
     * @param vehicleId     车辆id
     * @param deviceId      终端id
     * @param simCardNumber sim卡号
     * @param param         参数
     * @param msgSno        流水号
     * @param deviceType    终端类型
     */
    void setDeviceUpgrade(String vehicleId, String deviceId, String simCardNumber, List<DeviceUpgrade> param,
        Integer msgSno, String deviceType);

}
