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

    private static final String DELETE_ERROR_MSSAGE1 = "部分传感器已经和车辆绑定了，到【";

    private static final String DELETE_ERROR_MSSAGE2 = "】中解除绑定后才可以删除哟！";

    @Override
    public Page<TransduserManage> getTransduserManage(int transduserType, String param) {
        Page<TransduserManage> transduserManages = transduserDao.findTransduserManageBytype(transduserType, param);
        for (TransduserManage transduserManage : transduserManages) {
            String baudReteName = BaudRateUtil.getBaudRateVal(transduserManage.getBaudrate());// 波特率名称
            String oddEvenChenk = ParityCheckUtil.getParityCheckVal(transduserManage.getOddEvenCheck());// 奇偶校验名称
            String compensateName = CompEnUtil.getCompEnVal(transduserManage.getCompensate());// 补偿使能名称
            String filterFactorName = FilterFactorUtil.getFilterFactorVal(transduserManage.getFilterFactor());// 滤波系数名称
            String autoTime = UploadTimeUtil.getUploadTimeVal(transduserManage.getAutoTime());// 自动上传时间名称
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
            // 查询传感器信息
            TransduserManage t =
                getSensorByNumber(transduserManage.getSensorNumber(), transduserManage.getSensorType());
            // 判断是什么类型的传感器
            String sensorTypeName = getSensorType(transduserManage.getSensorType());
            // 校验传感器编号是否正确
            if (!transduserManage.getSensorNumber().matches("^[A-Za-z0-9_#\\*\\u4e00-\\u9fa5\\-]{1,25}")) {
                return new JsonResultBean(JsonResultBean.FAULT, sensorTypeError);
            }
            if (t != null && !t.getId().equals(transduserManage.getId())) {
                return new JsonResultBean(JsonResultBean.FAULT, sensorTypeName + sensorTypeExist);
            }
            transduserManage.setCreateDataUsername(SystemHelper.getCurrentUsername());
            flag = transduserDao.addTransduserManage(transduserManage);
            if (flag) {
                msg = "新增" + sensorTypeName + "：" + transduserManage.getSensorNumber();
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
            // 查询传感器信息
            TransduserManage t = getSensorByNumber(manager.getSensorNumber(), manager.getSensorType());
            // 判断是什么类型的传感器
            String sensorTypeName = getSensorType(manager.getSensorType());
            if (t != null && !t.getId().equals(manager.getId())) {
                return new JsonResultBean(JsonResultBean.FAULT, sensorTypeName + sensorTypeExist);
            }
            // 校验传感器编号是否正确
            if (!manager.getSensorNumber().matches("^[A-Za-z0-9_#\\*\\u4e00-\\u9fa5\\-]{1,25}")) {
                return new JsonResultBean(JsonResultBean.FAULT, sensorTypeName + sensorTypeError);
            }
            TransduserManage transduserManage = transduserDao.findTransduserManageById(manager.getId());
            manager.setUpdateDataTime(new Date());
            manager.setUpdateDataUsername(SystemHelper.getCurrentUsername());// 当前操作用户
            boolean result = transduserDao.updateTransduserManage(manager);
            if (result) {
                // 查询传感器是否绑定了车辆
                Map<String, Object> map = new HashMap<>();
                map.put("sensorId", manager.getId());
                List<TransdusermonitorSet> list = sensorSettingsDao.findVehicleIdBySensorIdOrsensorType(map);
                if (list != null && list.size() > 0) {
                    for (TransdusermonitorSet set : list) {
                        // 维护缓存
                        redisVehicleService.updateVehicleSensorBind(set);
                    }
                }
                String brforeType = transduserManage.getSensorNumber();
                String nowType = manager.getSensorNumber();
                String message = "";
                if (brforeType != null && !brforeType.isEmpty() && nowType != null && !nowType.isEmpty()) {
                    if (brforeType.equals(nowType)) {
                        message = "修改" + sensorTypeName + " : " + nowType;
                    } else {
                        message = "修改" + sensorTypeName + " : " + brforeType + " 为 : " + nowType;
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
        Integer boundNumber = findBoundNumberById(id);// 查询绑定车辆条数
        TransduserManage t = findTransduserManageById(id);// 查询传感器信息
        boolean result = false;
        String msg = "";
        String message = "";
        String type = "";
        if (t != null && t.getSensorType() != null && t.getSensorNumber() != null) {
            String sensorTypeName = getSensorType(t.getSensorType());// 判断是什么类型的传感器
            if (boundNumber > 0) {
                if (t.getSensorType() == 1) {
                    type = "温度监测";
                } else if (t.getSensorType() == 2) {
                    type = "湿度监测";
                } else {
                    type = "正反转管理";
                }
                message = DELETE_ERROR_MSSAGE1 + type + DELETE_ERROR_MSSAGE2 + "<br/> 已绑定传感器型号如下: <br/>" + t
                    .getSensorNumber();
                return new JsonResultBean(JsonResultBean.FAULT, message);
            }
            result = transduserDao.deleteTransduserManage(id);
            if (result) {
                msg = "删除" + sensorTypeName + "：" + t.getSensorNumber();
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
        String baudReteName = BaudRateUtil.getBaudRateVal(transduserManage.getBaudrate());// 波特率名称
        String oddEvenChenk = ParityCheckUtil.getParityCheckVal(transduserManage.getOddEvenCheck());// 奇偶校验名称
        String compensateName = CompEnUtil.getCompEnVal(transduserManage.getCompensate());// 补偿使能名称
        String filterFactorName = FilterFactorUtil.getFilterFactorVal(transduserManage.getFilterFactor());// 滤波系数名称
        String autoTime = UploadTimeUtil.getUploadTimeVal(transduserManage.getAutoTime());// 自动上传时间名称
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
    // transduserManage.setId(transdusermonitorSet.getSensorId());// 传感器id
    // transduserManage.setSensorNumber(transdusermonitorSet.getSensorNumber());// 传感器型号
    // transduserManage.setBaudrate(transdusermonitorSet.getBaudrate());// 波特率
    // transduserManage.setOddEvenCheck(transdusermonitorSet.getOddEvenCheck());// 奇偶校验
    // transduserManage.setCompensate(transdusermonitorSet.getCompensate());// 补偿使能
    // transduserManage.setFilterFactor(transdusermonitorSet.getFilterFactor());// 滤波系数
    // transduserManage.setSensorType(transdusermonitorSet.getSensorType());// 传感器类别
    // transduserManage.setRemark(transdusermonitorSet.getRemark());// 备注
    // boolean result = updateTransduserManage(transduserManage);
    // boolean flag = false;
    // if (result == true) {
    // transdusermonitorSet.setUpdateDataUsername(SystemHelper.getCurrentUsername());
    // flag = transduserDao.updateSensorVehicle(transdusermonitorSet);// 修改传感器
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
            Integer boundNumber = findBoundNumberById(id);// 查询绑定车辆条数
            TransduserManage t = findTransduserManageById(id);// 查询传感器信息
            if (t != null && t.getSensorNumber() != null && t.getSensorType() != null) {
                String sensorTypeName = getSensorType(t.getSensorType());
                if (t.getSensorType() == 1) {
                    type = "温度监测";
                } else if (t.getSensorType() == 2) {
                    type = "湿度监测";
                } else {
                    type = "正反转管理";
                }
                if (boundNumber > 0) {
                    message.append(t.getSensorNumber() + "<br/>");
                    continue;
                }
                boolean result = transduserDao.deleteTransduserManage(id);
                if (result) {
                    msg.append("删除").append(sensorTypeName).append(" : ").append(t.getSensorNumber()).append(" <br/>");
                }
            }
        }
        if (message.length() > 0) {
            String deleteFailMsg =
                DELETE_ERROR_MSSAGE1 + type + DELETE_ERROR_MSSAGE2 + "<br/> 已绑定传感器型号如下: <br/>" + message.toString();
            return new JsonResultBean(JsonResultBean.FAULT, deleteFailMsg);
        }

        if (!msg.toString().isEmpty()) {
            logSearchService.addLog(ipAddress, msg.toString(), "3", "batch", "批量删除传感器");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public Integer findBoundNumberById(String id) throws Exception {
        return transduserDao.checkBoundNumberById(id);
    }

    /**
     * 导入文件
     */
    @Override
    public Map importSensor(MultipartFile multipartFile, Integer sensorType, String ipAddress) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("flag", 0);
        StringBuilder errorMsg = new StringBuilder();
        String resultInfo = "";
        // 导入的文件
        ImportExcel importExcel = new ImportExcel(multipartFile, 1, 0);
        // excel 转换成 list
        List<TransduserManage> list = importExcel.getDataList(TransduserManage.class, null);
        String temp;
        List<TransduserManage> importList = new ArrayList<>();
        StringBuilder errorMsgBuilder = new StringBuilder();
        StringBuilder message = new StringBuilder();
        // 校验需要导入的传感器
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                TransduserManage sensor = list.get(i);
                if ("REPEAT".equals(sensor.getSensorNumber())) {
                    continue;
                }
                // 列表中重复数据
                for (int j = list.size() - 1; j > i; j--) {
                    if (!StringUtils.isBlank(list.get(j).getSensorNumber()) && list.get(j).getSensorNumber()
                        .equals(sensor.getSensorNumber())) {
                        temp = sensor.getSensorNumber();
                        errorMsg.append("第").append(i + 1).append("条传感器型号跟第").append(j + 1).append("条重复，值是：")
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
                // 校验必填字段
                if (StringUtils.isBlank(sensor.getSensorNumber())) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i + 1).append("条数据必填字段未填<br/>");
                    continue;
                }
                // 校验传感器型号是否正确
                if (!sensor.getSensorNumber().matches("^[A-Za-z0-9_#\\*\\u4e00-\\u9fa5\\-]{1,25}")) {
                    errorMsg.append("第").append(i + 1).append("条数据【传感器型号】异常，仅中文、字母、数字或特殊符号*、-、_、#，长度不超过25位<br/>");
                    continue;
                }
                // 校验备注是否正确
                String remark = null;
                if (sensorType != 3) {
                    remark = sensor.getRemark();
                } else {
                    remark = sensor.getFilterFactorName();
                }
                if (remark != null && !"".equals(remark)) {
                    if (remark.length() > 40) {
                        errorMsg.append("第").append(i + 1).append("条数据【备注】异常，超过最大长度限制，最多只能输入40位" + "<br/>");
                        continue;
                    }
                }
                // 与数据库是否有重复数据
                TransduserManage transduserManage =
                    transduserDao.getSensorByNumber(sensor.getSensorNumber(), sensorType);
                if (transduserManage != null) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i + 1).append("条传感器型号“").append(sensor.getSensorNumber())
                        .append("”数据库已存在<br/>");
                    continue;
                }
                // 传感器类别
                sensor.setSensorType(sensorType);
                // 补偿使能
                if (Converter.toBlank(sensor.getCompensateName()).equals("使能")) {
                    sensor.setCompensate(1);
                } else if (Converter.toBlank(sensor.getCompensateName()).equals("禁用")) {
                    sensor.setCompensate(2);
                } else {
                    sensor.setCompensate(1);
                }
                // 滤波系数
                if (sensorType != 3) {
                    if (Converter.toBlank(sensor.getFilterFactorName()).equals("实时")) {
                        sensor.setFilterFactor(1);
                    } else if (Converter.toBlank(sensor.getFilterFactorName()).equals("平滑")) {
                        sensor.setFilterFactor(2);
                    } else if (Converter.toBlank(sensor.getFilterFactorName()).equals("平稳")) {
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
                message.append("导入").append(getSensorType(sensorType)).append(" : ").append(sensor.getSensorNumber())
                    .append(" <br/>");
            }
        } else {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", errorMsgBuilder.toString());
            resultMap.put("resultInfo", "成功导入0条数据!");
            return resultMap;
        }
        // 组装导入结果
        if (importList.size() > 0) {
            // 导入逻辑（暂时只有新增，具体导入逻辑还需需求确定）
            boolean flag = transduserDao.addTransduserByBatch(importList);
            if (flag) {
                resultInfo += "导入成功" + importList.size() + "条数据,导入失败" + (list.size() - importList.size()) + "条数据。";
                resultMap.put("flag", 1);
                resultMap.put("errorMsg", errorMsg);
                resultMap.put("resultInfo", resultInfo);
                logSearchService
                    .addLog(ipAddress, message.toString(), "3", "batch", "导入" + getSensorType(sensorType) + "传感器");
            } else {
                resultMap.put("flag", 0);
                resultMap.put("resultInfo", "导入失败！");
                return resultMap;
            }
        } else {
            resultInfo += "导入成功" + importList.size() + "条数据,导入失败" + (list.size() - importList.size()) + "条数据。";
            resultMap.put("flag", 1);
            resultMap.put("errorMsg", errorMsg);
            resultMap.put("resultInfo", resultInfo);
        }
        return resultMap;
    }

    /**
     * 生成模板
     */
    @Override
    @MethodLog(name = "生成模板", description = "生成模板")
    public boolean generateTemplate(HttpServletResponse response, Integer sensorType) throws Exception {
        String sensorName = getSensorType(sensorType);
        String filename = sensorName + "列表模板";
        ExportExcelUtil.setResponseHead(response, filename);
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // 表头
        headList.add("传感器型号");
        headList.add("补偿使能");
        if (sensorType != 3) {
            headList.add("滤波系数");
        }
        headList.add("备注");
        // 必填字段
        requiredList.add("传感器型号");
        /*
         * requiredList.add("补偿使能"); if (sensorType != 3) { requiredList.add("滤波系数"); }
         */
        // 默认设置一条数据
        exportList.add("AOE-56826");
        exportList.add("使能");
        if (sensorType != 3) {
            exportList.add("平滑");
        }
        exportList.add("");

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<String, String[]>();
        // 补偿使能
        String[] compensateName = { "使能", "禁用" };
        selectMap.put("补偿使能", compensateName);
        // 滤波系数
        String[] filterFactorName = { "实时", "平滑", "平稳" };
        selectMap.put("滤波系数", filterFactorName);
        ExportExcel export = null;
        export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // 输出导文件
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
        return true;
    }

    @Override
    public boolean export(String title, int type, HttpServletResponse response, Integer sensorType) throws Exception {
        String transduserName = getSensorType(sensorType); // 传感器类型
        String filename = transduserName + "管理"; // 文件名
        ExportExcelUtil.setResponseHead(response, filename);
        List<TransduserManage> transduserManage = getTransduserManage(sensorType, null); // 导出的数据
        if (transduserManage != null) {
            ExportExcel export = null;
            if (sensorType != 3) {
                export = new ExportExcel(title, TransduserManage.class, type, null);
            } else {
                export = new ExportExcel(title, VeerManage.class, type, null);
            }
            export.setDataList(transduserManage);
            // 输出导文件
            OutputStream out;
            out = response.getOutputStream();
            export.write(out);// 将文档对象写入文件输出流
            out.flush();
            out.close();
            return true;
        }
        return false;
    }

    public String getSensorType(int sensorType) {
        if (sensorType == 1) {
            return "温度传感器";
        } else if (sensorType == 2) {
            return "湿度传感器";
        } else if (sensorType == 3) {
            return "正反转传感器";
        } else {
            return "未知传感器";
        }
    }
}
