package com.zw.app.domain.expireDate;

import com.zw.app.entity.monitor.AppExpireRemindDetailQueryEntity;
import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;

/***
 @Author zhengjc
 @Date 2019/11/25 9:43
 @Description 到期查询条件对象
 @version 1.0
 **/
@Data
public class AppExpireDateQuery extends BaseQueryBean {

    public static AppExpireDateQuery getInstance(AppExpireRemindDetailQueryEntity entity) {
        AppExpireDateQuery appExpireDateQuery = new AppExpireDateQuery();
        appExpireDateQuery.setPage(entity.getPage());
        appExpireDateQuery.setLimit(entity.getLimit());
        return appExpireDateQuery;
    }

}
