package com.zw.app.domain.monitor;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/5/20 16:30
 */
@Data
public class BasicLocationInfoNew implements Serializable {
    private static final long serialVersionUID = -4462409553404070238L;
    /**
     * 监控对象名称
     */
    private String name;
    /**
     * 监控对象类型
     */
    private Integer type;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 具体位置信息
     */
    private String address;
    /**
     * 状态持续时间（秒）
     */
    private Long duration;
    /**
     * 定位时间（yyyy-MM-dd hh:mm:ss）
     */
    private String gpsTime;
    /**
     * 电量（等0-100）
     */
    private Integer battery;
    /**
     * wifi信号强度（等级1-5）
     */
    private Integer wifi;
    /**
     * 信号强度
     */
    private Integer signalStrength;
    /**
     * 信号类型
     * 4: 2G 5: 3G 6: 4G 7: 5G
     */
    private Integer signalType;
    /**
     * 速度
     */
    private String speed;

    /**
     * 定位模式 0：卫星+基站 定位；  1：基站 定位；  2：卫星定位；  3：WIFI+基站 定位；  4：卫星+WIFI+基站 定位；
     */
    private Integer pattern;

    /**
     * 卫星数量
     */
    private Integer satellitesNumber;

    private String id;//监控对象id
    private String time;// 定位时间（yyMMddhhmmss）
    /**
     * 维度
     */
    private Double latitude;
    /**
     * 经度
     */
    private Double longitude;

    /**
     * 方向
     */
    private Double angle;

    /**
     * 车辆图片
     */
    private String ico;

    /**
     * GPS里程数据
     */
    private Double gpsMileage;

}
