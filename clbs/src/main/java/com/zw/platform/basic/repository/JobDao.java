package com.zw.platform.basic.repository;

import com.zw.platform.basic.domain.JobDO;

import java.util.List;

/**
 * 职位DAO类
 *
 * @author zhangjuan
 */
public interface JobDao {
    /**
     * 获取所有的职位类别
     *
     * @return 职位类别列表
     */
    List<JobDO> getAllJob();
}
