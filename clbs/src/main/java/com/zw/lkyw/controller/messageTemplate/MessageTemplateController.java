package com.zw.lkyw.controller.messageTemplate;

import com.github.pagehelper.Page;
import com.zw.lkyw.domain.messageTemplate.MessageTemplateBean;
import com.zw.lkyw.domain.messageTemplate.MessageTemplateForm;
import com.zw.lkyw.domain.messageTemplate.MessageTemplateInfo;
import com.zw.lkyw.domain.messageTemplate.MessageTemplateQuery;
import com.zw.lkyw.repository.mysql.messageTemplate.MessageTemplateDao;
import com.zw.lkyw.service.messageTemplate.MessageTemplateService;
import com.zw.platform.commons.Auth;
import com.zw.platform.controller.core.RoleController;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 下发消息模板控制器层
 * @author XK on 2019/12/26 09:46
 */

@RequestMapping("/lkyw/message/template")
@Controller
public class MessageTemplateController {

    private Logger log = LogManager.getLogger(RoleController.class);

    @Value("${sys.error.msg}")
    private String syError;

    @Autowired
    private HttpServletRequest request;

    /**
     * 下发消息模板页面
     */
    private static final String LIST_PAGE = "vas/lkyw/messageTemplate/list";

    /**
     * 修改模板页面
     */
    private static final String TEMPLATE_EDIT = "vas/lkyw/messageTemplate/edit";

    /**
     * 新增页面
     */
    private static final String ADD_PAGE = "vas/lkyw/messageTemplate/add";

    /**
     * 导入页面
     */
    private static final String EXPORT_PAGE = "vas/lkyw/messageTemplate/import";

    /**
     * 错误页面
     */
    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private MessageTemplateService messageTemplateService;

    @Autowired
    private MessageTemplateDao messageTemplateDao;

    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String getListPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = "/templateList", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getTerminalType(MessageTemplateQuery query) {
        try {
            Page<MessageTemplateInfo> result = messageTemplateService.findMessageTemplate(query);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            log.error("查询下发消息模板异常", e);
            return new PageGridBean(false);
        }
    }

    @RequestMapping(value = "/page", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean terminalTypePage(MessageTemplateQuery query) {
        try {
            query.setStatus(MessageTemplateQuery.ENABLE);
            Page<MessageTemplateInfo> result = messageTemplateService.findMessageTemplate(query);
            final Page<String> resultPage = result.stream()
                                                .map(MessageTemplateInfo::getContent)
                                                .collect(Collectors.toCollection(Page::new));
            resultPage.setTotal(result.getTotal());
            return new PageGridBean(query, resultPage, true);
        } catch (Exception e) {
            log.error("获取所有消息模板异常", e);
            return new PageGridBean(PageGridBean.FAULT, syError);
        }
    }

    /**
     * 新增消息模板
     * @param form 新增信息
     * @return JsonResultBean
     */
    @RequestMapping(value = "/addTemplate", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addTemplateInfo(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final MessageTemplateForm form) {

        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return messageTemplateService.addTemplate(form, ipAddress);
        } catch (Exception e) {
            log.error("新增下发消息模板异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 新增页面
     * @return
     */
    @RequestMapping(value = { "/templateAddPage" }, method = RequestMethod.GET)
    public String templateAddPage() {
        return ADD_PAGE;
    }

    /**
     * 终端型号修改页面
     */
    @Auth
    @RequestMapping(value = { "/templateEditPage_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView templateEditPage(@PathVariable String id) {
        try {
            ModelAndView mav = new ModelAndView(TEMPLATE_EDIT);
            List<MessageTemplateInfo> info = messageTemplateService.findTemplateById(id);
            if (info != null) {
                mav.addObject("result", info.get(0));
            }
            return mav;
        } catch (Exception e) {
            log.error("下发消息模板修改页面弹出时出现异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改下发消息模板
     * @param bean 修改信息
     * @return JsonResultBean
     */
    @RequestMapping(value = "/updateTemplate", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateTemplate(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final MessageTemplateBean bean) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return messageTemplateService.updateTemplate(bean, ipAddress);
        } catch (Exception e) {
            log.error("修改下发消息模板时出现异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 删除下发消息模板
     */
    @RequestMapping(value = "/deleteTemplate", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteTemplate(String ids) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            List<String> templateIds = Arrays.asList(ids.split(","));
            return messageTemplateService.deleteTemplate(templateIds, ipAddress);
        } catch (Exception e) {
            log.error("删除下发消息模板异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 下载下发消息模板
     */
    @RequestMapping(value = "/downloadMessageTemplate", method = RequestMethod.GET)
    public void downloadTemplate(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "下发消息模板");
            messageTemplateService.generateMessageTemplate(response);
        } catch (Exception e) {
            log.error("下载下发消息模板异常", e);
        }
    }

    /**
     * 导入下发消息模板信息
     */
    @RequestMapping(value = "/importTemplate", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importTemplate(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Map<String, Object> resultMap = messageTemplateService.importTemplate(file, ipAddress);
            String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("导入下发消息模板信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 导出下发消息模板信息
     */


    /**
     * 弹出导入页面
     * @return String
     * @Title: 导入
     */
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public String importPage() {
        return EXPORT_PAGE;
    }

    /**
     * 导出下发消息模板信息
     *
     * @param res res
     */
    @RequestMapping(value = "/exportTemplate", method = RequestMethod.GET)
    public void export(HttpServletResponse res, @RequestParam(value = "fuzzyParam", required = false)
            String fuzzyParam) {
        try {
            messageTemplateService.exportTemplate(fuzzyParam, res);
        } catch (Exception e) {
            log.error("导出下发消息模板列表异常", e);
        }
    }

}
