package com.zw.lkyw.domain.videoCarouselReport;

import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.excel.annotation.ExcelField;

import lombok.Data;

@Data
public class VideoInspectionDetail {

    private static final long serialVersionUID = -6449046835379779926L;
    @ExcelField(title = "车牌")
    private String monitorName;

    @ExcelField(title = "车牌颜色")
    private String color;

    @ExcelField(title = "车辆类型")
    private String objectType = "其他车辆";

    @ExcelField(title = "企业名称")
    private String groupName;
    /**
     * 通道号
     */
    @ExcelField(title = "通道号")
    private Integer channelNum;
    /**
     * 巡检开始时间
     */
    @ExcelField(title = "巡检开始时间")
    private String startTime;


    private String beginTime;

    /**
     *状态 0:成功 1:失败
     */
    private String status;

    /**
     *状态 0:成功 1:失败
     */
    @ExcelField(title = "状态")
    private String statusStr;

    /**
     * 失败原因
     *  1:	终端离线
     *  2: 视频请求超时
     *  3: 终端网络不稳定及其他
     */
    private Integer failReason;

    /**
     * 失败原因
     */
    @ExcelField(title = "失败原因")
    private String failReasonStr;

    public void setStartTime(String startTime) {
        if (startTime.contains("-")) {
            this.startTime = startTime;
            return;
        }
        this.startTime = DateUtil.getLongToDateStr(Long.parseLong(startTime) * 1000, null);
    }

    public void setStatus(String status) {
        if ("0".equals(status)) {
            this.statusStr = "成功";
        } else if ("1".equals(status)) {
            this.statusStr = "失败";
        } else {
            this.statusStr = status;
        }
        this.status = status;
    }

    public void setFailReason(Integer failReason) {
        if (failReason == 1) {
            this.failReasonStr = "终端离线";
        } else if (failReason == 2) {
            this.failReasonStr = "视频请求超时";
        } else {
            this.failReasonStr = "终端网络不稳定及其他";
        }
    }

}
