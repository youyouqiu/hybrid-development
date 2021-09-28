package com.zw.platform.service.mileageSensor.impl;

import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.share.BaudRateUtil;
import com.zw.platform.domain.share.CompEnUtil;
import com.zw.platform.domain.share.FilterFactorUtil;
import com.zw.platform.domain.share.ParityCheckUtil;
import com.zw.platform.domain.vas.mileageSensor.MileageSensor;
import com.zw.platform.domain.vas.mileageSensor.MileageSensorQuery;
import com.zw.platform.repository.vas.MileageSensorDao;
import com.zw.platform.service.mileageSensor.MileageSensorService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportExcel;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.BeanUtils;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p> Title:里程传感器基础信息ServiceImpl <p> Copyright: Copyright (c) 2016 <p> Company: ZhongWei <p> team: ZhongWeiTeam
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月16日 11:08
 */
@Service
public class MileageSensorServiceImpl implements MileageSensorService {
    @Autowired
    private MileageSensorDao mileageSensorDao;

    @Autowired
    private LogSearchService logSearchService;

    @Value("${mileage.sensor.type.use}")
    private String mileageSensorTypeUse;

    @Value("${set.success}")
    private String setSuccess;

    @Value("${mileage.sensor.type.exist}")
    private String mileageSensorTypeExist;

    @Value("${add.success}")
    private String addSuccess;

    private static final String DELETE_ERROR_MSSAGE = "部分传感器已经和车辆绑定了，到【里程监测设置】中解除绑定后才可以删除哟！";

    private static final String REGEXP = "^[A-Za-z0-9_#\\*\\u4e00-\\u9fa5\\-]{1,25}$";

