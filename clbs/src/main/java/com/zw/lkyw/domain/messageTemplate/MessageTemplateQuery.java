package com.zw.lkyw.domain.messageTemplate;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MessageTemplateQuery extends BaseQueryBean {
    public static final int ENABLE = 1;

    private Integer status;
}
