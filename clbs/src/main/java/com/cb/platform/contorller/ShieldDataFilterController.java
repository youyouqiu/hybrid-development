package com.cb.platform.contorller;

import com.cb.platform.service.ShieldDataFilterService;
import com.zw.platform.util.common.JsonResultBean;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

/**
 * 屏蔽数据筛选报表
 * @author zhangsq
 * @date 2018/5/15 14:25
 */
@RestController
@RequestMapping("/cb/cbReportManagement/shieldDataFilter")
@Log4j2
public class ShieldDataFilterController {

    @Value("${sys.error.msg}")
    private String sysErrorMsg;
    @Autowired
    private ShieldDataFilterService shieldDataFilterService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView enterprise() {
        return new ModelAndView("modules/cbReportManagement/shieldDataFilter");
    }

    /**
     * 查询连续性分析报表数据
     * @param monitorId     车辆id
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param breakSecond   中断时长(s)
     * @param breakDistance 中断距离
     */
    @RequestMapping("/getContinuityAnalysisList")
    public JsonResultBean getContinuityAnalysisList(String monitorId, String startTime, String endTime,
        Integer breakSecond, Double breakDistance) {
        try {
            if (StringUtils.isBlank(monitorId) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)
                || breakSecond == null || breakSecond < 30 || breakSecond > 86400
                || (breakDistance != null && (breakDistance < 0.0 || breakDistance > 999.9))) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数错误！");
            }
            return shieldDataFilterService
                .getContinuityAnalysisList(monitorId, startTime, endTime, breakSecond, breakDistance);
        } catch (Exception e) {
            log.error("查询连续性分析报表数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导出连续性分析报表数据
     * @param monitorId     车辆id
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param breakSecond   中断时长(s)
     * @param breakDistance 中断距离
     */
    @RequestMapping(value = "/exportContinuityAnalysisList")
    public void exportContinuityAnalysisList(HttpServletResponse response, String monitorId, String startTime,
        String endTime, Integer breakSecond, Double breakDistance) {
        try {
            shieldDataFilterService.exportContinuityAnalysisList(response, monitorId, startTime, endTime, breakSecond,
                breakDistance);
        } catch (Exception e) {
            log.error(" 导出连续性分析报表报表异常", e);
        }
    }
}
