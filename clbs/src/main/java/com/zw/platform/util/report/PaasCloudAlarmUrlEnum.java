package com.zw.platform.util.report;

import com.zw.platform.commons.UrlConvert;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * pass cloud url 枚举类(现在的url太多了，以后会更多)
 *
 * @author create by zhouzongbo on 2020/6/18.
 */
public enum PaasCloudAlarmUrlEnum implements UrlConvert {
    /**
     * 报表管理 报警报表 报警查询
     * 查询报警信息
     */
    QUERY_ALARM_INFO("/alarm/list", HttpMethod.POST),

    /**
     * 报表管理 报警报表 报警查询
     * 分页查询报警信息
     */
    PAGE_QUERY_ALARM_INFO("/alarm/v1.2/list", HttpMethod.POST),

    /**
     * 查询当天时间范围外的报警
     */
    QUERY_TODAY_OUT_OF_TIME_ALARM("/alarm/sichuan/besides_time/list", HttpMethod.POST),

    /**
     * 平台超速列表
     */
    PLATE_OVER_SPEED_PAGE_LIST("/alarm/platform/speed/list/page", HttpMethod.POST),

    /**
     * 报表管理 报警报表 io报警查询
     * 查询IO报警信息
     */
    QUERY_IO_ALARM_INFO("/alarm/io/list", HttpMethod.POST),
    /**
     * 报表管理 报警报表 809转发报警查询
     * 查询809转发报警
     */
    QUERY809_FORWARD_ALARM_INFO("/alarm/809/list", HttpMethod.POST),
    /**
     * 查询同一时间的报警
     */
    QUERY_THE_SAME_TIME_ALARM_INFO("/alarm/time/list", HttpMethod.POST),
    /**
     * 查询全局报警数量
     */
    QUERY_GLOBAL_ALARM_NUMBER("/alarm/global/num", HttpMethod.GET),
    /**
     * 查询全局报警最早开始时间
     */
    QUERY_GLOBAL_ALARM_EARLIEST_START_TIME("/alarm/global/min_time", HttpMethod.POST),
    /**
     * 查询报警次数(总的和已处理的)统计
     */
    HANDLE_ALARM_BATCH("/alarm/status/batch", HttpMethod.PUT),
    /**
     * 报警处理(同一报警类型之前的报警全部处理)
     */
    HANDLE_ALARM_SINGLE("/alarm/v1.2/status", HttpMethod.PUT),
    /**
     * 报警处理 只处理一条报警记录
     */
    QUERY_ALARM_NUMBER_COUNT("/alarm/count/type", HttpMethod.POST),
    /**
     * 报警处理 处理指定的多条报警记录
     */
    ALARM_HANDLE_BATCH("/alarm/handle/batch", HttpMethod.PUT),
    /**
     * 离线位移日处理接口, 待定
     */
    OFFLINE_DISPLACEMENT_DEAL_URL("/positional/handle/offline_move", HttpMethod.PUT),
    /**
     * 离线位移日批量处理接口
     */
    OFFLINE_DISPLACEMENT_BATCH_DEAL_URL("/positional/handle/offline_move/batch", HttpMethod.PUT),

    /**
     * 企业-车辆与终端运行状态列表
     */
    VEHICLE_DEVICE_STATE_URL("/adas/device/vehicle/status/page", HttpMethod.POST),

    /**
     * 获取单条车辆与终端运行状态信息
     */
    SINGLE_VEHICLE_DEVICE_STATE_URL("/adas/device/vehicle/status/info", HttpMethod.POST),

    /**
     * 设备维修记录分页查询
     */
    DEVICE_REPAIR_PAGE_URL("/adas/device/repair/page", HttpMethod.POST),

    /**
     * 根据主键获取设备维修列表
     */
    DEVICE_REPAIR_LIST_URL("/adas/device/repair/list/pk", HttpMethod.POST),

    /**
     * 确认设备报修
     */
    CONFIRM_DEVICE_REPAIR_URL("/adas/device/repair/confirm", HttpMethod.PUT),

    /**
     * 完成设备维修
     */
    FINISH_DEVICE_REPAIR_URL("/adas/device/repair/finish", HttpMethod.PUT),
    /**
     * 批量确认设备报修
     */
    BATCH_CONFIRM_DEVICE_REPAIR_URL("/adas/device/repair/confirm/batch", HttpMethod.PUT),

    /**
     * 批量完成设备维修
     */
    BATCH_FINISH_DEVICE_REPAIR_URL("/adas/device/repair/finish/batch", HttpMethod.PUT),;


    /**
     * uri
     * 如: /positional/travel/report
     */
    private final String path;
    /**
     * 请求方法
     */
    private final HttpMethod httpMethod;

    PaasCloudAlarmUrlEnum(String path, HttpMethod httpMethod) {
        this.path = path;
        this.httpMethod = httpMethod;
    }

    /**
     * path cloud api pair
     */
    private static final Map<String, String> API_URL = new HashMap<>(values().length);

    /**
     * 聚合address + path
     *
     * @param address address
     */
    public static void assembleUrl(String address) {
        for (PaasCloudAlarmUrlEnum value : values()) {
            API_URL.put(value.name(), address + value.getPath());
        }
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getUrl() {
        return API_URL.get(this.name());
    }

    @Override
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }
}
