package com.zw.platform.service.basicinfo.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.RodSensor;
import com.zw.platform.domain.basicinfo.form.RodSensorForm;
import com.zw.platform.domain.basicinfo.form.RodSensorImportForm;
import com.zw.platform.domain.basicinfo.query.RodSensorQuery;
import com.zw.platform.repository.modules.RodSensorDao;
import com.zw.platform.service.basicinfo.RodSensorService;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ExportOilBoxExcel;
import com.zw.platform.util.excel.ImportExcel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Tdz on 2016/7/25.
 */
@Service
public class RodSensorServiceImpl implements RodSensorService {
    private static Logger log = LogManager.getLogger(RodSensorServiceImpl.class);

    private static final Integer SENSOR_NUMBER_MAXLENGTH = 20; // 传感器型号最大长度

    private static final Integer SENSOR_LENGTH_MAXLENGTH = 5; // 传感器长度最大允许长度

    private static final String TEMPLATE_COMMENT =
        "注：红色标注为必填；滤波系数如不填默认“平滑”，波特率默认“9600”，奇偶校验默认“偶校验”，" + "补偿使能默认“使能”(另：整理前请删除示例数据，谢谢)";

    private static final String DELETE_ERROR_MSSAGE = "部分传感器已经和车辆绑定了，到【油量车辆设置】中解除绑定后才可以删除哟！";

    @Value("${add.success}")
    private String addSuccess;

    @Autowired
    private RodSensorDao rodSensorDao;

    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;

    @Override
    public RodSensor get(String id) throws Exception {
        if (StringUtils.isNotBlank(id)) {
            return rodSensorDao.get(id);
        }
        return null;
    }

