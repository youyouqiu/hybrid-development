package com.zw.adas.domain.report.inspectuser;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @author wanxing
 * @Title: 巡检人员查询类
 * @date 2020/12/3016:27
 */
@Data
public class InspectUserQuery extends BaseQueryBean {

    /**
     * 巡检应答人
     */
    private String answerUser;
    /**
     * 应答状态 0：未处理，1：已处理，2：已过期, -1代表全部
     */
    @NotNull(message = "应答状态不能为空")
    @Max(2)
    @Min(-1)
    private Integer status;

    /**
     * 巡检开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "巡检开始时间不能为空")
    private Date inspectStartTime;
    /**
     * 巡检结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "巡检结束时间不能为空")
    private Date inspectEndTime;

    /**
     * 组织id
     */
    private List<String> orgIds;
}
