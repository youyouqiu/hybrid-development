package com.zw.platform.controller.carbonmgt;


import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.domain.vas.carbonmgt.form.MobileSourceEnergyReportForm;
import com.zw.platform.domain.vas.carbonmgt.query.MileageQuery;
import com.zw.platform.domain.vas.carbonmgt.query.TimeEnergyStatisticsQuery;
import com.zw.platform.service.carbonmgt.EnergySavingProductsDataBeforeService;
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
 * 移动源能耗报表-节油产品前能耗数据 <p>Title: EnergySavingProductsDataBeforeController.java</p> <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p> <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2017年2月14日下午1:48:06
 * @version 1.0
 */
@Controller
@RequestMapping("/v/carbonmgt/energySavingBefore")
public class EnergySavingProductsDataBeforeController {
    private static Logger log = LogManager.getLogger(EnergySavingProductsDataBeforeController.class);

    private static final String QUERY_BY_DATE_PAGE =
		"vas/carbonmgt/mobileSorceEnergyReport/energySavingProductsDataBefore/list";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private MileageQuery query = new MileageQuery();

    @Autowired
    private EnergySavingProductsDataBeforeService espdbService;

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
    public JsonResultBean initQueryPage(String groupId)
        throws BusinessException {
        JSONObject msg = new JSONObject();
        // 初始化车辆信息
        try {
            msg.put("vehicleInfoList", espdbService.getVehicleInfoList(groupId));
        } catch (Exception e) {
            log.error("error", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        return new JsonResultBean(msg);
    }

    /**
     * 节油产品前能耗数据初始界面
     * @Title: queryByDatePage
     * @return
     * @throws BusinessException
     * @return ModelAndView
     * @throws @author
     *             Liubangquan
     */
    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
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
            log.error("节油产品前能耗数据初始界面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }

    }

    /**
     * 查询数据
     * @Title: getListPage
     * @return
     * @throws BusinessException
     * @return PageGridBean
     * @throws BusinessException
     * @author Liubangquan
     */
    @RequestMapping(value = {"/queryByDatePage"}, method = RequestMethod.POST)
    @ResponseBody
    public List<MobileSourceEnergyReportForm> getListPage(String queryWay, String startDate, String endDate,
                                                          String groupId, String vehicleId)
        throws Exception {
        query.setQueryWay(queryWay);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setGroupId(groupId);
        query.setBrand(vehicleId);
        List<MobileSourceEnergyReportForm> list = espdbService.queryByDate(queryWay, startDate, endDate, groupId,
            vehicleId);
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
    public void export(HttpServletResponse response, HttpServletRequest request) {
        try {
            String filename = "节油产品前能耗数据";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            espdbService.export("节油产品前能耗数据", 1, response, query);
        } catch (Exception e) {
            log.error("导出节油产品前能耗数据列表异常", e);
        }

    }

}
