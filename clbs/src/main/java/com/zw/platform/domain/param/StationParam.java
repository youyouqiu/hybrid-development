package com.zw.platform.domain.param;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Title:F3超待设备基站参数设置
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年07月18日 13:42
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class StationParam extends BaseFormBean {

    private String vehicleNumber;//车辆编号
    private String vid;
    private Integer requitePattern;//上报模式( 1按频率上报  2按定点上报)
    private Integer requiteInterval;//上报时间间隔
    private Integer locationNumber;//上报时间间隔 ? 定点时间个数
    private Integer locationPattern;//定位模式
    private String requiteTime;//上报起始时间点
    private String locationTime;//定点时间
    private Integer locationTimeNum;//定点个数
    private String[] locationTimes;//定点时间集合

}
