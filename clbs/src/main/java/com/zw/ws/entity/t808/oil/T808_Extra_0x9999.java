package com.zw.ws.entity.t808.oil;

import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jiangxiaoqiang on 2016/11/8.
 * T808用户自定义扩展
 * 油位/油耗传感器参数设置项
 */
@Data
public class T808_Extra_0x9999 implements Serializable {

    /**
     * 详细参数
     */
    private List<OilVehicleSetting> oilVehicleSetting;
}
