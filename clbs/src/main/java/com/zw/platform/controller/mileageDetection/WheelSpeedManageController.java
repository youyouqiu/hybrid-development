package com.zw.platform.controller.mileageDetection;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.vas.mileageSensor.MileageSensor;
import com.zw.platform.domain.vas.mileageSensor.MileageSensorQuery;
import com.zw.platform.service.mileageSensor.MileageSensorService;
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
 * <p> Title:里程传感器基础信息 <p> Copyright: Copyright (c) 2016 <p> Company: ZhongWei <p> team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月16日 11:16
 */
@Controller
@RequestMapping("/v/meleMonitor/wheelSpeedSensor")
public class WheelSpeedManageController {
    @Autowired
    private MileageSensorService service;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${add.success}")
    private String addSuccess;

    @Value("${set.success}")
    private String setSuccess;

    @Value("${mileage.sensor.type.exist}")
    private String mileageSensorTypeExist;

    @Value("${mileage.sensor.type.use}")
    private String mileageSensorTypeUse;

    private static Logger logger = LogManager.getLogger(WheelSpeedManageController.class);

    private static final String LIST_PAGE = "vas/meleMonitor/wheelSpeedSensor/list";

    private static final String ADD_PAGE = "vas/meleMonitor/wheelSpeedSensor/add";

    private static final String EDIT_PAGE = "vas/meleMonitor/wheelSpeedSensor/edit";

    private static final String IMPORT_PAGE = "vas/meleMonitor/wheelSpeedSensor/import";

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

    @RequestMapping(value = {"/edit"}, method = RequestMethod.GET)
    public String editPage() {
        return EDIT_PAGE;
    }

    /**
     * 返回修改轮速传感器界面
     *
     * @param id
     * @return
     */
    @RequestMapping(value = {"/edit_{id}"}, method = RequestMethod.GET)
    public ModelAndView editMobileSourceBaseInfo(@PathVariable("id") String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            MileageSensor form = service.findById(id);
            mav.addObject("result", form);
            return mav;
        } catch (Exception e) {
            logger.error("修改传感器弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 获取传感器信息
     *
     * @param id
     * @return
     */
    @RequestMapping(value = {"/getInfo_{id}"}, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getInfo(@PathVariable("id") String id) {
        try {
            if (id != null && !"".equals(id)) {
                MileageSensor form = service.findById(id);
                return new JsonResultBean(JsonResultBean.SUCCESS, JSON.toJSONString(form));
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("获取传感器信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 修改轮速传感器信息
     *
     * @param form
     * @return
     */
    @RequestMapping(value = {"/edit"}, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean edit(final MileageSensor form) {
        try {
            if (form != null) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return service.updateMileageSensor(form, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("修改轮速传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 新增轮速传感器信息
     *
     * @param form
     * @return
     */
    @RequestMapping(value = {"/add"}, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean savePage(final MileageSensor form) {
        try {
            if (form != null) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return service.addMileageSensor(form, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("新增轮速传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 根据轮速传感器id删除里程传感器信息
     *
     * @param id 轮速传感器id
     * @return JsonResultBean
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (id != null && !id.isEmpty()) {
                List<String> ids = new ArrayList<>();
                ids.add(id);
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                JsonResultBean result = service.deleteBatchMileageSensor(ids, ip);
                if (StringUtils.isNotBlank(result.getMsg())) {
                    return new JsonResultBean(JsonResultBean.FAULT, result.getMsg());
                }
                return result;
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("删除轮速传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据轮速传感器id批量删除轮速传感器信息
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
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return service.deleteBatchMileageSensor(ids, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("批量删除轮速传感器", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 分页查询
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public PageGridBean list(final MileageSensorQuery query) {
        try {
            Page<MileageSensor> result = service.findByQuery(query);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            logger.error("分页查询分组（findByQuery）异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 弹出导入轮速传感器界面
     *
     * @return
     */
    @RequestMapping(value = {"/import"}, method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }

    /**
     * 导入轮速传感器信息
     *
     * @param file
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importFluxSensor(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request); // 客户端的IP地址
            Map resultMap = service.addImportSensor(file, ipAddress);
            String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            logger.error("导入轮速传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 下载模板
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void downloadTank(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "轮速传感器信息列表模板");
            service.generateTemplate(response);
        } catch (Exception e) {
            logger.error("下载轮速传感器信息列表模板异常", e);
        }
    }

    /**
     * 导出
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void exportTank(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "轮速传感器信息列表");
            service.export(null, 1, response);
        } catch (Exception e) {
            logger.error("导出轮速传感器信息列表异常", e);
        }
    }
}