    @Override
    public JsonResultBean addMileageSensor(MileageSensor mileageSensor, String ipAddress) throws Exception {
        MileageSensor t = findBySensorType(mileageSensor.getSensorType());
        if (t != null && !t.getId().equals(mileageSensor.getId())) {
            return new JsonResultBean(JsonResultBean.FAULT, mileageSensorTypeExist);
        }
        mileageSensor.setCreateDataTime(new Date());
        mileageSensor.setCreateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = mileageSensorDao.addMileageSensor(mileageSensor);
        if (flag) {
            String msg = "新增轮速传感器：" + mileageSensor.getSensorType();
            logSearchService.addLog(ipAddress, msg, "3", "里程传感器管理", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS, addSuccess);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean updateMileageSensor(MileageSensor mileageSensor, String ipAddress) throws Exception {
        MileageSensor t = findBySensorType(mileageSensor.getSensorType());
        if (t != null && !t.getId().equals(mileageSensor.getId())) {
            return new JsonResultBean(JsonResultBean.FAULT, mileageSensorTypeExist);
        }
        MileageSensor beforeMileageSensor = findById(mileageSensor.getId()); // 修改前的数据
        if (beforeMileageSensor != null) {
            mileageSensor.setUpdateDataTime(new Date());
            mileageSensor.setUpdateDataUsername(SystemHelper.getCurrentUsername());
            boolean flag = mileageSensorDao.updateMileageSensor(mileageSensor);
            if (flag) {
                String msg = "";
                String beforeType = beforeMileageSensor.getSensorType(); // 修改前的类型
                String type = mileageSensor.getSensorType(); // 修改后的类型
                if (!beforeType.isEmpty() && !type.isEmpty()) {
                    if (!beforeType.equals(type)) { // 修改了型号
                        msg =
                            "修改轮速传感器 : " + beforeMileageSensor.getSensorType() + " 为 " + mileageSensor.getSensorType();
                    } else {
                        msg = "修改轮速传感器：" + mileageSensor.getSensorType();
                    }
                    logSearchService.addLog(ipAddress, msg, "3", "里程传感器管理", "-", "");
                }
                // 维护传感器和车的缓存
                Map<String, Object> map = new HashMap<>();
                map.put("id", mileageSensor.getId());
                // 查询所有绑定传感器的车辆
                List<Map<String, Object>> list = mileageSensorDao.findBindingMonitor(map);
                if (list != null && list.size() > 0) {
                    // 具有绑定关系
                    map = list.get(0);
                    RedisHelper.addToHash(RedisKeyEnum.VEHICLE_MILEAGE_MONITOR_LIST.of(),
                            (String) map.get("id"), mileageSensor.getSensorType());
                }
                return new JsonResultBean(JsonResultBean.SUCCESS, setSuccess);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 批量添加轮速传感器
     * @param mileageSensors
     * @throws Exception
     */
    @Override
    public void addBatchMileageSensors(List<MileageSensor> mileageSensors) throws Exception {
        for (MileageSensor m : mileageSensors) {
            m.setCreateDataTime(new Date());
            m.setCreateDataUsername(SystemHelper.getCurrentUsername());
        }
        this.mileageSensorDao.addBatchMileageSensors(mileageSensors);
    }

    /**
     * 根据轮速传感器id删除轮速传感器信息(包括单个删除和批量删除)
     * @param mileageSensors
     * @param ipAddress
     * @return
     * @throws Exception
     */
    @Override
    public JsonResultBean deleteBatchMileageSensor(List<String> mileageSensors, String ipAddress) throws Exception {
        StringBuilder message = new StringBuilder();
        //没有绑定的传感器id
        List<String> unbindSensors = new ArrayList<String>();
        String bindSensor = "";
        //绑定的传感器型号日志记录
        StringBuilder deleteFailMsg = new StringBuilder();
        for (String id : mileageSensors) {
            if (!id.isEmpty()) {
                bindSensor = checkConfig(id); // 检查是否有配置
                if (!StringUtils.isEmpty(bindSensor)) { //该传感器有绑定配置
                    deleteFailMsg.append(bindSensor).append("</br>");
                    continue;
                }
                MileageSensor form = mileageSensorDao.findById(id);
                message.append("删除轮速传感器 : ").append(form.getSensorType()).append(" <br/>");
                unbindSensors.add(id);
            }
        }
        boolean flag = false;
        if (unbindSensors.size() > 0) {
            flag = mileageSensorDao.deleteBatchMileageSensor(unbindSensors);
            if (flag) {
                if (unbindSensors.size() == 1) {
                    logSearchService.addLog(ipAddress, message.toString(), "3", "", "-", "");
                } else {
                    logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "批量删除轮速传感器");
                }
            }
        }
        if (!StringUtils.isEmpty(deleteFailMsg.toString())) {
            return new JsonResultBean(JsonResultBean.SUCCESS,
                DELETE_ERROR_MSSAGE + "</br>" + "已绑定传感器型号如下：</br>" + deleteFailMsg);
        }
        return new JsonResultBean(flag);
    }

    /**
     * 根据轮速传感器id查询轮速传感器信息
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public MileageSensor findById(String id) throws Exception {
        MileageSensor m = this.mileageSensorDao.findById(id);
        this.setBean(m);
        return m;
    }

    /**
     * 分页查询轮速传感器信息
     * @param query
     * @return
     * @throws Exception
     */
    @Override
    public Page<MileageSensor> findByQuery(MileageSensorQuery query) throws Exception {
        if (query != null) {
            String simpleQueryParam = query.getSimpleQueryParam();
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(simpleQueryParam));
        }
        Page<MileageSensor> list = PageHelperUtil.doSelect(query, () -> mileageSensorDao.findByQuery(query));
        for (MileageSensor m : list) {
            this.setBean(m);
        }
        return list;
    }

    /**
     * 查询全部的轮速传感器
     * @return
     * @throws Exception
     */
    @Override
    public List<MileageSensor> findAll() throws Exception {
        return this.mileageSensorDao.findAll();
    }

    /**
     * 设置参数
     * @param ms
     */
    private void setBean(MileageSensor ms) throws Exception {
        ms.setFilterFactorStr(FilterFactorUtil.getFilterFactorVal(ms.getFilterFactor()));
        ms.setCompEnStr(CompEnUtil.getCompEnVal(ms.getCompEn()));
        ms.setParityCheckStr(ParityCheckUtil.getParityCheckVal(ms.getParityCheck()));
        ms.setBaudRateStr(BaudRateUtil.getBaudRateVal(ms.getBaudRate()));
    }

    /**
     * 根据轮速传感器型号查询轮速传感器信息
     * @param sensorType
     * @return
     * @throws Exception
     */
    @Override
    public MileageSensor findBySensorType(String sensorType) throws Exception {
        return this.mileageSensorDao.findBySensorType(sensorType);
    }

    /**
     * 查询轮速传感器被绑定的个数
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public String checkConfig(String id) throws Exception {
        return this.mileageSensorDao.checkConfig(id);
    }

    /**
     * 导入轮速传感器
     * @param multipartFile
     * @param ipAddress
     * @return
     * @throws Exception
     */
    @Override
    public Map addImportSensor(MultipartFile multipartFile, String ipAddress) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("flag", 0);
        StringBuilder errorMsg = new StringBuilder();
        String resultInfo = "";
        StringBuilder message = new StringBuilder();
        // 导入的文件
        ImportExcel importExcel = new ImportExcel(multipartFile, 1, 0);
        // excel 转换成 list
        List<MileageSensor> list = importExcel.getDataList(MileageSensor.class, null);
        String temp;
        List<MileageSensor> importList = new ArrayList<MileageSensor>();
        int cellNum = importExcel.getLastCellNum();
        if (cellNum != 5) {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", "请将导入文件按照模板格式整理后再导入");
            resultMap.put("resultInfo", "导入失败！");
            return resultMap;
        }
        // 校验需要导入的油箱
        if (list.size() == 0) {
            resultMap.put("errorMsg", "");
            resultMap.put("resultInfo", "无任何数据导入");
            return resultMap;
        }
        // 获取已入库的所有型号
        Page<MileageSensor> dblist = mileageSensorDao.findByQuery(null);
        // 将已入库的型号转换为map
        Map<String, MileageSensor> mappedMileageSensor =
            dblist.stream().collect(Collectors.toMap(MileageSensor::getSensorType, (p) -> p));
        for (int i = 0; i < list.size(); i++) {
            MileageSensor sensor = list.get(i);
            if ("REPEAT".equals(sensor.getSensorType())) {
                continue;
            }
            // 列表中重复数据
            for (int j = list.size() - 1; j > i; j--) {
                if (list.get(j).getSensorType().equals(sensor.getSensorType())) {
                    temp = sensor.getSensorType();
                    errorMsg.append("第").append(i + 1).append("行跟第").append(j + 1).append("行重复，值是：").append(temp)
                        .append("<br/>");
                    list.get(j).setSensorType("REPEAT");
                }
            }
        }
        for (int i = 0; i < list.size(); i++) {
            MileageSensor sensor = list.get(i);
            if ("REPEAT".equals(sensor.getSensorType())) {
                continue;
            }
            // 校验必填字段
            if (StringUtils.isBlank(sensor.getSensorType())) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据必填字段未填<br/>");
                continue;
            }

            // 校验必填字段
            if (!Pattern.matches(REGEXP, sensor.getSensorType())) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据【传感器型号】异常，仅中文、字母、数字或特殊符号*、-、_、#，长度不超过25位<br/>");
                continue;
            }

            // 与数据库是否有重复数据
            if (mappedMileageSensor.containsKey(sensor.getSensorType())) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据【传感器型号】异常，传感器型号为“").append(sensor.getSensorType())
                    .append("”已存在<br/>");
                continue;
            }

            // 奇偶校验
            if (Converter.toBlank(sensor.getParityCheckStr()).equals("奇校验")) {
                sensor.setParityCheck(1);
            } else if (Converter.toBlank(sensor.getParityCheckStr()).equals("偶校验")) {
                sensor.setParityCheck(2);
            } else if (Converter.toBlank(sensor.getParityCheckStr()).equals("无校验")) {
                sensor.setParityCheck(3);
            } else {
                sensor.setParityCheck(3);
            }
            // 波特率
            if (Converter.toBlank(sensor.getBaudRateStr()).equals("2400")) {
                sensor.setBaudRate(1);
            } else if (Converter.toBlank(sensor.getBaudRateStr()).equals("4800")) {
                sensor.setBaudRate(2);
            } else if (Converter.toBlank(sensor.getBaudRateStr()).equals("9600")) {
                sensor.setBaudRate(3);
            } else if (Converter.toBlank(sensor.getBaudRateStr()).equals("19200")) {
                sensor.setBaudRate(4);
            } else if (Converter.toBlank(sensor.getBaudRateStr()).equals("38400")) {
                sensor.setBaudRate(5);
            } else if (Converter.toBlank(sensor.getBaudRateStr()).equals("57600")) {
                sensor.setBaudRate(6);
            } else if (Converter.toBlank(sensor.getBaudRateStr()).equals("115200")) {
                sensor.setBaudRate(7);
            } else {
                sensor.setBaudRate(3);
            }
            // 补偿使能
            if (Converter.toBlank(sensor.getCompEnStr()).equals("使能")) {
                sensor.setCompEn(1);
            } else if (Converter.toBlank(sensor.getCompEnStr()).equals("禁用")) {
                sensor.setCompEn(2);
            } else {
                sensor.setCompEn(1);
            }
            // 滤波系数
            if (Converter.toBlank(sensor.getFilterFactorStr()).equals("实时")) {
                sensor.setFilterFactor(1);
            } else if (Converter.toBlank(sensor.getFilterFactorStr()).equals("平滑")) {
                sensor.setFilterFactor(2);
            } else if (Converter.toBlank(sensor.getFilterFactorStr()).equals("平稳")) {
                sensor.setFilterFactor(3);
            } else {
                sensor.setFilterFactor(2);
            }
            sensor.setCreateDataTime(new Date());
            sensor.setCreateDataUsername(SystemHelper.getCurrentUsername());
            importList.add(sensor);
            message.append("导入轮速传感器 : ").append(sensor.getSensorType()).append(" <br/>");
        }

