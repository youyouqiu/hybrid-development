package com.zw.platform.service.workhourmgt.impl;


import com.github.pagehelper.PageHelper;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.vas.workhourmgt.form.VibrationSensorForm;
import com.zw.platform.domain.vas.workhourmgt.query.VibrationSensorQuery;
import com.zw.platform.repository.vas.VibrationSensorBindDao;
import com.zw.platform.repository.vas.VibrationSensorDao;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.workhourmgt.VibrationSensorService;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RegexUtils;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportExcel;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
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


@Service
public class VibrationSensorServiceImpl implements VibrationSensorService {
    @Autowired
    private VibrationSensorDao vibrationSensorDao;

    @Autowired
    private VibrationSensorBindDao vibrationSensorBindDao;

    @Autowired
    private LogSearchService logSearchService;

    private static final String DELETE_ERROR_MSSAGE = "部分传感器已经和车辆绑定了，到【工时车辆设置】中解除绑定后才可以删除哟！";

    @Override
    public List<VibrationSensorForm> findVibrationSensorByPage(VibrationSensorQuery query, boolean doPage) {
        if (query != null) {
            String simpleQueryParam = query.getSimpleQueryParam();
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(simpleQueryParam));
        }
        return query != null && doPage
                ? PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                .doSelectPage(() -> vibrationSensorDao.findVibrationSensor(query))
                : vibrationSensorDao.findVibrationSensor(query);
    }

    @Override
    public JsonResultBean addVibrationSensor(VibrationSensorForm form, String ipAddress) {
        form.setCreateDataTime(new Date());
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = vibrationSensorDao.addVibrationSensor(form);
        if (flag) {
            String logs = "新增振动传感器：" + form.getSensorType();
            logSearchService.addLog(ipAddress, logs, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean deleteVibrationSensor(String id, String ipAddress) throws Exception {
        String[] ids = id.split(",");
        List<String> li = Arrays.asList(ids);
        StringBuilder message = new StringBuilder();
        StringBuilder msg = new StringBuilder();
        StringBuilder layermsg = new StringBuilder();
        for (String sensorId : li) {
            List<VibrationSensorForm> list = findById(sensorId);
            if (list.size() != 0) {
                msg.append(list.get(0).getSensorType() + "<br/>");
                continue;
            }
            VibrationSensorForm vibrationSensorForm = findVibrationSensorById(sensorId); // 被删除的振动传感器型号
            if (vibrationSensorForm != null) {
                boolean flag = vibrationSensorDao.deleteVibrationSensor(sensorId);
                if (flag) {
                    if (!vibrationSensorForm.getSensorType().isEmpty()) {
                        message.append("删除振动传感器 : ").append(vibrationSensorForm.getSensorType()).append(" <br/>");
                    }
                }
            }
        }
        if (msg.length() > 0) {
            layermsg.append(DELETE_ERROR_MSSAGE).append("<br/> 已绑定传感器型号如下: <br/>" + msg);
            return new JsonResultBean(JsonResultBean.FAULT, layermsg.toString());
        }
        if (!message.toString().isEmpty()) {
            if (li.size() == 1) {
                logSearchService.addLog(ipAddress, message.toString(), "3", "", "-", "");
            } else {
                logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "批量删除振动传感器");
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public boolean updateVibrationSensor(VibrationSensorForm form, String ipAddress) throws Exception {
        // 修改前振动传感器的编号（名字）
        String beforeName = findVibrationSensorById(form.getId()).getSensorType();
        boolean result = vibrationSensorDao.updateVibrationSensor(form);
        if (result) {
            String id = form.getId();
            // 查询出传感器是否绑定了车，从而获取到车的id
            List<String> list = vibrationSensorBindDao.findWorkHourVehicleBySensorId(id);
            if (list != null && list.size() > 0) {
                for (String vehicleId : list) {
                    // 维护车和传感器缓存
                    RedisHelper.addToHash(RedisKeyEnum.VEHICLE_SHOCK_MONITOR_LIST.of(),
                            vehicleId, form.getSensorType());
                }
            }
            // 平台日志记录
            if (!beforeName.equals(form.getSensorType())) {
                // 修改前后名字不相同
                logSearchService.addLog(ipAddress, "修改振动传感器：" + beforeName + " 修改为  " + form.getSensorType(), "3", "",
                    "-", "");
            } else {
                logSearchService.addLog(ipAddress, "修改振动传感器：" + form.getSensorType(), "3", "", "-", "");
            }
            return true;
        }
        return false;
    }

    @Override
    public VibrationSensorForm findVibrationSensorById(String id) throws Exception {
        return vibrationSensorDao.findVibrationSensorById(id);
    }

    @Override
    public int findByNumber(String sensorNumber) throws Exception {
        return vibrationSensorDao.findByNumber(sensorNumber);
    }

    @Override
    public boolean export(String title, int type, HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(title, VibrationSensorForm.class, type, null);
        List<VibrationSensorForm> exportList = vibrationSensorDao.findVibrationSensor(null);
        if (null != exportList && exportList.size() > 0) {
            for (VibrationSensorForm form : exportList) {
                form.setParity(getParityStr(form.getParity()));
                form.setBaudRate(getBaudRateStr(form.getBaudRate()));
                form.setInertiaCompEnStr(getInertiaCompEnStr(Converter.toBlank(form.getInertiaCompEn())));
                form.setFilterFactorStr(getFilterFactorStr(Converter.toBlank(form.getFilterFactor())));
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
     * 获取奇偶校验对应的值
     * @Title: getParityStr
     * @param parity
     * @return
     * @return String
     * @throws @author
     *             Liubangquan
     */
    private String getParityStr(String parity) {
        String str = "";
        if (Converter.toBlank(parity).equals("1")) {
            str = "奇校验";
        } else if (Converter.toBlank(parity).equals("2")) {
            str = "偶校验";
        } else {
            str = "无校验";
        }
        return str;
    }

    /**
     * 获取奇偶校验对应的key
     * @Title: getParityStr
     * @param parity
     * @return
     * @return String
     * @throws @author
     *             Liubangquan
     */
    private String getParity(String parity) {
        String str = "";
        if (Converter.toBlank(parity).equals("奇校验")) {
            str = "1";
        } else if (Converter.toBlank(parity).equals("偶校验")) {
            str = "2";
        } else {
            str = "3";
        }
        return str;
    }

    /**
     * 获取波特率对应的值
     * @Title: getBaudRateStr
     * @param baudRate
     * @return
     * @return String
     * @throws @author
     *             Liubangquan
     */
    private String getBaudRateStr(String baudRate) {
        String str = "";
        if (Converter.toBlank(baudRate).equals("1")) {
            str = "2400";
        } else if (Converter.toBlank(baudRate).equals("2")) {
            str = "4800";
        } else if (Converter.toBlank(baudRate).equals("4")) {
            str = "19200";
        } else if (Converter.toBlank(baudRate).equals("5")) {
            str = "38400";
        } else if (Converter.toBlank(baudRate).equals("6")) {
            str = "57600";
        } else if (Converter.toBlank(baudRate).equals("7")) {
            str = "115200";
        } else {
            str = "9600";
        }
        return str;
    }

    /**
     * 获取波特率对应的key
     * @Title: getBaudRateStr
     * @param baudRate
     * @return
     * @return String
     * @throws @author
     *             Liubangquan
     */
    private String getBaudRate(String baudRate) {
        String str = "";
        if (Converter.toBlank(baudRate).equals("2400")) {
            str = "1";
        } else if (Converter.toBlank(baudRate).equals("4800")) {
            str = "2";
        } else if (Converter.toBlank(baudRate).equals("19200")) {
            str = "4";
        } else if (Converter.toBlank(baudRate).equals("38400")) {
            str = "5";
        } else if (Converter.toBlank(baudRate).equals("57600")) {
            str = "6";
        } else if (Converter.toBlank(baudRate).equals("115200")) {
            str = "7";
        } else {
            str = "3";
        }
        return str;
    }

    /**
     * 获取补偿使能对应的值
     * @Title: getInertiaCompEnStr
     * @param inertiaCompEn
     * @return
     * @return String
     * @throws @author
     *             Liubangquan
     */
    private String getInertiaCompEnStr(String inertiaCompEn) {
        String str = "";
        if (Converter.toBlank(inertiaCompEn).equals("2")) {
            str = "禁用";
        } else {
            str = "使能";
        }
        return str;
    }

    /**
     * 获取补偿使能对应的key
     * @Title: getInertiaCompEnStr
     * @param inertiaCompEn
     * @return
     * @return String
     * @throws @author
     *             Liubangquan
     */
    private String getInertiaCompEn(String inertiaCompEn) {
        String str = "";
        if (Converter.toBlank(inertiaCompEn).equals("禁用")) {
            str = "2";
        } else {
            str = "1";
        }
        return str;
    }

    /**
     * 获取滤波系数对应的值
     * @Title: getFilterFactorStr
     * @param filterFactor
     * @return
     * @return String
     * @throws @author
     *             Liubangquan
     */
    private String getFilterFactorStr(String filterFactor) {
        String str = "";
        if (Converter.toBlank(filterFactor).equals("1")) {
            str = "实时";
        } else if (Converter.toBlank(filterFactor).equals("2")) {
            str = "平滑";
        } else {
            str = "平稳";
        }
        return str;
    }

    /**
     * 获取滤波系数对应的key
     * @Title: getFilterFactorStr
     * @param filterFactor
     * @return
     * @return String
     * @throws @author
     *             Liubangquan
     */
    private String getFilterFactor(String filterFactor) {
        String str = "";
        if (Converter.toBlank(filterFactor).equals("实时")) {
            str = "1";
        } else if (Converter.toBlank(filterFactor).equals("平滑")) {
            str = "2";
        } else {
            str = "3";
        }
        return str;
    }

    @Override
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<String>();
        List<String> requiredList = new ArrayList<String>();
        List<Object> exportList = new ArrayList<Object>();
        // 表头
        headList.add("传感器型号");
        headList.add("传感器厂商");
        headList.add("奇偶校验");
        headList.add("波特率");
        headList.add("补偿使能");
        headList.add("滤波系数");
        headList.add("备注");
        // 必填字段
        requiredList.add("传感器型号");
        requiredList.add("奇偶校验");
        requiredList.add("波特率");
        requiredList.add("补偿使能");
        requiredList.add("滤波系数");
        // 默认设置一条数据
        exportList.add("example");
        exportList.add("北京中位科技有限公司");
        exportList.add("奇校验");
        exportList.add("9600");
        exportList.add("使能");
        exportList.add("平稳");
        exportList.add("备注信息");

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<String, String[]>();

        // 奇偶校验
        String[] parity = {"奇校验", "偶校验", "无校验"};
        selectMap.put("奇偶校验", parity);
        // 波特率
        String[] baudRate = {"2400", "4800", "9600", "19200", "38400", "57600", "115200"};
        selectMap.put("波特率", baudRate);
        // 补偿使能
        String[] inertiaCompEn = {"使能", "禁用"};
        selectMap.put("补偿使能", inertiaCompEn);
        // 滤波系数
        String[] filterFactor = {"平稳", "实时", "平滑"};
        selectMap.put("滤波系数", filterFactor);

        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
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
    public Map importData(MultipartFile multipartFile, String ipAddress) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("flag", 0);
        StringBuilder errorMsg = new StringBuilder();
        String resultInfo = "";
        StringBuilder message = new StringBuilder(); // 日志记录语句
        int failNum = 0;
        int totalNum = 0;
        // 导入的文件
        ImportExcel importExcel = new ImportExcel(multipartFile, 1, 0);
        // excel 转换成 list
        List<VibrationSensorForm> list = importExcel.getDataList(VibrationSensorForm.class, null);
        String temp;
        List<VibrationSensorForm> importList = new ArrayList<>();
        StringBuilder errorMsgBuilder = new StringBuilder();
        // 校验需要导入的传感器
        if (list != null && list.size() > 0) {
            totalNum = list.size();
            for (int i = 0; i < list.size(); i++) {
                VibrationSensorForm form = list.get(i);
                // 校验必填字段
                if (Converter.toBlank(form.getSensorType()).equals("")
                    || Converter.toBlank(form.getParity()).equals("")) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i + 1).append("条数据必填字段未填</br>");
                    failNum++;
                    continue;
                } else {
                    // 奇偶校验
                    form.setParity(getParity(Converter.toBlank(form.getParity())));
                    // 波特率
                    form.setBaudRate(getBaudRate(Converter.toBlank(form.getBaudRate())));
                    // 补偿使能
                    form.setInertiaCompEn(
                        Converter.toInteger(getInertiaCompEn(Converter.toBlank(form.getInertiaCompEnStr()))));
                    // 滤波系数
                    form.setFilterFactor(
                        Converter.toInteger(getFilterFactor(Converter.toBlank(form.getFilterFactorStr()))));
                }
                if (!RegexUtils.checkRightfulString1(Converter.toBlank(form.getSensorType()))) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i + 1).append("行传感器型号中包含特殊字符，值是：").append(form.getSensorType()).append(
                        "<br/>");
                    failNum++;
                    continue;
                }
                // 传感器型号长度限制
                if (form.getSensorType().length() > 20) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i + 1).append("行传感器型号长度超过20个字符！<br/>");
                    failNum++;
                    continue;
                }
                // 列表中重复数据
                boolean f = false;
                for (int j = i + 1; j < list.size(); j++) {
                    if (list.get(j).getSensorType().equals(form.getSensorType())) {
                        temp = form.getSensorType();
                        errorMsg.append("第").append(i + 2).append("行跟第").append(j + 2).append("行重复,值是 :").append(
                            temp).append("<br/>");
                        failNum++;
                        f = true;
                        break;
                        // list.remove(j);
                    }
                }
                if (f) {
                    continue;
                }
                // 与数据库是否有重复数据
                List<VibrationSensorForm> tankList = vibrationSensorDao.findVibrationSensorByType(
                    Converter.toBlank(form.getSensorType()));
                if (tankList != null && tankList.size() > 0) {
                    resultMap.put("flag", 0);
                    errorMsg.append("传感器型号为“").append(form.getSensorType()).append("”已存在<br/>");
                    failNum++;
                    continue;
                }

                form.setCreateDataTime(new Date());
                form.setCreateDataUsername(SystemHelper.getCurrentUsername());
                importList.add(form);
                message.append("导入振动传感器 : ").append(form.getSensorType()).append("<br/>");
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
            boolean flag = vibrationSensorDao.addByBatch(importList);
            if (flag) {
                resultInfo += "成功" + (totalNum - failNum) + "条,失败" + failNum + "条。";
                resultMap.put("flag", 1);
                resultMap.put("errorMsg", errorMsg);
                resultMap.put("resultInfo", resultInfo);
                logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "导入振动传感器");
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

    @Override
    public int findByNumber(String id, String sensorNumber) throws Exception {
        return vibrationSensorDao.isExist(id, sensorNumber);
    }

    @Override
    public List<VibrationSensorForm> findById(String id) throws Exception {
        return vibrationSensorDao.findById(id);
    }

}
