package com.zw.platform.repository.oilsubsidy;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.zw.platform.domain.oilsubsidy.line.LineDO;

/**
 * @author wanxing
 * @Title: crud增删改类
 * @date 2020/10/915:46
 */
public interface CrudDao<D> {
    /**
     * 添加
     * @param d
     * @return
     */
    boolean add(D d);

    /**
     * 添加
     * @param list
     * @return
     */
    boolean addBatch(@Param("list") List<D> list);

    /**
     * 修改
     * @param d
     * @return
     */
    boolean update(D d);

    /**
     * 删除
     * @param id
     * @return
     */
    boolean delete(String id);

    /**
     * 删除
     * @param id
     * @return
     */
    boolean deleteBatch(@Param("list") Collection<String> id);

    /**
     * 获取
     * @param id
     * @return
     */
    LineDO getAllFieldById(String id);

}
