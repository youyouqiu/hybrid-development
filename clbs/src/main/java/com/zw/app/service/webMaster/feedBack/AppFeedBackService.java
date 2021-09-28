package com.zw.app.service.webMaster.feedBack;

import com.zw.app.domain.webMaster.feedBack.FeedBack;
import com.zw.app.domain.webMaster.feedBack.FeedBackQuery;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author lijie
 * @date 2018/8/29 16:09
 */
public interface AppFeedBackService {

    List<FeedBack> searchFeedBack(FeedBackQuery feedBackQuery, boolean doPage);//分页查询app意见反馈

    Boolean sendFeedBack(String feedback);//发送意见反馈

    boolean listExport(FeedBackQuery feedBackQuery, HttpServletResponse res) throws Exception;

    boolean export(String title, int type, HttpServletResponse res) throws Exception;

}
