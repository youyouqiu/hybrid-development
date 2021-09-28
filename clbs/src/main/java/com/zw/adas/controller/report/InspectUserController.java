package com.zw.adas.controller.report;

import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.zw.adas.domain.report.inspectuser.InspectUserDTO;
import com.zw.adas.domain.report.inspectuser.InspectUserQuery;
import com.zw.adas.service.riskdisposerecord.InspectUserService;
import com.zw.platform.basic.core.ConfigHelper;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.excel.ExportExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

/**
 * @author wanxing
 * @Title: 巡检监控人员
 * @date 2020/12/3016:22
 */
@Controller
@RequestMapping("/adas/inspectUser/")
@Slf4j
public class InspectUserController {

    @Autowired
    private InspectUserService inspectUserService;

    @Autowired
    private ConfigHelper configHelper;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListByKeyWord(@Valid InspectUserQuery query, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new PageGridBean(PageGridBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        }
        return inspectUserService.getListByKeyWord(query);
    }

    @RequestMapping(value = "/export", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportList(@Valid InspectUserQuery query, BindingResult bindingResult,
        HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数错误");
        }
        try {
            ExportExcelUtil.setResponseHead(response, "监管平台巡检监控人员报表");
            inspectUserService.export(response, query);
        } catch (Exception e) {
            log.error("导出监控平台巡检人员报表错误", e);
            return new JsonResultBean(JsonResultBean.FAULT, "导出错误，请联系管理员");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @RequestMapping(value = "/answer/{id}", method = RequestMethod.GET)
    @ResponseBody
    public InspectUserDTO answer(@PathVariable String id) {
        InspectUserDTO inspectUserDTO = inspectUserService.getById(id);
        if (inspectUserDTO == null) {
            throw new RuntimeException("数据库中不存在");
        }
        Integer answerStatus = inspectUserDTO.getAnswerStatus();
        if (answerStatus == 2) {
            //过期
            return inspectUserDTO;
        }
        if (answerStatus == 1) {
            String prefix = "/";
            if (!configHelper.isSslEnabled()) {
                prefix = fdfsWebServer.getWebServerUrl();
            }
            inspectUserDTO.setMediaUrl(prefix + inspectUserDTO.getMediaUrl());
            //正常应答
            return inspectUserDTO;
        }
        return inspectUserService.generateUserInfo(inspectUserDTO);
    }



    /**
     * 应答
     * @param id
     * @param image
     * @param type 应答类型，type:1弹窗应答（多个人可以应答），2：列表应答（只允许应答一次）
     * @return
     */
    @RequestMapping(value = "/answer", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean answer(String id, MultipartFile image, Integer type) {

        try {
            JsonResultBean jsonResultBean = checkParameter(image, type);
            if (jsonResultBean != null) {
                return jsonResultBean;
            }
            InspectUserDTO inspectUserDTO = inspectUserService.getById(id);
            if (inspectUserDTO == null) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数Id传递错误");
            }
            if (inspectUserDTO.getAnswerStatus() == 2) {
                return new JsonResultBean(JsonResultBean.FAULT, "超过应答时限，不能再应答");
            }
            if (inspectUserDTO.getExpireTime().getTime() - System.currentTimeMillis() <= 0) {
                inspectUserService.updateAnswerById(id);
                return new JsonResultBean(JsonResultBean.FAULT, "超过应答时限，不能再应答");
            }
            if (type == 1) {
                //列表巡检应答
                if (inspectUserDTO.getAnswerStatus() == 1) {
                    return new JsonResultBean(JsonResultBean.FAULT, "该巡检已应答");
                }
            }
            inspectUserService.updateAndAnswer(type, inspectUserDTO, image);
        } catch (IOException e) {
            log.error("应答巡检人员错误", e);
            return new JsonResultBean(JsonResultBean.FAULT, "应答巡检人员错误,请联系管理员");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    private JsonResultBean checkParameter(MultipartFile image, Integer type) {
        if (image.isEmpty()) {
            return new JsonResultBean(JsonResultBean.FAULT, "图片不能为空");
        }
        if (image.getSize() > 1024 * 1024) {
            return new JsonResultBean(JsonResultBean.FAULT, "图片限制1M");
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.contains("jpeg")) {
            //前端默认转成该图片格式
            return new JsonResultBean(JsonResultBean.FAULT, "图片格式不支持");
        }
        boolean flag = type == null || (type != 1 && type != 2);
        if (flag) {
            return new JsonResultBean(JsonResultBean.FAULT, "类型错误");
        }
        return null;
    }

    @RequestMapping(value = "/check/{id}", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean check(@PathVariable String id) {
        InspectUserDTO inspectUserDTO = inspectUserService.getById(id);
        if (inspectUserDTO.getAnswerStatus() == 2) {
            return new JsonResultBean(JsonResultBean.FAULT, "超过应答时限，不能再应答");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }
}
