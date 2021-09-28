package com.zw.platform.service.taskcenter;

import com.zw.platform.domain.taskmanagement.DesignateInfo;
import com.zw.platform.domain.taskmanagement.TaskInfo;
import com.zw.platform.domain.taskmanagement.TaskInfoQuery;
import com.zw.platform.util.common.JsonResultBean;

import java.text.ParseException;
import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/11/7 13:50
 */
public interface TaskManagementService {

    List<TaskInfo> getTaskList(TaskInfoQuery query);

    JsonResultBean addTask(TaskInfo taskInfo, String ipAddress) throws Exception;

    List<DesignateInfo> getDesignateList(TaskInfoQuery query) throws ParseException;

    JsonResultBean updateTask(TaskInfo taskInfo, String ipAddress) throws Exception;

    TaskInfo findTaskInfoById(String id);

    JsonResultBean deleteTask(String id, String ipAddress) throws Exception;

    JsonResultBean addDesignate(DesignateInfo designateInfo, String ipAddress) throws Exception;

    JsonResultBean updateDesignate(DesignateInfo designateInfo, String ipAddress) throws Exception;

    DesignateInfo getDesignateById(String id);

    JsonResultBean deleteDesignate(String id, String ipAddress) throws Exception;

    boolean checkTaskName(String name, String id);

    String getTaskTree();

    boolean checkDesignateName(String name, String id);

    JsonResultBean updateForcedEnd(String id, String ipAddress) throws Exception;

    List<DesignateInfo> findDesignateByTaskId(String taskId);
}
