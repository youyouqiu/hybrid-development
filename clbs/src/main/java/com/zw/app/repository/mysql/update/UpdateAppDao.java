package com.zw.app.repository.mysql.update;

import com.zw.app.domain.update.UpdateInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/***
 @Author gfw
 @Date 2018/12/11 13:59
 @Description 根据平台查询最新版本
 @version 1.0
 **/
public interface UpdateAppDao {
    /**
     * 根据平台判断APP是否更新
     * @param platform
     * @return
     */
    List<UpdateInfo> getVersionInfo(@Param("platform")String platform);
}
