package com.zw.app.util;

import com.zw.app.annotation.AppVersionEntity;
import com.zw.app.entity.BaseEntity;
import com.zw.app.entity.MyHashMap;
import com.zw.app.exception.VersionErrorException;
import com.zw.app.util.common.AppBaseArg;
import com.zw.app.util.common.AppResultBean;
import com.zw.app.util.common.ExecuteFunction;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/***
 @Author gfw
 @Date 2018/12/12 14:57
 @Description App版本控制
 @version 1.0
 **/
public class AppVersionUtil {

    private static Logger logger = LogManager.getLogger(AppVersionUtil.class);

    private static final String syserror = "系统响应异常，请稍后再试或联系管理员！";

    public static String dealVersionName(String url, Integer version) {
        url = checkUrl(url);
        List<AppVersionEntity> list = (List<AppVersionEntity>) (MyHashMap.getInstance().get(url));
        String method = "";
        for (AppVersionEntity appVersionEntity : list) {
            if (appVersionEntity.getVersion() > version) {
                break;
            }
            method = appVersionEntity.getMethod();
        }
        if ("".equals(method)) {
            method = list.get(list.size() - 1).getMethod();
        }

        return method;
    }

    private static String checkUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return url;
        }

        if (url.contains("/detailLocationInfo")) {
            List<String> urlInfos = new ArrayList<>(Arrays.asList(url.split("/")));
            urlInfos.remove(urlInfos.size() - 1);
            return String.join("/", urlInfos);
        }

        // 判断路径是是否包含UUid,如果包含需要截取掉url
        // 列如 /clbs/app/monitor/76a1c4bf-bcfb-4451-a54b-9af490d98630/history/stop
        // 暂时只处理获取停止数据历史数据接口
        if (!url.contains("/app/monitor") || !url.contains("/history/stop")) {
            return url;
        }
        // 分割字符串
        List<String> urlInfos = new ArrayList<>(Arrays.asList(url.split("/")));

        if (url.contains("/detailLocationInfo/")) {
            urlInfos.remove(urlInfos.size() - 1);
            return String.join("/", urlInfos);
        }

        if (CollectionUtils.isEmpty(urlInfos)) {
            return url;
        }
        if (urlInfos.size() < 5) {
            return url;
        }
        String isParam = urlInfos.get(4);
        if (StringUtils.isBlank(isParam) || !isParam.matches("(\\w{8}(-\\w{4}){3}-\\w{12}?)")) {
            return url;
        }
        // 去除掉url中的参数
        urlInfos.remove(4);
        return String.join("/", urlInfos);
    }

    private static String getMethodName(HttpServletRequest request, BaseEntity baseEntity) {
        String requestURI = request.getRequestURI();
        return AppVersionUtil.dealVersionName(requestURI, baseEntity.getVersion());
    }

    private static void checkAPPVersion(BindingResult result) {
        if (result.getAllErrors().size() != 0) {
            throw new VersionErrorException(result.getAllErrors().toString());
        }
    }

    private static Method getMethod(HttpServletRequest request, Object service, BaseEntity baseEntity,
        Class<?>... classes) throws NoSuchMethodException {
        return service.getClass().getMethod(getMethodName(request, baseEntity), classes);
    }

    public static Object executeResult(HttpServletRequest request, BindingResult result, Object service,
        BaseEntity baseEntity) throws Exception {
        checkAPPVersion(result);
        Object resultData = null;
        Method method = getMethod(request, service, baseEntity, baseEntity.getArgClasses());
        resultData = method.invoke(service, baseEntity.getArgs());
        return resultData;
    }

    public static Object executeResult(AppBaseArg appBaseArg) throws Exception {
        return executeResult(appBaseArg.getRequest(), appBaseArg.getResult(), appBaseArg.getService(),
            appBaseArg.getBaseEntity());
    }

    public static AppResultBean getVersionError(BindingResult result) {
        return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
    }

    /**
     * 拿到结果直接封装使用new AppResultBean进行封装，使用该方法
     * @param request
     * @param base
     * @param result
     * @param service
     * @return
     */
    public static AppResultBean getResultData(HttpServletRequest request, BaseEntity base, BindingResult result,
        Object service) {

        ExecuteFunction executeFunction = (obj) -> new AppResultBean(obj);
        return getAppResultBean(request, base, result, service, executeFunction);
    }

    /**
     * 拿到结果需要进行需要进行对结果进行二次处理
     * @param request
     * @param base
     * @param result
     * @param service
     * @param executeFunction
     * @return
     */
    public static AppResultBean getResultData(HttpServletRequest request, BaseEntity base, BindingResult result,
        Object service, ExecuteFunction executeFunction) {
        return getAppResultBean(request, base, result, service, executeFunction);
    }

    /**
     * 拿到结果就是AppResultBean
     * @param request
     * @param base
     * @param result
     * @param service
     * @return
     */
    public static AppResultBean getAppResultBean(HttpServletRequest request, BaseEntity base, BindingResult result,
        Object service) {
        ExecuteFunction executeFunction = (obj) -> (AppResultBean) obj;
        return getAppResultBean(request, base, result, service, executeFunction);
    }

    private static AppResultBean getAppResultBean(HttpServletRequest request, BaseEntity base, BindingResult result,
        Object service, ExecuteFunction executeFunction) {
        AppBaseArg appBaseArg = null;
        try {
            appBaseArg = AppBaseArg.getInstance(request, result, service, base);
            Object resultData = AppVersionUtil.executeResult(appBaseArg);
            return executeFunction.execute(resultData);
        } catch (VersionErrorException versionErrorException) {
            return AppVersionUtil.getVersionError(result);
        } catch (Exception e) {
            String errorMsg = "";
            if (appBaseArg != null) {
                errorMsg = appBaseArg.getExceptionIfo();
            }
            logger.error(errorMsg, e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, syserror);
        }
    }

}
