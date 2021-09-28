package com.zw.platform.domain.basicinfo.query;

import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * sim卡Query
 * @author wangying
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SimcardQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * sim卡信息
     */
    private String id;

    /**
     * sim卡号
     */
    private String simcardNumber;

    /**
     * 启停状态
     */
    private Integer isStart;

    /**
     * 运营商
     */
    private String operator;

    /**
     * 开卡时间
     */
    @ExcelField(title = "开卡时间")
    @DateTimeFormat(pattern = "yyyy-mm-dd")
    private Date openCardTime;

    /**
     * 容量
     */
    private String capacity;

    /**
     * 网络类型
     */
    private String networkType;

    /**
     * 套餐流量
     */
    private String simFlow;

    /**
     * 已用流量
     */
    private String useFlow;

    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    private String alertsFlow;
    @DateTimeFormat(pattern = "yyyy-mm-dd")
    private Date endTime;

    private String iccid;
    private String imsi;
    private String correctionCoefficient;
    private String forewarningCoefficient;
    private String hourThresholdValue;
    private String dayThresholdValue;
    private String monthThresholdValue;

    private String monthlyStatement;

    private String monthRealValue;

    private String dayRealValue;

    private String imei;

    private String deviceNumber;

    private String monthTrafficDeadline;

    private String remark;

    private String groupName;
    private String groupType;

    /**
     * 所属企业id
     */
    private String groupId;
}
