package com.zw.platform.service.workhourmgt.impl;

import com.github.pagehelper.Page;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.vas.workhourmgt.WorkHourSensorInfo;
import com.zw.platform.domain.vas.workhourmgt.form.WorkHourSensorForm;
import com.zw.platform.domain.vas.workhourmgt.query.WorkHourSensorQuery;
import com.zw.platform.repository.vas.TransduserDao;
import com.zw.platform.repository.vas.WorkHourSensorDao;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.workhourmgt.WorkHourSensorService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportExcel;
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

/**
 * 工时传感器
 *
 * @author denghuabing
 * @version 1.0
 * @date 2018.5.29
 */
@Service
public class WorkHourSensorServiceImpl implements WorkHourSensorService {

    @Autowired
    private WorkHourSensorDao workHourSensorDao;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private TransduserDao transduserDao;

    private static final String DELETE_ERROR_MSSAGE1 = "部分传感器已经和车辆绑定了，到【";

    private static final String DELETE_ERROR_MSSAGE2 = "】中解除绑定后才可以删除哟！";

    private static final String PATTERN = "^[A-Za-z0-9_#\\*\\u4e00-\\u9fa5\\-]+$";

    /**
     * 型号校验
     *
     * @param sensorNumber
     * @return
     */
    @Override
    public boolean repetition(String sensorNumber, String id) {
        //通过传感器型号找是否重复
        WorkHourSensorForm form = workHourSensorDao.findWorkHourSensorByName(sensorNumber);
        if (form != null) {
            if (id != null) {
                //用id排除自身原本型号名可以重复
                WorkHourSensorForm workHourSensorForm = workHourSensorDao.findWorkHourSensorById(id);
                if (sensorNumber.equals(workHourSensorForm.getSensorNumber())) {
                    //用修改时的名称等于以前的名称也可以修改
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * 分页查询
     *
     * @param query
     * @return
     */
    @Override
    public Page<WorkHourSensorInfo> findByPage(WorkHourSensorQuery query) {
        //SimpleQueryParam不为空或''
        if (!StringUtil.isNullOrEmpty(query.getSimpleQueryParam())) {
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(
                    query.getSimpleQueryParam()
            ));
        }

        return PageHelperUtil.doSelect(query, () -> workHourSensorDao.findByPage(query));
    }

    /**
     * 新增工时传感器
     *
     * @param form
     * @param ipAddress
     * @return
     */
    @Override
    public JsonResultBean addWorkHourSensor(WorkHourSensorForm form, String ipAddress) throws Exception {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = workHourSensorDao.addWorkHourSensor(form);
        if (flag) {
            logSearchService.addLog(ipAddress, "新增工时传感器：" + form.getSensorNumber(),
                    "3", "", "_", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 根据id查询工时传感器信息
     *
     * @param id
     * @return
     */
    @Override
    public WorkHourSensorForm findWorkHourSensorById(String id) {
        return workHourSensorDao.findWorkHourSensorById(id);
    }

    /**
     * 修改工时传感器
     *
     * @param form
     * @param ipAddress
     * @return
     */
    @Override
    public JsonResultBean updateWorkHourSensor(WorkHourSensorForm form, String ipAddress) throws Exception {
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        //查询原来的型号
        String oldNumber = findWorkHourSensorById(form.getId()).getSensorNumber();
        boolean flag = workHourSensorDao.updateWorkHourSensor(form);
        if (flag) {
            //log日志
            String msg = "";
            if (!oldNumber.equals(form.getSensorNumber())) {
                msg += "修改工时传感器型号：" + oldNumber + "修改为：" + form.getSensorNumber();
            } else {
                msg += "修改工时传感器型号：" + oldNumber;
            }
            logSearchService.addLog(ipAddress, msg, "3", "", "_", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 删除工时传感器
     *
     * @param id
     * @param ipAddress
     * @return
     */
    @Override
    public JsonResultBean deleteWorkHourSensor(String id, String ipAddress) throws Exception {
        //检测传感器是否绑定了车辆   绑定了车辆不能删除
        Integer number = transduserDao.checkBoundNumberById(id);
        // 查询传感器
        WorkHourSensorForm workHourSensor = findWorkHourSensorById(id);
        if (number > 0 && workHourSensor.getSensorNumber() != null && workHourSensor.getSensorType() != null) {
            String msg = DELETE_ERROR_MSSAGE1 + "工时车辆设置" + DELETE_ERROR_MSSAGE2 + "<br/> 已绑定传感器型号如下: <br/>"
                    + workHourSensor.getSensorNumber();
            return new JsonResultBean(JsonResultBean.FAULT, msg);
        }
        boolean flag = workHourSensorDao.deleteWorkHourSensor(id);
        if (flag) {
            logSearchService.addLog(ipAddress, "删除工时传感器:" + workHourSensor.getSensorNumber(),
                    "3", "", "_", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 批量删除
     *
     * @param deltems
     * @param ipAddress
     * @return
     */
    @Override
    public JsonResultBean deleteMore(String deltems, String ipAddress) throws Exception {
        String[] workHourIds = deltems.split(",");
        //有绑定的型号的信息
        StringBuilder message = new StringBuilder();
        if (workHourIds != null) {
            //获取删除的id的型号
            StringBuilder msg = new StringBuilder();
            for (String id : workHourIds) {
                //检测传感器是否绑定了车辆   绑定了车辆不能删除
                Integer number = transduserDao.checkBoundNumberById(id);
                // 查询传感器
                WorkHourSensorForm workHourSensor = findWorkHourSensorById(id);
                if (number > 0 && workHourSensor.getSensorNumber() != null
                        && workHourSensor.getSensorType() != null) {
                    message.append(workHourSensor.getSensorNumber() + "<br/>");
                    continue;
                }
                boolean flag = workHourSensorDao.deleteWorkHourSensor(id);
                if (flag) {
                    msg.append(workHourSensor.getSensorNumber()).append("<br/>");
                }
            }
            if (message.length() > 0) {
                //有绑定车辆的传感器  不能删除
                String errorMag = DELETE_ERROR_MSSAGE1 + "工时车辆设置" + DELETE_ERROR_MSSAGE2
                        + "<br/> 已绑定传感器型号如下: <br/>" + message.toString();
                if (msg.length() > 0) {
                    //删掉的加日志
                    logSearchService.addLog(ipAddress, "批量删除工时传感器:" + msg.toString(),
                            "3", "batch", "批量删除工时传感器");
                }
                return new JsonResultBean(JsonResultBean.FAULT, errorMag);
            }
            if (msg.length() > 0) {
                logSearchService.addLog(ipAddress, "批量删除工时传感器：" + msg.toString(),
                        "3", "batch", "批量删除工时传感器");
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 创建模板
     *
     * @param response
     */
    @Override
    public void generateTemplate(HttpServletResponse response) throws IOException {
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<String> exportList = new ArrayList<>();
        //表头
        headList.add("传感器型号（必填）");
        headList.add("检测方式");
        headList.add("滤波系数");
        headList.add("波特率");
        headList.add("奇偶校验");
        headList.add("补偿使能");
        headList.add("备注");
        //必填项
        requiredList.add("传感器型号（必填）");
        requiredList.add("检测方式");
        requiredList.add("滤波系数");
        requiredList.add("波特率");
        requiredList.add("奇偶校验");
        requiredList.add("补偿使能");
        //默认数据
        exportList.add("F3-PosInv-Sensor");
        exportList.add("电压比较式");
        exportList.add("平滑");
        exportList.add("9600");
        exportList.add("无校验");
        exportList.add("使能");
        exportList.add("不知道");

        //组装下拉列表
        Map<String, String[]> selectMap = new HashMap<>();
        //检测方式(1:电压比较式;2:油耗阈值式;3:油耗波动式)
        String[] detectionMode = {"电压比较式", "油耗阈值式", "油耗波动式"};
        selectMap.put("检测方式", detectionMode);
        //滤波系数（1:实时,2:平滑,3:平稳）
        String[] filterFactor = {"平滑", "实时", "平稳"};
        selectMap.put("滤波系数", filterFactor);
        //波特率
        String[] baudRate = {"2400", "4800", "9600", "19200", "38400", "57600", "115200"};
        selectMap.put("波特率", baudRate);
        //奇偶校验（1：奇校验；2：偶校验；3：无校验）
        String[] oddEvenCheck = {"奇校验", "偶校验", "无校验"};
        selectMap.put("奇偶校验", oddEvenCheck);
        //补偿使能（1:使能,2:禁用）
        String[] compensate = {"使能", "禁用"};
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

    /**
     * 导出excel表
     *
     * @param title
     * @param type
     * @param response
     * @throws Exception
     */
    @Override
    public void exportWorkHourSensor(String title, int type, HttpServletResponse response)
            throws Exception {
        ExportExcel export = new ExportExcel(title, WorkHourSensorForm.class, type, null);
        List<WorkHourSensorForm> exportList = workHourSensorDao.exportWorkHourSensor();
        if (exportList != null && exportList.size() > 0) {
            for (int i = 0; i < exportList.size(); i++) {
                WorkHourSensorForm form = exportList.get(i);
                if (form.getOddEvenCheck() != null) {
                    if (form.getOddEvenCheck() == 1) {
                        form.setOddEvenCheckForExport("奇校验");
                    } else if (form.getOddEvenCheck() == 2) {
                        form.setOddEvenCheckForExport("偶校验");
                    } else if (form.getOddEvenCheck() == 3) {
                        form.setOddEvenCheckForExport("无校验");
                    } else {
                        form.setOddEvenCheckForExport("无校验");
                    }
                }
                if (form.getBaudRate() != null) {
                    switch (form.getBaudRate()) {
                        case 1:
                            form.setBaudRateForExport("2400");
                            break;
                        case 2:
                            form.setBaudRateForExport("4800");
                            break;
                        case 3:
                            form.setBaudRateForExport("9600");
                            break;
                        case 4:
                            form.setBaudRateForExport("19200");
                            break;
                        case 5:
                            form.setBaudRateForExport("38400");
                            break;
                        case 6:
                            form.setBaudRateForExport("57600");
                            break;
                        case 7:
                            form.setBaudRateForExport("115200");
                            break;
                        default:
                            form.setBaudRateForExport("9600");
                            break;
                    }
                }
                if (form.getCompensate() != null) {
                    if (form.getCompensate() == 1) {
                        form.setCompensateForExport("使能");
                    } else if (form.getCompensate() == 2) {
                        form.setCompensateForExport("禁用");
                    } else {
                        form.setCompensateForExport("禁用");
                    }
                }
                if (form.getFilterFactor() != null) {
                    if (form.getFilterFactor() == 1) {
                        form.setFilterFactorForExport("实时");
                    } else if (form.getFilterFactor() == 2) {
                        form.setFilterFactorForExport("平滑");
                    } else if (form.getFilterFactor() == 3) {
                        form.setFilterFactorForExport("平稳");
                    } else {
                        form.setFilterFactorForExport("平滑");
                    }
                }
                if (form.getDetectionMode() != null) {
                    if (form.getDetectionMode() == 1) {
                        form.setDetectionModeForExport("电压比较式");
                    } else if (form.getDetectionMode() == 2) {
                        form.setDetectionModeForExport("油耗阈值式");
                    } else if (form.getDetectionMode() == 3) {
                        form.setDetectionModeForExport("油耗波动式");
                    } else {
                        form.setDetectionModeForExport("电压比较式");
                    }
                }
            }
        }
        export.setDataList(exportList);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
    }

    /**
     * 批量导入
     *
     * @param file
     * @param ipAddress
     * @return
     */
    @Override
    public Map<String, Object> importWorkHourSensor(MultipartFile file, String ipAddress) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        StringBuilder errorMsg = new StringBuilder();
        String resultInfo = "";
        ImportExcel importExcel = new ImportExcel(file, 1, 0);
        List<WorkHourSensorForm> list = importExcel.getDataList(WorkHourSensorForm.class, null);
        List<WorkHourSensorForm> importList = new ArrayList<>();
        StringBuilder message = new StringBuilder();
        String temp = "";
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getSensorNumber() == null || "REPEAT".equals(list.get(i).getSensorNumber())) {
                    continue;
                }
                for (int j = i + 1; j < list.size(); j++) {
                    if ((list.get(i).getSensorNumber()).equals(
                            list.get(j).getSensorNumber())) {
                        temp = list.get(j).getSensorNumber();
                        errorMsg.append("第").append(i + 1).append("条【传感器型号】跟第").append(j + 1)
                                .append("条重复,值是 : ").append(temp + "<br/>");
                        list.get(j).setSensorNumber("REPEAT");
                    }
                }
            }
            for (int i = 0; i < list.size(); i++) {
                WorkHourSensorForm form = list.get(i);
                String sensorNumber = form.getSensorNumber();
                if ("REPEAT".equals(list.get(i).getSensorNumber())) {
                    continue;
                }
                if (StringUtil.isNullOrEmpty(sensorNumber)) {
                    errorMsg.append("第" + (i + 1) + "条数据【传感器型号】异常，传感器型号不能为空<br/>");
                    continue;
                }
                //校验型号是否重复
                if (!repetition(sensorNumber, null)) {
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
                String detectionModeForExport = form.getDetectionModeForExport();
                if (StringUtil.isNullOrEmpty(detectionModeForExport)) {
                    errorMsg.append("第" + (i + 1) + "条数据【检测方式】异常，检测方式不能为空<br/>");
                    continue;
                }
                if ("电压比较式".equals(detectionModeForExport)) {
                    form.setDetectionMode(1);
                } else if ("油耗阈值式".equals(detectionModeForExport)) {
                    form.setDetectionMode(2);
                } else if ("油耗波动式".equals(detectionModeForExport)) {
                    form.setDetectionMode(3);
                } else {
                    form.setDetectionMode(1);
                }
                String filterFactorForExport = form.getFilterFactorForExport();
                if (StringUtil.isNullOrEmpty(filterFactorForExport)) {
                    errorMsg.append("第" + (i + 1) + "条数据【滤波系数】异常，滤波系数不能为空<br/>");
                    continue;
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
                String baudRateForExport = form.getBaudRateForExport();
                if (StringUtil.isNullOrEmpty(baudRateForExport)) {
                    errorMsg.append("第").append(i + 1).append("条数据【波特率】异常，波特率不能为空<br/>");
                    continue;
                }
                switch (baudRateForExport) {
                    case "2400":
                        form.setBaudRate(1);
                        break;
                    case "4800":
                        form.setBaudRate(2);
                        break;
                    case "9600":
                        form.setBaudRate(3);
                        break;
                    case "19200":
                        form.setBaudRate(4);
                        break;
                    case "38400":
                        form.setBaudRate(5);
                        break;
                    case "57600":
                        form.setBaudRate(6);
                        break;
                    case "115200":
                        form.setBaudRate(7);
                        break;
                    default:
                        form.setBaudRate(3);
                        break;
                }
                String oddEvenCheckForExport = form.getOddEvenCheckForExport();
                if (StringUtil.isNullOrEmpty(oddEvenCheckForExport)) {
                    errorMsg.append("第" + (i + 1) + "条数据【奇偶校验】异常，奇偶校验不能为空<br/>");
                    continue;
                }
                if ("奇校验".equals(oddEvenCheckForExport)) {
                    form.setOddEvenCheck(1);
                } else if ("偶校验".equals(oddEvenCheckForExport)) {
                    form.setOddEvenCheck(2);
                } else if ("无校验".equals(oddEvenCheckForExport)) {
                    form.setOddEvenCheck(3);
                } else {
                    form.setOddEvenCheck(3);
                }
                String compensateForExport = form.getCompensateForExport();
                if (StringUtil.isNullOrEmpty(compensateForExport)) {
                    errorMsg.append("第" + (i + 1) + "条数据【使能补偿】异常，使能补偿不能为空<br/>");
                    continue;
                }
                if ("使能".equals(compensateForExport)) {
                    form.setCompensate(1);
                } else if ("禁用".equals(compensateForExport)) {
                    form.setCompensate(2);
                } else {
                    form.setCompensate(2);
                }
                form.setCreateDataUsername(SystemHelper.getCurrentUsername());
                importList.add(form);
                message.append("导入传感器：" + form.getSensorNumber() + "<br/>");
            }
        }
        if (importList != null && importList.size() > 0) {
            boolean flag = workHourSensorDao.addWorkHourSensorByBatch(importList);
            if (flag) {
                resultInfo += "导入成功" + importList.size() + "条数据,导入失败" + (list.size() - importList.size()) + "条数据。";
                resultMap.put("errorMsg", errorMsg);
                resultMap.put("resultInfo", resultInfo);
                logSearchService.addLog(ipAddress, message.toString(),
                        "3", "batch", "导入传感器");
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
}
