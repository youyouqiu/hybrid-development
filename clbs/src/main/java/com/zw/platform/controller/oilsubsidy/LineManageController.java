package com.zw.platform.controller.oilsubsidy;

import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.oilsubsidy.line.DirectionDTO;
import com.zw.platform.domain.oilsubsidy.line.LineDTO;
import com.zw.platform.domain.oilsubsidy.line.LineQuery;
import com.zw.platform.service.oilsubsidy.LineManageService;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.talkback.common.ControllerTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * 线路管理
 * @author XK
 */
@Controller
@RequestMapping("/m/line/manage")
@Slf4j
@Validated
public class LineManageController {

    private static final String LIST_PAGE = "/modules/oilSubsidyManage/lineManage/list";

    private static final String ADD_PAGE = "/modules/oilSubsidyManage/lineManage/add";

    private static final String EDIT_PAGE = "/modules/oilSubsidyManage/lineManage/edit";

    private static final String DETAIL_PAGE = "/modules/oilSubsidyManage/lineManage/detail";

    @Autowired
    private LineManageService lineManageService;

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(LineQuery query) {
        return ControllerTemplate.getResultBean(() -> lineManageService.getListByKeyword(query), query, "查询线路出错");
    }

    @RequestMapping(value = {"/add"}, method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }

    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@RequestBody @Valid LineDTO lineDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT,
                SpringBindingResultWrapper.warpErrors(bindingResult));
        }
        String message = checkFirstLastStationRelation(lineDTO);
        if (StringUtils.isNotBlank(message)) {
            return new JsonResultBean(JsonResultBean.FAULT, message);
        }
        return ControllerTemplate.getResultBean(() -> lineManageService.add(lineDTO));
    }

    /**
     * 校验上下行站点
     * @param lineDTO
     * @return
     */
    private String checkFirstLastStationRelation(LineDTO lineDTO) {
        List<DirectionDTO> direction = lineDTO.getDirection();
        DirectionDTO upside = direction.get(0);
        DirectionDTO downside = direction.get(1);

        //上行起点、终点站
        String upsideFirstStationId1 = upside.getFirstStationId();
        String upsideLastStationId1 = upside.getLastStationId();
        //下行起点、终点站
        String downsideFirstStationId1 = downside.getFirstStationId();
        String downsideLastStationId1 = downside.getLastStationId();

        List<String> upsideStationIds = upside.getStationIds();
        List<String> downsideStationIds = downside.getStationIds();

        //下行起点、终点站
        String upsideFirstStationId2 = upsideStationIds.get(0);
        String upsideLastStationId2 = upsideStationIds.get(upsideStationIds.size() - 1);

        //下行起点、终点站
        String downsideFirstStationId2 = downsideStationIds.get(0);
        String downsideLastStationId2 = downsideStationIds.get(downsideStationIds.size() - 1);
        if (!upsideFirstStationId1.equals(upsideFirstStationId2)) {
            return "上行起点站与上行站点信息第一站不一致";
        }
        if (!upsideLastStationId1.equals(upsideLastStationId2)) {
            return "上行终点站与上行站点信息最后一站不一致";
        }
        if (!downsideFirstStationId1.equals(downsideFirstStationId2)) {
            return "下行起点站与下行站点信息第一站不一致";
        }
        if (!downsideLastStationId1.equals(downsideLastStationId2)) {
            return "下行终点站与下行站点信息最后一站不一致";
        }
        return "";
    }

    @RequestMapping(value = { "/edit_{id}" }, method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable("id") String id) {
        return ControllerTemplate.editPage(EDIT_PAGE, () ->  obj2String(id));
    }

    @RequestMapping(value = { "/update" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean update(@RequestBody @Validated LineDTO lineDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT,
                SpringBindingResultWrapper.warpErrors(bindingResult));
        }
        String message = checkFirstLastStationRelation(lineDTO);
        if (StringUtils.isNotBlank(message)) {
            return new JsonResultBean(JsonResultBean.FAULT, message);
        }
        return ControllerTemplate.getResultBean(() -> lineManageService.update(lineDTO));
    }

    @RequestMapping(value = { "/delete_{id}" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id")String id) {
        return ControllerTemplate.getResultBean(() -> lineManageService.delete(id));
    }

    @RequestMapping(value = { "/deleteBatch" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteBatch(String ids) {
        String[] idsStr = ids.split(",");
        String message;
        try {
            message = lineManageService.delBatchReturnStr(Arrays.asList(idsStr));
        } catch (BusinessException e) {
            log.info(e.getMessage(), e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
        if (message.contains("不能被删除")) {
            return new JsonResultBean(JsonResultBean.FAULT, message);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @RequestMapping(value = { "/detail_{id}" }, method = RequestMethod.GET)
    public ModelAndView detail(@PathVariable("id") String id) {
        return ControllerTemplate.editPage(DETAIL_PAGE, () -> obj2String(id));
    }

    @RequestMapping(value = { "/getLineByOrgId/{orgId}" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getLineByOrgId(@PathVariable("orgId") String orgId) {
        return ControllerTemplate.getResultBean(() -> lineManageService.getLineByOrgId(orgId));
    }

    private String obj2String(String id) throws BusinessException {
        return JSON.toJSONString(lineManageService.getById(id));
    }
}
