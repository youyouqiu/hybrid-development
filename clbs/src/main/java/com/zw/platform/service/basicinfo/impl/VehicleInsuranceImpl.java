package com.zw.platform.service.basicinfo.impl;

import com.github.pagehelper.Page;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.event.VehicleDeleteEvent;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.VehicleInsuranceInfo;
import com.zw.platform.domain.basicinfo.form.VehicleInsuranceForm;
import com.zw.platform.domain.basicinfo.query.VehicleInsuranceQuery;
import com.zw.platform.repository.modules.VehicleInsuranceDao;
import com.zw.platform.service.basicinfo.VehicleInsuranceService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportExcel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import static com.zw.platform.basic.constant.HistoryRedisKeyEnum.EXPIRE_INSURANCE_ID;

/**
 * 车辆保险impl
 * @author zhouzongbo on 2018/5/10 9:27
 */
@Service
public class VehicleInsuranceImpl implements VehicleInsuranceService {

    private static final Logger log = LogManager.getLogger(VehicleInsuranceImpl.class);

    private static final String INSURANCE_ID_REGEX = "^[0-9a-zA-Z]{1,30}$";

    /**
     * 0-100正则
     */
    private static final String ZONE_TO_ONE_HUNDRED_REGEX = "^(((\\d|[1-9]\\d)(\\.\\d{1,2})?)|100|100.0|100.00)$";

    /**
     * double(9,1)
     */
    private static final String DOUBLE_REGEX = "^(?:0\\.[1-9]|[1-9][0-9]{0,8}|[1-9][0-9]{0,6}\\.[0-9])$";

    @Autowired
    private VehicleInsuranceDao vehicleInsuranceDao;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private UserService userService;

    @Override
    public Page<VehicleInsuranceInfo> findVehicleInsuranceList(VehicleInsuranceQuery vehicleInsuranceQuery) {
        //用户id
        String userUUID = userService.getCurrentUserUuid();
        List<String> groupIdList = userService.getCurrentUserOrgIds();
        vehicleInsuranceQuery.setUserUUID(userUUID);
        vehicleInsuranceQuery.setGroupList(groupIdList);
        vehicleInsuranceQuery
            .setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(vehicleInsuranceQuery.getSimpleQueryParam()));
        // insuranceTipType 1:即将到期
        if (vehicleInsuranceQuery.getInsuranceTipType() == 1) {
            // 当前用户下的已过期的保险单号
            vehicleInsuranceQuery.setInsuranceList(RedisHelper.getList(EXPIRE_INSURANCE_ID.of()));
            if (CollectionUtils.isEmpty(vehicleInsuranceQuery.getInsuranceList())) {
                return new Page<>();
            }
        }

