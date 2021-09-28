package com.cb.platform.service;

import com.cb.platform.domain.ChatDo;
import com.cb.platform.domain.query.ChatQuery;
import com.github.pagehelper.Page;
import com.zw.platform.domain.reportManagement.SpeedAlarm;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 聊天记录业务接口
 */
public interface ChatService {

    /**
     * 根据查询条件查询聊天记录
     * @param chatQuery
     * @return
     * @throws Exception
     */
    public List<ChatDo> findAll(ChatQuery chatQuery)throws  Exception;

    /**
     * 导出聊天记录统计列表
     * @param title
     * @param type 导出类型（1:导出数据；2：导出模板）
     * @param res
     * @param chatDoList
     * @return
     */
    boolean export(String title, int type, HttpServletResponse res, List<ChatDo> chatDoList) throws Exception;
}
