package com.zw.platform.service.connectionparamsset_809.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.domain.VehicleSpotCheckInfo;
import com.cb.platform.repository.mysqlDao.SpotCheckReportDao;
import com.zw.adas.domain.common.AdasRiskStatus;
import com.zw.adas.domain.riskManagement.AdasAlarmDealInfo;
import com.zw.adas.domain.riskManagement.AdasFunctionIdToZwRelation;
import com.zw.adas.domain.riskManagement.bean.AdasRiskEventEsBean;
import com.zw.adas.service.elasticsearch.AdasElasticSearchService;
import com.zw.adas.service.riskdisposerecord.AdasRiskService;
import com.zw.adas.utils.AdasCommonHelper;
import com.zw.app.domain.alarm.AlarmMorePos;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.ConfigDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.dto.result.UserMenuDTO;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.ProtocolInfo;
import com.zw.platform.domain.connectionparamsset_809.AlarmHandleParam;
import com.zw.platform.domain.connectionparamsset_809.AlarmSettingBean;
import com.zw.platform.domain.connectionparamsset_809.PlantParam;
import com.zw.platform.domain.connectionparamsset_809.PlantParamQuery;
import com.zw.platform.domain.connectionparamsset_809.T809AlarmMapping;
import com.zw.platform.domain.connectionparamsset_809.T809AlarmSetting;
import com.zw.platform.domain.connectionparamsset_809.T809PlantFormCheck;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.domain.multimedia.HandleAlarms;
import com.zw.platform.domain.netty.ServerParamList;
import com.zw.platform.domain.oil.AlarmHandle;
import com.zw.platform.domain.reportManagement.SuperPlatformMsg;
import com.zw.platform.domain.reportManagement.T809AlarmForwardInfoMiddleQuery;
import com.zw.platform.domain.reportManagement.Zw809MessageDO;
import com.zw.platform.domain.vas.alram.AlarmType;
import com.zw.platform.push.common.MessageAsyncTaskExecutor;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.modules.ConnectionParamsConfigDao;
import com.zw.platform.repository.modules.ConnectionParamsSetDao;
import com.zw.platform.repository.modules.ProtocolUtilDao;
import com.zw.platform.repository.vas.AlarmSearchDao;
import com.zw.platform.service.alarm.AlarmSearchService;
import com.zw.platform.service.connectionparamsset_809.ConnectionParamsConfigService;
import com.zw.platform.service.connectionparamsset_809.ConnectionParamsSetService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.reportManagement.SuperPlatformMsgService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.FormulaCalculator;
import com.zw.platform.util.FormulaCalculatorResp;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.DelayedEventTrigger;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import com.zw.platform.util.sleep.SleepUtils;
import com.zw.protocol.msg.AlarmDealMessage;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t809.T809Message;
import com.zw.protocol.msg.t809.T809MsgHead;
import com.zw.protocol.msg.t809.body.ExchangeInfo;
import com.zw.protocol.msg.t809.body.SupervisionAlarmInfo;
import com.zw.protocol.msg.t809.body.module.AlarmProcessAck;
import com.zw.protocol.msg.t809.body.module.CheckAck;
import com.zw.protocol.msg.t809.body.module.Extend809AlarmAck;
import com.zw.protocol.msg.t809.body.module.ExtendGngAck;
import com.zw.protocol.msg.t809.body.module.ExtendPlatformMsgInfo;
import com.zw.protocol.msg.t809.body.module.PlatformAlarmAck;
import com.zw.protocol.msg.t809.body.module.PlatformAlarmAckInfo;
import com.zw.protocol.msg.t809.body.module.PlatformAlarmInfo;
import com.zw.protocol.msg.t809.body.module.PlatformMsgAckInfo;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.protocol.util.ProtocolTypeUtil;
import com.zw.ws.common.PublicVariable;
import io.netty.channel.Channel;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.query.SearchScope;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.zw.platform.util.ConstantUtil.CONNECT_PROTOCOL_TYPE_808_2019;
import static com.zw.platform.util.MenuConstants.MENU_CONNECTION_PARAMS;

@Service
public class ConnectionParamsSetServiceImpl implements ConnectionParamsSetService {
    private static final String[] MAIN_STATUS_NAME =
        { "关闭连接", "正常连接", "IP地址不正确", "接入码不正确", "用户没有注册", "密码错误", "资源紧张(正在连接)", "其他(正在连接)", "链路异常关闭(正在连接)",
            "上级平台主动关闭(正在连接)", "等待连接", "正在链接" };

    private static final String[] BRANCH_STATUS_NAME = { "关闭", "正常连接", "校验码错误", "资源紧张，稍后再连接(已经占用)", "其他" };

    private static final Logger logger = LogManager.getLogger(ConnectionParamsSetServiceImpl.class);

    /**
     * 从链路IP最多为10个
     */
    private static final Integer BRANCH_MAX_SIZE = 10;

    /**
     * 从链路IP默认值
     */
    private static final String BRANCH_DEFAULT = "0.0.0.0";

    @Autowired
    ConnectionParamsSetDao connectionParamsSetDao;

    @Autowired
    ServerParamList serverParamList;

    @Autowired
    ConnectionParamsConfigDao connectionParamsConfigDao;

    @Autowired
    ConnectionParamsConfigService connectionParamsConfigService;

    @Autowired
    LogSearchService logSearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private NewConfigDao configDao;

    @Autowired
    private SuperPlatformMsgService superPlatformMsgService;

    @Autowired
    private AlarmSearchDao alarmDao;

    @Autowired
    private SpotCheckReportDao spotCheckReportDao;

    @Autowired
    private AdasElasticSearchService adasEsService;

    @Autowired
    private AdasCommonHelper adasCommonHelper;

    @Autowired
    private AdasRiskService adasRiskService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private DelayedEventTrigger trigger;

    @Autowired
    private ProtocolUtilDao protocolUtilDao;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private MessageAsyncTaskExecutor messageAsyncTaskExecutor;

    @Autowired
    private AlarmSearchService alarmSearchService;

    @Value("${alarm.deal.batch.size}")
    private int alarmBatchSize;

    @Value("${alarm.deal.batch.thread}")
    private boolean alarmDealBatchThread;

    @Value("${inspection.enable}")
    private boolean inspectionEnable;

    private static String getConnectType(int type, int connectType) {
        StringBuilder connectTypeName = new StringBuilder();
        switch (connectType) {
            case 1://服务开启状态
                if (1 == type) {
                    connectTypeName.append("开启");
                } else {
                    connectTypeName.append("关闭");
                }
                break;
            case 2://服务端、客户端开启状态
                if (0 == type) {
                    connectTypeName.append("断开");
                } else if (1 == type) {
                    connectTypeName.append("连接");
                } else if (2 == type) {
                    connectTypeName.append("注销中");
                }
                break;
            case 3:
                //从链路服务端
                if (1 == type) {
                    connectTypeName.append("连接");
                } else {
                    connectTypeName.append("断开");
                }
                break;
            default:
                break;
        }
        return connectTypeName.toString();
    }

    /**
     * 获取企业属性名称
     */
    private static String getGroupPropertyName(Integer groupProperty) {
        StringBuilder groupPropertyName = new StringBuilder();
        switch (groupProperty) {
            case 0:
                groupPropertyName.append("政府");
                break;
            case 1:
                groupPropertyName.append("经营性企业");
                break;
            case 2:
                groupPropertyName.append("非经营性企业");
                break;

            default:
                break;
        }
        return groupPropertyName.toString();
    }

