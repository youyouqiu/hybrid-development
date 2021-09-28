package com.zw.platform.domain.statistic.info;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;


/**
 * @author zhouzongbo on 2018/9/10 16:38
 */
@Data
public class LoadManagementStatisticInfo implements Serializable {

    private static final long serialVersionUID = 490067412273767632L;

    public static final int EFFECTIVE_DATA = 0;

    public static final int NONE_EFFECTIVE_DATA = 1;

    private String id;

    private byte[] vehicleId;

    /**
     * gps时间
     */
    private long vtime = 0L;

    /**
     * 车牌号
     */
    @ExcelField(title = "监控对象")
    private String plateNumber;

    /**
     * 所属企业
     */
    @ExcelField(title = "所属企业")
    private String groupName;


    @ExcelField(title = "载重状态")
    private String statusStr;

    @ExcelField(title = "时间")
    private String vtimeStr = "";

    @ExcelField(title = "状态持续时长")
    private String continueTimeStr;

    /**
     * 瞬时重量(kg): loadWeight(载荷重量)/unit(重量单位)
     */
    @ExcelField(title = "瞬时载重(kg)")
    private Double instanceWeight;

    @ExcelField(title = "位置")
    private String address;

    /**
     * 经度
     */
    private String longtitude;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 速度
     */
    private String speed;

    /**
     * 持续时长
     */
    private Long continueTime;

    /**
     * 载重obj
     */
    private String loadObjOne;

    private String loadObjTwo;

    /**
     * 是否是有效数据 0:有效 1:无效 3:空白数据
     */
    private Integer effectiveData = 0;

    /**
     * 载重状态 01: 空载； 02: 满载； 03: 超载； 04: 装载； 05: 卸载；06: 轻载；07: 重载
     */
    private Integer status;

    /**
     * 载重相对值
     */
    private Double weightAd;

    /**
     * 原始 AD 值
     */
    private Double originalAd;

    /**
     * 浮动零点
     */
    private Double floatAd;

    /**
     *经度维度组合key，方便做逆地址查询
     */
    private transient String addressKey;
}

