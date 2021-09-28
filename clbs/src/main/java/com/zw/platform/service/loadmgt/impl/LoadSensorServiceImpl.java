package com.zw.platform.service.loadmgt.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.query.LoadSensorQuery;
import com.zw.platform.domain.vas.loadmgt.ZwMSensorInfo;
import com.zw.platform.domain.vas.loadmgt.form.LoadSensorForm;
import com.zw.platform.domain.vas.loadmgt.form.LoadSensorImportForm;
import com.zw.platform.repository.vas.LoadSensorDao;
import com.zw.platform.repository.vas.TransduserDao;
import com.zw.platform.service.loadmgt.LoadSensorService;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
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

/***
 @Author gfw
 @Date 2018/9/6 14:25
 @Description 载重传感器接口实现
 @version 1.0
 **/
@Service
public class LoadSensorServiceImpl implements LoadSensorService {

    private static Logger log = LogManager.getLogger(LoadSensorServiceImpl.class);

    /**
     * 传感器型号最大长度
     */
    private static final Integer SENSOR_NUMBER_MAX_LENGTH = 25;

    private static final String REGEXP = "^[A-Za-z0-9_#\\*\\u4e00-\\u9fa5\\-]+$";

    /**
     * 传感器长度最大允许长度
     */
    private static final Integer SENSOR_LENGTH_MAX_LENGTH = 5;

    private static final String DELETE_ERROR_MESSAGE = "部分传感器已经和车辆绑定了,请解除后再删除传感器！";

    private static final String import_ERROR_MESSAGE = "请将导入文件按照模板格式整理后再导入！";

    private static final String import_ERROR_INFO = "请将导入文件按照模板格式整理后再导入！";

    private static final String import_ERROR_MSG = "成功导入0条数据!";

    private static final String REPEAT_VALUE = "REPEAT-REPEAT-REPEAT";

    private static final String TEMPLATE_COMMENT =
        "注：红色标注为必填；传感器型号组合规则：品牌+型号+长度，" + "如：“soway-01-500”；滤波系数如不填默认“平滑”，波特率默认“9600”，奇偶校验默认“偶校验”，"
            + "补偿使能默认“使能”(另：整理前请删除示例数据，谢谢)";

    @Autowired
    LoadSensorDao loadSensorDao;

    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;

    @Autowired
    private TransduserDao transduserDao;

    @Value("${add.success}")
    private String addSuccess;

