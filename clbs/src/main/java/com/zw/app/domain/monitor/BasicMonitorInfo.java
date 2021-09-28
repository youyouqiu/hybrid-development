package com.zw.app.domain.monitor;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.io.Serializable;

/**
 * 监控对象基本信息
 * @author hujun
 * @date 2018/8/24 14:15
 */
@Data
public class BasicMonitorInfo implements Serializable{
    private static final long serialVersionUID = 1L;

    private String group; //所属企业名称
    private String assign; //所属分组名称
    private String createDate; //创建日期
    private String billingDate; //计费日期
    private String expireDate; //到期日期
    private JSONObject vehicle; //车辆信息
    private JSONObject people; //人员信息
    private JSONObject thing; //物品信息
}
