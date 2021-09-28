package com.zw.platform.domain.basicinfo.driverDiscernManage;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 下发指令参数实体
 * @author tianzhangxu
 * @version 1.0
 * @date 2020/10/13 14:14
 */
@Data
public class DriverDiscernManageIssueParam implements Serializable {
    private static final long serialVersionUID = 696991896364177571L;

    /**
     * 设置类型0：增加（全替换），
     *
     * 1：删除（全删除），
     *
     * 2：删除指定条目，
     *
     * 3：修改(如果设备存在人脸 id，那么替换当前设备的人脸图片。如果设备不存在人脸 id，那么新增人脸)
     */
    private Integer type;

    /**
     * 企业/分组权限及通讯类型筛选后的车辆ID
     */
    private List<String> vehicleIds;

    /**
     * 选取的从业人员ID
     */
    private List<String> proIds;

    /**
     * 终端协议类型
     */
    private String deviceType;

}
