package com.cb.platform.domain.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 *
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class ChatGroupQuery extends BaseQueryBean  implements Serializable {
    /**
     * 讨论组名称
     */
    private String groupName;
    /**
     * 创建人
     */
    private String createDataUsername;

}
