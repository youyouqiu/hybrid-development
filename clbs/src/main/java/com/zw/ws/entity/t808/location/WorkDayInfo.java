package com.zw.ws.entity.t808.location;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiangxiaoqiang on 2016/10/14.
 */
@Data
public class WorkDayInfo implements Serializable {
    private Integer len;//信息长度

    private Integer id;//外设ID

    private List<WorkDayDataInfo> datas = new ArrayList<>();
}