package com.zw.platform.service.systems.impl;

import com.github.pagehelper.Page;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.zw.adas.utils.FastDFSClient;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.systems.DeviceUpgrade;
import com.zw.platform.domain.systems.query.DeviceUpgradeQuery;
import com.zw.platform.repository.vas.DeviceUpgradeDao;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.systems.DeviceUpgradeService;
import com.zw.platform.util.PageHelperUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class DeviceUpgradeServiceImpl implements DeviceUpgradeService, IpAddressService {


    @Autowired
    private DeviceUpgradeDao deviceUpgradeDao;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Autowired
    private LogSearchService logSearchService;

    private Logger logger = LogManager.getLogger(DeviceUpgradeServiceImpl.class);


    /**
     *新增和修改升级文件接口
     */
    @Override
    public String addDeviceUpgradeFile(MultipartFile file, DeviceUpgrade deviceUpgrade)
        throws Exception {
        String address = getIpAddress();
        //代表新增
        String url;
        if (StringUtils.isEmpty(deviceUpgrade.getUpgradeFileId()) && file != null) {
            //新增
            //上传文件到fastDfs
            url = getUrl(fastDFSClient.uploadFile(file));
            //代表的是新增
            deviceUpgrade.setUploadTime(new Date());
            deviceUpgrade.setUrl(url);
            deviceUpgrade.setFileName(file.getOriginalFilename());
            deviceUpgrade.setUpgradeFileId(UUID.randomUUID().toString());
            deviceUpgradeDao.addDeviceUpgradeFile(deviceUpgrade);
            logSearchService.addLog(address, SystemHelper.getCurrentUsername() + "新增终端升级文件:"
                + file.getOriginalFilename(), "3", "", "终端升级文件");
            return "新增升级文件成功";
        } else {
            //代表的是修改
            DeviceUpgrade oldFile = deviceUpgradeDao.queryDeviceUpgradeById(deviceUpgrade.getUpgradeFileId());
            String message = SystemHelper.getCurrentUsername() + "修改终端升级文件:" + oldFile.getFileName();
            if (!StringUtils.isEmpty(deviceUpgrade.getUpgradeFileId()) && file != null) {
                //代表文件已经覆盖了
                try {
                    fastDFSClient.deleteFile(oldFile.getUrl());
                } catch (Exception e) {
                    logger.info("fastDFSClient未找到文件", e);
                }
                url = getUrl(fastDFSClient.uploadFile(file));
                deviceUpgrade.setFileName(file.getOriginalFilename());
                deviceUpgrade.setUploadTime(new Date());
                deviceUpgrade.setUrl(url);
                if (!oldFile.getFileName().equals(file.getOriginalFilename())) {
                    message += ",并将原文件替换为:" + file.getOriginalFilename();
                }
            }
            deviceUpgradeDao.updateDeviceUpgradeFile(deviceUpgrade);
            logSearchService.addLog(address, message, "3", "", "终端升级文件");
            return "修改升级文件成功";
        }
    }

    private String getUrl(String url) {
        if (StringUtils.isEmpty(url)) {
            return "";
        }
        return url.split(fdfsWebServer.getWebServerUrl())[1];
    }

    @Override
    public void updateDeviceUpgradeFile(DeviceUpgrade deviceUpgrade) {

    }

    @Override
    public void addDeviceUpgradeByBatch(List<DeviceUpgrade> list) {

    }

    @Override
    public void deleteDeviceUpgradeById(List<String> ids) {
        deviceUpgradeDao.deleteDeviceUpgradeById(ids);
    }

    @Override
    public void deleteDeviceUpgradeById(String id) {
        DeviceUpgrade deviceUpgrade = deviceUpgradeDao.queryDeviceUpgradeById(id);
        deviceUpgradeDao.deleteDeviceUpgradeFile(id);
        if (deviceUpgrade != null) {
            //删除fastDfs上的文件
            logSearchService.addLog(getIpAddress(), SystemHelper.getCurrentUsername() + "删除终端升级文件:"
                + deviceUpgrade.getFileName(), "3", "", "终端升级文件");
            try {
                fastDFSClient.deleteFile(deviceUpgrade.getUrl());
            } catch (Exception e) {
                logger.info("fastDFSClient未找到文件", e);
            }
        }
    }

    @Override
    public Page<DeviceUpgrade> queryList(DeviceUpgradeQuery query) {
        return PageHelperUtil.doSelect(query, () -> deviceUpgradeDao.queryList());
    }
}
