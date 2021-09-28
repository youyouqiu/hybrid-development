package com.zw.platform.controller.generalCargoReport;

import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.generalCargoReport.CargoSearchQuery;
import com.zw.platform.domain.generalCargoReport.CargoSpotCheckForm;
import com.zw.platform.service.generalCargoReport.SpotCheckCargoReortService;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.excel.TemplateExportExcel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 普货抽查表
 */
@Controller
@RequestMapping("/s/cargo/spotCheck")
public class SpotCheckCargoReortController {

    @Autowired
    SpotCheckCargoReortService spotCheckCargoReortService;

    @Autowired
    TemplateExportExcel templateExportExcel;

    @Autowired
    UserService userService;

    @Value("${sys.error.msg}")
    private String sysError;

    private static Logger log = LogManager.getLogger(SpotCheckCargoReortController.class);

    private static final String LIST_PAGE = "modules/sdReportManagement/cargoSpotCheck";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private static final String DATE_FORMAT_MIN = "yyyy-MM-dd HH:mm";

    /**
     * 获取普货抽查页面
     * @author lijie
     * @date 2019/8/29 16:40
     */
    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public ModelAndView spotCheckList() {
        try {
            ModelAndView mav = new ModelAndView(LIST_PAGE);
            String username = userService.getCurrentUserInfo().getUsername();
            RedisKey redisKey = HistoryRedisKeyEnum.CARGO_SPOT_CHECK_INFORMATION.of(username);
            RedisHelper.delete(redisKey);
            return mav;
        } catch (Exception e) {
            log.error("查询普货抽查表界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 普货抽查批量设置接口
     * @author lijie
     * @date 2019/8/29 17:40
     */
    @RequestMapping(value = { "/batchDeal" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean batchDeal(String dealMeasure, String dealResult) {
        try {
            boolean success = spotCheckCargoReortService.batchDeal(dealMeasure, dealResult);
            if (success) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("批量设置普货抽查表处理信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    /**
     * 查询普货抽查表数据
     * @author lijie
     * @date 2018/9/2 10:59
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean searchFeedBack(CargoSearchQuery cargoSpotCheckQuery) {
        try {
            if (StringUtils.isNotBlank(cargoSpotCheckQuery.getTime()) && StringUtils
                .isNotBlank(cargoSpotCheckQuery.getGroupIds()) && cargoSpotCheckQuery.getLength() != 0
                && cargoSpotCheckQuery.getStart() >= 0) {
                Page<CargoSpotCheckForm> feedBacks = spotCheckCargoReortService.searchSpotCheck(cargoSpotCheckQuery);
                return new PageGridBean(cargoSpotCheckQuery, feedBacks, true);
            } else {
                return new PageGridBean(false);
            }
        } catch (Exception e) {
            log.error("查询普货抽查表数据异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 导出(生成excel文件)
     * @param res res
     */
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    public void export(HttpServletResponse res, CargoSearchQuery cargoSearchQuery) {
        try {
            List<CargoSpotCheckForm> cargoSpotCheckForms =
                spotCheckCargoReortService.exportSearchSpotCheck(cargoSearchQuery);
            String time = cargoSearchQuery.getTime();
            Map<String, Object> data = new HashMap<>();
            data.put("cargoSpotCheckForms", cargoSpotCheckForms);
            data.put("time", DateUtil.getDateToString(DateUtil.getStringToDate(time, DATE_FORMAT_MIN), "yyyy年MM月dd日"));
            String fileName = time.substring(0, 10).replaceAll("-", "") + "道路运输车辆动态监控抽查表";
            templateExportExcel
                .templateExportExcel("/file/cargoReport/道路运输车辆动态监控抽查表（普货运输企业）模板.xls", res, data, fileName);
        } catch (Exception e) {
            log.error("导出普货月报表异常", e);
        }
    }

}
