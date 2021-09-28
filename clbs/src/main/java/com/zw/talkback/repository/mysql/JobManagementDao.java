package com.zw.talkback.repository.mysql;

import com.zw.platform.util.common.BaseQueryBean;
import com.zw.talkback.domain.basicinfo.form.JobInfoData;
import com.zw.talkback.domain.basicinfo.form.JobManagementFromData;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface JobManagementDao {
    List<JobInfoData> find(BaseQueryBean query);

    JobInfoData findJobById(String id);

    void addJobInfo(JobManagementFromData fromData);

    List<JobInfoData> findAll();

    List<JobInfoData> findJobCountByJobName(String jobName);

    void updateJobInfo(JobManagementFromData form);

    void deleteJob(String ids);

    int checkBinding(String id);

    List<Map<String, String>> getJobNameById(String id);

    String findJobByPeopleId(String peopleId);

    List<Map<String, String>> getJobIconByPids(@Param("onlineIds") Set<String> onlineIds);

    Set<String> findAllIconName();

    List<Map<String, String>> findJobMapList();

    JobInfoData findJobByMonitorId(@Param("monitorId") String monitorId);

    /**
     * 修改监控对象的职位信息
     * @param monitorId 监控对象ID
     * @param jobId     职位ID
     * @param isIncumbency 在职状态
     * @return 是否修改成功
     */
    boolean updateMonitorJobId(@Param("monitorId") String monitorId, @Param("jobId") String jobId,
        @Param("isIncumbency") int isIncumbency);
}
