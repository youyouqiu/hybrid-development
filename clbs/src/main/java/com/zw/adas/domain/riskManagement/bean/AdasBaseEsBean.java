package com.zw.adas.domain.riskManagement.bean;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class AdasBaseEsBean implements Serializable {

    /* 媒体索引的id */
    private String id;

    @JSONField(name = "create_time")
    private Date createTime = new Date();
}
