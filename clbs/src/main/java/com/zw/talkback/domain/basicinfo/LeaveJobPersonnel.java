package com.zw.talkback.domain.basicinfo;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 离职人员分组维护
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LeaveJobPersonnel extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String peopleId;

    private String assignmentId;

    private String peopleNumber;

    private String assignmentName;
}
