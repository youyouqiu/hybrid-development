package com.zw.platform.controller.monitoring;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.param.form.CommandParametersForm;
import com.zw.platform.domain.systems.DeviceUpgrade;
import com.zw.platform.domain.systems.query.DeviceUpgradeQuery;
import com.zw.platform.domain.vas.monitoring.MonitorCommandBindForm;
import com.zw.platform.domain.vas.monitoring.query.CommandParametersQuery;
import com.zw.platform.service.monitoring.CommandParametersService;
import com.zw.platform.service.systems.DeviceUpgradeService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.VehicleUtil;
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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 指令参数
 */
@Controller
@RequestMapping("/v/monitoring/commandParam")
public class CommandParametersController {

    private Logger logger = LogManager.getLogger(CommandParametersController.class);

    private static final String LIST_PAGE = "vas/monitoring/commandParam/list";

    private static final String EDIT_PAGE = "vas/monitoring/commandParam/edit";

    private static final String BIND_PAGE = "vas/monitoring/commandParam/bind";

    private static final String EDIT_UPGRADE_FILE_PAGE = "vas/monitoring/commandParam/upgradeFile";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private CommandParametersService commandParametersService;

    @Autowired
    private DeviceUpgradeService deviceUpgradeService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @Auth
    public String getListPage() {
        return LIST_PAGE;
    }

