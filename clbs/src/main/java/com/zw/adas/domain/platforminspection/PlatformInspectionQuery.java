package com.zw.adas.domain.platforminspection;

import java.util.Date;
import java.util.Set;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

import com.zw.platform.util.common.BaseQueryBean;

import lombok.Data;

/**
 * @author wanxing
 * @Title: 平台巡检查询类
 * @date 2020/11/2415:04
 */
@Data
public class PlatformInspectionQuery extends BaseQueryBean {

    /**
     * 车辆数量
     */
    private Set<String> vehicleIds;

    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 结束时间
     */
    @NotNull(message = "结束时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /**
     * 巡检类型
     */
    @NotNull(message = "巡检类型不能为空")
    @Max(value = 3, message = "最大值为3")
    @Min(value = -1, message = "最小值为-1")
    private Integer inspectionType;

    /**
     * 车辆模糊查询字段
     */
    private String keyword;

}
