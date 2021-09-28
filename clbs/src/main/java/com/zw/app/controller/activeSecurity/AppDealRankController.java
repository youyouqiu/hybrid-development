package com.zw.app.controller.activeSecurity;

import com.zw.app.domain.alarm.DealRiskNum;
import com.zw.app.domain.alarm.RiskRankResult;
import com.zw.app.entity.methodParameter.RiskRankEntity;
import com.zw.app.service.activeSecurity.AppDealRankService;
import com.zw.app.util.AppVersionUtil;
import com.zw.app.util.common.AppResultBean;
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


@Controller
@RequestMapping("/app/risk/dealRank")
public class AppDealRankController {
    private static Logger log = LogManager.getLogger(AppDealRankController.class);

    @Autowired
    AppDealRankService appDealRankService;

    @Value("${sys.error.msg}")
    private String sysError;

    @RequestMapping(value = {"/getDealRank"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getDealRank(HttpServletRequest request, @Validated RiskRankEntity riskRankEntity,
        BindingResult result) {
        try {
            if (result.getAllErrors().size() != 0) {
                return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
            }
            String requestURI = request.getRequestURI();
            String meth = AppVersionUtil.dealVersionName(requestURI, riskRankEntity.getVersion());
            Method method =
                appDealRankService.getClass().getMethod(meth, String.class, String.class, String.class, Integer.class);
            List<RiskRankResult> riskRankResults =
                (List<RiskRankResult>) method.invoke(appDealRankService, riskRankEntity.getVehicleIds(),
                    riskRankEntity.getStartTime(),
                    riskRankEntity.getEndTime(), riskRankEntity.getStatus());
            return new AppResultBean(riskRankResults);
        } catch (Exception e) {
            log.error("获取当前用户报警处置列表异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    @RequestMapping(value = {"/getDealNum"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getDealNum(HttpServletRequest request, @Validated RiskRankEntity riskRankEntity,
        BindingResult result) {
        try {
            if (result.getAllErrors().size() != 0) {
                return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
            }
            String requestURI = request.getRequestURI();
            String meth = AppVersionUtil.dealVersionName(requestURI, riskRankEntity.getVersion());
            Method method =
                appDealRankService.getClass().getMethod(meth, String.class, String.class, String.class);
            DealRiskNum dealRiskNum =
                (DealRiskNum) method.invoke(appDealRankService, riskRankEntity.getVehicleIds(),
                    riskRankEntity.getStartTime(),
                    riskRankEntity.getEndTime());
            return new AppResultBean(dealRiskNum);
        } catch (Exception e) {
            log.error("获取当前用户报警处置数量异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }
}
