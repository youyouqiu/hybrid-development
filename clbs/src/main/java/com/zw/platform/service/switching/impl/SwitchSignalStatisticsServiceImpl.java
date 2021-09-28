package com.zw.platform.service.switching.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.vas.alram.IoVehicleConfigInfo;
import com.zw.platform.domain.vas.switching.IoStatistics;
import com.zw.platform.domain.vas.switching.SwitchSignalStatisticsInfo;
import com.zw.platform.domain.vas.switching.query.SwitchSignalQuery;
import com.zw.platform.repository.vas.SwitchingSignalDao;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.switching.SwitchSignalStatisticsService;
import com.zw.platform.util.ConvertUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RedisUtil;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.common.ZipUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.zw.platform.basic.core.RedisHelper.SIX_HOUR_REDIS_EXPIRE;
import static java.util.Comparator.comparing;

/**
 * @author penghj
 * @version 1.0
 * @date 2018/9/6 16:38
 */
@Service
public class SwitchSignalStatisticsServiceImpl implements SwitchSignalStatisticsService {
    private static Logger log = LogManager.getLogger(SwitchSignalStatisticsServiceImpl.class);

    @Resource
    private SwitchingSignalDao switchingSignalDao;

    @Resource
    private UserService userService;

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /**
     * 开关信号报表表格数据 KEY 终端
     */
    private static final String SWITCH_SIGNAL_REPORT_FORM_DATA_TERMINAL_KEY = "switchSignalReportFormData_terminal";
    /**
     * io采集1
     */
    private static final String SWITCH_SIGNAL_REPORT_FORM_DATA_ACQUISITIONBOARD_ONE_KEY =
        "switchSignalReportFormData_acquisitionBoardOne";
    /**
     * io采集2
     */
    private static final String SWITCH_SIGNAL_REPORT_FORM_DATA_ACQUISITIONBOARD_TWO_KEY =
        "switchSignalReportFormData_acquisitionBoardTwo";

    @Override
    public List<SwitchSignalStatisticsInfo> getBindSwitchSignalVehicle() throws Exception {
        List<SwitchSignalStatisticsInfo> list = new ArrayList<>();
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // 获取当前用户所属组织及下级组织
        List<String> orgList = userService.getOrgUuidsByUser(userId);
        if (userId != null && !"".equals(userId) && CollectionUtils.isNotEmpty(orgList)) {
            list = switchingSignalDao.getBindSwitchSignalVehicle(userService.getUserUuidById(userId), orgList);
        }
        return list;
    }

