package com.zw.app.domain.webMaster.feedBack;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

/**
 * @author lijie
 * @date 2018/8/29 16:11
 */
@Data
public class FeedBackInfo {
    private static final long serialVersionUID = 1L;
    private String id = UUID.randomUUID().toString();
    private Date submitDate;
    private String userName;
    private String feedBack;
}
