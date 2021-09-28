package com.zw.platform.service.sensor.impl;

import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.basicinfo.form.TyrePressureSensorForm;
import com.zw.platform.domain.basicinfo.query.TyrePressureSensorQuery;
import com.zw.platform.repository.vas.TransduserDao;
import com.zw.platform.repository.vas.TyrePressureSensorDao;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.sensor.TyrePressureSensorService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportExcel;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class TyrePressureSensorServiceImpl implements TyrePressureSensorService {

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private TyrePressureSensorDao tyrePressureSensorDao;

    @Autowired
    private TransduserDao transduserDao;

    @Autowired
    private UserService userService;

    private static final String DELETE_ERROR_MSSAGE1 = "部分传感器已经和车辆绑定了，到【";
    private static final String DELETE_ERROR_MSSAGE2 = "】中解除绑定后才可以删除哟！";
    private static final String DELETE_ERROR_MSSAGE3 = "<br/> 已绑定传感器型号如下: <br/>";
    private static final String PATTERN = "^[A-Za-z0-9_#\\*\\u4e00-\\u9fa5\\-]+$";

    @Override
    public List<TyrePressureSensorForm> getList(TyrePressureSensorQuery query) {

        if (StringUtils.isNotEmpty(query.getSimpleQueryParam())) {
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam()));
        }
        return PageHelperUtil.doSelect(query, () -> tyrePressureSensorDao.getList(query));
    }

    @Override
    public JsonResultBean saveSensor(TyrePressureSensorForm form) throws Exception {
        form.setCreateDataUsername(userService.getCurrentUserInfo().getUsername());
        boolean flag = tyrePressureSensorDao.saveSensor(form);
        if (flag) {
            String message = "新增胎压传感器：" + form.getSensorNumber();
            logSearchService.addLog(getIpAddress(), message, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public TyrePressureSensorForm findSensorById(String id) {
        return tyrePressureSensorDao.findSensorById(id);
    }

    @Override
    public JsonResultBean updateSensor(TyrePressureSensorForm form) throws Exception {
        form.setUpdateDataUsername(userService.getCurrentUserInfo().getUsername());
        TyrePressureSensorForm oldTyrePressureSensorForm = tyrePressureSensorDao.findSensorById(form.getId());
        boolean flag = tyrePressureSensorDao.updateSensor(form);
        if (flag) {
            StringBuilder message = new StringBuilder("");
            if (!oldTyrePressureSensorForm.getSensorNumber().equals(form.getSensorNumber())) {
                message.append("修改胎压传感器：").append(oldTyrePressureSensorForm.getSensorNumber()).append("为：")
                    .append(form.getSensorNumber());
            } else {
                message.append("修改胎压传感器：").append(form.getSensorNumber());
            }
            logSearchService.addLog(getIpAddress(), message.toString(), "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean deleteSensor(String id) throws Exception {
        TyrePressureSensorForm tyrePressureSensorForm = tyrePressureSensorDao.findSensorById(id);
        // 查看传感器是否有绑定关系
        Integer number = transduserDao.checkBoundNumberById(id);
        StringBuilder message = new StringBuilder("");
        if (number != null && number > 0) {
            message.append(DELETE_ERROR_MSSAGE1).append("胎压监测设置").append(DELETE_ERROR_MSSAGE2)
                .append(DELETE_ERROR_MSSAGE3).append(tyrePressureSensorForm.getSensorNumber());
            return new JsonResultBean(JsonResultBean.FAULT, message.toString());
        } else {
            boolean flag = tyrePressureSensorDao.deleteSensor(id);
            if (flag) {
                message.append("删除传感器：").append(tyrePressureSensorForm.getSensorNumber());
                logSearchService.addLog(getIpAddress(), message.toString(), "3", "", "-", "");
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @Override
    public boolean checkSensorName(String name, String id) {
        TyrePressureSensorForm tyrePressureSensorForm = tyrePressureSensorDao.findSensorByName(name);
        if (tyrePressureSensorForm != null) {
            // id不为空  修改时校验
            return StringUtils.isNotEmpty(id) && id.equals(tyrePressureSensorForm.getId());
        }
        return true;
    }

    @Override
    public JsonResultBean deleteMore(String ids) throws Exception {
        String[] moreId = ids.split(",");
        StringBuilder errorMessage = new StringBuilder("");
        StringBuilder message = new StringBuilder("");
        if (moreId != null && moreId.length > 0) {
            for (String id : moreId) {
                TyrePressureSensorForm tyrePressureSensorForm = tyrePressureSensorDao.findSensorById(id);
                // 查看传感器是否有绑定关系
                Integer number = transduserDao.checkBoundNumberById(id);
                if (number != null && number > 0) {
                    errorMessage.append(tyrePressureSensorForm.getSensorNumber()).append("</br>");
                } else {
                    boolean flag = tyrePressureSensorDao.deleteSensor(id);
                    if (flag) {
                        message.append(tyrePressureSensorForm.getSensorNumber()).append("</br>");
                    }
                }
            }
            if (message.length() > 0) {
                logSearchService.addLog(getIpAddress(), "批量删除工时传感器：" + message.toString(), "3", "batch", "批量删除胎压传感器");
            }
            if (errorMessage.length() > 0) {
                String errorResult =
                    DELETE_ERROR_MSSAGE1 + "胎压监测设置" + DELETE_ERROR_MSSAGE2 + DELETE_ERROR_MSSAGE3 + errorMessage
                        .toString();
                return new JsonResultBean(JsonResultBean.FAULT, errorResult);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public void generateTemplate(HttpServletResponse response) throws IOException {
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<String> exportList = new ArrayList<>();
        //表头
        headList.add("传感器型号（必填）");
        headList.add("补偿使能");
        headList.add("滤波系数");
        headList.add("备注");
        //必填项
        requiredList.add("传感器型号（必填）");
        //默认数据
        exportList.add("F3-tyrepressure-Sensor");
        exportList.add("使能");
        exportList.add("平滑");
        exportList.add("传感器功能稳定！");

        //组装下拉列表
        Map<String, String[]> selectMap = new HashMap<>();
        //奇偶校验（1：奇校验；2：偶校验；3：无校验）
        String[] oddEvenCheck = { "平滑", "平稳", "实时" };
        selectMap.put("滤波系数", oddEvenCheck);
        //补偿使能（1:使能,2:禁用）
        String[] compensate = { "使能", "禁用" };
        selectMap.put("补偿使能", compensate);

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

    @Override
    public void exportSensor(String title, int type, HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(title, TyrePressureSensorForm.class, type, null);
        TyrePressureSensorQuery query = new TyrePressureSensorQuery();
        List<TyrePressureSensorForm> list = tyrePressureSensorDao.getList(query);
        for (TyrePressureSensorForm form : list) {
            if (form.getCompensate() != null) {
                switch (form.getCompensate()) {
                    case 1:
                        form.setCompensateName("使能");
                        break;
                    case 2:
                        form.setCompensateName("禁用");
                        break;
                    default:
                        break;
                }
            }
            if (form.getFilterFactor() != null) {
                switch (form.getFilterFactor()) {
                    case 1:
                        form.setFilterFactorName("实时");
                        break;
                    case 2:
                        form.setFilterFactorName("平滑");
                        break;
                    case 3:
                        form.setFilterFactorName("平稳");
                        break;
                    default:
                        break;
                }
            }
        }
        export.setDataList(list);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
    }

    @Override
    public Map<String, Object> importSensor(MultipartFile file) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        StringBuilder errorMsg = new StringBuilder();
        String resultInfo = "";
        ImportExcel importExcel = new ImportExcel(file, 1, 0);
        List<TyrePressureSensorForm> list = importExcel.getDataList(TyrePressureSensorForm.class, null);
        List<TyrePressureSensorForm> importList = new ArrayList<>();
        StringBuilder message = new StringBuilder();
        String temp = "";
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getSensorNumber() == null || "REPEAT".equals(list.get(i).getSensorNumber())) {
                    continue;
                }
                for (int j = i + 1; j < list.size(); j++) {
                    if ((list.get(i).getSensorNumber()).equals(list.get(j).getSensorNumber())) {
                        temp = list.get(j).getSensorNumber();
                        errorMsg.append("第").append(i + 1).append("条【传感器型号】跟第").append(j + 1).append("条重复,值是 : ")
                            .append(temp + "<br/>");
                        list.get(j).setSensorNumber("REPEAT");
                    }
                }
            }
            String username = userService.getCurrentUserInfo().getUsername();
            for (int i = 0; i < list.size(); i++) {
                TyrePressureSensorForm form = list.get(i);
                String sensorNumber = form.getSensorNumber();
                if ("REPEAT".equals(list.get(i).getSensorNumber())) {
                    continue;
                }
                if (StringUtil.isNullOrEmpty(sensorNumber)) {
                    errorMsg.append("第" + (i + 1) + "条数据【传感器型号】异常，传感器型号不能为空<br/>");
                    continue;
                }
                //校验型号是否重复
                if (!checkSensorName(sensorNumber, null)) {
                    errorMsg.append("第" + (i + 1) + "条数据【传感器型号】异常，传感器型号重复<br/>");
                    continue;
                }
                if (sensorNumber.length() > 25) {
                    errorMsg.append("第" + (i + 1) + "条数据【传感器型号】异常，仅中文、字母、数字或特殊符号*、-、_、#，长度不超过25位<br/>");
                    continue;
                }
                if (!Pattern.matches(PATTERN, sensorNumber)) {
                    errorMsg.append("第" + (i + 1) + "条数据【传感器型号】异常，仅中文、字母、数字或特殊符号*、-、_、#，长度不超过25位<br/>");
                    continue;
                }
                String filterFactorForExport = form.getFilterFactorName();
                if (StringUtil.isNullOrEmpty(filterFactorForExport)) {
                    filterFactorForExport = "平滑";
                }
                if ("实时".equals(filterFactorForExport)) {
                    form.setFilterFactor(1);
                } else if ("平滑".equals(filterFactorForExport)) {
                    form.setFilterFactor(2);
                } else if ("平稳".equals(filterFactorForExport)) {
                    form.setFilterFactor(3);
                } else {
                    form.setFilterFactor(2);
                }
                String compensateForExport = form.getCompensateName();
                if (StringUtil.isNullOrEmpty(compensateForExport)) {
                    compensateForExport = "使能";
                }
                if ("使能".equals(compensateForExport)) {
                    form.setCompensate(1);
                } else if ("禁用".equals(compensateForExport)) {
                    form.setCompensate(2);
                } else {
                    form.setCompensate(2);
                }
                form.setCreateDataUsername(username);
                importList.add(form);
                message.append("导入传感器：" + form.getSensorNumber() + "<br/>");
            }
        }
        if (importList != null && importList.size() > 0) {
            boolean flag = tyrePressureSensorDao.addSensorByBatch(importList);
            if (flag) {
                resultInfo += "导入成功" + importList.size() + "条数据,导入失败" + (list.size() - importList.size()) + "条数据。";
                resultMap.put("errorMsg", errorMsg);
                resultMap.put("resultInfo", resultInfo);
                logSearchService.addLog(getIpAddress(), message.toString(), "3", "batch", "导入传感器");
            } else {
                resultMap.put("resultInfo", "导入失败");
                return resultMap;
            }
        } else {
            resultMap.put("errorMsg", errorMsg);
            resultMap.put("resultInfo", "导入0条数据");
            return resultMap;
        }
        return resultMap;
    }

    @Override
    public List<TyrePressureSensorForm> findAllSensor() {
        return tyrePressureSensorDao.findAllSensor();
    }

}
