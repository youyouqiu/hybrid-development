package com.zw.platform.domain.other.protocol.jl.dto;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;

/**
 * @author denghuabing
 * @version V1.0
 * @description: TODO
 * @date 2020/6/12
 **/
@Data
public class StoppedVehicleQuery extends BaseQueryBean {

    private String platformId;
    /**
     * 时间(格式:yyyyMMdd)
     */
    private String date;
    /**
     * 	0    全部 (正常 + 未定位 + 离线)  1	未定位  2	未定位 + 离线  3	离线
     */
    private String type;
}
