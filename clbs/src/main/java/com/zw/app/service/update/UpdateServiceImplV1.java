package com.zw.app.service.update;

import com.zw.app.annotation.AppMethodVersion;
import com.zw.app.annotation.AppServerVersion;
import com.zw.app.controller.AppVersionConstant;
import com.zw.app.domain.update.UpdateInfo;
import com.zw.app.entity.BaseEntity;
import com.zw.app.repository.mysql.update.UpdateAppDao;
import com.zw.app.util.common.AppResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/***
 @Author gfw
 @Date 2018/12/11 13:57
 @Description 获取平台最新版本实现
 @version 1.0
 **/
@Service
@AppServerVersion
public class UpdateServiceImplV1 implements UpdateService {
    @Autowired
    UpdateAppDao updateAppDao;

    @Value("${app.minimum.version}")
    private Integer miniVersion;

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_ONE, url = "/clbs/app/update/version")
    public AppResultBean getVersion(BaseEntity baseEntity) {
        List<UpdateInfo> versionInfo = updateAppDao.getVersionInfo(baseEntity.getPlatform());
        if (versionInfo != null && versionInfo.size() != 0) {
            UpdateInfo updateInfo = versionInfo.get(0);
            if (updateInfo.getVersion().equals(baseEntity.getVersion())) {
                return new AppResultBean("当前版本已是最新版本");
            } else {
                HashMap<String, Object> map = new HashMap<>(20);
                map.put("version", updateInfo.getVersion());
                map.put("message", updateInfo.getUpdateMessage());
                map.put("url", updateInfo.getAppUrl());
                return new AppResultBean(map);
            }
        }
        return new AppResultBean("数据出错");
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_ONE, url = "/clbs/app/update/force")
    public AppResultBean getnewestVersion(BaseEntity baseEntity) {
        HashMap<String, Object> map = new HashMap<>(20);
        if (baseEntity.getVersion().compareTo(miniVersion) >= 0) {
            map.put("flag", true);
            return new AppResultBean(map);
        } else {
            map.put("flag", false);
            List<UpdateInfo> versionInfo = updateAppDao.getVersionInfo(baseEntity.getPlatform());
            if (versionInfo != null && versionInfo.size() != 0) {
                UpdateInfo updateInfo = versionInfo.get(0);
                map.put("message", updateInfo.getUpdateMessage());
                map.put("url", updateInfo.getAppUrl());
                map.put("version", updateInfo.getVersion());
                return new AppResultBean(map);
            } else {
                return new AppResultBean("数据出错");
            }
        }
    }
}
