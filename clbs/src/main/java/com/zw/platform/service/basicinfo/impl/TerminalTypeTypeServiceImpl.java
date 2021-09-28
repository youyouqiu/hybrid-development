package com.zw.platform.service.basicinfo.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.DeviceChannelSettingInfo;
import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.TerminalTypeInfo;
import com.zw.platform.domain.basicinfo.TerminalTypeRedisInfo;
import com.zw.platform.repository.modules.TerminalTypeDao;
import com.zw.platform.service.basicinfo.TerminalTypeService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.Translator;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportExcel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2021/2/23 14:49
 */
@Service
public class TerminalTypeTypeServiceImpl implements TerminalTypeService {

    private static final Logger log = LogManager.getLogger(TerminalTypeTypeServiceImpl.class);

    private static final int NUMBER_ZERO = 0;

    private static final int NUMBER_ONE = 1;

    private static final int CHANNEL_NUMBER_MAX = 35; // 通道号个数最大值

    private static final int CHANNEL_NUMBER_MIN = 1; // 通道号个数最小值

    private static final int CAMERAS_NUMBER_MAX = 5; // 摄像头个数最大值

    private static final int TERMINAL_TYPE_MAX_LENGTH = 30; // 终端型号最大长度

    private static final int TERMINAL_TYPE_MIN_LENGTH = 2; // 终端型号最小长度

    private static final String TERMINAL_TYPE_PATTERN = "[A-Za-z0-9_#\\*\\u4e00-\\u9fa5\\-]*"; // 终端型号正则表达式

    private static final String VALUE_YES = "是";

    private static final String VALUE_NO = "否";

    public static final Pattern DEVICE_MAC_CHECKER = Pattern.compile("^([0-9a-fA-F]{2})(([/\\s-][0-9a-fA-F]{2}){5})$");

    public static final Translator<Integer, String> SAMPLING_RATE =
        Translator.of(0, "8KHZ", 1, "22.05KHZ", 2, "44.1KHZ", 3, "48KHZ");
    public static final Translator<Integer, String> VOCAL_TRACT = Translator.of(0, "单声道", 1, "双声道");

    public static final Translator<Integer, String> AUDIO_FORMAT = Translator
        .of(0, "ADPCMA", 2, "G726-16K", 3, "G726-24K", 4, "G726-32K", 5, "G726-40K", 6, "G711a",
            Translator.Pair.of(7, "G711u"), Translator.Pair.of(8, "AAC-ADTS"));
    @Autowired
    private TerminalTypeDao terminalTypeDao;

    @Autowired
    private LogSearchService logSearchService;

