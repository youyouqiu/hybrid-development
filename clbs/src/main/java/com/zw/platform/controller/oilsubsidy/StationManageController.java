package com.zw.platform.controller.oilsubsidy;

import com.zw.platform.commons.Auth;
import com.zw.platform.domain.oilsubsidy.station.StationDTO;
import com.zw.platform.domain.oilsubsidy.station.StationQuery;
import com.zw.platform.service.oilsubsidy.StationManageService;
import com.zw.platform.util.common.*;
import com.zw.talkback.common.ControllerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 站点管理
 *
 * @author XK
 */
@Controller
@RequestMapping("/m/station/manage")
public class StationManageController {
    private static final String REQUEST_TOKEN = "avoidRepeatSubmitToken";
    private static final String LIST_PAGE = "/modules/oilSubsidyManage/stationManage/list";

    private static final String ADD_PAGE = "/modules/oilSubsidyManage/stationManage/add";

    private static final String EDIT_PAGE = "/modules/oilSubsidyManage/stationManage/edit";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private StationManageService stationManageService;

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = {"/add"}, method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }

    @RequestMapping(value = {"/edit_{id}.gsp"}, method = RequestMethod.GET)
    public ModelAndView editStationBaseInfo(@PathVariable("id") String id) {
        try {
            StationDTO stationDTO = stationManageService.getById(id);
            ModelAndView view = new ModelAndView(EDIT_PAGE);
            view.addObject("result", stationDTO);
            return view;
        } catch (Exception e) {
            return new ModelAndView(ERROR_PAGE);
        }

    }


    /**
     * 添加站点信息
     *
     * @param stationDTO    站点添加实体类
     * @param bindingResult 校验结果
     * @return 添加结果
     */
    @AvoidRepeatSubmitToken(removeToken = true)
    @RequestMapping(value = {"/add"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated(ValidGroupAdd.class) StationDTO stationDTO,
                              final BindingResult bindingResult, HttpServletRequest request) {
        JsonResultBean result;
        if (bindingResult.hasErrors()) {
            result = new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        } else {
            result = ControllerTemplate.getResult(() -> stationManageService.add(stationDTO));
        }

        //报错后把上一次的token放入session中
        if (!result.isSuccess()) {
            String clientToken = request.getParameter(REQUEST_TOKEN);
            request.getSession(false).setAttribute(REQUEST_TOKEN, clientToken);
        }
        return result;
    }

    /**
     * 更新站点信息
     *
     * @param stationDTO    站点更新请求体
     * @param bindingResult 校验结果
     * @return 更新结果
     */
    @RequestMapping(value = {"/update"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean update(@Validated({ValidGroupUpdate.class}) @ModelAttribute("form") StationDTO stationDTO,
                                 final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT,
                    SpringBindingResultWrapper.warpErrors(bindingResult));
        }
        return ControllerTemplate.getResult(() -> stationManageService.update(stationDTO));
    }

    /**
     * 单个删除站点信息
     *
     * @param id 站点id
     * @return 删除结果
     */
    @RequestMapping(value = {"/delete_{id}.gsp"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") String id) {
        return ControllerTemplate.getResult(() -> stationManageService.delete(id));
    }

    /**
     * 批量删除站点信息
     *
     * @param ids 站点ID集合字符串，多个用逗号隔开
     * @return 删除结果
     */
    @RequestMapping(value = "/deleteMore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(final String ids) {
        if (StringUtils.isBlank(ids)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数不能为空");
        }
        List<String> idList = new ArrayList<>(Arrays.asList(ids.split(",")));

        List<String> useNames = stationManageService.getUsedName(idList);
        JsonResultBean result = ControllerTemplate.getResult(() -> stationManageService.delBatch(idList));
        if (!result.isSuccess()) {
            return result;
        }

        //重新封装批量删除的返回信息
        String message = "成功删除" + result.getObj() + "条记录";
        if (useNames.isEmpty()) {
            message = message + "!";
        } else {
            message = message + "。" + StringUtils.join(useNames, "、") + "这" + useNames.size()
                    + "个站已被使用，不能被删除哦!";
        }
        return new JsonResultBean(JsonResultBean.SUCCESS, message);
    }

    /**
     * 获取站点详情
     *
     * @param id id
     * @return 站点详情
     */
    @RequestMapping(value = {"/detail_{id}.gsp"}, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getById(@PathVariable("id") String id) {
        return ControllerTemplate.getResult(() -> stationManageService.getById(id));
    }

    /**
     * 分页查询站点信息
     *
     * @param query 分页查询请求体
     * @return 分页查询信息
     */
    @RequestMapping(value = {"/list"}, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(StationQuery query) {
        return ControllerTemplate.getResultBean(() -> stationManageService.getListByKeyword(query), query, "");
    }

    /**
     * @return
     */
    @RequestMapping(value = {"/getAll"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAll() {
        return ControllerTemplate.getResultBean(() -> stationManageService.getAll());
    }
}