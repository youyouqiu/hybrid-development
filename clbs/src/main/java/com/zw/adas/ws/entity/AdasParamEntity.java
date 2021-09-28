package com.zw.adas.ws.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

/***
 @Author gfw
 @Date 2019/6/6 16:24
 @Description 川冀表参数定义 列表实体
 @version 1.0
 **/
@Data
public class AdasParamEntity {
    /**
     * 监控对象 车牌号
     */
    private String brand;
    /**
     * 车辆id
     */
    private String vehicleId;
    /**
     * 是否绑定 0:否 1:绑定
     */
    private Integer bindFlag;
    /**
     * 协议类型
     */
    private Integer protocolType;

    /**
     * 分组id
     */
    private String groupId;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 状态信息 0:离线 1:在线
     */
    private String status;

    /**
     * 下发状态
     */
    private List<Map<String, Object>> statusMap;

}