    @Override
    public JsonResultBean getSwitchSignalChartInfo(SwitchSignalQuery query) throws Exception {
        query.setVehicleIdBytes(UuidUtils.getBytesFromUUID(UUID.fromString(query.getVehicleId())));
        query.setStartTimeLong(DateUtils.parseDate(query.getStartTime(), DATE_FORMAT).getTime() / 1000);
        query.setEndTimeLong(DateUtils.parseDate(query.getEndTime(), DATE_FORMAT).getTime() / 1000);
        String userName = SystemHelper.getCurrentUsername();
        final RedisKey keyTerminalIo = HistoryRedisKeyEnum.SWITCH_SIGNAL_REPORT_FORM_DATA_TERMINAL_KEY.of(userName);
        final RedisKey keyIo1 =
                HistoryRedisKeyEnum.SWITCH_SIGNAL_REPORT_FORM_DATA_ACQUISITIONBOARD_ONE_KEY.of(userName);
        final RedisKey keyIo2 =
                HistoryRedisKeyEnum.SWITCH_SIGNAL_REPORT_FORM_DATA_ACQUISITIONBOARD_TWO_KEY.of(userName);
        RedisHelper.delete(keyTerminalIo);
        RedisHelper.delete(keyIo1);
        RedisHelper.delete(keyIo2);
        JSONObject msg = new JSONObject();
        //终端io
        List<Map<String, Object>> terminalAllList = new ArrayList<>();
        //采集板1
        List<Map<String, Object>> acquisitionBoardOneAllList = new ArrayList<>();
        //采集板2
        List<Map<String, Object>> acquisitionBoardTwoAllList = new ArrayList<>();

        String startStr = query.getStartTime().replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
        String endStr = query.getEndTime().replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
        Map<String, String> param = new HashMap<>();
        param.put("monitorId", query.getVehicleId());
        param.put("startTime", startStr);
        param.put("endTime", endStr);
        String resultStr = HttpClientUtil.send(PaasCloudUrlEnum.IO_EPORT_URL, param);
        JSONObject obj = JSONObject.parseObject(resultStr);
        List<IoStatistics> switchSignalInfo =
            JSONObject.parseArray(obj.getString("data"), IoStatistics.class);
        // PAAS提供的数据是反的，这里把它正过来
        switchSignalInfo = Lists.reverse(switchSignalInfo);

        if (CollectionUtils.isNotEmpty(switchSignalInfo)) {
            List<IoVehicleConfigInfo> functionIdBingIoSite =
                switchingSignalDao.getFunctionIdBingIoSite(query.getVehicleId());
            if (CollectionUtils.isNotEmpty(functionIdBingIoSite)) {
                //终端下绑定的功能检测类型
                List<IoVehicleConfigInfo> terminalFunction =
                    functionIdBingIoSite.stream().filter(info -> info.getIoType() == 1)
                        .sorted(comparing(IoVehicleConfigInfo::getIoSite)).collect(Collectors.toList());
                //采集板1下绑定的功能检测类型
                List<IoVehicleConfigInfo> acquisitionBoardOneFunction =
                    functionIdBingIoSite.stream().filter(info -> info.getIoType() == 2)
                        .sorted(comparing(IoVehicleConfigInfo::getIoSite)).collect(Collectors.toList());
                //采集板2下绑定的功能检测类型
                List<IoVehicleConfigInfo> acquisitionBoardTwoFunction =
                    functionIdBingIoSite.stream().filter(info -> info.getIoType() == 3)
                        .sorted(comparing(IoVehicleConfigInfo::getIoSite)).collect(Collectors.toList());
                for (IoStatistics info : switchSignalInfo) {
                    Map<String, Object> terminalInfo = new HashMap<>(16);
                    installBasicInfo(info, terminalInfo);
                    Map<String, Object> acquisitionBoardOneInfo = new HashMap<>(16);
                    installBasicInfo(info, acquisitionBoardOneInfo);
                    Map<String, Object> acquisitionBoardTwoInfo = new HashMap<>(16);
                    installBasicInfo(info, acquisitionBoardTwoInfo);
                    //终端io
                    installTerminalInfo(terminalFunction, info, terminalInfo);
                    terminalAllList.add(terminalInfo);
                    //io采集板1
                    installAcquisitionBoardInfo(acquisitionBoardOneFunction, info.getIoObjOne(),
                        acquisitionBoardOneInfo);
                    acquisitionBoardOneAllList.add(acquisitionBoardOneInfo);
                    //io采集板2
                    installAcquisitionBoardInfo(acquisitionBoardTwoFunction, info.getIoObjTwo(),
                        acquisitionBoardTwoInfo);
                    acquisitionBoardTwoAllList.add(acquisitionBoardTwoInfo);
                }
            }
        }
        RedisHelper.addObjectToList(keyTerminalIo, Lists.reverse(terminalAllList), SIX_HOUR_REDIS_EXPIRE);
        RedisHelper.addObjectToList(keyIo1, Lists.reverse(acquisitionBoardOneAllList), SIX_HOUR_REDIS_EXPIRE);
        RedisHelper.addObjectToList(keyIo2, Lists.reverse(acquisitionBoardTwoAllList), SIX_HOUR_REDIS_EXPIRE);
        String terminalZip = JSON.toJSONString(terminalAllList);
        terminalZip = ZipUtil.compress(terminalZip);
        msg.put("terminal", terminalZip);
        String acquisitionBoardOneZip = JSON.toJSONString(acquisitionBoardOneAllList);
        acquisitionBoardOneZip = ZipUtil.compress(acquisitionBoardOneZip);
        msg.put("acquisitionBoardOne", acquisitionBoardOneZip);
        String acquisitionBoardTwoZip = JSON.toJSONString(acquisitionBoardTwoAllList);
        acquisitionBoardTwoZip = ZipUtil.compress(acquisitionBoardTwoZip);
        msg.put("acquisitionBoardTwo", acquisitionBoardTwoZip);
        return new JsonResultBean(msg);
    }

    @Override
    public PageGridBean getSwitchSignalTerminalFormInfo(SwitchSignalQuery query) throws Exception {
        String terminalKey = SWITCH_SIGNAL_REPORT_FORM_DATA_TERMINAL_KEY + "-" + SystemHelper.getCurrentUsername();
        return getPageInfo(query, terminalKey);
    }

