package com.zw.platform.service.personalized;

import com.zw.platform.domain.basicinfo.form.FastNavConfigForm;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;

public interface FastNavConfigService {

    List<FastNavConfigForm> getList(String userId);

    JsonResultBean add(FastNavConfigForm fastNavConfigForm, String ipAddress) throws Exception;

    JsonResultBean delete(String userId, String order, String ipAddress) throws Exception;

    FastNavConfigForm findBySort(String userId, String order);

    /**
     * 修改导航顺序
     * @param editOrder   修改的导航位置顺序
     * @param editedOrder 被修改的导航位置顺序
     * @param editId      修改的导航id
     * @param editedId    被修改的导航id
     * @param userId      用户id
     * @return json
     * @throws Exception e
     */
    JsonResultBean updateOrders(String editOrder, String editedOrder, String editId, String editedId, String userId)
        throws Exception;
}
