package com.zw.platform.service.basicinfo;

import com.zw.platform.basic.dto.F3SimCardDTO;
import com.zw.platform.domain.basicinfo.SimcardInfo;
import com.zw.platform.domain.basicinfo.query.SimcardQuery;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * sim卡管理service
 * @author wangying
 */
public interface SimcardService {


    /**
     * 根据id查询sim卡信息
     */
    SimcardInfo findSimcardById(String id);

    /**
     * 根据id查询sim卡信息
     */
    SimcardInfo findVehicleBySimcardNumber(String simcardNumber);

    /**
     * 生成导入模板
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;

    /**
     * 根据当前登录用户查询其组织下的simcard，如果绑定了车的需显示车牌号
     * @author Fan Lu
     */
    List<Map<String, Object>> findSimCardByUser(SimcardQuery query);

    /**
     * 查询simcard所属组织
     * @param id id
     * @return simcard信息及其组织
     * @author Fan Lu
     */
    Map<String, Object> findSimcardGroupById(String id);

    SimcardInfo findBySIMCard(String simcardNumber);

    /**
     * 查询simcard是否已经绑定组织
     * @param id id
     * @return 是否绑定组织
     * @author Fan Lu
     */
    int getIsBand(String id);

    SimcardInfo isExist(String id, String simcardNumber);

    F3SimCardDTO getF3SimInfo(String id);
}
