package com.zw.platform.controller.carbonmgt;


import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.domain.vas.carbonmgt.form.TimeEnergyStatisticsForm;
import com.zw.platform.domain.vas.carbonmgt.query.TimeEnergyStatisticsQuery;
import com.zw.platform.service.carbonmgt.TimeEnergyStatisticsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.List;


@Controller
@RequestMapping("/v/carbonmgt/timestatistics")
public class TimeEnergyStatisticsController {
    private static Logger log = LogManager.getLogger(TimeEnergyStatisticsController.class);

    private static final String QUERY_BY_DATE_PAGE = "vas/carbonmgt/timeEnergy/timeEnergyStatistics";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private TimeEnergyStatisticsQuery query = new TimeEnergyStatisticsQuery();

    @Autowired
    private TimeEnergyStatisticsService timeEnergyStatisticsService;

    @Autowired
    private ConfigService configService;

    /**
     * 初始化查询界面的查询条件-车辆列表
     * @Title: initQueryPage
     * @return
     * @throws BusinessException
     * @return JsonResultBean
     * @author Liubangquan
     */
    @RequestMapping(value = {"/initVehicleInfoList"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean initQueryPage(String groupId, String brand)
        throws BusinessException {
        JSONObject msg = new JSONObject();
        // 初始化车辆信息
        try {
            msg.put("vehicleInfoList", timeEnergyStatisticsService.getVehicleInfoList(groupId));
        } catch (Exception e) {
            log.error("error", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        return new JsonResultBean(msg);
    }

    /**
     * 时间能耗统计初始化界面
     * @Title: queryByDatePage
     * @return
     * @throws BusinessException
     * @return String
     * @author Liubangquan
     */
    @Auth
    @RequestMapping(value = {"/queryByDatePage"}, method = RequestMethod.GET)
    public ModelAndView queryByDatePage() {
        try {
            ModelAndView mav = new ModelAndView(QUERY_BY_DATE_PAGE);
            String[] curOrg = configService.getCurOrgId(); // 查询页面默认组织
            TimeEnergyStatisticsQuery query = new TimeEnergyStatisticsQuery();
            if (null != curOrg && curOrg.length > 0) {
                query.setGroupId(curOrg[0]);
                query.setGroupName(curOrg[1]);
            }
            mav.addObject("result", query);
            return mav;
        } catch (Exception e) {
            log.error("时间能耗统计初始化界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 按日期查询
     * @Title: getListPage
     * @param query
     * @return
     * @throws BusinessException
     * @return PageGridBean
     * @throws BusinessException
     * @author Liubangquan
     */
    @RequestMapping(value = {"/queryByDatePage"}, method = RequestMethod.POST)
    @ResponseBody
    public List<TimeEnergyStatisticsForm> getListPage(String queryWay, String startDate, String endDate, String groupId,
                                                      String vehicleId, String year, String month, String quarter)
        throws BusinessException {
        query.setQueryWay(queryWay);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setGroupId(groupId);
        query.setBrand(vehicleId);
        query.setYear(year);
        query.setMonth(month);
        query.setQuarter(quarter);
        List<TimeEnergyStatisticsForm> list = timeEnergyStatisticsService.queryByDate(queryWay, startDate, endDate,
            groupId, vehicleId);
        return list;
    }

    /**
     * 时间能耗统计导出列表
     * @Title: export
     * @param response
     * @param request
     * @throws UnsupportedEncodingException
     * @return void
     * @throws @author
     *             Liubangquan
     */
    @RequestMapping(value = "/export.gsp", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletResponse response, HttpServletRequest request)
        throws UnsupportedEncodingException {
        String filename = "";
        if (query.getQueryWay().equals("list1")) {
            filename = "时间能耗列表";
        } else if (query.getQueryWay().equals("list2")) {
            filename = "时间能耗日报表";
        } else if (query.getQueryWay().equals("list3")) {
            filename = "时间能耗月报表";
        } else if (query.getQueryWay().equals("list4")) {
            filename = "时间能耗季度报表";
        } else if (query.getQueryWay().equals("list5")) {
            filename = "时间能耗年报表";
        }
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition",
            "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
        response.setContentType("application/msexcel;charset=UTF-8");
        if (query.getQueryWay().equals("list1")) {
            try {
                timeEnergyStatisticsService.export("时间能耗列表", 1, response, query);
            } catch (Exception e) {
                log.error("error", e);
            }
        } else if (query.getQueryWay().equals("list2")) {
            timeEnergyStatisticsService.export("时间能耗日报表", 1, response, query);
        } else if (query.getQueryWay().equals("list3")) {
            timeEnergyStatisticsService.export("时间能耗月报表", 1, response, query);
        } else if (query.getQueryWay().equals("list4")) {
            timeEnergyStatisticsService.export("时间能耗季度报表", 1, response, query);
        } else if (query.getQueryWay().equals("list5")) {
            timeEnergyStatisticsService.export("时间能耗年报表", 1, response, query);
        }
    }

}
