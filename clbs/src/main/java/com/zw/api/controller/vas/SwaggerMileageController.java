package com.zw.api.controller.vas;

import com.zw.platform.domain.vas.carbonmgt.form.MileageForm;
import com.zw.platform.domain.vas.carbonmgt.query.MileageQuery;
import com.zw.platform.service.carbonmgt.MileageService;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.util.StrUtil;
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
@RequestMapping("/swagger/v/carbonmgt/mileage")
@Api(tags = { "里程能耗报表" }, description = "里程能耗报表相关api")
public class SwaggerMileageController {
    private static Logger log = LogManager.getLogger(SwaggerMileageController.class);
    private static final String LIST_PAGE = "vas/carbonmgt/mileage/list";
    private MileageQuery query = new MileageQuery();

    @Autowired
    private MileageService mileageService;

    @Autowired
    private ConfigService configService;

    /**
     * 查询数据
     */
    @ApiOperation(value = "获取里程能耗列表",
        notes = "请按照查询方式输入对应的查询条件,如按日期统计需输入页数，" + "每页显示条数，开始时间，结束时间，组织id，车辆id;其它查询方式以此类推。", authorizations = {
            @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
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
            dataType = "string"),
        @ApiImplicitParam(name = "year", value = "年(如2017)", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "month", value = "月(如2)", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "quarter", value = "季度(如2)", required = true, paramType = "query",
            dataType = "string"), @ApiImplicitParam(name = "queryWay",
            value = "查询方式(list1:按日期查询;" + "list2:按日期统计;list3:按月份统计;list4:按季度统计;list5:按年份统计)", required = true,
            paramType = "query", dataType = "string") })
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public List<MileageForm> getListPage(String queryWay, String startDate, String endDate, String groupId,
        String vehicleId, String year, String month, String quarter, String group) throws BusinessException {
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setBrand(vehicleId);
        query.setQueryWay(queryWay);
        query.setGroupId(groupId);
        query.setGroup(group);
        query.setYear(year);
        query.setMonth(month);
        query.setQuarter(quarter);
        return mileageService.queryByDate(queryWay, startDate, endDate, groupId, vehicleId);
    }

    /**
     * 导出excel表
     * @throws UnsupportedEncodingException
     */
    @ApiOperation(value = "导出里程能耗列表", notes = "请先执行查询方法(获取里程能耗列表)后，再执行此方法方可导出", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/export.gsp", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletResponse response, HttpServletRequest request) throws UnsupportedEncodingException {
        //thingInfoService.exportThingInfo(null, 0, null);
        String filename = null;
        if (query.getQueryWay().equals("list1")) {
            filename = "里程能耗统计";
        } else if (query.getQueryWay().equals("list2")) {
            filename = "里程能耗日报表";
        } else if (query.getQueryWay().equals("list3")) {
            filename = "里程能耗月报表";
        } else if (query.getQueryWay().equals("list4")) {
            filename = "里程能耗季度报表";
        } else if (query.getQueryWay().equals("list5")) {
            filename = "里程能耗年报表";
        }
        if (StrUtil.isBlank(filename)) {
            return;
        }
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition",
            "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
        response.setContentType("application/msexcel;charset=UTF-8");
        if (query.getQueryWay().equals("list1")) {
            mileageService.exportMileage("里程能耗列表", 1, response, query);
        } else if (query.getQueryWay().equals("list2")) {
            mileageService.exportMileage("里程能耗日报表", 1, response, query);
        } else if (query.getQueryWay().equals("list3")) {
            mileageService.exportMileage("里程能耗月报表", 1, response, query);
        } else if (query.getQueryWay().equals("list4")) {
            mileageService.exportMileage("里程能耗季度报表", 1, response, query);
        } else if (query.getQueryWay().equals("list5")) {
            mileageService.exportMileage("里程能耗年报表", 1, response, query);
        }
    }
}
