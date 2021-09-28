package com.zw.talkback.service.baseinfo;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.talkback.domain.basicinfo.form.JobInfoData;
import com.zw.talkback.domain.basicinfo.form.JobManagementFromData;
import com.zw.talkback.domain.basicinfo.query.JobManagementQuery;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;


public interface JobManagementService {
    Page<JobInfoData> findByPage(JobManagementQuery query);

    JobInfoData findJobById(String id);

    void addJobInfo(JobManagementFromData fromData, String ip);

    JSONObject uploadImg(MultipartFile file, HttpServletRequest request) throws IOException;

    List<JobInfoData> findAll();

    boolean isChangeJobIcon(JobManagementFromData form, FTPClient ftpClient) throws IOException;

    boolean checkJobName(String jobName, String id);

    void updateJobInfo(JobManagementFromData form, String ip);

    void deleteJob(String id, FTPClient ftpClient, String ip) throws IOException;

    boolean checkBinding(String id);

    Map<String, String> getJobNameByPid(String id);

    String findJobIcoUrByPeopleId(String peopleId);

    List<Map<String, String>> getJobIconByPids(Set<String> onlineIds);
}
