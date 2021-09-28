package com.zw.platform.domain.vas.workhourmgt.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 
 *  振动传感器与车的绑定Form
 * 
 * @author wangying
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class VibrationSensorBindForm extends BaseFormBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
    /**
     * 车辆ID
     */
	@NotEmpty(message = "【车辆id】不能为空！", groups = { ValidGroupAdd.class,ValidGroupUpdate.class  })
    private String vehicleId;

    /**
     * 震动传感器ID
     */
	@NotEmpty(message = "【振动传感器id】不能为空！", groups = { ValidGroupAdd.class,ValidGroupUpdate.class  })
    private String shockSensorId;

    /**
     * 每秒采集个数
     */
	@Min(value=1,message = "【每秒采集个数】必须为整数，最大65535！",groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Max(value=65535,message = "【每秒采集个数】必须为整数，最大65535！",groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    private Integer collectNumber;

    /**
     * 上传个数
     */
	@Min(value=1,message = "【上传组数】必须为整数，最大65535！",groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Max(value=65535,message = "【上传组数】必须为整数，最大65535！",groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    private Integer uploadNumber;

    /**
     * 自动上传时间
     */
	@Pattern(message = "【自动上传时间】输入错误，只能输入01,02,03,04;其中01:被动,02:10s,03:20s,04:30s！",regexp = "^[0][1-4]{1}$", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    private String uploadTime;

    /**
     * 输出修正系数B
     */
    private String outputCorrectionB;

    /**
     * 输出修正系数K
     */
    private String outputCorrectionK;

    /**
     * 停机频率阈值
     */
    @Size(max = 20, message = "【停机频率阈值】长度不超过255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String outageFrequencyThreshold;

    /**
     * 怠速频率阈值
     */
    @Size(max = 20, message = "【怠速频率阈值】长度不超过255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String idleFrequencyThreshold;

    /**
     * 持续停机时间阈值
     */
    @Size(max = 20, message = "【持续停机时间阈值】长度不超过255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String continueOutageTimeThreshold;

    /**
     * 持续怠速时间阈值
     */
    @Size(max = 20, message = "【持续怠速时间阈值】长度不超过255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String continueIdleTimeThreshold;

    /**
     * 报警频率阈值
     */
    @Size(max = 20, message = "【报警频率阈值】长度不超过255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String alarmFrequencyThreshold;

    /**
     * 工作频率阈值
     */
    @Size(max = 20, message = "【工作频率阈值】长度不超过255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String workFrequencyThreshold;

    /**
     * 持续报警时间阈值
     */
    @Size(max = 20, message = "【持续报警时间阈值】长度不超过255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String continueAlarmTimeThreshold;

    /**
     * 持续工作时间阈值
     */
    @Size(max = 20, message = "【持续工作时间阈值】长度不超过255！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String continueWorkTimeThreshold;
    
    /**
     *  传感器型号
     */
    private String sensorType;

    /**
     * 传感器厂商
     */
    private String manufacturers;
	
    /**
     * 波特率
     */
    private String baudRate;

    /**
     * 奇偶校验
     */
    private String parity;

    /**
     * 补偿使能
     */
    private Integer inertiaCompEn;

    /**
     * 滤波系数
     */
    private Integer filterFactor;

    private Integer status; // 下发状态
    
    private String brand; // 车牌号
    
    private String groupId; // 组织
    
    private String vehicleType; // 车辆类型
    
    /**
     * 车辆id
     */
    private String vId;
    
    /**
     * 下发参数id
     */
    private String paramId;
}
