package com.zw.app.domain.monitor;

import lombok.Data;

import java.io.Serializable;

/**
 * 监控对象基础位置信息
 * @author hujun
 * @date 2018/8/21 11:09
 */
@Data
public class BasicLocationInfo implements Serializable{
    private static final long serialVersionUID = 1L;

    private String name;// 监控对象名称
    private Integer type;// 监控对象类型
    private Integer status;// 状态
    private String address;// 具体位置信息
    private Long duration;// 状态持续时间（秒）
    private String gpsTime;// 定位时间（yyyy-MM-dd hh:mm:ss）
    private Integer battery;// 电量（等级1-5）
    private Integer wifi;// wifi信号强度（等级1-5）
    private Integer lbs;// lbs信号强度（等级1-5）
    private Integer gps;// 卫星信号强度（等级1-5）
    private Integer lbs_wifi;// lbs + wifi混合定位（等级1-5）
    private String speed;// 速度
}
