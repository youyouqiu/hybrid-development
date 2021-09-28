package com.zw.platform.controller.carbonmgt;


import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.domain.vas.carbonmgt.form.MobileSourceEnergyReportForm;
import com.zw.platform.domain.vas.carbonmgt.query.MileageQuery;
import com.zw.platform.service.carbonmgt.MileageEnergyReportService;
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


/**
 * 移动源能耗报表-里程能耗报表 <p>Title: MileageEnergyReportController.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company:
 * ZhongWei</p> <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2017年2月14日下午1:46:28
 * @version 1.0
 */
@Controller
@RequestMapping("/v/carbonmgt/mileageEnergyReport")
public class MileageEnergyReportController {

    private static Logger log = LogManager.getLogger(MileageEnergyReportController.class);

    private static final String LIST_PAGE = "vas/carbonmgt/mobileSorceEnergyReport/mileageEnergyReport/list";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private MileageQuery query = new MileageQuery();

    @Autowired
    private MileageEnergyReportService mileageEnergyReportService;

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
        JSONObject msg = new JSONObject();
        // 初始化车辆信息
        try {
            msg.put("vehicleInfoList", mileageEnergyReportService.getVehicleInfoList(groupId));
        } catch (Exception e) {
            log.error("获取车辆列表数据失败", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        return new JsonResultBean(msg);
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
     * @param queryWay
     * @param startDate
     * @param endDate
     * @param groupId
     * @param vehicleId
     * @param year
     * @param month
     * @param quarter
     * @param group
     * @return
     * @throws BusinessException
     * @return List<MileageForm>
     * @throws @author
     *             Liubangquan
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public List<MobileSourceEnergyReportForm> getListPage(String queryWay, String startDate, String endDate,
                                                          String groupId, String vehicleId, String year, String month,
                                                          String quarter, String group)
        throws Exception {
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setBrand(vehicleId);
        query.setQueryWay(queryWay);
        query.setGroupId(groupId);
        query.setGroup(group);
        query.setYear(year);
        query.setMonth(month);
        query.setQuarter(quarter);
        return mileageEnergyReportService.queryByDate(queryWay, startDate, endDate, groupId, vehicleId);
    }

    /**
     * 导出
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
    public void export(HttpServletResponse response, HttpServletRequest request) {
        try {
            // thingInfoService.exportThingInfo(null, 0, null);
            String filename = "";
            if (query.getQueryWay().equals("list1")) {
                filename = "里程能耗列表";
            } else if (query.getQueryWay().equals("list2")) {
                filename = "里程能耗日报表";
            } else if (query.getQueryWay().equals("list3")) {
                filename = "里程能耗月报表";
            } else if (query.getQueryWay().equals("list4")) {
                filename = "里程能耗季度报表";
            } else if (query.getQueryWay().equals("list5")) {
                filename = "里程能耗年报表";
            }
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            if (query.getQueryWay().equals("list1")) {
                mileageEnergyReportService.exportMileage("里程能耗列表", 1, response, query);
            } else if (query.getQueryWay().equals("list2")) {
                mileageEnergyReportService.exportMileage("里程能耗日报表", 1, response, query);
            } else if (query.getQueryWay().equals("list3")) {
                mileageEnergyReportService.exportMileage("里程能耗月报表", 1, response, query);
            } else if (query.getQueryWay().equals("list4")) {
                mileageEnergyReportService.exportMileage("里程能耗季度报表", 1, response, query);
            } else if (query.getQueryWay().equals("list5")) {
                mileageEnergyReportService.exportMileage("里程能耗年报表", 1, response, query);
            }
        } catch (Exception e) {
            log.error("导出里程能耗报表异常", e);
        }
    }

}
