package com.zw.app.domain.webMaster.feedBack;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author lijie
 * @date 2018/8/29 15:41
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FeedBackQuery extends BaseQueryBean  implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String userName;
    private String startTime;
    private String endTime;
    private String submitDate;
    private String feedBack;
}
