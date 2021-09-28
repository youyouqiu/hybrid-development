package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.StrUtil;
import com.zw.protocol.util.ProtocolTypeUtil;
import lombok.Data;

import java.io.Serializable;

@Data
public class T809InfoCheckAck implements Serializable {
    /**
     * 报警来源
     */
    private Integer warnSrc;

    /**
     * 报警类型
     */
    private Integer warnType;

    /**
     * 报警时间
     */
    private Long warnTime;

    /**
     * 报警信息 ID
     */
    private String infoId;

    /**
     * 报警信息 ID
     */
    private String alarmId;

    /**
     * 车牌号码。非车辆相关报警全填0
     */
    private String vehicleNo;

    /**
     * 车牌颜色，按照JT/T 697.7-2014的规定。非车辆相关报警全填0
     */
    private Integer vehicleColor;

    /**
     * 驾驶员姓名长度
     */
    private Integer driverLength;

    /**
     * 驾驶员姓名
     */
    private String driver;

    /**
     * 驾驶员驾照号码长度
     */
    private Integer driverNoLength;

    /**
     * 驾驶员驾照号码
     */
    private String driverNo;

    /**
     * 报警级别
     */
    private Integer level;

    /**
     * 经度,单位为 1*10^-6 度
     */
    private Integer lon;

    /**
     * 纬度,单位为 1*10^-6 度
     */
    private Integer lat;

    /**
     * 海拔高度,单位为米(m)
     */
    private Integer altitude;

    /**
     * 行车速度，单位为千米每小时(km/h)
     */
    private Integer vec1;

    /**
     * 行驶记录速度,单位为千米每小时(km/h)
     */
    private Integer vec2;

    /**
     * 报警状态,1:报警开始;2:报警结束
     */
    private Integer status;

    /**
     * 方向,0-359,正北为 0,顺时针
     */
    private Integer direction;

    /**
     * 上报报警信息长度
     */
    private Integer infoLength;

    /**
     * 上报报警信息内容
     */
    private String infoContent;

    /**
     * 时间id
     */
    private String eventId;

    /**
     * 平台id
     */
    private String platId;

    /**
     * 流水号
     */
    private String msgSn;

    /**
     * 按照协议类型返回具体协议类型的数据长度
     * @param connectProtocolType
     * @return
     */
    public Integer getDataLength(String connectProtocolType) {
        Integer dataLength = driverLength + driverNoLength + infoLength;
        if (ProtocolTypeUtil.isGStandard(connectProtocolType)) {
            dataLength = 65 + dataLength;
        } else {
            dataLength = 49 + dataLength;
        }
        return dataLength;
    }

    /**
     * 初始化相关长度字段
     */
    public void initLength() {
        driverNoLength = StrUtil.getLen(driverNo);
        driverLength = StrUtil.getLen(driver);
        infoLength = StrUtil.getLen(infoContent);
    }
}
