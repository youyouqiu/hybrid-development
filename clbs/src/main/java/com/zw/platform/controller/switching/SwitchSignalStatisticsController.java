package com.zw.platform.controller.switching;

import com.alibaba.fastjson.JSON;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.vas.switching.SwitchSignalStatisticsInfo;
import com.zw.platform.domain.vas.switching.query.SwitchSignalQuery;
import com.zw.platform.service.switching.SwitchSignalStatisticsService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2018/9/6 13:58
 */
@Controller
@RequestMapping("/v/switching/switchSignalStatistics")
public class SwitchSignalStatisticsController {
    private static Logger log = LogManager.getLogger(SwitchSignalStatisticsController.class);

    private static final String LIST_PAGE = "vas/switching/switchSignalStatistics/switchSignalStatistics";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Resource
    private SwitchSignalStatisticsService switchSignalStatisticsService;

    /**
     * 开关信号报表页面
     *
     * @return
     */
    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list() {
        try {
            ModelAndView mav = new ModelAndView(LIST_PAGE);
            List<SwitchSignalStatisticsInfo> vehicleList = switchSignalStatisticsService.getBindSwitchSignalVehicle();
            mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
            return mav;
        } catch (Exception e) {
            log.error("获取开关信号报表页异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 获取开关信号报表图表信息
     *
     * @param query
     * @return
     */
    @ResponseBody
    @RequestMapping("/getSwitchSignalChartInfo")
    public JsonResultBean getSwitchSignalChartInfo(SwitchSignalQuery query) {
        try {
            return switchSignalStatisticsService.getSwitchSignalChartInfo(query);
        } catch (Exception e) {
            log.error("获取开关信号报表图表信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获得开关信号报表终端表格信息
     *
     * @param query
     * @return
     */
    @ResponseBody
    @RequestMapping("/getSwitchSignalTerminalFormInfo")
    public PageGridBean getSwitchSignalTerminalFormInfo(SwitchSignalQuery query) {
        try {
            return switchSignalStatisticsService.getSwitchSignalTerminalFormInfo(query);
        } catch (Exception e) {
            log.error("获得开关信号报表终端表格信息", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 获得开关信号报表采集板1表格信息
     *
     * @param query
     * @return
     */
    @ResponseBody
    @RequestMapping("/getSwitchSignalAcquisitionBoardOneFormInfo")
    public PageGridBean getSwitchSignalAcquisitionBoardOneFormInfo(SwitchSignalQuery query) {
        try {
            return switchSignalStatisticsService.getSwitchSignalAcquisitionBoardOneFormInfo(query);
        } catch (Exception e) {
            log.error("获得开关信号报表采集板1表格信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 获得开关信号报表采集板2表格信息
     *
     * @param query
     * @return
     */
    @ResponseBody
    @RequestMapping("/getSwitchSignalAcquisitionBoardTwoFormInfo")
    public PageGridBean getSwitchSignalAcquisitionBoardTwoFormInfo(SwitchSignalQuery query) {
        try {
            return switchSignalStatisticsService.getSwitchSignalAcquisitionBoardTwoFormInfo(query);
        } catch (Exception e) {
            log.error("获得开关信号报表采集板2表格信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }
}
