package com.zw.platform.domain.generalCargoReport;

import lombok.Data;


/**
 * @author CJY
 */
@Data
public class WorkHandOverRecordDO {
    /**
     * 企业id
     */
    private String orgId;

    /**
     * 企业名称
     */
    private String orgName;

    /**
     * 车辆总数
     */
    private Integer monitorSum = 0;

    /**
     * 在线数
     */
    private Integer onlineMonitorNumber = 0;

    /**
     * 离线数
     */
    private Integer offOnlineMonitorNumber = 0;

    /**
     * 停运数
     */
    private Integer stopOperationMonitorNumber = 0;

    /**
     * 下发短信数
     */
    private Long sendNoteMonitorNumber = 0L;

    /**
     * 报警记录总数
     */
    private Integer alarmRecordSum = 0;

    /**
     * 超速报警数
     */
    private Integer overSpeed = 0;

    /**
     * 疲劳驾驶数
     */
    private Integer fatigueDriving = 0;

    /**
     * 不按规定线路行驶数
     */
    private Integer refuseStipulatePathDriving = 0;

    /**
     * 凌晨2-5点行驶数
     */
    private Integer exceptionMover = 0;

    /**
     * 遮挡摄像头数
     */
    private Integer holdBackCameraWork = 0;

    /**
     * 其它违规行为
     */
    private Integer otherRefuseRule = 0;

    /**
     *  报警、异常类型及车号
     */
    private String alarmMonitorName;
}
