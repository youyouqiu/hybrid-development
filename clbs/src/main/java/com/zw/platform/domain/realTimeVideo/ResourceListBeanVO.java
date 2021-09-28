package com.zw.platform.domain.realTimeVideo;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Set;


@Data
@EqualsAndHashCode(callSuper = false)
public class ResourceListBeanVO {
    /**
     * 资源列表基础类集合
     */
    private List<ResourceListBean> resourceListBeans = Lists.newArrayList();

    /**
     * FTP查询类集合
     */
    private List<VideoFTPQuery> resourceList = Lists.newArrayList();

    /**
     * 日期集合，用于页面展示
     */
    private Set<String> calendarSet = Sets.newHashSet();

}
