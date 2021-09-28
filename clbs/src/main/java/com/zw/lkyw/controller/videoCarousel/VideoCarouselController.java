package com.zw.lkyw.controller.videoCarousel;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.zw.lkyw.service.videoCarousel.VideoCarouselService;
import com.zw.platform.commons.Auth;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.ZipUtil;
import org.springframework.web.multipart.MultipartFile;

/**
 * 视频轮播
 * @author denghuabing on 2019/12/23 10:37
 */
@RequestMapping("/lkyw/videoCarousel")
@Controller
public class VideoCarouselController {

    private Logger log = LogManager.getLogger(VideoCarouselController.class);

    @Autowired
    private VideoCarouselService videoCarouselService;

    private static final String LIST_PAGE = "vas/lkyw/videoCarousel/list";

    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String getListPage() {
        return LIST_PAGE;
    }

    /**
     * 超过5000的树结构
     * @param id
     * @param type      group 企业，assignment 分组
     * @param isChecked 是否勾选
     * @return
     */
    @RequestMapping(value = "/getMonitor", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMonitor(String id, String type, Boolean isChecked) {
        try {
            Map<String, JSONArray> result = videoCarouselService.getMonitor(id, type, isChecked);
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("获取车辆树信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取车辆树信息异常");
        }
    }

    /**
     * 少于5000树展开，并有模糊查询
     * @param queryType  查询类型 monitor,device,simCard,group,assignment,people(从业人员)
     * @param queryParam
     * @return
     */
    @RequestMapping(value = "/tree", method = RequestMethod.POST)
    @ResponseBody
    public String getTree(String queryType, String queryParam, Boolean isChecked) {
        try {
            String result = videoCarouselService.getTree(queryType, queryParam, isChecked).toJSONString();
            result = ZipUtil.compress(result);
            return result;
        } catch (Exception e) {
            log.error("获取车辆树信息异常", e);
            return null;
        }
    }

    /**
     * 用户的视频设置
     * @return
     */
    @RequestMapping(value = "/getVideoSetting", method = RequestMethod.POST)
    @ResponseBody
    public String getVideoSetting() {
        return videoCarouselService.getVideoSetting();
    }

    /**
     * 用户的视频设置
     * @return
     */
    @RequestMapping(value = "/videoSet", method = RequestMethod.POST)
    @ResponseBody
    public boolean videoSet(String setting) {
        return videoCarouselService.videoSet(setting);
    }


    /**
     * 视频轮播截图存储
     * @return
     */
    @RequestMapping(value = "/saveMedia", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveMedia(MultipartFile file, String vehicleId, String channelNum) {
        try {
            boolean result = videoCarouselService.saveMedia(file, vehicleId, channelNum);
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("获取车辆树信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

}

