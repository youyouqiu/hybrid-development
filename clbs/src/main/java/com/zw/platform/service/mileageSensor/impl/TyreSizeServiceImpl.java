package com.zw.platform.service.mileageSensor.impl;

import com.github.pagehelper.Page;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.vas.mileageSensor.TyreSize;
import com.zw.platform.domain.vas.mileageSensor.TyreSizeQuery;
import com.zw.platform.repository.vas.TyreSizeDao;
import com.zw.platform.service.mileageSensor.TyreSizeService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportNewExcel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.regex.Pattern;

@Service
public class TyreSizeServiceImpl implements TyreSizeService {
    @Autowired
    TyreSizeDao tyreSizeDao;

    @Autowired
    private LogSearchService logSearchService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${tyre.size.set}")
    private String tyreSizeSet;

    @Value("${tyre.size.use}")
    private String tyreSizeUse;

    @Value("${set.success}")
    private String setSuccess;

    @Value("${add.success}")
    private String addSuccess;

    private static final String DELETE_ERROR_MSG = "部分传感器已经和车辆绑定了，到【里程监测设置】中解除绑定后才可以删除哟！";

    /**
     * 新增轮胎规格
     */
    @Override
    public JsonResultBean addTyreSize(TyreSize tyreSize, String ipAddress) {
        TyreSize t = findByTypeAndName(tyreSize.getTireType(), tyreSize.getSizeName());
        if (t != null && !t.getId().equals(tyreSize.getId())) {
            return new JsonResultBean(JsonResultBean.FAULT, tyreSizeSet);
        }
        boolean flag = tyreSizeDao.addTyreSize(tyreSize);
        if (flag) {
            if (!tyreSize.getSizeName().isEmpty()) {
                String msg = "新增轮胎规格：" + tyreSize.getSizeName() + " 种类为 : " + tyreSize.getTireType();
                logSearchService.addLog(ipAddress, msg, "3", "轮胎规格管理", "-", "");
                return new JsonResultBean(JsonResultBean.SUCCESS, addSuccess);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 修改轮胎规格信息
     */
    @Override
    public JsonResultBean updateTyreSize(TyreSize tyreSize, String ipAddress) {
        TyreSize t = findByTypeAndName(tyreSize.getTireType(), tyreSize.getSizeName());
        if (t != null && !t.getId().equals(tyreSize.getId())) {
            return new JsonResultBean(JsonResultBean.FAULT, tyreSizeSet);
        }
        TyreSize form = tyreSizeDao.findById(tyreSize.getId());
        if (form != null) {
            boolean flag = tyreSizeDao.updateTyreSize(tyreSize);
            if (flag) {
                String beforeSizeName = form.getSizeName();
                String tyreSizeName = tyreSize.getSizeName();
                String beforeType = form.getTireType();
                String tireType = tyreSize.getTireType();
                String message;
                if (!tyreSizeName.equals(beforeSizeName) && tireType.equals(beforeType)) { // 修改轮胎规格,但为修改轮胎种类
                    message = "修改轮胎种类为 " + tireType + " 的轮胎规格 : " + beforeSizeName + " 为 " + tyreSizeName;
                } else if (!tyreSizeName.equals(beforeSizeName)) { // 修改轮胎规格,也修改了轮胎种类
                    message =
                        "修改轮胎规格 : " + beforeSizeName + " 为 " + tyreSizeName + "和修改轮胎种类" + beforeType + " 为 " + tireType;
                } else if (!tireType.equals(beforeType)) { // 修改轮胎种类,没有修改轮胎规格
                    message = "修改轮胎规格 : " + tyreSizeName + " 的轮胎种类 : " + beforeType + " 为 " + tireType;
                } else {
                    message = "修改轮胎规格 : " + tyreSizeName + " 的信息";
                }
                logSearchService.addLog(ipAddress, message, "3", "轮胎规格管理", "-", "");
                return new JsonResultBean(JsonResultBean.SUCCESS, setSuccess);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 根据轮胎规格设置id删除轮胎规格设置(包括单个删除和批量删除)
     * @param tyreSizes 轮胎规格设置id集合
     */
    @Override
    public JsonResultBean deleteBatchTyreSize(List<String> tyreSizes, String ipAddress) {
        StringBuilder message = new StringBuilder();
        //没有绑定的轮胎id
        List<String> unbindSensors = new ArrayList<>();
        String bindSensor;
        //绑定的轮胎型号日志记录
        StringBuilder deleteFailMsg = new StringBuilder();
        for (String id : tyreSizes) {
            if (!id.isEmpty()) {
                bindSensor = checkConfig(id);
                if (StringUtils.isNotBlank(bindSensor)) {
                    deleteFailMsg.append(bindSensor).append("</br>");
                    continue;
                }
                TyreSize form = tyreSizeDao.findById(id);
                if (form != null) {
                    message.append("删除轮胎规格 ：").append(form.getSizeName()).append(" <br/>");
                    unbindSensors.add(id);
                }
            }
        }
        if (unbindSensors.size() > 0) {
            this.tyreSizeDao.deleteBatchTyreSize(unbindSensors);
            if (tyreSizes.size() == 1) {
                logSearchService.addLog(ipAddress, message.toString(), "3", "轮胎规格管理", "-", "");
            } else {
                logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "批量删除轮胎规格设置");
            }
        }
        if (StringUtils.isNotBlank(deleteFailMsg.toString())) {
            return new JsonResultBean(JsonResultBean.SUCCESS,
                DELETE_ERROR_MSG + "</br>" + "已绑定轮胎规格如下：</br>" + deleteFailMsg);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 根据轮胎规格id查询轮胎规格
     * @param id 轮胎规格id
     * @return TyreSize
     */
    @Override
    public TyreSize findById(String id) {
        return this.tyreSizeDao.findById(id);
    }

    /**
     * 根据轮胎种类和轮胎规格查询轮胎规格信息
     * @param tireType 类型
     * @param sizeName 规格
     */
    @Override
    public TyreSize findByTypeAndName(String tireType, String sizeName) {
        return this.tyreSizeDao.findByTypeAndName(tireType, sizeName);
    }

    /**
     * 分页查询轮胎规格列表
     */
    @Override
    public Page<TyreSize> findByQuery(TyreSizeQuery query) {
        if (query != null) {
            String simpleQueryParam = query.getSimpleQueryParam();
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(simpleQueryParam));
        }
        return PageHelperUtil.doSelect(query, () -> tyreSizeDao.findByQuery(query));
    }

    /**
     * 查询全部的轮胎规格信息
     */
    @Override
    public List<TyreSize> findAll() {
        return tyreSizeDao.findAll();
    }

    /**
     * 查询被绑定的轮胎规格的数量
     * @param id 轮胎规格id
     * @return Integer
     */
    @Override
    public String checkConfig(String id) {
        return this.tyreSizeDao.checkConfig(id);
    }

    @Override
    public Map addImportTyreSize(MultipartFile multipartFile, String ipAddress) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("flag", 0);
        StringBuilder errorMsg = new StringBuilder();
        String resultInfo = "";
        StringBuilder message = new StringBuilder();
        // 导入的文件
        ImportNewExcel importExcel = new ImportNewExcel(multipartFile, 1, 0);
        // excel 转换成 list
        List<TyreSize> list = importExcel.getDataList(TyreSize.class);
        String temp;
        List<TyreSize> importList = new ArrayList<>();
        // 校验需要导入的油箱
        if (list.size() == 0) {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", "请将导入文件按照模板格式整理后再导入！");
            resultMap.put("resultInfo", "导入失败！");
            return resultMap;
        }
        // 列表中重复数据
        if (CollectionUtils.isNotEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                TyreSize sensor = list.get(i);
                if (StringUtil.isNullOrBlank(sensor.getSizeName()) || StringUtil
                    .isNullOrBlank(sensor.getRollingRadiusStr()) || StringUtil.isNullOrBlank(sensor.getTireType())
                    || "REPEAT".equals(sensor.getTireType())) {
                    continue;
                }
                for (int j = i + 1; j < list.size(); j++) {
                    if (sensor.getTireType().equals(list.get(j).getTireType()) && sensor.getSizeName()
                        .equals(list.get(j).getSizeName())) {
                        temp = sensor.getSizeName() + "#" + sensor.getTireType();
                        errorMsg.append("第").append(i + 1).append("行跟第").append(j + 1).append("行重复，值是：").append(temp)
                            .append("<br/>");
                        list.get(j).setTireType("REPEAT");
                    }
                }
            }
        }
        for (int i = 0; i < list.size(); i++) {
            TyreSize sensor = list.get(i);
            // 校验必填字段
            if (StringUtils.isBlank(sensor.getTireType()) || StringUtils.isBlank(sensor.getSizeName())
                || sensor.getRollingRadiusStr() == null) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据必填字段未填 </br>");
                continue;
            }
            if ("REPEAT".equals(sensor.getTireType())) {
                continue;
            }
            try {
                sensor.setRollingRadius(Integer.valueOf(sensor.getRollingRadiusStr()));
            } catch (Exception ex) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据【轮胎滚动半径】异常，轮胎滚动半径值错误 </br>");
                continue;
            }

            if (!"斜交轮胎".equals(sensor.getTireType()) && !"子午线轮胎".equals(sensor.getTireType())) {
                errorMsg.append("第").append(i + 1).append("条数据【轮胎类别】异常，轮胎类别错误 </br>");
                continue;
            }
            if (!Pattern.matches("^[A-Za-z0-9_#*\\u4e00-\\u9fa5\\-./]{1,25}$", sensor.getSizeName())) {
                errorMsg.append("第").append(i + 1).append("条数据【轮胎规格】异常，请输入中文、字母、数字或特殊符号*、-、_、#、/、.,长度不超过25位 </br>");
                continue;
            }
            if (sensor.getRollingRadius() > 65535) {
                errorMsg.append("第").append(i + 1).append("条数据【轮胎滚动半径】异常，轮胎滚动半径超过65535 </br>");
                continue;
            }
            // 与数据库是否有重复数据
            if (this.tyreSizeDao.findByTypeAndName(sensor.getTireType(), sensor.getSizeName()) != null) {
                resultMap.put("flag", 0);
                errorMsg.append("轮胎种类为“").append(sensor.getTireType()).append("”且轮胎规格“").append(sensor.getSizeName())
                    .append("”已存在<br/>");
                continue;
            }
            sensor.setCreateDataTime(new Date());
            sensor.setRemark("");
            sensor.setCreateDataUsername(SystemHelper.getCurrentUsername());
            importList.add(sensor);
            message.append("导入轮胎规格 : ").append(sensor.getSizeName()).append(" <br/>");
        }
        // 组装导入结果
        if (importList.size() == 0) {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", errorMsg);
            resultMap.put("resultInfo", "成功导入0条数据。");
            return resultMap;
        }

        // 导入逻辑（暂时只有新增，具体导入逻辑还需需求确定）
        boolean flag = tyreSizeDao.addBatchTyreSize(importList);
        if (flag) {
            resultInfo += "导入成功" + importList.size() + "条数据,导入失败" + (list.size() - importList.size()) + "条数据。";
            resultMap.put("flag", 1);
            resultMap.put("errorMsg", errorMsg);
            resultMap.put("resultInfo", resultInfo);
            logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "导入轮胎规格");
        } else {
            resultMap.put("flag", 0);
            resultMap.put("resultInfo", "导入失败！");
            return resultMap;
        }
        resultMap.put("errorMsg", errorMsg);
        resultMap.put("resultInfo", resultInfo);
        return resultMap;
    }

    /**
     * 导出
     */
    @Override
    public boolean export(String title, int type, HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(title, TyreSize.class, 1);
        Page<TyreSize> list = tyreSizeDao.findByQuery(null);
        for (TyreSize ts : list) {
            ts.setRollingRadiusStr(ts.getRollingRadius().toString());
        }
        export.setDataList(list);
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
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // 表头
        headList.add("轮胎种类");
        headList.add("轮胎规格");
        headList.add("轮胎滚动半径(mm)");
        // 必填字段
        requiredList.add("轮胎种类");
        requiredList.add("轮胎规格");
        requiredList.add("轮胎滚动半径(mm)");
        // 默认设置一条数据
        exportList.add("斜交轮胎");
        exportList.add("14-00-20");
        exportList.add("590");

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<>();
        // 种类
        String[] parity = { "斜交轮胎", "子午线轮胎" };
        selectMap.put("种类", parity);
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
