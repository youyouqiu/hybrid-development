package com.zw.app.repository.mysql.webMaster.feedBack;

import com.alibaba.fastjson.JSONObject;
import com.zw.app.domain.webMaster.feedBack.FeedBack;
import com.zw.app.domain.webMaster.feedBack.FeedBackQuery;

import java.util.List;

/**
 * @author lijie
 * @date 2018/8/29 16:11
 */
public interface AppFeedBackDao {

    Integer getFeedBackTotal(FeedBackQuery feedBackQuery);//获取数据的总数

    List<FeedBack> searchFeedBack(FeedBackQuery feedBackQuery);//分页查询

    Boolean addFeedBack(JSONObject jsonObject);//添加意见反馈的信息入数据库

}
