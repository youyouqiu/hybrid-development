package com.zw.platform.service.obdManager.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.pagehelper.Page;
import com.google.common.collect.ImmutableMap;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.ObdEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.form.OBDDataInfo;
import com.zw.platform.domain.basicinfo.form.OBDManagerSettingForm;
import com.zw.platform.domain.basicinfo.form.OBDVehicleDataInfo;
import com.zw.platform.domain.basicinfo.form.OBDVehicleTypeForm;
import com.zw.platform.domain.basicinfo.query.OBDVehicleTypeQuery;
import com.zw.platform.domain.infoconfig.form.MonitorInfo;
import com.zw.platform.repository.modules.OBDManagerSettingDao;
import com.zw.platform.repository.modules.OBDVehicleTypeDao;
import com.zw.platform.service.obdManager.OBDVehicleTypeService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RedisUtil;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.common.ZipUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportExcel;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import com.zw.protocol.msg.t808.body.LocationInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.zw.platform.basic.constant.ObdEnum._0xF0141;

/**
 * @author Administrator
 */
@Service
public class OBDVehicleTypeServiceImpl implements OBDVehicleTypeService, IpAddressService {
    private static final Map<Integer, String> OBD_ALARM_MAP = ImmutableMap.<Integer, String>builder()
            .put(0, "????????????")
            .put(1, "??????????????????")
            .put(2, "???????????????")
            .put(3, "???????????????")
            .put(4, "???????????????")
            .put(5, "??????????????????????????????")
            .put(6, "??????????????????????????????")
            .put(7, "????????????")
            .put(8, "Flash????????????")
            .put(9, "CAN??????????????????")
            .put(10, "3D?????????????????????")
            .put(11, "RTC????????????")
            .build();

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private OBDVehicleTypeDao obdVehicleTypeDao;

    @Autowired
    private OBDManagerSettingDao obdManagerSettingDao;

    @Autowired
    private UserService userService;

    private static final String PATTERN = "^[A-Za-z0-9_#\\*\\u4e00-\\u9fa5\\-]+$";

    private static final String PATTERN2 = "^(0x)[A-F0-9]+$";

