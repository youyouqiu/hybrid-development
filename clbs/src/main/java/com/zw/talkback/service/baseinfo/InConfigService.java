package com.zw.talkback.service.baseinfo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 对讲信息录入服务层
 */
public interface InConfigService {

    /**
     * 生成导入模板
     * @param response
     * @return boolean
     * @Title: generateTemplate
     * @author Liubangquan
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;

    /**
     * 判断群组下绑定对象的数量是否超限
     * @param id        群组ID或分组ID
     * @param type      1 群组 2 组织
     * @param monitorId 监控对象ID 可为空
     * @return 合法的群组ID
     */
    List<String> getAllAssignmentVehicleNumber(String id, int type, String monitorId);
}
