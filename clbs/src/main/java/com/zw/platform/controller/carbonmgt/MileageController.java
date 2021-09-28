package com.zw.platform.controller.carbonmgt;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.domain.vas.carbonmgt.form.MileageForm;
import com.zw.platform.domain.vas.carbonmgt.query.MileageQuery;
import com.zw.platform.service.carbonmgt.MileageService;
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
 * 里程Controller Modification by Wjy on 2016/9/21.
 */
@Controller
@RequestMapping("/v/carbonmgt/mileage")
public class MileageController {
    private static Logger log = LogManager.getLogger(MileageController.class);

    private static final String LIST_PAGE = "vas/carbonmgt/mileage/list";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private MileageQuery query = new MileageQuery();

    @Autowired
    private MileageService mileageService;

    @Autowired
    private ConfigService configService;

    /*
     * @Auth
     * @RequestMapping(value = {"/list"}, method = RequestMethod.GET) public String listPage() throws BusinessException
     * { return LIST_PAGE; }
     */

    /**
     * 查询数据
     */
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
    @RequestMapping(value = "/export.gsp", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletResponse response, HttpServletRequest request)
        throws UnsupportedEncodingException {
        // thingInfoService.exportThingInfo(null, 0, null);
        String filename = "";
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

    /**
     * 初始化查询界面的查询条件
     * @return JsonResultBean
     * @throws BusinessException
     * @Title: initQueryPage
     * @author wjy
     */
    @RequestMapping(value = { "/initVehicleInfoList" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean initQueryPage(String groupId, String brand) throws BusinessException {
        JSONObject msg = new JSONObject();
        // 初始化车辆信息
        try {
            msg.put("vehicleInfoList", mileageService.getVehicleInfoList(groupId));
        } catch (Exception e) {
            log.error("error", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        return new JsonResultBean(msg);
    }

    /**
     * 里程能耗统计初始化界面
     * @return String
     * @Title: list
     * @author wangjianyu
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
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
}
