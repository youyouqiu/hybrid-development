package com.zw.platform.service.functionconfig;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.functionconfig.FenceInfo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Title: 围栏service
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 * @version 1.0
 * @author: wangying
 * @date 2016年8月4日下午2:14:48
 */
public interface FenceService {

    /**
         * @return List<DeviceInfo>
     * @throws @author wangying
     * @Title: 查询围栏
     */
    List<FenceInfo> findFence() throws Exception;

    /**
         * @return List<String>
     * @throws @author wangying
     * @Title: 查询围栏类型
     */
    List<String> findFenceType() throws Exception;

    /**
         * @param type
     * @return FenceInfo
     * @throws @author
     * @Title: 根据围栏类型和所属企业查询
     */
    List<Map<String, Object>> findFenceByType(String type, List<String> orgIds) throws Exception;

    FenceInfo findFenceInfoById(String id) throws Exception;

    int checkBindByOrgId(String id);

    /**
     * 获取围栏详情
     * @param id   围栏Id
     * @param type 围栏类型（对应表名）
     * @return 围栏详情
     */
    JSONObject getFenceDetail(String id, String type);

    /**
     * 查看围栏详情
     * @param id 围栏Id
     * @param type 围栏类型（对应表名）
     * @return 围栏详情
     */
    JSONObject previewFence(String id, String type);

}
