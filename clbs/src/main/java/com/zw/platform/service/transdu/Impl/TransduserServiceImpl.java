package com.zw.platform.service.transdu.Impl;

import com.github.pagehelper.Page;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.share.BaudRateUtil;
import com.zw.platform.domain.share.CompEnUtil;
import com.zw.platform.domain.share.FilterFactorUtil;
import com.zw.platform.domain.share.ParityCheckUtil;
import com.zw.platform.domain.share.UploadTimeUtil;
import com.zw.platform.domain.vas.f3.TransduserManage;
import com.zw.platform.domain.vas.f3.TransdusermonitorSet;
import com.zw.platform.domain.vas.f3.VeerManage;
import com.zw.platform.repository.vas.SensorSettingsDao;
import com.zw.platform.repository.vas.TransduserDao;
import com.zw.platform.service.redis.RedisVehicleService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.transdu.TransduserService;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.excel.ImportExcel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransduserServiceImpl implements TransduserService {
    private static Logger log = LogManager.getLogger(TransduserServiceImpl.class);

    @Autowired
    private TransduserDao transduserDao;

    @Autowired
    private SensorSettingsDao sensorSettingsDao;

    private RedisVehicleService redisVehicleService;

    @Autowired
    private LogSearchService logSearchService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${sensor.type.error}")
    private String sensorTypeError;

    @Value("${sensor.type.exist}")
    private String sensorTypeExist;

    @Autowired
    public void setRedisVehicleService(RedisVehicleService redisVehicleService) {
        this.redisVehicleService = redisVehicleService;
    }

    private static final String DELETE_ERROR_MSSAGE1 = "????????????????????????????????????????????????";

    private static final String DELETE_ERROR_MSSAGE2 = "??????????????????????????????????????????";

    @Override
    public Page<TransduserManage> getTransduserManage(int transduserType, String param) {
        Page<TransduserManage> transduserManages = transduserDao.findTransduserManageBytype(transduserType, param);
        for (TransduserManage transduserManage : transduserManages) {
            String baudReteName = BaudRateUtil.getBaudRateVal(transduserManage.getBaudrate());// ???????????????
            String oddEvenChenk = ParityCheckUtil.getParityCheckVal(transduserManage.getOddEvenCheck());// ??????????????????
            String compensateName = CompEnUtil.getCompEnVal(transduserManage.getCompensate());// ??????????????????
            String filterFactorName = FilterFactorUtil.getFilterFactorVal(transduserManage.getFilterFactor());// ??????????????????
            String autoTime = UploadTimeUtil.getUploadTimeVal(transduserManage.getAutoTime());// ????????????????????????
            transduserManage.setBaudrateName(baudReteName);
            transduserManage.setOddEvenCheckName(oddEvenChenk);
            transduserManage.setCompensateName(compensateName);
            transduserManage.setFilterFactorName(filterFactorName);
            transduserManage.setAutotimeName(autoTime);
        }
        return transduserManages;
    }

    @Override
    public JsonResultBean addTransduserManage(TransduserManage transduserManage, String ipAddress) throws Exception {
        boolean flag = false;
        String msg = "";
        if (transduserManage.getSensorNumber() != null && transduserManage.getSensorType() != null) {
            // ?????????????????????
            TransduserManage t =
                getSensorByNumber(transduserManage.getSensorNumber(), transduserManage.getSensorType());
            // ?????????????????????????????????
            String sensorTypeName = getSensorType(transduserManage.getSensorType());
            // ?????????????????????????????????
            if (!transduserManage.getSensorNumber().matches("^[A-Za-z0-9_#\\*\\u4e00-\\u9fa5\\-]{1,25}")) {
                return new JsonResultBean(JsonResultBean.FAULT, sensorTypeError);
            }
            if (t != null && !t.getId().equals(transduserManage.getId())) {
                return new JsonResultBean(JsonResultBean.FAULT, sensorTypeName + sensorTypeExist);
            }
            transduserManage.setCreateDataUsername(SystemHelper.getCurrentUsername());
            flag = transduserDao.addTransduserManage(transduserManage);
            if (flag) {
                msg = "??????" + sensorTypeName + "???" + transduserManage.getSensorNumber();
            }
        }
        if (flag) {
            logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);

    }

    @Override
    public JsonResultBean updateTransduserManage(TransduserManage manager, String ipAddress) throws Exception {
        if (manager.getSensorNumber() != null && manager.getSensorType() != null) {
            // ?????????????????????
            TransduserManage t = getSensorByNumber(manager.getSensorNumber(), manager.getSensorType());
            // ?????????????????????????????????
            String sensorTypeName = getSensorType(manager.getSensorType());
            if (t != null && !t.getId().equals(manager.getId())) {
                return new JsonResultBean(JsonResultBean.FAULT, sensorTypeName + sensorTypeExist);
            }
            // ?????????????????????????????????
            if (!manager.getSensorNumber().matches("^[A-Za-z0-9_#\\*\\u4e00-\\u9fa5\\-]{1,25}")) {
                return new JsonResultBean(JsonResultBean.FAULT, sensorTypeName + sensorTypeError);
            }
            TransduserManage transduserManage = transduserDao.findTransduserManageById(manager.getId());
            manager.setUpdateDataTime(new Date());
            manager.setUpdateDataUsername(SystemHelper.getCurrentUsername());// ??????????????????
            boolean result = transduserDao.updateTransduserManage(manager);
            if (result) {
                // ????????????????????????????????????
                Map<String, Object> map = new HashMap<>();
                map.put("sensorId", manager.getId());
                List<TransdusermonitorSet> list = sensorSettingsDao.findVehicleIdBySensorIdOrsensorType(map);
                if (list != null && list.size() > 0) {
                    for (TransdusermonitorSet set : list) {
                        // ????????????
                        redisVehicleService.updateVehicleSensorBind(set);
                    }
                }
                String brforeType = transduserManage.getSensorNumber();
                String nowType = manager.getSensorNumber();
                String message = "";
                if (brforeType != null && !brforeType.isEmpty() && nowType != null && !nowType.isEmpty()) {
                    if (brforeType.equals(nowType)) {
                        message = "??????" + sensorTypeName + " : " + nowType;
                    } else {
                        message = "??????" + sensorTypeName + " : " + brforeType + " ??? : " + nowType;
                    }
                }
                logSearchService.addLog(ipAddress, message, "3", "", "-", "");
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean deleteTransduserManage(String id, String ipAddress) throws Exception {
        Integer boundNumber = findBoundNumberById(id);// ????????????????????????
        TransduserManage t = findTransduserManageById(id);// ?????????????????????
        boolean result = false;
        String msg = "";
        String message = "";
        String type = "";
        if (t != null && t.getSensorType() != null && t.getSensorNumber() != null) {
            String sensorTypeName = getSensorType(t.getSensorType());// ?????????????????????????????????
            if (boundNumber > 0) {
                if (t.getSensorType() == 1) {
                    type = "????????????";
                } else if (t.getSensorType() == 2) {
                    type = "????????????";
                } else {
                    type = "???????????????";
                }
                message = DELETE_ERROR_MSSAGE1 + type + DELETE_ERROR_MSSAGE2 + "<br/> ??????????????????????????????: <br/>" + t
                    .getSensorNumber();
                return new JsonResultBean(JsonResultBean.FAULT, message);
            }
            result = transduserDao.deleteTransduserManage(id);
            if (result) {
                msg = "??????" + sensorTypeName + "???" + t.getSensorNumber();
            }
        }
        if (result) {
            logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @Override
    public TransduserManage findTransduserManageById(String id) throws Exception {
        TransduserManage transduserManage = transduserDao.findTransduserManageById(id);
        String baudReteName = BaudRateUtil.getBaudRateVal(transduserManage.getBaudrate());// ???????????????
        String oddEvenChenk = ParityCheckUtil.getParityCheckVal(transduserManage.getOddEvenCheck());// ??????????????????
        String compensateName = CompEnUtil.getCompEnVal(transduserManage.getCompensate());// ??????????????????
        String filterFactorName = FilterFactorUtil.getFilterFactorVal(transduserManage.getFilterFactor());// ??????????????????
        String autoTime = UploadTimeUtil.getUploadTimeVal(transduserManage.getAutoTime());// ????????????????????????
        transduserManage.setBaudrateName(baudReteName);
        transduserManage.setOddEvenCheckName(oddEvenChenk);
        transduserManage.setCompensateName(compensateName);
        transduserManage.setFilterFactorName(filterFactorName);
        transduserManage.setAutotimeName(autoTime);
        return transduserManage;
    }

    @Override
    public TransduserManage getSensorByNumber(String sensorNumber, Integer sensorType) throws Exception {
        return transduserDao.getSensorByNumber(sensorNumber, sensorType);
    }

    // public boolean updateSensorVehicle(TransdusermonitorSet transdusermonitorSet) throws Exception {
    // TransduserManage transduserManage = new TransduserManage();
    // transduserManage.setId(transdusermonitorSet.getSensorId());// ?????????id
    // transduserManage.setSensorNumber(transdusermonitorSet.getSensorNumber());// ???????????????
    // transduserManage.setBaudrate(transdusermonitorSet.getBaudrate());// ?????????
    // transduserManage.setOddEvenCheck(transdusermonitorSet.getOddEvenCheck());// ????????????
    // transduserManage.setCompensate(transdusermonitorSet.getCompensate());// ????????????
    // transduserManage.setFilterFactor(transdusermonitorSet.getFilterFactor());// ????????????
    // transduserManage.setSensorType(transdusermonitorSet.getSensorType());// ???????????????
    // transduserManage.setRemark(transdusermonitorSet.getRemark());// ??????
    // boolean result = updateTransduserManage(transduserManage);
    // boolean flag = false;
    // if (result == true) {
    // transdusermonitorSet.setUpdateDataUsername(SystemHelper.getCurrentUsername());
    // flag = transduserDao.updateSensorVehicle(transdusermonitorSet);// ???????????????
    // }
    // return flag;
    // }
    //
    // public boolean deleteSensorVehicle(String id) throws Exception {
    // return transduserDao.deleteSensorVehicle(id);
    // }

    @Override
    public JsonResultBean updateBatchTransduserManages(List<String> ids, String ipAddress) throws Exception {
        StringBuilder msg = new StringBuilder();
        StringBuilder message = new StringBuilder();

        String type = "";
        for (String id : ids) {
            Integer boundNumber = findBoundNumberById(id);// ????????????????????????
            TransduserManage t = findTransduserManageById(id);// ?????????????????????
            if (t != null && t.getSensorNumber() != null && t.getSensorType() != null) {
                String sensorTypeName = getSensorType(t.getSensorType());
                if (t.getSensorType() == 1) {
                    type = "????????????";
                } else if (t.getSensorType() == 2) {
                    type = "????????????";
                } else {
                    type = "???????????????";
                }
                if (boundNumber > 0) {
                    message.append(t.getSensorNumber() + "<br/>");
                    continue;
                }
                boolean result = transduserDao.deleteTransduserManage(id);
                if (result) {
                    msg.append("??????").append(sensorTypeName).append(" : ").append(t.getSensorNumber()).append(" <br/>");
                }
            }
        }
        if (message.length() > 0) {
            String deleteFailMsg =
                DELETE_ERROR_MSSAGE1 + type + DELETE_ERROR_MSSAGE2 + "<br/> ??????????????????????????????: <br/>" + message.toString();
            return new JsonResultBean(JsonResultBean.FAULT, deleteFailMsg);
        }

        if (!msg.toString().isEmpty()) {
            logSearchService.addLog(ipAddress, msg.toString(), "3", "batch", "?????????????????????");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public Integer findBoundNumberById(String id) throws Exception {
        return transduserDao.checkBoundNumberById(id);
    }

    /**
     * ????????????
     */
    @Override
    public Map importSensor(MultipartFile multipartFile, Integer sensorType, String ipAddress) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("flag", 0);
        StringBuilder errorMsg = new StringBuilder();
        String resultInfo = "";
        // ???????????????
        ImportExcel importExcel = new ImportExcel(multipartFile, 1, 0);
        // excel ????????? list
        List<TransduserManage> list = importExcel.getDataList(TransduserManage.class, null);
        String temp;
        List<TransduserManage> importList = new ArrayList<>();
        StringBuilder errorMsgBuilder = new StringBuilder();
        StringBuilder message = new StringBuilder();
        // ??????????????????????????????
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                TransduserManage sensor = list.get(i);
                if ("REPEAT".equals(sensor.getSensorNumber())) {
                    continue;
                }
                // ?????????????????????
                for (int j = list.size() - 1; j > i; j--) {
                    if (!StringUtils.isBlank(list.get(j).getSensorNumber()) && list.get(j).getSensorNumber()
                        .equals(sensor.getSensorNumber())) {
                        temp = sensor.getSensorNumber();
                        errorMsg.append("???").append(i + 1).append("????????????????????????").append(j + 1).append("?????????????????????")
                            .append(temp).append("<br/>");
                        list.get(j).setSensorNumber("REPEAT");
                    }
                }
            }
            for (int i = 0; i < list.size(); i++) {
                TransduserManage sensor = list.get(i);
                if ("REPEAT".equals(sensor.getSensorNumber())) {
                    continue;
                }
                // ??????????????????
                if (StringUtils.isBlank(sensor.getSensorNumber())) {
                    resultMap.put("flag", 0);
                    errorMsg.append("???").append(i + 1).append("???????????????????????????<br/>");
                    continue;
                }
                // ?????????????????????????????????
                if (!sensor.getSensorNumber().matches("^[A-Za-z0-9_#\\*\\u4e00-\\u9fa5\\-]{1,25}")) {
                    errorMsg.append("???").append(i + 1).append("?????????????????????????????????????????????????????????????????????????????????*???-???_???#??????????????????25???<br/>");
                    continue;
                }
                // ????????????????????????
                String remark = null;
                if (sensorType != 3) {
                    remark = sensor.getRemark();
                } else {
                    remark = sensor.getFilterFactorName();
                }
                if (remark != null && !"".equals(remark)) {
                    if (remark.length() > 40) {
                        errorMsg.append("???").append(i + 1).append("???????????????????????????????????????????????????????????????????????????40???" + "<br/>");
                        continue;
                    }
                }
                // ?????????????????????????????????
                TransduserManage transduserManage =
                    transduserDao.getSensorByNumber(sensor.getSensorNumber(), sensorType);
                if (transduserManage != null) {
                    resultMap.put("flag", 0);
                    errorMsg.append("???").append(i + 1).append("?????????????????????").append(sensor.getSensorNumber())
                        .append("?????????????????????<br/>");
                    continue;
                }
                // ???????????????
                sensor.setSensorType(sensorType);
                // ????????????
                if (Converter.toBlank(sensor.getCompensateName()).equals("??????")) {
                    sensor.setCompensate(1);
                } else if (Converter.toBlank(sensor.getCompensateName()).equals("??????")) {
                    sensor.setCompensate(2);
                } else {
                    sensor.setCompensate(1);
                }
                // ????????????
                if (sensorType != 3) {
                    if (Converter.toBlank(sensor.getFilterFactorName()).equals("??????")) {
                        sensor.setFilterFactor(1);
                    } else if (Converter.toBlank(sensor.getFilterFactorName()).equals("??????")) {
                        sensor.setFilterFactor(2);
                    } else if (Converter.toBlank(sensor.getFilterFactorName()).equals("??????")) {
                        sensor.setFilterFactor(3);
                    } else {
                        sensor.setFilterFactor(2);
                    }
                } else {
                    sensor.setRemark(sensor.getFilterFactorName());
                    sensor.setFilterFactor(null);
                }
                sensor.setCreateDataTime(new Date());
                sensor.setCreateDataUsername(SystemHelper.getCurrentUsername());
                importList.add(sensor);
                message.append("??????").append(getSensorType(sensorType)).append(" : ").append(sensor.getSensorNumber())
                    .append(" <br/>");
            }
        } else {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", errorMsgBuilder.toString());
            resultMap.put("resultInfo", "????????????0?????????!");
            return resultMap;
        }
        // ??????????????????
        if (importList.size() > 0) {
            // ???????????????????????????????????????????????????????????????????????????
            boolean flag = transduserDao.addTransduserByBatch(importList);
            if (flag) {
                resultInfo += "????????????" + importList.size() + "?????????,????????????" + (list.size() - importList.size()) + "????????????";
                resultMap.put("flag", 1);
                resultMap.put("errorMsg", errorMsg);
                resultMap.put("resultInfo", resultInfo);
                logSearchService
                    .addLog(ipAddress, message.toString(), "3", "batch", "??????" + getSensorType(sensorType) + "?????????");
            } else {
                resultMap.put("flag", 0);
                resultMap.put("resultInfo", "???????????????");
                return resultMap;
            }
        } else {
            resultInfo += "????????????" + importList.size() + "?????????,????????????" + (list.size() - importList.size()) + "????????????";
            resultMap.put("flag", 1);
            resultMap.put("errorMsg", errorMsg);
            resultMap.put("resultInfo", resultInfo);
        }
        return resultMap;
    }

    /**
     * ????????????
     */
    @Override
    @MethodLog(name = "????????????", description = "????????????")
    public boolean generateTemplate(HttpServletResponse response, Integer sensorType) throws Exception {
        String sensorName = getSensorType(sensorType);
        String filename = sensorName + "????????????";
        ExportExcelUtil.setResponseHead(response, filename);
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // ??????
        headList.add("???????????????");
        headList.add("????????????");
        if (sensorType != 3) {
            headList.add("????????????");
        }
        headList.add("??????");
        // ????????????
        requiredList.add("???????????????");
        /*
         * requiredList.add("????????????"); if (sensorType != 3) { requiredList.add("????????????"); }
         */
        // ????????????????????????
        exportList.add("AOE-56826");
        exportList.add("??????");
        if (sensorType != 3) {
            exportList.add("??????");
        }
        exportList.add("");

        // ?????????????????????map
        Map<String, String[]> selectMap = new HashMap<String, String[]>();
        // ????????????
        String[] compensateName = { "??????", "??????" };
        selectMap.put("????????????", compensateName);
        // ????????????
        String[] filterFactorName = { "??????", "??????", "??????" };
        selectMap.put("????????????", filterFactorName);
        ExportExcel export = null;
        export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // ???????????????
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// ????????????????????????????????????
        out.close();
        return true;
    }

    @Override
    public boolean export(String title, int type, HttpServletResponse response, Integer sensorType) throws Exception {
        String transduserName = getSensorType(sensorType); // ???????????????
        String filename = transduserName + "??????"; // ?????????
        ExportExcelUtil.setResponseHead(response, filename);
        List<TransduserManage> transduserManage = getTransduserManage(sensorType, null); // ???????????????
        if (transduserManage != null) {
            ExportExcel export = null;
            if (sensorType != 3) {
                export = new ExportExcel(title, TransduserManage.class, type, null);
            } else {
                export = new ExportExcel(title, VeerManage.class, type, null);
            }
            export.setDataList(transduserManage);
            // ???????????????
            OutputStream out;
            out = response.getOutputStream();
            export.write(out);// ????????????????????????????????????
            out.flush();
            out.close();
            return true;
        }
        return false;
    }

    public String getSensorType(int sensorType) {
        if (sensorType == 1) {
            return "???????????????";
        } else if (sensorType == 2) {
            return "???????????????";
        } else if (sensorType == 3) {
            return "??????????????????";
        } else {
            return "???????????????";
        }
    }
}
