package com.zw.platform.domain.basicinfo.form;


import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;


/**
 * <p>Title: 车辆组织关联表</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p> <p>team: ZhongWeiTeam</p>
 * @author: Fan Lu
 * @date 2016年9月012日上午11：36
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class VehicleGroupForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String vehicleId;

    private String groupId;

    public static VehicleGroupForm of(String monitorId, String groupId, String createDataUserName) {
        VehicleGroupForm groupForm = new VehicleGroupForm();
        groupForm.setVehicleId(monitorId);
        groupForm.setGroupId(groupId);
        groupForm.setCreateDataTime(new Date());
        groupForm.setCreateDataUsername(createDataUserName);
        return groupForm;
    }

}