package com.zw.platform.domain.basicinfo.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/***
 @Author gfw
 @Date 2018/9/6 14:52
 @Description 条件查询query
 @version 1.0
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class LoadSensorQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 温度、湿度、正反转传感器 id
     */
    private String id;
    /**
     * 传感器型号
     */
    private String sensorNumber;
    /**
     * 波特率
     */
    private long baudRate;
    /**
     * 奇偶校验
     */
    private long oddEvenCheck;
    /**
     * 补偿使能
     */
    private long compensate;
    /**
     * 滤波系数
     */
    private long filterFactor;
    /**
     * 自动上传时间
     */
    private long autoTime;
    /**
     * 1: 温度传感器;2: 湿度传感器;3: 正反转传感器;4: 工时传感器;5：液位传感器；6：载重传感器
     */
    private long sensorType;
    /**
     * 备注
     */
    private String remark;
    /**
     *
     */
    private long flag;
    private Date createDataTime;
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;
    /**
     * 检测方式(1:电压比较式;2:油耗阈值式;3.油耗波动式)
     */
    private long detectionMode;
    /**
     * 传感器长度
     */
    private String sensorLength;
    /**
     * 个性参数 json
     */
    private String individualityParameters;
}
