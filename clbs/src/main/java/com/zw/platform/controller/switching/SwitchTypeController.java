package com.zw.platform.controller.switching;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.query.SensorConfigQuery;
import com.zw.platform.domain.vas.switching.SwitchType;
import com.zw.platform.service.switching.SwitchTypeService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.excel.ExportExcelUtil;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <p> Title: <p> Copyright: Copyright (c) 2016 <p> Company: ZhongWei <p> team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年06月21日 15:10
 */
@Controller
@RequestMapping("/m/switching/type")
public class SwitchTypeController {

    private static final String LIST_PAGE = "vas/switching/type/list";

    private static final String EDIT_PAGE = "vas/switching/type/edit";

    private static final String ADD_PAGE = "vas/switching/type/add";

    private static final String IMPORT_PAGE = "vas/switching/type/import";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private SwitchTypeService switchTypeService;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${switch.typeid.null}")
    private String switchTypeidNull;

    @Value("${switch.typeid.error}")
    private String switchTypeidError;

    @Value("${switch.typeid.exist}")
    private String switchTypeidExist;

    @Value("${switch.typename.exist}")
    private String switchTypeNameExist;

    @Value("${switch.type.exist}")
    private String switchTypeExist;

    private static Logger log = LogManager.getLogger(SwitchTypeController.class);

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = {"/edit_{id}"}, method = RequestMethod.GET)
    public ModelAndView editMobileSourceBaseInfo(@PathVariable("id") String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            SwitchType form = switchTypeService.findByid(id);
            mav.addObject("result", form);
            return mav;
        } catch (Exception e) {
            log.error("弹出修改监测功能类型页面时异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = {"/edit"}, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean editPage(final SwitchType form) {
        try {
            if (form != null) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return switchTypeService.updateSwitchType(form, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改检测功能类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 删除外设信息
     *
     * @param id
     * @return JsonResultBean
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (id != null && !id.isEmpty()) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return switchTypeService.deleteById(id, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除检测功能类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (items != null && !items.isEmpty()) {
                String[] item = items.split(",");
                List<String> ids = Arrays.asList(item);
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return switchTypeService.deleteBatchSwitchType(ids, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量删除检测功能类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = {"/add"}, method = RequestMethod.GET)
    public String add() {
        return ADD_PAGE;
    }

    @RequestMapping(value = {"/add"}, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean addPlant(SwitchType type) {
        try {
            if (type != null) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return switchTypeService.addSwitchType(type, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增检测功能类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 信息配置-信息录入界面
     *
     * @return String
     * @Title: add
     * @author Liubangquan
     */
    @RequestMapping(value = {"/addAllowlist"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addAllow() {
        try {
            JSONObject msg = new JSONObject();
            msg.put("typeList", switchTypeService.findAllow());
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("信息配置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 分页查询用户
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public PageGridBean list(final SensorConfigQuery query) {
        try {
            Page<SwitchType> result = switchTypeService.findByPage(query);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            log.error("分页查询用户（findByPage）异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 返回导入界面
     *
     * @return
     */
    @RequestMapping(value = {"/import"}, method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importFluxSensor(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request); // Ip地址
            Map resultMap = switchTypeService.addImportSwitchType(file, ipAddress);
            String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("导入异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 下载模板
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void downloadTank(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "检测功能类型模板");
            switchTypeService.generateTemplate(response);
        } catch (Exception e) {
            log.error("下载模板异常", e);
        }
    }

    /**
     * 导出
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void exportTank(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "检测功能类型列表");
            switchTypeService.export(null, 1, response);
        } catch (Exception e) {
            log.error("导出模板异常", e);
        }
    }

    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("id") String id, @RequestParam("state") String state, @RequestParam("flag"
    ) Integer flag) {
        try {
            SwitchType vt = switchTypeService.findByStateRepetition(id, state, flag);
            if (vt == null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("校验检查功能类型存在异常", e);
            return false;
        }
    }
}
