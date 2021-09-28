package com.zw.platform.repository.modules;

import com.zw.platform.domain.taskmanagement.DesignateInfo;
import com.zw.platform.domain.taskmanagement.DesignateMonitorInfo;
import com.zw.platform.domain.taskmanagement.TaskInfo;
import com.zw.platform.domain.taskmanagement.TaskInfoQuery;
import com.zw.platform.domain.taskmanagement.TaskItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/11/7 14:21
 */
public interface TaskManagementDao {
    List<TaskInfo> getTaskList(@Param("list") List<String> list, @Param("query") TaskInfoQuery query);

    boolean addTaskInfo(TaskInfo taskInfo);

    boolean addTaskItem(List<TaskItem> list);

    TaskInfo findTaskInfoById(String id);

    boolean editTask(TaskInfo taskInfo);

    boolean deleteTaskItem(String id);

    boolean deleteTask(String id);

    List<DesignateInfo> getDesignateList(@Param("list") List<String> list, @Param("query") TaskInfoQuery query);

    boolean addDesignateInfo(DesignateInfo designateInfo);

    boolean addDesignatePeople(List<DesignateMonitorInfo> list);

    DesignateInfo findDesignateInfoById(String id);

    boolean deleteDesignatePeople(String id);

    boolean editDesignate(DesignateInfo taskInfo);

    boolean deleteDesignate(String id);

    TaskInfo checkTaskName(@Param("name") String name, @Param("gropId")  String gropId);

    List<TaskInfo> getTaskTree(@Param("list") List<String> list);

    DesignateInfo checkDesignateName(@Param("name") String name, @Param("gropId") String gropId);

    List<DesignateInfo> checkConflict(@Param("list") List<String> list, @Param("ids") Set<String> ids);

    boolean updateForcedEnd(@Param("id") String id);

    List<DesignateInfo> findDesignateByTaskId(String taskId);
}
