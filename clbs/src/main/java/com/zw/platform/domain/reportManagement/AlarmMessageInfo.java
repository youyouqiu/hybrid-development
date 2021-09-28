package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import redis.clients.jedis.Response;

import java.io.Serializable;

/**
 * 报警信息统计info
 * @author craete by zhouzongbo
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AlarmMessageInfo extends BaseFormBean implements Serializable {

    private static final long serialVersionUID = 1322934727195408121L;

    private byte[] vehicleIdByte;
    private String vehicleId;

    @ExcelField(title = "监控对象")
    private String plateNumber;

    @ExcelField(title = "所属分组")
    private String assignmentName;

    private String plateColor;

    @ExcelField(title = "车牌颜色")
    private String plateColorStr;
    /**
     * 报警类型
     */
    private Integer alarmType;

    @ExcelField(title = "报警类型")
    private String alarmTypeName;
    private Response<String> alarmTypeResponse;
    @ExcelField(title = "报警数量")
    private Integer alarmNumber;

    @ExcelField(title = "已处理数")
    private Integer hasDealNum = 0;

    /*子列表数据*/
    /**
     * 报警时间
     */
    private Long alarmStartTime;

    private String alarmStartTimeStr;

    /**
     * 速度
     */
    private String speed;

    /**
     * 开始位置经、纬度
     */
    private String alarmStartLocation;

    /**
     * 处理状态: 0:未处理;1:已处理
     */
    private Integer status;

    /**
     * 分组key: 车辆ID + 报警类型
     */
    private String groupByKey;
}
