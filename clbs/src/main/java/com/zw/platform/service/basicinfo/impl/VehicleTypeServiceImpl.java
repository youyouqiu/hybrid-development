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
            String msg = "???????????????" + form.getVehicleType();
            logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
        }

    }

    @MethodLog(name = "???????????? User", description = "???????????? User")
    @Override
    public Page<VehicleTypeDO> findByPage(VehicleTypeQuery query) throws Exception {
        if (StringUtils.isNotBlank(query.getSimpleQueryParam())) {
            // ??????????????????
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
            // ????????????????????????????????????
            boolean isBindingSubType = this.checkTypeIsBindingSubType(item[i]);
            //???????????????????????????
            boolean isBindingVehicle = newVehicleTypeDao.getIsBandVehicle(item[i]);
            if (isBindingSubType) {
                result.append(vehicleType.getType()).append(",");
            }
            if (isBindingVehicle) {
                resultVehicle.append(vehicleType.getType()).append(",");
            }
            if (!isBindingSubType && !isBindingVehicle) {
                boolean flag = newVehicleTypeDao.delete(item[i]);
                if (flag) { // ????????????,????????????
                    msg.append("???????????????").append(vehicleType.getType()).append(" <br/>");
                }
            }
        }
        if (!msg.toString().isEmpty()) {
            if (item.length == 1) {
                logSearchServiceImpl.addLog(ipAddress, msg.toString(), "3", "", "-", "");
            } else {
                logSearchServiceImpl.addLog(ipAddress, msg.toString(), "3", "batch", "??????????????????");
            }
            if (StringUtils.isNotBlank(result.toString())) {
                buildMessage.append("????????????" + result.toString() + "??????????????????,???????????????????????????!").append("<br/>");
                buildFlag = true;
            }
            if (StringUtils.isNotBlank(resultVehicle.toString())) {
                buildMessage.append("????????????" + resultVehicle.toString() + "???????????????,???????????????????????????!");
                buildFlag = true;
            }
            if (buildFlag) {
                return new JsonResultBean(JsonResultBean.FAULT, buildMessage.toString());
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else if (StringUtils.isNotBlank(result.toString()) || StringUtils.isNotBlank(resultVehicle.toString())) {
            if (StringUtils.isNotBlank(result.toString())) {
                buildMessage.append("????????????" + result.toString() + "??????????????????,???????????????????????????!").append("<br/>");
                buildFlag = true;
            }
            if (StringUtils.isNotBlank(resultVehicle.toString())) {
                buildMessage.append("????????????" + resultVehicle.toString() + "???????????????,???????????????????????????!");
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
            msg = "???????????????" + form.getVehicleType();
        } else {
            msg = "???????????????" + beforeVehicleType + " ????????????" + form.getVehicleType();
        }
        logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
        boolean flag = newVehicleTypeDao.update(form.convertTypeDo());
        if (flag) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @MethodLog(name = "??????", description = "??????")
    @Override
    public boolean exportVehicleType(String title, int type, HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(title, VehicleTypeDTO.class, 1, null);
        List<VehicleTypeDTO> exportList = newVehicleTypeDao.getByKeyword(null);
        export.setDataList(exportList);
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// ????????????????????????????????????
        out.close();
        return true;
    }

    @MethodLog(name = "????????????", description = "????????????")
    public Map importVehicleType(MultipartFile multipartFile, String ipAddress) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("flag", 0);
        String errorMsg = "";
        String resultInfo = "";
        // ???????????????
        ImportExcel importExcel = new ImportExcel(multipartFile, 1, 0);
        Row row = importExcel.getRow(0);
        String string = importExcel.getCellValue(row, 0).toString();
        StringBuilder msg = new StringBuilder();
        if (string.contains("????????????")) {
            // excel ????????? list
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
                        errorMsg += "???" + (i + 1) + "?????????" + (j + 1) + "?????????????????????" + temp + "<br/>";
                        list.get(j).setVehicleType("REPEAT");
                    }
                }
            }

            List<VehicleTypeForm> importList = new ArrayList<VehicleTypeForm>();
            // ?????????????????????Device
            List<VehicleTypeDO> vehicleTypeList = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                VehicleTypeForm vehicleTypeForm = list.get(i);
                if ("REPEAT".equals(list.get(i).getVehicleType())) {
                    continue;
                }
                // ??????????????????
                if (StringUtils.isBlank(vehicleTypeForm.getCategory())) {
                    resultMap.put("flag", 0);
                    errorMsg += "???" + (i + 1) + "?????????????????????????????????<br/>";
                    continue;
                }
                if (StringUtils.isBlank(vehicleTypeForm.getVehicleType())) {
                    resultMap.put("flag", 0);
                    errorMsg += "???" + (i + 1) + "???????????????????????????<br/>";
                    continue;
                }

                // ??????????????????
                if (vehicleTypeForm.getVehicleType() != null) {
                    String category = vehicleTypeForm.getCategory();// ??????????????????
                    String vehicleType = vehicleTypeForm.getVehicleType();// ??????????????????
                    if ("".equals(category)) { // ?????????????????????????????????????????????
                        category = "????????????";
                    }
                    VehicleTypeDO typeInfo =
                        newVehicleTypeDao.findByVehicleTypeAndCategory(vehicleType, category);// ????????????????????????????????????????????????
                    VehicleCategoryDO checkCategory = newVehicleCategoryDao.findByCategory(category);// ??????????????????id
                    if (checkCategory == null) { // ???????????????????????????
                        resultMap.put("flag", 0);
                        errorMsg += "???" + (i + 1) + "?????????????????????(" + (StringUtils.isNotBlank(category) ? category : "")
                                + ")?????????<br/>";
                        continue;
                    }
                    // ???????????????????????????????????????????????????
                    if (typeInfo == null || typeInfo.getId() == null || "".equals(typeInfo.getId())) {
                        vehicleTypeForm.setVehicleCategory(checkCategory.getId());
                    } else {
                        resultMap.put("flag", 0);
                        errorMsg += "???" + (i + 1) + "???????????????????????????(" + category + "," + vehicleType + ")?????????<br/>";
                        continue;
                    }
                }

                // ??????????????????
                if (vehicleTypeForm.getVehicleType().length() > 20) {
                    resultMap.put("flag", 0);
                    errorMsg += "???" + (i + 1) + "???????????????????????? " + vehicleTypeForm.getVehicleType() + " ??????????????????20<br/>";
                    continue;
                }
                // ???????????????????????????????????????
                if (StringUtils.isNotBlank(vehicleTypeForm.getVehicleType())) {
                    Pattern pattern = Pattern.compile("^[0-9a-zA-Z_\u4E00-\u9FA5]{0,20}$");
                    Matcher matcher = pattern.matcher(vehicleTypeForm.getVehicleType());
                    if (!matcher.matches()) {
                        resultMap.put("flag", 0);
                        errorMsg += "???" + (i + 1) + "????????????????????????????????????,?????????????????????????????????????????????<br/>";
                        continue;
                    }
                }
                Integer serviceCycle = vehicleTypeForm.getServiceCycle();
                if (serviceCycle != null) {
                    if (serviceCycle > BIGEST_SERVICE_CYCLE || serviceCycle <= LEAST_SERVICE_CYCLE) {
                        resultMap.put("flag", 0);
                        errorMsg += "???" + (i + 1) + "????????????????????????(KM)??? "
                                + vehicleTypeForm.getServiceCycle() + " ?????????????????????????????????<br/>";
                        continue;
                    }

                }
                vehicleTypeForm.setCreateDataUsername(SystemHelper.getCurrentUsername());
                // ?????????
                vehicleTypeForm.setCreateDataTime(new Date()); // ????????????
                vehicleTypeList.add(new VehicleTypeDO(vehicleTypeForm));
                importList.add(vehicleTypeForm);
                msg.append("?????????????????? : ").append(vehicleTypeForm.getVehicleType()).append(" <br/>");
            }
            // ??????????????????
            if (importList.size() > 0) {
                // ???????????????????????????????????????????????????????????????????????????
                int num = newVehicleTypeDao.addBatch(vehicleTypeList);
                if (num > 0) {
                    resultInfo += "????????????" + importList.size() + "?????????,????????????" + (list.size() - importList.size()) + "????????????";
                    resultMap.put("flag", 1);
                    resultMap.put("errorMsg", errorMsg);
                    resultMap.put("resultInfo", resultInfo);
                    logSearchServiceImpl.addLog(ipAddress, msg.toString(), "3", "batch", "??????????????????");
                } else {
                    resultMap.put("flag", 0);
                    resultMap.put("resultInfo", "???????????????");
                    return resultMap;
                }

            } else {
                resultMap.put("flag", 0);
                resultMap.put("errorMsg", errorMsg);
                resultMap.put("resultInfo", "????????????0????????????");
                return resultMap;
            }
        } else {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", errorMsg);
            resultMap.put("resultInfo", "????????????????????????????????????");
            return resultMap;
        }
        return resultMap;
    }

    @Override
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<String>();
        List<String> requiredList = new ArrayList<String>();
        List<Object> exportList = new ArrayList<Object>();
        // ??????
        headList.add("????????????");
        headList.add("????????????");
        headList.add("??????????????????(KM)");
        headList.add("??????");
        // ????????????
        requiredList.add("????????????");
        requiredList.add("????????????");


        // ?????????????????????map
        Map<String, String[]> selectMap = new HashMap<String, String[]>();
        // ????????????
        // String[] vehicleCategorys = {"????????????","????????????","?????????????????????","??????????????????","????????????","????????????","????????????"};
        List<String> list = newVehicleCategoryDao.getAll().stream().map(VehicleCategoryDO::getVehicleCategory)
            .collect(Collectors.toList());
        String[] vehicleCategorys = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            vehicleCategorys[i] = list.get(i);
        }
        selectMap.put("????????????", vehicleCategorys);
        // ????????????????????????
        if (StringUtils.isNotBlank(vehicleCategorys[0])) {
            exportList.add(vehicleCategorys[0]);
        } else {
            exportList.add("????????????????????????");
        }
        exportList.add("??????");
        exportList.add(10000);
        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }

        // ???????????????
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// ????????????????????????????????????
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
