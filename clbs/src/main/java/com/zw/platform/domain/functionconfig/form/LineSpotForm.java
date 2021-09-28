package com.zw.platform.domain.functionconfig.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 关键点form
 *
 * @author  Tdz
 * @create 2017-04-01 13:41
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class LineSpotForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 线id
     */
    private String lineId;

    /**
     * 名称
     */
    private String name;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 到达时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date arriveTime;

    /**
     * 离开时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date leaveTime;

    /**
     * 描述
     */
    private String description;

}
