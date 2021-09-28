package com.zw.lkyw.service.videoCarousel;

import com.alibaba.fastjson.JSONArray;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author denghuabing on 2019/12/26 10:11
 */
public interface VideoCarouselService {

    /**
     * 权限下超过5000个监控对象树不展开，根据id获取权限下的监控对象树（监控对象属性包含通道号相关字段）
     * @param id
     * @param type group 企业，assignment 分组
     * @return
     */
    Map<String, JSONArray> getMonitor(String id, String type, Boolean isChecked);

    /**
     * 权限不超过5000个监控对象树，展开，监控对象属性包含通道号相关字段
     * @param queryType
     * @param queryParam
     * @return
     */
    JSONArray getTree(String queryType, String queryParam, Boolean isChecked);

    String getVideoSetting();

    boolean videoSet(String setting);

    boolean saveMedia(MultipartFile fileData, String vehicleId, String channelNum);
}
