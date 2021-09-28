package com.zw.platform.service.connectionparamsset_809;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.domain.connectionparamsset_809.T809ForwardConfig;
import com.zw.platform.domain.connectionparamsset_809.T809ForwardConfigQuery;

import java.util.List;

/**
 * 809监控对象转发管理service
 * @author hujun
 */
public interface ConnectionParamsConfigService {
    /**
     * 查询809转发绑定关系数据
     * @author hujun
     */
    Page<T809ForwardConfig> findConfig(T809ForwardConfigQuery query) throws Exception;
    
    /**
     * 保存809转发绑定关系数据
     * @author hujun
     */
    boolean addConfig(String platFormId, String vehicleId, String ipAddress, String platFormName, String protocolType)
        throws Exception;
    
    /**
     * 删除809转发绑定关系数据
     * @author hujun
     */
    boolean deleteConfig(List<String> configIds, String ipAddress) throws Exception;

    /**
     * 获取809监控对象转发树
     * @author hujun
     */
    JSONObject getT809ForwardTree();

    /**
     * 根据车辆绑定关系id查找车辆转发绑定关系id
     * @author hujun
     */
    List<String> findConfigIdByVConfigIds(List<String> vcids);
}
