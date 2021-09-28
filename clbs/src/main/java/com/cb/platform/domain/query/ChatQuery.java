package com.cb.platform.domain.query;

import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 *
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class ChatQuery  extends BaseQueryBean  implements Serializable {
    /**
     * 聊天内容
     */
    private String chatContent;

    /**
     * 发送者用户id
     */
    private String fromUserName;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;
}