    /**
     * 列表
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getList(CommandParametersQuery query) {
        try {
            if (query != null) {
                Page<CommandParametersForm> list = commandParametersService.getList(query);
                return new PageGridBean(list, true);
            }
            return new PageGridBean(PageGridBean.FAULT);
        } catch (Exception e) {
            logger.error("获取指令参数类别异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @RequestMapping(value = "/bind_{vid}_{commandType}_{deviceType}", method = RequestMethod.GET)
    public ModelAndView addPage(@PathVariable("vid") String vid, @PathVariable("commandType") String commandType,
        @PathVariable("deviceType") Integer deviceType) {
        try {
            ModelAndView mav = new ModelAndView(BIND_PAGE);
            List<MonitorCommandBindForm> referVehicleList = new ArrayList<>();
            if (!StringUtils.isEmpty(vid) && !StringUtils.isEmpty(commandType)) {
                referVehicleList = commandParametersService.findReferVehicle(commandType, null, deviceType);
            }
            mav.addObject("commandName", commandParametersService.getCommandName(commandType));
            mav.addObject("brand", findBrandByVid(vid));
            mav.addObject("commandType", commandType);
            mav.addObject("vid", vid);
            mav.addObject("referVehicleList", JSON.toJSONString(referVehicleList));
            //用于判断页面是否为批量设置调整，批量设置无读取功能
            mav.addObject("settingmore", 0);
            return mav;
        } catch (Exception e) {
            logger.error("指令参数设置页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 批量设置
     */
    @RequestMapping(value = "/settingMore_{vid}_{commandType}_{deviceType}.gsp", method = RequestMethod.GET)
    public ModelAndView settingMore(@PathVariable("vid") String vid, @PathVariable("commandType") String commandType,
        @PathVariable("deviceType") Integer deviceType, HttpServletResponse response) {
        try {
            if (StringUtils.isBlank(vid)) {
                response.setContentType("text/htmlcharset=utf-8");
                PrintWriter out = response.getWriter();
                out.println("<script language='javascript'>");
                out.println("$('#commonWin').modal('hide');");
                out.println("layer.msg('请选择一条数据！');");
                out.println("</script>");
                return null;
            } else {
                ModelAndView mav = new ModelAndView(BIND_PAGE);
                List<MonitorCommandBindForm> referVehicleList = new ArrayList<>();
                if (!StringUtils.isEmpty(vid) && !StringUtils.isEmpty(commandType)) {
                    referVehicleList = commandParametersService.findReferVehicle(commandType, null, deviceType);
                }
                mav.addObject("commandName", commandParametersService.getCommandName(commandType));
                mav.addObject("brand", findBrandByVid(vid));
                mav.addObject("commandType", commandType);
                mav.addObject("vid", vid);
                mav.addObject("referVehicleList", JSON.toJSONString(referVehicleList));
                //用于判断页面是否为批量设置调整，批量设置无读取功能
                mav.addObject("settingmore", 1);
                return mav;
            }
        } catch (Exception e) {
            logger.error("指令参数设置页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    private String findBrandByVid(String id) {
        Set<String> moIds = Arrays.stream(id.split(",")).collect(Collectors.toSet());
        return VehicleUtil.batchGetBindInfosByRedis(moIds, Lists.newArrayList("name")).values()
            .stream()
            .map(BindDTO::getName)
            .collect(Collectors.joining(","));
    }

    @RequestMapping(value = "/edit_{vid}_{commandType}_{deviceType}", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable("vid") String vid, @PathVariable("commandType") String commandType,
        @PathVariable("deviceType") Integer deviceType) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            List<MonitorCommandBindForm> referVehicleList = new ArrayList<>();
            if (!StringUtils.isEmpty(vid) && !StringUtils.isEmpty(commandType)) {
                //获取设置参数信息
                commandParametersService.findInfo(mav, vid, commandType);
                referVehicleList = commandParametersService.findReferVehicle(commandType, vid, deviceType);
            }
            mav.addObject("commandType", commandType);
            mav.addObject("commandName", commandParametersService.getCommandName(commandType));
            mav.addObject("brand", findBrandByVid(vid));
            mav.addObject("vid", vid);
            mav.addObject("referVehicleList", JSON.toJSONString(referVehicleList));
            return mav;
        } catch (Exception e) {
            logger.error("", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public JsonResultBean delete(String id, String vid, String commandType) {
        try {
            if (!StringUtils.isEmpty(id) && !StringUtils.isEmpty(vid) && !StringUtils.isEmpty(commandType)) {
                return commandParametersService.delete(id, vid, commandType);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("删除指令参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 读取
     * @param videoTactics 拍照参数定时定距判断
     */
    @RequestMapping(value = "/getParam", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getParam(String vid, String commandType, String videoTactics) {
        try {
            if (StringUtils.isNotBlank(vid) && StringUtils.isNotBlank(commandType)) {
                return commandParametersService.sendParam(vid, commandType, videoTactics);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("读取指令参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取参考对象信息
     */
    @RequestMapping(value = "/getReferenceInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getReferenceInfo(String vid, String commandType) {
        try {
            if (StringUtils.isNotBlank(vid) && StringUtils.isNotBlank(commandType)) {
                return commandParametersService.getReferenceInfo(vid, commandType);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("获取参考对象信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 指令参数-下发参数
     * @param commandType 指令类型 11:通讯参数; 12:终端参数; 131:无线升级; 132:控制终端连接指定服务器;
     *                    133:终端关机; 134:终端复位; 135:恢复出厂设置; 136: 关闭数据通道; 137:关闭所有无线通信;
     *                    14:位置汇报参数; 16:电话参数; 17:视频拍照参数;
     *                    18:GNSS参数; 19:事件设置; 20:电话本设; 21:信息点播菜单; 22:基站参数设置
     *                    24:RS232串口参数; 25:RS485串口参数; 26:CAN总线参数;
     *                    311:删除所有圆形; 312:删除所有矩形; 313:删除所有多边形; 314:删除所有路线 138:下发升级包
     */
    @RequestMapping(value = "/sendParamByCommandType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendParamByCommandType(String monitorIds, Integer commandType, String upgradeType) {
        try {
            return commandParametersService.sendParamByCommandType(monitorIds, commandType, upgradeType);
        } catch (Exception e) {
            logger.error("指令参数-下发参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 指令参数-保存参数
     * @param commandType 指令类型 11:通讯参数; 12:终端参数; 131:无线升级; 132:控制终端连接指定服务器;
     *                    133:终端关机; 134:终端复位; 135:恢复出厂设置; 136: 关闭数据通道; 137:关闭所有无线通信;
     *                    14:位置汇报参数; 16:电话参数; 17:视频拍照参数;
     *                    18:GNSS参数; 19:事件设置; 20:电话本设; 21:信息点播菜单; 22:基站参数设置
     *                    24:RS232串口参数; 25:RS485串口参数; 26:CAN总线参数;
     *                    311:删除所有圆形; 312:删除所有矩形; 313:删除所有多边形; 314:删除所有路线;
     */
    @RequestMapping(value = "/saveParamByCommandType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveParamByCommandType(String monitorIds, String paramJsonStr, Integer commandType) {
        try {
            return commandParametersService.saveParamByCommandType(null, monitorIds, paramJsonStr, commandType);
        } catch (Exception e) {
            logger.error("指令参数-保存参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 指令参数-保存参数
     * @param commandType 138:下发升级包
     */
    @RequestMapping(value = "/saveParamByCommandTypeWithFile", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveParamByCommandTypeWithFile(@RequestParam(value = "file", required = false)
        MultipartFile file, String monitorIds, String paramJsonStr, Integer commandType) {
        try {
            if (file != null) {
                String originalFilename = file.getOriginalFilename();
                String regex = "^(.+)_(.+)_(.+)_(.+)_(.+)\\.(.+)$";
                if (!originalFilename.matches(regex)) {
                    return new JsonResultBean(JsonResultBean.FAULT, "上传文件不符合升级文件命名规范!");
                }
            }
            return commandParametersService.saveParamByCommandType(file, monitorIds, paramJsonStr, commandType);
        } catch (Exception e) {
            logger.error("指令参数-保存参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/saveDeviceUpgrade", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveDeviceUpgrade(@RequestParam(value = "file", required = false) MultipartFile file,
        DeviceUpgrade deviceUpgrade) {
        try {
            if (file == null && StringUtils.isEmpty(deviceUpgrade.getUpgradeFileId())) {
                return new JsonResultBean(JsonResultBean.FAULT, "升级文件不能为空");
            }
            if (deviceUpgrade.getUpgradeType() == null) {
                return new JsonResultBean(JsonResultBean.FAULT, "升级类型为空");
            }
            // if (file != null &&  file.getBytes().length > 10 * 1000 * 1024) {
            //     //文件不能大于10M
            //     return new JsonResultBean(JsonResultBean.FAULT, "文件不能大于10M");
            // }
            //得到文件的流
            String message = deviceUpgradeService.addDeviceUpgradeFile(file, deviceUpgrade);
            return new JsonResultBean(JsonResultBean.SUCCESS, message);
        } catch (Exception e) {
            logger.error("指令参数-保存参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/deviceUpgradeList", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean deviceUpgradeList(DeviceUpgradeQuery query) {
        try {
            Page<DeviceUpgrade> result = deviceUpgradeService.queryList(query);
            return new PageGridBean(result, true);
        } catch (Exception e) {
            logger.error("指令参数-分页查询异常", e);
            return new PageGridBean(JsonResultBean.FAULT);
        }
    }

    @RequestMapping(value = "/deleteDeviceUpgrade_{upgradeFileId}", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteDeviceUpgrade(@PathVariable("upgradeFileId") String upgradeFileId) {
        try {
            deviceUpgradeService.deleteDeviceUpgradeById(upgradeFileId);
            return new JsonResultBean(JsonResultBean.SUCCESS, "删除升级文件成功");
        } catch (Exception e) {
            logger.error("指令参数-删除文件异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }


    /**
     * 指令参数-下发单条存储多媒体数据检索上传命令
     */
    @RequestMapping(value = "/sendOneMediaSearchUp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendOneMediaSearchUp(String monitorId, Integer mediaId, Integer deleteSign) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("msgSN", commandParametersService.sendOneMediaSearchUpMsg(monitorId, mediaId, deleteSign));
            return new JsonResultBean(msg);
        } catch (Exception e) {
            logger.error("指令参数-下发单条存储多媒体数据检索上传命令异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 指令参数-删除终端围栏
     */
    @RequestMapping(value = "/sendDeleteDeviceFence", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendDeleteDeviceFence(String monitorIds, Integer commandType, Integer deviceFence) {
        try {
            return commandParametersService.sendDeleteDeviceFence(monitorIds, commandType, deviceFence);
        } catch (Exception e) {
            logger.error("指令参数-删除终端围栏异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}