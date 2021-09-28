package com.zw.platform.service.monitoring.impl;

import com.alibaba.fastjson.JSONObject;
import com.cb.platform.domain.VehicleScheduler;
import com.cb.platform.repository.mysqlDao.VehicleScheduleDao;
import com.zw.lkyw.domain.SendMsgBasicInfo;
import com.zw.lkyw.domain.SendMsgDetail;
import com.zw.lkyw.domain.SendMsgMonitorInfo;
import com.zw.lkyw.utils.sendMsgCache.SendMsgCache;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.basic.service.RoleService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.commons.SecurityPasswordHelper;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.form.OBDManagerSettingForm;
import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.customenum.RecordCollectionEnum;
import com.zw.platform.domain.multimedia.MultimediaRetrieval;
import com.zw.platform.domain.multimedia.MultimediaUpload;
import com.zw.platform.domain.multimedia.Photograph;
import com.zw.platform.domain.multimedia.Record;
import com.zw.platform.domain.multimedia.form.OrderForm;
import com.zw.platform.domain.param.PositionParam;
import com.zw.platform.domain.param.SpeedLimitParam;
import com.zw.platform.domain.param.TelBack;
import com.zw.platform.domain.sendTxt.Answer;
import com.zw.platform.domain.sendTxt.DeviceCommand;
import com.zw.platform.domain.sendTxt.InformationService;
import com.zw.platform.domain.sendTxt.OBDParam;
import com.zw.platform.domain.sendTxt.OilElectric;
import com.zw.platform.domain.sendTxt.OilElectricControl;
import com.zw.platform.domain.sendTxt.OriginalOrder;
import com.zw.platform.domain.sendTxt.RecordCollection;
import com.zw.platform.domain.sendTxt.RecordCollectionDataA;
import com.zw.platform.domain.sendTxt.RecordCollectionDataB;
import com.zw.platform.domain.sendTxt.RecordSend;
import com.zw.platform.domain.sendTxt.SendQuestion;
import com.zw.platform.domain.sendTxt.SendTxt;
import com.zw.platform.domain.sendTxt.VehicleCommand;
import com.zw.platform.domain.sendTxt.VehicleControllerInfo;
import com.zw.platform.domain.vas.alram.AlarmParameter;
import com.zw.platform.domain.vas.alram.AlarmSetting;
import com.zw.platform.domain.vas.alram.OutputControlSend;
import com.zw.platform.domain.vas.alram.OutputControlSendInfo;
import com.zw.platform.domain.vas.alram.form.AlarmParameterSettingForm;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.modules.OBDVehicleTypeDao;
import com.zw.platform.repository.vas.AlarmSearchDao;
import com.zw.platform.repository.vas.AlarmSettingDao;
import com.zw.platform.service.monitoring.OrderService;
import com.zw.platform.service.multimedia.MultimediaService;
import com.zw.platform.service.obdManager.OBDManagerSettingService;
import com.zw.platform.service.realTimeVideo.RealTimeVideoService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.sendTxt.SendTxtService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.Monitor8104Cache;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.msg.t808.T808MsgHead;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.protocol.util.ProtocolTypeUtil;
import com.zw.ws.entity.t808.parameter.ParamItem;
import com.zw.ws.entity.t808.parameter.T8080x8608;
import com.zw.ws.entity.t808.parameter.T808_0x8103;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author  Tdz
 * @create 2017-04-21 9:09
 **/
@Service
public class OrderServiceImpl implements OrderService, IpAddressService {
    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT1 = "yyyy-MM-dd HH:mm:ss";

    private static final String DATE_FORMAT2 = "YYMMddHHmmss";
    private static final String[] MONITOR_FIELD =
        { "id", "name", "deviceId", "simCardNumber", "deviceType", "deviceNumber" };
    @Autowired
    private MultimediaService multimediaService;

    @Autowired
    private UserService userService;

    @Autowired
    VehicleScheduleDao vehicleScheduleDao;

    @Autowired
    private SendTxtService sendTxtService;

    @Resource
    private AlarmSettingDao alarmSettingDao;

    @Resource
    private AlarmSearchDao alarmSearchDao;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private OBDVehicleTypeDao obdVehicleTypeDao;

    @Autowired
    private OBDManagerSettingService obdManagerSettingService;

    @Autowired
    private RealTimeVideoService realTimeVideoService;

    @Autowired
    private SendMsgCache sendMsgCache;

    @Autowired
    private Monitor8104Cache monitor8104Cache;

    @Autowired
    private RoleService roleService;

    @Autowired
    private LogSearchService logSearchService;

