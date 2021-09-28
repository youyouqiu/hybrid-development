package com.zw.platform.domain.basicinfo.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 终端Query
 * @author wangying
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DeviceQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id; // Id
    private String deviceNumber; // 终端编号
    private String deviceName; // 设备名称
    private Integer isStart; // 启停状态
    private String deviceType; // 设备类型
    private Integer channelNumber; // 通道数
    private Integer isVideo; // 是否视频
    private String barCode; // 条码
    private String manuFacturer; // 制造商
    private Integer flag; // 逻辑删除标志
    private Date installTime; //安装时间
    private Date procurementTime;//采购时间
    private Date createDataTime; //录入时间
    private String remark; //
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;
    private String groupName;
    private String groupType;
    /**
     * 终端型号
     */
    private String terminalType;
    /**
     * 终端厂商
     */
    private String terminalManufacturer;

    /**
     * 所属企业id
     */
    private String groupId;
}