    @Override
    public Page<OBDVehicleTypeForm> getList(BaseQueryBean queryBean) {
        if (StringUtils.isNotEmpty(queryBean.getSimpleQueryParam())) {
            queryBean.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(queryBean.getSimpleQueryParam()));
        }
        return PageHelperUtil.doSelect(queryBean, () -> obdVehicleTypeDao.findList(queryBean));
    }

    @Override
    public JsonResultBean addVehicleType(OBDVehicleTypeForm form) {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = obdVehicleTypeDao.addVehicleType(form);
        String message = "";
        if (form.getType() == 0) {
            message = "??????OBD??????????????????" + form.getName() + "???";
        } else if (form.getType() == 1) {
            message = "???????????????OBD??????????????????" + form.getName() + "???";
        }
        if (flag) {
            logSearchService.addLog(getIpAddress(), message, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean updateVehicleType(OBDVehicleTypeForm form) {
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        OBDVehicleTypeForm old = obdVehicleTypeDao.findVehicleTypeById(form.getId());
        boolean flag = obdVehicleTypeDao.updateVehicleType(form);
        if (flag) {
            StringBuilder sb = new StringBuilder();
            if (old.getType() == 0) {
                sb.append("??????OBD??????????????????").append(old.getName()).append("???");
            } else if (old.getType() == 1) {
                sb.append("???????????????OBD??????????????????").append(old.getName()).append("???");
            }
            if (!form.getName().equals(old.getName())) {
                if (form.getType() == 0) {
                    sb.append("??????OBD??????????????????").append(form.getName()).append("???");
                } else if (form.getType() == 1) {
                    sb.append("??????OBD???????????????????????????").append(form.getName()).append("???");
                }
            }
            logSearchService.addLog(getIpAddress(), sb.toString(), "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean delete(String id) {
        Set<String> ids = Arrays.stream(id.split(",")).collect(Collectors.toSet());
        Map<String, OBDVehicleTypeForm> obdVehicleTypeFormMap = obdVehicleTypeDao.getByIds(ids)
            .stream()
            .collect(Collectors.toMap(OBDVehicleTypeForm::getId, Function.identity()));
        Map<String, List<OBDManagerSettingForm>> obdSettingMap = obdManagerSettingDao.getByObdVehicleTypeIds(ids)
            .stream()
            .collect(Collectors.groupingBy(OBDManagerSettingForm::getObdVehicleTypeId));
        //???????????????id
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        //????????????
        StringBuilder message = new StringBuilder();
        for (String tid : ids) {
            OBDVehicleTypeForm obdVehicleTypeForm = obdVehicleTypeFormMap.get(tid);
            Integer type = obdVehicleTypeForm.getType();
            String name = obdVehicleTypeForm.getName();
            //??????????????????obd??????
            List<OBDManagerSettingForm> obdSettingList = obdSettingMap.get(tid);
            if (CollectionUtils.isNotEmpty(obdSettingList)) {
                if (type == 0) {
                    message.append("OBD??????????????????").append(name).append("?????????OBD????????????????????????????????????????????????????????????</br>");
                } else if (type == 1) {
                    message.append("OBD???????????????????????????").append(name).append("?????????OBD????????????????????????????????????????????????????????????</br>");
                }
                continue;
            }
            result.add(tid);
            if (type == 0) {
                sb.append("??????OBD??????????????????").append(name).append("???");
            } else if (type == 1) {
                sb.append("??????OBD???????????????????????????").append(name).append("???");
            }
        }

        if (CollectionUtils.isEmpty(result)) {
            return new JsonResultBean(JsonResultBean.FAULT, message.toString());
        }
        boolean flag = obdVehicleTypeDao.delete(result);
        if (!flag) {
            return new JsonResultBean(JsonResultBean.FAULT, message.toString());
        }
        String ip = getIpAddress();
        if (ids.size() > 1) {
            logSearchService.addLog(ip, sb.toString(), "3", "batch", "OBD????????????????????????");
        } else {
            logSearchService.addLog(ip, sb.toString(), "3", "", "-", "");
        }
        if (message.length() > 0) {
            return new JsonResultBean(JsonResultBean.SUCCESS, message.toString());
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public Map<String, Object> importVehicleType(MultipartFile file) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        StringBuilder errorMsg = new StringBuilder();
        String resultInfo = "";
        ImportExcel importExcel = new ImportExcel(file, 1, 0);
        List<OBDVehicleTypeForm> excelDataList = importExcel.getDataList(OBDVehicleTypeForm.class);
        if (CollectionUtils.isEmpty(excelDataList)) {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", "??????????????????");
            resultMap.put("resultInfo", "????????????0?????????!");
            return resultMap;
        }
        int excelDataSize = excelDataList.size();
        String currentUsername = SystemHelper.getCurrentUsername();
        List<OBDVehicleTypeForm> importList = new ArrayList<>();
        StringBuilder message = new StringBuilder();
        List<OBDVehicleTypeForm> allObdVehicleTypeList = obdVehicleTypeDao.findAll();
        Set<String> alreadyExistCodeSet = allObdVehicleTypeList
            .stream()
            .map(OBDVehicleTypeForm::getCode)
            .collect(Collectors.toSet());
        Set<String> typeAndNameSet = allObdVehicleTypeList
            .stream()
            .map(obj -> {
                Integer type = obj.getType();
                String name = obj.getName();
                return type + name;
            }).collect(Collectors.toSet());
        Map<String, Integer> obdVehicleTypeCodeMap = new HashMap<>(excelDataSize);
        Map<String, Integer> obdVehicleTypeNameMap = new HashMap<>(excelDataSize);
        for (int i = 0; i < excelDataSize; i++) {
            OBDVehicleTypeForm obdVehicleTypeForm = excelDataList.get(i);
            String typeStr = obdVehicleTypeForm.getTypeStr();
            boolean typeStrNotNull = StringUtils.isNotBlank(typeStr);
            String name = obdVehicleTypeForm.getName();
            boolean nameNotBlank = StringUtils.isNotBlank(name);
            String code = obdVehicleTypeForm.getCode();
            boolean codeNotBlank = StringUtils.isNotBlank(code);
            boolean nameOrCodeIsRepeat = false;
            if (typeStrNotNull && nameNotBlank) {
                String typeAndNameKey = typeStr + name;
                Integer typeAndNameIndex = obdVehicleTypeNameMap.get(typeAndNameKey);
                if (typeAndNameIndex == null) {
                    obdVehicleTypeNameMap.put(typeAndNameKey, i);
                } else {
                    nameOrCodeIsRepeat = true;
                    errorMsg.append("???").append(i + 1).append("??????????????????/????????????????????????").append(typeAndNameIndex + 1)
                        .append("?????????,?????????").append(name).append("<br/>");
                }
            }
            if (codeNotBlank) {
                Integer codeIndex = obdVehicleTypeCodeMap.get(code);
                if (codeIndex == null) {
                    obdVehicleTypeCodeMap.put(code, i);
                } else {
                    nameOrCodeIsRepeat = true;
                    errorMsg.append("???").append(i + 1).append("????????????ID?????????").append(codeIndex + 1).append("?????????????????????")
                        .append(code).append("<br/>");
                }
            }
            if (nameOrCodeIsRepeat) {
                continue;
            }
            if (!nameNotBlank) {
                errorMsg.append("???").append(i + 1).append("????????????????????????/???????????????????????????????????????/???????????????????????????<br/>");
                continue;
            }
            if (!typeStrNotNull) {
                errorMsg.append("???").append(i + 1).append("????????????????????????????????????????????????????????????<br/>");
                continue;
            }
            if (!codeNotBlank) {
                errorMsg.append("???").append(i + 1).append("??????????????????ID??????????????????ID????????????<br/>");
                continue;
            }
            int type;
            if ("?????????".equals(typeStr)) {
                type = 0;
            } else if ("?????????".equals(typeStr)) {
                type = 1;
            } else {
                errorMsg.append("???").append(i + 1).append("???????????????????????????????????????????????????????????????<br/>");
                continue;
            }
            obdVehicleTypeForm.setType(type);
            if (name.length() > 20 || !Pattern.matches(PATTERN, name)) {
                errorMsg.append("???").append(i + 1).append("????????????????????????/?????????????????????????????????????????????????????????????????????*???-???_???#??????????????????20???<br/>");
                continue;
            }
            if (typeAndNameSet.contains(type + name)) {
                errorMsg.append("???").append(i + 1).append("????????????????????????/???????????????????????????????????????/?????????????????????<br/>");
                continue;
            }
            if (code.length() != 10 || !Pattern.matches(PATTERN2, code)) {
                errorMsg.append("???").append(i + 1).append("??????????????????ID?????????????????????10??????0x00000000~0xFFFFFFFF<br/>");
                continue;
            }
            if (alreadyExistCodeSet.contains(code)) {
                errorMsg.append("???").append(i + 1).append("??????????????????ID??????????????????ID??????<br/>");
                continue;
            }
            String description = obdVehicleTypeForm.getDescription();
            if (StringUtils.isNotBlank(description) && description.length() > 50) {
                errorMsg.append("???").append(i + 1).append("??????????????????????????????????????????1-50???<br/>");
                continue;
            }
            obdVehicleTypeForm.setCreateDataUsername(currentUsername);
            importList.add(obdVehicleTypeForm);
            if (type == 0) {
                message.append("??????OBD?????????????????????").append(name).append("???<br/>");
            } else {
                message.append("??????OBD??????????????????????????????").append(name).append("???<br/>");
            }
        }
        int importSize = importList.size();
        if (importSize > 0) {
            boolean flag = obdVehicleTypeDao.addByBatch(importList);
            if (flag) {
                resultInfo += "????????????" + importSize + "?????????,????????????" + (excelDataSize - importSize) + "????????????";
                resultMap.put("errorMsg", errorMsg);
                resultMap.put("resultInfo", resultInfo);
                logSearchService.addLog(getIpAddress(), message.toString(), "3", "batch", "??????OBD????????????");
            } else {
                resultMap.put("resultInfo", "????????????");
            }
            return resultMap;
        }
        resultMap.put("errorMsg", errorMsg);
        resultMap.put("resultInfo", "??????0?????????");
        return resultMap;
    }

    @Override
    public void generateTemplate(HttpServletResponse response) throws IOException {
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<String> exportList = new ArrayList<>();
        //??????
        headList.add("????????????");
        headList.add("????????????/???????????????");
        headList.add("??????ID");
        headList.add("??????");
        //?????????
        requiredList.add("????????????");
        requiredList.add("????????????/???????????????");
        requiredList.add("??????ID");
        //????????????
        exportList.add("?????????");
        exportList.add("????????????-??????");
        exportList.add("0xFFAA0001");
        exportList.add("???OBD??????");

        //??????????????????
        Map<String, String[]> selectMap = new HashMap<>();
        //0:?????????;1:?????????
        String[] type = { "?????????", "?????????" };
        selectMap.put("????????????", type);

        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // ???????????????
        OutputStream out = response.getOutputStream();
        // ????????????????????????????????????
        export.write(out);
        out.close();
    }

    @Override
    public void export(String title, int type, HttpServletResponse response, String query) throws IOException {
        if (StringUtils.isNotEmpty(query)) {
            query = StringUtil.mysqlLikeWildcardTranslation(query);
        }
        ExportExcel export = new ExportExcel(title, OBDVehicleTypeForm.class, type);
        List<OBDVehicleTypeForm> obdVehicleTypeFormList = obdVehicleTypeDao.findExport(query);
        for (OBDVehicleTypeForm form : obdVehicleTypeFormList) {
            if (form.getType() == 0) {
                form.setTypeStr("?????????");
            } else if (form.getType() == 1) {
                form.setTypeStr("?????????");
            }
        }
        export.setDataList(obdVehicleTypeFormList);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
    }

    @Override
    public boolean repetition(String name, Integer type, String id) {
        List<OBDVehicleTypeForm> list = obdVehicleTypeDao.findByNameAndType(name, type);
        if (list != null && list.size() > 0) {
            if (id != null) {
                for (OBDVehicleTypeForm form : list) {
                    if (id.equals(form.getId())) {
                        return true;
                    }
                }
            }
            //????????????
            return false;
        }
        return true;
    }

    @Override
    public OBDVehicleTypeForm findById(String id) {
        return obdVehicleTypeDao.findById(id);
    }

    @Override
    public boolean checkCode(String code, String id) {
        List<OBDVehicleTypeForm> list = obdVehicleTypeDao.findByCode(code);
        if (list != null && list.size() > 0) {
            if (id != null) {
                for (OBDVehicleTypeForm form : list) {
                    if (id.equals(form.getId())) {
                        return true;
                    }
                }
            }
            //code??????
            return false;
        }
        return true;
    }

    @Override
    public JsonResultBean getObdVehicleDataReport(String monitorId, String startTimeStr, String endTimeStr)
        throws Exception {
        String currentUsername = SystemHelper.getCurrentUsername();
        RedisKey redisKey = HistoryRedisKeyEnum.OBD_VEHICLE_DATA_REPORT_FORM_DATA_KEY.of(currentUsername);
        if (RedisHelper.isContainsKey(redisKey)) {
            RedisHelper.delete(redisKey);
        }
        if (StringUtils.isBlank(monitorId) || StringUtils.isBlank(startTimeStr) || StringUtils.isBlank(endTimeStr)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        JSONObject msg = new JSONObject();
        JSONArray result = new JSONArray();
        long startTime = DateUtil.getStringToLong(startTimeStr, null) / 1000;
        long endTime = DateUtil.getStringToLong(endTimeStr, null) / 1000;
        List<OBDVehicleDataInfo> obdVehicleDataList = getObdVehicleData(monitorId, startTime, endTime);
        obdVehicleDataList.sort(Comparator.comparing(OBDVehicleDataInfo::getVtime));
        if (CollectionUtils.isEmpty(obdVehicleDataList)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        //??????obd??????
        for (OBDVehicleDataInfo obdInfo : obdVehicleDataList) {
            this.installObdInfo(obdInfo);
            result.add(obdInfo);
        }
        //??????redis??????????????????
        RedisHelper.addToList(redisKey,
            obdVehicleDataList.stream().sorted(Comparator.comparing(OBDVehicleDataInfo::getVtime).reversed())
                .map(JSON::toJSONString).collect(Collectors.toList()));
        RedisHelper.expireKey(redisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        msg.put("result", result);
        String msgResult = JSON.toJSONString(msg, SerializerFeature.DisableCircularReferenceDetect);
        msgResult = ZipUtil.compress(msgResult);
        return new JsonResultBean(msgResult);
    }

    private List<OBDVehicleDataInfo> getObdVehicleData(String vehicleId, long startTime, long endTime) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", vehicleId);
        params.put("startTime", String.valueOf(startTime));
        params.put("endTime", String.valueOf(endTime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_OBD_VEHICLE_DATA, params);
        return PaasCloudUrlUtil.getResultListData(str, OBDVehicleDataInfo.class);
    }

    @Override
    public void installObdInfo(OBDVehicleDataInfo obdInfo) {
        //??????
        obdInfo.setVtimeStr(DateUtil.getLongToDateStr(obdInfo.getVtime() * 1000, null));
        Optional.ofNullable(ObjectUtils.firstNonNull(obdInfo.getObdOriginalVehicleData(), obdInfo.getObdObj()))
                .filter(StringUtils::isNotBlank)
                .map(JSON::parseObject)
                .ifPresent(obdJsonObject -> {
                    final Map<Integer, String> obdMap = this.prepareObdDataStream(obdJsonObject, true);
                    obdMap.forEach((id, value) -> ObdEnum.of(id).ifPresent(e -> e.set(obdInfo, e.parseValue(value))));
                });
        obdInfo.setObdOriginalVehicleData(null);
    }

    @Override
    public List<String> getBandObdSensorVehicle() {
        //???????????????????????????
        List<String> userOwnVehicleIds = VehicleUtil.sortVehicles(userService.getCurrentUserMonitorIds());
        List<String> bandSensorVehicle = obdVehicleTypeDao.getBandObdSensorMonitorId();
        //????????? obd??????????????????
        if (CollectionUtils.isEmpty(bandSensorVehicle) || CollectionUtils.isEmpty(userOwnVehicleIds)) {
            return new ArrayList<>();
        }
        return userOwnVehicleIds.stream().filter(bandSensorVehicle::contains).collect(Collectors.toList());
    }

    @Override
    public PageGridBean getOBDVehicleDataTable(OBDVehicleTypeQuery query) {
        String currentUsername = SystemHelper.getCurrentUsername();
        RedisKey redisKey = HistoryRedisKeyEnum.OBD_VEHICLE_DATA_REPORT_FORM_DATA_KEY.of(currentUsername);
        List<String> obdVehicleDataTableJsonStrList =
            RedisHelper.getList(redisKey, (query.getStart() + 1), (query.getStart() + query.getLimit()));
        if (CollectionUtils.isEmpty(obdVehicleDataTableJsonStrList)) {
            return new PageGridBean(query, new Page<>(), true);
        }
        List<OBDVehicleDataInfo> obdVehicleDataTableList =
            obdVehicleDataTableJsonStrList.stream().map(jsonStr -> JSON.parseObject(jsonStr, OBDVehicleDataInfo.class))
                .collect(Collectors.toList());
        Page<OBDVehicleDataInfo> result = RedisUtil.queryPageList(obdVehicleDataTableList, query, redisKey);
        return new PageGridBean(query, result, true);
    }

    @Override
    public List<OBDVehicleTypeForm> findAll() {
        return obdVehicleTypeDao.findAll();
    }

    @Override
    public JsonResultBean findByCode(String code) {
        return new JsonResultBean(obdVehicleTypeDao.findByCode(code));
    }

    @Override
    public OBDVehicleDataInfo convertStreamToObdInfo(LocationInfo info) {
        OBDVehicleDataInfo obdInfo = new OBDVehicleDataInfo();
        obdInfo.setAddress(info.getPositionDescription());
        obdInfo.setGpsTime(info.getGpsTime());
        obdInfo.setUploadtime(info.getUploadtime());
        MonitorInfo monitorInfo = info.getMonitorInfo();
        if (monitorInfo == null) {
            return obdInfo;
        }
        obdInfo.setId(monitorInfo.getMonitorId());
        obdInfo.setPlateNumber(monitorInfo.getMonitorName());
        obdInfo.setGroupName(monitorInfo.getGroupName());
        final JSONObject obd = info.getObd();
        if (null == obd) {
            return obdInfo;
        }
        Map<Integer, String> obdDataStreamInfoMap = this.prepareObdDataStream(obd, true);
        for (Map.Entry<Integer, String> entry : obdDataStreamInfoMap.entrySet()) {
            final Integer id = entry.getKey();
            final String value = entry.getValue();
            ObdEnum.of(id).ifPresent(e -> e.set(obdInfo, e.parseValue(value)));
        }
        return obdInfo;
    }

    @Override
    public List<OBDDataInfo> convertStreamToObdList(LocationInfo info) {
        final List<OBDDataInfo> result = new ArrayList<>();
        final JSONObject obd = info.getObd();
        if (null == obd) {
            return result;
        }
        Map<Integer, String> obdDataStreamInfoMap = this.prepareObdDataStream(obd, true);
        for (Map.Entry<Integer, String> entry : obdDataStreamInfoMap.entrySet()) {
            final Integer id = entry.getKey();
            final String value = entry.getValue();
            ObdEnum.of(id).ifPresent(e -> result.add(new OBDDataInfo(e.getDisplayName(), e.parseValue(value))));
        }
        return result;
    }

    @Override
    public Map<Integer, String> prepareObdDataStream(JSONObject obdJsonObject, boolean needAlarmInfo) {
        JSONArray streamList = obdJsonObject.getJSONArray("streamList");
        if (CollectionUtils.isEmpty(streamList)) {
            return new HashMap<>(0);
        }
        final DecimalFormat decimalFormat = new DecimalFormat("#0.##");
        final Map<Integer, String> result = new HashMap<>((int) (streamList.size() / .75 + 1));
        for (int i = 0, len = streamList.size(); i < len; i++) {
            JSONObject obdDataStream = streamList.getJSONObject(i);
            Integer id = obdDataStream.getInteger("id");
            Object value = obdDataStream.get("value");
            if (id == null || value == null) {
                continue;
            }
            final Optional<ObdEnum> optional = ObdEnum.of(id);
            if (!optional.isPresent()) {
                continue;
            }
            final ObdEnum obdEnum = optional.get();
            if (obdEnum == ObdEnum._0xF0FF) {
                if (needAlarmInfo) {
                    String alarmMessage = this.convertObdAlarmInfo(value);
                    if (StringUtils.isNotEmpty(alarmMessage)) {
                        result.put(id, StringUtil.cut(alarmMessage, "", ","));
                    }
                }
                continue;
            }
            if (obdEnum == ObdEnum._0xF014) {
                final List<String> troubleCodes = obdDataStream.getJSONArray("value").toJavaList(String.class);
                result.put(id, this.convertObdTroubleCodeInfo(troubleCodes));
                result.put(_0xF0141.getId(), String.valueOf(troubleCodes.size()));
                continue;
            }

            if (obdEnum.isNumeric()) {
                try {
                    final String formattedNumber = decimalFormat.format(
                            new BigDecimal(value.toString()).setScale(2, BigDecimal.ROUND_HALF_UP));
                    result.put(id, formattedNumber);
                } catch (NumberFormatException e) {
                    result.put(id, value.toString());
                }
            } else {
                result.put(id, value.toString());
            }
        }
        return result;
    }

    private String convertObdAlarmInfo(Object value) {
        final StringBuilder builder = new StringBuilder();
        long longValue = Long.parseLong(value.toString());
        final Map<Integer, String> obdAlarmMap = OBD_ALARM_MAP;
        final int size = obdAlarmMap.size();
        for (int i = 0; i < size; i++) {
            if (((longValue >> i) & 1) == 1) {
                builder.append(obdAlarmMap.get(i)).append(",");
            }
        }
        return builder.length() > 0 ? builder.substring(0, builder.length() - 1) : builder.toString();
    }

    private String convertObdTroubleCodeInfo(List<String> troubleCodes) {
        final StringBuilder builder = new StringBuilder();
        for (String troubleCode : troubleCodes) {
            builder.append("[").append(troubleCode).append("]???");
        }
        return builder.length() > 0 ? builder.substring(0, builder.length() - 1) : builder.toString();
    }
}
