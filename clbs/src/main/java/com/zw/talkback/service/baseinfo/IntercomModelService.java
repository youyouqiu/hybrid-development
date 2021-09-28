package com.zw.talkback.service.baseinfo;

import com.zw.platform.util.common.JsonResultBean;
import com.zw.talkback.domain.intercom.form.IntercomModelForm;
import com.zw.talkback.domain.intercom.info.IntercomModelInfo;
import com.zw.talkback.domain.intercom.info.OriginalModelInfo;
import com.zw.talkback.domain.intercom.query.IntercomModelQuery;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 对讲机型service
 */
public interface IntercomModelService {

    /**
     * 新增对讲机型
     * @param form
     */
    JsonResultBean addIntercomModel(IntercomModelForm form, String ipAddress);

    /**
     * 更新对讲机型
     * @param form
     */
    JsonResultBean updateIntercomModel(IntercomModelForm form, String ipAddress);

    void exportIntercomModels(IntercomModelQuery query, HttpServletResponse response) throws IOException;

    List<OriginalModelInfo> getIntercomModels(IntercomModelQuery query);

    /**
     * 删除对讲机型
     * @param id
     */
    JsonResultBean deleteIntercomModelById(String id, String ipAddress);

    /**
     * 删除对讲机型
     * @param ids
     */
    JsonResultBean deleteIntercomModelByIds(String ids, String ipAddress);

    /**
     * 根据id获取对讲机型
     * @param id
     * @return
     */
    IntercomModelInfo getIntercomModelById(String id);

}
