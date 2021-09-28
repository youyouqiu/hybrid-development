package com.zw.platform.controller.core;


import com.github.pagehelper.Page;
import com.zw.platform.domain.core.Resource;
import com.zw.platform.domain.core.query.ResourceQuery;
import com.zw.platform.service.core.ResourceService;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.PageGridBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;


/**
 * 资源 ResourceController
 */
@Controller
@RequestMapping("/c/resource")
public class SecResourceController {
    private static final String LIST_PAGE = "core/uum/resource/list";

    private static final String ADD_PAGE = "core/uum/resource/add";

    private static final String EDIT_PAGE = "core/uum/resource/edit";

    @Autowired
    private ResourceService resourceService;

    /**
     * 资源管理页面
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String listPage(ModelMap map) throws BusinessException {
        return LIST_PAGE;
    }

    /**
     * 资源管理数据
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(final ResourceQuery query) throws BusinessException {
        Page<Resource> result = resourceService.findResourceByPage(query);
        return new PageGridBean(query, result, true);
    }

    /**
     * 将form表单里面的String Date转换成Date型，字符串去掉空白
     */
    @InitBinder
    protected final void initBinder(final HttpServletRequest request, final ServletRequestDataBinder binder)
        throws Exception {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }
}