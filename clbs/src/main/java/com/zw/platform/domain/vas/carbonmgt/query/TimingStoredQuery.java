package com.zw.platform.domain.vas.carbonmgt.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by 王健宇 on 2017/2/16.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TimingStoredQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String province; // 省
    private String oilType; // 油料类型
    private String oilPrice; // 油料价格
    private String dayTime; // 时间
    private Short flag;//
    private Date createDataTime;//
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;
}