    @Override
    public boolean save809ConnectionParamsSet(PlantParam param, String ipAddress) {
        //添加初始化参数
        param.setCreateDataUsername(SystemHelper.getCurrentUsername());
        param.setBranchServer(0);
        param.setBranchStatus(0);
        param.setMainClient(0);
        param.setMainStatus(0);
        param.setServerStatus(0);
        //随机生成归属地平台口令
        param.setAuthorizeCode1(StringUtil.getRandomStringByLength(64));
        param.setAuthorizeCode2(StringUtil.getRandomStringByLength(64));
        //
        //将从链路IP字段中回车换行替换为‘#’
        String changeIpBranch = param.getIpBranch().replaceAll("\r\n", "\n");
        String newIpBranch = changeIpBranch.replaceAll("\n", "#");
        param.setIpBranch(newIpBranch);
        //保存参数
        boolean flag = connectionParamsSetDao.save809ConnectionParamsSet(param);
        //如果保存成功，则下发新增809上级平台指令至F3
        if (flag) {
            T809Message t809Message =
                MsgUtil.getT809Message(ConstantUtil.T809_PLATFORM_ADD, param.getIp(), param.getCenterId(), param);
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809())
                .writeAndFlush(MsgUtil.getMsg(ConstantUtil.T809_PLATFORM_ADD, t809Message));
            //日志语句
            //记录日志
            logSearchService
                .addLog(ipAddress, "新增809转发平台 : " + param.getPlatformName() + " IP为 : " + param.getIp(), "3", "809转发平台",
                    "-", "");
        }
        return flag;
    }

    @Override
    public PlantParam get809ConnectionParamsForEdit(PlantParamQuery plantParamQuery) {
        List<PlantParam> plantParam = get809ConnectionParamsSet(plantParamQuery);
        if (1 != plantParam.size()) {
            return null;
        }
        //获取该平台所属企业名称
        PlantParam param = plantParam.get(0);
        OrganizationLdap group = organizationService.getOrganizationByUuid(param.getGroupId());
        param.setGroupName(group == null ? "" : group.getName());
        return param;
    }

    @Override
    public boolean update809ConnectionParamsSet(PlantParam param, String ipAddress) {
        boolean flag = false;
        //查询以前设置参数
        PlantParamQuery plantParamQuery = new PlantParamQuery();
        plantParamQuery.setId(param.getId());
        List<PlantParam> plantParam = connectionParamsSetDao.get809ConnectionParamsSet(plantParamQuery);
        if (1 == plantParam.size()) {
            //组装原有不变更参数及固定参数
            PlantParam pa = plantParam.get(0);
            param.setAuthorizeCode1(pa.getAuthorizeCode1());
            param.setAuthorizeCode2(pa.getAuthorizeCode2());
            //将从链路IP字段中回车换行替换为‘#’
            String changeIpBranch = param.getIpBranch().replaceAll("\r\n", "\n");
            String newIpBranch = changeIpBranch.replaceAll("\n", "#");
            param.setIpBranch(newIpBranch);
            boolean alarmFlag = false;
            Integer alarmSet = connectionParamsSetDao.get809Mapping(pa.getId(), pa.getProtocolType());
            //设置过809报警参数且修改了协议
            if (alarmSet != null && alarmSet == 1 && !Objects.equals(pa.getProtocolType(), param.getProtocolType())) {
                //删除原809报警设置  设置表的809_mapping_flag为空
                T809AlarmSetting setting = new T809AlarmSetting();
                setting.setSettingId(pa.getId());
                connectionParamsSetDao.delete809AlarmMapping(setting);
                param.setMappingFlag(null);
                alarmFlag = true;
            }
            //保存修改参数
            flag = connectionParamsSetDao.update809ConnectionParamsSet(param);
            //如果保存成功，则下发新增809上级平台指令至F3
            if (flag) {
                //先发送删除平台指令
                T809Message t809Message =
                    MsgUtil.getT809Message(ConstantUtil.T809_PLATFORM_DELETE, pa.getIp(), pa.getCenterId(), null);
                WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809())
                    .writeAndFlush(MsgUtil.getMsg(ConstantUtil.T809_PLATFORM_DELETE, t809Message));
                //2秒后再发送新增平台指令
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        T809Message t809Message = MsgUtil
                            .getT809Message(ConstantUtil.T809_PLATFORM_ADD, param.getIp(), param.getCenterId(), param);
                        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809())
                            .writeAndFlush(MsgUtil.getMsg(ConstantUtil.T809_PLATFORM_ADD, t809Message));
                    }
                }, 2000);
                //日志语句
                //记录日志
                logSearchService
                    .addLog(ipAddress, "修改809转发平台 : " + param.getPlatformName() + " IP为 : " + param.getIp(), "3",
                        "809转发平台", "-", "");
                if (alarmFlag) {
                    //通知flink809报警设置修改
                    ZMQFencePub.pubChangeFence("19," + pa.getId());
                }
            }
        }
        return flag;
    }

    @Override
    public JsonResultBean delete809ConnectionParamsSet(String id, String ipAddress) {
        boolean flag;
        if (StringUtils.isBlank(id)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        List<String> ids = Arrays.asList(id.split(","));
        //先删除平台下所有转发绑定关系
        String cids = connectionParamsConfigDao.findConfigUuidByPids(ids);
        if (StringUtils.isNotBlank(cids)) { //若不存在绑定关系则直接删除平台
            return new JsonResultBean(JsonResultBean.FAULT, "平台连接绑定了转发监控对象，不能删除");
        }
        //删除绑定关系成功则开始删除平台
        //获取要删除的平台信息
        List<PlantParam> plantParams = connectionParamsSetDao.get809ConnectionParamsByIds(ids);
        //删除平台信息
        flag = connectionParamsSetDao.delete809ConnectionParamsSet(ids);
        if (!flag) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        //如果删除成功，则下发删除809上级平台指令至F3
        StringBuilder message = new StringBuilder();
        for (PlantParam p : plantParams) {
            T809Message t809Message =
                MsgUtil.getT809Message(ConstantUtil.T809_PLATFORM_DELETE, p.getIp(), p.getCenterId(), null);
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809())
                .writeAndFlush(MsgUtil.getMsg(ConstantUtil.T809_PLATFORM_DELETE, t809Message));
            message.append("删除809转发平台 : ").append(p.getPlatformName()).append(" IP为 : ").append(p.getIp())
                .append(" <br/>");
        }
        if (ids.size() == 1) {
            logSearchService.addLog(ipAddress, message.toString(), "3", "809转发平台", "-", "");
        } else {
            logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "批量删除809转发平台");
        }
        for (PlantParam item : plantParams) {
            //如果设置了809报警参数  删除809报警设置并通知flink
            if (item.getMappingFlag() != null && item.getMappingFlag() == 1) {
                T809AlarmSetting setting = new T809AlarmSetting();
                setting.setSettingId(item.getId());
                connectionParamsSetDao.delete809AlarmMapping(setting);
                ZMQFencePub.pubChangeFence("19," + item.getId());
            }
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public List<PlantParam> get809ConnectionParamsSet(PlantParamQuery plantParamQuery) {
        List<PlantParam> plantParam = connectionParamsSetDao.get809ConnectionParamsSet(plantParamQuery);
        List<ProtocolInfo> protocolByType = protocolUtilDao.findProtocolByType(809);
        Map<String, ProtocolInfo> protocolInfoMap =
            protocolByType.stream().collect(Collectors.toMap(ProtocolInfo::getProtocolCode, Function.identity()));
        plantParam.forEach(p -> {
            p.setServerStatusName(getConnectType(p.getServerStatus(), 1));
            p.setMainClientName(getConnectType(p.getMainClient(), 2));
            p.setBranchServerName(getConnectType(p.getBranchServer(), 3));
            p.setMainStatusName(getMainStatusName(p.getMainStatus()));
            p.setBranchStatusName(getBranchStatusName(p.getBranchStatus()));
            String protocolName = protocolInfoMap.get(String.valueOf(p.getProtocolType())).getProtocolName();
            p.setProtocolTypeName(protocolName);
            p.setGroupPropertyName(getGroupPropertyName(p.getGroupProperty()));
        });
        return plantParam;
    }

    private String getMainStatusName(Integer status) {
        try {
            return MAIN_STATUS_NAME[status];
        } catch (Exception e) {
            return "";
        }
    }

    private String getBranchStatusName(Integer status) {
        try {
            return BRANCH_STATUS_NAME[status];
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public boolean checkedPlatFormCanOperate(String platFormId) {
        boolean flag = false;
        if (StringUtils.isNotBlank(platFormId)) {
            //查询当前平台参数
            List<String> platFormIds = Arrays.asList(platFormId.split(","));
            List<PlantParam> plantParams = connectionParamsSetDao.get809ConnectionParamsByIds(platFormIds);
            if (!plantParams.isEmpty()) {
                int num = 0;//记录可删除平台记录数量
                for (PlantParam p : plantParams) {
                    //检查平台个状态是否为关闭或断开
                    if (p.getServerStatus() == 0 && p.getMainClient() == 0 && p.getMainStatus() == 0
                        && p.getBranchServer() == 0 && p.getBranchStatus() == 0) {
                        num++;
                    }
                }
                //如果可删除平台数量与平台数量一致则予以删除权限
                if (plantParams.size() == num) {
                    flag = true;
                }
            }
        }
        return flag;
    }

    @Override
    public boolean check809PlatFormSole(String platFormName, String pid) {
        String chekedPid = connectionParamsSetDao.check809PlatFormSole(platFormName);
        //若查出的平台id就是当前平台id，则返回true
        if (StringUtils.isBlank(chekedPid)) { //若没有查出相关id，则返回true
            return true;
        } else {
            return StringUtils.isNotBlank(pid) && pid.equals(chekedPid);
        }
    }

    @Override
    public boolean check809ProtocolType(String protocolType, String pid) {
        String chekedPid = connectionParamsSetDao.check809ProtocolType(protocolType);
        //若查出的平台id就是当前平台id，则返回true
        if (StringUtils.isBlank(chekedPid)) { //若没有查出相关id，则返回true
            return true;
        } else {
            return StringUtils.isNotBlank(pid) && pid.equals(chekedPid);
        }
    }

    @Override
    public boolean check809Ip(T809PlantFormCheck param) {
        if (StringUtils.isNotBlank(param.getIp())) {
            //需要校验的ip
            String[] checkIp = param.getIp().split("#");
            //查出的已存在ip
            List<String> existIp = connectionParamsSetDao.get809Ip(param);
            if (existIp != null && !existIp.isEmpty()) {
                String ei = existIp.toString();
                for (String cip : checkIp) {
                    if (ei.contains("#" + cip + "#")) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean check809DateSole(T809PlantFormCheck param) {
        //若接入码没有填写则不校验
        if (param.getCenterId() == null) {
            return true;
        }
        //校验相同协议下数据唯一性
        if (param.getCenterId() != null) {
            List<String> ids = connectionParamsSetDao.getSolePlantParamId(param);
            if (ids == null || ids.isEmpty()) {
                return true;
            } else if (param.getPid() != null && ids.size() == 1) {
                return param.getPid().equals(ids.get(0));
            }
        }
        return false;
    }

    @Override
    public boolean check809CenterIdUnique(Integer centerId, String id) {
        List<String> settingIds = connectionParamsSetDao.get809IdsByCenterId(centerId, id);
        return CollectionUtils.isEmpty(settingIds);
    }

    @Override
    public JsonResultBean check809Unique(Integer centerId, String ip, String ipBranch, String id) {
        //将从链路IP字段中回车换行替换为‘#’
        String changeIpBranch = ipBranch.replaceAll("\r\n", "\n");
        String newIpBranch = changeIpBranch.replaceAll("\n", "#");
        List<String> branchIps = Arrays.asList(newIpBranch.split("#"));
        int branchSize = branchIps.size();

        // 校验多个IP时，最多输入10个
        if (branchSize > BRANCH_MAX_SIZE) {
            return new JsonResultBean(JsonResultBean.FAULT, "最多输入10个IP地址");
        }

        //校验多个IP
        if (branchSize > 1) {
            //判断是否有重复
            HashSet<String> set = new HashSet<>(branchIps);
            if (set.size() != branchSize) {
                return new JsonResultBean(JsonResultBean.FAULT, "从链路存在重复IP地址！");
            }

            //判断不能存在“0.0.0.0”
            if (branchIps.contains(BRANCH_DEFAULT)) {
                return new JsonResultBean(JsonResultBean.FAULT, "多个从链路IP时不能有(0.0.0.0)！");
            }
        }

        //校验数据库数据与传入参数是否有重复情况
        //获取除本身外其余所有的809设置信息
        List<PlantParam> plantParams = connectionParamsSetDao.get809ParamSet(id);
        if (CollectionUtils.isEmpty(plantParams)) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        //循环遍历组装 接入码_主IP_从IP 组合
        List<String> exists = new ArrayList<>();
        List<String> checkParams = new ArrayList<>();
        for (PlantParam param : plantParams) {
            String oldBranchIp = param.getIpBranch();
            String oldIp = param.getIp();
            Integer oldCenterId = param.getCenterId();
            if (null != oldBranchIp) {
                List<String> oldBranchIps = Arrays.asList(oldBranchIp.split("#"));
                oldBranchIps.forEach(o -> {
                    String combination = oldCenterId + "_" + oldIp + "_" + o;
                    exists.add(combination);
                });
            }
        }

        branchIps.forEach(o -> {
            String combination = centerId + "_" + ip + "_" + o;
            checkParams.add(combination);
        });

        //将传入的参数组合与数据库组合进行比对
        for (String check : checkParams) {
            if (exists.contains(check)) {
                return new JsonResultBean(JsonResultBean.FAULT, "保存失败，从链路IP与其他连接设置重复！");
            }
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public boolean checkPlatformNameUnique(String platformName, String id) {

        List<String> settingIds = connectionParamsSetDao.get809IdsByPlatformName(platformName, id);
        return CollectionUtils.isEmpty(settingIds);
    }

    @Override
    public void sendPlatformMsgAck(PlatformMsgAckInfo platformMsgAckInfo, String ipAddress) {
        if (platformMsgAckInfo == null) {
            return;
        }
        PlantParam plantParam = connectionParamsSetDao.getConnectionInfoById(platformMsgAckInfo.getPlatFormId());
        if (plantParam == null) {
            return;
        }
        //日志记录语句
        StringBuilder message = new StringBuilder();
        if (!platformMsgAckInfo.getMsgDataType().equals(0x9302)) {
            return;
        }
        // 平台间报文
        CheckAck ack = new CheckAck();
        int dataLength;
        if (plantParam.getProtocolType() == 100) { // 85658(809-2019)
            dataLength = 6;
            ack.setSourceMsgSn(platformMsgAckInfo.getMsgSn());
            ack.setSourceDataType(platformMsgAckInfo.getMsgID());
        } else { // 809-2011
            dataLength = 4;
            ack.setInfoId(platformMsgAckInfo.getInfoId());
        }
        ExchangeInfo info = new ExchangeInfo();
        info.setDataType(ConstantUtil.T809_UP_PLATFORM_MSG_INFO_ACK);
        info.setDataLength(dataLength);
        info.setData(MsgUtil.objToJson(ack));
        T809Message t809Message = MsgUtil
            .getT809Message(ConstantUtil.T809_UP_PLATFORM_MSG, plantParam.getIp(), plantParam.getCenterId(), info);
        Message resultMessage =
            MsgUtil.getMsg(ConstantUtil.T809_UP_PLATFORM_MSG, t809Message).assembleDesc809(plantParam.getId());
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(resultMessage);
        message.append("下发平台间报文应答");
        logSearchService.addLog(ipAddress, message.toString(), "3", "", "-", "");
    }

    /**
     * 查岗辅助方法
     */
    @Override
    public void assistOnInspection(SuperPlatformMsg msg, Message message) {
        if (inspectionEnable) {
            //添加一个3分钟的延时事件
            trigger.addEvent(3, TimeUnit.MINUTES, () -> {
                // 更新上级消息处理表状态
                superPlatformMsgService.updatePastData();
                // 获取上级平台消息处理表中的记录的处理状态,如果是未处理,则进行辅助运算
                Integer msgStatus = superPlatformMsgService.getMsgStatus(msg.getId());
                if (msgStatus == 0) {
                    T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
                    JSONObject t809MsgBody = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody()));
                    JSONObject t809MsgBodyData = t809MsgBody.getJSONObject("data");
                    // 获取消息中查询信息内容
                    String infoContent = t809MsgBodyData.getString("infoContent");
                    // 判断内容中是否有“=”号，如有进行截取等号之前部分
                    if (infoContent.contains("=")) {
                        infoContent = infoContent.substring(0, infoContent.indexOf("="));
                    }
                    // 进行公式计算
                    FormulaCalculatorResp resp = FormulaCalculator.getResult(infoContent);
                    // 判断计算结果，成功则另加一个（0-7分钟）的延时事件，进行自助应答
                    if (Boolean.TRUE.equals(resp.getIsSuccess())) {
                        //随机生成延时时间（秒）
                        long delay = new Random().nextInt(420);
                        //新增延时事件
                        trigger.addEvent(delay, TimeUnit.SECONDS, () -> {
                            // 更新上级消息处理表状态
                            superPlatformMsgService.updatePastData();
                            //判断当前上级平台消息处理表中的记录的处理状态，如还是未处理，则进行应答相关操作
                            Integer currentStatus = superPlatformMsgService.getMsgStatus(msg.getId());
                            if (currentStatus == 0) {
                                PlatformMsgAckInfo info = assembleAckInfo(message, resp);
                                // 更新上级平台消息处理表的记录
                                superPlatformMsgService.updateSuperPlatformMsg(info.getGangId(), 1, info.getAnswer());
                                // 9301查岗应答转发809
                                standard809GangAck(info);
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * 组装PlatformMsgAckInfo实体
     */
    private PlatformMsgAckInfo assembleAckInfo(Message message, FormulaCalculatorResp resp) {
        PlatformMsgAckInfo ackInfo = new PlatformMsgAckInfo();
        T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
        JSONObject t809MsgBody = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody()));
        JSONObject t809MsgBodyData = t809MsgBody.getJSONObject("data");
        T809MsgHead t809MsgHead = t809Message.getMsgHead();
        ackInfo.setInfoId(t809MsgBodyData.getInteger("infoId"));
        ackInfo.setObjectId(t809MsgBodyData.getString("objectId"));
        ackInfo.setObjectType(t809MsgBodyData.getInteger("objectType"));
        ackInfo.setMsgDataType(t809MsgBody.getInteger("dataType"));
        //对计算结果进行判断
        Double resultValue = resp.getResultValue();
        if (resultValue % 1 == 0) {
            //为整，取整
            ackInfo.setAnswer(Integer.toString(resultValue.intValue()));
        } else {
            //有小数，保留2位小数（四舍五入）
            DecimalFormat df = new DecimalFormat("#.00");
            ackInfo.setAnswer(df.format(resultValue));
        }
        ackInfo.setServerIp(t809MsgHead.getServerIp());
        ackInfo.setMsgGNSSCenterId(t809MsgHead.getMsgGNSSCenterId());
        ackInfo.setGroupId(t809MsgHead.getGroupId());
        ackInfo.setGangId(t809MsgHead.getHandleId());
        ackInfo.setMsgSn(t809MsgHead.getMsgSn());
        ackInfo.setMsgID(t809MsgHead.getMsgID());
        ackInfo.setPlatFormId(message.getDesc().getT809PlatId());
        return ackInfo;
    }

    @Override
    public List<String> getGroupId(String centerId, String ip) {
        if (StringUtils.isNotBlank(ip)) {
            return connectionParamsSetDao.getGroupId(centerId, ip);
        }
        return Collections.emptyList();
    }

    /**
     * 处理标准809报警督办
     */
    @Override
    public Integer sendFormAlarmAck(PlatformAlarmInfo info, String ipAddress) throws Exception {
        // 返回给前端的处理结果标识
        int resultMsg;
        // 报警时间
        Long warnTime = info.getWarnTime() * 1000;
        info.setWarnTime(warnTime);
        // 监控对象id
        String monitorId = info.getMonitorId();
        Map<String, Object> statusInfo = t809AlarmIsHandle(info);
        int handleStatus =
            statusInfo.get("status") != null ? Integer.parseInt(String.valueOf(statusInfo.get("status"))) : 5;
        StringBuilder message = new StringBuilder();
        String handleMsg;
        switch (handleStatus) {
            case 0: // 未处理
                // 更新报警处理表的记录的处理状态
                Long alarmStartTime = info.getAlarmStartTime();
                Integer alarmType = info.getAlarmType();
                String result = updateSuperPlatformMsgStatus(info.getAlarmMsgId(), info.getAlarmHandle());
                // 主动上报逻辑已经移到报
                // 在更新报警处理表的处理状态之前,先主动上报报警处理结果
                sendT809AlarmMsg(info);
                //跟新alarm_handle表的状态或者主动安全es报警的状态
                updateAlarmStatus(monitorId, alarmType, alarmStartTime, result, info.getEventId(),
                    info.getAlarmHandle());
                generalHandleAck(info);
                handleMsg = result;
                resultMsg = 0;
                // 督办报警抽查处理
                if (RedisHelper.isContainsKey(RedisKeyEnum.MONITOR_INFO.of(monitorId))) {
                    saveAlarmSupervisory(warnTime, monitorId, alarmType);
                }
                break;
            case 1: // 已处理
                Object handleTypStr = Optional.ofNullable(statusInfo.get("handleType")).orElse("0");
                Integer handleType = Integer.parseInt(String.valueOf(handleTypStr));
                updateSuperPlatformMsgStatus(info.getAlarmMsgId(), handleType);
                handleMsg = "消息已应答";
                resultMsg = 1;
                break;
            case 2:  // 已过期
                // 返回提示
                handleMsg = "督办消息已过期";
                resultMsg = 2;
                break;
            case 3:
                updateSuperPlatformMsgStatus(info.getAlarmMsgId(), info.getAlarmHandle());
                generalHandleAck(info);
                handleMsg = "未查询到报警信息";
                resultMsg = 3;
                break;
            default:
                handleMsg = "未查询到上级平台督办记录";
                resultMsg = 4;
                break;
        }
        message.append("督办报警：编号(").append(info.getMsgSn()).append(")，处理结果：").append(handleMsg);
        logSearchService.addLog(ipAddress, message.toString(), "3", "", "-", "");
        return resultMsg;
    }

    /**
     * 报警督办抽查存储
     * @param warnTime  warnTime
     * @param monitorId monitorId
     * @param alarmType alarmType
     */
    private void saveAlarmSupervisory(Long warnTime, String monitorId, Integer alarmType) {
        AlarmHandle alarmHandle;
        switch (alarmType) {
            case 76:
                alarmHandle = getCurrentAlarmInfo(warnTime, alarmType, monitorId);
                // 四川标准超速报警
                if (Objects.nonNull(alarmHandle)) {
                    Integer calStandard = alarmHandle.getCalStandard();
                    if (Objects.nonNull(calStandard) && calStandard == 2) {
                        addVehicleSpotCheckInfo(monitorId, warnTime, alarmHandle);
                    }
                }
                break;
            case 77:
                // 疲劳驾驶
                String jsonStr = RedisHelper.getString(HistoryRedisKeyEnum.MONITOR_ALARM_PARAM.of(monitorId, "77"));
                if (StringUtils.isBlank(jsonStr)) {
                    break;
                }
                JSONObject jsonObject = JSON.parseObject(jsonStr);
                String calStandard = jsonObject.getString("calStandard");
                if (!"2".equals(calStandard)) {
                    break;
                }
                alarmHandle = getCurrentAlarmInfo(warnTime, alarmType, monitorId);
                if (Objects.nonNull(alarmHandle)) {
                    addVehicleSpotCheckInfo(monitorId, warnTime, alarmHandle);
                }
                break;
            case 79:
                // 异动报警
            case 129:
                // 音视频客车超员
            case 7702:
                // 客运车辆禁行7702
            case 7703:
                // 山区公路禁行7703
            case 147:
                alarmHandle = getCurrentAlarmInfo(warnTime, alarmType, monitorId);
                if (Objects.nonNull(alarmHandle)) {
                    addVehicleSpotCheckInfo(monitorId, warnTime, alarmHandle);
                }
                break;
            default:
                break;
        }
    }

    private AlarmHandle getCurrentAlarmInfo(Long warnTime, Integer alarmType, String monitorId) {
        Map<String, String> params = new HashMap<>(8);
        params.put("monitorId", monitorId);
        params.put("alarmType", String.valueOf(alarmType));
        params.put("startTime", String.valueOf(warnTime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_CURRENT_ALARM_INFO, params);
        return PaasCloudUrlUtil.getResultData(str, AlarmHandle.class);
    }

    /**
     * 报警处理新增抽查
     * @param vehicleId   vehicleId
     * @param startTimeL  startTimeL
     * @param alarmHandle alarmHandle
     */
    private void addVehicleSpotCheckInfo(String vehicleId, long startTimeL, AlarmHandle alarmHandle) {
        VehicleSpotCheckInfo vehicleSpotCheckInfo = new VehicleSpotCheckInfo();
        vehicleSpotCheckInfo.setVehicleId(vehicleId);
        vehicleSpotCheckInfo.setLocationTime(new Date(startTimeL));
        Double speedLimit = alarmHandle.getSpeedLimit();
        vehicleSpotCheckInfo.setSpeedLimit(speedLimit != null ? String.valueOf(speedLimit) : null);
        vehicleSpotCheckInfo.setSpeed(alarmHandle.getSpeed());
        String alarmStartLocation = alarmHandle.getAlarmStartLocation();

        if (StringUtils.isNotEmpty(alarmStartLocation)) {
            String[] alarmStartLocations = alarmStartLocation.split(",");
            vehicleSpotCheckInfo.setLongtitude(alarmStartLocations[0]);
            vehicleSpotCheckInfo.setLatitude(alarmStartLocations[1]);
        }
        vehicleSpotCheckInfo.setSpotCheckContent(VehicleSpotCheckInfo.SPOT_CHECK_CONTENT_DEAL_ALARM);
        vehicleSpotCheckInfo.setSpotCheckUser(SystemHelper.getCurrentUsername());
        Date date = new Date();
        vehicleSpotCheckInfo.setSpotCheckTime(date);
        vehicleSpotCheckInfo.setActualViewDate(date);
        spotCheckReportDao.addVehicleSpotCheckInfo(vehicleSpotCheckInfo);
    }

    /**
     * 将报警转发表的报警记录都上报
     */
    private void sendT809AlarmMsg(PlatformAlarmInfo info) throws Exception {
        //808报警已经已经全部移动到webapi端，所以这里只处理主动安全报警
        String eventId = info.getEventId();
        if (StringUtils.isBlank(eventId)) {
            return;
        }
        // 组装14000消息
        Integer alarmType = info.getAlarmType();
        String monitorId = info.getMonitorId();
        Integer handleResult = info.getAlarmHandle();
        Integer handleId = info.getSourceMsgSn(); // 当前处理的报文序列号
        // 报警时间
        Long alarmStartTime = info.getWarnTime();
        AlarmHandleParam handleParam = AlarmHandleParam
            .getInstance(alarmType, monitorId, alarmStartTime, handleResult + "", handleId, eventId, null);
        initiativeSendAlarmHandle(handleParam);
    }

    /**
     * 报警督办应答
     * 收到标准809督办时,如果报警已经被处理,回复督办应答0x1401
     * (不区分协议类型,809-2011和809-2019 两个版本协议的应答内容全部都组装)
     */
    private void generalHandleAck(PlatformAlarmInfo info) {
        if (info == null) {
            return;
        }
        PlantParam plantParam = connectionParamsSetDao.getConnectionInfoById(info.getPlateFormId());
        if (plantParam == null) {
            return;
        }
        Zw809MessageDO zw809MessageDO = superPlatformMsgService.get809Message(info.getAlarmMsgId());
        if (zw809MessageDO == null) {
            return;
        }
        PlatformAlarmAck ack = PlatformAlarmAck.getInstance(zw809MessageDO, info);
        SupervisionAlarmInfo supervisionAlarmInfo = new SupervisionAlarmInfo();
        Integer color = info.getVehicleColor();
        if (color == null) {
            color = 2;
        }
        supervisionAlarmInfo.setVehicleColor(color);
        supervisionAlarmInfo.setData(MsgUtil.objToJson(ack));
        Integer connectProtocolType = plantParam.getProtocolType();
        if (CONNECT_PROTOCOL_TYPE_808_2019.contains(connectProtocolType)) {
            // 后续数据长度(2019-809 固定为7)
            supervisionAlarmInfo.setDataLength(7);
        } else {
            // 后续数据长度(2019-809 固定为5)
            supervisionAlarmInfo.setDataLength(5);
        }
        supervisionAlarmInfo.setDataType(ConstantUtil.T809_UP_WARN_MSG_URGE_TODO_ACK);

        supervisionAlarmInfo.setVehicleNo(info.getBrand());
        T809Message message = MsgUtil
            .getT809Message(ConstantUtil.T809_UP_WARN_MSG, plantParam.getIp(), plantParam.getCenterId(),
                supervisionAlarmInfo);
        Message t809Message =
            MsgUtil.getMsg(ConstantUtil.T809_UP_WARN_MSG, message).assembleDesc809(info.getPlateFormId());
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(t809Message);
        // 809-2019版协议下发0x1411: 是下级监管往上级平台发送数据, 但是我们是下级平台.
        // send0x1411Msg(plantParam, info);
    }

    /**
     * 下发0x1411消息
     */
    private void send0x1411Msg(PlantParam plantParam, PlatformAlarmInfo info) {
        if (plantParam == null || info == null) {
            return;
        }
        Integer connectProtocolType = plantParam.getProtocolType();
        // 809-2019版协议
        if (CONNECT_PROTOCOL_TYPE_808_2019.contains(connectProtocolType)) {
            PlatformAlarmAck ack = new PlatformAlarmAck();
            // 报警督办应答还需下发0x1411
            ack.setSourceDataType(info.getSourceDataType());
            ack.setSourceMsgSn(info.getSourceMsgSn());
            ack.setResult(info.getAlarmHandle());
            SupervisionAlarmInfo supervisionAlarmInfo = new SupervisionAlarmInfo();
            supervisionAlarmInfo.setData(MsgUtil.objToJson(ack));
            supervisionAlarmInfo.setDataType(ConstantUtil.T809_UP_WARN_MSG_URGE_TODO_ACK_INFO);
            supervisionAlarmInfo.setDataLength(7);
            T809Message t809Msg = MsgUtil
                .getT809Message(ConstantUtil.T809_UP_WARN_MSG, plantParam.getIp(), plantParam.getCenterId(),
                    supervisionAlarmInfo);
            Message msg = MsgUtil.getMsg(ConstantUtil.T809_UP_WARN_MSG, t809Msg).assembleDesc809(info.getPlateFormId());
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(msg);
        }
    }

    /**
     * 更新报警处理状态
     */
    private void updateAlarmStatus(String monitorId, Integer alarmTypeInt, Long startTime, String handleType,
        String eventId, Integer alarmHandle) throws Exception {
        if (StrUtil.isNotBlank(eventId)) {
            //更新主动安全中es中的报警
            try {
                adasRiskService.saveRiskDealInfos(AdasAlarmDealInfo.getInstance(eventId, handleType));
            } catch (Exception e) {
                logger.error("批量处理主动安全报警失败！", e);
            }
        } else {
            //更新alarm_handle表中的报警
            updateAlarmHandleStatus(monitorId, alarmTypeInt, startTime, alarmHandle);
        }

    }

    private void updateAlarmHandleStatus(String monitorId, Integer alarmTypeInt, Long startTime, Integer alarmHandle)
        throws Exception {
        HandleAlarms handleAlarms = new HandleAlarms();
        handleAlarms.setVehicleId(monitorId);
        if (alarmHandle != null) {
            String handleType = null;
            if (alarmHandle == 0) {
                handleType = "6";
            }
            if (alarmHandle == 1) {
                handleType = "7";
            }
            if (alarmHandle == 2) {
                handleType = "4";
            }
            if (alarmHandle == 3) {
                handleType = "5";
            }
            handleAlarms.setHandleType(handleType);
        }
        handleAlarms.setRemark(null);
        handleAlarms.setDealOfMsg(null);
        alarmSearchService.handleAlarmSingle(handleAlarms, startTime, String.valueOf(alarmTypeInt), false);
    }

    /**
     * 报警督办(判断该条报警是否在报警转发表和报警处理表中存在)
     */
    private Map<String, Object> t809AlarmIsHandle(PlatformAlarmInfo info) {
        // 报警处理状态(0:未处理 1:已处理 2 已过期 3:未查询到报警记录 4 未查询到督办记录)
        Integer handleStatus = 3;
        Map<String, Object> result = new HashMap<>();
        // 获取上级平台消息处理表中的记录的处理状态,如果是已过期的,就不做任何处理
        Integer msgStatus = superPlatformMsgService.getMsgStatus(info.getAlarmMsgId());
        if (msgStatus == null) {
            // 未查询到督办记录
            handleStatus = 4;
        } else if (msgStatus == 1) {
            //  督办消息已应答
            handleStatus = 1;
        } else if (msgStatus == 2) {
            //  督办消息已过期
            handleStatus = 2;
        }
        handleStatus = getHandleStatus(info, handleStatus, msgStatus);
        result.put("status", handleStatus);
        return result;
    }

    private Integer getHandleStatus(PlatformAlarmInfo info, Integer handleStatus, Integer msgStatus) {
        String monitorId = info.getMonitorId();
        String eventId = info.getEventId();
        // 未处理状态才进行报警记录查询
        if (StrUtil.isNotBlank(monitorId) && (msgStatus != null && msgStatus == 0)) {
            //判断是否为主动安全报警（存储到es中）
            if (StrUtil.isNotBlank(eventId)) {
                Integer status = adasEsService.esGetRiskEventById(eventId).getStatus();
                if (AdasRiskStatus.TREATED.getCode().equals(status)) {
                    // 报警已处理
                    handleStatus = 1;
                } else {
                    // 报警未处理
                    handleStatus = 0;
                }
            } else {
                handleStatus = getAlarmHandleStatus(info, handleStatus, monitorId);
            }

        }
        return handleStatus;
    }

    private Integer getAlarmHandleStatus(PlatformAlarmInfo info, Integer handleStatus, String monitorId) {
        // 先根据监控对象id,报警类型，报警时间查询报警记录,获取处理结果
        // 报警开始时间
        Long alarmStartTime = info.getAlarmStartTime();
        // 报警编号
        Integer alarmType = info.getAlarmType();
        // 再查询报警记录
        AlarmHandle alarmHandle = getAlarmInfoByMonitorId(monitorId, alarmStartTime, alarmType);
        if (alarmHandle != null) {
            // 报警处理结果
            int status = alarmHandle.getStatus();
            // 报警未处理
            if (status == 0) {
                handleStatus = 0;
            } else { // 报警已处理
                handleStatus = 1;
            }
        }
        return handleStatus;
    }

    private AlarmHandle getAlarmInfoByMonitorId(String monitorId, Long alarmStartTime, Integer alarmType) {
        Map<String, String> params = new HashMap<>(8);
        params.put("monitorId", monitorId);
        params.put("alarmType", String.valueOf(alarmType));
        params.put("startTime", String.valueOf(alarmStartTime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_ALARM_INFO_BY_MONITOR_ID, params);
        return PaasCloudUrlUtil.getResultData(str, AlarmHandle.class);
    }

    private String updateSuperPlatformMsgStatus(String msgId, Integer handleResult) {
        String result = "";
        switch (handleResult) {
            case 0:
                result = "处理中";
                break;
            case 1:
                result = "已处理完毕";
                break;
            case 2:
                result = "不做处理";
                break;
            case 3:
                result = "将来处理";
                break;
            default:
                break;
        }
        // 更新上级平台消息处理表对应记录的处理状态和处理结果
        superPlatformMsgService.updateSuperPlatformMsg(msgId, 1, result);
        return result;
    }

    /**
     * 获取监控对象绑定的所有转发平台
     */
    @Override
    public List<PlantParam> getMonitorPlatform(String monitorId) {
        // 获取监控对象绑定id
        ConfigDO configDO = configDao.getByMonitorId(monitorId);
        if (Objects.isNull(configDO)) {
            return new ArrayList<>();
        }
        // 查询监控对象绑定的转发平台
        return connectionParamsSetDao.getPlatformInfoByMonitorConfigId(configDO.getId());
    }

    /**
     * 获取转发平台id和groupId
     */
    @Override
    public List<PlantParam> getPlatformFlag(String centerId, String ip) {
        if (StringUtils.isNotBlank(ip)) {
            return connectionParamsSetDao.getPlatformFlag(centerId, ip);
        }
        return new ArrayList<>();
    }

    /**
     * 获取转发平台groupId
     */
    @Override
    public PlantParam getPlatformInfoById(String id, String serviceIp, String centerId) {
        if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(serviceIp) && StringUtils.isNotBlank(centerId)) {
            return connectionParamsSetDao.getPlatformGroupId(id, serviceIp, centerId);
        }
        return null;
    }

    /**
     * 查岗
     */
    @Override
    public Integer sendPlatformGangAck(PlatformMsgAckInfo info, String ipAddress) {
        String gangId = info.getGangId();
        // 获取上级平台消息处理表中的记录的处理状态,如果是已过期的,就不做任何处理
        Integer msgStatus = superPlatformMsgService.getMsgStatus(gangId);
        if (msgStatus == null) {
            msgStatus = 3;
        }
        int resultMsg; // 返回给前端的处理结果标识(1:已处理 2:已过期 3:未找到该条消息)
        String msg;
        switch (msgStatus) {
            case 0: // 未处理
                // 更新上级平台消息处理表的记录
                superPlatformMsgService.updateSuperPlatformMsg(gangId, 1, info.getAnswer());
                gangAck(info);
                resultMsg = 0;
                msg = "已处理";
                break;
            case 1: // 已处理
                resultMsg = 1;
                msg = "消息已应答";
                break;
            case 2: // 已过期
                resultMsg = 2;
                msg = "查岗消息已过期";
                break;
            default:
                resultMsg = 3;
                msg = "未查询到查岗记录";
                break;
        }
        StringBuilder message = new StringBuilder();
        message.append("平台查岗：编号(").append(info.getInfoId()).append(")，处理结果：").append(msg);
        if (resultMsg != 2 && resultMsg != 3) {
            message.append("，应答内容：").append(info.getAnswer());
        }
        logSearchService.addLog(ipAddress, message.toString(), "3", "", "-", "");
        return resultMsg;
    }

    /**
     * 下发查岗应答消息
     */
    private void gangAck(PlatformMsgAckInfo info) {
        Integer dataType = info.getMsgDataType();
        switch (dataType) {
            case 0x9301: // 标准809查岗
                standard809GangAck(info);
                break;
            case 0x9305: // 西藏809查岗
                extend809GangAck(info);
                break;
            default:
                break;
        }
    }

    /**
     * 标准809查岗应答
     */
    private void standard809GangAck(PlatformMsgAckInfo info) {
        if (info == null) {
            return;
        }
        PlantParam plantParam = connectionParamsSetDao.getConnectionInfoById(info.getPlatFormId());
        if (plantParam == null) {
            return;
        }
        ExchangeInfo exchangeInfo = new ExchangeInfo();
        CheckAck ack = new CheckAck();
        //川标 沪标 查岗应答
        if (plantParam.getProtocolType() == Integer.parseInt(ProtocolTypeUtil.T809_SI_CHUAN_PROTOCOL_809_2013)) {
            ack.setInfoId(info.getInfoId());
            ack.setObjectId(info.getObjectId());
            setObjetType(info, plantParam, ack);
            ack.setInfoLength(info.getAnswer().getBytes(Charset.forName("GBK")).length);
            ack.setInfoContent(info.getAnswer());
            exchangeInfo.setData(MsgUtil.objToJson(ack));
            exchangeInfo.setDataType(ConstantUtil.T809_UP_PLATFORM_MSG_POST_QUERY_ACK);
            exchangeInfo.setDataLength(29 + info.getAnswer().length());
        } else {
            ack.setInfoId(info.getInfoId());
            ack.setInfoLength(info.getAnswer().getBytes(Charset.forName("GBK")).length);
            ack.setInfoContent(info.getAnswer());
            ack.setObjectId(info.getObjectId());
            ack.setSourceDataType(info.getMsgDataType());
            ack.setSourceMsgSn(info.getMsgSn());
            setObjetType(info, plantParam, ack);
            // 组装应答人和应答人电话
            UserLdap userLdap = SystemHelper.getCurrentUser();
            if (userLdap != null) {
                ack.setResponder(userLdap.getUsername());
                ack.setResponderTel(userLdap.getMobile());
            } else {
                ack.setResponder("admin");
                ack.setResponderTel("");
            }
            exchangeInfo.setData(MsgUtil.objToJson(ack));
            exchangeInfo.setDataType(ConstantUtil.T809_UP_PLATFORM_MSG_POST_QUERY_ACK);
            int dataLength;
            if (plantParam.getProtocolType() == 100) { // 35658(809-2011)
                dataLength = 67 + ack.getInfoLength();
            } else { // 809-2011
                dataLength = 8 + ack.getInfoLength();
            }
            exchangeInfo.setDataLength(dataLength);
        }
        T809Message msg = MsgUtil
            .getT809Message(ConstantUtil.T809_UP_PLATFORM_MSG, plantParam.getIp(), plantParam.getCenterId(),
                exchangeInfo);
        Message t809Message =
            MsgUtil.getMsg(ConstantUtil.T809_UP_PLATFORM_MSG, msg).assembleDesc809(info.getPlatFormId());
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(t809Message);
    }

    private void setObjetType(PlatformMsgAckInfo info, PlantParam plantParam, CheckAck ack) {
        //组装809连接平台查询参数
        Integer objectType = info.getObjectType();
        switch (objectType) {
            case 1: // 当前连接的下级平台(objectId,由平台行政区划代码和平台唯一编码组成)
                ack.setObjectId(plantParam.getZoneDescription() + plantParam.getCenterId());
                break;
            case 2: // 下级平台所属单一业户(objectId,为业户经营许可证号)
            case 3: // 下级平台所属所有业户(objectId,为所有业户的经营许可证号)
                OrganizationLdap orgInfo = organizationService.getOrganizationByUuid(info.getGroupId());
                if (orgInfo != null) { // 经营许可证号
                    ack.setObjectId(orgInfo.getLicense());
                }
                break;
            default:
                break;
        }
        ack.setObjectType(info.getObjectType());
    }

    /**
     * 西藏扩展809查岗应答
     */
    private void extend809GangAck(PlatformMsgAckInfo info) {
        ExtendGngAck ack = new ExtendGngAck();
        ack.setInfoId(info.getInfoId());
        ack.setInfoLength(info.getAnswer().getBytes(Charset.forName("GBK")).length);
        ack.setInfoContent(info.getAnswer());
        ack.setDutyman(SystemHelper.getCurrentUsername());
        //组装809连接平台查询参数
        T809PlantFormCheck queryParam = new T809PlantFormCheck();
        queryParam.setCenterId(info.getMsgGNSSCenterId());
        queryParam.setGroupId(info.getGroupId());
        queryParam.setIp(info.getServerIp());
        ExchangeInfo exchangeInfo = new ExchangeInfo();
        exchangeInfo.setData(MsgUtil.objToJson(ack));
        exchangeInfo.setDataType(ConstantUtil.T809_UP_ENTERPRISE_ON_DUTY_ACK);
        exchangeInfo.setDataLength(ack.getInfoLength());
        PlantParam param = get809ConnectParam(info.getServerIp(), info.getMsgGNSSCenterId());
        if (param != null) {
            T809Message message = MsgUtil
                .getT809Message(ConstantUtil.T809_UP_PLATFORM_MSG, info.getServerIp(), info.getMsgGNSSCenterId(),
                    exchangeInfo);
            Message t809Message =
                MsgUtil.getMsg(ConstantUtil.T809_UP_PLATFORM_MSG, message).assembleDesc809(param.getId());
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(t809Message);
        }
    }

    /**
     * 西藏809督办消息应答
     */
    @Override
    public Integer sendExtendHandleAck(ExtendPlatformMsgInfo info, String ipAddress) {
        // 获取上级平台消息处理表中的记录的处理状态,如果是已过期的,就不做任何处理
        Integer msgStatus = superPlatformMsgService.getMsgStatus(info.getMsgId());
        if (msgStatus == null) {
            msgStatus = 3;
        }
        int resultMsg; // 返回给前端的处理结果标识(1:已处理 2:已过期 3:未找到该条消息)
        String msg;
        switch (msgStatus) {
            case 0:
                updateSuperPlatformMsgStatus(info.getMsgId(), info.getResult()); // 更新上级平台消息处理表记录
                extendHandleAck(info); // 下发应答
                resultMsg = 0;
                msg = "已处理";
                break;
            case 1:
                resultMsg = 1;
                msg = "消息已应答";
                break;
            case 2:
                resultMsg = 2;
                msg = "消息已过期";
                break;
            default:
                resultMsg = 3;
                msg = "未查询到督办记录";
                break;
        }
        logSearchService.addLog(ipAddress, "西藏809督办：编号(" + info.getInfoId() + ")，处理结果:" + msg, "3", "", "-", "");
        return resultMsg;
    }

    /**
     * 收到标准809督办时,如果报警已经被处理,回复督办应答0x1306
     */
    private void extendHandleAck(ExtendPlatformMsgInfo info) {
        Extend809AlarmAck ack = new Extend809AlarmAck();
        SupervisionAlarmInfo supervisionAlarmInfo = new SupervisionAlarmInfo();
        ack.setInfoId(info.getInfoId());
        ack.setResult(info.getResult()); // 处理结果
        ack.setDutyman(SystemHelper.getCurrentUsername()); // 督办响应用户
        supervisionAlarmInfo.setData(MsgUtil.objToJson(ack));
        supervisionAlarmInfo.setDataType(ConstantUtil.T809_UP_ENTERPRISE_HANDLE_ACK);
        supervisionAlarmInfo.setDataLength(String.valueOf(info.getResult()).length());
        PlantParam param = get809ConnectParam(info.getServerIp(), info.getMsgGNSSCenterId());
        if (param != null) {
            T809Message message = MsgUtil
                .getT809Message(ConstantUtil.T809_UP_PLATFORM_MSG, info.getServerIp(), info.getMsgGNSSCenterId(),
                    supervisionAlarmInfo);
            Message t809Message =
                MsgUtil.getMsg(ConstantUtil.T809_UP_PLATFORM_MSG, message).assembleDesc809(param.getId());
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(t809Message);
        }
    }

    /**
     * 根据IP和接入码查询809连接参数
     */
    private PlantParam get809ConnectParam(String serverIp, Integer msgGNSSCenterId) {
        List<PlantParam> plantParam = getPlatformFlag(msgGNSSCenterId.toString(), serverIp);
        if (plantParam != null && !plantParam.isEmpty()) {
            return plantParam.get(0);
        }
        return null;
    }

    /**
     * 809报警设置
     */
    @Override
    public JsonResultBean add809AlarmMapping(T809AlarmSetting t809AlarmSetting, String ip) {
        List<T809AlarmMapping> list = new ArrayList<>();
        String id = t809AlarmSetting.getSettingId();
        List<String> ids = new ArrayList<>();
        ids.add(id);
        //查询平台信息
        List<PlantParam> plantParam = connectionParamsSetDao.get809ConnectionParamsByIds(ids);
        Integer protocol = t809AlarmSetting.getProtocolType();
        String json = t809AlarmSetting.getAlarmJson();
        List<AlarmSettingBean> result = JSON.parseArray(json, AlarmSettingBean.class);
        //存放映射 key:809,time  value:808
        Map<String, Set<Integer>> map = prepareAlarmMap(result);
        for (Map.Entry<String, Set<Integer>> entries : map.entrySet()) {
            String pos809 = entries.getKey();
            Set<Integer> pos808s = entries.getValue();
            for (Integer pos808 : pos808s) {
                //组装数据
                set808to809(list, pos809, t809AlarmSetting, pos808.toString());
            }
        }
        //先删除之前设置
        connectionParamsSetDao.delete809AlarmMapping(t809AlarmSetting);
        //新增
        if (!list.isEmpty()) {
            connectionParamsSetDao.add809AlarmMapping(list);
        } else {
            //平台删除完报警设置  添加不能使用的数据供flink判断
            T809AlarmMapping t809AlarmMapping = new T809AlarmMapping();
            t809AlarmMapping.setSettingId(id);
            t809AlarmMapping.setProtocolType(protocol);
            t809AlarmMapping.setPos808("-2");
            t809AlarmMapping.setPos809("0x0000");
            t809AlarmMapping.setTime(1);
            t809AlarmMapping.setCreateDataUsername(SystemHelper.getCurrentUsername());
            list.add(t809AlarmMapping);
            connectionParamsSetDao.add809AlarmMapping(list);
        }
        //zw_m_809_setting表809_mapping_flag字段置1
        connectionParamsSetDao.update809AlarmMapping(t809AlarmSetting);
        logSearchService.addLog(ip, "修改平台【" + plantParam.get(0).getPlatformName() + "】809报警设置", "3", "", "-", "");
        //通知flink809报警设置发送改变
        ZMQFencePub.pubChangeFence("19," + id);
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    private Map<String, Set<Integer>> prepareAlarmMap(List<AlarmSettingBean> result) {
        Map<String, Set<Integer>> map = new HashMap<>();
        if (result != null && !result.isEmpty()) {
            for (AlarmSettingBean bean : result) {
                if (StringUtils.isEmpty(bean.getPos808())) {
                    continue;
                }
                Set<Integer> pos808s = new HashSet<>();
                String[] pos = bean.getPos808().split(",");
                for (String pos808 : pos) {
                    switch (pos808) {
                        case "125":  //视频报警需要设置通道号
                        case "126":
                        case "1271":
                        case "1272":
                        case "130":
                            Collection<Integer> alarmTypes = getAlarmPos(pos808);
                            pos808s.addAll(alarmTypes);
                            break;
                        default:
                            pos808s.add(Integer.parseInt(pos808));
                            break;
                    }
                }
                map.put(bean.getPos809() + "," + bean.getTime(), pos808s);
            }
        }
        return map;
    }

    private void set808to809(List<T809AlarmMapping> list, String timeAnd809, T809AlarmSetting t809AlarmSetting,
        String pos808) {
        String[] items = timeAnd809.split(",");
        T809AlarmMapping t809AlarmMapping = new T809AlarmMapping();
        t809AlarmMapping.setSettingId(t809AlarmSetting.getSettingId());
        t809AlarmMapping.setProtocolType(t809AlarmSetting.getProtocolType());
        t809AlarmMapping.setPos808(pos808);
        t809AlarmMapping.setPos809(items[0]);
        t809AlarmMapping.setTime(Integer.parseInt(items[1]));
        t809AlarmMapping.setCreateDataUsername(SystemHelper.getCurrentUsername());
        list.add(t809AlarmMapping);
    }

    /**
     * 获取报警类型list
     * @param alarmType 报警类型字符串
     */
    private Collection<Integer> getAlarmPos(String alarmType) {
        AlarmMorePos alarmMorePos = AlarmMorePos.valueOf("pos_" + alarmType);
        return new HashSet<>(alarmMorePos.getMorePos());
    }

    @Override
    public Map<String, String> get809AlarmMapping(T809AlarmSetting t809AlarmSetting) {
        String id = t809AlarmSetting.getSettingId();
        Integer protocol = t809AlarmSetting.getProtocolType();
        //放809映射
        Map<String, String> map = new HashMap<>();
        //查询平台信息
        Integer flag = connectionParamsSetDao.get809Mapping(id, protocol);
        if (flag != null && flag == 1) {
            //平台的此协议设置了报警映射
            List<AlarmSettingBean> list = connectionParamsSetDao.get809AlarmMapping(t809AlarmSetting);
            map.put("flag", "1");
            map.put("result", JSON.toJSONString(list));
        } else {
            map.put("flag", "0");
        }
        return map;
    }

    @Override
    public List<AlarmType> getAlarmType(Integer protocolType) {
        List<AlarmType> alarms = alarmDao.getAlarmType("");
        List<AlarmType> alarmTypeList = new ArrayList<>();
        String[] arrPos = new String[] { "125", "126", "1271", "1272", "130" };
        String[] arrName = new String[] { "视频信号丢失", "视频信号遮挡", "主存储器故障", "灾备存储器故障", "异常驾驶行为" };
        // 其他809需要隐藏的报警(35658-809新增报警)
        // 违规行驶报警、前撞报警、车道偏离报警、胎压异常报警、右转盲区异常报警、危险驾驶行为报警、碰撞侧翻报警
        List<String> otherProtocolHideAlarm = Arrays.asList("15", "16", "17", "148", "149", "157", "158");
        for (int i = 0; i < arrPos.length; i++) {
            AlarmType alarmType = new AlarmType();
            alarmType.setPos(arrPos[i]);
            alarmType.setName(arrName[i]);
            alarmType.setType("videoAlarm");
            alarmTypeList.add(alarmType);
        }
        if (protocolType != 100) {
            alarms =
                alarms.stream().filter(e -> !otherProtocolHideAlarm.contains(e.getPos())).collect(Collectors.toList());
        }
        for (AlarmType alarm : alarms) {
            //长时间下线属于平台报警移除，过滤掉主动安全的报警
            if ("长时间下线".equals(alarm.getName()) || "adasAlarm".equals(alarm.getType())) {
                continue;
            }
            switch (alarm.getType()) {
                case "alert": // 预警
                case "vehicleAlarm": // 车辆报警
                case "faultAlarm": // 故障报警
                case "driverAlarm": // 驾驶员报警
                case "platAlarm": // 平台报警
                    alarmTypeList.add(alarm);
                    break;
                case "videoAlarm": // 视频报警
                    if (!alarm.getName().contains("视频信号丢失") && !alarm.getName().contains("视频信号遮挡") && !alarm.getName()
                        .contains("主存储器") && !alarm.getName().contains("灾备存储器") && !alarm.getName()
                        .contains("异常驾驶行为")) {
                        alarmTypeList.add(alarm);
                    }
                    break;
                default:
                    break;
            }
        }
        return alarmTypeList;
    }

    @Override
    public void initiativeSendAlarmHandle(AlarmHandleParam handleParam) throws Exception {
        Integer alarmType = handleParam.getAlarmType();

        String monitorId = handleParam.getMonitorId();
        Long alarmStartTime = handleParam.getAlarmStartTime();
        Integer isAutoDeal = handleParam.getIsAutoDeal();

        String eventId = handleParam.getEventId();
        String riskIds = handleParam.getRiskIds();
        if (Objects.isNull(alarmStartTime)) {
            // 主动安全的报警处理没有报警开始时间，主动安全不使用这个值，给个默认值防止报错
            alarmStartTime = 0L;
        }
        // 报警处理  处理当天的所有同类型报警
        Date stringToDate = DateUtil.getLongToDate(alarmStartTime);
        String alarmDay = DateUtil.getDateToString(stringToDate, "yyyyMMdd");
        String startTimeStr = alarmDay + "000000";
        String endTimeStr = alarmDay + "235959";
        Long startTime;
        if (handleParam.getIsAutoDeal() == 1) {
            startTime = alarmStartTime;
        } else {
            startTime = DateUtil.getStringToLong(startTimeStr, DateUtil.DATE_FORMAT);
        }

        Long endTime = DateUtil.getStringToLong(endTimeStr, DateUtil.DATE_FORMAT);

        List<PlantParam> platformIp = getMonitorPlatform(monitorId);
        if (CollectionUtils.isEmpty(platformIp)) {
            return;
        }
        BindDTO bindDTO = MonitorUtils.getBindDTO(monitorId, "id", "name", "plateColor");
        if (bindDTO == null) {
            return;
        }
        // 监控对象标识
        String brand = bindDTO.getName();
        // 车牌颜色
        Integer color = bindDTO.getPlateColor();
        for (PlantParam param : platformIp) {
            T809AlarmForwardInfoMiddleQuery queryParam = T809AlarmForwardInfoMiddleQuery
                .getInstance(alarmType, monitorId, alarmStartTime, param, startTime, endTime);
            Set<String> riskIdSet = getRiskIdSet(eventId, riskIds);
            //协议类型
            Integer protocolType = param.getProtocolType();

            List<T809AlarmForwardInfoMiddleQuery> infos;
            if ((String.valueOf(protocolType).equals(ProtocolTypeUtil.T809_JING_PROTOCOL_809_2019) && isAutoDeal == 1
                || String.valueOf(protocolType).equals(ProtocolTypeUtil.T809_ZW_PROTOCOL_809_2019))
                && isAutoDeal == 1) {
                infos = SleepUtils.waitAndDo(() -> this.getAlarmForwardInfoMiddle(queryParam));
                logEmptyData(queryParam, infos, "手动");
            } else {
                infos = getNeedTransportAlarmInfo(queryParam, riskIdSet);
            }

            if (CollectionUtils.isEmpty(infos)) {
                continue;
            }
            // 消息列表
            List<T809Message> alarmList = getForwardAlarmList(handleParam, brand, color, param, protocolType, infos);
            if (!alarmList.isEmpty()) {
                logger.info("开始下发0x1403, 监控对象[{}], 共计{}条", brand, alarmList.size());
                sendResultTo809(param.getId(), alarmList);
                updateAlarmForwardInfoMiddle(infos, queryParam);
            }
        }
    }

    /**
     * 打印主动安全处理日志方便进行后续问题验证和排查
     * @param queryParam
     */

    private void logEmptyData(T809AlarmForwardInfoMiddleQuery queryParam, List<T809AlarmForwardInfoMiddleQuery> infos,
        String method) {
        if (CollectionUtils.isEmpty(infos)) {
            logger.info(method + "主动安全模块ALARM_FORWARD_INFO_MIDDLE未查询到数据,条件为:" + JSONObject.toJSONString(queryParam));
        }
    }

    public void sendDealAlarm809(AlarmHandleParam handleParam) throws Exception {
        Integer alarmType = handleParam.getAlarmType();
        String monitorId = handleParam.getMonitorId();
        Long alarmStartTime = handleParam.getAlarmStartTime();

        List<PlantParam> platformIp = getMonitorPlatform(monitorId);
        if (CollectionUtils.isEmpty(platformIp)) {
            return;
        }
        final RedisKey key = RedisKeyEnum.MONITOR_INFO.of(monitorId);
        final Map<String, String> vehicleInfo = RedisHelper.getHashMap(key, "name", "plateColor");
        if (vehicleInfo == null) {
            return;
        }
        // 监控对象标识
        String brand = vehicleInfo.get("name");
        // 车牌颜色
        Integer color = Integer.parseInt(vehicleInfo.get("plateColor"));
        boolean isZwMonitor = isZwMonitor(monitorId);
        for (PlantParam param : platformIp) {
            Integer finalAlarmType = getFinalEventType(alarmType, param.getProtocolType(), isZwMonitor);
            T809AlarmForwardInfoMiddleQuery queryParam = T809AlarmForwardInfoMiddleQuery
                .getInstance(finalAlarmType, monitorId, alarmStartTime, param, null, null);
            queryParam.setRiskEventId(handleParam.getEventId());
            //协议类型
            Integer protocolType = param.getProtocolType();

            List<T809AlarmForwardInfoMiddleQuery> infos =
                SleepUtils.waitAndDo(() -> this.getAlarmForwardInfoMiddle(queryParam));

            if (CollectionUtils.isEmpty(infos)) {
                logEmptyData(queryParam, infos, "自动");
                continue;
            }
            // 消息列表
            List<T809Message> alarmList = getForwardAlarmList(handleParam, brand, color, param, protocolType, infos);
            if (!alarmList.isEmpty()) {
                logger.info("补偿下发0x1403, 监控对象[{}], 共计{}条", brand, alarmList.size());
                sendResultTo809(param.getId(), alarmList);
                updateAlarmForwardInfoMiddle(infos, queryParam);
            }
        }
    }

    private void sendResultTo809(String platformId, List<T809Message> alarmList) {
        //开启用守护线程处理模式
        if (alarmDealBatchThread) {
            AlarmDealMessage alarmDealMessage = new AlarmDealMessage();
            alarmDealMessage.setT809PlatId(platformId);
            alarmDealMessage.setT809MessageList(alarmList);
            messageAsyncTaskExecutor.offerAlarmDeal(alarmDealMessage);
            return;
        }

        //新启线程处理模式
        taskExecutor.execute(() -> {
            int total = alarmList.size();
            int batchCount = (int) Math.ceil((double) total / alarmBatchSize);
            Channel channel = WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809());
            List<T809Message> subList;
            for (int i = 0; i < batchCount; i++) {
                subList = alarmList.subList(i * alarmBatchSize, Math.min((i + 1) * alarmBatchSize, total));
                Message msg = MsgUtil.getMsg(ConstantUtil.T809_UP_WARN_MSG_LIST, subList).assembleDesc809(platformId);
                channel.writeAndFlush(msg);
            }
        });
    }

    private List<T809Message> getForwardAlarmList(AlarmHandleParam handleParam, String brand, Integer color,
        PlantParam param, Integer protocolType, List<T809AlarmForwardInfoMiddleQuery> infos) {
        if (ProtocolTypeUtil.isNeedAlarmIdStandard(String.valueOf(protocolType))) {
            return getAlarmId809Message(handleParam, brand, color, param, infos);
        }
        String company = null;
        String operator;
        if (handleParam.getIsAutoDeal() == 1) {
            company = organizationService.getOrgByEntryDn("ou=organization").getName();
            if (handleParam.getOperator() != null) {
                operator = handleParam.getOperator();
            } else {
                operator = "admin";
            }

        } else {
            company = userService.getCurrentUserOrg().getName();
            operator = SystemHelper.getCurrentUsername();
        }
        List<T809Message> t809MessageList = new ArrayList<>();
        for (T809AlarmForwardInfoMiddleQuery alarmInfo : infos) {
            // 加入这个判断是为了避免上报两次督办那条报警的处理结果
            //这里做的是普通808报警需要判断的逻辑，主动安全另外处理
            boolean isAlarm = isAlarm(handleParam, alarmInfo);
            boolean isSecurity = alarmInfo.getRiskEventId() != null;
            if (isSecurity || isAlarm) {
                PlatformAlarmAck ack = PlatformAlarmAckInfo.getInstance(handleParam.getHandleType(), alarmInfo)
                    .assembleDealerInfo(operator, company);
                T809Message msg = getAlarmHandleMsgBody(brand, color, param, ack, isSecurity);
                if (msg != null) {
                    t809MessageList.add(msg);
                }
            }
        }
        return t809MessageList;
    }

    private boolean isAlarm(AlarmHandleParam handleParam, T809AlarmForwardInfoMiddleQuery alarmInfo) {
        return handleParam.getHandleId() == null || handleParam.getHandleId().equals(alarmInfo.getMsgSn());
    }

    @Override
    public void pushConnectionStatusByPlatformId(String platformId) {
        Map<String, String> statusAndGroupId = connectionParamsSetDao.getT809ConnectionStatusAndGroupIdById(platformId);
        if (MapUtils.isNotEmpty(statusAndGroupId)) {
            boolean isConnected = PublicVariable.checkConnectionStatus(statusAndGroupId.get("connectionStatus"));
            List<String> users = getT809OfflineReconnectUsers(statusAndGroupId.get("groupId"));
            sendT809OfflineReconnectMsg(users, isConnected);
        }
    }

    @Override
    public void cancelRemindByGroupId(String groupId) {
        List<String> users = getT809OfflineReconnectUsers(groupId);
        sendT809OfflineReconnectMsg(users, true);
    }

    private void sendT809OfflineReconnectMsg(List<String> users, boolean isConnected) {
        for (String user : users) {
            simpMessagingTemplate
                .convertAndSendToUser(user, ConstantUtil.WEB_SOCKET_T809_OFFLINE_RECONNECT, isConnected);
        }
    }

    private List<String> getT809OfflineReconnectUsers(String groupId) {
        OrganizationLdap org = organizationService.getOrganizationByUuid(groupId);
        if (Objects.isNull(org)) {
            return new ArrayList<>();
        }
        List<UserDTO> userList = userService.getUserByOrgDn(org.getId().toString(), SearchScope.ONELEVEL);
        if (CollectionUtils.isEmpty(userList)) {
            return Collections.emptyList();
        }
        List<String> groupUsers = new ArrayList<>();
        List<String> users = WebSubscribeManager.getInstance().getCheckUsers();
        for (UserDTO userDTO : userList) {
            groupUsers.add(userDTO.getUsername());
        }
        groupUsers.retainAll(users);
        // 对用户菜单权限进行验证，用户拥有“系统管理/ 809管理/转发平台连接”权限时，才进行809断线重连消息推送
        List<String> filterUsers = new ArrayList<>();
        groupUsers.forEach(o -> {
            UserMenuDTO userMenuDTO = userService.loadUserPermission(o);
            if (userMenuDTO.getMenuIds().contains(MENU_CONNECTION_PARAMS)) {
                filterUsers.add(o);
            }
        });
        return filterUsers;
    }

    private Set<String> getRiskIdSet(String eventId, String riskIds) {
        Set<String> riskIdSet = new HashSet<>();
        if (StrUtil.isNotBlank(riskIds)) {
            riskIdSet.addAll(Arrays.asList(riskIds.split(",")));
        } else if (StrUtil.isNotBlank(eventId)) {
            //风险可能已经同步到es中，所以有可能会有问题
            riskIdSet.addAll(adasEsService.esGetRiskIdByEventId(null, eventId));
        }
        return riskIdSet;
    }

    private List<T809Message> getAlarmId809Message(AlarmHandleParam handleParam, String brand, Integer color,
        PlantParam param, List<T809AlarmForwardInfoMiddleQuery> infos) {
        List<T809Message> t809MessageList = new ArrayList<>();
        OrganizationLdap org;
        String operator;
        if (handleParam.getIsAutoDeal() == 1) {
            org = organizationService.getOrgByEntryDn("ou=organization");
            operator = "admin";
        } else {
            org = userService.getCurrentUserOrg();
            operator = SystemHelper.getCurrentUsername();
        }
        AlarmProcessAck alarmProcessAck;
        for (T809AlarmForwardInfoMiddleQuery alarmInfo : infos) {
            //督办应答模块handleId不为空，应为督办已经把结果给上级平台，所以这边就不需要上传该条报警的结果了
            if (handleParam.getHandleId() != null && Objects
                .equals(handleParam.getEventId(), alarmInfo.getRiskEventId())) {
                continue;
            }
            //不管是督办还是处理都全部上报给平台
            alarmProcessAck = AlarmProcessAck
                .getInstance(org.getName(), handleParam.getHandleType(), alarmInfo.getMsgSn(), operator,
                    alarmInfo.getAlarmId());
            T809Message msg = getAlarmProcessMsgBody(brand, color, param, alarmProcessAck);
            if (msg != null) {
                t809MessageList.add(msg);
            }
        }
        return t809MessageList;
    }

    /**
     * 获得川冀标body实体
     */
    private T809Message getAlarmProcessMsgBody(String brand, Integer color, PlantParam param, AlarmProcessAck ack) {
        if (param == null) {
            return null;
        }
        // IP地址
        String serverIp = param.getIp();
        // 接入码
        Integer msgGNSSCenterId = param.getCenterId();
        SupervisionAlarmInfo supervisionAlarmInfo = SupervisionAlarmInfo.getInstance(brand, color, ack);
        return MsgUtil.getT809Message(ConstantUtil.T809_UP_WARN_MSG, serverIp, msgGNSSCenterId, supervisionAlarmInfo);
    }

    /**
     * 更新报警转发中间表的报警处理状态
     */
    private void updateAlarmForwardInfoMiddle(List<T809AlarmForwardInfoMiddleQuery> infos,
        T809AlarmForwardInfoMiddleQuery queryParam) {
        // 先删除原来的,再将状态新增进去
        if (CollectionUtils.isEmpty(infos) || queryParam == null) {
            return;
        }
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(infos));
        HttpClientUtil.send(PaasCloudHBaseAccessEnum.BATCH_UPDATE_STATUS, params);
    }

    /**
     * 获取消息体
     */
    private T809Message getAlarmHandleMsgBody(String brand, Integer color, PlantParam param, PlatformAlarmAck ack,
        boolean isSecurity) {
        if (param == null) {
            return null;
        }
        // 809连接协议类型
        Integer protocolType = param.getProtocolType();
        if (!isSecurity && CONNECT_PROTOCOL_TYPE_808_2019.contains(protocolType)) {
            return null;
        }

        String serverIp = param.getIp(); // IP地址
        Integer msgGNSSCenterId = param.getCenterId(); // 接入码
        SupervisionAlarmInfo supervisionAlarmInfo = new SupervisionAlarmInfo(); // 消息体
        if (CONNECT_PROTOCOL_TYPE_808_2019.contains(protocolType)) {
            //809-2019版本协议(新增沪标809-2019) 中位标准
            supervisionAlarmInfo.setDataType(ConstantUtil.T809_2019_UP_WARN_MSG_ADPT_TODO_INFO);
            supervisionAlarmInfo.setDataLength(7); // 后续数据长度(数据部分字段长度相加)
        } else {
            supervisionAlarmInfo.setDataType(ConstantUtil.T809_UP_WARN_MSG_ADPT_TODO_INFO);
            supervisionAlarmInfo.setDataLength(5); // 后续数据长度
        }
        supervisionAlarmInfo.setVehicleNo(brand);
        supervisionAlarmInfo.setVehicleColor(color);
        supervisionAlarmInfo.setData(MsgUtil.objToJson(ack));
        return MsgUtil.getT809Message(ConstantUtil.T809_UP_WARN_MSG, serverIp, msgGNSSCenterId, supervisionAlarmInfo);
    }

    /**
     * 获取需要转发的报警信息:
     * ①川标主动安全报警上报该事件对应风险全部事件
     * ②川标808普通报警上报该报警之前的全部同类型的报警
     * ③冀标主动安全报警上报该事件对应风险的全部事件
     * ④冀标普通808报警上报该报警之前的全部同类型的报警
     */
    private List<T809AlarmForwardInfoMiddleQuery> getNeedTransportAlarmInfo(T809AlarmForwardInfoMiddleQuery queryParam,
        Set<String> riskIdSet) {
        List<T809AlarmForwardInfoMiddleQuery> infos;

        if (CollectionUtils.isNotEmpty(riskIdSet)) {
            List<T809AlarmForwardInfoMiddleQuery> queryList = intSecurityQueryParam(queryParam, riskIdSet);
            int eventSize = queryList.size();
            infos = SleepUtils.waitAndDo(() -> queryList.stream()
                    .map(this::getAlarmForwardInfoMiddle)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList()));
            if (eventSize != infos.size()) {
                logger.info("es查询事件的数量是：" + eventSize + "转发表的数量是：" + infos.size());
            }
            logEmptyData(queryParam, infos, "手动");
        } else {
            infos = getAlarmForwardInfoMiddle(queryParam);
        }
        // 获取监控对象需要转发到上级平台的小于等于报警开始时间的全部同类型报警
        return infos;
    }

    private List<T809AlarmForwardInfoMiddleQuery> getAlarmForwardInfoMiddle(T809AlarmForwardInfoMiddleQuery query) {
        Map<String, String> params = new HashMap<>(8);
        params.put("monitorId", query.getMonitorIdStr());
        params.put("platId", query.getPlatIdStr());
        params.put("alarmType", String.valueOf(query.getAlarmType()));
        params.put("startTime", String.valueOf(query.getStartTime()));
        params.put("endTime", String.valueOf(query.getEndTime()));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_ALARM_FORWARD_INFO_MIDDLE, params);
        return PaasCloudUrlUtil.getResultListData(str, T809AlarmForwardInfoMiddleQuery.class);
    }

    /**
     * 组装809主动安全查询相关参数
     */
    private List<T809AlarmForwardInfoMiddleQuery> intSecurityQueryParam(T809AlarmForwardInfoMiddleQuery queryParam,
        Set<String> riskIds) {
        List<T809AlarmForwardInfoMiddleQuery> result = new ArrayList<>();
        //因为风险处置记录模块查询最新的风险时候，数据可能还没有入库，所以这里需要进行的等待
        List<AdasRiskEventEsBean> eventEsBeans =
            SleepUtils.waitAndDo(() -> adasEsService.getEventEsBeanByRiskId(riskIds.toArray(new String[] {})));
        if (CollectionUtils.isEmpty(eventEsBeans)) {
            return result;
        }
        String vehicleId = eventEsBeans.get(0).getVehicleId();
        boolean isZwMonitor = isZwMonitor(vehicleId);

        Set<Long> times = new HashSet<>();
        Set<Integer> alarmTypes = new HashSet<>();
        for (AdasRiskEventEsBean event : eventEsBeans) {
            long time = event.getEventTime().getTime();
            Integer alarmType = getFinalEventType(event.getEventType(), queryParam.getProtocolType(), isZwMonitor);
            times.add(time);
            alarmTypes.add(alarmType);
            T809AlarmForwardInfoMiddleQuery data = new T809AlarmForwardInfoMiddleQuery();
            BeanUtils.copyProperties(queryParam, data);
            data.setTime(time);
            data.setAlarmType(alarmType);
            result.add(data);

        }

        queryParam.setTimes(times);
        queryParam.setAlarmTypes(alarmTypes);
        return result;
    }

    private Integer getFinalEventType(Integer eventType, Integer protocolType809, boolean isZwMonitor) {
        Integer alarmType;
        if (!isZwMonitor && ProtocolTypeUtil.T809_ZW_PROTOCOL_809_2019.equals(protocolType809 + "")) {
            //不是中位的车，但是要转zw标准（川桂标）
            alarmType = adasCommonHelper.getT808AlarmType(AdasFunctionIdToZwRelation.convertZwFunctionId(eventType));

        } else {
            alarmType = adasCommonHelper.getT808AlarmType(eventType);
        }

        return alarmType;
    }

    private boolean isZwMonitor(String vehicleId) {
        BindDTO monitor = MonitorUtils.getBindDTO(vehicleId, "deviceType");
        return monitor.getDeviceType() != null && monitor.getDeviceType().equals(ProtocolTypeUtil.ZW_PROTOCOL_808_2019);
    }

}
