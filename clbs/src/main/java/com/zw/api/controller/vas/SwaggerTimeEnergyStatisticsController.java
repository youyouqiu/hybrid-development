package com.zw.api.controller.vas;

import com.zw.platform.domain.vas.carbonmgt.form.TimeEnergyStatisticsForm;
import com.zw.platform.domain.vas.carbonmgt.query.TimeEnergyStatisticsQuery;
import com.zw.platform.service.carbonmgt.TimeEnergyStatisticsService;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.util.common.BusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@RequestMapping("/swagger/v/carbonmgt/timestatistics")
@Api(tags = { "时间能耗报表" }, description = "时间能耗报表相关api")
public class SwaggerTimeEnergyStatisticsController {
    private static Logger log = LogManager.getLogger(SwaggerTimeEnergyStatisticsController.class);
    private static final String QUERY_BY_DATE_PAGE = "vas/carbonmgt/timeEnergy/timeEnergyStatistics";

    private TimeEnergyStatisticsQuery query = new TimeEnergyStatisticsQuery();

    @Autowired
    private TimeEnergyStatisticsService timeEnergyStatisticsService;

    @Autowired
    private ConfigService configService;

    /**
     * 按日期查询
     * @param query
     * @return PageGridBean
     * @throws BusinessException
     * @throws BusinessException
     * @Title: getListPage
     * @author Liubangquan
     */
    @ApiOperation(value = "获取时间能耗列表", notes = "请按照查询方式输入对应的查询条件,如按日期统计需输入页数，每页显示条数，开始时间，结束时间，组织id，车辆id;其它查询方式以此类推。",
        authorizations = { @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
                scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "startDate", value = "开始时间(如2017-02-14 00:00:00)", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "endDate", value = "结束时间(如2017-02-14 23:00:00)", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "组织id(如ou=zw,ou=organization)", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "String"),
        @ApiImplicitParam(name = "year", value = "年(如2017)", required = true, paramType = "query", dataType = "String"),
        @ApiImplicitParam(name = "month", value = "月(如2)", required = true, paramType = "query", dataType = "String"),
        @ApiImplicitParam(name = "quarter", value = "季度(如2)", required = true, paramType = "query",
            dataType = "String"), @ApiImplicitParam(name = "queryWay",
            value = "查询方式(list1:按日期查询;list2:按日期统计;list3:按月份统计;list4:按季度统计;" + "list5:按年份统计)", required = true,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = { "/queryByDatePage" }, method = RequestMethod.POST)
    @ResponseBody
    public List<TimeEnergyStatisticsForm> getListPage(String queryWay, String startDate, String endDate, String groupId,
        String vehicleId, String year, String month, String quarter) throws BusinessException {
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
     * @param response
     * @param request
     * @return void
     * @throws UnsupportedEncodingException
     * @throws
     * @Title: export
     * @author Liubangquan
     */
    @ApiOperation(value = "导出时间能耗列表", notes = "请先执行查询方法(获取时间能耗列表)后，再执行此方法方可导出", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/export.gsp", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletResponse response, HttpServletRequest request) throws UnsupportedEncodingException {
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
