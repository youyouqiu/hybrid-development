package com.zw.platform.util.excel.validator;

import com.cb.platform.domain.VehicleTravelForm;
import com.cb.platform.repository.mysqlDao.VehicleTravelDao;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.JsonResultBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class VehicleTravelValidator extends ImportValidator<VehicleTravelForm> {
    private Map<String, String> allBuses;

    private VehicleTravelDao vehicleTravelDao;

    private List<String> travelIds = new ArrayList<>();

    public VehicleTravelValidator(Map<String, String> allBuses, VehicleTravelDao vehicleTravelDao) {
        this.allBuses = allBuses;
        this.vehicleTravelDao = vehicleTravelDao;
    }

    @Override
    public JsonResultBean validate(List<VehicleTravelForm> list, boolean isCheckGroupName, List<OrganizationLdap> organizations) {
        checkData(list);
        String invalidMessage = getInvalidInfo();
        invalidMessage = invalidMessage.isEmpty() ? "" : "导入文件有以下错误，请修复后重新导入：<br/>" + invalidMessage;
        return new JsonResultBean(invalidMessage.isEmpty(), invalidMessage);
    }

    private void checkData(List<VehicleTravelForm> list) {

        if (list != null && list.size() > 0) {
            for (int i = 0, len = list.size(); i < len; i++) {
                VehicleTravelForm vehicleTravelForm = list.get(i);
                if (!isNOError(i, vehicleTravelForm)) {
                    break;
                }
            }
        }
    }

    private boolean isNOError(int i, VehicleTravelForm vehicleTravelForm) {
        boolean isNoError;
        isNoError =
            checkAddress(vehicleTravelForm.getAddress(), i) &&
            checkStartAndEndTime(vehicleTravelForm.getStartTime(), vehicleTravelForm.getEndTime(), i) &&
            checkTravelContent(vehicleTravelForm.getTravelContent(), i) &&
            checkRemark(vehicleTravelForm.getRemark(), i) && checkVehicleIsBus(vehicleTravelForm.getBrand(), i) &&
            checkTravalId(vehicleTravelForm.getTravelId(), i);
        return isNoError;
    }

    private boolean checkStartAndEndTime(Date startTime, Date endTime, int index) {

        if (startTime != null && endTime != null && startTime.getTime() > endTime.getTime()) {
            recordInvalidInfo(String.format("第%d条数据【开始时间不能大于结束时间】不能为空<br/>", index + 1));
            return false;
        }
        return true;
    }

    private boolean checkTravalId(String travelId, int index) {
        if (StringUtil.isNullOrBlank(travelId)) {
            recordInvalidInfo(String.format("第%d条数据【行程单号】不能为空<br/>", index + 1));
            return false;
        }
        if (isRepeateTravelId(travelId)) {
            recordInvalidInfo(String.format("第%d条数据【行程单号：%s】已经存在<br/>", index + 1, travelId));
            return false;
        }
        if (travelIds.contains(travelId)) {
            recordInvalidInfo(String.format("第%d条数据【行程单号：%s】在导入列表已经存在了<br/>", index + 1, travelId));
            return false;
        }
        travelIds.add(travelId);
        return true;

    }

    private boolean isRepeateTravelId(String travelId) {
        boolean isRepeate = false;
        List<String> travelIdList = vehicleTravelDao.isRepeateTravelId(null, travelId.toLowerCase());
        if (travelIdList != null && travelIdList.size() > 0) {
            isRepeate = true;
        }
        return isRepeate;
    }

    private boolean checkTravelContent(String travelContent, int index) {
        if (travelContent != null) {
            int len = travelContent.length();
            if (len > 500) {
                recordInvalidInfo(String.format("第%d条数据【行程内容：%s】为1~500位<br/>", index + 1, travelContent));
                return false;
            }
        }
        return true;

    }

    private boolean checkVehicleIsBus(String brand, int index) {
        if (StringUtil.isNullOrBlank(brand)) {
            recordInvalidInfo(String.format("第%d条数据【车牌号】不能为空<br/>", index + 1));
            return false;
        }
        if (StringUtil.isNullOrBlank(allBuses.get(brand))) {
            recordInvalidInfo(String.format("第%d条数据【车牌号：%s】不是客车<br/>", index + 1, brand));
            return false;
        }
        return true;
    }

    private boolean checkAddress(String address, int index) {
        if (address != null) {
            int len = address.length();
            if (len > 20) {
                recordInvalidInfo(String.format("第%d条数据【行程地点：%s】为1~20位<br/>", index + 1, address));
                return false;
            }
        }
        return true;

    }

    private boolean checkRemark(String remark, int index) {
        if (remark != null) {
            int len = remark.length();
            if (len > 50) {
                recordInvalidInfo(String.format("第%d条数据【行程备注：%s】为1~20位<br/>", index + 1, remark));
                return false;
            }
        }
        return true;
    }
}
