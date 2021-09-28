package com.zw.platform.service.basicinfo.impl;

import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.zw.platform.basic.domain.VehicleCategoryDO;
import com.zw.platform.basic.domain.VehicleTypeDO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.basic.repository.NewVehicleCategoryDao;
import com.zw.platform.basic.repository.NewVehicleTypeDao;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.form.VehicleSubTypeForm;
import com.zw.platform.domain.basicinfo.form.VehicleTypeForm;
import com.zw.platform.domain.basicinfo.query.VehicleTypeQuery;
import com.zw.platform.service.basicinfo.VehicleTypeService;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportExcel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Modification by Wjy on 2016/7/26.
 */
@Deprecated
@Service("oldVehicleTypeService")
public class VehicleTypeServiceImpl implements VehicleTypeService {
    private static Logger log = LogManager.getLogger(VehicleTypeServiceImpl.class);

    @Autowired
    private NewVehicleTypeDao newVehicleTypeDao;

    @Autowired
    private NewVehicleCategoryDao newVehicleCategoryDao;

    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;

    private static final Integer BIGEST_SERVICE_CYCLE = 99999;

    private static final Integer LEAST_SERVICE_CYCLE = 0;

    @Override
    public void add(VehicleTypeForm form, String ipAddress) throws Exception {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        form.setCreateDataTime(new Date());
        boolean flag = newVehicleTypeDao.add(new VehicleTypeDO(form));
        if (flag) {
            String msg = "新增车型：" + form.getVehicleType();
            logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
        }

    }

