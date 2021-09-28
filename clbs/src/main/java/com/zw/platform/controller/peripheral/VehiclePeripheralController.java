package com.zw.platform.controller.peripheral;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.query.AssignmentQuery;
import com.zw.platform.domain.vas.f3.Peripheral;
import com.zw.platform.service.sensor.PeripheralService;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.collections4.CollectionUtils;
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
@Controller
@RequestMapping("/v/sensorConfig/vehiclePeripheral")
public class VehiclePeripheralController {
    private static final String LIST_PAGE = "vas/sensorConfig/vehiclePeripheral/list";

    private static final String EDIT_PAGE = "vas/sensorConfig/vehiclePeripheral/edit";

    private static final String ADD_PAGE = "vas/sensorConfig/vehiclePeripheral/add";

    private static final String IMPORT_PAGE = "vas/sensorConfig/vehiclePeripheral/import";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${peripheral.no.exist}")
    private String peripheralNoExist;

    @Value("${peripheral.name.exist}")
    private String peripheralNameExist;

    @Value("${peripheral.id.length.exist}")
    private String peripheralIdAndLengthExist;

    @Autowired
    private PeripheralService peripheralService;

    @Resource
    private HttpServletRequest request;

    private static final Logger logger = LogManager.getLogger(VehiclePeripheralController.class);

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = { "/edit_{peripheralId}" }, method = RequestMethod.GET)
    public ModelAndView editMobileSourceBaseInfo(@PathVariable("peripheralId") String peripheralId) {
        ModelAndView mav = new ModelAndView(EDIT_PAGE);
        try {
            mav.addObject("result", peripheralService.findById(peripheralId));
            return mav;
        } catch (Exception e) {
            logger.error("外设管理弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = { "/edit" }, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean editPage(final Peripheral form) {
        try {
            if (form != null) {
                return peripheralService.updatePeripheral(form);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("修改外设设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 删除外设信息
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (StringUtils.isNotBlank(id)) {
                return peripheralService.deleteById(id);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("删除外设设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (StringUtils.isNotBlank(items)) {
                String[] item = items.split(",");
                return peripheralService.deleteByBatch(item);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("批量删除外设设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    public String add() {
        return ADD_PAGE;
    }

    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean addPeripheral(Peripheral peripheral) {
        try {
            List<Peripheral> tp = this.peripheralService.findByIdentId(peripheral.getIdentId());
            if (!"0XE3".equals(peripheral.getIdentId().toUpperCase())) {
                if (tp != null && tp.size() > 0) {
                    return new JsonResultBean(JsonResultBean.FAULT, peripheralNoExist);
                }
            } else {
                String ml = peripheral.getMsgLength() == null ? "" : peripheral.getMsgLength().toString();
                String idAndLength = peripheral.getIdentId().toUpperCase() + ml;
                for (Peripheral p : tp) {
                    String msgLength = p.getMsgLength() == null ? "" : p.getMsgLength().toString();
                    String newIdAndLength = p.getIdentId().toUpperCase() + msgLength;
                    if (idAndLength.equals(newIdAndLength)) {
                        return new JsonResultBean(JsonResultBean.FAULT, peripheralIdAndLengthExist);
                    }
                }
            }
            List<Peripheral> tp1 = this.peripheralService.getByIdentName(peripheral.getName());
            if (CollectionUtils.isNotEmpty(tp1)) {
                return new JsonResultBean(JsonResultBean.FAULT, peripheralNameExist);
            }
            peripheral.setCreateDataUsername(SystemHelper.getCurrentUsername());
            return peripheralService.addPeripheral(peripheral);
        } catch (Exception e) {
            logger.error("新增外设设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 信息配置-信息录入界面
     */
    @RequestMapping(value = { "/addAllowlist" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addAllow() {
        try {
            JSONObject msg = new JSONObject();
            // 初始化车辆信息
            msg.put("peripheralList", peripheralService.findAllow());
            return new JsonResultBean(msg);
        } catch (Exception e) {
            logger.error("信息配置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 分页查询用户
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public PageGridBean list(final AssignmentQuery query) {
        try {
            return new PageGridBean(query, peripheralService.findByPage(query), true);
        } catch (Exception e) {
            logger.error("分页查询用户（findByPage）异常", e);
            return new PageGridBean(false);
        }
    }

    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importFluxSensor(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            Map<String, Object>  resultMap = peripheralService.addImportPeripheral(file);
            String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            logger.error("导入文件异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 下载模板
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void downloadTank(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "外设管理列表模板");
            peripheralService.generateTemplate(response);
        } catch (Exception e) {
            logger.error("下载外设管理列表模板异常", e);
        }
    }

    /**
     * 导出
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void exportTank(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "外设管理列表");
            peripheralService.export(null, 1, response);
        } catch (Exception e) {
            logger.error("导出文件异常", e);
        }
    }

}
