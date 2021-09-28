package com.zw.api2.controller.energymanager;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.vas.carbonmgt.form.TimeEnergyStatisticsForm;
import com.zw.platform.domain.vas.carbonmgt.query.TimeEnergyStatisticsQuery;
import com.zw.platform.service.carbonmgt.TimeEnergyStatisticsService;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
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
@RequestMapping("/api/v/carbonmgt/timestatistics")
@Api(tags = {"时间能耗报表_dev"}, description = "时间能耗报表相关api")
public class ApiTimeEnergyStatisticsController {

    private static Logger log = LogManager.getLogger(ApiTimeEnergyStatisticsController.class);

    private static final String QUERY_BY_DATE_PAGE = "vas/carbonmgt/timeEnergy/timeEnergyStatistics";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private TimeEnergyStatisticsQuery query = new TimeEnergyStatisticsQuery();

    @Autowired
    private TimeEnergyStatisticsService timeEnergyStatisticsService;

    @Autowired
    private ConfigService configService;

    /**
     * 初始化查询界面的查询条件-车辆列表
     *
     * @return JsonResultBean
     * @throws BusinessException
     * @Title: initQueryPage
     * @author Liubangquan
     */
    @RequestMapping(value = {"/initVehicleInfoList"}, method = RequestMethod.POST)
    @ApiOperation(value = "通过企业ID获取车辆集合IDS", authorizations = {
            @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
                    @AuthorizationScope(scope = "global", description = "des")})})
    @ResponseBody
    public JsonResultBean initQueryPage(@ApiParam(value = "企业ID", required = true) String groupId)
            throws BusinessException {
        JSONObject msg = new JSONObject();
        // 初始化车辆信息
        try {
            msg.put("vehicleInfoList", timeEnergyStatisticsService.getVehicleInfoList(groupId));
        } catch (Exception e) {
            log.error("error", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
        return new JsonResultBean(msg);
    }

    /**
     * 时间能耗统计初始化界面
     *
     * @return String
     * @throws BusinessException
     * @Title: queryByDatePage
     * @author Liubangquan
     */
    @Auth
    @RequestMapping(value = {"/queryByDatePage"}, method = RequestMethod.GET)
    @ApiOperation(value = "通过企业ID获取车辆集合IDS", authorizations = {
            @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
                    @AuthorizationScope(scope = "global", description = "des")})})
    public JsonResultBean queryByDatePage() {
        try {
            ModelAndView mav = new ModelAndView(QUERY_BY_DATE_PAGE);
            String[] curOrg = configService.getCurOrgId(); // 查询页面默认组织
            TimeEnergyStatisticsQuery query = new TimeEnergyStatisticsQuery();
            if (null != curOrg && curOrg.length > 0) {
                query.setGroupId(curOrg[0]);
                query.setGroupName(curOrg[1]);
            }
            mav.addObject("result", query);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("时间能耗统计初始化界面弹出异常", e);
            return new JsonResultBean(e.getMessage());
        }
    }

    /**
     * 按日期查询
     *
     * @param query
     * @return PageGridBean
     * @throws BusinessException
     * @throws BusinessException
     * @Title: getListPage
     * @author Liubangquan
     */
    @RequestMapping(value = {"/queryByDatePage"}, method = RequestMethod.POST)
    @ApiOperation(value = "获取时间能耗报表", authorizations = {
            @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
                    @AuthorizationScope(scope = "global", description = "des")})})
    @ResponseBody
    public List<TimeEnergyStatisticsForm> getListPage(@ApiParam(value = "查询方式", required = true) String queryWay,
                                                      @ApiParam(value = "开始时间", required = true) String startDate,
                                                      @ApiParam(value = "结束时间", required = true) String endDate,
                                                      @ApiParam(value = "企业ID", required = true) String groupId,
                                                      @ApiParam(value = "车辆ID", required = true) String vehicleId,
                                                      @ApiParam(value = "年份", required = true) String year,
                                                      @ApiParam(value = "月份", required = true) String month,
                                                      @ApiParam(value = "季度", required = true) String quarter,
                                                      @ApiParam(value = "页数", required = true) Long page,
                                                      @ApiParam(value = "每页显示多少条", required = true) Long limit) throws BusinessException {
        query.setQueryWay(queryWay);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setGroupId(groupId);
        query.setBrand(vehicleId);
        query.setYear(year);
        query.setMonth(month);
        query.setQuarter(quarter);
        List<TimeEnergyStatisticsForm> list =
                timeEnergyStatisticsService.queryByDate(queryWay, startDate, endDate, groupId, vehicleId);
        return list;
    }

    /**
     * 时间能耗统计导出列表
     *
     * @param response
     * @param request
     * @return void
     * @throws UnsupportedEncodingException
     * @throws @author                      Liubangquan
     * @Title: export
     */
    @RequestMapping(value = "/export.gsp", method = RequestMethod.GET)
    @ResponseBody
    @Deprecated
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
