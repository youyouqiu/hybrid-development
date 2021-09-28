
package com.zw.ws.entity.t808.oil;

import lombok.Data;

import java.io.Serializable;


/**
 * 工时设置参数
 * @author create by zhouzongbo
 */
@Data
public class WorkHourSettingParam implements Serializable {


    private static final long serialVersionUID = -6241408010349759774L;

    /**
     * 补偿使能
     */
    private Integer compensate;

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
     * 工时检测方式(1:电压比较式;2:油耗阈值式;3.油耗波动式)
     */
    private Integer workInspectionMethod;

    /**
     * 阀值1 电压阈值（V）
     */
    private Integer threshOne;

    /**
     * 波动计算个数
     */
    private Integer waveNum;

    /**
     * 波动计算时段:
     * 1：10 秒
     * 2：15 秒；
     * 3：20 秒；
     * 4：30 秒(缺省值)；
     * 5：60 秒；
     */
    private Integer waveTime;

    /**
     * 平滑系数
     */
    private Integer smoothParam;

    /**
     * 状态变换持续时长(s)
     */
    private Integer lastTime;

    /**
     * 传感器序号: 0:发动机1; 1:发动机2
     */
    private Integer sensorSequence;

    /**
     * 保留项1
     */
    private byte[] reservedItem2 = new byte[12];
    /**
     * 保留项2
     */
    private byte[] reservedItem3 = new byte[26];

    /**
     * 滤波方式01-实时；02-平滑（缺省值）；03-平稳；
     */
    private Integer smoothing;

    /**
     * 阀值2 默认0xFFFF
     */
    @Deprecated
    private Integer threshTwo;

    /**
     * 待机报警阈值
     */
    @Deprecated
    private Integer thresholdStandbyAlarm;

    /**
     * 总工作时长基值
     */
    @Deprecated
    private Long totalWorkBaseValue;

    /**
     * 总待机时长基值
     */
    @Deprecated
    private Long totalAwaitBaseValue;

    /**
     * 总停机时长基值
     */
    @Deprecated
    private Long totalHaltBaseValue;
}
