package com.zw.app.domain.monitor;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.io.Serializable;

/**
 * 监控对象详细信息实体
 * @author hujun
 * @date 2018/8/22 10:50
 */
@Data
public class DetailLocationInfo implements Serializable{
    private static final long serialVersionUID = 1L;

    private String deviceNo;//监控对象编号
    private String sim;//sim卡号
    private String org;//所属企业名称
    private String assigns;//所属分组名称
    private JSONArray sensors;//传感器数据
}
