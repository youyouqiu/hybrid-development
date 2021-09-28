package com.zw.platform.domain.basicinfo.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 离线报表导出查询query
 * @author XK
 */
@Data
public class OfflineExportQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 创建日期
     */
    private Date creatDataTime;

    /**
     * 完成状态
     */
    private Integer status;
}
