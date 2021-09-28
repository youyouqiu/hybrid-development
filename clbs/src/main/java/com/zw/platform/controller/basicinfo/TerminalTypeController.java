package com.zw.platform.controller.basicinfo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.DeviceChannelSettingInfo;
import com.zw.platform.domain.basicinfo.TerminalTypeInfo;
import com.zw.platform.domain.basicinfo.TerminalTypeQuery;
import com.zw.platform.service.basicinfo.TerminalTypeService;
import com.zw.platform.util.GetIpAddr;
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
import java.util.List;
import java.util.Map;

/**
 * @Author: zjc
 * @Description:终端型号controller(和deviceController进行拆分)
 * @Date: create in 2021/1/5 13:51
 */

@Controller
@RequestMapping("/m/basicinfo/equipment/device")
public class TerminalTypeController {

    @Autowired
    private TerminalTypeService terminalTypeService;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String syError;
    private static final Logger log = LogManager.getLogger(TerminalTypeController.class);

    private static final String TERMINAL_TYPE_ADD = "modules/basicinfo/equipment/device/modelAdd";

    private static final String TERMINAL_TYPE_EDIT = "modules/basicinfo/equipment/device/modelEdit";

    private static final String TERMINAL_TYPE_IMPORT = "modules/basicinfo/equipment/device/modelImport";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    /**
     * 终端管理子模块->终端型号
     * 获取终端型号
     * @return
     */
    @RequestMapping(value = "/terminalTypeList", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getTerminalType(TerminalTypeQuery query) {
        try {
            Page<TerminalTypeInfo> result = terminalTypeService.getTerminalType(query);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            log.error("查询终端类型列表异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 终端型号新增页面
     * @return list page
     */
    @Auth
    @RequestMapping(value = { "/terminalAddPage" }, method = RequestMethod.GET)
    public String terminalTypeAddPage() {
        return TERMINAL_TYPE_ADD;
    }

    /**
     * 终端型号修改页面
     */
    @Auth
    @RequestMapping(value = { "/terminalEditPage_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView terminalTypeEditPage(@PathVariable String id) {
        try {
            ModelAndView mav = new ModelAndView(TERMINAL_TYPE_EDIT);
            TerminalTypeInfo result = terminalTypeService.getTerminalTypeInfoById(id);
            // 终端型号音视频参数
            List<DeviceChannelSettingInfo> deviceChannelSettingInfoList = result.getDeviceChannelSettingInfoList();
            if (deviceChannelSettingInfoList != null && deviceChannelSettingInfoList.size() > 0) {
                String channelListStr = JSON.toJSONString(deviceChannelSettingInfoList);
                JSONArray data = JSONArray.parseArray(channelListStr);
                mav.addObject("data", data);
            }
            // 根据终端型号id查询绑定该终端型号的终端id
            List<String> deviceId = terminalTypeService.queryDeviceInfoByTerminalTypeId(id);
            int bindFlag; // (0:不支持修改 1:支持修改)
            // 不为空,有终端绑定了该终端型号,不支持修改终端型号的终端厂商和终端型号
            if (deviceId != null && deviceId.size() > 0) {
                bindFlag = 1;
            } else { // 为空,没有终端绑定该终端型号,支持修改终端厂商和终端型号
                bindFlag = 0;
            }
            mav.addObject("bindFlag", bindFlag);
            mav.addObject("result", result);
            return mav;
        } catch (Exception e) {
            log.error("终端型号修改页面弹出时出现异常", e);
            return new ModelAndView(ERROR_PAGE);
        }

    }

    /**
     * 终端型号导入页面
     */
    @Auth
    @RequestMapping(value = { "/terminalImportPage" }, method = RequestMethod.GET)
    public String terminalTypeImportPage() {
        return TERMINAL_TYPE_IMPORT;
    }

    /**
     * 下载模终端型号模板
     */
    @RequestMapping(value = "/downloadType", method = RequestMethod.GET)
    public void downloadTerminalType(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "终端型号列表模板");
            terminalTypeService.generateTypeTemplate(response);
        } catch (Exception e) {
            log.error("下载终端型号列表模板异常", e);
        }
    }

    /**
     * 新增终端型号
     */
    @RequestMapping(value = "/addTerminalType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addTerminalTypeInfo(String terminalTypeInfo) {
        try {
            if (StringUtils.isNotBlank(terminalTypeInfo)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                TerminalTypeInfo relevantInfo = JSONObject.parseObject(terminalTypeInfo, TerminalTypeInfo.class);
                terminalTypeService.addTerminalType(relevantInfo, ipAddress);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增终端类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 修改终端型号
     */
    @RequestMapping(value = "/updateTerminalType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateTerminalInfo(String editTerminalTypeInfo) {
        try {
            if (StringUtils.isNotBlank(editTerminalTypeInfo)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                TerminalTypeInfo relevantInfo = JSONObject.parseObject(editTerminalTypeInfo, TerminalTypeInfo.class);
                terminalTypeService.updateTerminalTypeInfo(relevantInfo, ipAddress);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改终端类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 删除终端型号
     */
    @RequestMapping(value = "/deleteTerminalType_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean deleteTerminalType(@PathVariable String id) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Map<String, Object> resultMap = terminalTypeService.deleteTerminalType(id, ipAddress);
            String errorMsg = String.valueOf(resultMap.get("errorMsg"));
            if (StringUtils.isNotBlank(errorMsg)) {
                return new JsonResultBean(JsonResultBean.FAULT, errorMsg);
            } else {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        } catch (Exception e) {
            log.error("删除终端型号异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 批量删除终端型号
     */
    @RequestMapping(value = "/batchDeleteTerminalType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean batchDeleteTerminalType(String ids) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Map<String, Object> resultMap = terminalTypeService.deleteTerminalType(ids, ipAddress);
            String errorMsg = String.valueOf(resultMap.get("errorMsg"));
            if (StringUtils.isNotBlank(errorMsg)) {
                return new JsonResultBean(JsonResultBean.FAULT, errorMsg);
            } else {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        } catch (Exception e) {
            log.error("批量删除终端型号异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 导入终端型号信息
     */
    @RequestMapping(value = "/importTerminalType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importTerminalType(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Map<String, Object> resultMap = terminalTypeService.importTerminalType(file, ipAddress);
            String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("导入终端型号信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 导出终端型号
     */
    @RequestMapping(value = "/exportTerminalInfo", method = RequestMethod.GET)
    @ResponseBody
    public void exportTerminalInfo(HttpServletResponse response, String fuzzyParam) {
        try {
            ExportExcelUtil.setResponseHead(response, "终端型号列表");
            terminalTypeService.exportTerminalType(null, 1, fuzzyParam, response);
        } catch (Exception e) {
            log.error("导出终端型号列表出错", e);
        }
    }

    /**
     * 查询终端厂商列表
     */
    @RequestMapping(value = "/TerminalManufacturer", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getTerminalManufacturerList() {
        try {
            List<String> result = terminalTypeService.getTerminalManufacturer();
            JSONObject msg = new JSONObject();
            msg.put("result", result);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询终端厂商列表出错", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 根据终端厂商名称查询终端信号(因为前台会传递的值包含[]特殊字符，前台报404错误)
     * 参考给地址：https://cloud.tencent.com/developer/article/1142521
     * https://moon-rui.github.io/2018/05/29/tomcat_ie_error.html
     */
    @RequestMapping(value = "/getTerminalTypeByName", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTerminalTypeByName(String name) {
        try {
            List<Map<String, Object>> result = terminalTypeService.getTerminalTypeByName(name);
            JSONObject msg = new JSONObject();
            msg.put("result", result);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询终端厂商列表出错", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }

    }

    /**
     * 根据终端厂商和终端型号校验数据库中是否有重复记录
     * @param terminalType         终端型号
     * @param terminalManufacturer 终端厂商
     */
    @RequestMapping(value = "/verifyTerminalType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean verifyTerminalTypeByManufacturer(String terminalType, String terminalManufacturer) {
        try {
            boolean result = terminalTypeService.verifyTerminalTypeByManufacturer(terminalType, terminalManufacturer);
            if (result) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("根据终端厂商和终端型号校验数据库中是否有重复记录异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    @RequestMapping(value = "/repetitionMacAddress", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean repetitionMacAddress(String deviceId, String macAddress) {
        try {
            boolean result = terminalTypeService.repetitionMacAddress(deviceId, macAddress);
            if (result) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("根据终端Id和Mac地址校验数据库中是否有重复记录异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

}
