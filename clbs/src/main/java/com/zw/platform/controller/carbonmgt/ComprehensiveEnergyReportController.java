package com.zw.platform.controller.carbonmgt;


import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.domain.vas.carbonmgt.form.MobileSourceEnergyReportForm;
import com.zw.platform.domain.vas.carbonmgt.query.MileageQuery;
import com.zw.platform.domain.vas.carbonmgt.query.TimeEnergyStatisticsQuery;
import com.zw.platform.service.carbonmgt.ComprehensiveEnergyReportService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;


/**
 * 移动源能耗报表-综合能耗报表 <p>Title: ComprehensiveEnergyReportController.java</p> <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p> <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2017年2月14日下午1:50:24
 * @version 1.0
 */
@Controller
@RequestMapping("/v/carbonmgt/comprehensiveEnergyReport")
public class ComprehensiveEnergyReportController {
    private static Logger log = LogManager.getLogger(ComprehensiveEnergyReportController.class);

    private static final String LIST_PAGE = "vas/carbonmgt/mobileSorceEnergyReport/comprehensiveEnergyReport/list";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private TimeEnergyStatisticsQuery query = new TimeEnergyStatisticsQuery();

    @Autowired
    private ComprehensiveEnergyReportService comprehensiveEnergyReportService;

    @Autowired
    private ConfigService configService;

    /**
     * 初始化查询界面的查询条件
     * @Title: initQueryPage
     * @param groupId
     * @param brand
     * @return
     * @throws BusinessException
     * @return JsonResultBean
     * @throws @author
     *             Liubangquan
     */
    @RequestMapping(value = {"/initVehicleInfoList"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean initQueryPage(String groupId, String brand) {
        try {
            JSONObject msg = new JSONObject();
            // 初始化车辆信息
            msg.put("vehicleInfoList", comprehensiveEnergyReportService.getVehicleInfoList(groupId));
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("综合能耗报表获取车辆列表数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }

    /**
     * 里程能耗统计初始化界面
     * @Title: list
     * @return String
     * @author wangjianyu
     */
    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public ModelAndView queryByDatePage() {
        try {
            ModelAndView mav = new ModelAndView(LIST_PAGE);
            MileageQuery query = new MileageQuery();
            String[] curOrg = configService.getCurOrgId(); // 查询页面默认组织
            if (null != curOrg && curOrg.length > 0) {
                query.setGroupId(curOrg[0]);
                query.setGroupName(curOrg[1]);
            }
            mav.addObject("result", query);
            return mav;
        } catch (Exception e) {
            log.error("里程能耗统计初始化界面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 查询数据
     * @Title: getListPage
     * @param startDate
     * @param endDate
     * @param groupId
     * @param vehicleId
     * @param year
     * @param month
     * @return
     * @throws BusinessException
     * @return List<MileageForm>
     * @throws @author
     *             Liubangquan
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public List<MobileSourceEnergyReportForm> getListPage(String startDate, String endDate, String groupId,
                                                          String vehicleId, String year, String month)
        throws Exception {
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setBrand(vehicleId);
        query.setGroupId(groupId);
        query.setYear(year);
        query.setMonth(month);
        return comprehensiveEnergyReportService.queryByDate(startDate, endDate, groupId, vehicleId, year, month);
    }

}
