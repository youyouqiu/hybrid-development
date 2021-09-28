package com.zw.talkback.repository.mysql;

import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/***
 @Author zhengjc
 @Date 2019/8/1 16:17
 @Description 组合和个呼号码
 @version 1.0
 **/
public interface CallNumberDao {

    /**
     * 更新个呼号码
     * @param number 个呼号码
     * @param status 0代表
     */
    void updatePersonCallNumber(@Param("number") String number, @Param("status") Byte status);

    /**
     * @param number
     * @param status
     */
    void updateGroupCallNumber(@Param("number") String number, @Param("status") Byte status);

    /**
     * 查询所有的可以用的个呼号码
     * @return
     */
    List<String> getAllAvailablePersonCallNumber();

    /**
     * 查询所有的可以用的组呼号码
     * @return
     */
    List<String> getAllAvailableGroupCallNumber();

    /**
     * 批量更新个呼号码
     * @param personNumbers 个呼号码
     * @param status        0代表
     */
    void updatePersonCallNumberBatch(@Param("personNumbers") Collection<String> personNumbers,
        @Param("status") Byte status);

    /**
     * 初始化默认组呼和个呼号码
     */
    void addCallNumbers(@Param("callNumbers")List<Integer> callNumbers);

    /**
     * 检验组呼和个呼号码是否已经初始化了
     * @return
     */
    long checkCallNumber();
}
