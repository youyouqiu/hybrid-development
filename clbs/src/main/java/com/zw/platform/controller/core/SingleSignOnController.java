package com.zw.platform.controller.core;

import com.zw.platform.service.core.UserService;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/5/22 14:35
 * APP2.0.0新增
 */
@Controller
@RequestMapping("/m/user")
public class SingleSignOnController {
    private static Logger log = LogManager.getLogger(SingleSignOnController.class);

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private UserService userService;

    /**
     * 生成客户端id
     */
    @ResponseBody
    @RequestMapping(value = "/clientId", method = RequestMethod.POST)
    public JsonResultBean generatingClientId(String userName) {
        try {
            if (StringUtils.isNotBlank(userName)) {
                return new JsonResultBean(userService.generatingClientId(userName));
            }
            return new JsonResultBean(JsonResultBean.FAULT, "用户名为空");
        } catch (Exception e) {
            log.error("生成客户端id异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 检查客户端id是否改变
     */
    @ResponseBody
    @RequestMapping(value = "/clientId/check", method = RequestMethod.POST)
    public Boolean checkClientId(String userName, String clientId) {
        try {
            return userService.checkClientId(userName, clientId);
        } catch (Exception e) {
            log.error("检查客户端id是否改变异常", e);
            return false;
        }
    }
}