        return PageHelperUtil
            .doSelect(vehicleInsuranceQuery, () -> vehicleInsuranceDao.findVehicleInsuranceList(vehicleInsuranceQuery));
    }

    @Override
    public boolean add(VehicleInsuranceForm vehicleInsuranceForm, String ip) throws Exception {
        vehicleInsuranceForm.setCreateDataUsername(SystemHelper.getCurrentUsername());
        String insuranceId = vehicleInsuranceForm.getInsuranceId();
        vehicleInsuranceForm.setInsuranceId(insuranceId.toUpperCase());
        boolean flag = vehicleInsuranceDao.addVehicleInsurance(vehicleInsuranceForm);
        if (flag) {
            String msg = "新增保险单号: " + insuranceId;
            logSearchService.addLog(ip, msg, "3", "", "-", "");
        }
        return flag;
    }

    @Override
    public VehicleInsuranceInfo getVehicleInsuranceById(String id) {
        return vehicleInsuranceDao.getVehicleInsuranceById(id);
    }

    @Override
    public boolean updateVehicleInsurance(VehicleInsuranceForm vehicleInsuranceForm, String ip) throws Exception {
        vehicleInsuranceForm.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        String insuranceId = vehicleInsuranceForm.getInsuranceId();
        vehicleInsuranceForm.setInsuranceId(insuranceId.toUpperCase());
        boolean flag = vehicleInsuranceDao.updateVehicleInsurance(vehicleInsuranceForm);
        if (flag) {
            String msg = "修改保险单号: " + insuranceId;
            logSearchService.addLog(ip, msg, "3", "", "-", "");
        }
        return flag;
    }

    @Override
    public VehicleInsuranceInfo getVehicleInsuranceByInsuranceId(String insuranceId) {
        return vehicleInsuranceDao.getVehicleInsuranceByInsuranceId(insuranceId);
    }

    @Override
    public boolean delete(String id, String ip) throws Exception {
        VehicleInsuranceInfo vehicleInsuranceInfo = vehicleInsuranceDao.getVehicleInsuranceById(id);
        if (Objects.isNull(vehicleInsuranceInfo)) {
            return false;
        }
        boolean flag = vehicleInsuranceDao.delete(id);
        if (flag) {
            String msg = "删除保险号：" + vehicleInsuranceInfo.getInsuranceId();
            logSearchService.addLog(ip, msg, "3", "", "-", "");
        }
        return flag;
    }

    @Override
    public JsonResultBean deleteMore(String ids, String ip) throws Exception {
        String[] insuranceIds = ids.split(",");
        boolean resultFlag = false;
        for (String id : insuranceIds) {
            VehicleInsuranceInfo vehicleInsuranceInfo = vehicleInsuranceDao.getVehicleInsuranceById(id);
            if (Objects.isNull(vehicleInsuranceInfo)) {
                continue;
            }
            boolean flag = vehicleInsuranceDao.delete(id);
            if (flag && !resultFlag) {
                resultFlag = true;
            }
        }

        if (resultFlag) {
            logSearchService.addLog(ip, "批量删除保险单号", "3", "", "", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public void getExport(HttpServletResponse response, String simpleQueryParam, Integer insuranceTipType) {
        ExportExcel exportExcel = new ExportExcel(null, VehicleInsuranceForm.class, 1, null);
        List<VehicleInsuranceForm> exportList = new ArrayList<>();
        VehicleInsuranceQuery query = new VehicleInsuranceQuery();
        query.setPage(0L);
        query.setLimit(0L);
        query.setSimpleQueryParam(simpleQueryParam);
        query.setInsuranceTipType(insuranceTipType);
        List<VehicleInsuranceInfo> vehicleInsuranceList = this.findVehicleInsuranceList(query);
        for (VehicleInsuranceInfo vehicleInsuranceInfo : vehicleInsuranceList) {
            VehicleInsuranceForm vehicleInsuranceForm = new VehicleInsuranceForm();
            BeanUtils.copyProperties(vehicleInsuranceInfo, vehicleInsuranceForm);
            vehicleInsuranceForm.setActualCostStr(vehicleInsuranceInfo.getActualCost());
            String discount = vehicleInsuranceInfo.getDiscount();
            if (StringUtils.isNotBlank(discount)) {
                vehicleInsuranceForm.setDiscount(Double.valueOf(discount));
            }
            exportList.add(vehicleInsuranceForm);
        }

        exportExcel.setDataList(exportList);
        try (OutputStream outputStream = response.getOutputStream()) {
            exportExcel.write(outputStream);
        } catch (Exception e) {
            log.error("导出车辆保险数据失败", e);
        }
    }

    @Override
    public void buildTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // 表头
        headList.add("保险单号");
        headList.add("车牌号");
        headList.add("保险类型");
        headList.add("保险公司");
        headList.add("保险开始时间");
        headList.add("保险到期时间");
        headList.add("提前提醒天数");
        headList.add("保险金额");
        headList.add("折扣率(%)");
        headList.add("实际费用");
        headList.add("代理人");
        headList.add("电话");
        headList.add("备注");
        // 必填字段
        requiredList.add("保险单号");
        requiredList.add("车牌号");
        // 设置默认数据
        exportList.add("123456789");
        // 车辆车牌
        List<Map<String, Object>> list = vehicleService.getUbBindSelectList();
        if (CollectionUtils.isNotEmpty(list)) {
            exportList.add(list.get(0).get("brand"));
        } else {
            exportList.add("请先添加车牌号!");
        }
        exportList.add("保险类型");
        exportList.add("平安");
        exportList.add(Converter.toString(new Date(), "yyyy-MM-dd"));
        exportList.add(Converter.toString(new Date(), "yyyy-MM-dd"));
        exportList.add("5");
        exportList.add("100");
        exportList.add("99.99");
        exportList.add("20000");
        exportList.add("中位");
        exportList.add("13411111111");
        exportList.add("备注");

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<>();
        String[] vehicleBrands = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> map = list.get(i);
            vehicleBrands[i] = (String) map.get("brand");
        }
        if (vehicleBrands.length > 0) {
            selectMap.put("车牌号", vehicleBrands);
        }

        ExportExcel exportExcel = new ExportExcel(headList, requiredList, selectMap);
        Row rows = exportExcel.addRow();
        for (int i = 0; i < exportList.size(); i++) {
            exportExcel.addCell(rows, i, exportList.get(i));
        }

        try (OutputStream outputStream = response.getOutputStream()) {
            exportExcel.write(outputStream);
        } catch (Exception e) {
            log.error("下载车辆保险模板失败", e);
        }
    }

    @Override
    public Map<String, Object> importVehicleInsurance(MultipartFile file, String ipAddress) throws Exception {
        Map<String, Object> resultMap = new HashMap<>(16);
        //0:导入失败或者未导入数据，1:导入成功
        resultMap.put("flag", 0);
        ImportExcel importExcel = new ImportExcel(file, 1, 0);
        // 获取表头数量
        short lastCellNum = importExcel.getRow(0).getLastCellNum();
        // 错误信息
        StringBuilder errorMsgBuilder = new StringBuilder();
        // 返回消息
        StringBuilder message = new StringBuilder();
        // 保险单号重复验证
        Set<String> checkVehicleInsurance = new HashSet<>();
        List<VehicleInsuranceForm> dataList;
        if (lastCellNum == 13) {
            dataList = importExcel.getDataList(VehicleInsuranceForm.class, null);
            if (CollectionUtils.isNotEmpty(dataList)) {
                List<VehicleInsuranceForm> importList = new ArrayList<>();
                for (int i = 0; i < dataList.size(); i++) {
                    VehicleInsuranceForm form = new VehicleInsuranceForm();
                    VehicleInsuranceForm vehicleInsuranceForm = dataList.get(i);
                    String insuranceId = vehicleInsuranceForm.getInsuranceId();
                    String brand = vehicleInsuranceForm.getBrand();
                    // 必填字段车牌号和保险单号校验
                    if (StringUtils.isEmpty(insuranceId)) {
                        errorMsgBuilder.append("第").append(i + 1).append("条必填字段【保险单号】未填<br/>");
                        continue;
                    }
                    if (StringUtils.isEmpty(brand)) {
                        errorMsgBuilder.append("第").append(i + 1).append("条必填字段【车牌号】未填<br/>");
                        continue;
                    }
                    // 重复验证保险单号
                    VehicleInsuranceInfo vehicleInsurance =
                        vehicleInsuranceDao.getVehicleInsuranceByInsuranceId(insuranceId);
                    if (checkVehicleInsurance.contains(insuranceId) || Objects.nonNull(vehicleInsurance)) {
                        errorMsgBuilder.append("第").append(i + 1).append("条保险单号: ").append(insuranceId)
                            .append("重复<br/>");
                        continue;
                    } else {
                        checkVehicleInsurance.add(insuranceId);
                    }
                    if (!Pattern.matches(INSURANCE_ID_REGEX, insuranceId)) {
                        errorMsgBuilder.append("第").append(i + 1).append("条字段【保险单号】输入类型为正整数和字母,长度范围为1~30位");
                        continue;
                    }
                    // 输入类型为正整数和字母（小写转大写）
                    form.setInsuranceId(insuranceId.toUpperCase());
                    // 根据车牌号查询车辆id
                    VehicleDTO vehicleInfo = vehicleService.getByName(brand);
                    if (Objects.isNull(vehicleInfo)) {
                        errorMsgBuilder.append("第").append(i + 1).append("条字段【车牌号】不存在");
                        continue;
                    } else {
                        form.setVehicleId(vehicleInfo.getId());
                    }
                    // 非必填字段校验
                    String insuranceType = vehicleInsuranceForm.getInsuranceType();
                    if (Converter.toBlank(insuranceType).length() < 1
                        || Converter.toBlank(insuranceType).length() > 50) {
                        form.setInsuranceType("");
                    } else {
                        form.setInsuranceType(insuranceType);
                    }
                    String company = vehicleInsuranceForm.getCompany();
                    if (Converter.toBlank(company).length() < 1 || Converter.toBlank(company).length() > 50) {
                        form.setCompany("");
                    } else {
                        form.setCompany(company);
                    }
                    Date startTime = Converter.toDate(vehicleInsuranceForm.getStartTimeStr(), "yyyy-MM-dd");
                    Date endTime = Converter.toDate(vehicleInsuranceForm.getEndTimeStr(), "yyyy-MM-dd");
                    if (Objects.nonNull(startTime) && Objects.nonNull(endTime) && startTime.after(endTime)) {
                        errorMsgBuilder.append("第").append(i + 1).append("行,保险开始时间不能大于保险到期时间");
                        continue;
                    }
                    form.setStartTime(startTime);
                    form.setEndTime(endTime);
                    Short preAlert = vehicleInsuranceForm.getPreAlert();
                    if (Objects.nonNull(preAlert) && preAlert.intValue() >= 1 && preAlert.intValue() <= 60) {
                        form.setPreAlert(preAlert);
                    } else {
                        form.setPreAlert(null);
                    }
                    // 保险金额 1-9位
                    Integer amountInsured = vehicleInsuranceForm.getAmountInsured();
                    if (Pattern.matches("^[1-9]\\d{1,8}$", Converter.toBlank(amountInsured))) {
                        form.setAmountInsured(amountInsured);
                    } else {
                        form.setAmountInsured(null);
                    }
                    // 折扣率(%) 正数,输入值范围0-100
                    Double discount = vehicleInsuranceForm.getDiscount();
                    if (Pattern.matches(ZONE_TO_ONE_HUNDRED_REGEX, Converter.toBlank(discount))) {
                        form.setDiscount(discount);
                    } else {
                        form.setDiscount(null);
                    }
                    // 实际费用
                    String actualCostStr = vehicleInsuranceForm.getActualCostStr();
                    if (Pattern.matches(DOUBLE_REGEX, Converter.toBlank(actualCostStr))) {
                        form.setActualCost(Double.valueOf(actualCostStr));
                    } else {
                        form.setActualCost(null);
                    }
                    String agent = vehicleInsuranceForm.getAgent();
                    if (Converter.toBlank(agent).length() >= 1 && Converter.toBlank(agent).length() <= 10) {
                        form.setAgent(agent);
                    } else {
                        form.setAgent("");
                    }
                    // 电话
                    String phone = vehicleInsuranceForm.getPhone();
                    if (Pattern.matches("^\\d{7,13}$", Converter.toBlank(phone))) {
                        form.setPhone(phone);
                    } else {
                        form.setPhone("");
                    }
                    // 备注
                    String remark = vehicleInsuranceForm.getRemark();
                    if (Converter.toBlank(remark).length() >= 1 && Converter.toBlank(remark).length() <= 50) {
                        form.setRemark(remark);
                    } else {
                        form.setRemark("");
                    }
                    form.setCreateDataUsername(SystemHelper.getCurrentUsername());
                    importList.add(form);
                    message.append("导入车辆保险 : ").append(vehicleInsuranceForm.getInsuranceId()).append(" <br/>");
                }
                if (CollectionUtils.isNotEmpty(importList)) {
                    boolean flag = vehicleInsuranceDao.addBatchVehicleInsurance(importList);
                    if (flag) {
                        logSearchService.addLog(ipAddress, "导入保险单号", "3", "", "", "");
                        String resultInfo =
                            "导入成功" + importList.size() + "条数据，导入失败: " + (dataList.size() - importList.size()) + "条数据！";
                        resultMap.put("flag", 1);
                        resultMap.put("errorMsg", errorMsgBuilder.toString());
                        resultMap.put("resultInfo", resultInfo);
                        return resultMap;
                    }
                }
                resultMap.put("errorMsg", errorMsgBuilder.toString());
                resultMap.put("resultInfo", "导入成功0条数据！");
                return resultMap;
            } else {
                resultMap.put("errorMsg", errorMsgBuilder.toString());
                resultMap.put("resultInfo", "导入成功0条数据！");
                return resultMap;
            }
        } else {
            resultMap.put("errorMsg", errorMsgBuilder.toString());
            resultMap.put("resultInfo", "车辆保险导入模板不正确！");
            return resultMap;
        }
    }

    @Override
    public boolean deleteByVehicleIds(String vehicleId, String ipAddress) throws Exception {
        if (StringUtils.isNotBlank(vehicleId)) {
            String[] vehicleIds = vehicleId.split(",");
            boolean flag = vehicleInsuranceDao.deleteByVehicleIds(vehicleIds);
            if (flag) {
                logSearchService.addLog(ipAddress, "删除车辆下的保险信息", "3", "", "");
            }
            return flag;
        }
        return false;
    }

    @Override
    public boolean deleteByVehicleId(String ipAddress, String vehicleId, String brand) throws Exception {
        if (StringUtils.isNotBlank(vehicleId)) {
            /*String bindingInsurance = vehicleInsuranceDao.findBindingInsuranceByVehicleId(vehicleId);
            boolean flag = vehicleInsuranceDao.deleteByVehicleId(vehicleId);
            if(flag && StringUtils.isNotBlank(bindingInsurance)) {
                String message = "删除车辆: " + brand +" 下的保险单号信息: " + bindingInsurance;
                logSearchService.addLog(ipAddress,message,"3","","");
            }*/
            return vehicleInsuranceDao.deleteByVehicleId(vehicleId);
        }
        return false;
    }

    @EventListener
    public void listenVehicleDeleteEvent(VehicleDeleteEvent event) {
        try {
            deleteByVehicleIds(StringUtils.join(event.getIds(), ","), event.getIpAddress());
        } catch (Exception e) {
            log.error("删除车辆保险数据失败", e);
        }
    }

    @Override
    public List<Map<String, String>> findExpireVehicleInsurance() {
        return vehicleInsuranceDao.findExpireVehicleInsuranceVehIds();
    }

    @Override
    public List<Map<String, Object>> findVehicleMapSelect() {
        return getGroupVehicle();
    }

    /**
     * 获取用户权限的车，以及组织和下级组织的车
     * @return list
     */
    private List<Map<String, Object>> getGroupVehicle() {
        List<Map<String, Object>> listRedis = new ArrayList<>();
        List<String> vehicles = vehicleService.getUserOwnIds(null, null);
        if (CollectionUtils.isNotEmpty(vehicles)) {
            Map<String, VehicleDTO> vehicleMap = MonitorUtils.getVehicleMap(vehicles);
            for (Map.Entry<String, VehicleDTO> entry : vehicleMap.entrySet()) {
                if (Objects.nonNull(entry.getValue())) {
                    VehicleDTO vehicleDTO = entry.getValue();
                    // 重新组装车辆的key-value(brand-id)
                    Map<String, Object> vhMap = new HashMap<>(16);
                    vhMap.put("brand", vehicleDTO.getName());
                    vhMap.put("id", vehicleDTO.getId());
                    listRedis.add(vhMap);
                }
            }
        }
        return listRedis;
    }
}
