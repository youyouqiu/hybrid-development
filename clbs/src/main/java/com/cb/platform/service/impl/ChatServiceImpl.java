package com.cb.platform.service.impl;

import com.cb.platform.domain.ChatDo;
import com.cb.platform.domain.query.ChatQuery;
import com.cb.platform.repository.mysqlDao.ChatDao;
import com.cb.platform.service.ChatService;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatDao chatDao;

    @Override
    public List<ChatDo> findAll(ChatQuery chatQuery) throws Exception {
        if (chatQuery != null && StringUtils.isNotEmpty(chatQuery.getFromUserName())) {
            chatQuery.setFromUserName(StringUtil.mysqlLikeWildcardTranslation(chatQuery.getFromUserName()));
        }
        if (chatQuery != null && StringUtils.isNotEmpty(chatQuery.getChatContent())) {
            chatQuery.setChatContent(StringUtil.mysqlLikeWildcardTranslation(chatQuery.getChatContent()));
        }
        return chatDao.findByQuery(chatQuery);
    }

    @Override
    public boolean export(String title, int type, HttpServletResponse res, List<ChatDo> chatDoList) throws Exception {
        return ExportExcelUtil
            .export(new ExportExcelParam(title, type, chatDoList, ChatDo.class, null, res.getOutputStream()));
    }
}
