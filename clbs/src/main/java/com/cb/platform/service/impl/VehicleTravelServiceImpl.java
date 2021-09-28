package com.cb.platform.service.impl;

import com.cb.platform.domain.VehicleTravelForm;
import com.cb.platform.domain.VehicleTravelQuery;
import com.cb.platform.repository.mysqlDao.VehicleTravelDao;
import com.cb.platform.service.VehicleTravelService;
import com.cb.platform.util.OPERATE;
import com.github.pagehelper.PageHelper;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.MonitorBaseDTO;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MapUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.excel.ImportExcel;
import com.zw.platform.util.excel.validator.ImportValidator;
import com.zw.platform.util.excel.validator.VehicleTravelValidator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VehicleTravelServiceImpl implements VehicleTravelService {
    private static final Logger logger = LogManager.getLogger(VehicleTravelService.class);

    @Autowired
    private VehicleTravelDao vehicleTravelDao;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private LogSearchService logSearchService;

    @Override
    public void addVehicleTravel(VehicleTravelForm vehicleTravelForm) {
        vehicleTravelForm.setTravelId(vehicleTravelForm.getTravelId().toUpperCase());
        vehicleTravelDao.addVehicleTravel(vehicleTravelForm);
        addLog(vehicleTravelForm.getTravelId(), OPERATE.ADD);
    }

    @Override
    public void deleteVehicleTravelById(String id) {
        vehicleTravelDao.deleteVehicleTravelById(id);
        addLog(vehicleTravelDao.findLogTravelIdsByIds(Collections.singletonList(id)), OPERATE.DELETE);
    }

    @Override
    public void updateVehicleTravel(VehicleTravelForm form) {
        form.setTravelId(form.getTravelId().toUpperCase());
        VehicleTravelForm saveForm = vehicleTravelDao.findVehicleTravelById(form.getId());
        BeanUtils.copyProperties(form, saveForm);
        vehicleTravelDao.updateVehicleTravel(saveForm);
        addLog(form.getTravelId(), OPERATE.UPDATE);
    }

    @Override
    public VehicleTravelForm findVehicleTravelById(String id) {
        VehicleTravelForm vehicleTravelForm = vehicleTravelDao.findVehicleTravelById(id);
        Map<String, String> brandMap = getVehInfoMaps(Collections.singletonList(vehicleTravelForm.getVehicleId()));
        vehicleTravelForm.setBrand(brandMap.get(vehicleTravelForm.getVehicleId()));
        return vehicleTravelForm;
    }

    @Override
    public List<VehicleTravelForm> searchVehicleTravels(VehicleTravelQuery query, boolean doPage) {
        transformQueryParam(query);
        List<VehicleTravelForm> vehicleTravels = doPage
                ? PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                .doSelectPage(() -> vehicleTravelDao.searchVehicleTravels(query))
                : vehicleTravelDao.searchVehicleTravels(query);
        List<String> resultVehicleId = getResultVehicleId(vehicleTravels);
        Map<String, String> brandMap = getVehInfoMaps(resultVehicleId);
        setBrand(vehicleTravels, brandMap);
        return vehicleTravels;
    }

    private List<String> getResultVehicleId(List<VehicleTravelForm> vehicleTravels) {
        List<String> vehicleIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(vehicleTravels)) {
            for (VehicleTravelForm form : vehicleTravels) {
                vehicleIds.add(form.getVehicleId());

            }
        }
        return vehicleIds;

    }

    @Override
    public boolean isRepeateTravelId(String id, String travelId) {
        boolean isRepeate = false;
        List<String> travelIds = vehicleTravelDao.isRepeateTravelId(id, travelId);
        if (CollectionUtils.isNotEmpty(travelIds)) {
            isRepeate = true;
        }
        return isRepeate;
    }

    @Override
    public boolean export(String title, int type, HttpServletResponse res, List<VehicleTravelForm> vehicleTravelForms)
        throws IOException {
        addLog(null, OPERATE.EXPORT);
        return ExportExcelUtil.export(
            new ExportExcelParam(title, type, vehicleTravelForms, VehicleTravelForm.class, null,
                res.getOutputStream()));
    }

    private String getTravelIds(List<VehicleTravelForm> vehicleTravelForms) {
        StringBuffer travelIds = new StringBuffer();
        if (CollectionUtils.isNotEmpty(vehicleTravelForms)) {
            for (VehicleTravelForm vtf : vehicleTravelForms) {
                travelIds.append(vtf.getTravelId());
                travelIds.append(",");
            }

        }
        return travelIds.substring(0, travelIds.length() - 1);
    }

    @Override
    public JsonResultBean importVehicleTravel(MultipartFile file, String ipAddress) {
        JsonResultBean resultBean;
        try {
            //组装获取权限下车辆信息MAP brand - id
            Map<String, String> vehicleNameAndIdMap = getBusNameAndIdMap();
            ImportExcel importExcel = getImportExcel(file, vehicleNameAndIdMap);
            List<VehicleTravelForm> list = importExcel.getDataList(VehicleTravelForm.class);
            resultBean = importExcel.getImportValidator().validate(list, false, null);
            if (resultBean.isSuccess()) {
                transformStoreData(list, vehicleNameAndIdMap);
                resultBean = storeImportData(list);
            }
        } catch (Exception e) {
            logger.error("导入旅游客车行程失败");
            resultBean = new JsonResultBean(JsonResultBean.FAULT);
        }
        return resultBean;

    }

    private Map<String, String> getBusNameAndIdMap() {
        final List<MonitorBaseDTO> buses = vehicleService.getByCategoryName("客车");
        return buses.stream().collect(Collectors.toMap(MonitorBaseDTO::getName, MonitorBaseDTO::getId, (o, p) -> o));
    }

    private void transformStoreData(List<VehicleTravelForm> list, Map<String, String> busesMap) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (VehicleTravelForm form : list) {
                form.setTravelId(form.getTravelId().toUpperCase());
                form.setVehicleId(busesMap.get(form.getBrand()));
            }
        }
    }

    @Override
    public void generateTemplateType(OutputStream out)
        throws IOException {
        List<String> headList = initExcelHead();
        List<String> requiredList = initrequireHead();
        Map<String, String[]> selectMap = initSelectMap();
        List<Object> exportList = initExcelData(selectMap);
        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        addExcelData(exportList, export);
        export.write(out);
        out.close();

    }

    private Map<String, String[]> initSelectMap() {
        Map<String, String[]> selectMap = new HashMap<>(16);
        Map<String, String> busesMap = getBusNameAndIdMap();
        String[] busesArr = new String[busesMap.size()];
        int i = 0;
        for (String brand : busesMap.keySet()) {
            busesArr[i++] = brand;
        }
        selectMap.put("车牌号(必填)", busesArr);
        return selectMap;
    }

    @Override
    public void deleteVehicleTravelByIds(String ids) {
        vehicleTravelDao.deleteVehicleTravelByIds(ids);
        addLog(null, OPERATE.DELETEBATCH);
    }

    @Override
    public void deleteVehicleTravelByVehicleIds(List<String> vehicleIds, boolean deleteSuccess) {
        if (deleteSuccess) {
            vehicleTravelDao.deleteVehicleTravelByVehicleIds(vehicleIds);
        }
    }

    private void addExcelData(List<Object> exportList, ExportExcel export) {
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
    }

    private List<Object> initExcelData(Map<String, String[]> selectMap) {
        List<Object> exportList = new ArrayList<Object>();
        String[] busesArr = selectMap.get("车牌号(必填)");
        exportList.add("2018051688");
        exportList.add(busesArr[0]);
        exportList.add(DateUtil.getDateToString(new Date(), null));
        exportList.add(DateUtil.getDateToString(new Date(), null));
        exportList.add("重庆大坪石油路");
        exportList.add("这里是行程内容");
        exportList.add("这里是备注信息");
        return exportList;
    }

    private List<String> initrequireHead() {
        List<String> requiredList = new ArrayList<String>();
        requiredList.add("行程单号(必填)");
        requiredList.add("车牌号(必填)");
        return requiredList;
    }

    private List<String> initExcelHead() {
        List<String> headList = new ArrayList<String>();
        headList.add("行程单号(必填)");
        headList.add("车牌号(必填)");
        headList.add("开始时间(yyyy-MM-dd HH:mm:ss)");
        headList.add("结束时间(yyyy-MM-dd HH:mm:ss)");
        headList.add("地点");
        headList.add("行程内容");
        headList.add("备注");
        return headList;
    }

    private JsonResultBean storeImportData(List<VehicleTravelForm> list) {
        vehicleTravelDao.addVehicleTravelByBatch(list);
        String importMessage = String.format("车辆旅程管理模块：成功导入%d条数据", list.size());
        addLog(null, OPERATE.IMPORT);
        return new JsonResultBean(JsonResultBean.SUCCESS, importMessage);
    }

    private ImportExcel getImportExcel(MultipartFile file, Map<String, String> busesMap)
        throws IOException {
        ImportExcel importExcel = new ImportExcel(file, 1, 0, DateUtil.DATE_FORMAT_SHORT);
        ImportValidator<VehicleTravelForm> validator = new VehicleTravelValidator(busesMap,
            vehicleTravelDao);
        importExcel.setImportValidator(validator);
        return importExcel;
    }

    private void transformQueryParam(VehicleTravelQuery query) {
        List<String> queryVehicleIds = vehicleService.getUserOwnIds(query.getBrand(), null);
        query.setVehicleIdList(queryVehicleIds);
    }



    private void setBrand(List<VehicleTravelForm> vehicleTravels, Map<String, String> brandMap) {
        if (CollectionUtils.isNotEmpty(vehicleTravels)) {
            for (VehicleTravelForm vtf : vehicleTravels) {
                vtf.setBrand(brandMap.get(vtf.getVehicleId()));
            }
        }
    }

    private Map<String, String> getVehInfoMaps(List<String> vehicleIds) {
        Map<String, String> brandMap = new HashMap<>(vehicleIds.size());
        if (CollectionUtils.isNotEmpty(vehicleIds)) {
            List<RedisKey> monitorInfoKeys = RedisKeyEnum.MONITOR_INFO.ofs(vehicleIds);
            List<Map<String, String>> monitorMapList = RedisHelper.batchGetHashMap(monitorInfoKeys);
            brandMap = monitorMapList
                    .stream()
                    .filter(MapUtils::isNotEmpty)
                    .map(map -> MapUtil.mapToObj(map, BindDTO.class))
                    .collect(Collectors.toMap(BindDTO::getId, BindDTO::getName));
        }
        return brandMap;
    }

    private void addLog(String travelId, OPERATE operate) {
        String ip = getIpAddress();
        StringBuilder logs = new StringBuilder();
        logs.append(operate);
        logs.append("行程单号");
        if (!StringUtil.isNullOrBlank(travelId)) {
            logs.append("：");
            logs.append(travelId);
        }
        try {
            logSearchService.addLog(ip, logs.toString(), "3", "旅游客车行程管理", operate.toString());
        } catch (Exception e) {
            logger.error(operate + "旅游客车行程失败！");
        }

    }

}