    @Override
    public boolean takePhoto(OrderForm form) {
        BindDTO bindDTO = MonitorUtils.getBindDTO(form.getVid(), MONITOR_FIELD);
        if (bindDTO != null) {
            String deviceId = bindDTO.getDeviceId();
            String mobile = bindDTO.getSimCardNumber();
            Photograph photograph = new Photograph();
            photograph.setChroma(form.getChroma());
            photograph.setCommand(form.getCommand());
            photograph.setContrast(form.getContrast());
            photograph.setDistinguishability(form.getDistinguishability());
            photograph.setLuminance(form.getLuminance());
            photograph.setQuality(form.getQuality());
            photograph.setSaturability(form.getSaturability());
            photograph.setWayID(form.getWayID());
            photograph.setSaveSign(form.getSaveSign());
            photograph.setTime(form.getTime());
            if (mobile != null) {
                multimediaService.photograph(deviceId, photograph, mobile, form.getSerialNumber(), bindDTO);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean getVideo(OrderForm form) {
        BindDTO vehicleInfo = MonitorUtils.getBindDTO(form.getVid(), MONITOR_FIELD);
        if (vehicleInfo != null) {
            String deviceId = vehicleInfo.getDeviceId();
            String mobile = vehicleInfo.getSimCardNumber();
            Photograph photograph = new Photograph();
            photograph.setChroma(form.getChroma());
            photograph.setCommand(0xFFFF);
            photograph.setContrast(form.getContrast());
            photograph.setDistinguishability(form.getDistinguishability());
            photograph.setLuminance(form.getLuminance());
            photograph.setQuality(form.getQuality());
            photograph.setSaturability(form.getSaturability());
            photograph.setWayID(form.getWayID());
            photograph.setSaveSign(form.getSaveSign());
            photograph.setTime(form.getTime());
            if (mobile != null) {
                multimediaService.photograph(deviceId, photograph, mobile, form.getSerialNumber(), vehicleInfo);
                return true;
            }
        }
        return false;
    }

    /**
     * 输出控制
     * @param outputControlSend
     */
    @Override
    public boolean outputControlBy13(OutputControlSend outputControlSend, boolean isAuto) {
        BindDTO bindDTO = MonitorUtils.getBindDTO(outputControlSend.getVid(), MONITOR_FIELD);
        if (bindDTO != null) {
            String deviceId = bindDTO.getDeviceId();
            String mobile = bindDTO.getSimCardNumber();
            String deviceNumber = bindDTO.getDeviceNumber();
            String deviceType = bindDTO.getDeviceType();
            if (deviceId != null && mobile != null && deviceNumber != null) {
                // 获取当前用户的用户名
                //是否是联动自动下发
                String userName;
                if (isAuto) {
                    userName = "admin";
                } else {
                    userName = SystemHelper.getCurrentUsername();
                }
                // 订阅推送消息
                Integer msgSN = DeviceHelper.getRegisterDevice(outputControlSend.getVid(), deviceNumber);
                if (null != msgSN) {
                    SubscibeInfo info =
                        new SubscibeInfo(userName, deviceId, msgSN, ConstantUtil.T808_VEHICLE_CONTROL_ACK);
                    SubscibeInfoCache.getInstance().putTable(info);
                    T808Message message = MsgUtil
                        .get808Message(mobile, ConstantUtil.T808_VEHICLE_CONTROLLER, msgSN, outputControlSend,
                            deviceType);
                    WebSubscribeManager.getInstance()
                        .sendMsgToAll(message, ConstantUtil.T808_VEHICLE_CONTROLLER, deviceId);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 输出控制
     * @param outputControlSendInfo
     */
    @Override
    public boolean outputControlBy19(OutputControlSendInfo outputControlSendInfo, String vehicleId, boolean isAuto) {
        BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId, MONITOR_FIELD);
        if (bindDTO != null) {
            String deviceId = bindDTO.getDeviceId();
            String mobile = bindDTO.getSimCardNumber();
            String deviceNumber = bindDTO.getDeviceNumber();
            String deviceType = bindDTO.getDeviceType();
            if (deviceId != null && mobile != null && deviceNumber != null) {
                // 获取当前用户的用户名
                //是否是联动自动下发
                String userName;
                if (isAuto) {
                    userName = "admin";
                } else {
                    userName = SystemHelper.getCurrentUsername();
                }
                // 订阅推送消息
                Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
                if (null != msgSN) {
                    SubscibeInfo info =
                        new SubscibeInfo(userName, deviceId, msgSN, ConstantUtil.T808_VEHICLE_CONTROL_ACK);
                    SubscibeInfoCache.getInstance().putTable(info);
                    T808Message message = MsgUtil
                        .get808Message(mobile, ConstantUtil.T808_VEHICLE_CONTROLLER, msgSN, outputControlSendInfo,
                            deviceType);
                    WebSubscribeManager.getInstance()
                        .sendMsgToAll(message, ConstantUtil.T808_VEHICLE_CONTROLLER, deviceId);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean regularReports(OrderForm form) {
        BindDTO bindDTO = MonitorUtils.getBindDTO(form.getVid(), MONITOR_FIELD);
        if (bindDTO != null) {
            String deviceId = bindDTO.getDeviceId();
            String mobile = bindDTO.getSimCardNumber();
            String deviceType = bindDTO.getDeviceType();
            PositionParam positionParam = new PositionParam();
            if (form.getOrderType() == 30) {
                positionParam.setPositionUpTactics(0);
            } else if (form.getOrderType() == 31) {
                positionParam.setPositionUpTactics(1);
            } else if (form.getOrderType() == 32) {
                positionParam.setPositionUpTactics(2);
            }
            positionParam.setPositionUpScheme(0);
            positionParam.setDefaultDistanceUpSpace(form.getDefaultDistanceUpSpace());
            positionParam.setDefaultTimeUpSpace(form.getDefaultTimeUpSpace());
            positionParam.setDormancyUpDistanceSpace(form.getDormancyUpDistanceSpace());
            positionParam.setDormancyUpTimeSpace(form.getDormancyUpTimeSpace());
            positionParam.setDriverLoggingOutUpDistanceSpace(form.getDriverLoggingOutUpDistanceSpace());
            positionParam.setDriverLoggingOutUpTimeSpace(form.getDriverLoggingOutUpTimeSpace());
            positionParam.setEmergencyAlarmUpDistanceSpace(form.getEmergencyAlarmUpDistanceSpace());
            positionParam.setEmergencyAlarmUpTimeSpace(form.getEmergencyAlarmUpTimeSpace());
            if (mobile != null) {
                sendTxtService
                    .setPositionParam(deviceId, mobile, positionParam, form.getSerialNumber(), deviceType, null);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean telBack(OrderForm form) {
        BindDTO vehicleInfo = MonitorUtils.getBindDTO(form.getVid(), MONITOR_FIELD);
        if (vehicleInfo != null) {
            String deviceId = vehicleInfo.getDeviceId();
            String mobile = vehicleInfo.getSimCardNumber();
            TelBack telBack = new TelBack();
            telBack.setMobile(form.getRegRet());
            if (form.getOrderType() == 40) {
                telBack.setSign(0);
            } else if (form.getOrderType() == 41) {
                telBack.setSign(1);
            }
            if (mobile != null) {
                multimediaService
                    .telListen(deviceId, telBack, mobile, form.getSerialNumber(), vehicleInfo.getDeviceType());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean sendTxt(OrderForm form) throws Exception {
        VehicleDTO config = MonitorUtils.getVehicle(form.getVid());
        if (config != null) {
            String deviceId = config.getDeviceId();
            String mobile = config.getSimCardNumber();
            String groupId = config.getOrgId();
            SendTxt txt = getSendTxt(form);
            String deviceType = config.getDeviceType();
            //获取下发文本信息的监控对象信息
            SendMsgMonitorInfo monitorInfo =
                SendMsgMonitorInfo.getInstance(form.getVid(), config.getName(), config.getOrgName());
            //当监控对象类型是车辆时  组装车辆信息
            if (Objects.equals(config.getMonitorType(), MonitorTypeEnum.VEHICLE.getType())) {
                monitorInfo.assembelVehicleInfo(config.getPlateColor(), config.getVehicleTypeName());
            }
            SendMsgBasicInfo basicInfo = realTimeVideoService.getBasicInfo(txt, form.getSerialNumber());
            SendMsgDetail detail;
            String content;
            if (mobile != null) {
                VehicleScheduler ed = new VehicleScheduler();
                ed.setVehicleId(form.getVid());
                content = form.getTxt();
                if (!StringUtils.isEmpty(content) && (content.contains("zw") || content.contains("ZW"))) {
                    content = "";
                }
                ed.setContent(content);
                ed.setGroupId(groupId);
                ed.setSendDate(new Date());
                ed.setSendTime(new Date());
                ed.setSendUsername(SystemHelper.getCurrentUsername());
                ed.setCreateDataUsername(SystemHelper.getCurrentUsername());
                //系统下发（联动策略）设置下发方式为0
                basicInfo.setSendType(0);
                if (!form.getLinkageMsg()) {
                    // 不是联动策略短信下发进入
                    boolean flag = vehicleScheduleDao.add(ed);
                    basicInfo.setSendType(1);
                }
                detail = SendMsgDetail.getSendMsg(monitorInfo, basicInfo);
                //放入咖啡因缓存  等待放回结果组装后存入HBase
                sendMsgCache.putMsgCache(detail);
                sendTxtService.sendTxt(deviceId, mobile, txt, form.getSerialNumber(), deviceType);
                final String title = "监控对象 : " + config.getName() + " 文本信息下发";
                final String msg = SendTxt.convertTxtToLogMsg(config.getDeviceType(), txt);
                logSearchService.addLog(this.getIpAddress(), msg, "3", "more", title);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean sendTxtOnly(OrderForm form) throws Exception {
        VehicleDTO vehicle = MonitorUtils.getVehicle(form.getVid());
        if (vehicle != null) {
            String deviceId = vehicle.getDeviceId();
            String mobile = vehicle.getSimCardNumber();
            String groupId = vehicle.getOrgId();
            SendTxt txt = getSendTxt(form);
            String deviceType = vehicle.getDeviceType();
            //获取下发文本信息的监控对象信息
            SendMsgMonitorInfo monitorInfo =
                SendMsgMonitorInfo.getInstance(form.getVid(), vehicle.getName(), vehicle.getOrgName());
            //当监控对象类型是车辆时  组装车辆信息
            if (Objects.equals(vehicle.getMonitorType(), MonitorTypeEnum.VEHICLE.getType())) {
                monitorInfo.assembelVehicleInfo(vehicle.getPlateColor(), vehicle.getVehicleTypeName());
            }
            SendMsgBasicInfo basicInfo = realTimeVideoService.getBasicInfo(txt, form.getSerialNumber());
            SendMsgDetail detail;
            String content;
            if (mobile != null) {
                VehicleScheduler ed = new VehicleScheduler();
                ed.setVehicleId(form.getVid());
                content = form.getTxt();
                if (!StringUtils.isEmpty(content) && (content.contains("zw") || content.contains("ZW"))) {
                    content = "";
                }
                ed.setContent(content);
                ed.setGroupId(groupId);
                ed.setSendDate(new Date());
                ed.setSendTime(new Date());
                ed.setSendUsername(SystemHelper.getCurrentUsername());
                ed.setCreateDataUsername(SystemHelper.getCurrentUsername());
                //系统下发（联动策略）设置下发方式为0
                basicInfo.setSendType(0);
                if (!form.getLinkageMsg()) {
                    // 不是联动策略短信下发进入
                    boolean flag = vehicleScheduleDao.add(ed);
                    basicInfo.setSendType(1);
                }
                detail = SendMsgDetail.getSendMsg(monitorInfo, basicInfo);
                //放入咖啡因缓存  等待放回结果组装后存入HBase
                sendMsgCache.putMsgCache(detail);
                sendTxtService.sendTxtOnly(deviceId, mobile, txt, form.getSerialNumber(), deviceType);
                return true;
            }
        }
        return false;
    }

    /**
     * 获取下发文本信息的详情
     * @param form 下发表单
     * @return SendMsgDetail
     */
    @Override
    public SendMsgDetail getSendMsgDetail(OrderForm form) {
        SendMsgBasicInfo basicInfo;
        SendMsgMonitorInfo monitorInfo;
        SendTxt txt = getSendTxt(form);
        basicInfo = realTimeVideoService.getBasicInfo(txt, form.getSerialNumber());
        basicInfo.assembleSendResult(1, "终端离线");
        Map<String, SendMsgMonitorInfo> map =
            realTimeVideoService.assblemSendMsgMonitorInfo(Collections.singletonList(form.getVid()));
        monitorInfo = map.get(form.getVid());
        return SendMsgDetail.getSendMsg(monitorInfo, basicInfo);
    }

    private SendTxt getSendTxt(OrderForm form) {
        SendTxt txt = new SendTxt();
        Integer sigo;
        Integer emergency = 0;
        Integer displayTerminalDisplay = 0;
        Integer tts = 0;
        Integer advertisingDisplay = 0;
        if (form.getMarks() != null) {
            String[] makes = form.getMarks().split(",");
            for (int i = 0; i < makes.length; i++) {
                if ("1".equals(makes[i])) {
                    emergency = 1;
                } else if ("3".equals(makes[i])) {
                    // 终端显示器显示
                    displayTerminalDisplay = 1;
                } else if ("4".equals(makes[i])) {
                    // 终端TTS读播
                    tts = 1;
                } else if ("5".equals(makes[i])) {
                    // 广告屏显示
                    advertisingDisplay = 1;
                } else if ("6".equals(makes[i])) {
                    // tts读播并处理
                    tts = 1;
                }
            }
        }
        Integer deviceType = form.getDeviceType();
        if (DeviceInfo.judgeProtocolType(deviceType) == T808MsgHead.PROTOCOL_TYPE_2019) {
            Integer messageTypeOne = form.getMessageTypeOne();
            Integer messageTypeTwo = form.getMessageTypeTwo();
            sigo = messageTypeOne + (displayTerminalDisplay << 2) + (tts << 3) + (messageTypeTwo << 5);
            txt.setType(form.getTextType());
            txt.setDeviceType(String.valueOf(deviceType));
        } else {
            sigo = emergency + (displayTerminalDisplay << 2) + (tts << 3) + (advertisingDisplay << 4);
        }

        txt.setSign(sigo);
        txt.setTxt(form.getTxt());
        return txt;
    }

    @Override
    public boolean sendQuestion(OrderForm form) throws Exception {
        BindDTO bindDTO = MonitorUtils.getBindDTO(form.getVid(), MONITOR_FIELD);
        if (bindDTO != null) {
            String deviceId = bindDTO.getDeviceId();
            String mobile = bindDTO.getSimCardNumber();
            Integer sigo = 0;
            Integer emergency = 0;
            Integer tts = 0;
            Integer advertisingDisplay = 0;
            SendQuestion sendQuestion = new SendQuestion();
            List<Answer> answerList = new ArrayList<>();
            if (form.getMarks() != null) {
                String[] makes = form.getMarks().split(",");
                for (int i = 0; i < makes.length; i++) {
                    if (makes[i].equals("1")) {
                        emergency = 1;
                    } else if (makes[i].equals("4")) {
                        tts = 1;
                    } else if (makes[i].equals("5")) {
                        advertisingDisplay = 1;
                    }
                }
            }
            sigo = emergency + (tts << 3) + (advertisingDisplay << 4);
            sendQuestion.setSign(sigo);
            sendQuestion.setRegRet(form.getQuestion().getBytes(Charset.forName("GBK")).length);
            sendQuestion.setQuestion(form.getQuestion());
            String[] ask = form.getValue().split(",");
            for (int i = 0; i < ask.length; i++) {
                Answer answer = new Answer();
                answer.setId(i);
                answer.setLen(ask[i].getBytes(Charset.forName("GBK")).length);
                answer.setValue(ask[i]);
                answerList.add(answer);
            }
            sendQuestion.setAnswers(answerList);
            if (mobile != null) {
                sendTxtService.sendQuestion(deviceId, mobile, sendQuestion, form.getSerialNumber(), bindDTO);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean terminalControl(OrderForm form) throws Exception {
        BindDTO bindDTO = MonitorUtils.getBindDTO(form.getVid(), MONITOR_FIELD);
        if (bindDTO != null) {
            String deviceId = bindDTO.getDeviceId();
            String mobile = bindDTO.getSimCardNumber();
            String deviceNumber = bindDTO.getDeviceNumber();
            String deviceType = bindDTO.getDeviceType();
            Integer msgSN = DeviceHelper.getRegisterDevice(form.getVid(), deviceNumber);
            if (msgSN != null) {
                DeviceCommand deviceCommand = new DeviceCommand();
                deviceCommand.setCw(form.getCw());
                sendTxtService.deviceCommand(mobile, deviceCommand, msgSN, deviceId, deviceType, null);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean vehicleControl(OrderForm form) throws Exception {
        BindDTO bindDTO = MonitorUtils.getBindDTO(form.getVid(), MONITOR_FIELD);
        if (bindDTO != null) {
            String deviceId = bindDTO.getDeviceId();
            String mobile = bindDTO.getSimCardNumber();
            VehicleCommand vehicleCommand = new VehicleCommand();
            Integer sign = form.getSign();
            vehicleCommand.setSign(sign);
            vehicleCommand.setType(0x01);
            String deviceType = bindDTO.getDeviceType();
            if (ProtocolTypeUtil.checkDeviceType2019(deviceType)) {
                // 此处由于没有其他业务需求, 因此先固定写
                List<VehicleControllerInfo> infoList = new ArrayList<>();
                infoList.add(getVehicleControlInfo2019(sign, vehicleCommand));
                vehicleCommand.setInfoList(infoList);
                vehicleCommand.setNum(infoList.size());
            } else {
                // 此处由于没有其他业务需求, 因此先固定写
                List<VehicleControllerInfo> infoList = new ArrayList<>();
                infoList.add(getVehicleControlInfo2013(sign));
                vehicleCommand.setInfoList(infoList);
                vehicleCommand.setNum(infoList.size());
            }

            if (mobile != null) {
                sendTxtService.vehicleCommand(deviceId, mobile, vehicleCommand, form.getSerialNumber(), deviceType);
                return true;
            }
        }
        return false;
    }

    private VehicleControllerInfo getVehicleControlInfo2013(Integer sign) {
        VehicleControllerInfo vehicleControllerInfo = new VehicleControllerInfo();
        vehicleControllerInfo.setId(0x0001);
        // 由于2013(0:车门解锁; 1: 车门加锁)版本和2019(0: 车门锁闭; 1: 车门开启)版本不一致,因此需要替换
        vehicleControllerInfo.setInfo(sign);
        return vehicleControllerInfo;
    }

    private VehicleControllerInfo getVehicleControlInfo2019(Integer sign, VehicleCommand vehicleCommand) {
        VehicleControllerInfo vehicleControllerInfo = new VehicleControllerInfo();
        vehicleControllerInfo.setId(0x0001);
        // 由于2013(0:车门解锁; 1: 车门加锁)版本和2019(0: 车门锁闭; 1: 车门开启)版本不一致,因此需要替换
        vehicleControllerInfo.setInfo((sign == 0 ? 1 : 0));
        vehicleControllerInfo.setType(vehicleCommand.getType());
        vehicleControllerInfo.setSign((sign == 0 ? 1 : 0));
        return vehicleControllerInfo;
    }

    @Override
    public boolean updateSpeedMax(OrderForm form) throws Exception {
        String vehicleId = form.getVid();
        BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId, MONITOR_FIELD);
        if (bindDTO == null) {
            return false;
        }
        String deviceId = bindDTO.getDeviceId();
        String mobile = bindDTO.getSimCardNumber();
        SpeedLimitParam speedLimitParam = new SpeedLimitParam();
        Integer maxSpeed = form.getMasSpeed();
        Integer speedTime = form.getSpeedTime();
        speedLimitParam.setMasSpeed(maxSpeed);
        speedLimitParam.setSpeedTime(speedTime);
        if (mobile == null) {
            return false;
        }
        String deviceType = bindDTO.getDeviceType();
        sendTxtService.setSpeedMax(deviceId, mobile, speedLimitParam, form.getSerialNumber(), deviceType);
        // 查询是否设置报警参数
        List<AlarmSetting> alarmSettingList = alarmSearchDao.findSpeedParameter(vehicleId);
        List<AlarmParameter> rodeNetWorkSpeedAlarmParamList = alarmSearchDao.findAlarmParametByName("超速报警（路网）");
        String username = SystemHelper.getCurrentUsername();
        Date nowDate = new Date();
        /*if (CollectionUtils.isEmpty(alarmSettingList)) {
            List<AlarmParameterSetting> needAddList = new ArrayList<>();
            List<AlarmParameter> speedAlarmParamList = alarmSearchDao.findAlarmParametByName("超速报警");
            for (AlarmParameter alarmParameter : speedAlarmParamList) {
                String paramCode = alarmParameter.getParamCode();
                AlarmParameterSetting alarmParameterSetting = new AlarmParameterSetting();
                alarmParameterSetting.setAlarmParameterId(alarmParameter.getId());
                if ("param1".equals(paramCode)) {
                    alarmParameterSetting.setParameterValue(maxSpeed.toString());
                } else if ("param2".equals(paramCode)) {
                    alarmParameterSetting.setParameterValue(speedTime.toString());
                }
                alarmParameterSetting.setId(UUID.randomUUID().toString());
                alarmParameterSetting.setVehicleId(vehicleId);
                alarmParameterSetting.setFlag(1);
                alarmParameterSetting.setCreateDataTime(nowDate);
                alarmParameterSetting.setCreateDataUsername(username);
                alarmParameterSetting.setAlarmPush(1);
                needAddList.add(alarmParameterSetting);
            }
            for (AlarmParameter alarmParameter : rodeNetWorkSpeedAlarmParamList) {
                AlarmParameterSetting alarmParameterSetting = new AlarmParameterSetting();
                alarmParameterSetting.setAlarmParameterId(alarmParameter.getId());
                alarmParameterSetting.setParameterValue(maxSpeed.toString());
                alarmParameterSetting.setId(UUID.randomUUID().toString());
                alarmParameterSetting.setVehicleId(vehicleId);
                alarmParameterSetting.setFlag(1);
                alarmParameterSetting.setCreateDataTime(nowDate);
                alarmParameterSetting.setCreateDataUsername(username);
                alarmParameterSetting.setAlarmPush(1);
                needAddList.add(alarmParameterSetting);
            }
            List<AlarmParameter> routeDepartureAlarmParamList =
                alarmSearchDao.findAlarmParameterByNameAndType("路线偏离报警", "platAlarm");
            for (AlarmParameter alarmParameter : routeDepartureAlarmParamList) {
                if (!Objects.equals(alarmParameter.getParamCode(), "param1")) {
                    continue;
                }
                AlarmParameterSetting alarmParameterSetting = new AlarmParameterSetting();
                alarmParameterSetting.setId(UUID.randomUUID().toString());
                alarmParameterSetting.setVehicleId(vehicleId);
                alarmParameterSetting.setAlarmParameterId(alarmParameter.getId());
                alarmParameterSetting.setParameterValue("0");
                alarmParameterSetting.setAlarmPush(1);
                alarmParameterSetting.setFlag(1);
                alarmParameterSetting.setCreateDataTime(nowDate);
                alarmParameterSetting.setCreateDataUsername(username);
                needAddList.add(alarmParameterSetting);
            }
            List<AlarmParameter> plateSpeedAlarmParamList =
                alarmSearchDao.findAlarmParameterByNameAndType("超速报警", "platAlarm");
            for (AlarmParameter alarmParameter : plateSpeedAlarmParamList) {
                if (!Objects.equals(alarmParameter.getParamCode(), "param1")) {
                    continue;
                }
                AlarmParameterSetting alarmParameterSetting = new AlarmParameterSetting();
                alarmParameterSetting.setId(UUID.randomUUID().toString());
                alarmParameterSetting.setVehicleId(vehicleId);
                alarmParameterSetting.setAlarmParameterId(alarmParameter.getId());
                alarmParameterSetting.setParameterValue("0");
                alarmParameterSetting.setAlarmPush(1);
                alarmParameterSetting.setFlag(1);
                alarmParameterSetting.setCreateDataTime(nowDate);
                alarmParameterSetting.setCreateDataUsername(username);
                needAddList.add(alarmParameterSetting);
            }
            List<AlarmParameter> abnormalAlarmParamList =
                alarmSearchDao.findAlarmParameterByNameAndType("异动报警", "platAlarm");
            for (AlarmParameter alarmParameter : abnormalAlarmParamList) {
                if (!Objects.equals(alarmParameter.getParamCode(), "param1")) {
                    continue;
                }
                AlarmParameterSetting alarmParameterSetting = new AlarmParameterSetting();
                alarmParameterSetting.setId(UUID.randomUUID().toString());
                alarmParameterSetting.setVehicleId(vehicleId);
                alarmParameterSetting.setAlarmParameterId(alarmParameter.getId());
                alarmParameterSetting.setParameterValue("0");
                alarmParameterSetting.setAlarmPush(1);
                alarmParameterSetting.setFlag(1);
                alarmParameterSetting.setCreateDataTime(nowDate);
                alarmParameterSetting.setCreateDataUsername(username);
                needAddList.add(alarmParameterSetting);
            }
            abnormalAlarmParamList.stream().filter(obj -> Objects.equals(obj.getParamCode(), "param1")).findAny()
                .ifPresent(obj -> {
                    RedisKey redisKey = HistoryRedisKeyEnum.MONITOR_ALARM_PARAM.of(vehicleId, "77");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("calStandard", "0");
                    RedisHelper.setString(redisKey, jsonObject.toJSONString());
                });

            if (CollectionUtils.isNotEmpty(needAddList)) {
                alarmSettingDao.addAlarmSettingByBatch(needAddList);
            }
            // 添加redis缓存
            RedisKey vehicleRedisKey = HistoryRedisKeyEnum.ALARM_PUSH_SET_MONITOR_ID.of(vehicleId);
            RedisKey deviceRedisKey = HistoryRedisKeyEnum.ALARM_PUSH_SET_DEVICE_ID.of(deviceId);
            JSONObject defaultType = new JSONObject();
            final JSONObject defaultType1 = WebSubscribeManager.getInstance().getDefaultType(1);
            final JSONObject defaultType2 = WebSubscribeManager.getInstance().getDefaultType(2);
            defaultType.putAll(defaultType1);
            defaultType.putAll(defaultType2);
            String vehicleKey = vehicleRedisKey.get();
            String deviceKey = deviceRedisKey.get();
            JSONObject deviceJsonObj = new JSONObject();
            JSONObject vehicleJsonObj = new JSONObject();
            deviceJsonObj.put(deviceKey, defaultType);
            vehicleJsonObj.put(vehicleKey, defaultType);
            RedisHelper.setString(vehicleRedisKey, vehicleJsonObj.toJSONString());
            RedisHelper.setString(deviceRedisKey, deviceJsonObj.toJSONString());
        }*/
        // 如果报警参数未设置，只做终端下发；  如果有报警设置，修改并通知flink
        if (CollectionUtils.isNotEmpty(alarmSettingList)) {
            for (AlarmSetting alarmSet : alarmSettingList) {
                String paramCode = alarmSet.getParamCode();
                AlarmParameterSettingForm alarmForm = new AlarmParameterSettingForm();
                if ("param1".equals(paramCode)) {
                    alarmForm.setParameterValue(form.getMasSpeed().toString());
                } else if ("param2".equals(paramCode)) {
                    alarmForm.setParameterValue(form.getSpeedTime().toString());
                }
                alarmForm.setAlarmParameterId(alarmSet.getAlarmParameterId());
                alarmForm.setVehicleId(vehicleId);
                alarmForm.setUpdateDataTime(nowDate);
                alarmForm.setUpdateDataUsername(username);
                alarmSettingDao.updateAlarmSettings(alarmForm);
            }
            if (CollectionUtils.isNotEmpty(rodeNetWorkSpeedAlarmParamList)) {
                AlarmParameter alarmParameter = rodeNetWorkSpeedAlarmParamList.get(0);
                AlarmParameterSettingForm alarmForm = new AlarmParameterSettingForm();
                alarmForm.setParameterValue(maxSpeed.toString());
                alarmForm.setAlarmParameterId(alarmParameter.getId());
                alarmForm.setVehicleId(vehicleId);
                alarmForm.setUpdateDataTime(nowDate);
                alarmForm.setUpdateDataUsername(username);
                alarmSettingDao.updateAlarmSettings(alarmForm);
            }
            // 通知storm更新数据库数据
            ZMQFencePub.pubChangeFence("9");
            // 通知storm更新缓存数据
            ZMQFencePub.pubChangeFence("12");
        }
        return true;
    }

    @Override
    public boolean recordCollection(OrderForm form) throws Exception {
        BindDTO bindDTO = MonitorUtils.getBindDTO(form.getVid(), MONITOR_FIELD);
        if (bindDTO != null) {
            String deviceId = bindDTO.getDeviceId();
            String mobile = bindDTO.getSimCardNumber();
            RecordCollection recordCollection = new RecordCollection();
            Integer cw = 0x00;
            switch (form.getCommandSign()) {
                case "0H":
                    cw = 0x00;
                    break;
                case "1H":
                    cw = 0x01;
                    break;
                case "2H":
                    cw = 0x02;
                    break;
                case "3H":
                    cw = 0x03;
                    break;
                case "4H":
                    cw = 0x04;
                    break;
                case "5H":
                    cw = 0x05;
                    break;
                case "6H":
                    cw = 0x06;
                    break;
                case "7H":
                    cw = 0x07;
                    break;
                case "8H":
                    cw = 0x08;
                    break;
                case "9H":
                    cw = 0x09;
                    break;
                case "10H":
                    cw = 0x10;
                    break;
                case "11H":
                    cw = 0x11;
                    break;
                case "12H":
                    cw = 0x12;
                    break;
                case "13H":
                    cw = 0x13;
                    break;
                case "14H":
                    cw = 0x14;
                    break;
                case "15H":
                    cw = 0x15;
                    break;
                default:
                    break;
            }
            recordCollection.setCw(cw);
            String commandSign = form.getCommandSign();
            List<String> beforeEightCommandSigns = RecordCollectionEnum.TIME_RECORDCOLLECTIONENUM_LIST;
            RecordCollectionDataA recordCollectionDataA = new RecordCollectionDataA();
            recordCollectionDataA.setTag(0xAA75);
            recordCollectionDataA.setCW(cw);
            recordCollectionDataA.setLen(0);
            recordCollectionDataA.setKeep(0x00);
            recordCollection.setData(recordCollectionDataA);
            if (beforeEightCommandSigns.contains(commandSign)) {
                RecordCollectionDataB recordCollectionDataB = new RecordCollectionDataB();
                String startTime = LocalDateUtils.dateTimeFormatYYMM(LocalDateUtils.localDateTime(form.getStartTime()));
                recordCollectionDataB.setStartTime(startTime);
                String endTime = LocalDateUtils.dateTimeFormatYYMM(LocalDateUtils.localDateTime(form.getEndTime()));
                recordCollectionDataB.setEndTime(endTime);
                recordCollectionDataB.setMaxSum(3);
                recordCollectionDataA.setLen(0x0E);
                recordCollectionDataA.setData(recordCollectionDataB);
            }
            if (mobile != null) {
                String deviceType = bindDTO.getDeviceType();
                sendTxtService.recordCollection(deviceId, mobile, recordCollection, form.getSerialNumber(), deviceType);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean recordSend(OrderForm form) throws Exception {
        BindDTO bindDTO = MonitorUtils.getBindDTO(form.getVid(), MONITOR_FIELD);
        if (bindDTO != null) {
            String deviceId = bindDTO.getDeviceId();
            String mobile = bindDTO.getSimCardNumber();
            JSONObject data = new JSONObject();
            JSONObject body = new JSONObject();

            body.put("vehicleID", form.getVin());
            body.put("plateNumber", form.getPlateNumber());
            body.put("plateType", form.getPlateType());

            data.put("CW", 0x82);
            data.put("tag", 0xAA75);
            //长度是固定的41而不是用户填的数据长度
            data.put("len", 0x29);
            data.put("keep", 0x00);
            data.put("data", body);
            RecordSend recordSend = new RecordSend();
            recordSend.setData(data);
            recordSend.setCw(0x82);
            if (mobile != null) {
                sendTxtService
                    .recordSend(deviceId, mobile, recordSend, form.getSerialNumber(), bindDTO.getDeviceType());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean multimediaRetrieval(OrderForm form) throws Exception {
        BindDTO bindDTO = MonitorUtils.getBindDTO(form.getVid(), MONITOR_FIELD);
        if (bindDTO != null) {
            String deviceId = bindDTO.getDeviceId();
            String mobile = bindDTO.getSimCardNumber();
            MultimediaRetrieval multimediaRetrieval = new MultimediaRetrieval();
            multimediaRetrieval.setType(form.getType());
            multimediaRetrieval.setWayID(form.getWayID());
            multimediaRetrieval.setEventCode(form.getEventCode());
            multimediaRetrieval.setStartTime(
                DateUtil.getDateToString(DateUtil.getStringToDate(form.getStartTime(), DATE_FORMAT1), DATE_FORMAT2));
            multimediaRetrieval.setEndTime(
                DateUtil.getDateToString(DateUtil.getStringToDate(form.getEndTime(), DATE_FORMAT1), DATE_FORMAT2));
            if (mobile != null) {
                String deviceType = bindDTO.getDeviceType();
                multimediaService
                    .multimediaRetrieval(deviceId, multimediaRetrieval, mobile, form.getSerialNumber(), deviceType);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean multimediaUpload(OrderForm form) {
        MultimediaUpload multimediaUpload = new MultimediaUpload();
        multimediaUpload.setType(form.getType());
        multimediaUpload.setWayID(form.getWayID());
        multimediaUpload.setEventCode(form.getEventCode());
        multimediaUpload.setStartTime(
            DateUtil.getDateToString(DateUtil.getStringToDate(form.getStartTime(), DATE_FORMAT1), DATE_FORMAT2));
        multimediaUpload.setEndTime(
            DateUtil.getDateToString(DateUtil.getStringToDate(form.getEndTime(), DATE_FORMAT1), DATE_FORMAT2));
        if (form.getDeleteSign() == null) {
            multimediaUpload.setDeleteSign(0);
        } else {
            multimediaUpload.setDeleteSign(form.getDeleteSign());
        }
        BindDTO bindDTO = MonitorUtils.getBindDTO(form.getVid(), MONITOR_FIELD);
        if (bindDTO != null) {
            multimediaService.multimediaUpload(multimediaUpload, bindDTO.getDeviceId(), bindDTO.getSimCardNumber(),
                form.getSerialNumber(), bindDTO.getDeviceType());
            return true;
        }
        return false;
    }

    @Override
    public boolean record(OrderForm form) {
        BindDTO bindDTO = MonitorUtils.getBindDTO(form.getVid(), MONITOR_FIELD);
        if (bindDTO != null) {
            String deviceId = bindDTO.getDeviceId();
            String mobile = bindDTO.getSimCardNumber();
            Record record = new Record();
            record.setCommand(form.getVoiceCommand());
            record.setFrequency(form.getFrequency());
            record.setSaveSign(form.getSaveSign());
            record.setTime(form.getTime());
            if (mobile != null) {
                multimediaService.record(deviceId, record, mobile, form.getSerialNumber(), bindDTO.getDeviceType());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean originalOrder(OrderForm form) throws Exception {
        BindDTO bindDTO = MonitorUtils.getBindDTO(form.getVid(), MONITOR_FIELD);
        if (bindDTO != null) {
            String deviceId = bindDTO.getDeviceId();
            String mobile = bindDTO.getSimCardNumber();
            String deviceType = bindDTO.getDeviceType();
            OriginalOrder originalOrder = new OriginalOrder();
            originalOrder.setType(Integer.parseInt(form.getParam(), 16));
            originalOrder.setData(StringUtil.gbkStringToBytes(form.getData()));
            if (mobile != null) {
                sendTxtService.originalOrder(deviceId, mobile, originalOrder, form.getSerialNumber(), deviceType);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean terminalParameters(OrderForm form) throws Exception {
        BindDTO bindDTO = MonitorUtils.getBindDTO(form.getVid(), MONITOR_FIELD);
        if (bindDTO != null) {
            String deviceId = bindDTO.getDeviceId();
            String mobile = bindDTO.getSimCardNumber();
            String deviceType = bindDTO.getDeviceType();
            monitor8104Cache.put(form.getSerialNumber() + "_" + deviceId, deviceId);
            sendTxtService.terminalParameters(deviceId, mobile, form.getSerialNumber(), deviceType);
        }
        return true;
    }

    @Override
    public boolean informationService(OrderForm form) throws Exception {
        BindDTO bindDTO = MonitorUtils.getBindDTO(form.getVid(), MONITOR_FIELD);
        if (bindDTO != null) {
            String deviceId = bindDTO.getDeviceId();
            String mobile = bindDTO.getSimCardNumber();
            InformationService informationService = new InformationService();
            informationService.setType(1);
            informationService.setLen(form.getValue().getBytes(Charset.forName("GBK")).length);
            informationService.setPackageSum(form.getValue().getBytes(Charset.forName("GBK")).length);
            informationService.setValue(form.getValue());
            if (mobile != null) {
                String deviceType = bindDTO.getDeviceType();
                sendTxtService
                    .informationService(deviceId, mobile, informationService, form.getSerialNumber(), deviceType);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean oilElectric(OrderForm form) throws Exception {
        String userId = SystemHelper.getCurrentUser().getId().toString();
        if (userId != null) {
            Collection<Group> currentRoles = roleService.getByMemberNameStr(userId);
            List<String> roleName = new ArrayList<>();
            for (Group group : currentRoles) {
                if (group.getRoleName() != null) {
                    roleName.add(group.getName());
                }
            }
            if (roleName.contains("POWER_USER") || roleName.contains("ROLE_ADMIN")) {
                BindDTO monitor = MonitorUtils.getBindDTO(form.getVid(), MONITOR_FIELD);
                if (monitor != null) {
                    String deviceId = monitor.getDeviceId();
                    String mobile = monitor.getSimCardNumber();
                    String deviceNumber = monitor.getDeviceNumber();
                    if (deviceId != null && mobile != null && deviceNumber != null) {
                        OilElectricControl control = new OilElectricControl();
                        OilElectric oilElectric = new OilElectric();
                        oilElectric.setType(0xF3);
                        oilElectric.setSensorId(0x90);
                        oilElectric.setSign(1);
                        oilElectric.setControlType(2);
                        oilElectric.setControlStauts(form.getFlag());
                        oilElectric.setControlIo(1);
                        oilElectric.setControlTime(0xFFFF);
                        List<OilElectric> list = new ArrayList<>();
                        list.add(oilElectric);
                        control.setNum(list.size());
                        control.setSign(1);
                        control.setInfoList(list);
                        UserDTO userDTO = userService.getByDn("uid=admin,ou=organization");
                        String sendDownCommand = "";
                        if (userDTO != null) {
                            sendDownCommand = userDTO.getSendDownCommand();// 获取admin的加密的下发口令
                        }
                        if (mobile != null && SecurityPasswordHelper
                            .isPasswordValid(sendDownCommand, form.getOilElectricMsg())) {
                            // 获取当前用户的用户名
                            String userName = SystemHelper.getCurrentUsername();
                            // 订阅推送消息
                            Integer msgSN = DeviceHelper.getRegisterDevice(form.getVid(), deviceNumber);
                            if (null != msgSN) {
                                SubscibeInfo info =
                                    new SubscibeInfo(userName, deviceId, msgSN, ConstantUtil.T808_VEHICLE_CONTROL_ACK);
                                SubscibeInfoCache.getInstance().putTable(info);
                                sendTxtService.oilElectric(deviceId, mobile, control, msgSN, monitor.getDeviceType());
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean updateTerminalPlate(OrderForm orderForm, String ip) throws Exception {
        String vid = orderForm.getVid();
        VehicleDTO oldVehicle = vehicleService.getById(vid);
        if (Objects.isNull(oldVehicle)) {
            return false;
        }
        //复制新的车辆信息 （必须新建一个对象：后面会再次进行车辆信息的查询，受Mybatis的缓存机制影响，所有不能直接对查询出的结果进行修改）
        BeanCopier beanCopier = BeanCopier.create(VehicleDTO.class, VehicleDTO.class, false);
        VehicleDTO vehicleDTO = new VehicleDTO();
        beanCopier.copy(oldVehicle, vehicleDTO, null);
        T808_0x8103 t808X8103 = new T808_0x8103();
        List paramItems = t808X8103.getParamItems();
        String newPlateNumber = orderForm.getBrand();
        if (StringUtils.isNotEmpty(newPlateNumber)) {
            // 车牌号 指令: 0x0083
            addParamItem(paramItems, newPlateNumber, ConstantUtil.SETTING_PLATE_NUMBER);
            // 修改平台车牌号
            vehicleDTO.setName(newPlateNumber);
        }

        // 车牌颜色 指令: 0x0084 1:蓝色 2:黄色 3:黑色 4:白色 9:其他 0:未上牌时取0
        String plateColor = orderForm.getPlateColor();
        if (StringUtils.isNotBlank(plateColor) && !"-1".equals(plateColor)) {
            addParamItem(paramItems, plateColor, ConstantUtil.SETTING_PLATE_COLOR);
            vehicleDTO.setPlateColor(Integer.valueOf(plateColor));
        }
        // 省域ID 指令: 0x0081
        String provinceId = orderForm.getProvinceId();
        if (StringUtils.isNotBlank(provinceId)) {
            addParamItem(paramItems, provinceId, ConstantUtil.SETTING_PROVINCE_ID);
            vehicleDTO.setProvinceId(provinceId);
        }
        // 市域ID 指令: 0x0082
        String cityId = orderForm.getCityId();
        if (StringUtils.isNotEmpty(cityId)) {
            addParamItem(paramItems, cityId, ConstantUtil.SETTING_CITY_ID);
            vehicleDTO.setCityId(cityId);
        }
        if (CollectionUtils.isEmpty(paramItems)) {
            return false;
        }
        t808X8103.setParametersCount(paramItems.size());
        Integer transNo = DeviceHelper.getRegisterDevice(vid, vehicleDTO.getDeviceNumber());
        if (null == transNo) {
            return false;
        }
        getSubscribeInfo(vehicleDTO, transNo, t808X8103);
        vehicleService.update(vehicleDTO);
        return true;

    }

    /**
     * 添加参数
     * @param paramItems
     * @param paramValue
     */
    private void addParamItem(List paramItems, String paramValue, Integer paramId) {
        ParamItem plateColorItem = new ParamItem();
        plateColorItem.setParamId(paramId);
        plateColorItem.setParamLength(paramValue.length());
        plateColorItem.setParamValue(paramValue);
        paramItems.add(plateColorItem);
    }

    private void getSubscribeInfo(VehicleDTO vehicleDTO, Integer transNo, T808_0x8103 benchmark) {
        String deviceId = vehicleDTO.getDeviceId();
        SubscibeInfo subscibeInfo =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, transNo, ConstantUtil.T808_DEVICE_GE_ACK, 1);
        SubscibeInfoCache.getInstance().putTable(subscibeInfo);

        String simNum = vehicleDTO.getSimCardNumber();
        T808Message message =
            MsgUtil.get808Message(simNum, ConstantUtil.T808_SET_PARAM, transNo, benchmark, vehicleDTO.getDeviceType());
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_PARAM, deviceId);
    }

    @Override
    public JsonResultBean findOBD() {
        return new JsonResultBean(obdVehicleTypeDao.findAll());
    }

    @Override
    public boolean sendOBDParam(OrderForm orderForm, String ip) throws Exception {
        // 报警参数设置下发
        String vehicleId = orderForm.getVid();
        // 获取车辆及设备信息
        BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId, MONITOR_FIELD);
        if (Objects.nonNull(bindDTO)) {
            OBDParam obdParam = new OBDParam();
            String deviceNumber = bindDTO.getDeviceNumber();
            String deviceId = bindDTO.getDeviceId();
            String simcardNumber = bindDTO.getSimCardNumber();
            obdParam.setSensorID(0xE5);
            obdParam.setDataLen(0x08);
            if (orderForm.getUploadTime() == null) {
                obdParam.setUploadTime(0xFFFFFFFF);
            } else {
                // 单位 : ms
                obdParam.setUploadTime(orderForm.getUploadTime() * 1000);
            }
            obdParam.setVehicleId(orderForm.getVid());
            String obdVehicleTypeId = orderForm.getObdVehicleTypeId();
            obdParam.setVehicleTypeId(orderForm.getVehicleTypeId());
            //下发obd信息
            Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
            if (null == msgSN) {
                return false;
            }
            sendTxtService.sendOBD(simcardNumber, msgSN, obdParam, deviceId, bindDTO.getDeviceType());
            //修改车辆的obd参数设置
            if (StringUtils.isNotBlank(obdVehicleTypeId)) {
                OBDManagerSettingForm form = new OBDManagerSettingForm();
                form.setVehicleId(vehicleId);
                form.setObdVehicleTypeId(obdVehicleTypeId);
                form.setTime(orderForm.getUploadTime());
                List<OBDManagerSettingForm> obdSettingByVid = obdManagerSettingService.findObdSettingByVid(vehicleId);
                if (CollectionUtils.isNotEmpty(obdSettingByVid)) {
                    form.setId(obdSettingByVid.get(0).getId());
                    obdManagerSettingService.updateObdManagerSetting(form);
                } else {
                    obdManagerSettingService.addObdManagerSetting(form);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 链路检测
     * @param vehicleId
     * @return
     * @throws Exception
     */
    @Override
    public boolean sendLindCheck(String vehicleId) {
        BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId, MONITOR_FIELD);
        if (bindDTO == null) {
            return false;
        }
        String mobile = bindDTO.getSimCardNumber();
        if (mobile == null) {
            return false;
        }
        String deviceId = bindDTO.getDeviceId();
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, "");
        if (null == msgSN) {
            return false;
        }
        // 订阅推送消息
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN, ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        T808Message message =
            MsgUtil.get808Message(mobile, ConstantUtil.T808_LINK_CHECK, msgSN, null, bindDTO.getDeviceType());
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_LINK_CHECK, deviceId);
        return true;

    }

    @Override
    public boolean sendFenceQuery(OrderForm form, String ip) {
        String vehicleId = form.getVid();
        BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId, MONITOR_FIELD);
        if (bindDTO == null) {
            return false;
        }
        String mobile = bindDTO.getSimCardNumber();
        if (mobile == null) {
            return false;
        }
        String deviceId = bindDTO.getDeviceId();

        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, "");
        if (null == msgSN) {
            return false;
        }
        T8080x8608 sendParam = T8080x8608.getInstance(form);
        // 订阅推送消息
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN, ConstantUtil.T808_FENCE_QUERY_RESP);
        SubscibeInfoCache.getInstance().putTable(info);
        T808Message message =
            MsgUtil.get808Message(mobile, ConstantUtil.T808_FENCE_QUERY, msgSN, sendParam, bindDTO.getDeviceType());
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_FENCE_QUERY, deviceId);
        return true;
    }

    @Override
    public boolean sendDriverActiveReport(OrderForm form, String ip) {
        String vehicleId = form.getVid();
        BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId, MONITOR_FIELD);
        if (Objects.isNull(bindDTO)) {
            return false;
        }
        String mobile = bindDTO.getSimCardNumber();
        String deviceId = bindDTO.getDeviceId();
        String deviceNumber = bindDTO.getDeviceNumber();
        Integer msgSn = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        if (null == msgSn) {
            return false;
        }

        // 订阅推送消息
        SubscibeInfo subscibeInfo =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSn, ConstantUtil.T808_DRIVER_INFO);
        SubscibeInfoCache.getInstance().putTable(subscibeInfo);

        // 组装T808消息
        T808Message t808Message =
            MsgUtil.get808Message(mobile, ConstantUtil.DEVICE_ACTIVE_REPORT, msgSn, null, bindDTO.getDeviceType());
        WebSubscribeManager.getInstance().sendMsgToAll(t808Message, ConstantUtil.DEVICE_ACTIVE_REPORT, deviceId);
        return true;
    }
}
