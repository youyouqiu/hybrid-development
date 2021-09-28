package com.zw.platform.domain.other.protocol.jl.dto;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @author denghuabing
 * @version V1.0
 * @description: TODO
 * @date 2020/6/12
 **/
@Data
public class StoppedVehicleRecordQuery extends BaseQueryBean {
    private String ids;
    private List<String> vehicleIds;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date uploadDateStart;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date uploadDateEnd;
    /**
     * 不传：全部 0: 失败; 1: 成功
     */
    private Integer state;
    /**
     * 报停原因(1:天气原因,2:车辆故障,3:路阻,4:终端报修,9:其他  不传 全部)
     */
    private Integer stopCauseCode;
}
