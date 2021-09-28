package com.zw.platform.controller.realTimeVideo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.realTimeVideo.VideoSetting;
import com.zw.platform.domain.vas.alram.query.AlarmSettingQuery;
import com.zw.platform.service.realTimeVideo.VideoParamSettingService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MapUtil;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.vo.realTimeVideo.VideoParamVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;


/**
 * 视频参数设置
 *
 * @author hujun
 * @date 2017年12月28日上午12:28:00
 */
@Controller
@RequestMapping("/realTimeVideo/videoSetting")
public class VideoParamSettingController {
    private static Logger log = LogManager.getLogger(VideoParamSettingController.class);

    @Autowired
    private VideoParamSettingService videoParamSettingService;

    private static final String LIST_PAGE = "vas/monitoring/videoparametersetting/list";

    private static final String SET_PAGE = "vas/monitoring/videoparametersetting/edit";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    /**
     * 跳转到设置列表页面
     */
    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list() {
        return LIST_PAGE;
    }

    /**
     * 分页查询所有权限内的车辆
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(AlarmSettingQuery query) {
        try {
            Page<Map<String, Object>> findVideoSetting = videoParamSettingService.findVideoSetting(query);
            log.info("分页查询所有车辆视频参数");
            return new PageGridBean(findVideoSetting, true);
        } catch (Exception e) {
            log.error("分页查询所有车辆视频参数异常", e);
            return new PageGridBean(false);
        }

    }

    /**
     * 跳转到设置页面，将参考对象放入session域对象中
     *
     * @param vehicleId 监控对象id
     * @param model     传递数据到前端
     */
    @RequestMapping(value = "/setting/{vehicleId}.gsp{monitorType}", method = RequestMethod.GET)
    public String setting(@PathVariable("vehicleId") String vehicleId, @PathVariable("monitorType") Integer monitorType,
        ModelMap model) {
        try {
            Map<String, String> monitorInfoMap = RedisHelper.hgetAll(RedisKeyEnum.MONITOR_INFO.of(vehicleId));
            String brand = "";
            String deviceType = null;
            if (monitorInfoMap != null) {
                BindDTO bindDTO = MapUtil.mapToObj(monitorInfoMap, BindDTO.class);
                String name = bindDTO.getName();
                deviceType = bindDTO.getDeviceType();
                brand = name == null ? "" : name;
            }
            List<JSONObject> allReferVehicle = videoParamSettingService.getAllReferVehicle(vehicleId, deviceType);
            model.put("vehicles", JSON.toJSONString(allReferVehicle));
            model.put("brand", brand);
            model.put("vehicleId", vehicleId);
            model.put("monitorType", monitorType);
            log.info("跳转到设置视频参数页面");
        } catch (Exception e) {
            log.error("跳转到设置视频参数页面失败", e);
        }
        return SET_PAGE;
    }

    /**
     * 根据车辆id查看视频参数详情
     *
     * @param vehicleId 监控对象id
     * @return JsonResultBean
     */
    @RequestMapping(value = "/view/{vehicleId}", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean view(@PathVariable String vehicleId) {
        try {
            Map<String, Object> videoParam = videoParamSettingService.getVideoParam(vehicleId);
            log.info("查看视频参数");
            return new JsonResultBean(videoParam);
        } catch (Exception e) {
            log.error("查看视频参数失败", e);
            return new JsonResultBean(false, sysErrorMsg);
        }
    }

    /**
     * 保存视频参数
     *
     * @param videoParam 音视频设置参数(包括监控对象id,音视频参数,音视频通道参数,报警参数,录像设置参数,休眠唤醒参数)
     * @return JsonResultBean
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveParam(@RequestParam("videoParam") String videoParam) {
        try {
            if (StringUtils.isNotBlank(videoParam)) {
                videoParamSettingService.saveAllParam(JSON.parseObject(videoParam, VideoParamVo.class));
                log.info("保存视频参数成功");
                return new JsonResultBean("保存成功");
            }
            return new JsonResultBean("保存失败");
        } catch (Exception e) {
            log.error("保存视频参数失败", e);
            return new JsonResultBean(false, sysErrorMsg);
        }
    }

    /**
     * 下发视频参数
     *
     * @param vehicleId 监控对象id
     * @return JsonResultBean
     */
    @RequestMapping(value = "/sendVideoSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendVideoSetting(String vehicleId) {
        try {
            if (StringUtils.isNotBlank(vehicleId)) {
                videoParamSettingService.sendVideoSetting(vehicleId);
                log.info("下发成功");
                return new JsonResultBean(true, "下发成功");
            }
            return new JsonResultBean(false, "下发失败");
        } catch (Exception e) {
            log.error("下发失败", e);
            return new JsonResultBean(false, sysErrorMsg);
        }
    }

    /**
     * 恢复默认
     */
    @RequestMapping(value = "/repristination_{id}", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean repristination(@PathVariable("id") final String vehicleId) {
        try {
            if (vehicleId != null) {
                videoParamSettingService.delete(vehicleId);
                log.info("恢复默认音视频参数成功");
                return new JsonResultBean(true);
            }
            return new JsonResultBean(false);
        } catch (Exception e) {
            log.error("恢复默认音视频参数失败", e);
            return new JsonResultBean(false, sysErrorMsg);
        }
    }

    /**
     * 批量恢复默认
     *
     * @return result
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(String deltems) {
        try {
            if (StringUtils.isNotBlank(deltems)) {
                videoParamSettingService.deleteMore(deltems);
                return new JsonResultBean(true);
            }
            return new JsonResultBean(false);
        } catch (Exception e) {
            log.error("批量恢复默认失败", e);
            return new JsonResultBean(false, sysErrorMsg);
        }
    }

    /**
     * 单独设置音视频参数
     *
     * @param videoParam 视频参数(包括监控对象id 和 逻辑通道号)
     * @return JsonResultBean
     */
    @RequestMapping(value = "/saveVideoParam", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean videoParam(@RequestParam("videoParam") String videoParam) {
        try {
            if (StringUtils.isNotBlank(videoParam)) {
                VideoSetting videoSetting = JSON.parseObject(videoParam, VideoSetting.class);
                videoParamSettingService.saveVideoParam(videoSetting);
                return new JsonResultBean(true);
            }
            return new JsonResultBean(false);
        } catch (Exception e) {
            log.error("保存音视频参数失败", e);
            return new JsonResultBean(false, sysErrorMsg);
        }
    }

    /**
     * 根据车辆id和逻辑通道号查询视频参数
     *
     * @param vehicleId    监控对象
     * @param logicChannel 逻辑通道号
     * @return JsonResultBean
     */
    @RequestMapping(value = "/videoParam", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean videoParam(String vehicleId, String logicChannel) {
        try {
            if (StringUtils.isNotBlank(vehicleId) && StringUtils.isNotBlank(logicChannel)) {
                return videoParamSettingService.getVideoParam(vehicleId, Integer.parseInt(logicChannel));
            } else {
                return new JsonResultBean(false);
            }
        } catch (Exception e) {
            log.error("获取音视频参数失败", e);
            return new JsonResultBean(false, sysErrorMsg);
        }
    }

    /**
     * 根据车辆id单独获取休眠唤醒参数设置值
     *
     * @param vehicleId 监控对象id
     * @return JsonResultBean
     * @author hujun
     */
    @RequestMapping(value = "/getVideoSleepParam", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getVideoSleepParam(String vehicleId) {
        try {
            if (StringUtils.isNotBlank(vehicleId)) {
                return videoParamSettingService.getVideoSleep(vehicleId);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("获取休眠唤醒参数失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
