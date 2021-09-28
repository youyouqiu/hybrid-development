package com.zw.talkback.service.baseinfo.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.ImmutableMap;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.StringUtil;
import com.zw.talkback.domain.basicinfo.form.JobInfoData;
import com.zw.talkback.domain.basicinfo.form.JobManagementFromData;
import com.zw.talkback.domain.basicinfo.query.JobManagementQuery;
import com.zw.talkback.repository.mysql.IntercomPersonnelDao;
import com.zw.talkback.repository.mysql.JobManagementDao;
import com.zw.talkback.service.baseinfo.JobManagementService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class JobManagementServiceImpl implements JobManagementService {
    @Autowired
    private JobManagementDao jobManagementDao;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private IntercomPersonnelDao personnelDao;


    @Override
    public Page<JobInfoData> findByPage(JobManagementQuery query) {
        if (StringUtils.isNotBlank(query.getSimpleQueryParam())) {
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam()));
        }
        return PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                .doSelectPage(() -> jobManagementDao.find(query));
    }

    @Override
    public JobInfoData findJobById(String id) {
        return jobManagementDao.findJobById(id);
    }

    @Override
    public String findJobIcoUrByPeopleId(String peopleId) {
        return jobManagementDao.findJobByPeopleId(peopleId);
    }

    @Override
    public void addJobInfo(JobManagementFromData fromData, String ip) {
        fromData.setCreateDataUsername(SystemHelper.getCurrentUsername());
        jobManagementDao.addJobInfo(fromData);
        String message = "职位管理：新增职位（" + fromData.getJobName() + "）";
        logSearchService.addLog(ip, message, "3", "职位管理");
    }

    @Override
    public JSONObject uploadImg(MultipartFile file, HttpServletRequest request) throws IOException {
        JSONObject resultMap = new JSONObject();
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage read = ImageIO.read(inputStream);
            if (read != null) {
                int width = read.getWidth();
                int height = read.getHeight();
                if (width < 30 || width > 40 || height < 37 || height > 47) {
                    resultMap.put("imgName", "1");
                    return resultMap;
                }
            }
            String newName = "";
            if (!file.isEmpty()) {
                String filePath = request.getSession().getServletContext().getRealPath("/") + "upload/";
                Files.createDirectories(Paths.get(filePath));
                String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
                if (suffix.equals(".png")) {
                    newName = (System.currentTimeMillis() + "" + new Random().nextInt(100) + suffix);
                    // 转存文件
                    file.transferTo(new File(filePath + newName));
                    String jobFilePath =
                        request.getSession().getServletContext().getRealPath("/") + "resources/img/job/";
                    Files.createDirectories(Paths.get(jobFilePath));
                    file.transferTo(new File(jobFilePath + newName));
                    resultMap.put("imgName", newName);
                } else {
                    // 删除文件
                    Files.deleteIfExists(Paths.get(filePath + newName));
                    // 返回0 前端判断图片类型
                    resultMap.put("imgName", "0");
                }
            }
            return resultMap;
        }
    }

    @Override
    public List<JobInfoData> findAll() {
        return jobManagementDao.findAll();
    }

    @Override
    public boolean isChangeJobIcon(JobManagementFromData form, FTPClient ftpClient) throws IOException {
        JobInfoData beforeData = jobManagementDao.findJobById(form.getId());
        if (beforeData.getJobIconName().equals(form.getJobIconName())) {
            return false;
        }
        deleteJovPic(beforeData.getJobIconName());
        ftpClient.deleteFile(beforeData.getJobIconName());
        return true;
    }

    /**
     * 删除项目文件下的职位图标
     */
    private void deleteJovPic(String fileName) throws IOException {
        String filePath = System.getProperty("clbs.root") + "job/";
        Files.deleteIfExists(Paths.get(filePath + fileName));
    }

    @Override
    public boolean checkJobName(String jobName, String id) {
        List<JobInfoData> list = jobManagementDao.findJobCountByJobName(jobName);
        if (list.size() == 0) {
            return true;
        }
        return id != null && list.size() == 1 && (id.equals(list.get(0).getId()));
    }

    @Override
    public void updateJobInfo(JobManagementFromData form, String ip) {
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        String nowJobName = form.getJobName();
        String jobId = form.getId();
        //维护人员信息中职位缓存
        maintenanceJobCache(nowJobName, jobId);
        jobManagementDao.updateJobInfo(form);
        String message = "职位管理：修改职位（" + nowJobName + "）";
        logSearchService.addLog(ip, message, "3", "职位管理");
    }

    /**
     * 维护人员信息中职位缓存
     */
    private void maintenanceJobCache(String nowJobName, String jobId) {
        JobInfoData oldJobInfo = jobManagementDao.findJobById(jobId);
        //判断是否修改了职位名称,如果修改了需要维护人员缓存；
        if (oldJobInfo != null && !Objects.equals(oldJobInfo.getJobIconName(), nowJobName)) {
            List<String> peopleIdList = personnelDao.getPeopleIdListByJobId(jobId);
            if (CollectionUtils.isNotEmpty(peopleIdList)) {
                final Set<RedisKey> keys =
                        peopleIdList.stream().map(RedisKeyEnum.MONITOR_INFO::of).collect(Collectors.toSet());
                RedisHelper.batchAddToHash(keys, ImmutableMap.of("jobName", nowJobName));
            }
        }
    }

    @Override
    public void deleteJob(String id, FTPClient ftpClient, String ip) throws IOException {
        JobInfoData jobInfoData = jobManagementDao.findJobById(id);
        ftpClient.deleteFile(jobInfoData.getJobIconName());
        deleteJovPic(jobInfoData.getJobIconName());
        jobManagementDao.deleteJob(id);
        String message = "职位管理：删除职位（" + jobInfoData.getJobName() + "）";
        logSearchService.addLog(ip, message, "3", "职位管理");
    }

    @Override
    public boolean checkBinding(String id) {
        int count = jobManagementDao.checkBinding(id);
        return count > 0;
    }

    @Override
    public Map<String, String> getJobNameByPid(String id) {
        return jobManagementDao.getJobNameById(id).get(0);
    }

    @Override public List<Map<String, String>> getJobIconByPids(Set<String> onlineIds) {
        return jobManagementDao.getJobIconByPids(onlineIds);

    }
}
