package com.zw.platform.controller.OBDManager;

import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.form.OBDVehicleTypeForm;
import com.zw.platform.domain.basicinfo.query.OBDVehicleTypeQuery;
import com.zw.platform.service.obdManager.OBDVehicleTypeService;
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

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * OBD车型配置
 * create by denghuabing 2018.12.25
 * @author Administrator
 */
@Controller
@RequestMapping("/v/obdManager/obdVehicleType")
public class OBDVehicleTypeController {
    private final Logger logger = LogManager.getLogger(OBDVehicleTypeController.class);

    @Autowired
    private OBDVehicleTypeService obdVehicleTypeService;

    private static final String LIST_PAGE = "vas/obdManager/obdVehicleType/list";

    private static final String ADD_PAGE = "vas/obdManager/obdVehicleType/add";

    private static final String EDIT_PAGE = "vas/obdManager/obdVehicleType/edit";

    private static final String IMPOTR_PAGE = "vas/obdManager/obdVehicleType/import";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @Auth
    public String getListPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public String addPage() {
        return ADD_PAGE;
    }

    @RequestMapping(value = "/edit_{id}", method = RequestMethod.GET)
    public ModelAndView updatePage(@PathVariable("id") String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            OBDVehicleTypeForm form = obdVehicleTypeService.findById(id);
            mav.addObject("result", form);
            return mav;
        } catch (Exception e) {
            logger.error("获取OBD车型配置修改页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/import", method = RequestMethod.GET)
    public String importPage() {
        return IMPOTR_PAGE;
    }

    /**
     * 列表查询
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getList(OBDVehicleTypeQuery obdVehicleTypeQuery) {
        try {
            Page<OBDVehicleTypeForm> result = obdVehicleTypeService.getList(obdVehicleTypeQuery);
            return new PageGridBean(result, true);
        } catch (Exception e) {
            logger.error("获取车型配置列表异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 新增
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @AvoidRepeatSubmitToken(removeToken = true)
    @ResponseBody
    public JsonResultBean addVehicleType(OBDVehicleTypeForm form) {
        try {
            if (form != null) {
                return obdVehicleTypeService.addVehicleType(form);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("新增OBD车型配置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 修改
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean updateVehicleType(OBDVehicleTypeForm form) {
        try {
            if (form != null) {
                return obdVehicleTypeService.updateVehicleType(form);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("修改OBD车型配置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 删除
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") String id) {
        try {
            if (StringUtils.isNotEmpty(id)) {
                return obdVehicleTypeService.delete(id);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("删除OBD车型配置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导入
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importVehicleType(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            if (file != null) {
                Map<String, Object>  resultMap = obdVehicleTypeService.importVehicleType(file);
                String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
                return new JsonResultBean(true, msg);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("导入OBD车型配置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 下载模板
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "OBD车型配置模板");
            obdVehicleTypeService.generateTemplate(response);
        } catch (Exception e) {
            logger.error("OBD车型配置下载模板异常", e);
        }
    }

    /**
     * 导出
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response, String query) {
        try {
            ExportExcelUtil.setResponseHead(response, "OBD车型配置");
            obdVehicleTypeService.export(null, 1, response, query);
        } catch (Exception e) {
            logger.error("导出OBD车型配置异常", e);
        }
    }

    /**
     * 校验名称是否重复
     */
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(String name, Integer type, String id) {
        try {
            return obdVehicleTypeService.repetition(name, type, id);
        } catch (Exception e) {
            logger.error("校验名称是否重复异常", e);
            return false;
        }
    }

    /**
     * 校验车型id是否重复
     */
    @RequestMapping(value = "/checkCode", method = RequestMethod.POST)
    @ResponseBody
    public boolean checkCode(String code, String id) {
        try {
            return obdVehicleTypeService.checkCode(code, id);
        } catch (Exception e) {
            logger.error("校验车型id是否重复异常", e);
            return false;
        }
    }

    /**
     * 根据code查询车型
     */
    @RequestMapping(value = "/findByCode", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findByCode(String code) {
        try {
            if (StringUtils.isNotEmpty(code)) {
                return obdVehicleTypeService.findByCode(code);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("根据code查询车型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}
