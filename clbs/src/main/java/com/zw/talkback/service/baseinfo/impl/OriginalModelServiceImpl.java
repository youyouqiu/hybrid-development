package com.zw.talkback.service.baseinfo.impl;

import com.google.common.collect.Lists;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.GetIpAddr;
import com.zw.talkback.domain.intercom.info.OriginalModelInfo;
import com.zw.talkback.domain.lyxj.tsm3.Tsm3Result;
import com.zw.talkback.repository.mysql.OriginalModelDao;
import com.zw.talkback.service.baseinfo.OriginalModelService;
import com.zw.talkback.util.TalkCallUtil;
import com.zw.talkback.util.imports.ProgressDetail;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 原始机型
 */
@Service
public class OriginalModelServiceImpl implements OriginalModelService {

    private static final long serialVersionUID = 2746719026631599282L;

    @Resource
    private OriginalModelDao originalModelDao;

    @Autowired
    private TalkCallUtil talkCallUtil;

    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;

    @Override
    public List<OriginalModelInfo> findOriginalModelAndIntercomModel() {
        List<OriginalModelInfo> originalModelInfos = originalModelDao.findOriginalModelAndIntercomModel();
        return CollectionUtils.isEmpty(originalModelInfos) ? Lists.newArrayList() : originalModelInfos;
    }

    @Override
    public void addOriginalModelInfos(HttpServletRequest request) {
        ProgressDetail progress = new ProgressDetail();
        request.getSession().setAttribute("ORIGINAL_MODEL", progress);
        Integer pageSize = 300;
        Integer pageIndex = 1;
        List<OriginalModelInfo> data = new ArrayList<>();
        Tsm3Result deviceTypePageData;
        int total;
        int pageData = 0;
        //循环一直到数据全部取出来
        do {
            deviceTypePageData = talkCallUtil.getDeviceTypePageData("", pageSize, pageIndex);
            total = deviceTypePageData.getPageInfo().getTotalRecords();
            pageData += (deviceTypePageData.getRecords().size());
            progress.setTotal(total);
            progress.setProgress(pageData);
            pageIndex++;
            data.addAll(deviceTypePageData.getRecords());
        } while (pageData != total);
        if (data.size() > 0) {
            originalModelDao.addOriginalModel(data);
        }
        logSearchServiceImpl.addLog(new GetIpAddr().getIpAddr(request), "获取原始机型", "3", "", "-", "");

    }

    @Override
    public List<OriginalModelInfo> getOriginalModelByIndex(String index) {
        return originalModelDao.getOriginalModelByIndex(index);
    }

    @Override
    public List<Map<Long, String>> getAllOriginalModel(String index) {
        return originalModelDao.getAllOriginalModel(index);
    }

    @Override
    public List<OriginalModelInfo> getAllOriginalModel() {
        return originalModelDao.findAllOriginalModelInfo();
    }

}
