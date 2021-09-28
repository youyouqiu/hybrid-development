package com.zw.app.service.webMaster.feedBack.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.zw.app.domain.webMaster.feedBack.FeedBack;
import com.zw.app.domain.webMaster.feedBack.FeedBackQuery;
import com.zw.app.repository.mysql.webMaster.feedBack.AppFeedBackDao;
import com.zw.app.service.webMaster.feedBack.AppFeedBackService;
import com.zw.app.util.AppParamCheckUtil;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author lijie
 * @date 2018/8/29 16:09
 */
@Service
public class AppFeedBackServiceImpl implements AppFeedBackService {
    @Autowired
    AppFeedBackDao appFeedBackDao;

    @Autowired
    private UserService userService;

    /**
     * 分页查询app意见反馈
     * @author lijie
     * @date 2018/8/29 16:09
     */
    @Override
    public List<FeedBack> searchFeedBack(FeedBackQuery feedBackQuery, boolean doPage) {
        if (!AppParamCheckUtil.checkDate(feedBackQuery.getStartTime(), 1) || !AppParamCheckUtil
            .checkDate(feedBackQuery.getEndTime(), 1)) {
            return null;
        }
        return doPage
                ? PageHelper.startPage(feedBackQuery.getPage().intValue(), feedBackQuery.getLimit().intValue())
                        .doSelectPage(() -> appFeedBackDao.searchFeedBack(feedBackQuery))
                : appFeedBackDao.searchFeedBack(feedBackQuery);
    }

    /**
     * 发送意见反馈
     * @author lijie
     * @date 2018/9/4 18:09
     */
    @Override
    public Boolean sendFeedBack(String feedback) {
        String userName = SystemHelper.getCurrentUser().getUsername();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", UUID.randomUUID().toString());
        jsonObject.put("feedback", feedback);
        jsonObject.put("submitDate", new Date());
        jsonObject.put("userName", userName);
        return appFeedBackDao.addFeedBack(jsonObject);
    }

    @Override
    public boolean listExport(FeedBackQuery feedBackQuery, HttpServletResponse res) throws Exception {
        List<FeedBack> list = searchFeedBack(feedBackQuery, false);
        String userId = userService.getCurrentUserInfo().getId().toString();
        RedisKey key = HistoryRedisKeyEnum.APP_FEED_BACK.of(userId);
        RedisHelper.delete(key);
        RedisHelper.addObjectToList(key, list, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        return true;
    }

    @Override
    public boolean export(String title, int type, HttpServletResponse res) throws Exception {
        String userId = userService.getCurrentUserInfo().getId().toString();
        List<FeedBack> feedBackList =
                RedisHelper.getListObj(HistoryRedisKeyEnum.APP_FEED_BACK.of(userId), 0, -1);
        if (feedBackList == null) {
            return false;
        }
        return ExportExcelUtil.export(
                new ExportExcelParam(title, type, feedBackList, FeedBack.class, null, res.getOutputStream()));
    }
}