    @Override
    public PageGridBean getSwitchSignalAcquisitionBoardOneFormInfo(SwitchSignalQuery query) throws Exception {
        String acquisitionBoardOneKey =
            SWITCH_SIGNAL_REPORT_FORM_DATA_ACQUISITIONBOARD_ONE_KEY + "-" + SystemHelper.getCurrentUsername();
        return getPageInfo(query, acquisitionBoardOneKey);
    }

    @Override
    public PageGridBean getSwitchSignalAcquisitionBoardTwoFormInfo(SwitchSignalQuery query) throws Exception {
        String acquisitionBoardTwoKey =
            SWITCH_SIGNAL_REPORT_FORM_DATA_ACQUISITIONBOARD_TWO_KEY + "-" + SystemHelper.getCurrentUsername();
        return getPageInfo(query, acquisitionBoardTwoKey);
    }

    private PageGridBean getPageInfo(SwitchSignalQuery query, String key) throws Exception {
        Page<Object> result = new Page<>();
        try {
            List<Object> formData = RedisHelper.getListObj(HistoryRedisKeyEnum.SWITCH_SIGNAL_STATISTIC_KEY.of(key),
                    (query.getStart() + 1), (query.getStart() + query.getLimit()));
            if (CollectionUtils.isNotEmpty(formData)) {
                result = RedisUtil.queryPageList(
                    formData, query, HistoryRedisKeyEnum.SWITCH_SIGNAL_STATISTIC_KEY.of(key));
            }
            return new PageGridBean(query, result, true);
        } finally {
            result.close();
        }
    }

    /**
     * 组装终端信息
     * @param terminalFunction
     * @param info
     * @param terminalInfo
     */
    private void installTerminalInfo(List<IoVehicleConfigInfo> terminalFunction, IoStatistics info,
        Map<String, Object> terminalInfo) {
        List<Map<String, Object>> columnNameList = new ArrayList<>();
        //终端是否异常：0正常 1异常 2:无终端信息
        Integer terminalUnusual = 0;
        //io状态 (0断开 1闭合 2无接口)
        Integer ioOne = info.getIoOne();
        Integer ioTwo = info.getIoTwo();
        Integer ioThree = info.getIoThree();
        Integer ioFour = info.getIoFour();
        if (ioOne == null && ioTwo == null && ioThree == null && ioFour == null) {
            terminalUnusual = 2;
        }
        if (ioOne != null && ioTwo != null && ioThree != null && ioFour != null && ioOne == -1 && ioTwo == -1
            && ioThree == -1 && ioFour == -1) {
            terminalUnusual = 1;
        }
        if (CollectionUtils.isNotEmpty(terminalFunction)) {
            for (IoVehicleConfigInfo terminalFunctionInfo : terminalFunction) {
                Map<String, Object> terminalMap = new HashMap<>(16);
                terminalMap.put("state", 4);
                Integer ioSite = terminalFunctionInfo.getIoSite();
                String columnName = terminalFunctionInfo.getName();
                String stateName = "";
                terminalMap.put("columnName", columnName);
                if (terminalUnusual == 1) {
                    stateName = columnName + "异常";
                    terminalMap.put("state", 3);
                } else if (terminalUnusual == 0) {
                    if (ioSite == 0) {
                        stateName = getIoStateStr(ioOne, terminalFunctionInfo, terminalMap);
                    } else if (ioSite == 1) {
                        stateName = getIoStateStr(ioTwo, terminalFunctionInfo, terminalMap);
                    } else if (ioSite == 2) {
                        stateName = getIoStateStr(ioThree, terminalFunctionInfo, terminalMap);
                    } else if (ioSite == 3) {
                        stateName = getIoStateStr(ioFour, terminalFunctionInfo, terminalMap);
                    }
                }
                terminalMap.put("stateName", stateName);
                columnNameList.add(terminalMap);
            }
        }
        terminalInfo.put("ioInfo", columnNameList);
    }

