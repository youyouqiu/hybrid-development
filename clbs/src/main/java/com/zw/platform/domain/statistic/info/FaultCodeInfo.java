package com.zw.platform.domain.statistic.info;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;

/**
 * 故障码INFO
 * @author zhouzongbo on 2018/12/28 15:53
 */
@Data
public class FaultCodeInfo implements Serializable {
    private static final long serialVersionUID = 2465972413204452512L;

    private String id;

    /**
     * 监控对象编号
     */
    @ExcelField(title = "监控对象")
    private String monitorNumber;

    /**
     * 监控对象ID
     */
    private String monitorId;

    /**
     * 分组名
     */
    @ExcelField(title = "分组")
    private String assignmentName;

    /**
     * 上传时间
     */
    @ExcelField(title = "上传时间")
    private String uploadTime;

    /**
     * 车型名称/发动机类型
     */
    @ExcelField(title = "车型名称/发动机类型")
    private String obdName;

    /**
     * 故障码
     */
    @ExcelField(title = "故障码")
    private String faultCode;

    @ExcelField(title = "描述")
    private String description;

}
