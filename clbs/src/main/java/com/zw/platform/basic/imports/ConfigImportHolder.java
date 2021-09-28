package com.zw.platform.basic.imports;

import com.zw.platform.basic.domain.GroupMonitorDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.DeviceDTO;
import com.zw.platform.basic.dto.MonitorBaseDTO;
import com.zw.platform.basic.dto.SimCardDTO;
import com.zw.platform.domain.basicinfo.TerminalTypeInfo;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 信息配置导入导入中间临时变量 -- 避免数据在导入的时候重复多次的进行db查询
 * @author zhangjuan
 */
@Data
public class ConfigImportHolder {
    private List<BindDTO> importList;
    /**
     * 已经存在的车辆信息
     */
    private List<MonitorBaseDTO> existVehicleList;

    /**
     * 已经存在的人员信息
     */
    private List<MonitorBaseDTO> existPeopleList;

    /**
     * 已经存在的 物品信息
     */
    private List<MonitorBaseDTO> existThingList;

    /**
     * 用户拥有权限下的组织，名称-uuid的映射关系
     */
    private Map<String, String> orgMap;

    /**
     * 用户拥有权限下的组织，uuid-名称的映射关系
     */
    private Map<String, String> orgIdNameMap;

    /**
     * 终端型号 -- 终端厂商和终端信号与ID的映射关系
     */
    private Map<String, String> terminalTypeMap;

    /**
     * 终端信号列表
     */
    private List<TerminalTypeInfo> terminalTypeInfoList;

    /**
     * 导入车辆数量
     */
    private Integer importVehicleNum;

    /**
     * 导入人员数量
     */
    private Integer importPeopleNum;

    /**
     * 导入车辆
     */
    private Integer importThingNum;

    /**
     * 分组下最大监控对象数量
     */
    private Integer groupMaxMonitorNum;

    /**
     * 分组与监控对象绑定关系
     */
    private List<GroupMonitorDO> newGroupMonitorList;

    /**
     * 对讲导入专用，需要修改的分组与监控对象绑定关系列表
     * 定位对象转换成对讲对象时，若原来绑定的分组中有新导入的群组，需要更新组旋钮位置编号信息
     */
    private List<GroupMonitorDO> updateGroupMonitorList;

    /**
     * 已经存在的sim卡
     */
    private Map<String, SimCardDTO> existSimMap;
    /**
     * 已经存在的终端列表
     */
    private Map<String, DeviceDTO> existDeviceMap;

    /**
     * 对讲专用--个呼号码列表
     */
    private List<String> numbers;

    /**
     * 对讲专用 -- 第三方平台客户代码
     */
    private Long customCode;

}
