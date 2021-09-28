package com.zw.platform.domain.other.protocol.jl.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;

import java.util.Set;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/6/15 15:21
 */
@Data
public class AlarmVehiclePageReq extends BaseQueryBean {
    private static final long serialVersionUID = -8338021494098749760L;

    /**
     * 车辆id
     */
    private Set<String> monitorIds;
    private String monitorIdsStr;
    /**
     * 开始日期
     */
    private String uploadStartDate;
    /**
     * 结束日期
     */
    private String uploadEndDate;
    /**
     * 不传:全部
     * 上传状态：0: 失败; 1: 成功
     */
    private Integer uploadState;
    /**
     * 不传:全部
     * 报警类型 0:紧急报警; 10:疲劳报警; 200:禁入报警; 201:禁出报警; 210:偏航报警
     * 41:超速报警; 53:夜间行驶报警;
     */
    private Integer alarmType;
    /**
     * 不传:全部
     * 报警处理状态 1:处理中; 2:已处理完毕; 3:不做处理; 4:将来处理;
     */
    private Integer alarmHandleStatus;
}
