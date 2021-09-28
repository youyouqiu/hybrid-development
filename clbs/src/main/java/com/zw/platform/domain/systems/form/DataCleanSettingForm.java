package com.zw.platform.domain.systems.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;

/**
 * @author denghuabing
 * @version V1.0
 * @description: TODO
 * @date 2020/10/27
 **/
@Data
public class DataCleanSettingForm extends BaseFormBean {
    public static final String POSITIONAL_SETTING = "1";
    public static final String ALARM_SETTING = "2";
    public static final String MEDIA_SETTING = "3";
    public static final String LOG_SETTING = "4";
    public static final String SPOT_CHECK_SETTING = "5";
    // 默认值
    public static final Integer DEFAULT_VALUE = 12;

    private Integer positionalTime;
    private Integer alarmTime;
    private Integer mediaTime;
    private Integer logTime;
    private Integer spotCheckTime;
    private String time;
    private String cleanType;
}
