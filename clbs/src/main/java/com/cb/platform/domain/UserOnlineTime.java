package com.cb.platform.domain;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class UserOnlineTime extends BaseFormBean implements Serializable {

    private String userId; // 用户id

    @ExcelField(title = "用户名")
    private String userName; // 用户名

    private String groupId; // 组织id

    @ExcelField(title = "道路运输企业")
    private String groupName; // 企业名

    @ExcelField(title = "上线日期")
    private Date onlineDate; // 上线日期

    private Date offlineTime; // 下线时间

    @ExcelField(title = "当天总在线时长")
    private long onlineDuration; // 当天总在线时长
}
