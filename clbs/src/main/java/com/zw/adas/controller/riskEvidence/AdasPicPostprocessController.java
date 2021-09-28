package com.zw.adas.controller.riskEvidence;

import com.github.pagehelper.Page;
import com.zw.adas.domain.riskManagement.query.PicProcessPageQuery;
import com.zw.adas.service.riskEvidence.AdasPicPostprocessService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * ADAS图片后处理配置
 *
 * @author Zhang Yanhui
 * @since 2021/7/12 16:02
 */

@Slf4j
@Controller
@RequestMapping("/adas/risk/pic-postprocess")
public class AdasPicPostprocessController {

    @Autowired
    private AdasPicPostprocessService adasPicPostprocessService;

    /**
     * 报警图片处理管理
     */
    private static final String LIST_PAGE = "modules/imgAlarmDispose/list";

    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String getListPage() {
        return LIST_PAGE;
    }

    @ResponseBody
    @RequestMapping(value = {"/list"}, method = RequestMethod.POST)
    public PageGridBean page(@Valid PicProcessPageQuery query) {
        try {
            final Page<?> page = adasPicPostprocessService.page(query);
            return new PageGridBean(query, page, true);
        } catch (Exception e) {
            log.error("查询图片处理列表失败", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/add"}, method = RequestMethod.POST)
    public JsonResultBean add(String monitorIds) {
        if (StringUtils.isBlank(monitorIds)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        final Set<String> monitorIdSet = new HashSet<>(Arrays.asList(monitorIds.split(",")));
        if (CollectionUtils.isEmpty(monitorIdSet)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        try {
            int count = adasPicPostprocessService.add(monitorIdSet);
            return new JsonResultBean(count);
        } catch (Exception e) {
            log.error("新增处理车辆失败", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/remove"}, method = RequestMethod.POST)
    public JsonResultBean remove(String monitorId) {
        if (StringUtils.isEmpty(monitorId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        try {
            final int count = adasPicPostprocessService.batchRemove(Collections.singleton(monitorId));
            return new JsonResultBean(count);
        } catch (Exception e) {
            log.error("移除处理车辆失败", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/batchRemove"}, method = RequestMethod.POST)
    public JsonResultBean batchRemove(String monitorIds) {
        if (StringUtils.isBlank(monitorIds)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        final Set<String> monitorIdSet = new HashSet<>(Arrays.asList(monitorIds.split(",")));
        if (CollectionUtils.isEmpty(monitorIdSet)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        try {
            int count = adasPicPostprocessService.batchRemove(monitorIdSet);
            return new JsonResultBean(count);
        } catch (Exception e) {
            log.error("移除处理车辆失败", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

}