    @Override
    public Page<TerminalTypeInfo> getTerminalType(BaseQueryBean query) throws Exception {
        if (StringUtils.isNotBlank(query.getSimpleQueryParam())) {
            // 特殊字符转译
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam()));
        }
        return PageHelperUtil.doSelect(query, () -> terminalTypeDao.getTerminalTypeList(query.getSimpleQueryParam()));
    }

    /**
     * 根据id查询终端型号信息
     * @param id 终端型号id
     */
    @Override
    public TerminalTypeInfo getTerminalTypeInfoById(String id) throws Exception {
        TerminalTypeInfo terminalTypeInfo = terminalTypeDao.getTerminalTypeInfo(id);
        if (terminalTypeInfo != null) {
            // 终端型号视频参数设置信息
            String channelId = terminalTypeInfo.getDeviceChannelId();
            if (StringUtils.isNotBlank(channelId)) {
                List<String> channelIds = Arrays.asList(channelId.split(","));
                List<DeviceChannelSettingInfo> settingInfo = terminalTypeDao.getChannelParamByChannelId(channelIds);
                terminalTypeInfo.setDeviceChannelSettingInfoList(settingInfo);
            }
        }
        return terminalTypeInfo;
    }

    @Override
    public List<String> queryDeviceInfoByTerminalTypeId(String terminalTypeId) {
        List<String> deviceIds = new ArrayList<>();
        if (StringUtils.isNotBlank(terminalTypeId)) {
            deviceIds = terminalTypeDao.getDeviceIdByTerminalTypeId(terminalTypeId);
        }
        return deviceIds;
    }

    /**
     * 新增终端型号
     */
    @Override
    public void addTerminalType(TerminalTypeInfo info, String ipAddress) throws Exception {
        if (info != null) {
            // 判断终端型号是否支持视频
            Integer supportVideo = info.getSupportVideoFlag();
            if (supportVideo == NUMBER_ONE) { // 终端型号支持视频
                // 增加终端型号音视频信息
                addChannelParam(info);
            }
            info.setCreateDataUsername(SystemHelper.getCurrentUsername());
            // 新增终端型号信息
            terminalTypeDao.addTerminalType(info);
            saveTerminalTypeInfoToRedis(info);
            String message =
                "新增终端型号 (" + "厂商 :" + info.getTerminalManufacturer() + ", 型号 :" + info.getTerminalType() + ")";
            logSearchService.addLog(ipAddress, message, "3", "");
        }
    }

    /**
     * 删除终端类型(批量删除)
     * @param id
     * @throws Exception
     */
    @Override
    public Map<String, Object> deleteTerminalType(String id, String ipAddress) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        if (StringUtils.isNotBlank(id)) {
            List<String> terminalTypeIds = Arrays.asList(id.split(","));
            if (terminalTypeIds.size() > 0) {
                List<TerminalTypeInfo> terminals = terminalTypeDao.getTerminalTypeInfoByIds(terminalTypeIds);
                List<TerminalTypeInfo> resultDeleteType = getResultDeleteType(terminals, resultMap);
                if (resultDeleteType.size() > NUMBER_ZERO) {
                    List<String> needDelChannelId = getChannelId(terminals);
                    if (needDelChannelId != null && needDelChannelId.size() > 0) {
                        terminalTypeDao.deleteChannelSet(needDelChannelId);
                    }
                    List<String> deleteTerminalTypeIds =
                        resultDeleteType.stream().map(TerminalTypeInfo::getId).collect(Collectors.toList());
                    terminalTypeDao.deleteTerminalType(deleteTerminalTypeIds);
                    addDeleteTypeLog(resultDeleteType, ipAddress, terminalTypeIds.size());
                }
            }
        }
        return resultMap;
    }

    /**
     * 判断终端型号是否与终端绑定,过滤出没有绑定可以删除的终端型号
     * @param terminals
     * @param resultMap
     * @return
     */
    private List<TerminalTypeInfo> getResultDeleteType(List<TerminalTypeInfo> terminals,
        Map<String, Object> resultMap) {
        List<TerminalTypeInfo> resultDelete = new ArrayList<>();
        StringBuilder errorMessage = new StringBuilder();
        if (!CollectionUtils.isEmpty(terminals)) {
            for (int index = 0; index < terminals.size(); index++) {
                TerminalTypeInfo info = terminals.get(index);
                // 根据终端型号id查询绑定该终端型号的终端id
                List<String> deviceId = queryDeviceInfoByTerminalTypeId(info.getId());
                // 不为空,有终端绑定了该终端型号,不支持修删除终端型号
                if (!CollectionUtils.isEmpty(deviceId)) {
                    errorMessage.append("终端型号 :").append(info.getTerminalType()).append("(")
                        .append(info.getTerminalManufacturer()).append(")").append("已经绑定了终端,请解除绑定后再删除。<br/>");
                } else { // 为空,没有终端绑定该终端型号,支持删除终端型号
                    resultDelete.add(info);
                }
            }
            resultMap.put("errorMsg", errorMessage);
        }
        return resultDelete;
    }

    /**
     * 生成终端型号导入模板
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    public void generateTypeTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // 表头
        headList.add("终端厂商");
        headList.add("终端型号");
        headList.add("是否支持拍照");
        headList.add("摄像头个数");
        headList.add("是否支持行驶记录仪");
        headList.add("是否支持监听");
        headList.add("是否支持视频");
        headList.add("是否支持主动安全");
        headList.add("是否为一体机");
        headList.add("实时流音频格式");
        headList.add("实时流采样率");
        headList.add("实时流声道数");
        headList.add("存储流音频格式");
        headList.add("存储流采样率");
        headList.add("存储流声道数");
        headList.add("通道号个数");
        // 必填字段
        requiredList.add("终端厂商");
        requiredList.add("终端型号");
        // 默认设置一条数据
        exportList.add("[f]F3");
        exportList.add("F3-default");
        exportList.add("是");
        exportList.add("1"); // 摄像头个数
        exportList.add("否");
        exportList.add("是");
        exportList.add("否");
        exportList.add("是");
        exportList.add("否");
        exportList.add("");
        exportList.add("");
        exportList.add("");
        exportList.add("");
        exportList.add("");
        exportList.add("");
        exportList.add("");
        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<>();
        // 终端厂商
        List<String> terminalManufacturer = terminalTypeDao.getTerminalManufacturer();
        String[] manufacturer = terminalManufacturer.toArray(new String[0]);
        selectMap.put("终端厂商", manufacturer);
        // 是否支持拍照
        String[] supportPhotoFlag = { "是", "否" };
        selectMap.put("是否支持拍照", supportPhotoFlag);
        // 摄像头个数
        String[] camerasNumber = { "1", "2", "3", "4", "5" };
        selectMap.put("摄像头个数", camerasNumber);
        // 是否支持行驶记录仪
        String[] supportDrivingRecorderFlag = { "是", "否" };
        selectMap.put("是否支持行驶记录仪", supportDrivingRecorderFlag);
        String[] supportMonitoringFlag = { "是", "否" };
        selectMap.put("是否支持监听", supportMonitoringFlag);
        // 是否视频
        String[] supportVideoFlag = { "是", "否" };
        selectMap.put("是否支持视频", supportVideoFlag);
        // 是否支持主动安全
        String[] activeSafety = { "是", "否" };
        selectMap.put("是否支持主动安全", activeSafety);
        // 是否为一体机
        String[] allInOne = { "是", "否" };
        selectMap.put("是否为一体机", allInOne);
        // 音频格式 0:ADPCMA; 2:G726-16K; 3:G726-24K; 4:G726-32K; 5:G726-40K; 6:G711a; 7:G711u
        String[] audioFormat =
            { "ADPCMA", "G726-16K", "G726-24K", "G726-32K", "G726-40K", "G711a", "G711u", "AAC-ADTS" };
        selectMap.put("实时流音频格式", audioFormat);
        selectMap.put("存储流音频格式", audioFormat);
        String[] samplingRate = { "8KHZ", "22.05KHZ", "44.1KHZ", "48KHZ" };
        String[] vocalTract = { "单声道", "双声道" };
        selectMap.put("实时流采样率", samplingRate);
        selectMap.put("存储流采样率", samplingRate);
        selectMap.put("实时流声道数", vocalTract);
        selectMap.put("存储流声道数", vocalTract);
        // 通道号个数
        String[] channelNumber =
            { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35" };
        selectMap.put("通道号个数", channelNumber);
        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // 输出导文件
        OutputStream out;
        out = response.getOutputStream();
        // 将文档对象写入文件输出流
        export.write(out);
        out.close();
    }

    /**
     * 修改终端型号
     */
    @Override
    public void updateTerminalTypeInfo(TerminalTypeInfo info, String ipAddress) throws Exception {
        // 根据id查询修改前的终端型号信息
        TerminalTypeInfo beforeInfo = getTerminalTypeInfoById(info.getId());
        if (beforeInfo != null) {
            // 判断是否修改了终端型号视频参数设置
            // 修改前是否支持视频 0:否; 1:是;
            int beforeVideoFlag = beforeInfo.getSupportVideoFlag();
            // 现在是否支持视频
            int nowVideoFlag = info.getSupportVideoFlag();
            // 修改前支持视频,修改后不支持视频
            if (beforeVideoFlag == 1 && nowVideoFlag == 0) {
                // 删除终端型号关联的视频参数设置
                deleteChannelSet(beforeInfo.getDeviceChannelId());
                info.setDeviceChannelId("");
            } else if (beforeVideoFlag == 1 && nowVideoFlag == 1) { // 修改前和修改后都支持视频
                // 修改前和修改后都支持视频,不需要判断通道个数的是否改变,直接删除之前的通道号,将修改后的通道号新增进去
                // 删除修改之前的通道号,删除
                deleteBeforeChannel(beforeInfo);
                // 获取修改后的通道号,新增
                addChannelParam(info);
            } else if (beforeVideoFlag == 0 && nowVideoFlag == 1) { // 修改前不支持视频,修改后支持视频
                addChannelParam(info);
            }
            // 更新终端型号数据
            terminalTypeDao.updateTerminalType(info);
            saveTerminalTypeInfoToRedis(info); // 维护缓存
            String message =
                "修改终端型号 (" + "厂商: " + info.getTerminalManufacturer() + ", 型号: " + info.getTerminalType() + ")";
            logSearchService.addLog(ipAddress, message, "3", "");
        }
    }

    /**
     * 将终端型号信息存入缓存
     * @param info
     */
    private void saveTerminalTypeInfoToRedis(TerminalTypeInfo info) {
        if (info != null) {
            // 将终端型号信息存入缓存
            RedisHelper.delete(HistoryRedisKeyEnum.TERMINAL_TYPE_INFO.of(info.getId()));
            TerminalTypeRedisInfo redisInfo = new TerminalTypeRedisInfo();
            Integer[] optionals = new Integer[4];
            if (info.getSupportPhotoFlag() == 1) { // 终端型号支持拍照
                redisInfo.setPhotoParam(info.getCamerasNumber());
                optionals[3] = 1;
            } else {
                optionals[3] = 0;
            }
            if (info.getSupportVideoFlag() == 1) { // 终端型号支持视频
                redisInfo.setVideoParam(info.getChannelNumber());
                optionals[2] = 1;
            } else {
                optionals[2] = 0;
            }
            if (info.getSupportDrivingRecorderFlag() == 1) { // 是否支持行驶记录仪
                optionals[1] = 1;
            } else {
                optionals[1] = 0;
            }
            if (info.getSupportMonitoringFlag() == 1) { //  是否支持语音监听
                optionals[0] = 1;
            } else {
                optionals[0] = 0;
            }
            String result = StringUtils.join(optionals);
            if (StringUtils.isNotBlank(result) && !"0000".equals(result)) {
                redisInfo.setOptional(Integer.valueOf(result, 2));
                RedisHelper.setString(
                    HistoryRedisKeyEnum.TERMINAL_TYPE_INFO.of(info.getId()), JSONObject.toJSONString(redisInfo));
            }
        }
    }

    /**
     * 删除修改之前的音视频参数
     */
    private void deleteBeforeChannel(TerminalTypeInfo beforeInfo) {
        // 判断id,修改前的更新,新增的存入数据库
        List<String> channelIdList = Arrays.asList(beforeInfo.getDeviceChannelId().split(","));
        if (!CollectionUtils.isEmpty(channelIdList)) {
            terminalTypeDao.deleteChannelSet(channelIdList);
        }
    }

    /**
     * 处理修改后的通道号个数小于修改前的通道号个数的情况,并返回id
     */
    private void addChannelParam(TerminalTypeInfo info) {
        List<DeviceChannelSettingInfo> settingInfo = info.getDeviceChannelSettingInfoList();
        String channelIds = "";
        if (!CollectionUtils.isEmpty(settingInfo)) {
            List<String> channelId =
                settingInfo.stream().map(DeviceChannelSettingInfo::getId).collect(Collectors.toList());
            info.setDeviceChannelId(String.join(",", channelId));
            // 批量新增
            terminalTypeDao.addChannelSetParam(settingInfo);
            channelIds = String.join(",", channelId);
        }
        info.setDeviceChannelId(channelIds);
    }

    /**
     * 更新视频参数设置(循环insert -> 后面可优化为批量增加,提升效率)
     */
    private void updateChannelSet(List<DeviceChannelSettingInfo> settingInfo) {
        // 更新数据
        settingInfo.forEach(set -> terminalTypeDao.updateChannelSetParam(set));
    }

    /**
     * 删除终端型号关联的视频参数设置
     */
    private void deleteChannelSet(String channelIds) {
        if (StringUtils.isNotBlank(channelIds)) {
            List<String> channelId = Arrays.asList(channelIds.split(","));
            terminalTypeDao.deleteChannelSet(channelId);
        }
    }

    /**
     * 记录删除终端型号的日志
     */
    private void addDeleteTypeLog(List<TerminalTypeInfo> resultDeleteType, String ipAddress, int terminalTypeIdSize)
        throws Exception {
        StringBuilder message = new StringBuilder(); // 记录删除日志
        resultDeleteType.forEach(type -> {
            RedisHelper.delete(HistoryRedisKeyEnum.TERMINAL_TYPE_INFO.of(type.getId()));
            message.append("删除终端型号 ( 厂商:").append(type.getTerminalManufacturer()).append(",型号: ")
                .append(type.getTerminalType()).append(") <br/>");
        });
        if (terminalTypeIdSize == 1) { // 单个删除
            logSearchService.addLog(ipAddress, message.toString(), "3", "");
        } else { // 批量删除
            logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "批量删除终端型号");
        }
    }

    /**
     * 遍历集合,获取通道号id
     * @param terminalTypeInfo
     */
    private List<String> getChannelId(List<TerminalTypeInfo> terminalTypeInfo) {
        List<String> channelId = new ArrayList<>();
        if (!CollectionUtils.isEmpty(terminalTypeInfo)) {
            List<String> channelIdStr =
                terminalTypeInfo.stream().map(TerminalTypeInfo::getDeviceChannelId).collect(Collectors.toList());
            channelId = channelIdStr.stream().flatMap((id) -> filterString(id)).collect(Collectors.toList());
        }
        return channelId;
    }

    /**
     * 字符串转换为List
     */
    private Stream<String> filterString(String channelId) {
        List<String> ids = new ArrayList<>();
        if (StringUtils.isNotBlank(channelId)) {
            ids = Arrays.asList(channelId.split(","));
        }
        return ids.stream();
    }

    /**
     * 导入终端型号信息
     */
    @Override
    public Map<String, Object> importTerminalType(MultipartFile multipartFile, String ipAddress) throws Exception {
        // 导入的文件
        ImportExcel importExcel = new ImportExcel(multipartFile, NUMBER_ONE, NUMBER_ZERO);
        // excel 转换成 list
        List<TerminalTypeInfo> importExcelDataList = importExcel.getDataList(TerminalTypeInfo.class, null);
        Integer total =
            (importExcelDataList != null && importExcelDataList.size() > 0 ? importExcelDataList.size() : 0);
        List<TerminalTypeInfo> resultImportList = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("flag", NUMBER_ZERO);
        StringBuilder errorMsg = new StringBuilder();
        String resultInfo = "";
        //  当前用户
        String userName = SystemHelper.getCurrentUsername();
        StringBuilder message = new StringBuilder();
        if (!CollectionUtils.isEmpty(importExcelDataList)) {
            List<String> terminalManufactureList = terminalTypeDao.getTerminalManufacturer(); // 终端厂商
            for (int index = 0; index < importExcelDataList.size(); index++) {
                TerminalTypeInfo type = importExcelDataList.get(index);
                // 验证数据的正确性
                // 终端厂商
                String terminalManufacturer = type.getTerminalManufacturer();
                if (StringUtils.isBlank(terminalManufacturer)) {
                    resultMap.put("flag", NUMBER_ZERO);
                    errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,终端厂商未填<br/>");
                    continue;
                }
                if (!terminalManufactureList.contains(terminalManufacturer)) {
                    resultMap.put("flag", NUMBER_ZERO);
                    errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,请选择已有的终端厂商<br/>");
                    continue;
                }
                // 终端型号
                String terminalType = type.getTerminalType();
                if (StringUtils.isBlank(terminalType)) {
                    resultMap.put("flag", NUMBER_ZERO);
                    errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,终端型号未填<br/>");
                    continue;
                }
                // 根据终端厂商和终端型号查询数据库中是否存在重复的记录
                boolean verifyFlag = verifyTerminalTypeByManufacturer(terminalType, terminalManufacturer);
                if (!verifyFlag) {
                    resultMap.put("flag", NUMBER_ZERO);
                    errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,终端型号已存在<br/>");
                    continue;
                }
                if (terminalType.length() > TERMINAL_TYPE_MAX_LENGTH) {
                    resultMap.put("flag", NUMBER_ZERO);
                    errorMsg.append("第").append(index + NUMBER_ONE)
                        .append("行数据,终端型号长度不能大于" + TERMINAL_TYPE_MAX_LENGTH + "<br/>");
                    continue;
                }
                if (terminalType.length() < TERMINAL_TYPE_MIN_LENGTH) {
                    resultMap.put("flag", NUMBER_ZERO);
                    errorMsg.append("第").append(index + NUMBER_ONE)
                        .append("行数据,终端型号长度不能小于" + TERMINAL_TYPE_MIN_LENGTH + "<br/>");
                    continue;
                }
                if (!terminalType.matches(TERMINAL_TYPE_PATTERN)) {
                    resultMap.put("flag", NUMBER_ZERO);
                    errorMsg.append("第").append(index + NUMBER_ONE)
                        .append("行数据,终端型号不符合命名规则,请输入中文、字母、数字或特殊符号*、-、_、#<br/>");
                    continue;
                }
                // 是否支持拍照
                String supportPhotoFlag = type.getSupportPhotoFlagStr();
                if (StringUtils.isBlank(supportPhotoFlag)) {
                    resultMap.put("flag", NUMBER_ZERO);
                    errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,是否支持拍照未填写<br/>");
                    continue;
                }
                if ("是".equals(supportPhotoFlag)) { // 支持拍照,判断摄像头个数是否为空
                    type.setSupportPhotoFlag(NUMBER_ONE);
                    // 未填写摄像头个数
                    // 摄像头个数
                    Integer camerasNumber = type.getCamerasNumber();
                    if (camerasNumber == null) {
                        resultMap.put("flag", NUMBER_ZERO);
                        errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,摄像头个数未填写<br/>");
                        continue;
                    }
                    if (camerasNumber > CAMERAS_NUMBER_MAX) {
                        resultMap.put("flag", NUMBER_ZERO);
                        errorMsg.append("第").append(index + NUMBER_ONE)
                            .append("行数据,摄像头个数不能超过" + CAMERAS_NUMBER_MAX + "个<br/>");
                        continue;
                    }
                    if (camerasNumber < CHANNEL_NUMBER_MIN) {
                        resultMap.put("flag", NUMBER_ZERO);
                        errorMsg.append("第").append(index + NUMBER_ONE)
                            .append("行数据,摄像头个数不能低于" + CHANNEL_NUMBER_MIN + "个<br/>");
                        continue;
                    }
                } else { // 不支持拍照,将摄像头个数设置为0
                    type.setSupportPhotoFlag(NUMBER_ZERO);
                    type.setCamerasNumber(NUMBER_ZERO);
                }

                // 是否支持行驶记录仪
                String supportDrivingRecorderFlag = type.getSupportDrivingRecorderFlagStr();
                if (StringUtils.isBlank(supportDrivingRecorderFlag)) {
                    resultMap.put("flag", NUMBER_ZERO);
                    errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,是否支持行驶记录仪未填写<br/>");
                    continue;
                }
                if ("是".equals(supportDrivingRecorderFlag)) {
                    type.setSupportDrivingRecorderFlag(NUMBER_ONE);
                } else {
                    type.setSupportDrivingRecorderFlag(NUMBER_ZERO);
                }

                // 是否支持监听
                String supportMonitoringFlag = type.getSupportMonitoringFlagStr();
                if (StringUtils.isBlank(supportMonitoringFlag)) {
                    resultMap.put("flag", NUMBER_ZERO);
                    errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,是否支持监听未填写<br/>");
                    continue;
                }
                if ("是".equals(supportMonitoringFlag)) {
                    type.setSupportMonitoringFlag(NUMBER_ONE);
                } else {
                    type.setSupportMonitoringFlag(NUMBER_ZERO);
                }

                // 是否支持视频
                String supportVideoFlag = type.getSupportVideoFlagStr();
                if (StringUtils.isBlank(supportVideoFlag)) {
                    resultMap.put("flag", NUMBER_ZERO);
                    errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,是否支持视频未填写<br/>");
                    continue;
                }

                if ("否".equals(supportVideoFlag)) { // 不支持视频
                    type.setAudioFormat(null);
                    type.setChannelNumber(null);
                    type.setSupportVideoFlag(NUMBER_ZERO);
                } else { // 支持视频
                    type.setSupportVideoFlag(NUMBER_ONE);
                    String audioFormatStr = type.getAudioFormatStr(); //音频格式
                    if (StringUtils.isBlank(audioFormatStr)) {
                        resultMap.put("flag", NUMBER_ZERO);
                        errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,实时流音频格式未填写<br/>");
                        continue;
                    }
                    Integer audioFormat = AUDIO_FORMAT.p2b(audioFormatStr, 0);
                    type.setAudioFormat(audioFormat);

                    String samplingRateStr = type.getSamplingRateStr(); //采样率
                    if (StringUtils.isBlank(samplingRateStr)) {
                        resultMap.put("flag", NUMBER_ZERO);
                        errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,实时流采样率未填写<br/>");
                        continue;
                    }
                    int samplingRate = SAMPLING_RATE.p2b(samplingRateStr, 0);
                    type.setSamplingRate(samplingRate);

                    String vocalTractStr = type.getVocalTractStr(); //音频格式
                    if (StringUtils.isBlank(vocalTractStr)) {
                        resultMap.put("flag", NUMBER_ZERO);
                        errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,实时流通道数未填写<br/>");
                        continue;
                    }
                    int vocalTract = VOCAL_TRACT.p2b(vocalTractStr, 0);
                    type.setVocalTract(vocalTract);
                    String storageAudioFormatStr = type.getStorageAudioFormatStr(); //音频格式
                    if (StringUtils.isBlank(audioFormatStr)) {
                        resultMap.put("flag", NUMBER_ZERO);
                        errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,存储流音频格式未填写<br/>");
                        continue;
                    }
                    Integer storageAudioFormat = AUDIO_FORMAT.p2b(storageAudioFormatStr, 0);
                    type.setStorageAudioFormat(storageAudioFormat);

                    String storageSamplingRateStr = type.getStorageSamplingRateStr(); //采样率
                    if (StringUtils.isBlank(storageSamplingRateStr)) {
                        resultMap.put("flag", NUMBER_ZERO);
                        errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,存储流采样率未填写<br/>");
                        continue;
                    }
                    int storageSamplingRate = SAMPLING_RATE.p2b(storageSamplingRateStr, 0);
                    type.setStorageSamplingRate(storageSamplingRate);

                    String storageVocalTractStr = type.getStorageVocalTractStr(); //音频格式
                    if (StringUtils.isBlank(storageVocalTractStr)) {
                        resultMap.put("flag", NUMBER_ZERO);
                        errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,存储流通道数未填写<br/>");
                        continue;
                    }
                    int storageVocalTract = VOCAL_TRACT.p2b(storageVocalTractStr, 0);
                    type.setStorageVocalTract(storageVocalTract);

                    // 通道号个数
                    Integer channelNumber = type.getChannelNumber();
                    if (channelNumber == null) {
                        resultMap.put("flag", NUMBER_ZERO);
                        errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,通道号个数未填写<br/>");
                        continue;
                    }
                    if (channelNumber > CHANNEL_NUMBER_MAX) {
                        resultMap.put("flag", NUMBER_ZERO);
                        errorMsg.append("第").append(index + NUMBER_ONE)
                            .append("行数据,通道号个数最多为" + CHANNEL_NUMBER_MAX + "个<br/>");
                        continue;
                    }
                    if (channelNumber < CHANNEL_NUMBER_MIN) {
                        resultMap.put("flag", NUMBER_ZERO);
                        errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,通道号个数最少为1个<br/>");
                        continue;
                    }
                    // 支持视频,根据通道号个数添加通道号参数组
                    List<DeviceChannelSettingInfo> channelSet = addChannelSetParam(channelNumber);
                    type.setDeviceChannelSettingInfoList(channelSet);
                    List<String> channelId =
                        channelSet.stream().map(DeviceChannelSettingInfo::getId).collect(Collectors.toList());
                    type.setDeviceChannelId(String.join(",", channelId));
                }

                // 是否支持主动安全
                String activeSafety = type.getActiveSafetyStr();
                if (StringUtils.isBlank(activeSafety)) {
                    resultMap.put("flag", NUMBER_ZERO);
                    errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,是否支持主动安全未填写<br/>");
                    continue;
                }
                if ("是".equals(activeSafety)) {
                    type.setActiveSafety(NUMBER_ONE);
                } else {
                    type.setActiveSafety(NUMBER_ZERO);
                }

                // 是否为一体机
                String allInOne = type.getAllInOneStr();
                if (StringUtils.isBlank(allInOne)) {
                    resultMap.put("flag", NUMBER_ZERO);
                    errorMsg.append("第").append(index + NUMBER_ONE).append("行数据,是否为一体机未填写<br/>");
                    continue;
                }
                if ("是".equals(allInOne)) {
                    type.setAllInOne(NUMBER_ONE);
                } else {
                    type.setAllInOne(NUMBER_ZERO);
                }

                type.setCreateDataUsername(userName);
                message.append("导入终端型号 ( 厂商: ").append(type.getTerminalManufacturer()).append(", 型号: ")
                    .append(type.getTerminalType()).append(" ) <br/>");
                resultImportList.add(type);
            }
            if (resultImportList.size() > NUMBER_ZERO) {
                // 批量新增终端型号
                batchAddTerminalType(resultImportList);
                resultInfo +=
                    "导入成功" + resultImportList.size() + "条数据,导入失败" + (total - resultImportList.size()) + "条数据。";
                resultMap.put("flag", NUMBER_ONE);
                resultMap.put("errorMsg", errorMsg);
                resultMap.put("resultInfo", resultInfo);
                if (!message.toString().isEmpty()) { // 记录日志
                    logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "导入终端型号");
                }
            } else {
                resultMap.put("flag", NUMBER_ZERO);
                resultMap.put("errorMsg", errorMsg);
                resultMap.put("resultInfo", "成功导入0条数据。");
            }
        } else {
            resultMap.put("errorMsg", "请检查文件内容是否填写");
            resultMap.put("resultInfo", "无效文件。");
        }
        return resultMap;
    }

    /**
     * 根据终端厂商和终端型号校验数据库中是否有重复的记录
     * @param terminalType         终端型号
     * @param terminalManufacturer 终端厂商
     */
    @Override
    public boolean verifyTerminalTypeByManufacturer(String terminalType, String terminalManufacturer) throws Exception {
        if (StringUtils.isNotBlank(terminalType) && StringUtils.isNotBlank(terminalManufacturer)) {
            List<String> terminalTypes =
                terminalTypeDao.getTerminalTypeByTerminalManufacturer(terminalType, terminalManufacturer);
            if (terminalTypes == null || terminalTypes.size() == 0) { // 根据终端型号和终端厂商没有查询到重复的记录
                return true;
            }
        }
        return false;
    }

    /**
     * 为终端型号添加音视频参数
     * @param channelNumber 通道号个数
     */
    private List<DeviceChannelSettingInfo> addChannelSetParam(int channelNumber) {
        List<DeviceChannelSettingInfo> channelSettingInfoList = new ArrayList<>();
        Integer logicChannel;
        for (int i = 1; i <= channelNumber; i++) {
            DeviceChannelSettingInfo settingInfo = new DeviceChannelSettingInfo();
            // 物理通道号34 对应逻辑通道号36 物理通道号35 对应逻辑通道号37
            if (channelNumber == CHANNEL_NUMBER_MAX && (i == 34 || i == CHANNEL_NUMBER_MAX)) {
                logicChannel = i + 2;
            } else {
                logicChannel = i;
            }
            settingInfo.setPhysicsChannel(i); // 物理通道号
            settingInfo.setLogicChannel(logicChannel); // 逻辑通道号
            settingInfo.setChannelType(NUMBER_ZERO); // 通道类型
            settingInfo.setConnectionFlag(NUMBER_ONE); //  是否连接云台
            settingInfo.setSort(i); // 排序
            settingInfo.setStreamType(NUMBER_ONE); // 码流类型
            channelSettingInfoList.add(settingInfo);
        }
        return channelSettingInfoList;
    }

    /**
     * 批量新增终端型号
     */
    private void batchAddTerminalType(List<TerminalTypeInfo> typeInfoList) {
        if (!CollectionUtils.isEmpty(typeInfoList)) {
            List<DeviceChannelSettingInfo> channelSettingInfoList = new ArrayList<>();
            // 批量新增终端型号
            terminalTypeDao.addTerminalTypeToBatch(typeInfoList);
            typeInfoList.forEach(type -> {
                if (!CollectionUtils.isEmpty(type.getDeviceChannelSettingInfoList())) {
                    channelSettingInfoList.addAll(type.getDeviceChannelSettingInfoList());
                }
                saveTerminalTypeInfoToRedis(type);
            });
            if (channelSettingInfoList.size() > 0) {
                // 批量新增音视频参数
                terminalTypeDao.addChannelSetParam(channelSettingInfoList);
            }
        }
    }

    @Override
    public void exportTerminalType(String title, int type, String fuzzyParam, HttpServletResponse response)
        throws Exception {
        ExportExcel export = new ExportExcel(title, TerminalTypeInfo.class, NUMBER_ONE);
        List<TerminalTypeInfo> queryDate = terminalTypeDao.getAllTerminalType();
        if (!CollectionUtils.isEmpty(queryDate)) {
            if (StringUtils.isNotBlank(fuzzyParam)) {
                String upperCaseParam = fuzzyParam.toUpperCase();
                Set<TerminalTypeInfo> filterExportList =
                    queryDate.stream().filter(data -> data.getTerminalType().toUpperCase().contains(upperCaseParam))
                        .collect(Collectors.toSet());
                queryDate.clear();
                queryDate.addAll(filterExportList);
            }
            for (TerminalTypeInfo typeInfo : queryDate) {
                // 是否支持拍照
                Integer supportPhotoFlag = typeInfo.getSupportPhotoFlag();
                if (supportPhotoFlag != null) {
                    if (supportPhotoFlag == NUMBER_ZERO) { // 不支持拍照
                        typeInfo.setSupportPhotoFlagStr(VALUE_NO);
                        typeInfo.setCamerasNumber(NUMBER_ZERO); //  摄像头个数设置为0
                    } else {
                        typeInfo.setSupportPhotoFlagStr(VALUE_YES);
                    }
                }

                // 是否支持行驶记录仪
                Integer supportDrivingRecorderFlag = typeInfo.getSupportDrivingRecorderFlag();
                if (supportDrivingRecorderFlag != null) {
                    if (supportDrivingRecorderFlag == NUMBER_ZERO) { // 不支持行驶记录仪
                        typeInfo.setSupportDrivingRecorderFlagStr(VALUE_NO);
                    } else {
                        typeInfo.setSupportDrivingRecorderFlagStr(VALUE_YES);
                    }
                }
                if (typeInfo.getActiveSafety() != null) {
                    if (typeInfo.getActiveSafety() == NUMBER_ZERO) {
                        typeInfo.setActiveSafetyStr(VALUE_NO);
                    } else {
                        typeInfo.setActiveSafetyStr(VALUE_YES);
                    }
                }
                if (typeInfo.getAllInOne() != null) {
                    if (typeInfo.getAllInOne() == NUMBER_ZERO) {
                        typeInfo.setAllInOneStr(VALUE_NO);
                    } else {
                        typeInfo.setAllInOneStr(VALUE_YES);
                    }
                }

                // 是否支持监听
                Integer supportMonitoringFlag = typeInfo.getSupportMonitoringFlag();
                if (supportMonitoringFlag != null) {
                    if (supportMonitoringFlag == NUMBER_ZERO) {
                        typeInfo.setSupportMonitoringFlagStr(VALUE_NO);
                    } else {
                        typeInfo.setSupportMonitoringFlagStr(VALUE_YES);
                    }
                }

                // 是否支持视频
                Integer supportVideoFlag = typeInfo.getSupportVideoFlag();
                if (supportVideoFlag != null) {
                    if (supportVideoFlag == NUMBER_ZERO) { // 不支持视频
                        typeInfo.setSupportVideoFlagStr(VALUE_NO);
                        typeInfo.setAudioFormatStr("");
                        typeInfo.setChannelNumber(null);
                    } else { // 支持视频
                        typeInfo.setSupportVideoFlagStr(VALUE_YES);

                        // 音视频格式
                        Integer audioFormat = typeInfo.getAudioFormat();
                        String audioFormatStr = AUDIO_FORMAT.b2p(audioFormat);
                        typeInfo.setAudioFormatStr(audioFormatStr);
                        String samplingRateStr = SAMPLING_RATE.b2p(typeInfo.getSamplingRate());
                        typeInfo.setSamplingRateStr(samplingRateStr);
                        String vocalTractStr = VOCAL_TRACT.b2p(typeInfo.getVocalTract());
                        typeInfo.setVocalTractStr(vocalTractStr);
                        String storageAudioFormatStr = AUDIO_FORMAT.b2p(typeInfo.getStorageAudioFormat());
                        typeInfo.setStorageAudioFormatStr(storageAudioFormatStr);
                        String storageSamplingRateStr = SAMPLING_RATE.b2p(typeInfo.getStorageSamplingRate());
                        typeInfo.setStorageSamplingRateStr(storageSamplingRateStr);
                        String storageVocalTractStr = VOCAL_TRACT.b2p(typeInfo.getStorageVocalTract());
                        typeInfo.setStorageVocalTractStr(storageVocalTractStr);
                    }
                }
            }
        }
        export.setDataList(queryDate);
        // 输出导文件
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
    }

    @Override
    public List<String> getTerminalManufacturer() throws Exception {
        TreeSet<String> result = new TreeSet<>(terminalTypeDao.getTerminalManufacturer()); // 去重
        return new ArrayList<>(result);
    }

    @Override
    public void queryTerminalTypeSaveToRedis() {
        List<TerminalTypeInfo> queryData = terminalTypeDao.getAllTerminalType();
        queryData.forEach(data -> saveTerminalTypeInfoToRedis(data));
    }

    @Override
    public List<Map<String, Object>> getTerminalTypeByName(String name) {
        return terminalTypeDao.getTerminalTypeByFacturerName(name);
    }

    @Override
    public boolean repetitionMacAddress(String deviceId, String macAddress) {
        boolean matches = DEVICE_MAC_CHECKER.matcher(macAddress).matches();
        if (matches) {
            List<DeviceInfo> list = terminalTypeDao.getListByDeviceIdAndMacAddress(macAddress);
            if (StringUtils.isNotBlank(deviceId) && list.size() == 0) {
                return true;
            }
            if (StringUtils.isNotBlank(deviceId) && list.size() == 1) {
                if (deviceId.equals(list.get(0).getId())) {
                    return true;
                }
            }
            if (StringUtils.isBlank(deviceId) && list.size() == 0) {
                return true;
            }
        }
        return false;
    }
}
