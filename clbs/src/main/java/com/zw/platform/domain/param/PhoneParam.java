package com.zw.platform.domain.param;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by LiaoYuecai on 2017/4/11.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PhoneParam extends BaseFormBean implements Serializable {
	private static final long serialVersionUID = 1L;
    private String vid;
    private String platformPhoneNumber;
    private String resetPhoneNumber;
    private String reInitialPhoneNumber;
    private String platformSMSPhoneNumber;
    private String receiveDeviceSMSTxtAlarmPhoneNumber;
    private Integer deviceAnswerPhoneType;
    private Integer timesMaxCallTime;
    private Integer monthlyMaxCallTime;
    private String listenPhoneNumber;
    private String platformPrivilegeSMSNumber;
}