    /**
     * 条件查询 分页列表展示
     * @param query
     * @return 分页信息 ZwMSensorInfo
     * @throws Exception
     * @Author gfw
     */
    @Override
    public Page<ZwMSensorInfo> findByPage(LoadSensorQuery query) throws Exception {
        if (query != null) {
            // mybatis 设置分页

            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam()));
            return PageHelperUtil.doSelect(query, () -> loadSensorDao.findListByQuery(query));
        }
        return null;
    }

    /**
     * 新增 单个载重传感器
     * @param form
     * @param ipAddress
     * @return
     * @throws Exception
     */
    @Override
    public JsonResultBean add(LoadSensorForm form, String ipAddress) throws Exception {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        form.setCreateDataTime(new Date());
        // 判定传感器型号是否一致
        List<ZwMSensorInfo> bySensorNumber = loadSensorDao.findBySensorNumber(form.getSensorNumber());
        if (bySensorNumber.size() > 0) {
            return new JsonResultBean(JsonResultBean.FAULT, "已存在该型号传感器");
        }
        boolean flag = loadSensorDao.add(form);
        if (flag) {
            String logs = "新增载重传感器：" + form.getSensorNumber();
            logSearchServiceImpl.addLog(ipAddress, logs, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS, addSuccess);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 导出传感器列表
     * @param title
     * @param type
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    public void exportList(String title, int type, HttpServletResponse response, LoadSensorQuery query)
        throws Exception {
        ExportExcel export = new ExportExcel(title, LoadSensorForm.class, 1, null);
        List<LoadSensorForm> exportList = new ArrayList<>();
        List<ZwMSensorInfo> list = loadSensorDao.findListByQuery(query);
        for (ZwMSensorInfo info : list) {
            LoadSensorForm form = new LoadSensorForm();
            BeanUtils.copyProperties(info, form);
            form.setCompensate(new Long(info.getCompensate()).intValue());
            form.setBaudRate(new Long(info.getBaudRate()).intValue());
            form.setOddEvenCheck(new Long(info.getOddEvenCheck()).intValue());
            form.setFilterFactor(new Long(info.getFilterFactor()).intValue());
            // 处理form数据，将数字代表的信息转换成相对应的明文信息
            dealLoadSensorExportForm(form);
            exportList.add(form);
        }
        export.setDataList(exportList);
        OutputStream out;
        out = response.getOutputStream();
        /**
         * 文档对象写入文档输出流
         */
        export.write(out);
        out.close();
    }

    /**
     * 批量删除载重传感器
     * @param id
     * @param ipAddress
     * @return
     * @throws Exception
     */
    @Override
    public JsonResultBean deleteMore(String id, String ipAddress) throws Exception {
        String[] items = id.split(",");
        StringBuilder message = new StringBuilder();
        JSONObject msg = new JSONObject();
        StringBuilder deleteFailMsg = new StringBuilder();
        boolean noBind = dealBindSensor(items, message, deleteFailMsg);
        recorderDeleteLog(ipAddress, items, message);
        if (noBind) {
            return new JsonResultBean(msg);
        } else {
            String errorMsg = DELETE_ERROR_MESSAGE + "</br>" + "已绑定传感器型号如下：</br>" + deleteFailMsg;
            return new JsonResultBean(JsonResultBean.FAULT, errorMsg);

        }
    }

    /**
     * 生成导入模板
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    public void generateTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // 表头
        headList.add("传感器型号");
        //        headList.add("传感器长度(mm)");
        headList.add("滤波系数*");
        headList.add("波特率*");
        headList.add("奇偶校验*");
        headList.add("补偿使能*");
        headList.add("备注*");
        // 必填字段
        requiredList.add("传感器型号");
        //        requiredList.add("传感器长度(mm)");
        // 默认设置一条数据
        exportList.add("a-01-1");
        //        exportList.add("500");
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
        // 传感器类型
        // ExportExcel export = new ExportExcel(headList, requiredList,selectMap);
        ExportOilBoxExcel export = new ExportOilBoxExcel(TEMPLATE_COMMENT, headList, requiredList, selectMap);
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
     * 根据传输模板批量导入载重传感器
     * @param multipartFile
     * @param request
     * @param ipAddress
     * @return
     * @throws Exception
     */
    @Override
    public Map importBatch(MultipartFile multipartFile, HttpServletRequest request, String ipAddress) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        Integer limitNum = 6;
        resultMap.put("flag", 0);
        StringBuilder errorMsg = new StringBuilder();
        StringBuilder message = new StringBuilder();
        int failNum = 0;
        int totalNum = 0;
        // 导入的文件
        ImportExcel importExcel = new ImportExcel(multipartFile, 2, 0);
        int cellNum = importExcel.getLastCellNum();
        /**
         * 元素不够
         */
        if (cellNum != limitNum) {
            resultMapInfo(0, resultMap, import_ERROR_MESSAGE, import_ERROR_INFO);
            return resultMap;
        }
        // excel 转换成 list
        List<LoadSensorImportForm> list = importExcel.getDataList(LoadSensorImportForm.class, null);
        String temp = "";
        if (null != list && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if ("REPEAT".equals(list.get(i).getSensorNumber()) || StringUtil
                    .isNullOrBlank(list.get(i).getSensorNumber())) {
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
            totalNum = list.size();

            List<LoadSensorImportForm> importList = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                StringBuffer sb = new StringBuffer();
                LoadSensorImportForm loadSensorForm = list.get(i);
                if ("REPEAT".equals(list.get(i).getSensorNumber())) {
                    continue;
                }
                // 校验必填字段
                if (Converter.toBlank(loadSensorForm.getSensorNumber()).equals("")) {
                    resultMap.put("flag", 1);
                    errorMsg.append("第").append(i + 1).append("条数据必填字段未填<br/>");
                    failNum++;
                    continue;
                }
                if (!Pattern.matches(REGEXP, loadSensorForm.getSensorNumber())) {
                    resultMap.put("flag", 1);
                    errorMsg.append("第").append(i + 1).append("条数据【传感器型号】异常，仅中文、字母、数字或特殊符号*、-、_、#，长度不超过25位<br/>");
                    failNum++;
                    continue;
                } else if (Converter.toBlank(loadSensorForm.getSensorNumber()).length() > SENSOR_NUMBER_MAX_LENGTH) {
                    resultMap.put("flag", 1);
                    errorMsg.append("第").append(i + 1).append("条数据【传感器型号】异常，仅中文、字母、数字或特殊符号*、-、_、#，长度不超过25位<br/>");
                    failNum++;
                    continue;
                }
                // 编号重复
                String a = loadSensorForm.getSensorNumber();
                List<ZwMSensorInfo> list1 = loadSensorDao.findBySensorNumber(a);
                if (list1.size() != 0) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i + 1).append("条数据【载重编号】异常，“").append(loadSensorForm.getSensorNumber())
                        .append("”已存在<br/>");
                    failNum++;
                    continue;
                }
                // 判断滤波系数、波特率、奇偶校验、补偿使能是否为空，如果为空，填写默认值
                if (StringUtils.isBlank(loadSensorForm.getFilteringFactorStr())) {
                    loadSensorForm.setFilteringFactorStr("平滑");
                }
                if (StringUtils.isBlank(loadSensorForm.getBaudRateStr())) {
                    loadSensorForm.setBaudRateStr("9600");
                }
                if (StringUtils.isBlank(loadSensorForm.getOddEvenCheckStr())) {
                    loadSensorForm.setOddEvenCheckStr("偶校验");
                }
                if (StringUtils.isBlank(loadSensorForm.getCompensationCanMakeStr())) {
                    loadSensorForm.setCompensationCanMakeStr("使能");
                }
                dealRodSensorForm(loadSensorForm, request, "import");
                importList.add(loadSensorForm);
                message.append("导入载重传感器 : ").append(loadSensorForm.getSensorNumber()).append(" <br/>");
            }

            // 组装导入结果
            if (importList.size() > 0) {
                boolean flag = false;
                // 导入逻辑 批量新增
                flag = loadSensorDao.addByBatch(importList);
                if (flag) {
                    logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "batch", "导入油位传感器");
                    String resultInfo = "";
                    resultInfo += "成功" + (totalNum - failNum) + "条,失败" + failNum + "条。";
                    resultMapInfo(1, resultMap, errorMsg.toString(), resultInfo);
                } else {
                    resultMap.put("flag", 0);
                    resultMap.put("resultInfo", "导入失败！");
                    return resultMap;
                }
            } else {
                resultMapInfo(0, resultMap, errorMsg.toString(), import_ERROR_MSG);
                return resultMap;
            }
        }
        return resultMap;
    }

    private void resultMapInfo(int i, Map<String, Object> resultMap, String msg, String info) {
        resultMap.put("flag", i);
        resultMap.put("errorMsg", msg);
        resultMap.put("resultInfo", info);
    }

    /**
     * @param id 传感器id
     * @return
     */
    @Override
    public ZwMSensorInfo getById(String id) {
        if (StringUtils.isNotBlank(id)) {
            return loadSensorDao.findSensorById(id);
        }
        return null;
    }

    /**
     * @param form      传感器信息
     * @param ipAddress ip地址
     * @return
     */
    @Override
    public JsonResultBean update(LoadSensorForm form, String ipAddress) throws Exception {
        // 记录前传感器名字
        String beforeName = loadSensorDao.findSensorById(form.getId()).getSensorNumber();
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        form.setUpdateDataTime(new Date());
        boolean flag = loadSensorDao.update(form);
        if (flag) {
            String message;
            // 编号不相同
            if (!beforeName.equals(form.getSensorNumber())) {
                message = "修改载重传感器型号：" + beforeName + " 修改为  " + form.getSensorNumber();
            } else {
                message = "修改载重传感器型号：" + form.getSensorNumber();
            }
            logSearchServiceImpl.addLog(ipAddress, message, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @Override
    public boolean repetition(String sensorNumber, String id) {
        //通过传感器型号找是否重复
        List<ZwMSensorInfo> bySensorNumber = loadSensorDao.findBySensorNumber(sensorNumber);
        if (bySensorNumber != null && !bySensorNumber.isEmpty()) {
            if (id != null) {
                //用id排除自身原本型号名可以重复
                ZwMSensorInfo sensorById = loadSensorDao.findSensorById(id);
                if (sensorNumber.equals(sensorById.getSensorNumber())) {
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
     * 处理form数据-导出 导出：将数字代表的信息转换成相对应的明文信息 导入：将相应的明文信息转换成数据库的数字
     * @param form
     * @Title: dealRodSensorForm
     * @author Liubangquan
     */
    private void dealLoadSensorExportForm(LoadSensorForm form) throws Exception {
        // 启停状态
        /*
         * if (Converter.toBlank(form.getIsStart()).equals("1")) { form.setIsStartStr("启用"); } else if
         * (Converter.toBlank(form.getIsStart()).equals("2")) { form.setIsStartStr("停用"); }
         */
        // 补偿使能
        if (Converter.toBlank(form.getCompensate()).equals("1")) {
            form.setCompensateStr("使能");
        } else if (Converter.toBlank(form.getCompensate()).equals("2")) {
            form.setCompensateStr("禁能");
        } else {
            form.setCompensateStr("使能");
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
        if (Converter.toBlank(form.getFilterFactor()).equals("1")) {
            form.setFilterFactorStr("实时");
        } else if (Converter.toBlank(form.getFilterFactor()).equals("2")) {
            form.setFilterFactorStr("平滑");
        } else {
            form.setFilterFactorStr("平稳");
        }
        // 波特率
        if (Converter.toBlank(form.getBaudRate()).equals("1")) {
            form.setBaudRateStr("2400");
        } else if (Converter.toBlank(form.getBaudRate()).equals("2")) {
            form.setBaudRateStr("4800");
        } else if (Converter.toBlank(form.getBaudRate()).equals("3")) {
            form.setBaudRateStr("9600");
        } else if (Converter.toBlank(form.getBaudRate()).equals("4")) {
            form.setBaudRateStr("19200");
        } else if (Converter.toBlank(form.getBaudRate()).equals("5")) {
            form.setBaudRateStr("38400");
        } else if (Converter.toBlank(form.getBaudRate()).equals("6")) {
            form.setBaudRateStr("57600");
        } else if (Converter.toBlank(form.getBaudRate()).equals("7")) {
            form.setBaudRateStr("115200");
        } else {
            form.setBaudRateStr("9600");
        }
        // 出厂时间
        // form.setFactoryDateStr(Converter.toString(form.getFactoryDate(), "yyyy-MM-dd"));
    }

    /**
     * 查询删除的传感器中是否有已经进行过绑定的传感器
     * @param items
     * @param message
     * @param deleteFailMsg
     * @return
     * @throws Exception
     */
    private boolean dealBindSensor(String[] items, StringBuilder message, StringBuilder deleteFailMsg) {
        boolean noBind = true;
        for (String sensorId : items) {
            int bindRodSensorNum = getIsBand(sensorId);
            if (bindRodSensorNum > 0) {
                noBind = false;
                ZwMSensorInfo rs = loadSensorDao.findSensorById(sensorId);
                deleteFailMsg.append(null != rs ? rs.getSensorNumber() : "").append("</br>");
                continue;
            } else {
                // 根据id查询传感器信息
                ZwMSensorInfo rodSensor = loadSensorDao.findSensorById(sensorId);
                boolean flag = loadSensorDao.deleteById(sensorId);
                // 删除成功
                if (flag) {
                    message.append("删除载重传感器 ： ").append(rodSensor.getSensorNumber()).append(" <br/>");
                }
            }
        }
        return noBind;
    }

    /**
     * 判定载重传感器是否已经绑定
     * @param sensorId
     * @return
     */
    private int getIsBand(String sensorId) {
        return transduserDao.checkBoundNumberById(sensorId);
    }

    /**
     * 记录删除的传感器日志
     * @param ipAddress
     * @param items
     * @param message
     * @throws Exception
     */
    private void recorderDeleteLog(String ipAddress, String[] items, StringBuilder message) throws Exception {
        if (!message.toString().isEmpty()) {
            if (items.length == 1) {
                logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "", "-", "");
            } else {
                logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "batch", "批量删除载重传感器");
            }

        }
    }

    /**
     * 处理form数据 导出：将数字代表的信息转换成相对应的明文信息 导入：将相应的明文信息转换成数据库的数字
     * @param loadSensorImportForm
     * @param importOrExport
     * @return void
     * @Title: dealRodSensorForm
     * @author Liubangquan
     */
    private void dealRodSensorForm(LoadSensorImportForm loadSensorImportForm, HttpServletRequest request,
        String importOrExport) throws Exception {
        // 补偿使能
        if (Converter.toBlank(loadSensorImportForm.getCompensationCanMakeStr()).equals("使能")) {
            loadSensorImportForm.setCompensationCanMake((short) 1);
        } else if (Converter.toBlank(loadSensorImportForm.getCompensationCanMakeStr()).equals("禁用")) {
            loadSensorImportForm.setCompensationCanMake((short) 2);
        } else {
            loadSensorImportForm.setCompensationCanMake((short) 1);
        }
        // 奇偶校验
        if (Converter.toBlank(loadSensorImportForm.getOddEvenCheckStr()).equals("无校验")) {
            loadSensorImportForm.setOddEvenCheck((short) 3);
        } else if (Converter.toBlank(loadSensorImportForm.getOddEvenCheckStr()).equals("奇校验")) {
            loadSensorImportForm.setOddEvenCheck((short) 1);
        } else if (Converter.toBlank(loadSensorImportForm.getOddEvenCheckStr()).equals("偶校验")) {
            loadSensorImportForm.setOddEvenCheck((short) 2);
        } else {
            loadSensorImportForm.setOddEvenCheck((short) 3);
        }
        // 滤波系数
        if (Converter.toBlank(loadSensorImportForm.getFilteringFactorStr()).equals("实时")) {
            loadSensorImportForm.setFilterFactor("1");
        } else if (Converter.toBlank(loadSensorImportForm.getFilteringFactorStr()).equals("平滑")) {
            loadSensorImportForm.setFilterFactor("2");
        } else {
            loadSensorImportForm.setFilterFactor("3");
        }
        // 波特率
        if (Converter.toBlank(loadSensorImportForm.getBaudRateStr()).equals("2400")) {
            loadSensorImportForm.setBaudRate("1");
        } else if (Converter.toBlank(loadSensorImportForm.getBaudRateStr()).equals("4800")) {
            loadSensorImportForm.setBaudRate("2");
        } else if (Converter.toBlank(loadSensorImportForm.getBaudRateStr()).equals("9600")) {
            loadSensorImportForm.setBaudRate("3");
        } else if (Converter.toBlank(loadSensorImportForm.getBaudRateStr()).equals("19200")) {
            loadSensorImportForm.setBaudRate("4");
        } else if (Converter.toBlank(loadSensorImportForm.getBaudRateStr()).equals("38400")) {
            loadSensorImportForm.setBaudRate("5");
        } else if (Converter.toBlank(loadSensorImportForm.getBaudRateStr()).equals("57600")) {
            loadSensorImportForm.setBaudRate("6");
        } else if (Converter.toBlank(loadSensorImportForm.getBaudRateStr()).equals("115200")) {
            loadSensorImportForm.setBaudRate("7");
        } else {
            loadSensorImportForm.setBaudRate("3");
        }

        // 获取当前用户
        SecurityContextImpl securityContextImpl =
            (SecurityContextImpl) request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
        String createDataUsername = securityContextImpl.getAuthentication().getName();
        loadSensorImportForm.setCreateDataUsername(createDataUsername);
    }
}
