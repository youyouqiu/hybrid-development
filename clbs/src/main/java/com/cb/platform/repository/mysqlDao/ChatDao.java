package com.cb.platform.repository.mysqlDao;

import com.cb.platform.domain.ChatDo;
import com.cb.platform.domain.query.ChatQuery;

import java.util.List;

/**
 *
 */
public interface ChatDao {

    /**
     * 查询聊天记录
     * @param query 查询条件
     * @return
     * @throws Exception
     */
    List<ChatDo> findByQuery(final ChatQuery query)throws  Exception;

}
