package com.zw.platform.basic.repository;

import com.zw.platform.basic.domain.FriendDO;
import com.zw.platform.basic.dto.FriendDTO;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 好友管理
 * @author zhangjuan
 */
public interface FriendDao {
    /**
     * 获取用户好友列表
     * @param userId 用户Id
     * @return 好友列表
     */
    List<FriendDTO> getByUserId(@Param("userId") Long userId);

    /**
     * 删除用户好友
     * @param userId 用户Id
     * @return 操作结果
     */
    boolean deleteByUserId(@Param("userId") Long userId);

    /**
     * 添加用户好友
     * @param friends 好友
     * @return 是否添加成功
     */
    boolean insert(@Param("friends") Collection<FriendDO> friends);
}


