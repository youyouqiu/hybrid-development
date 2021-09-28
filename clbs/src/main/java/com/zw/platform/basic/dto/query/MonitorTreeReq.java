package com.zw.platform.basic.dto.query;

import com.zw.platform.domain.enmu.ProtocolEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 分组单节点查询
 * @author zhangjuan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitorTreeReq {
    public static final Integer REAL_TIME_MONITORING = 1;
    public static final Integer REAL_TIME_VIDEO = 2;
    public static final Integer LKYW = 10;
    private String type;

    /**
     * 对树节点是否进行勾选，与返回节点属性checked的值对应
     */
    private boolean isChecked;

    /**
     * 页面类型 1：实时监控 2：实时视频 10 两客一危
     */
    private Integer webType;

    /**
     * 是否需要ACC状态-在线才返回
     */
    private boolean needAccStatus;

    /**
     * 是否需要 是否是轮播页面的查询
     */
    private boolean needCarousel;

    /**
     * 监控对象的协议类型
     */
    private Collection<String> deviceTypes;

    /**
     * 监控模糊搜索查询类型
     * name/空:按监控对象名称，
     * simcardNumber/simCardNumber：按终端手机号
     * deviceNumber:按终端号
     * professional：从业人员
     * group：按分组名查询
     * org 按组织名称查询
     */
    private String queryType;

    /**
     * 模糊搜索关键字
     */
    private String keyword;

    /**
     * 监控对象类型 monitor：allMonitor vehicle：车
     */
    private String monitorType;

    /**
     * 根据监控对象名称搜索
     */
    private String vehicleTypeName;

    /**
     * 监控对象在离线状态 0:未上线 1、在线，2在线停车，3在线行驶，4报警，5超速报警,6未定位,7未上线,8离线,9心跳
     */
    private Integer status;

    /**
     * 是否需要包含离职人员
     */
    private boolean needQuitPeople;

    /**
     * 分组是否需要返回监控对象在离线状态
     */
    private boolean needOnlineStatus;

    /**
     * 分组是否需要返回监控对象数量
     */
    private boolean needMonitorCount;

    /**
     * 分组是否需要返回在线监控对象数量
     */
    private boolean needOnlineMonitorCount;

    /**
     * 监控对象限制数量
     */
    private Integer limitMonitorNum;

    private Set<String> monitorIds;

    public String getQueryType() {
        if (Objects.equals("group", queryType)) {
            return "org";
        }
        if (Objects.equals("assignName", queryType)) {
            return "group";
        }
        if (Objects.equals("assignment", queryType)) {
            return "group";
        }
        if (Objects.equals("monitor", queryType)) {
            return "name";
        }
        return queryType;
    }

    public List<String> getDeviceTypes(String monitorType, String deviceType, Integer webType, boolean isNew) {
        List<String> deviceTypes = null;
        if (StringUtils.isBlank(deviceType)) {
            return null;
        }
        //实时监控两客一危  群发消息获取监控对象
        if (Objects.equals(webType, 10)) {
            if ("11".equals(deviceType)) {
                if (isNew) {
                    deviceTypes = new ArrayList<>();
                    deviceTypes.add(deviceType);
                } else {
                    deviceTypes = Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2019_STR);
                }
            } else {
                deviceTypes = Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2011_2013_STR);
            }
        }
        if (!"vehicle".equals(monitorType) || StringUtils.isBlank(deviceType)) {
            return deviceTypes;
        }

        if ("1".equals(deviceType)) {
            deviceTypes = Arrays.asList(ProtocolEnum.REALTIME_VIDEO_DEVICE_TYPE);
        } else {
            deviceTypes = new ArrayList<>();
            deviceTypes.add(deviceType);
        }
        return deviceTypes;
    }

    /**
     * /treeStateInfos接口使用
     * @param status     监控对象状态
     * @param deviceType deviceType
     * @return 协议类型集合
     */
    public List<String> getDeviceTypes(Integer status, String deviceType) {
        if (StringUtils.isBlank(deviceType)) {
            return null;
        }
        List<String> deviceTypes = null;

        if (Objects.equals(status, 1)) {
            deviceTypes = new ArrayList<>();
            Set<String> protocol2011And2019 =
                new HashSet<>(Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2019_STR));
            if (protocol2011And2019.contains(deviceType)) {
                deviceTypes.add(deviceType);
            } else {
                deviceTypes.addAll(Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2011_2013_STR));
            }
        } else if (Objects.equals(status, 10)) {
            deviceTypes = new ArrayList<>();
            if (Objects.equals(deviceType, "11")) {
                deviceTypes.add("11");
            } else {
                deviceTypes.addAll(Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2011_2013_STR));
                deviceTypes.addAll(Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2019_STR));
                deviceTypes.remove("11");
            }
        }
        return deviceTypes;

    }
}
