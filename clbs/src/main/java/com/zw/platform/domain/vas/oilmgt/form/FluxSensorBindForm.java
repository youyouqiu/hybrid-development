package com.zw.platform.domain.vas.oilmgt.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 
 *  流量传感器与车的绑定Form
 * 
 * @author wangying
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FluxSensorBindForm extends BaseFormBean implements Serializable {
	private static final long serialVersionUID = 1L;

	 /**
     * 油耗传感器ID
     */
	@NotEmpty(message = "【流量传感器id】不能为空！", groups = { ValidGroupAdd.class,ValidGroupUpdate.class  })
    private String oilWearId;

    /**
     * 车辆ID
     */
	@NotEmpty(message = "【车辆id】不能为空！", groups = { ValidGroupAdd.class,ValidGroupUpdate.class  })
    private String vehicleId;

    /**
     * 自动上传时间
     */
	@Pattern(message = "【自动上传时间】输入错误，只能输入01,02,03,04;其中01:被动,02:10s,03:20s,04:30s！",regexp = "^[0][1-4]{1}$", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    private String autoUploadTime;

    /**
     * 输出修正系数K
     */
    private String outputCorrectionK;

    /**
     * 输出修正系数B
     */
    private String outputCorrectionB;
}
