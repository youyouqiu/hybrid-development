package com.zw.platform.domain.connectionparamsset_809;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class T809ForwardConfig extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 车辆绑定关系id
     */
    private String configId;
    /**
     * 车辆id
     */
    private String vehicleId;
    /**
     * 平台id
     */
    private String plantFormId;
    /**
     * 车牌号
     */
    private String brand;
    /**
     * 企业名称
     */
    private String orgName;
    /**
     * 平台名称
     */
    private String plantFormName;
    /**
     * 平台ip
     */
    private String plantFormIp;
    /**
     * 平台端口
     */
    private String plantFormPort;
    /**
     * 平台接入码
     */
    private Integer platFormCenterId;
    private String protocolType;
    private Integer vehicleColor;
    /**
     * 车辆编码 上级平台编码
     */
    private String vehicleCode;
}
