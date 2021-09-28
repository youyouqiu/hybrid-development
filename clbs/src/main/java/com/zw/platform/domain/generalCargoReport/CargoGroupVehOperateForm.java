package com.zw.platform.domain.generalCargoReport;

import com.zw.platform.domain.basicinfo.form.BatchUpdateVehicleForm;
import com.zw.platform.domain.basicinfo.form.VehicleForm;
import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.Map;

/***
 @Author zhengjc
 @Date 2019/8/29 15:39
 @Description 普货车辆操作记录
 @version 1.0
 **/

@Data
public class CargoGroupVehOperateForm extends BaseFormBean {

    /**
     * 操作时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;

    /**
     * 企业id
     */
    private String groupId;

    /**
     * 车辆id
     */
    private String vehicleId;

    /**
     * 操作类型，1：新增，2：修改，3：删除
     */
    private Integer type;

    /**
     * 营运状态，1：营运，0：停运
     */
    private Integer operatingState;

    public static CargoGroupVehOperateForm getInstance(Date time, String groupId, String vehicleId, Integer type,
        Integer operatingState) {
        CargoGroupVehOperateForm form = new CargoGroupVehOperateForm();
        form.time = time;
        form.groupId = groupId;
        form.vehicleId = vehicleId;
        form.type = type;
        form.operatingState = operatingState;
        return form;
    }

    public static CargoGroupVehOperateForm getInstance(VehicleForm vehicleForm, String operate) {
        CargoGroupVehOperateForm form = new CargoGroupVehOperateForm();
        form.time = new Date();
        form.groupId = vehicleForm.getGroupId();
        form.vehicleId = vehicleForm.getId();
        form.type = getTypeByOperate(operate);
        form.operatingState = vehicleForm.getIsStart();
        return form;
    }

    public static CargoGroupVehOperateForm getInstance(BatchUpdateVehicleForm vehicleForm, String operate) {
        CargoGroupVehOperateForm form = new CargoGroupVehOperateForm();
        form.time = new Date();
        form.groupId = vehicleForm.getGroupId();
        form.vehicleId = vehicleForm.getId();
        form.type = getTypeByOperate(operate);
        form.operatingState = Integer.valueOf(vehicleForm.getIsStart());
        return form;
    }

    public static CargoGroupVehOperateForm getInstance(Map vehicle, String operate, int isStart) {
        CargoGroupVehOperateForm form = new CargoGroupVehOperateForm();
        form.time = new Date();
        form.groupId = vehicle.get("groupId").toString();
        form.vehicleId = vehicle.get("id").toString();
        form.type = getTypeByOperate(operate);
        form.operatingState = isStart;
        return form;
    }

    public static CargoGroupVehOperateForm getInstance(Map vehicle, String operate) {
        CargoGroupVehOperateForm form = new CargoGroupVehOperateForm();
        form.time = new Date();
        form.groupId = vehicle.get("groupId").toString();
        form.vehicleId = vehicle.get("id").toString();
        form.type = getTypeByOperate(operate);
        form.operatingState = Integer.parseInt(vehicle.get("isStart").toString());
        return form;
    }

    private static int getTypeByOperate(String operate) {
        //默认是删除
        int type = 3;
        if ("add".equals(operate)) {
            type = 1;
        } else if ("update".equals(operate)) {
            type = 2;
        } else if ("delete".equals(operate)) {
            type = 3;
        }
        return type;
    }

}
