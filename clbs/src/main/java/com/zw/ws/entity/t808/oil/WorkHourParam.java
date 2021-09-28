
package com.zw.ws.entity.t808.oil;

import lombok.Data;

import java.io.Serializable;


@Data
public class WorkHourParam implements Serializable {

	private static final long serialVersionUID = 1L;

    /**
     * 波特率
     */
    private Integer baudRate;
    /**
     * 奇偶校验
     */
    private Integer parity;

    /**
     * 补偿使能
     */
    private Integer inertiaCompEn;
    
    /**
     * 滤波系数
     */
    private Integer filterFactor;
    
    /**
     * 自动上传时间
     */
    private Integer uploadTime;
    /**
     * 输出修正系数K
     */
    private Integer outputCorrectionK;
    /**
     * 输出修正系数B
     */
    private Integer outputCorrectionB;
    /**
     * 停机频率阈值
     */
    private Integer outageFrequencyThreshold;
    /**
     * 持续停机时间阈值
     */
    private Integer continueOutageTimeThreshold;
    /**
     * 工作频率阈值
     */
    private Integer workFrequencyThreshold;
    /**
     * 怠速频率阈值
     */
    private Integer idleFrequencyThreshold;
    /**
     * 持续怠速时间阈值
     */
    private Integer continueIdleTimeThreshold;
    /**
     * 持续工作时间阈值
     */
    private Integer continueWorkTimeThreshold;
    /**
     * 持续报警时间阈值
     */
    private Integer continueAlarmTimeThreshold;
    /**
     * 每秒采集个数
     */
    private Integer collectNumber;
    /**
     * 上传个数
     */
    private Integer uploadNumber;
    /**
     * 报警频率阈值
     */
    private Integer alarmFrequencyThreshold;
    /**
     * 保留项1
     */
	private byte[] reservedItem1 = new byte[14];
	/**
	 *  保留项2
	 */
	private byte[] reservedItem2 = new byte[12];
}
