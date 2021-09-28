package com.zw.app.controller.personalCenter.statistics;

import com.alibaba.fastjson.JSONObject;
import com.zw.app.domain.personalCenter.AppOlineReportDetail;
import com.zw.app.domain.personalCenter.ReportEntity;
import com.zw.app.service.personalCenter.AppOnlineReportService;
import com.zw.app.util.AppVersionUtil;
import com.zw.app.util.common.AppResultBean;
import com.zw.platform.domain.BigDataReport.OnlineReport;
import io.swagger.annotations.Api;
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
 * @date 2018/12/10 15:00
 */
@Controller
@RequestMapping("/app/reportManagement/onlineReport")
@Api(tags = {"app上线统计"}, description = "app综合统计相关接口")
public class AppOnlineReportController {
    @Value("${sys.error.msg}")
    private String sysError;
    private static Logger log = LogManager.getLogger(AppOnlineReportController.class);

    @Autowired
    AppOnlineReportService appOnlineReportService;
    /**
     * 获取上线率排行
     *
     * @author lijie
     * @date 2018/12/10 16:11
     */
    @RequestMapping(value = {"/list"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getOnlineReport(HttpServletRequest request, @Validated ReportEntity reportEntity,
                                         BindingResult result) {
        try {
            if (result.getAllErrors().size() != 0) {
                return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
            }
            String requestURI = request.getRequestURI();
            String meth = AppVersionUtil.dealVersionName(requestURI, reportEntity.getVersion());
            Method method =
                    appOnlineReportService.getClass().getMethod(meth, String.class, String.class, String.class);
            List<OnlineReport> onlineReports =
                    (List<OnlineReport>) method.invoke(appOnlineReportService, reportEntity.getMoniterIds(),
                            reportEntity.getStartTime(),
                            reportEntity.getEndTime());
            if(onlineReports!=null){
                JSONObject objJson = new JSONObject(); // 传入JSONObject
                objJson.put("onlineReports", onlineReports);
                return new AppResultBean(objJson);
            }else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("上线率统计页面查询监控对象上线率信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取上线率详细信息
     *
     * @author lijie
     * @date 2018/12/11 10:11
     */
    @RequestMapping(value = {"/detail"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getOnlineReportDetail(HttpServletRequest request, @Validated ReportEntity reportEntity,
                                               BindingResult result) {
        try {
            if (result.getAllErrors().size() != 0) {
                return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
            }
            String requestURI = request.getRequestURI();
            String meth = AppVersionUtil.dealVersionName(requestURI, reportEntity.getVersion());
            Method method =
                    appOnlineReportService.getClass().getMethod(meth, String.class, String.class, String.class);
            List<AppOlineReportDetail> appOlineReportDetails =
                    (List<AppOlineReportDetail>) method.invoke(appOnlineReportService, reportEntity.getMoniterId(),
                            reportEntity.getStartTime(),
                            reportEntity.getEndTime());
            if(appOlineReportDetails!=null){
                JSONObject objJson = new JSONObject(); // 传入JSONObject
                objJson.put("details", appOlineReportDetails);
                return new AppResultBean(objJson);
            }else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("上线率统计页面查询监控对象上线信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

}
