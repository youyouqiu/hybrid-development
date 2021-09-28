package com.zw.platform.service.reportManagement.impl;

import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.reportManagement.VehicleOperationStatusReport;
import com.zw.platform.service.reportManagement.VehicleOperationStatusService;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleOperationStatusServiceImpl implements VehicleOperationStatusService {

    @Autowired
    private NewVehicleDao newVehicleDao;

    @Override
    public List<VehicleOperationStatusReport> getVehicleOperationInfoById(String vehicleIds) throws Exception {
        List<VehicleOperationStatusReport> result = new ArrayList<>();
        if (StringUtils.isBlank(vehicleIds)) {
            return result;
        }
        RedisKey key = HistoryRedisKeyEnum.VEHICLE_OPERATION_STATUS_REPORT.of(SystemHelper.getCurrentUsername());
        if (RedisHelper.isContainsKey(key)) {
            RedisHelper.delete(key);
        }
        List<String> vehicleIdList = Arrays.asList(vehicleIds.split(","));
        result = newVehicleDao.getVehicleOperationStatusById(vehicleIdList);
        if (CollectionUtils.isNotEmpty(result)) {
            for (VehicleOperationStatusReport data : result) {
                String plateColor = VehicleUtil.getPlateColorStr(data.getPlateColor().toString());
                String operationStatus = getVehicleOperationStatusStr(data.getOperatingState());
                String roadTransportValidityStartStr = getYMDStrByDate(data.getRoadTransportValidityStart());
                String roadTransportValidityStr = getYMDStrByDate(data.getRoadTransportValidity());
                data.setPlateColorStr(plateColor);
                data.setOperatingStateStr(operationStatus);
                data.setRoadTransportValidityStartStr(roadTransportValidityStartStr);
                data.setRoadTransportValidityStr(roadTransportValidityStr);
            }
            RedisHelper.addToList(key, result);
            RedisHelper.expireKey(key, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        }
        return result;
    }

    /**
     * 获取车辆营运状态
     */
    private String getVehicleOperationStatusStr(Integer operationStatus) throws Exception {
        String operationStatusStr = "";
        if (operationStatus != null) {
            switch (operationStatus) {
                case 0:
                    operationStatusStr = "营运";
                    break;
                case 1:
                    operationStatusStr = "停运";
                    break;
                case 2:
                    operationStatusStr = "挂失";
                    break;
                case 3:
                    operationStatusStr = "报废";
                    break;
                case 4:
                    operationStatusStr = "歇业";
                    break;
                case 5:
                    operationStatusStr = "注销";
                    break;
                case 6:
                    operationStatusStr = "迁出(过户)";
                    break;
                case 7:
                    operationStatusStr = "迁出(转籍)";
                    break;
                case 8:
                    operationStatusStr = "其他";
                    break;
                default:
                    break;
            }
        }
        return operationStatusStr;
    }

    private String getYMDStrByDate(Date date) throws Exception {
        String dataStr = "";
        if (date == null) {
            return dataStr;
        }
        dataStr = DateFormatUtils.format(date, "yyyy-MM-dd");
        return dataStr;
    }

    @Override
    public void exportVehicleOperationData(HttpServletResponse response, String param) throws Exception {
        ExportExcel export = new ExportExcel(null, VehicleOperationStatusReport.class, 1, null);
        RedisKey key = HistoryRedisKeyEnum.VEHICLE_OPERATION_STATUS_REPORT.of(SystemHelper.getCurrentUsername());
        List<VehicleOperationStatusReport> allExportList = RedisHelper.getList(key, VehicleOperationStatusReport.class);
        List<VehicleOperationStatusReport> exportResult = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(allExportList)) {
            exportResult.addAll(allExportList);
            if (StringUtils.isNotBlank(param)) {
                exportResult.clear();
                String upperCaseFuzzyParam = param.toUpperCase();
                List<VehicleOperationStatusReport> filterExportList =
                    allExportList.stream().filter(info -> info.getBrand().toUpperCase().contains(upperCaseFuzzyParam))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(filterExportList)) {
                    exportResult.addAll(filterExportList);
                }
            }
        }
        export.setDataList(exportResult);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
    }
}
