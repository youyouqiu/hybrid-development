package com.zw.platform.service.carbonmgt;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.vas.carbonmgt.MobileSourceManage;
import com.zw.platform.domain.vas.carbonmgt.form.MobileSourceManageForm;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by 王健宇 on 2017/2/21.
 */
public interface MobileSourceManageService {
    List<Positional> mobileSourceManage(String band, String startTime, String endTime) throws Exception;

    boolean add(JSONObject msg, String band, String startTime, String endTime) throws Exception;

    boolean find(String band, String startTime, String endTime) throws Exception;

    List<MobileSourceManageForm> findList(String brand) throws Exception;

    boolean del(String band, String startTime, String endTime) throws Exception;

    /**
     * 导出
     */
    boolean exportMobileSourceManage(String title, int type, HttpServletResponse response,
        List<MobileSourceManage> mobileInfo) throws Exception;
}