    @MethodLog(name = "分页查询 User", description = "分页查询 User")
    @Override
    public Page<VehicleTypeDO> findByPage(VehicleTypeQuery query) throws Exception {
        if (StringUtils.isNotBlank(query.getSimpleQueryParam())) {
            // 特殊字符转译
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam()));
        }
        return PageHelperUtil.doSelect(query, () -> newVehicleTypeDao.findByPage(query));
    }

    @Override
    public JsonResultBean delete(String id, String ipAddress) throws Exception {
        String[] item = id.split(",");
        StringBuilder msg = new StringBuilder();
        StringBuilder result = new StringBuilder();
        StringBuilder resultVehicle = new StringBuilder();
        StringBuilder buildMessage = new StringBuilder();
        boolean buildFlag = false;
        for (int i = 0; i < item.length; i++) {
            VehicleTypeDTO vehicleType = get(item[i]);
            // 判断是否绑定了车辆子类型
            boolean isBindingSubType = this.checkTypeIsBindingSubType(item[i]);
            //判断是否绑定了车辆
            boolean isBindingVehicle = newVehicleTypeDao.getIsBandVehicle(item[i]);
            if (isBindingSubType) {
                result.append(vehicleType.getType()).append(",");
            }
            if (isBindingVehicle) {
                resultVehicle.append(vehicleType.getType()).append(",");
            }
            if (!isBindingSubType && !isBindingVehicle) {
                boolean flag = newVehicleTypeDao.delete(item[i]);
                if (flag) { // 删除成功,记录日志
                    msg.append("删除车型：").append(vehicleType.getType()).append(" <br/>");
                }
            }
        }
        if (!msg.toString().isEmpty()) {
            if (item.length == 1) {
                logSearchServiceImpl.addLog(ipAddress, msg.toString(), "3", "", "-", "");
            } else {
                logSearchServiceImpl.addLog(ipAddress, msg.toString(), "3", "batch", "批量删除车型");
            }
            if (StringUtils.isNotBlank(result.toString())) {
                buildMessage.append("车辆类型" + result.toString() + "已绑定子类型,请先解除绑定再删除!").append("<br/>");
                buildFlag = true;
            }
            if (StringUtils.isNotBlank(resultVehicle.toString())) {
                buildMessage.append("车辆类型" + resultVehicle.toString() + "已绑定车辆,请先解除绑定再删除!");
                buildFlag = true;
            }
            if (buildFlag) {
                return new JsonResultBean(JsonResultBean.FAULT, buildMessage.toString());
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else if (StringUtils.isNotBlank(result.toString()) || StringUtils.isNotBlank(resultVehicle.toString())) {
            if (StringUtils.isNotBlank(result.toString())) {
                buildMessage.append("车辆类型" + result.toString() + "已绑定子类型,请先解除绑定再删除!").append("<br/>");
                buildFlag = true;
            }
            if (StringUtils.isNotBlank(resultVehicle.toString())) {
                buildMessage.append("车辆类型" + resultVehicle.toString() + "已绑定车辆,请先解除绑定再删除!");
                buildFlag = true;
            }
            if (buildFlag) {
                return new JsonResultBean(JsonResultBean.FAULT, buildMessage.toString());
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public VehicleTypeDTO get(String id) throws Exception {
        return newVehicleTypeDao.getById(id);
    }

    @Override
    public JsonResultBean update(VehicleTypeForm form, String ipAddress) throws Exception {
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        form.setUpdateDataTime(new Date());
        VehicleTypeDTO vehicleType = get(form.getId());
        String beforeVehicleType = vehicleType.getType();
        String msg = "";
        if (vehicleType.getType().equals(form.getVehicleType())) {
            msg = "修改车型：" + form.getVehicleType();
        } else {
            msg = "修改车型：" + beforeVehicleType + " 修改为：" + form.getVehicleType();
        }
        logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
        boolean flag = newVehicleTypeDao.update(form.convertTypeDo());
        if (flag) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @MethodLog(name = "导出", description = "导出")
    @Override
    public boolean exportVehicleType(String title, int type, HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(title, VehicleTypeDTO.class, 1, null);
        List<VehicleTypeDTO> exportList = newVehicleTypeDao.getByKeyword(null);
        export.setDataList(exportList);
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
        return true;
    }

    @MethodLog(name = "批量导入", description = "批量导入")
    public Map importVehicleType(MultipartFile multipartFile, String ipAddress) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("flag", 0);
        String errorMsg = "";
        String resultInfo = "";
        // 导入的文件
        ImportExcel importExcel = new ImportExcel(multipartFile, 1, 0);
        Row row = importExcel.getRow(0);
        String string = importExcel.getCellValue(row, 0).toString();
        StringBuilder msg = new StringBuilder();
        if (string.contains("车辆类别")) {
            // excel 转换成 list
            List<VehicleTypeForm> list = importExcel.getDataList(VehicleTypeForm.class, null);
            String temp;
            for (int i = 0; i < list.size(); i++) {
                if ("REPEAT".equals(list.get(i).getVehicleType())) {
                    continue;
                }
                for (int j = list.size() - 1; j > i; j--) {
                    if (StringUtils.isNotBlank(list.get(j).getCategory()) && list.get(j).getCategory()
                            .equals(list.get(i).getCategory()) && list.get(j).getVehicleType()
                            .equals(list.get(i).getVehicleType())) {
                        temp = list.get(i).getVehicleType();
                        errorMsg += "第" + (i + 1) + "行跟第" + (j + 1) + "行重复，值是：" + temp + "<br/>";
                        list.get(j).setVehicleType("REPEAT");
                    }
                }
            }

            List<VehicleTypeForm> importList = new ArrayList<VehicleTypeForm>();
            // 校验需要导入的Device
            List<VehicleTypeDO> vehicleTypeList = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                VehicleTypeForm vehicleTypeForm = list.get(i);
                if ("REPEAT".equals(list.get(i).getVehicleType())) {
                    continue;
                }
                // 校验必填字段
                if (StringUtils.isBlank(vehicleTypeForm.getCategory())) {
                    resultMap.put("flag", 0);
                    errorMsg += "第" + (i + 1) + "条数据车辆类别字段未填<br/>";
                    continue;
                }
                if (StringUtils.isBlank(vehicleTypeForm.getVehicleType())) {
                    resultMap.put("flag", 0);
                    errorMsg += "第" + (i + 1) + "条数据车辆类型未填<br/>";
                    continue;
                }

                // 校验车辆类型
                if (vehicleTypeForm.getVehicleType() != null) {
                    String category = vehicleTypeForm.getCategory();// 车辆类别名称
                    String vehicleType = vehicleTypeForm.getVehicleType();// 车辆类型名称
                    if ("".equals(category)) { // 若车辆类别为空，默认为其他车辆
                        category = "其他车辆";
                    }
                    VehicleTypeDO typeInfo =
                        newVehicleTypeDao.findByVehicleTypeAndCategory(vehicleType, category);// 查询车辆类型与类别是否有绑定信息
                    VehicleCategoryDO checkCategory = newVehicleCategoryDao.findByCategory(category);// 查询车辆类别id
                    if (checkCategory == null) { // 若不存在该车辆类别
                        resultMap.put("flag", 0);
                        errorMsg += "第" + (i + 1) + "条数据车辆类别(" + (StringUtils.isNotBlank(category) ? category : "")
                                + ")不存在<br/>";
                        continue;
                    }
                    // 若该车辆类别与车辆类型没有绑定关系
                    if (typeInfo == null || typeInfo.getId() == null || "".equals(typeInfo.getId())) {
                        vehicleTypeForm.setVehicleCategory(checkCategory.getId());
                    } else {
                        resultMap.put("flag", 0);
                        errorMsg += "第" + (i + 1) + "条数据车辆类别类型(" + category + "," + vehicleType + ")已存在<br/>";
                        continue;
                    }
                }

                // 验证字段长度
                if (vehicleTypeForm.getVehicleType().length() > 20) {
                    resultMap.put("flag", 0);
                    errorMsg += "第" + (i + 1) + "条，车辆类型为“ " + vehicleTypeForm.getVehicleType() + " ”的长度大于20<br/>";
                    continue;
                }
                // 车辆类型名称特殊字符的判断
                if (StringUtils.isNotBlank(vehicleTypeForm.getVehicleType())) {
                    Pattern pattern = Pattern.compile("^[0-9a-zA-Z_\u4E00-\u9FA5]{0,20}$");
                    Matcher matcher = pattern.matcher(vehicleTypeForm.getVehicleType());
                    if (!matcher.matches()) {
                        resultMap.put("flag", 0);
                        errorMsg += "第" + (i + 1) + "条，车辆类型包含特殊符号,请输入中文、英文、数字和下划线<br/>";
                        continue;
                    }
                }
                Integer serviceCycle = vehicleTypeForm.getServiceCycle();
                if (serviceCycle != null) {
                    if (serviceCycle > BIGEST_SERVICE_CYCLE || serviceCycle <= LEAST_SERVICE_CYCLE) {
                        resultMap.put("flag", 0);
                        errorMsg += "第" + (i + 1) + "条，保养里程间隔(KM)“ "
                                + vehicleTypeForm.getServiceCycle() + " ”最大请输入五位正整数<br/>";
                        continue;
                    }

                }
                vehicleTypeForm.setCreateDataUsername(SystemHelper.getCurrentUsername());
                // 创建者
                vehicleTypeForm.setCreateDataTime(new Date()); // 创建时间
                vehicleTypeList.add(new VehicleTypeDO(vehicleTypeForm));
                importList.add(vehicleTypeForm);
                msg.append("导入车辆类型 : ").append(vehicleTypeForm.getVehicleType()).append(" <br/>");
            }
            // 组装导入结果
            if (importList.size() > 0) {
                // 导入逻辑（暂时只有新增，具体导入逻辑还需需求确定）
                int num = newVehicleTypeDao.addBatch(vehicleTypeList);
                if (num > 0) {
                    resultInfo += "导入成功" + importList.size() + "条数据,导入失败" + (list.size() - importList.size()) + "条数据。";
                    resultMap.put("flag", 1);
                    resultMap.put("errorMsg", errorMsg);
                    resultMap.put("resultInfo", resultInfo);
                    logSearchServiceImpl.addLog(ipAddress, msg.toString(), "3", "batch", "导入车辆类型");
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
        } else {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", errorMsg);
            resultMap.put("resultInfo", "车辆类型导入模板不正确！");
            return resultMap;
        }
        return resultMap;
    }

    @Override
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<String>();
        List<String> requiredList = new ArrayList<String>();
        List<Object> exportList = new ArrayList<Object>();
        // 表头
        headList.add("车辆类别");
        headList.add("车辆类型");
        headList.add("保养里程间隔(KM)");
        headList.add("备注");
        // 必填字段
        requiredList.add("车辆类别");
        requiredList.add("车辆类型");


        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<String, String[]>();
        // 车辆类别
        // String[] vehicleCategorys = {"小型货车","载客货车","危险品运输车辆","货运运输车辆","工程车辆","特种车辆","其他车辆"};
        List<String> list = newVehicleCategoryDao.getAll().stream().map(VehicleCategoryDO::getVehicleCategory)
            .collect(Collectors.toList());
        String[] vehicleCategorys = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            vehicleCategorys[i] = list.get(i);
        }
        selectMap.put("车辆类别", vehicleCategorys);
        // 默认设置一条数据
        if (StringUtils.isNotBlank(vehicleCategorys[0])) {
            exportList.add(vehicleCategorys[0]);
        } else {
            exportList.add("请先添加车辆类别");
        }
        exportList.add("类型");
        exportList.add(10000);
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
    public VehicleTypeDO findByVehicleType(String vehicleType) throws Exception {
        return newVehicleTypeDao.getByVehicleType(vehicleType);
    }

    @Override
    public VehicleTypeDO findByVehicleType(String id, String vehicleType) throws Exception {
        return newVehicleTypeDao.getByIdAndVehicleType(id, vehicleType);
    }

    @Override
    public String findByVehicleTypet(String id) throws Exception {
        VehicleTypeDTO vehicleTypeDTO = newVehicleTypeDao.getById(id);
        return vehicleTypeDTO == null ? null : vehicleTypeDTO.getType();
    }

    @Override
    public List<VehicleTypeDO> findVehicleType(String vehicleCategory) throws Exception {
        return newVehicleTypeDao.findByCategoryIds(Lists.newArrayList(vehicleCategory));
    }

    @Override
    public boolean getIsBand(String id) throws Exception {
        return newVehicleTypeDao.getIsBandVehicle(id);
    }

    @Override
    public VehicleTypeDO findVehicleTypeId(String category, String vehicleType) throws Exception {
        return newVehicleTypeDao.findByVehicleTypeAndCategory(vehicleType, category);
    }

    @Override
    public boolean checkTypeIsBindingSubType(String id) {
        return newVehicleTypeDao.checkTypeIsBindingSubType(id);
    }

    @Override
    public List<VehicleSubTypeForm> findTypeIsBindingSubType(String id) {
        return newVehicleTypeDao.findBySubType(id);
    }

    @Override
    public List<VehicleTypeDO> findVehicleTypes(Integer standardInt) {
        return newVehicleTypeDao.findByStandard(standardInt);
    }
}