        // 组装导入结果
        if (importList.size() == 0) {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", errorMsg);
            resultMap.put("resultInfo", "成功导入0条数据。");
            return resultMap;
        }

        // 导入逻辑（暂时只有新增，具体导入逻辑还需需求确定）
        boolean flag = mileageSensorDao.addBatchMileageSensors(importList);
        if (flag) {
            resultInfo += "导入成功" + importList.size() + "条数据,导入失败" + (list.size() - importList.size()) + "条数据。";
            resultMap.put("flag", 1);
            resultMap.put("errorMsg", errorMsg);
            resultMap.put("resultInfo", resultInfo);
            logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "导入轮速传感器");
        } else {
            resultMap.put("flag", 0);
            resultMap.put("resultInfo", "导入失败！");
            return resultMap;
        }
        return resultMap;
    }

    /**
     * 导出
     */
    @Override
    public boolean export(String title, int type, HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(title, MileageSensor.class, 1, null);
        Page<MileageSensor> list = mileageSensorDao.findByQuery(null);
        List<MileageSensor> exportList = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (MileageSensor sensor : list) {
                MileageSensor form = new MileageSensor();
                BeanUtils.copyProperties(sensor, form); // bean 转 form
                // 奇偶校验
                form.setParityCheckStr(ParityCheckUtil.getParityCheckVal(form.getParityCheck()));
                // 波特率
                form.setBaudRateStr(BaudRateUtil.getBaudRateVal(form.getBaudRate()));
                // 补偿使能
                form.setCompEnStr(CompEnUtil.getCompEnVal(form.getCompEn()));
                // 滤波系数
                form.setFilterFactorStr(FilterFactorUtil.getFilterFactorVal(form.getFilterFactor()));
                exportList.add(form);
            }
        }
        export.setDataList(exportList);
        // 输出导文件
        OutputStream out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
        return true;
    }

    /**
     * 生成模板
     */
    @MethodLog(name = "生成模板", description = "生成模板")
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // 表头
        headList.add("轮速传感器型号");
        headList.add("奇偶校验");
        headList.add("波特率");
        headList.add("补偿使能");
        headList.add("滤波系数");
        // 必填字段
        requiredList.add("轮速传感器型号");
        // requiredList.add("奇偶校验");
        // requiredList.add("波特率");
        // requiredList.add("补偿使能");
        // requiredList.add("滤波系数");
        // 默认设置一条数据
        exportList.add("AOE-56826");
        exportList.add("无校验");
        exportList.add("9600");
        exportList.add("使能");
        exportList.add("平滑");

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<>();
        // 奇偶校验
        String[] parity = { "奇校验", "偶校验", "无校验" };
        selectMap.put("奇偶校验", parity);
        // 波特率
        String[] baudRate = { "2400", "4800", "9600", "19200", "38400", "57600", "115200" };
        selectMap.put("波特率", baudRate);
        // 补偿使能
        String[] inertiaCompEn = { "使能", "禁用" };
        selectMap.put("补偿使能", inertiaCompEn);
        // 滤波系数
        String[] filterFactor = { "实时", "平滑", "平稳" };
        selectMap.put("滤波系数", filterFactor);

        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // 输出导文件
        OutputStream out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
        return true;
    }
}
