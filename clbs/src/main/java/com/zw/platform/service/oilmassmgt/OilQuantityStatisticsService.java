/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information
 * of ZhongWei, Inc. You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with ZhongWei.
 */

package com.zw.platform.service.oilmassmgt;


import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.oil.Positional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


/**
 * TODO 油量统计Service <p>Title: OilQuantityStatisticsService.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company:
 * ZhongWei</p> <p>team: ZhongWeiTeam</p>
 * @author: wangying
 * @date 2016年11月4日下午4:34:38
 * @version 1.0
 */
public interface OilQuantityStatisticsService {
    List<Positional> getOilMassInfo(String band, String startTime, String endTime) throws Exception;

    /**
     * 计算油量详情
     * @Title: getInfoDtails
     * @param list
     * @return
     * @return JSONObject
     * @throws @author
     *             Liubangquan
     */
    public JSONObject getInfoDtails(List<Positional> list, String vehicleId, Integer[] signal) throws Exception;

    /**
     * 导出油量报表
     * @param response
     * @param type
     * @param vehicleId
     * @throws IOException
     */
    void exportOilPagInfoList(HttpServletResponse response, int type, String vehicleId)
        throws IOException;
}