    /**
     * 组装采集板信息
     * @param functionInfoList
     * @param ioObj
     * @param columnInfo
     */
    private void installAcquisitionBoardInfo(List<IoVehicleConfigInfo> functionInfoList, String ioObj,
        Map<String, Object> columnInfo) {
        List<Map<String, Object>> columnNameList = new ArrayList<>();
        // 采集板是否异常：0正常 1异常 2无采集板信息
        Integer acquisitionBoardUnusual = 0;
        // io传感器信息
        JSONObject cirIoCheckSensor = null;
        // IO总数
        Integer ioCount = null;
        // 0:表示所有io位正常 1:表示io位有正常或者异常
        Integer abnormal = null;
        // I/O状态集合
        JSONArray statusList = null;
        // I/O 异常集合
        JSONArray abnormalList = null;
        if (ioObj == null) {
            acquisitionBoardUnusual = 2;
        } else {
            cirIoCheckSensor = JSON.parseObject(ioObj);
            if (cirIoCheckSensor.getInteger("unusual") == 1) {
                acquisitionBoardUnusual = 1;
            } else {
                ioCount = cirIoCheckSensor.getInteger("ioCount");
                abnormal = cirIoCheckSensor.getInteger("abnormal");
                statusList = cirIoCheckSensor.getJSONArray("statusList");
                abnormalList = abnormal == 0 ? null : cirIoCheckSensor.getJSONArray("abnormalList");
            }
        }
        if (CollectionUtils.isNotEmpty(functionInfoList)) {
            for (IoVehicleConfigInfo functionInfo : functionInfoList) {
                //列信息
                Map<String, Object> columnInfoMap = new HashMap<>(16);
                columnInfoMap.put("state", 4);
                //io位
                Integer ioSite = functionInfo.getIoSite();
                //列名(表头)
                String columnName = functionInfo.getName();
                //列值
                String stateName = "";
                columnInfoMap.put("columnName", columnName);
                if (acquisitionBoardUnusual == 1) {
                    stateName = columnName + "异常";
                    columnInfoMap.put("state", 3);
                } else if (acquisitionBoardUnusual == 0) {
                    if (ioSite < ioCount) {
                        Integer ioIndex = ioSite / 32;
                        JSONObject statusJSONObject = statusList.getJSONObject(ioIndex);
                        Integer ioStatus = statusJSONObject.getInteger("ioStatus");
                        Integer ioAbnormal =
                            abnormalList == null ? null : abnormalList.getJSONObject(ioIndex).getInteger("ioAbnormal");
                        //0:正常;  1:异常
                        Integer ioUnusual = ioAbnormal == null ? 0 :
                            ConvertUtil.binaryIntegerWithOne(ioAbnormal, ioSite - (32 * ioIndex));
                        if (ioUnusual == 0) {
                            //状态(0:高电平; 1:低电平)
                            Integer state = ConvertUtil.binaryIntegerWithOne(ioStatus, ioSite - (32 * ioIndex));
                            stateName = getIoStateStr(state, functionInfo, columnInfoMap);
                        } else {
                            stateName = columnName + "异常";
                            columnInfoMap.put("state", 3);
                        }
                    }
                }
                columnInfoMap.put("stateName", stateName);
                columnNameList.add(columnInfoMap);
            }
        }
        columnInfo.put("ioInfo", columnNameList);
    }

    /**
     * 组装数据
     * @param info
     * @param map
     */
    private void installBasicInfo(IoStatistics info, Map<String, Object> map) {
        String brand = info.getMonitorName();
        String time = DateUtil.getLongToDateStr(info.getVTime() * 1000, null);
        String gpsMile = info.getGpsMile();
        gpsMile = (StringUtils.isNotBlank(gpsMile) && gpsMile.endsWith(".0")
            ? gpsMile.substring(0, gpsMile.lastIndexOf(".")) : gpsMile);
        String speed = info.getSpeed();
        speed = (StringUtils.isNotBlank(speed) && speed.endsWith(".0") ? speed.substring(0, speed.lastIndexOf(".")) :
            speed);
        String longtitude = info.getLongtitude();
        String latitude = info.getLatitude();
        map.put("brand", brand);
        map.put("time", time);
        map.put("gpsMile", gpsMile);
        map.put("speed", speed);
        map.put("longtitude", longtitude);
        map.put("latitude", latitude);
    }

    /**
     * 获得io的状态str
     * @param ioState
     * @param info
     * @return
     */
    private String getIoStateStr(Integer ioState, IoVehicleConfigInfo info, Map<String, Object> map) {
        //io状态str
        String ioStateStr = "";
        //电平状态
        Integer signalType = null;
        Integer state = 4;
        if (ioState != null) {
            if (ioState == 0) {
                signalType = info.getHighSignalType();
            } else if (ioState == 1) {
                signalType = info.getLowSignalType();
            }
            if (signalType != null) {
                if (signalType == 1) {
                    state = 1;
                    ioStateStr = info.getStateOne();
                } else if (signalType == 2) {
                    state = 2;
                    ioStateStr = info.getStateTwo();
                }
            }
        }
        map.put("state", state);
        return ioStateStr;
    }
}
