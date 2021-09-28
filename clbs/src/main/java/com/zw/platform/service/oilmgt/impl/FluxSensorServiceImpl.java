package com.zw.platform.service.oilmgt.impl;

import com.github.pagehelper.PageHelper;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.event.ConfigUnBindEvent;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.vas.oilmgt.FluxSensor;
import com.zw.platform.domain.vas.oilmgt.form.FluxSensorForm;
import com.zw.platform.domain.vas.oilmgt.query.FluxSensorQuery;
import com.zw.platform.event.ConfigUnbindVehicleEvent;
import com.zw.platform.repository.vas.FluxSensorBindDao;
import com.zw.platform.repository.vas.FluxSensorDao;
import com.zw.platform.service.oilmgt.FluxSensorService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportExcel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>Title: 流量传感器ServiceImp</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p> <p>team:
 * ZhongWeiTeam</p>
 * @version 1.0
 * @author: wangying
 * @date 2016年9月19日上午9:31:52
 */
@Service
public class FluxSensorServiceImpl implements FluxSensorService {

    private static Logger log = LogManager.getLogger(FluxSensorServiceImpl.class);

    @Value("${fluxsensor.type.use}")
    private String fluxsensorTypeUse;

    @Autowired
    private FluxSensorDao fluxSensorDao;

    @Autowired
    private FluxSensorBindDao fluxSensorBindDao;

    @Autowired
    private LogSearchService logSearchService;

    private static final String DELETE_ERROR_MSSAGE = "部分传感器已经和车辆绑定了，到【油耗车辆设置】中解除绑定后才可以删除哟！";

