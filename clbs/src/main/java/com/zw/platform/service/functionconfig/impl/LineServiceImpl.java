package com.zw.platform.service.functionconfig.impl;

import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.functionconfig.Line;
import com.zw.platform.domain.functionconfig.LineContent;
import com.zw.platform.domain.functionconfig.LineSpot;
import com.zw.platform.domain.functionconfig.query.LineSegmentInfo;
import com.zw.platform.repository.modules.LineDao;
import com.zw.platform.service.functionconfig.LineService;
import com.zw.platform.util.common.MethodLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LineServiceImpl implements LineService {
    @Autowired
    private LineDao lineDao;
    @Autowired
    private OrganizationService organizationService;

    /**
     * 根据id查询线的每个点list
     */
    @MethodLog(name = "查询线的详情", description = "查询线的详情")
    public List<LineContent> findLineContentById(String id) {
        return lineDao.findLineContentById(id);
    }

    @MethodLog(name = "查询线的详情", description = "查询线的详情")
    public List<LineContent> findLineContentsById(String id) {
        return lineDao.findLineContentsById(id);
    }

    @Override
    public List<LineSpot> findLineSpotByLid(String id) {
        return lineDao.findLineSpotByLid(id);
    }

    @Override
    public Line findLineById(String id) {
        Line line = lineDao.findLineById(id);
        if (line != null) {
            OrganizationLdap org = organizationService.getOrganizationByUuid(line.getGroupId());
            if (org != null) {
                line.setGroupName(org.getName());
            }
        }
        return line;
    }

    @Override
    public List<LineSpot> findLineSpotByVid(String vid) {
        return lineDao.findLineSpotByVid(vid);
    }

    @Override
    public List<LineSegmentInfo> findSegmentContentByLid(String id) {
        return lineDao.findSegmentContentByLid(id);
    }

}
