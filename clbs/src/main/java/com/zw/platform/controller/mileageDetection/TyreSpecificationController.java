package com.zw.platform.controller.mileageDetection;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.vas.mileageSensor.TyreSize;
import com.zw.platform.domain.vas.mileageSensor.TyreSizeQuery;
import com.zw.platform.service.mileageSensor.TyreSizeService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by LiaoYuecai on 2017/5/16.
 */
@Controller
@RequestMapping("/v/meleMonitor/tyreSpecification")
public class TyreSpecificationController {
    private static Logger log = LogManager.getLogger(TyreSpecificationController.class);

    @Autowired
    private TyreSizeService service;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${tyre.size.set}")
    private String tyreSizeSet;

    @Value("${tyre.size.use}")
    private String tyreSizeUse;

    @Value("${set.success}")
    private String setSuccess;

    @Value("${add.success}")
    private String addSuccess;

    private static final String LIST_PAGE = "vas/meleMonitor/tyreSpecification/list";

    private static final String ADD_PAGE = "vas/meleMonitor/tyreSpecification/add";

    private static final String EDIT_PAGE = "vas/meleMonitor/tyreSpecification/edit";

    private static final String IMPORT_PAGE = "vas/meleMonitor/tyreSpecification/import";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = {"/add"}, method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }

    /**
     * ????????????????????????
     *
     * @param id
     * @return
     */
    @RequestMapping(value = {"/edit_{id}"}, method = RequestMethod.GET)
    public ModelAndView editMobileSourceBaseInfo(@PathVariable("id") String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            TyreSize form = service.findById(id);
            mav.addObject("result", form);
            return mav;
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ????????????????????????
     *
     * @param form
     * @return
     */
    @RequestMapping(value = {"/edit"}, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean edit(final TyreSize form) {
        try {
            if (form != null) {
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                return service.updateTyreSize(form, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????id????????????????????????
     *
     * @param id
     * @return
     */
    @RequestMapping(value = {"/getInfo_{id}"}, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getInfo(@PathVariable("id") String id) {
        try {
            if (!id.isEmpty()) {
                TyreSize form = service.findById(id);
                return new JsonResultBean(JsonResultBean.SUCCESS, JSON.toJSONString(form));
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????????????????
     *
     * @param form
     * @return
     */
    @RequestMapping(value = {"/add"}, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean savePage(final TyreSize form) {
        try {
            if (form != null) {
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                return service.addTyreSize(form, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * ????????????
     *
     * @param id
     * @return JsonResultBean
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (id != null && !id.isEmpty()) {
                List<String> ids = new ArrayList<>();
                ids.add(id);
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                JsonResultBean result = service.deleteBatchTyreSize(ids, ip);
                if (StringUtils.isNotBlank(result.getMsg())) {
                    return new JsonResultBean(JsonResultBean.FAULT, result.getMsg());
                }
                return result;
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????????????????
     *
     * @return JsonResultBean
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (items != null && !items.isEmpty()) {
                String[] item = items.split(",");
                List<String> ids = Arrays.asList(item);
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                return service.deleteBatchTyreSize(ids, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????????????????
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public PageGridBean list(final TyreSizeQuery query) {
        try {
            Page<TyreSize> result = service.findByQuery(query);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            log.error("??????????????????", e);
            return new PageGridBean(false);
        }
    }

    /**
     * ??????????????????
     *
     * @return String
     */
    @RequestMapping(value = {"/import"}, method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importFluxSensor(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request); // ????????????IP??????
            Map resultMap = service.addImportTyreSize(file, ipAddress);
            String msg = "???????????????" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void downloadTank(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "??????????????????????????????");
            service.generateTemplate(response);
        } catch (Exception e) {
            log.error("??????????????????????????????????????????", e);
        }
    }

    /**
     * ????????????????????????
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void exportTank(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "????????????????????????");
            service.export(null, 1, response);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
        }
    }
}
