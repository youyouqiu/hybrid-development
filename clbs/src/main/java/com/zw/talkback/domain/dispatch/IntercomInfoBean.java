package com.zw.talkback.domain.dispatch;

import lombok.Data;

@Data
public class IntercomInfoBean extends IntercomObjectBean {
    /**
     * 对讲在线状态 1:在线 0:不在线
     */
    private Long status;

    /**
     * 用户当前所在组的id
     */
    private String assignmentId;

    /**
     * 用户当前所在组的name
     */
    private String assignmentName;

    /**
     * 用户当前所在组的所属企业Id
     */
    private String assignmentGroupId;

    /**
     * 用户当前所在组的所属企业name
     */
    private String assignmentGroupName;
}
