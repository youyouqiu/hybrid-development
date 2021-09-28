package com.zw.platform.controller.forwardplatform_808;

import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.connectionparamsset_809.T809ForwardConfig;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormConfigQuery;
import com.zw.platform.service.thirdplatform.NetCarService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 网约车转发
 */
@Controller
@RequestMapping("/m/forward-platform/net-car")
public class NetCarController {
    private static final String LIST_PAGE = "modules/forwardplatform/mf/netCar";

    private static final Logger log = LogManager.getLogger(NetCarController.class);
    @Autowired
    private NetCarService service;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 查询转发对象
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(IntercomPlatFormConfigQuery query) {
        try {
            Page<T809ForwardConfig> formList = (Page<T809ForwardConfig>) service.list(query);
            return new PageGridBean(query, formList, true);
        } catch (Exception e) {
            log.error("显示转发对象列表异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(String vehicleIds, String platformId) {
        try {
            if (StringUtils.isEmpty(vehicleIds)) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            service.add(vehicleIds, platformId);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("添加转发对象异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@RequestParam("id") final String id) {
        try {
            if (id == null) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            final boolean result = service.delete(id);
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("删除转发对象异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }
}
