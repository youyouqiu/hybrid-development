package com.zw.platform.repository.core;

import com.zw.platform.domain.core.CustomColumnConfigInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author zhouzongbo on 2019/3/11 15:26
 */
public interface CustomColumnDao {

    /**
     * 根据功能标识查询默认展示字段
     * @param mark   mark
     * @param status 列状态: 0：默认列;  1:非默认列
     * @return
     */
    List<CustomColumnConfigInfo> findDefaultCustomConfigByMark(@Param("mark") String mark,
        @Param("status") Integer status);

    /**
     * 查询所有列
     * @param status status
     * @return
     */
    List<CustomColumnConfigInfo> findAllCustomColumnInfo(@Param("status") Integer status);

    List<CustomColumnConfigInfo> findCustomColumnConfigInfo(@Param("userId") String userId, @Param("mark") String mark);

    Boolean deleteCustomColumnConfig(@Param("userId") String userId, @Param("mark") String mark);

    Boolean addCustomColumnConfigList(@Param("customList") List<CustomColumnConfigInfo> customColumnConfigInfoList);

    /**
     * 查询用户的个性化列设置
     * @param columnModule columnModule
     * @param userId       userId
     * @return list
     */
    List<CustomColumnConfigInfo> findUserCustomColumnInfo(@Param("userId") String userId,
        @Param("columnModule") String columnModule);

    /**
     * 删除用户下自定义列信息
     * @param userId userId
     * @return
     */
    Boolean deleteAllCustomColumnConfig(@Param("userId") String userId);

    /**
     * 删除用户下自定义列信息,按照mark进行删除
     * @param userId userId
     * @return
     */
    Boolean deleteAllCustomColumnConfigByMarks(@Param("userId") String userId, @Param("marks") Set<String> marks);

    Set<String> getAllMark();

    void deleteUserCustomColumn(@Param("userId") String userId, @Param("columnId") String columnId);

}
