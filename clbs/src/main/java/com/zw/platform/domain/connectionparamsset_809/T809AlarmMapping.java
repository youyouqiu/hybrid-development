package com.zw.platform.domain.connectionparamsset_809;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 808_809报警转发映射实体
 * create by denghuabing 2018.12.24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class T809AlarmMapping extends BaseFormBean {

    //809设置表id
    private String settingId;

    //808pos
    private String pos808;

    private String pos809;

    //间隔时间
    private Integer time;

    //协议类型（0:1078_809, 1:796_809, 4:山西_809, 8:黑龙江_809, 26:西藏_809）
    private Integer protocolType;
}
