package com.zw.platform.domain.taskmanagement;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;

import java.util.List;

/**
 * 任务信息
 * @author penghj
 * @version 1.0
 * @date 2019/11/6 16:08
 */
@Data
public class TaskInfo extends BaseFormBean {
    private static final long serialVersionUID = 1904131187401467125L;

    /**
     * 任务名
     */
    private String taskName;

    private String remark;

    private String groupId;

    private String groupName;

    /**
     * 关联指派id
     */
    private String designateIds;

    /**
     * 关联指派名
     */
    private String designateNames;

    /**
     * 任务项
     */
    private List<TaskItem> taskItems;

    private String taskItemsStr;
}
