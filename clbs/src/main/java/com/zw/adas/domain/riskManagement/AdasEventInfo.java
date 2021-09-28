package com.zw.adas.domain.riskManagement;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AdasEventInfo extends AdasInfo {

    /**
     * 序号 /
     */
    private Integer mediaSerialNumber;

    /**
     * 数量
     */
    private Integer mediaCount;

    /**
     * 黑标的媒体信息mediaInfo
     */
    private String mediaInfoStr;

    private String platformId;

    private String infoContent;

    private Integer drvLineId;

    /**
     * 流水号
     */
    private String msgSn;

    /**
     * 驾驶员驾照号码
     */
    private String driverNo;

    /**
     * 经度,单位为 1*10^-6 度
     */
    private Integer lng;

    /**
     * 纬度,单位为 1*10^-6 度
     */
    private Integer lat;

    /**
     * 海拔高度,单位为米(m)
     */
    private Integer altitude;

    private Integer grapherSpeed;

    private Byte picFlag;

    private Byte videoFlag;

    private String weather;

}
