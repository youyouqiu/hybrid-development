package com.zw.app.controller.order;

import com.alibaba.fastjson.JSONObject;
import com.zw.app.service.order.OrderSendService;
import com.zw.app.util.common.AppResultBean;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * APP指令下发controller
 */
@RequestMapping("/app/order")
@Controller
public class OrderSendController {
    private static Logger logger = LogManager.getLogger(OrderSendController.class);

    @Autowired
    private OrderSendService orderSendService;

    @Value("${sys.error.msg}")
    private String sysError;

    /**
     * 检查服务器通信是否正常
     * @return
     */
    @ApiOperation(value = "检查服务器通信是否正常", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/checkServerUnobstructed", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean checkServerUnobstructed() {
        try {
            String msg = orderSendService.checkServerUnobstructed();
            return new AppResultBean(msg);
        } catch (Exception e) {
            logger.error("检查服务器通信异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    @RequestMapping(value = "/call", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean sendCallOrder(String monitorId, HttpServletRequest request) {
        try {
            if (StringUtils.isNotBlank(monitorId)) {
                JSONObject result = orderSendService.send0x8201(monitorId, request);
                if (result != null) {
                    if (result.get("exceptionDetailMsg") != null) {
                        return new AppResultBean(AppResultBean.PARAM_ERROR,
                            result.get("exceptionDetailMsg").toString());
                    }
                    return new AppResultBean(result);
                }
            }
            return new AppResultBean(AppResultBean.PARAM_ERROR);
        } catch (Exception e) {
            logger.error("下发点名指令异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }
}
