package com.cb.platform.domain;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


@Data
@EqualsAndHashCode(callSuper = true)
public class UserLogin extends BaseFormBean implements Serializable {

    private String userId; // 用户uuid

    @ExcelField(title = "用户名")
    private String userName; // 用户名

    private String groupId; // 企业uuid

    @ExcelField(title = "道路运输企业")
    private String groupName; // 企业名称

    @ExcelField(title = "上线时间")
    private String onlineTime; // 上线时间

    @ExcelField(title = "下线时间")
    private String offlineTime; // 下线时间

    @ExcelField(title = "在线时长")
    private String formatDuration;

    private Long onlineDuration; // 在线时长
}
