package com.zw.platform.domain.oil;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by jiangxiaoqiang on 2016/9/28.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class VehicleActiveDate implements Serializable{
    private String vdate;
}