    @Override
    public List<FluxSensor> findFluxSensorByPage(FluxSensorQuery query, boolean doPage) {
        if (query != null) {
            String simpleQueryParam = query.getSimpleQueryParam();
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(simpleQueryParam));
        }
        return doPage
                ? PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                .doSelectPage(() -> fluxSensorDao.findSensor(query))
                : fluxSensorDao.findSensor(query);
    }

    @Override
    public JsonResultBean addFluxSensor(FluxSensorForm form, String ipAddress) throws Exception {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        // 创建者
        form.setCreateDataTime(new Date()); // 创建时间
        boolean flag = fluxSensorDao.addFluxSensor(form);
        if (flag) {
            String msg = "新增流量传感器：" + form.getOilWearNumber();
            logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @Override
    public FluxSensor findById(String id) throws Exception {
        if (id != null && !"".equals(id)) {
            return fluxSensorDao.findById(id);
        }
        return null;
    }

    @Override
    public FluxSensor findByNumber(String number) throws Exception {
        if (number != null && !"".equals(number)) {
            return fluxSensorDao.findByNumber(number);
        }
        return null;
    }

    @Override
    public JsonResultBean updateFluxSensor(FluxSensorForm form, String ipAddress) throws Exception {
        FluxSensor before = findById(form.getId());// 查询修改前流量传感器实体
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername()); // 修改者
        form.setUpdateDataTime(new Date()); // 修改时间
        boolean result = fluxSensorDao.updateFluxSensor(form);
        // 查询传感器绑定的情况 维护缓存
        List<String> list = fluxSensorBindDao.findBySensorId(form.getId());
        if (list != null && list.size() > 0) {
            for (String str : list) {
                RedisHelper.addToHash(RedisKeyEnum.VEHICLE_OIL_CONSUME_MONITOR_LIST.of(), str, form.getOilWearNumber());
            }
        }
        if (result) {
            String msg = "";// 日志语句
            String beforeSF = before.getOilWearNumber();// 修改前流量传感器型号
            String afterSF = form.getOilWearNumber();// 修改后流量传感器型号
            if (beforeSF.equals(afterSF)) {
                msg = "修改流量传感器：" + afterSF;
            } else {
                msg = "修改流量传感器：" + beforeSF + " 修改为 " + afterSF;
            }
            logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean deleteFluxSensor(String ids, String ipAddress) throws Exception {
        StringBuilder deleteMsg = new StringBuilder();
        StringBuilder message = new StringBuilder();
        String[] item = ids.split(",");
        for (int i = 0, n = item.length; i < n; i++) {
            List<FluxSensor> list = findOilWearByVid(item[i]); // 判断流量传感器是否正在使用
            FluxSensor flux = findById(item[i]); // 查询流量传感器信息
            if (list.size() == 0) {
                boolean flag = fluxSensorDao.deleteFluxSensor(item[i]);
                if (flag) {
                    message.append("删除流量传感器 ： ").append(flux.getOilWearNumber()).append(" <br/>");
                }
            } else {
                deleteMsg.append(flux.getOilWearNumber()).append("</br>");
            }
        }
        if (!"".equals(message.toString())) {
            if (item.length == 1) {
                logSearchService.addLog(ipAddress, message.toString(), "3", "", "-", ""); // 记录日志
            } else {
                logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "批量删除流量传感器");
            }
        }
        if (StringUtils.isEmpty(deleteMsg.toString())) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT,
                DELETE_ERROR_MSSAGE + "</br>" + "已绑定传感器型号如下：</br>" + deleteMsg);
        }
    }

    @Override
    public boolean deleteFluxSensorBind(String sensorId) throws Exception {
        if (sensorId != null && !"".equals(sensorId)) {
            return fluxSensorDao.deleteFluxSensorBind(sensorId);
        }
        return false;
    }

    @Override
    public boolean deleteFluxSensorBindByVid(String vehicleId, Integer type) {
        if (vehicleId != null && !"".equals(vehicleId)) {
            if (ConfigUnbindVehicleEvent.TYPE_SINGLE == type) {
                return fluxSensorDao.deleteFluxSensorBindByVehicleId(vehicleId);
            } else {
                List<String> monitorIds = Arrays.asList(vehicleId.split(","));
                return fluxSensorDao.deleteBatchFluxSensorBindByVehicleId(monitorIds);
            }
        }
        return false;
    }

    @EventListener
    public void updateVehicleUnbound(ConfigUnBindEvent event) {
        List<String> monitorIds = event.getUnbindList().stream().map(BindDTO::getId).collect(Collectors.toList());
        fluxSensorDao.deleteBatchFluxSensorBindByVehicleId(monitorIds);
    }

    @Override
    public Map importSensor(MultipartFile multipartFile, String ipAddress) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("flag", 0);
        StringBuilder errorMsg = new StringBuilder();
        String resultInfo = "";
        // 导入的文件
        ImportExcel importExcel = new ImportExcel(multipartFile, 1, 0);
        // excel 转换成 list
        List<FluxSensorForm> list = importExcel.getDataList(FluxSensorForm.class, null);
        String temp;
        List<FluxSensorForm> importList = new ArrayList<FluxSensorForm>();
        StringBuilder errorMsgBuilder = new StringBuilder();
        StringBuilder message = new StringBuilder();
        // 校验需要导入的油箱
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                FluxSensorForm sensor = list.get(i);
                if ("REPEAT".equals(sensor.getOilWearNumber())) {
                    continue;
                }
                // 列表中重复数据
                for (int j = i + 1; j < list.size(); j++) {
                    if (!StringUtils.isBlank(list.get(j).getOilWearNumber()) && list.get(j).getOilWearNumber()
                        .equals(sensor.getOilWearNumber())) {
                        temp = sensor.getOilWearNumber();
                        errorMsg.append("第").append(i + 1).append("行跟第").append(j + 1).append("行重复，值是：").append(temp)
                            .append("<br/>");
                        list.get(j).setOilWearNumber("REPEAT");
                    }
                }
            }
            for (int i = 0; i < list.size(); i++) {
                FluxSensorForm sensor = list.get(i);
                if ("REPEAT".equals(sensor.getOilWearNumber())) {
                    continue;
                }
                // 校验必填字段
                if (StringUtils.isBlank(sensor.getOilWearNumber())) {
                    sensor.setOilWearNumber("REPEAT");
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i + 1).append("条数据必填字段未填<br/>");
                    continue;
                }
                // 与数据库是否有重复数据
                FluxSensor fluxSensor = fluxSensorDao.findByNumber(sensor.getOilWearNumber());
                if (fluxSensor != null) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i + 1).append("条数据流量传感器型号为“").append(sensor.getOilWearNumber())
                        .append("”已存在<br/>");
                    continue;
                }
                // 校验字符
                String reg = "^[A-Za-z0-9_#\\*\\u4e00-\\u9fa5\\-]{1,25}$";
                if (!sensor.getOilWearNumber().matches(reg)) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i + 1).append("条数据【流量传感器型号】异常，仅中文、字母、数字或特殊符号*、-、_、#，长度不超过25位!<br/>");
                    continue;
                }
                // 奇偶校验
                if (Converter.toBlank(sensor.getParity()).equals("奇校验")) {
                    sensor.setParity("1");
                } else if (Converter.toBlank(sensor.getParity()).equals("偶校验")) {
                    sensor.setParity("2");
                } else if (Converter.toBlank(sensor.getParity()).equals("无校验")) {
                    sensor.setParity("3");
                } else {
                    sensor.setParity("3");
                }
                // 波特率
                if (Converter.toBlank(sensor.getBaudRate()).equals("2400")) {
                    sensor.setBaudRate("1");
                } else if (Converter.toBlank(sensor.getBaudRate()).equals("4800")) {
                    sensor.setBaudRate("2");
                } else if (Converter.toBlank(sensor.getBaudRate()).equals("9600")) {
                    sensor.setBaudRate("3");
                } else if (Converter.toBlank(sensor.getBaudRate()).equals("19200")) {
                    sensor.setBaudRate("4");
                } else if (Converter.toBlank(sensor.getBaudRate()).equals("38400")) {
                    sensor.setBaudRate("5");
                } else if (Converter.toBlank(sensor.getBaudRate()).equals("57600")) {
                    sensor.setBaudRate("6");
                } else if (Converter.toBlank(sensor.getBaudRate()).equals("115200")) {
                    sensor.setBaudRate("7");
                } else {
                    sensor.setBaudRate("3");
                }
                // 补偿使能
                if (Converter.toBlank(sensor.getInertiaCompEnStr()).equals("使能")) {
                    sensor.setInertiaCompEn(1);
                } else if (Converter.toBlank(sensor.getInertiaCompEnStr()).equals("禁用")) {
                    sensor.setInertiaCompEn(2);
                } else {
                    sensor.setInertiaCompEn(1);
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
                message.append("导入流量传感器 : ").append(sensor.getOilWearNumber()).append(" <br/>");
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
            boolean flag = fluxSensorDao.addFluxSensorByBatch(importList);
            if (flag) {
                resultInfo += "导入成功" + importList.size() + "条数据,导入失败" + (list.size() - importList.size()) + "条数据。";
                resultMap.put("flag", 1);
                resultMap.put("errorMsg", errorMsg);
                resultMap.put("resultInfo", resultInfo);
                logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "导入流量传感器数据");
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
     * 导出
     */
    @Override
    public boolean export(String title, int type, HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(title, FluxSensorForm.class, 1, null);
        List<FluxSensor> list = fluxSensorDao.findSensor(null);
        List<FluxSensorForm> exportList = new ArrayList<FluxSensorForm>();
        if (list != null && list.size() > 0) {
            for (FluxSensor sensor : list) {
                FluxSensorForm form = new FluxSensorForm();
                BeanUtils.copyProperties(sensor, form); // bean 转 form
                // 奇偶校验
                if (Converter.toBlank(form.getParity()).equals("1")) {
                    form.setParity("奇校验");
                } else if (Converter.toBlank(form.getParity()).equals("2")) {
                    form.setParity("偶校验");
                } else if (Converter.toBlank(form.getParity()).equals("3")) {
                    form.setParity("无校验");
                }
                // 波特率
                if (Converter.toBlank(form.getBaudRate()).equals("1")) {
                    form.setBaudRate("2400");
                } else if (Converter.toBlank(form.getBaudRate()).equals("2")) {
                    form.setBaudRate("4800");
                } else if (Converter.toBlank(form.getBaudRate()).equals("3")) {
                    form.setBaudRate("9600");
                } else if (Converter.toBlank(form.getBaudRate()).equals("4")) {
                    form.setBaudRate("19200");
                } else if (Converter.toBlank(form.getBaudRate()).equals("5")) {
                    form.setBaudRate("38400");
                } else if (Converter.toBlank(form.getBaudRate()).equals("6")) {
                    form.setBaudRate("57600");
                } else if (Converter.toBlank(form.getBaudRate()).equals("7")) {
                    form.setBaudRate("115200");
                }
                // 补偿使能
                if (Converter.toBlank(form.getInertiaCompEn()).equals("1")) {
                    form.setInertiaCompEnStr("使能");
                } else if (Converter.toBlank(form.getInertiaCompEn()).equals("2")) {
                    form.setInertiaCompEnStr("禁用");
                }
                // 滤波系数
                if (Converter.toBlank(form.getFilterFactor()).equals("1")) {
                    form.setFilterFactorStr("实时");
                } else if (Converter.toBlank(form.getFilterFactor()).equals("2")) {
                    form.setFilterFactorStr("平滑");
                } else if (Converter.toBlank(form.getFilterFactor()).equals("3")) {
                    form.setFilterFactorStr("平稳");
                }
                exportList.add(form);
            }
        }
        export.setDataList(exportList);
        // 输出导文件
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
        return true;
    }

    /**
     * 生成模板
     */
    @MethodLog(name = "生成模板", description = "生成模板")
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<String>();
        List<String> requiredList = new ArrayList<String>();
        List<Object> exportList = new ArrayList<Object>();
        // 表头
        headList.add("流量传感器型号");
        headList.add("奇偶校验");
        headList.add("波特率");
        headList.add("补偿使能");
        headList.add("滤波系数");
        headList.add("备注");
        // 必填字段
        requiredList.add("流量传感器型号");
        // 默认设置一条数据
        exportList.add("AOE-56826");
        exportList.add("无校验");
        exportList.add("9600");
        exportList.add("使能");
        exportList.add("平滑");
        exportList.add("备注");

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<String, String[]>();
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
    public FluxSensor findByNumber(String id, String number) throws Exception {
        return fluxSensorDao.isExist(id, number);
    }

    @Override
    public List<FluxSensor> findOilWearByVid(String id) {
        return fluxSensorDao.findOilWearByVid(id);
    }

}