    @Override
    public Page<RodSensor> findByPage(RodSensorQuery query) throws Exception {
        if (query != null) {
            String simpleQueryParam = query.getSimpleQueryParam();
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(simpleQueryParam));
        }
        return PageHelperUtil.doSelect(query, () -> rodSensorDao.find(query));
    }

    @Override
    public JsonResultBean add(RodSensorForm form, String ipAddress) throws Exception {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        form.setCreateDataTime(new Date());
        boolean flag = rodSensorDao.add(form);
        if (flag) {
            String logs = "新增油位传感器：" + form.getSensorNumber();
            logSearchServiceImpl.addLog(ipAddress, logs, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS, addSuccess);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @Override
    public JsonResultBean delete(String id, String ipAddress) throws Exception {
        String[] items = id.split(",");
        StringBuilder message = new StringBuilder();
        JSONObject msg = new JSONObject();
        StringBuilder deleteFailMsg = new StringBuilder();
        boolean noBind = dealAndAddMessage(items, message, deleteFailMsg);
        recorderDeleteLog(ipAddress, items, message);
        if (noBind) {
            return new JsonResultBean(msg);
        } else {
            String errorMsg = DELETE_ERROR_MSSAGE + "</br>" + "已绑定传感器型号如下：</br>" + deleteFailMsg;
            return new JsonResultBean(JsonResultBean.FAULT, errorMsg);

        }
    }

    private void recorderDeleteLog(String ipAddress, String[] items, StringBuilder message) throws Exception {
        if (!message.toString().isEmpty()) {
            if (items.length == 1) {
                logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "", "-", "");
            } else {
                logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "batch", "批量删除油位传感器");
            }

        }
    }

    private boolean dealAndAddMessage(String[] items, StringBuilder message, StringBuilder deleteFailMsg)
        throws Exception {
        boolean noBind = true;
        for (String sensorId : items) {
            int bindRodSensorNum = getIsBand(sensorId);
            if (bindRodSensorNum > 0) {
                noBind = false;
                RodSensor rs = findById(sensorId);
                deleteFailMsg.append(null != rs ? rs.getSensorNumber() : "").append("</br>");
                continue;
            } else {
                // 根据id查询传感器信息
                RodSensor rodSensor = rodSensorDao.findById(sensorId);
                boolean flag = rodSensorDao.delete(sensorId);
                if (flag) { // 删除成功
                    message.append("删除油位传感器 ： ").append(rodSensor.getSensorNumber()).append(" <br/>");
                }
            }
        }
        return noBind;
    }

    @Override
    public JsonResultBean update(RodSensorForm form, String ipAddress) throws Exception {
        String beforeName = findById(form.getId()).getSensorNumber();// 修改前传感器编号（名字）
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        form.setCreateDataTime(new Date());
        boolean flag = rodSensorDao.update(form);
        if (flag) {
            String message = "";
            if (!beforeName.equals(form.getSensorNumber())) { // 编号不相同
                message = "修改油位传感器：" + beforeName + " 修改为  " + form.getSensorNumber();

            } else {
                message = "修改油位传感器：" + form.getSensorNumber();
            }
            logSearchServiceImpl.addLog(ipAddress, message, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @Override
    public boolean exportInfo(String title, int type, HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(title, RodSensorForm.class, 1, null);
        List<RodSensorForm> exportList = new ArrayList<>();
        List<RodSensor> list = rodSensorDao.find(null);
        for (RodSensor info : list) {
            RodSensorForm form = new RodSensorForm();
            BeanUtils.copyProperties(info, form);
            dealRodSensorExportForm(form, null, "export"); // 处理form数据，将数字代表的信息转换成相对应的明文信息
            exportList.add(form);
        }
        export.setDataList(exportList);
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
        return true;
    }

    /**
     * 处理form数据-导出 导出：将数字代表的信息转换成相对应的明文信息 导入：将相应的明文信息转换成数据库的数字
     * @param form
     * @param importOrExport
     * @return void
     * @Title: dealRodSensorForm
     * @author Liubangquan
     */
    private void dealRodSensorExportForm(RodSensorForm form, HttpServletRequest request, String importOrExport)
        throws Exception {
        // 启停状态
        /*
         * if (Converter.toBlank(form.getIsStart()).equals("1")) { form.setIsStartStr("启用"); } else if
         * (Converter.toBlank(form.getIsStart()).equals("2")) { form.setIsStartStr("停用"); }
         */
        // 补偿使能
        if (Converter.toBlank(form.getCompensationCanMake()).equals("1")) {
            form.setCompensationCanMakeStr("使能");
        } else if (Converter.toBlank(form.getCompensationCanMake()).equals("2")) {
            form.setCompensationCanMakeStr("禁能");
        } else {
            form.setCompensationCanMakeStr("使能");
        }
        // 奇偶校验
        if (Converter.toBlank(form.getOddEvenCheck()).equals("3")) {
            form.setOddEvenCheckStr("无校验");
        } else if (Converter.toBlank(form.getOddEvenCheck()).equals("1")) {
            form.setOddEvenCheckStr("奇校验");
        } else if (Converter.toBlank(form.getOddEvenCheck()).equals("2")) {
            form.setOddEvenCheckStr("偶校验");
        } else {
            form.setOddEvenCheckStr("无校验");
        }
        // 滤波系数
        if (Converter.toBlank(form.getFilteringFactor()).equals("01")) {
            form.setFilteringFactorStr("实时");
        } else if (Converter.toBlank(form.getFilteringFactor()).equals("02")) {
            form.setFilteringFactorStr("平滑");
        } else {
            form.setFilteringFactorStr("平稳");
        }
        // 波特率
        if (Converter.toBlank(form.getBaudRate()).equals("01")) {
            form.setBaudRateStr("2400");
        } else if (Converter.toBlank(form.getBaudRate()).equals("02")) {
            form.setBaudRateStr("4800");
        } else if (Converter.toBlank(form.getBaudRate()).equals("03")) {
            form.setBaudRateStr("9600");
        } else if (Converter.toBlank(form.getBaudRate()).equals("04")) {
            form.setBaudRateStr("19200");
        } else if (Converter.toBlank(form.getBaudRate()).equals("05")) {
            form.setBaudRateStr("38400");
        } else if (Converter.toBlank(form.getBaudRate()).equals("06")) {
            form.setBaudRateStr("57600");
        } else if (Converter.toBlank(form.getBaudRate()).equals("07")) {
            form.setBaudRateStr("115200");
        } else {
            form.setBaudRateStr("9600");
        }
        // 出厂时间
        // form.setFactoryDateStr(Converter.toString(form.getFactoryDate(), "yyyy-MM-dd"));
    }

    /**
     * 处理form数据 导出：将数字代表的信息转换成相对应的明文信息 导入：将相应的明文信息转换成数据库的数字
     * @param form
     * @param importOrExport
     * @return void
     * @Title: dealRodSensorForm
     * @author Liubangquan
     */
    private void dealRodSensorForm(RodSensorImportForm form, HttpServletRequest request, String importOrExport)
        throws Exception {
        // 补偿使能
        if (Converter.toBlank(form.getCompensationCanMakeStr()).equals("使能")) {
            form.setCompensationCanMake((short) 1);
        } else if (Converter.toBlank(form.getCompensationCanMakeStr()).equals("禁能")) {
            form.setCompensationCanMake((short) 2);
        } else {
            form.setCompensationCanMake((short) 1);
        }
        // 奇偶校验
        if (Converter.toBlank(form.getOddEvenCheckStr()).equals("无校验")) {
            form.setOddEvenCheck((short) 3);
        } else if (Converter.toBlank(form.getOddEvenCheckStr()).equals("奇校验")) {
            form.setOddEvenCheck((short) 1);
        } else if (Converter.toBlank(form.getOddEvenCheckStr()).equals("偶校验")) {
            form.setOddEvenCheck((short) 2);
        } else {
            form.setOddEvenCheck((short) 3);
        }
        // 滤波系数
        if (Converter.toBlank(form.getFilteringFactorStr()).equals("实时")) {
            form.setFilteringFactor("01");
        } else if (Converter.toBlank(form.getFilteringFactorStr()).equals("平滑")) {
            form.setFilteringFactor("02");
        } else {
            form.setFilteringFactor("03");
        }
        // 波特率
        if (Converter.toBlank(form.getBaudRateStr()).equals("2400")) {
            form.setBaudRate("01");
        } else if (Converter.toBlank(form.getBaudRateStr()).equals("4800")) {
            form.setBaudRate("02");
        } else if (Converter.toBlank(form.getBaudRateStr()).equals("9600")) {
            form.setBaudRate("03");
        } else if (Converter.toBlank(form.getBaudRateStr()).equals("19200")) {
            form.setBaudRate("04");
        } else if (Converter.toBlank(form.getBaudRateStr()).equals("38400")) {
            form.setBaudRate("05");
        } else if (Converter.toBlank(form.getBaudRateStr()).equals("57600")) {
            form.setBaudRate("06");
        } else if (Converter.toBlank(form.getBaudRateStr()).equals("115200")) {
            form.setBaudRate("07");
        } else {
            form.setBaudRate("03");
        }

        // form.setFactoryDate(Converter.toDate(form.getFactoryDateStr(), "yyyy-MM-dd"));
        // 获取当前用户
        SecurityContextImpl securityContextImpl =
            (SecurityContextImpl) request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
        String createDataUsername = securityContextImpl.getAuthentication().getName();
        form.setCreateDataUsername(createDataUsername);
    }

    /**
     * 批量导入--匆删，以防后面需求变更后需要还原 liubq 2016-12-22
     */
    /*
     * @MethodLog(name = "批量导入", description = "批量导入") public Map importSensor(MultipartFile multipartFile,
     * HttpServletRequest request) { Map<String, Object> resultMap = new HashMap<String, Object>();
     * resultMap.put("flag", 0); String errorMsg = ""; String resultInfo = ""; int failNum = 0; int totalNum = 0; try {
     * // 导入的文件 ImportExcel importExcel = new ImportExcel(multipartFile, 1, 0); // excel 转换成 list List<RodSensorForm>
     * list = importExcel.getDataList(RodSensorForm.class, null); if (null != list && list.size() > 0) { totalNum =
     * list.size(); } String temp; int num=0; for(int i=0;i<list.size()-1;i++){ for(int j=list.size()-1;j>i;j--){
     * if(list.get(j).getSensorNumber().equals(list.get(i).getSensorNumber())){ temp = list.get(i).getSensorNumber();
     * errorMsg += "第" + (i + 1 + num) + "行跟第" + (j + 1 + num) + "行重复，值是："+temp+"<br/>"; num++; failNum ++;
     * list.remove(j); } } } List<RodSensorForm> importList = new ArrayList<RodSensorForm>(); int i = 0; //
     * 校验需要导入的RodSensor for (RodSensorForm rodSensorForm : list) { dealRodSensorForm(rodSensorForm, request, "import");
     * i++; // 校验必填字段 if (rodSensorForm.getSensorNumber() == null || rodSensorForm.getSensorNumber() == "" ||
     * rodSensorForm.getSensorLength() == null || rodSensorForm.getSensorLength() == "" || rodSensorForm.getIsStart() ==
     * null) { resultMap.put("flag", 1); errorMsg += "第" + i + "条数据必填字段未填<br/>"; failNum ++; continue; } else if
     * (!RegexUtils.checkRightfulString1(rodSensorForm.getSensorNumber())) { resultMap.put("flag", 1); errorMsg += "第" +
     * i + "条数据【"+ rodSensorForm.getSensorNumber() +"】:传感器型号包含特殊字符<br/>"; failNum ++; continue; } else if
     * (Converter.toBlank(rodSensorForm.getSensorNumber()).length() > SENSOR_NUMBER_MAXLENGTH) { resultMap.put("flag",
     * 1); errorMsg += "第" + i + "条数据【"+ rodSensorForm.getSensorNumber() +"】:传感器型号长度不超过" + SENSOR_NUMBER_MAXLENGTH +
     * "<br/>"; failNum ++; continue; } else if (!RegexUtils.checkRightNumber(rodSensorForm.getSensorLength())) {
     * resultMap.put("flag", 1); errorMsg += "第" + i + "条数据【"+ rodSensorForm.getSensorNumber() +"】:传感器长度不是正确的数字<br/>";
     * failNum ++; continue; } else if (Converter.toBlank(rodSensorForm.getSensorLength()).length() >
     * SENSOR_LENGTH_MAXLENGTH) { resultMap.put("flag", 1); errorMsg += "第" + i + "条数据【"+
     * rodSensorForm.getSensorNumber() +"】:传感器长度不超过" + SENSOR_LENGTH_MAXLENGTH + "位<br/>"; failNum ++; continue; } //
     * 编号重复 String a=rodSensorForm.getSensorNumber(); List<RodSensor> list1= rodSensorDao.findBySensorNumber(a); if
     * (list1.size()!= 0) { resultMap.put("flag", 0); errorMsg += "油感编号为“" + rodSensorForm.getSensorNumber() +
     * "”已存在<br/>"; failNum ++; continue; } importList.add(rodSensorForm); } // 组装导入结果 if (importList.size() > 0) {
     * boolean flag = false; // 导入逻辑（暂时只有新增，具体导入逻辑还需需求确定） try { flag = rodSensorDao.addByBatch(importList); }catch
     * (Exception e){ e.printStackTrace(); } if (flag) { resultInfo += "成功" + (totalNum - failNum) + "条,失败" + failNum +
     * "条。"; resultMap.put("flag", 1); resultMap.put("errorMsg", errorMsg); resultMap.put("resultInfo", resultInfo); }
     * else { resultMap.put("flag", 0); resultMap.put("resultInfo", "导入失败！"); return resultMap; } } else {
     * resultMap.put("flag", 0); resultMap.put("errorMsg", errorMsg); resultMap.put("resultInfo", "成功导入0条数据。"); return
     * resultMap; } } catch (InvalidFormatException e) { e.printStackTrace(); return resultMap; } catch (IOException e)
     * { e.printStackTrace(); return resultMap; } catch (InstantiationException e) { e.printStackTrace(); return
     * resultMap; } catch (IllegalAccessException e) { e.printStackTrace(); return resultMap; } return resultMap; }
     */
    @MethodLog(name = "批量导入", description = "批量导入")
    public Map importSensor(MultipartFile multipartFile, HttpServletRequest request, String ipAddress)
        throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("flag", 0);
        StringBuilder errorMsg = new StringBuilder();
        String resultInfo = "";
        StringBuilder message = new StringBuilder();
        int failNum = 0;
        int totalNum = 0;
        // 导入的文件
        ImportExcel importExcel = new ImportExcel(multipartFile, 2, 0);
        int cellNum = importExcel.getLastCellNum();
        if (cellNum != 7) {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", "请将导入文件按照模板格式整理后再导入");
            resultMap.put("resultInfo", "导入失败！");
            return resultMap;
        }
        // excel 转换成 list
        List<RodSensorImportForm> list = importExcel.getDataList(RodSensorImportForm.class, null);
        if (null != list && list.size() > 0) {
            totalNum = list.size();
        }
        String temp;
        for (int i = 0; i < list.size(); i++) {
            if ("REPEAT".equals(list.get(i).getSensorNumber())) {
                continue;
            }
            for (int j = list.size() - 1; j > i; j--) {
                if ((list.get(j).getSensorNumber()).equals(list.get(i).getSensorNumber())) {
                    // temp = list.get(i).getSensorNumber();
                    temp = list.get(j).getSensorNumber();
                    // errorMsg += "第" + (i + 1 + num) + "行跟第" + (j + 1 + num) + "行重复，值是："+temp+"<br/>";
                    // errorMsg += "第" + (i + 1 + num) + "行跟第" + (j + 1 + num) +
                    // "行重复，值是："+temp+""+"传感器型号组合规则是品牌+型号+长度<br/>";
                    errorMsg.append("第").append(i + 1).append("条跟第").append(j + 1).append("条重复,值是 : ")
                        .append(temp + "<br/>");
                    failNum++;
                    // list.remove(j);
                    list.get(j).setSensorNumber("REPEAT");
                }
            }
        }
        List<RodSensorImportForm> importList = new ArrayList<RodSensorImportForm>();
        for (int i = 0; i < list.size(); i++) {
            StringBuffer sb = new StringBuffer();
            RodSensorImportForm rodSensorForm = list.get(i);
            if ("REPEAT".equals((rodSensorForm.getSensorNumber()))) {
                continue;
            }
            // 校验必填字段
            if (Converter.toBlank(rodSensorForm.getSensorNumber()).equals("") || Converter
                .toBlank(rodSensorForm.getSensorLength()).equals("")) {
                resultMap.put("flag", 1);
                errorMsg.append("第").append(i + 1).append("条数据必填字段未填<br/>");
                failNum++;
                continue;
            } else if (!rodSensorForm.getSensorNumber().matches("^[A-Za-z0-9_#\\*\\u4e00-\\u9fa5\\-]{1,25}$")) {
                resultMap.put("flag", 1);
                errorMsg.append("第").append(i + 1).append("条数据【传感器型号】异常，仅中文、字母、数字或特殊符号*、-、_、#，长度不超过25位<br/>");
                failNum++;
                continue;
            } else if (Converter.toBlank(rodSensorForm.getSensorLength()).length() > SENSOR_LENGTH_MAXLENGTH || !Pattern
                .matches("^[1-9]\\d*$", rodSensorForm.getSensorLength())) {
                resultMap.put("flag", 1);
                errorMsg.append("第").append(i + 1).append("条数据【传感器长度】异常，传感器长度不超过").append(SENSOR_LENGTH_MAXLENGTH)
                    .append("位且为正整数<br/>");
                failNum++;
                continue;
            }
            // 编号重复
            String a = rodSensorForm.getSensorNumber();
            List<RodSensor> list1 = rodSensorDao.findBySensorNumber(a);
            if (list1.size() != 0) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据【传感器型号】异常，传感器为“").append(rodSensorForm.getSensorNumber())
                    .append("”已存在<br/>");
                failNum++;
                continue;
            }
            // 判断滤波系数、波特率、奇偶校验、补偿使能是否为空，如果为空，填写默认值
            if (StringUtils.isBlank(rodSensorForm.getFilteringFactorStr())) {
                rodSensorForm.setFilteringFactorStr("平滑");
            }
            if (StringUtils.isBlank(rodSensorForm.getBaudRateStr())) {
                rodSensorForm.setBaudRateStr("9600");
            }
            if (StringUtils.isBlank(rodSensorForm.getOddEvenCheckStr())) {
                rodSensorForm.setOddEvenCheckStr("偶校验");
            }
            if (StringUtils.isBlank(rodSensorForm.getCompensationCanMakeStr())) {
                rodSensorForm.setCompensationCanMakeStr("使能");
            }
            dealRodSensorForm(rodSensorForm, request, "import");
            importList.add(rodSensorForm);
            message.append("导入油量传感器 : ").append(rodSensorForm.getSensorNumber()).append(" <br/>");
        }
        // 组装导入结果
        if (importList.size() > 0) {
            boolean flag = false;
            // 导入逻辑（暂时只有新增，具体导入逻辑还需需求确定）
            flag = rodSensorDao.addByBatch(importList);
            if (flag) {
                resultInfo += "成功" + (totalNum - failNum) + "条,失败" + failNum + "条。";
                resultMap.put("flag", 1);
                resultMap.put("errorMsg", errorMsg);
                resultMap.put("resultInfo", resultInfo);
                logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "batch", "导入油位传感器");
            } else {
                resultMap.put("flag", 0);
                resultMap.put("resultInfo", "导入失败！");
                return resultMap;
            }

        } else {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", errorMsg);
            resultMap.put("resultInfo", "成功导入0条数据。");
            return resultMap;
        }
        return resultMap;
    }

    // --匆删，以防后面需求变更后需要还原 liubq 2016-12-22
    /*
     * @Override public boolean generateTemplate(HttpServletResponse response) { List<String> headList = new
     * ArrayList<String>(); List<String> requiredList = new ArrayList<String>(); List<Object> exportList = new
     * ArrayList<Object>(); // 表头 headList.add("传感器型号"); headList.add("传感器长度"); headList.add("滤波系数");
     * headList.add("波特率"); headList.add("奇偶校验"); headList.add("补偿使能"); // 必填字段 requiredList.add("传感器型号");
     * requiredList.add("传感器长度"); requiredList.add("滤波系数"); requiredList.add("波特率"); requiredList.add("奇偶检验");
     * requiredList.add("补偿使能"); // 默认设置一条数据 exportList.add("20160829001"); exportList.add("500"); exportList.add("平滑");
     * exportList.add("9600"); exportList.add("偶校验"); exportList.add("使能"); // 组装有下拉框的map Map<String,String[]> selectMap
     * = new HashMap<String,String[]>(); // 滤波系数 String[] filteringFactor = {"平滑","实时","平稳"}; selectMap.put("滤波系数",
     * filteringFactor); // 波特率 String[] baudRate = {"2400","4800","9600","19200","38400","57600","115200"};
     * selectMap.put("波特率", baudRate); // 奇偶校验 String[] oddEvenCheck = {"无校验","奇校验","偶校验"}; selectMap.put("奇偶校验",
     * oddEvenCheck); // 补偿使能 String[] compensation = {"使能","禁能"}; selectMap.put("补偿使能", compensation); ExportExcel
     * export = new ExportExcel(headList, requiredList,selectMap); Row row = export.addRow(); for (int j = 0; j <
     * exportList.size(); j++) { export.addCell(row, j, exportList.get(j)); } // 输出导文件 OutputStream out; try { out =
     * response.getOutputStream(); export.write(out);// 将文档对象写入文件输出流 out.close(); } catch (IOException e) {
     * e.printStackTrace(); return false; } return true; }
     */
    @Override
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<String>();
        List<String> requiredList = new ArrayList<String>();
        List<Object> exportList = new ArrayList<Object>();
        // 表头
        headList.add("传感器型号");
        headList.add("传感器长度(mm)");
        headList.add("滤波系数*");
        headList.add("波特率*");
        headList.add("奇偶校验*");
        headList.add("补偿使能*");
        headList.add("备注*");
        // 必填字段
        requiredList.add("传感器型号");
        requiredList.add("传感器长度(mm)");
        // 默认设置一条数据
        exportList.add("soway-01");
        exportList.add("500");
        exportList.add("平滑");
        exportList.add("9600");
        exportList.add("偶校验");
        exportList.add("使能");
        exportList.add("备注信息");

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<String, String[]>();
        // 滤波系数
        String[] filteringFactor = { "平滑", "实时", "平稳" };
        selectMap.put("滤波系数*", filteringFactor);
        // 波特率
        String[] baudRate = { "2400", "4800", "9600", "19200", "38400", "57600", "115200" };
        selectMap.put("波特率*", baudRate);
        // 奇偶校验
        String[] oddEvenCheck = { "无校验", "奇校验", "偶校验" };
        selectMap.put("奇偶校验*", oddEvenCheck);
        // 补偿使能
        String[] compensation = { "使能", "禁用" };
        selectMap.put("补偿使能*", compensation);

        // ExportExcel export = new ExportExcel(headList, requiredList,selectMap);
        ExportOilBoxExcel export = new ExportOilBoxExcel(TEMPLATE_COMMENT, headList, requiredList, selectMap);
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
    public RodSensor findByRodSensor(String sensorNumber) throws Exception {
        return rodSensorDao.findByRodSensor(sensorNumber);
    }

    @Override
    public int getIsBand(String id) throws Exception {
        return rodSensorDao.getIsBand(id);
    }

    @Override
    public RodSensor findById(String id) throws Exception {
        return rodSensorDao.findById(id);
    }

    @Override
    public RodSensor findByRodSensor(String id, String sensorNumber) throws Exception {
        return rodSensorDao.isExist(id, sensorNumber);
    }
}
