package com.zw.platform.controller.personalized;

import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.basicinfo.form.FastNavConfigForm;
import com.zw.platform.service.personalized.FastNavConfigService;
import com.zw.platform.util.IPAddrUtil;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 首页定制
 * create by denghuabing 2018.12.13
 */
@RequestMapping("/c/fastNav")
@Controller
public class FastNavConfigController {

    private final Logger logger = LogManager.getLogger(FastNavConfigController.class);

    @Autowired
    private FastNavConfigService fastNavConfigService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户的首页定制信息
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getList() {
        try {
            String userId = userService.getCurrentUserUuid();
            List<FastNavConfigForm> list = fastNavConfigService.getList(userId);
            return new JsonResultBean(list);
        } catch (Exception e) {
            logger.error("获取用户首页定制信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 修改导航顺序
     */
    @RequestMapping(value = "editOrder", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean editOrder(String editOrder, String editedOrder, String editId, String editedId) {
        try {
            String userId = userService.getCurrentUserUuid();
            if (StringUtils.isNotBlank(editId) || StringUtils.isNotBlank(editedId)) {
                return fastNavConfigService.updateOrders(editOrder, editedOrder, editId, editedId, userId);
            }
            return new JsonResultBean();
        } catch (Exception e) {
            logger.error("用户修改首页定制异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 新增或修改
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(FastNavConfigForm form) {
        try {
            if (form != null) {
                String ipAddress = IPAddrUtil.getClientIp(request);
                form.setUserId(userService.getCurrentUserUuid());
                return fastNavConfigService.add(form, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("用户修改首页定制异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 根据序号获取当前序号的定制信息
     */
    @RequestMapping(value = "/findBySort_{order}", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findBySort(@PathVariable("order") String order) {
        try {
            if (order != null) {
                String userId = userService.getCurrentUserUuid();
                FastNavConfigForm form = fastNavConfigService.findBySort(userId, order);
                return new JsonResultBean(form);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("获取当前序号的首页定制信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @RequestMapping(value = "/dalete_{order}", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("order") String order) {
        try {
            if (order != null) {
                String ipAddress = IPAddrUtil.getClientIp(request);
                String userId = userService.getCurrentUserUuid();
                return fastNavConfigService.delete(userId, order, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("用删除首页定制异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }
}
