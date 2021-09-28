package com.zw.app.service.monitor;

import com.zw.app.domain.expireDate.AppExpireDateEntity;
import com.zw.app.entity.monitor.AppExpireRemindDetailQueryEntity;

import java.util.List;
import java.util.Map;

public interface AppExpireRemindService {
    /**
     * 查询每种到期的总数
     * @param userName
     * @return
     */
    Map<String, Object> getExpireRemindInfos(String userName);

    /**
     * 查询每种到期的具体详情
     * @return
     */
    List<AppExpireDateEntity> getExpireRemindInfoDetails(
        AppExpireRemindDetailQueryEntity appExpireRemindDetailQueryEntity);
}
