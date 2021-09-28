package com.zw.platform.service.offlineExport.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.domain.basicinfo.query.OfflineExportQuery;
import com.zw.platform.repository.modules.OfflineExportDao;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.util.ConstantUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OfflineExportServiceImpl implements OfflineExportService {

    @Value("${fdfs.webServerUrl}")
    private String webServerUrl;

    @Value("${system.ssl.enable}")
    private boolean sslEnabled;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private OfflineExportDao offlineExportDao;

    @Override
    public List<String> findUserNameByDigestId(String digestId) {
        if (StringUtils.isEmpty(digestId)) {
            return new ArrayList<>();
        }
        return offlineExportDao.getUserNamesByDigestId(digestId);
    }

    @Override
    public Page<OfflineExportInfo> getPageOfflineExport(OfflineExportQuery query) {
        String userName = SystemHelper.getCurrentUsername();
        query.setUserName(userName);
        Page<OfflineExportInfo> page = PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                .doSelectPage(() -> offlineExportDao.getOfflineExportBySimpleQuery(query));
        for (OfflineExportInfo offlineExportInfo : page) {
            if (sslEnabled) {
                offlineExportInfo.setAssemblePath("/" + offlineExportInfo.getAssemblePath());
            } else {
                offlineExportInfo.setAssemblePath(webServerUrl + offlineExportInfo.getAssemblePath());
            }
            if (offlineExportInfo.getFileSize() != null) {
                BigDecimal b = BigDecimal.valueOf(offlineExportInfo.getFileSize() / 1024D);
                offlineExportInfo.setDoubleFileSize(b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
        }
        return page;
    }

    @Override
    public Map<String, String> addOfflineExport(OfflineExportInfo offlineExportInfo) {

        Map<String, String> sendMap = null;
        //默认还没有导出完
        //在这里统一设置用户名
        offlineExportInfo.setUserName(SystemHelper.getCurrentUsername());

        OfflineExportInfo data = offlineExportDao.getOfflineExportByDigestId(offlineExportInfo.getDigestId());
        //有相同的导出记录,就采用以前的数据，并替换用户名和文件名称以及创建时间
        if (null != data) {
            data.setCreateDateTime(offlineExportInfo.getCreateDateTime());
            data.setFileName(offlineExportInfo.getFileName());
            data.setUserName(offlineExportInfo.getUserName());
            if (data.getStatus() == 2) {
                sendMap = new HashMap<>();
                sendMap.put("id", data.getDigestId());
                sendMap.put("url", webServerUrl + data.getAssemblePath());
            }
            if (data.getStatus() == 3) {
                data.setStatus(offlineExportInfo.getStatus());
            }
        } else {
            data = offlineExportInfo;
        }

        offlineExportDao.addOfflineExportInfo(data);
        return sendMap;
    }

    @Override
    public void deleteOfflineExport(String deleteTime) {
        offlineExportDao.deleteOfflineExport(deleteTime);
    }

    @Override
    public Set<String> selectExportRealPath(String deleteTime) {
        return offlineExportDao.selectExportRealPath(deleteTime);
    }

    @Override
    public void updateExportStatus(String updateTime, int status) {
        offlineExportDao.updateExportStatus(updateTime, status);
    }

    @Override
    public void senExportResultMsg(Map<String, String> sendMap) {
        String userName = SystemHelper.getCurrentUsername();
        simpMessagingTemplate.convertAndSendToUser(userName, ConstantUtil.WEB_SOCKET_OFFLINE_EXPORT, sendMap);
    }

    @Override
    public OfflineExportInfo getInfoByDigestId(String id) {
        return offlineExportDao.getOfflineExportByDigestId(id);
    }

}
