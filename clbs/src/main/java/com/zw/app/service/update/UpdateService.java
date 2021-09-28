package com.zw.app.service.update;

import com.zw.app.entity.BaseEntity;
import com.zw.app.util.common.AppResultBean;

/***
 @Author gfw
 @Date 2018/12/11 13:55
 @Description 版本更新接口
 @version 1.0
 **/
public interface UpdateService {
    /**
     * 获取APP最新版本
     * @param platForm android/ios
     * @return
     */
    AppResultBean getVersion(BaseEntity baseEntity);
    AppResultBean getnewestVersion(BaseEntity baseEntity);
}
