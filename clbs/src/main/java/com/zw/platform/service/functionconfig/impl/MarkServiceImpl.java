package com.zw.platform.service.functionconfig.impl;

import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.functionconfig.Mark;
import com.zw.platform.repository.modules.MarkDao;
import com.zw.platform.service.functionconfig.MarkService;
import com.zw.platform.util.common.MethodLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>Title: 标注ServiceImpl</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: wangying
 * @date 2016年8月8日下午2:23:03
 */
@Service
public class MarkServiceImpl implements MarkService {
    @Autowired
    private MarkDao markDao;

    @Autowired
    private OrganizationService organizationService;

    /**
     * 根据id查询标注
     */
    @MethodLog(name = "根据id查询标注", description = "根据id查询标注")
    public Mark findMarkById(String id) {
        Mark mark = markDao.findMarkById(id);
        if (mark != null) {
            String orgId = mark.getGroupId();
            OrganizationLdap org = organizationService.getOrganizationByUuid(orgId);
            if (org != null) {
                mark.setGroupName(org.getName());
            }
        }
        return mark;
    }



}
