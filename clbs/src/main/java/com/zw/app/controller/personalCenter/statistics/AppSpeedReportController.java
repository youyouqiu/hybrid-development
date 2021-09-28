package com.zw.app.controller.personalCenter.statistics;

import com.alibaba.fastjson.JSONObject;
import com.zw.app.domain.personalCenter.AppOlineReportDetail;
import com.zw.app.domain.personalCenter.AppSpeedReportDetail;
import com.zw.app.domain.personalCenter.ReportEntity;
import com.zw.app.service.personalCenter.AppSpeedReportService;
import com.zw.app.util.AppVersionUtil;
import com.zw.app.util.common.AppResultBean;
import com.zw.platform.domain.BigDataReport.OnlineReport;
import com.zw.platform.domain.reportManagement.SpeedReport;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.List;

/**
 * app个人中心综合统计
 *
 * @author lijie
 * @date 2018/12/11 15:00
 */
@Controller
@RequestMapping("/app/reportManagement/speedReport")
@Api(tags = {"app超速统计"}, description = "app综合统计相关接口")
public class AppSpeedReportController {
    @Value("${sys.error.msg}")
    private String sysError;
    private static Logger log = LogManager.getLogger(AppSpeedReportController.class);

    @Autowired
    AppSpeedReportService appSpeedReportService;

    /**
     * 获取超速报警排行
     * @author lijie
     * @date 2018/12/11 15:11
     */
    @RequestMapping(value = {"/list"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getSpeedReport(HttpServletRequest request, @Validated ReportEntity reportEntity,
                                        BindingResult result) {
        try {
            if (result.getAllErrors().size() != 0) {
                return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
            }
            String requestURI = request.getRequestURI();
            String meth = AppVersionUtil.dealVersionName(requestURI, reportEntity.getVersion());
            Method method =
                    appSpeedReportService.getClass().getMethod(meth, String.class, String.class, String.class, int.class);
            List<SpeedReport> speedReports =
                    (List<SpeedReport>) method.invoke(appSpeedReportService, reportEntity.getMoniterIds(),
                            reportEntity.getStartTime(),
                            reportEntity.getEndTime(),
                            reportEntity.getType());
            if(speedReports!=null){
                JSONObject objJson = new JSONObject(); // 传入JSONObject
                objJson.put("speedReports", speedReports);
                return new AppResultBean(objJson);
            }else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("超速统计页面查询监控对象上线率信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }


    /**
     * 获取超速报警详细信息
     *
     * @author lijie
     * @date 2018/12/12 10:11
     */
    @RequestMapping(value = {"/detail"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getSpeedReportDetail(HttpServletRequest request, @Validated ReportEntity reportEntity,
                                              BindingResult result) {
        try {
            if (result.getAllErrors().size() != 0) {
                return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
            }
            String requestURI = request.getRequestURI();
            String meth = AppVersionUtil.dealVersionName(requestURI, reportEntity.getVersion());
            Method method =
                    appSpeedReportService.getClass().getMethod(meth, String.class, String.class, String.class ,int.class);
            List<AppSpeedReportDetail> appSpeedReportDetails =
                    (List<AppSpeedReportDetail>) method.invoke(appSpeedReportService, reportEntity.getMoniterId(),
                            reportEntity.getStartTime(),
                            reportEntity.getEndTime(),
                            reportEntity.getType());
            if(appSpeedReportDetails!=null){
                JSONObject objJson = new JSONObject(); // 传入JSONObject
                objJson.put("details", appSpeedReportDetails);
                return new AppResultBean(objJson);
            }else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("超速报警统计页面查询监控对象上线信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }
}
