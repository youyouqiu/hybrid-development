package com.zw.app.controller.activeSecurity;

import com.alibaba.fastjson.JSONObject;
import com.zw.app.domain.alarm.PercentageOfRank;
import com.zw.app.domain.alarm.RiskRankResult;
import com.zw.app.entity.methodParameter.DefaultConfiguration;
import com.zw.app.entity.methodParameter.RiskRankEntity;
import com.zw.app.entity.statisticsFuzzy.FuzzyRiskRankEntity;
import com.zw.app.service.activeSecurity.AppRiskRankService;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


@Controller
@RequestMapping("/app/risk/riskRank")
public class AppRiskRankController {

    private static Logger log = LogManager.getLogger(AppRiskRankController.class);

    @Value("${sys.error.msg}")
    private String sysError;

    @Autowired
    private AppRiskRankService appRiskRankService;

    @RequestMapping(value = {"/getMonitorOfUser"}, method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getMonitorOfUser(HttpServletRequest request, @Validated RiskRankEntity riskRankEntity,
        BindingResult result) {
        try {
            if (result.getAllErrors().size() != 0) {
                return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
            }
            String requestURI = request.getRequestURI();
            String meth = AppVersionUtil.dealVersionName(requestURI, riskRankEntity.getVersion());
            Method method = appRiskRankService.getClass().getMethod(meth, Integer.class, Integer.class, String.class);
            JSONObject invoke =
                (JSONObject) method.invoke(appRiskRankService, riskRankEntity.getPage(), riskRankEntity.getPageSize(),
                    riskRankEntity.getType());
            if (invoke == null) {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
            return new AppResultBean(invoke);
        } catch (Exception e) {
            log.error("获取当前用户监控对象列表异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    @RequestMapping(value = {"/getFuzzyMonitorOfUser"}, method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getFuzzyMonitorOfUser(HttpServletRequest request,
        @Validated FuzzyRiskRankEntity fuzzyRiskRankEntity, BindingResult result) {
        try {
            return getAppResultBean(request, fuzzyRiskRankEntity, result);
        } catch (Exception e) {
            log.error("模糊搜索当前用户监控对象列表异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    @RequestMapping(value = {"/getFuzzyVehicleOfUser"}, method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getFuzzyVehicleOfUser(HttpServletRequest request,
        @Validated FuzzyRiskRankEntity fuzzyRiskRankEntity, BindingResult result) {
        try {
            return getAppResultBean(request, fuzzyRiskRankEntity, result);
        } catch (Exception e) {
            log.error("模糊搜索当前用户监控对象列表异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    @RequestMapping(value = {"/getFuzzyHourPollsOfUser"}, method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getFuzzyHourPollsOfUser(HttpServletRequest request,
        @Validated FuzzyRiskRankEntity fuzzyRiskRankEntity, BindingResult result) {
        try {
            return getAppResultBean(request, fuzzyRiskRankEntity, result);
        } catch (Exception e) {
            log.error("模糊搜索当前用户监控对象列表异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    @RequestMapping(value = {"/getFuzzyPollingOilOfUser"}, method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getFuzzyPollingOilOfUser(HttpServletRequest request,
        @Validated FuzzyRiskRankEntity fuzzyRiskRankEntity, BindingResult result) {
        try {
            return getAppResultBean(request, fuzzyRiskRankEntity, result);
        } catch (Exception e) {
            log.error("模糊搜索当前用户监控对象列表异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    @RequestMapping(value = {"/getFuzzyOilSensorOfUser"}, method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getFuzzyOilSensorOfUser(HttpServletRequest request,
        @Validated FuzzyRiskRankEntity fuzzyRiskRankEntity, BindingResult result) {
        try {
            return getAppResultBean(request, fuzzyRiskRankEntity, result);
        } catch (Exception e) {
            log.error("模糊搜索当前用户监控对象列表异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    private AppResultBean getAppResultBean(HttpServletRequest request,
        @Validated FuzzyRiskRankEntity fuzzyRiskRankEntity, BindingResult result)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (result.getAllErrors().size() != 0) {
            return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
        }
        String requestURI = request.getRequestURI();
        String meth = AppVersionUtil.dealVersionName(requestURI, fuzzyRiskRankEntity.getVersion());
        Method method = appRiskRankService.getClass()
            .getMethod(meth, Integer.class, Integer.class, String.class, String.class, Integer.class);
        JSONObject invoke = (JSONObject) method
            .invoke(appRiskRankService, fuzzyRiskRankEntity.getPage(), fuzzyRiskRankEntity.getPageSize(),
                fuzzyRiskRankEntity.getType(), fuzzyRiskRankEntity.getSearch(),
                fuzzyRiskRankEntity.getSearchType());
        if (invoke == null) {
            return new AppResultBean(AppResultBean.PARAM_ERROR);
        }
        return new AppResultBean(invoke);
    }

    @RequestMapping(value = {"/getVehiclesOfUser"}, method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getVehiclesOfUser(HttpServletRequest request, @Validated RiskRankEntity riskRankEntity,
        BindingResult result) {
        try {
            if (result.getAllErrors().size() != 0) {
                return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
            }
            String requestURI = request.getRequestURI();
            String meth = AppVersionUtil.dealVersionName(requestURI, riskRankEntity.getVersion());
            Method method = appRiskRankService.getClass().getMethod(meth, Integer.class, Integer.class);
            JSONObject invoke =
                (JSONObject) method.invoke(appRiskRankService, riskRankEntity.getPage(), riskRankEntity.getPageSize());
            if (invoke == null) {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
            return new AppResultBean(invoke);
        } catch (Exception e) {
            log.error("获取当前用户监控对象列表异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    @RequestMapping(value = {"/getRiskRank"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getRiskRank(HttpServletRequest request, @Validated RiskRankEntity riskRankEntity,
        BindingResult result) {
        try {
            if (result.getAllErrors().size() != 0) {
                return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
            }
            String requestURI = request.getRequestURI();
            String meth = AppVersionUtil.dealVersionName(requestURI, riskRankEntity.getVersion());
            Method method = appRiskRankService.getClass().getMethod(meth, String.class, String.class, String.class,
                Integer.class);
            List<RiskRankResult> riskRankResults =
                (List<RiskRankResult>) method.invoke(appRiskRankService, riskRankEntity.getVehicleIds(),
                    riskRankEntity.getStartTime(), riskRankEntity.getEndTime(), riskRankEntity.getRiskType());
            if (riskRankResults == null) {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
            return new AppResultBean(riskRankResults);
        } catch (Exception e) {
            log.error("获取当前用户报警排行列表异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    @RequestMapping(value = {"/getPercentageOfRank"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getPercentageOfRank(HttpServletRequest request, @Validated RiskRankEntity riskRankEntity,
        BindingResult result) {
        try {
            if (result.getAllErrors().size() != 0) {
                return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
            }
            String requestURI = request.getRequestURI();
            String meth = AppVersionUtil.dealVersionName(requestURI, riskRankEntity.getVersion());
            Method method =
                appRiskRankService.getClass().getMethod(meth, String.class, String.class, String.class);
            PercentageOfRank percentage =
                (PercentageOfRank) method.invoke(appRiskRankService, riskRankEntity.getVehicleIds(),
                    riskRankEntity.getStartTime(), riskRankEntity.getEndTime());
            return new AppResultBean(percentage);
        } catch (Exception e) {
            log.error("获取当前用户报警排行列表异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    @RequestMapping(value = {"/getDefaultMonitorIds"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getDefaultMonitorIds(HttpServletRequest request,
        @Validated DefaultConfiguration defaultConfiguration,
        BindingResult result) {
        try {
            if (result.getAllErrors().size() != 0) {
                return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
            }
            String requestURI = request.getRequestURI();
            String meth = AppVersionUtil.dealVersionName(requestURI, defaultConfiguration.getVersion());
            Method method =
                appRiskRankService.getClass().getMethod(meth, String.class, Integer.class, Boolean.class);
            List<String> monitorIds =
                (List<String>) method.invoke(appRiskRankService, defaultConfiguration.getType(),
                    defaultConfiguration.getDefaultSize(), defaultConfiguration.getIsFilter());
            return new AppResultBean(monitorIds);
        } catch (Exception e) {
            log.error("获取默认监控对象id数据异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    @RequestMapping(value = {"/getAdasMonitorFlag"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean adasMonitorFlag(HttpServletRequest request,
        @Validated DefaultConfiguration defaultConfiguration,
        BindingResult result) {
        try {
            if (result.getAllErrors().size() != 0) {
                return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
            }
            String requestURI = request.getRequestURI();
            String meth = AppVersionUtil.dealVersionName(requestURI, defaultConfiguration.getVersion());
            Method method =
                appRiskRankService.getClass().getMethod(meth);
            JSONObject adasMonitorFlag =
                (JSONObject) method.invoke(appRiskRankService);
            return new AppResultBean(adasMonitorFlag);
        } catch (Exception e) {
            log.error("获取默认监控对象id数据异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    @RequestMapping(value = "/getVehicleIsBindRiskDefined", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getVehicleIsBindRiskDefined(HttpServletRequest request,
        @Validated RiskRankEntity riskRankEntity,
        BindingResult result) {
        try {
            if (result.getAllErrors().size() != 0) {
                return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
            }
            String requestURI = request.getRequestURI();
            String methodName = AppVersionUtil.dealVersionName(requestURI, riskRankEntity.getVersion());
            Class[] paramsClasses = new Class[] {String.class};
            Method method = appRiskRankService.getClass().getMethod(methodName, paramsClasses);
            Boolean isBindRiskFlag = (Boolean) method.invoke(appRiskRankService, riskRankEntity.getVehicleId());
            return new AppResultBean(isBindRiskFlag);
        } catch (Exception e) {
            log.error("查询监控对象是否设置风险定义异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }
}
