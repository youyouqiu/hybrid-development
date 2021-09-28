package com.zw.platform.service.intercomplatform;


import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.zw.platform.domain.intercomplatform.IntercomPlatForm;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormConfig;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormConfigQuery;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormConfigView;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormQuery;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;

/**
 * Created by LiaoYuecai on 2017/3/3.
 */
public interface IntercomPlatFormService {
    List<IntercomPlatForm> findList(IntercomPlatFormQuery query, boolean doPage) throws Exception;

    /**
     * 增加对讲平台信息
     *
     */
    JsonResultBean add(IntercomPlatForm form, String ipAddress) throws Exception;

    /**
     * 根据id删除对讲平台信息
     */
    JsonResultBean deleteById(List<String> ids, String ipAddress) throws Exception;

    /**
     * 修改对讲平台信息
     */
    JsonResultBean update(IntercomPlatForm form, String ipAddress) throws Exception;

    /**
     * 根据id查询对讲平台信息
     *
     */
    IntercomPlatForm findById(String id) throws Exception;

    Page<IntercomPlatFormConfigView> findConfigViewList(IntercomPlatFormConfigQuery query) throws Exception;

    void addConfig(IntercomPlatFormConfig config);

    void deleteConfigById(String id);

    List<String> findConFigIdByVIds(List<String> vehicleIds);

    List<String> findConFigIdByPIds(List<String> pids);

    void updateConfigById(IntercomPlatFormConfig config);

    IntercomPlatFormConfigView findConfigViewByConfigId(String configId);

    JSONArray getVehicleTreeByPlatform() throws Exception;

    JSONArray getVehicleTreeByThirdPlatform() throws Exception;

}
