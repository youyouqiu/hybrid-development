package com.zw.platform.controller.oilsubsidy;

import com.zw.platform.commons.Auth;
import com.zw.platform.domain.oilsubsidy.subsidyManage.SubsidyManageResp;
import com.zw.platform.service.oilsubsidy.SubsidyManageService;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 补传管理
 * @Author Tianzhangxu
 * @Date 2021/3/25 17:19
 */
@Controller
@RequestMapping("/m/subsidy/manage")
public class SubsidyManageController {
    private static final String LIST_PAGE = "/modules/oilSubsidyManage/subsidyManage/list";

    private static final Logger logger = LogManager.getLogger(StatisticalCheckOfLocationInformationController.class);

    @Autowired
    private SubsidyManageService subsidyManageService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @ResponseBody
    @RequestMapping(value = {"/detail"}, method = RequestMethod.POST)
    public JsonResultBean subsidyDetail(String orgIds) {
        try {
            List<SubsidyManageResp> detail = subsidyManageService.getDetail(orgIds);
            return new JsonResultBean(detail);
        } catch (Exception e) {
            logger.error("获取补传详情列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}
