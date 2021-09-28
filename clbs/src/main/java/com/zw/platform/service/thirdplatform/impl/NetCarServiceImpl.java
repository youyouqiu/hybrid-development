package com.zw.platform.service.thirdplatform.impl;

import com.github.pagehelper.page.PageMethod;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.connectionparamsset_809.T809ForwardConfig;
import com.zw.platform.domain.forwardplatform.ForwardMapping;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormConfigQuery;
import com.zw.platform.repository.modules.NetCarDao;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.thirdplatform.NetCarService;
import com.zw.platform.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class NetCarServiceImpl implements NetCarService {
    @Autowired
    private LogSearchService logService;

    @Autowired
    private UserService userService;

    @Autowired
    private NetCarDao netCarDao;

    @Override
    public List<T809ForwardConfig> list(IntercomPlatFormConfigQuery query) {
        final String userUuid = userService.getCurrentUserUuid();
        PageMethod.startPage(query.getPage().intValue(), query.getLimit().intValue());
        final String keyword = StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam());
        int platformId = Integer.parseInt(query.getIntercomPlatformId());
        return netCarDao.list(userUuid, keyword, platformId);
    }

    @Override
    public void add(String vehicleIds, String platformId) {
        if (StringUtils.isEmpty(vehicleIds)) {
            return;
        }
        //需要绑定的车id
        List<String> list = Arrays.asList(vehicleIds.split(","));
        String username = SystemHelper.getCurrentUsername();
        final int added = netCarDao.add(list, username, platformId);
        String message = "新增转发绑定关系" + added + "条";
        logService.log(message, "3", "", "-", "");
    }

    @Override
    public boolean delete(String mappings) {
        if (StringUtils.isEmpty(mappings)) {
            return false;
        }
        //需要解除绑定的车id
        final String[] ids = mappings.split(",");
        List<ForwardMapping> list = new ArrayList<>(ids.length / 2);
        for (int i = 0; i < ids.length; i += 2) {
            list.add(new ForwardMapping(ids[i], Integer.parseInt(ids[i + 1])));
        }
        int deleted = netCarDao.delete(list);
        String message = "解除转发绑定关系" + deleted + "条";
        logService.log(message, "3", "", "-", "");
        return true;
    }
}
