package com.cb.platform.contorller;

import com.cb.platform.domain.ChatGroupDo;
import com.cb.platform.domain.query.ChatGroupQuery;
import com.cb.platform.service.ChatGroupService;
import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/cb/chat/chatGroup")
public class ChatGroupController {

    private static Logger logger = LogManager.getLogger(ChatGroupController.class);

    private static final String LIST_PAGE = "modules/chatGroup/list";

    private static final String ADD_PAGE = "modules/chatGroup/add";

    private static final String EDIT_PAGE = "modules/chatGroup/edit";
    private static final String SHOW_PAGE = "modules/chatGroup/show";
    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private ChatGroupService chatGroupService;

    @Autowired
    private HttpServletRequest request;
    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${chat.group.name.exists}")
    private String chatGroupNameExists;

    /**
     * 获取表格页面
     * @return list page
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 分页查询
     * @param query query
     * @return PageGridBean
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final ChatGroupQuery query) {
        try {
            if (query != null) {
                query.setCreateDataUsername(SystemHelper.getCurrentUsername());
                Page<ChatGroupDo> result = chatGroupService.findAll(query);
                return new PageGridBean(query, result, true);
            }
            return new PageGridBean(PageGridBean.FAULT);
        } catch (Exception e) {
            logger.error("分页查询讨论组信息异常：" + e.getMessage(), e);
            return new PageGridBean(false);
        }
    }

    /**
     * 获取添加页面
     * @param map null
     * @return add page
     */
    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public String getAddPage(ModelMap map) {
        return ADD_PAGE;
    }

    /**
     * 添加聊天分组
     * @param form
     * @return
     */
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean addDevice(@ModelAttribute("form") final ChatGroupDo form) {
        try {
            if (form == null) {
                return new JsonResultBean(JsonResultBean.FAULT);
            } // 获取操作用户的IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            ChatGroupQuery query = new ChatGroupQuery();
            query.setGroupName(form.getGroupName());
            ChatGroupDo temp = this.chatGroupService.findByName(query);
            if (temp != null) {
                return new JsonResultBean(JsonResultBean.FAULT, chatGroupNameExists);
            }
            Integer result = chatGroupService.saveChatGroup(form, ipAddress);
            if (result > 0) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        } catch (Exception e) {
            logger.error("新增聊天分组", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 批量删除
     * @return JsonResultBean
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (StringUtils.isNotEmpty(items)) {
                // 获取操作用户的IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                Integer result = chatGroupService.delBathChatGroup(items, ipAddress);
                if (result > 0) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("批量删除聊天分组异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 删除信息
     * @param id
     * @return JsonResultBean
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (id != null && !id.isEmpty()) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                chatGroupService.delChatGroup(id, ip);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("删除检测功能类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 修改讨论组
     * @param id id
     * @return ModelAndView
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            // 设备所在的组织
            ChatGroupDo form = chatGroupService.getChatGroup(id);
            mav.addObject("result", form);
            return mav;
        } catch (Exception e) {
            logger.error("修改聊天分组弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 预览讨论组
     * @param id id
     * @return ModelAndView
     */
    @RequestMapping(value = "/show_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView showPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(SHOW_PAGE);
            // 设备所在的组织
            ChatGroupDo form = chatGroupService.getChatGroup(id);
            mav.addObject("result", form);
            return mav;
        } catch (Exception e) {
            logger.error("修改聊天分组弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改聊天分组
     * @param form form
     * @return JsonResultBean
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean edit(@ModelAttribute("form") final ChatGroupDo form) {
        try {
            if (form == null) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            ChatGroupQuery query = new ChatGroupQuery();
            query.setGroupName(form.getGroupName());
            ChatGroupDo temp = this.chatGroupService.findByName(query);
            if (temp != null && !temp.getGroupId().equals(form.getGroupId())) {
                return new JsonResultBean(JsonResultBean.FAULT, chatGroupNameExists);
            }
            // 获取操作用户的IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Integer result = chatGroupService.updateChatGroup(form, ipAddress);
            if (result > 0) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("修改聊天分组异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

}
