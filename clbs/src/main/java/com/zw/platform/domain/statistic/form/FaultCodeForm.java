package com.zw.platform.domain.statistic.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 故障码
 * @author zhouzongbo on 2018/12/28 15:57
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FaultCodeForm extends BaseFormBean implements Serializable{

    private static final long serialVersionUID = -9037114047983107905L;


    private String monitorNumber;

    /**
     * 监控对象ID
     */
    private String monitorId;

    /**
     * 分组名
     */
    private String assignmentName;

    /**
     * 上传时间
     */
    private Date uploadTime = new Date();

    private String faultCode;

    private String description;


}
