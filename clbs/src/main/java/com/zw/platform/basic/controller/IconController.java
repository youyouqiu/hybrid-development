package com.zw.platform.basic.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.core.ConfigHelper;
import com.zw.platform.basic.dto.IconDTO;
import com.zw.platform.basic.dto.query.MonitorTreeReq;
import com.zw.platform.basic.service.MonitorIconService;
import com.zw.platform.basic.service.MonitorService;
import com.zw.platform.basic.service.MonitorTreeService;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.query.MonitorTreeQuery;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.ZipUtil;
import com.zw.talkback.common.ControllerTemplate;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 对象图标个性化设置
 * @author yangyi
 */
@Log4j
@Controller
@RequestMapping("/m/personalized/ico")
public class IconController {
    private static final Logger logger = LogManager.getLogger(IconController.class);
    private static final String LIST_PAGE = "modules/personalized/ico/list";

    @Autowired
    private MonitorIconService monitorIconService;

    @Autowired
    private ConfigHelper configHelper;

    @Autowired
    private MonitorTreeService monitorTreeService;

    @Autowired
    private MonitorService monitorService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = { "/findIco" }, method = RequestMethod.POST)
    @ResponseBody
    public List<IconDTO> findIco() {
        return monitorIconService.getIconList();
    }

    @RequestMapping(value = { "/delIco" }, method = RequestMethod.POST)
    @ResponseBody
    public boolean delIco(String id, HttpServletRequest request) {
        return monitorIconService.deleteIcon(id, request);
    }

    /**
     * 图标上传
     * @param request request
     * @param file    file
     * @return 删除结果
     */
    @RequestMapping(value = { "/upload_img" }, method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> uploadImg(HttpServletRequest request, MultipartFile file) {
        try {
            return monitorIconService.uploadImg(request, file);
        } catch (Exception e) {
            log.error("图标上传失败", e);
            return ImmutableMap.of("state", "-1", "imgName", "");
        }

    }

    /**
     * 监控对象图标数
     * @param type       分组和组织 根节点是否可选 single：nocheck=true multiple 不组装 nocheck
     * @param icoType    监控对象类型 0:车 1：人 2：物
     * @param deviceType 为空：全部协议类型 1：实时视频终端协议类型 其他：对应的类型
     * @return 监控对象数量和树
     */
    @RequestMapping(value = "/vehicleTree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getVehicleTree(String type, String icoType, String deviceType) {
        try {
            List<String> deviceTypes = null;
            if (Objects.equals("1", deviceType)) {
                deviceTypes = Arrays.asList(ProtocolEnum.REALTIME_VIDEO_DEVICE_TYPE);
            } else if (StringUtils.isNotBlank(deviceType) && !Objects.equals("1", deviceType)) {
                deviceTypes = Collections.singletonList(deviceType);
            }
            MonitorTreeReq monitorTreeReq = new MonitorTreeReq();
            monitorTreeReq.setDeviceTypes(deviceTypes);
            monitorTreeReq.setType(type);
            MonitorTypeEnum monitorTypeEnum = MonitorTypeEnum.getByType(icoType);
            monitorTreeReq.setMonitorType(Objects.isNull(monitorTypeEnum) ? null : monitorTypeEnum.getEnName());
            monitorTreeReq.setNeedMonitorCount(true);
            JSONObject result = monitorTreeService.getMonitorTree(monitorTreeReq, false);
            return new JsonResultBean(ZipUtil.compress(result.toJSONString()));
        } catch (Exception e) {
            log.error("获取个性化图标树异常", e);
            return null;
        }
    }

    /**
     * 模糊搜索，重组树结构
     * @param type
     * @param queryParam 查询条件
     * @param deviceType 查询类型
     * @return
     */
    @RequestMapping(value = "/vehicleTreeFuzzy", method = RequestMethod.POST)
    @ResponseBody
    public String getVehicleTreeFuzzy(String type, String queryParam, String deviceType, Integer webType,
        String queryType) {
        MonitorTreeReq query = new MonitorTreeReq();
        if (Objects.equals(queryType, "group")) {
            queryType = "org";
        } else if (Objects.equals(queryType, "assignName")) {
            queryType = "group";
        } else {
            queryType = "name";
        }
        query.setQueryType(queryType);
        query.setType(type);
        query.setKeyword(queryParam);
        query.setWebType(webType);
        List<String> deviceTypes = null;
        if (Objects.equals("1", deviceType)) {
            deviceTypes = Arrays.asList(ProtocolEnum.REALTIME_VIDEO_DEVICE_TYPE);
        } else if (StringUtils.isNotBlank(deviceType) && !Objects.equals("1", deviceType)) {
            deviceTypes = Collections.singletonList(deviceType);
        }
        query.setDeviceTypes(deviceTypes);
        query.setNeedCarousel(false);
        query.setMonitorType("vehicle");
        query.setChecked(false);
        query.setNeedAccStatus(false);
        query.setNeedQuitPeople(false);
        String result = monitorTreeService.getMonitorTreeFuzzy(query).toJSONString();

        // 压缩数据
        try {
            result = ZipUtil.compress(result);
        } catch (Exception e) {
            logger.error("获取车辆树信息异常", e);
            return null;
        }
        return result;
    }

    /**
     * 获取监控对象Id的数量
     * @param parentId   分组ID活儿监控对象ID
     * @param type       group：按企业查询 assignment：按分组查询
     * @param deviceType 没有用到
     * @param webType    3： 4：
     * @return 监控对象数量
     */
    @RequestMapping(value = "/getCheckedVehicle", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getCheckedVehicle(String parentId, String type, String deviceType, Integer webType) {
        // 原来的逻辑里，deviceType字段没有用上,这里type做一下转义
        type = Objects.equals("assignment", type) ? "group" : "org";
        Set<String> monitorIds = monitorTreeService.getMonitorIdSet(parentId, type, webType);
        return new JsonResultBean(monitorIds.size());
    }

    /**
     * 获取车辆的数量
     * @param parentId 分组ID活儿监控对象ID
     * @param type     按企业查询 assignment：按分组查询
     * @return 车辆的数量
     */
    @RequestMapping(value = "/getVehicleCount", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getCheckedVehicle(String parentId, String type) {
        type = Objects.equals("assignment", type) ? "group" : "org";
        Set<String> monitorIds = monitorService.getMonitorByGroupOrOrgDn(parentId, type, null);
        monitorService.filterByMonitorType(MonitorTypeEnum.VEHICLE.getType(), monitorIds);
        return new JsonResultBean(monitorIds.size());
    }

    /**
     * 批量修改监控对象图标
     * @param vehicleIcon 图标Id
     * @param listStr     修改的监控对象列表json数组字符串 id-type(监控对象类型)数组
     * @return 是否更新成功
     */
    @RequestMapping(value = { "/updateObjectIcon" }, method = RequestMethod.POST)
    @ResponseBody
    public boolean updateVehicleIco(String vehicleIcon, String listStr) {
        if (StringUtils.isBlank(listStr)) {
            return false;
        }
        List<Map<String, String>> monitorList = JSON.parseObject(listStr, ArrayList.class);
        return monitorIconService.update(vehicleIcon, monitorList);
    }

    /**
     * 默认修改监控对象图标
     * @param listStr 恢复默认图标的监控对象列表json数组字符串 id-type(监控对象类型)数组
     * @return 是否更新成功
     */
    @RequestMapping(value = { "/deflutObjectIcon" }, method = RequestMethod.POST)
    @ResponseBody
    public boolean defaultVehicleIco(String listStr) {
        if (StringUtils.isBlank(listStr)) {
            return false;
        }
        List<Map<String, String>> monitorList = JSON.parseObject(listStr, ArrayList.class);
        return monitorIconService.delete(monitorList);
    }

    @RequestMapping(value = { "/getIcodirection" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getIcoDirection() {
        return new JsonResultBean(configHelper.getIcoDirection());
    }

    @RequestMapping(value = { "/updateicoDirection" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateIconDirection(String flag) {
        return ControllerTemplate.getBooleanResult(() -> monitorIconService.updateIconDirection(flag));
    }

    @RequestMapping(value = "/monitorTree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMonitorTree(MonitorTreeQuery monitorTreeQuery) {
        try {
            MonitorTreeReq monitorTreeReq = new MonitorTreeReq();
            monitorTreeReq.setQueryType(monitorTreeQuery.getQueryType());
            monitorTreeReq.setKeyword(monitorTreeQuery.getQueryParam());
            if (Objects.equals(monitorTreeQuery.getQueryType(), "vehType")) {
                monitorTreeReq.setQueryType(null);
                monitorTreeReq.setKeyword(null);
                monitorTreeReq.setVehicleTypeName(monitorTreeQuery.getQueryParam());
            }
            if (StringUtils.isNotBlank(monitorTreeQuery.getDevType())) {
                List<String> deviceTypes;
                if (Objects.equals(monitorTreeQuery.getDevType(), String.valueOf(ProtocolEnum.ONE))) {
                    deviceTypes = Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2011_2013_STR);
                } else {
                    deviceTypes = Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2019_STR);
                }
                monitorTreeReq.setDeviceTypes(deviceTypes);
            }
            boolean isReturnAll = Objects.equals(monitorTreeQuery.getQueryType(), "rightClick");
            JSONObject result = monitorTreeService.getMonitorTree(monitorTreeReq, isReturnAll);
            return new JsonResultBean(ZipUtil.compress(result.toJSONString()));
        } catch (Exception e) {
            log.error("获取实时监控两客一危群发消息组织树异常", e);
            return null;
        }
    }

}
