package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.commons.SystemHelper;
import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>Title: 设备组织关联表</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: Fan Lu
 * @date 2016年9月01日上午10:20
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DeviceGroupForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String deviceId;
    private String groupId;
    //增加这两个字段，为维护车和人（北斗）的缓存，做判断，勿删
    private String deviceType;
    private String functionalType;

    public static DeviceGroupForm of(String deviceId, String orgId, String userName) {
        DeviceGroupForm form = new DeviceGroupForm();
        form.setDeviceId(deviceId);
        form.setGroupId(orgId);
        form.setFlag(1);
        form.setCreateDataUsername(userName);
        form.setCreateDataTime(new Date());
        return form;
    }

    public static DeviceGroupForm initDeviceImport(DeviceForm device) {
        DeviceGroupForm form = new DeviceGroupForm();
        form.setDeviceId(device.getId());
        form.setGroupId(device.getGroupId());
        form.setDeviceType(device.getDeviceType());
        form.setFunctionalType(device.getFunctionalType());
        form.setCreateDataTime(new Date());
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        return form;
    }

}

