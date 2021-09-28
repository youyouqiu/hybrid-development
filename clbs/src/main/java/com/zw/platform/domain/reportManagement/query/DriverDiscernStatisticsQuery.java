package com.zw.platform.domain.reportManagement.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 *
 * @author penghj
 * @version 1.0
 * @date 2020/9/24 14:14
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DriverDiscernStatisticsQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 393136690363733981L;

    /**
     * 监控对象id 逗号分隔
     */
    private String monitorIds;

    /**
     * 比对开始日期
     */
    private String identificationStartDate;

    /**
     * 比对结束日期
     */
    private String identificationEndDate;

    /**
     * 比对结果
     * 不传:全部; 0:匹配成功; 1:匹配失败; 2:超时;
     * 3:没有启用该功能; 4:连接异常; 5:无指定人脸图片; 6:无人脸库;
     */
    private Integer identificationResult;

    /**
     * 比对类型
     * 不传:全部; 0:插卡比对; 1:巡检比对; 2:点火比对; 3:离开返回比对;
     */
    private Integer identificationType;
}
