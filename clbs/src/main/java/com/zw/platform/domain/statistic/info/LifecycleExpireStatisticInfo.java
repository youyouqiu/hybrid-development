package com.zw.platform.domain.statistic.info;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zhouzongbo on 2018/9/10 16:05
 */
@Data
public class LifecycleExpireStatisticInfo implements Serializable{

    private static final long serialVersionUID = -1857749638209661038L;

    /**
     * 监控对象标识
     */
    @ExcelField(title = "监控对象标识")
    private String monitorNumber;

    /**
     * 监控对象类型(0:车;1:人;2:物)
     */
    @ExcelField(title = "监控对象类型")
    private String monitorType;

    /**
     * 组织名称
     */
    @ExcelField(title = "组织名称")
    private String groupName;

    private String groupId;

    /**
     * 分组名称
     */
    @ExcelField(title = "分组")
    private String assignmentName;

    /**
     * 服务到期状态:全部: 0;未到期: 1;即将到期: 2; 已到期: 3
     */
    @ExcelField(title = "服务到期状态")
    private String lifecycleStatus;

    /**
     * 即将到期天数or已到期天数（正整数）
     */
    @ExcelField(title = "剩余/已到期天数")
    private Integer expireDays;

    /**
     * 服务到期时间
     */
    private Date expireDate;

    @ExcelField(title = "服务到期时间")
    private String expireDateStr;

    private String monitorId;
}
