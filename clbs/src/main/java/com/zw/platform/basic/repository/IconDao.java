package com.zw.platform.basic.repository;

import com.zw.platform.basic.domain.IconDO;
import com.zw.platform.basic.dto.IconDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * zw_c_ico_config 监控对象图标
 * @author zhangjuan
 */
public interface IconDao {

    /**
     * 根据ID获取图标
     * @param id 图标ID
     * @return 图标详情
     */
    IconDO getById(@Param("id") String id);

    /**
     * 获取全部的图标
     * @return 图标列表
     */
    List<IconDTO> getAll();

    /**
     * 图标删除
     * @param id id
     * @return 是否操作成功
     */
    boolean delete(@Param("id") String id);

    /**
     * 图标添加
     * @param iconDO 图标
     * @return 是否操作成功
     */
    boolean insert(IconDO iconDO);

}
