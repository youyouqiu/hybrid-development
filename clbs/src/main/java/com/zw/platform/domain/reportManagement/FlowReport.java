package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 流量实体类
 * @author zjc
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FlowReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @ExcelField(title = "监控对象")
    private String plateNumber;// 车牌号

    @ExcelField(title = "所属分组")
    private String assignmentNames;// 分组

    @ExcelField(title = "车牌颜色")
    private String plateColor;// 车牌颜色

    @ExcelField(title = "终端手机号")
    private String simcardNumber;// simcard卡号

    @ExcelField(title = "音视频个数")
    private Long videoCount;// 音视频个数

    // 预览时长
    @ExcelField(title = "预览时长（秒）")
    private Long previewTime;

    @ExcelField(title = "音视频流量（M）")
    private String flowValueStr;// 音视频流量

    private Double flowValue;// 音视频流量
}