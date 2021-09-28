package com.zw.platform.service.infoconfig;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.domain.ConfigDO;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.PeopleInfo;
import com.zw.platform.domain.basicinfo.PersonnelInfo;
import com.zw.platform.domain.basicinfo.ProfessionalsInfo;
import com.zw.platform.domain.basicinfo.SimcardInfo;
import com.zw.platform.domain.basicinfo.ThingInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.infoconfig.ConfigList;
import com.zw.platform.domain.infoconfig.form.Config1Form;
import com.zw.platform.domain.infoconfig.form.ConfigForm;
import com.zw.platform.domain.infoconfig.form.GroupForConfigForm;
import com.zw.platform.domain.infoconfig.form.MonitorInfo;
import com.zw.platform.domain.infoconfig.form.ProfessionalForConfigFrom;
import com.zw.platform.domain.infoconfig.query.ConfigDetailsQuery;
import com.zw.platform.domain.infoconfig.query.ConfigQuery;
import com.zw.platform.domain.netty.BindInfo;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 信息配置Service接口 <p>Title: ConfigService.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p> <p>team:
 * ZhongWeiTeam</p>
 * @author Liubangquan
 * @version 1.0
 * @since 2016年7月26日上午10:59:38
 * @deprecated 4.3.7
 */
@Deprecated
public interface ConfigService extends IpAddressService {

    ConfigDO get(String id) throws Exception;

    /**
     * 根据config的id获取config_professional数据
     * @author Liubangquan
     */
    List<ProfessionalForConfigFrom> getProfessionalForConfigListByConfigId(String id) throws BusinessException;


    /**
     * 获取人员信息列表
     * @return List<VehicleInfo>
     * @author wangying
     */
    List<VehicleInfo> getPeopleInfoList(String id) throws Exception;


    /**
     * 获取物品信息列表
     * @author Liubangquan
     */
    List<ThingInfo> getThingInfoList() throws Exception;

    /**
     * 获取外设信息列表（车）
     * @author Liubangquan
     */
    List<DeviceInfo> getDeviceInfoList(String id) throws Exception;

    /**
     * 获取外设信息列表(人员)
     * @author Liubangquan
     */
    List<DeviceInfo> getDeviceInfoListForPeople(String id) throws Exception;

    /**
     * 获取sim卡信息列表
     * @author Liubangquan
     */
    List<SimcardInfo> getSimcardInfoList(String id) throws Exception;

    /**
     * 获取从业人员信息列表
     * @author Liubangquan
     */
    List<ProfessionalsInfo> getProfessionalsInfoList() throws Exception;

    /**
     * 查询信息配置列表
     * @return List<ConfigList>
     * @author Liubangquan
     */
    Page<ConfigList> findByPage(final ConfigQuery query);

    /**
     * 查询车辆绑定详情
     */
    ConfigDetailsQuery configDetails(final String id) throws Exception;

    /**
     * 查询人员绑定详情
     */
    ConfigDetailsQuery peopleConfigDetails(final String id) throws Exception;

    /**
     * 查询物品绑定
     */
    ConfigDetailsQuery thingConfigDetails(final String id) throws Exception;

    /**
     * 生成导入模板
     * @author Liubangquan
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;

    List<ConfigDetailsQuery> configDetailsall();

    VehicleInfo getVehicleByDevice(final String id);

    VehicleInfo getVehicleByDeviceNew(String id);

    PeopleInfo getPeopleInfoByDevice(String id);

    ThingInfo getThingInfoByDevice(String id);

    PersonnelInfo getPeopleByDevice(final String id);

    /**
     * 判断是否已绑定
     */
    ConfigForm getIsBand(String vid, String did, String cid, String pid) throws Exception;

    List<ConfigForm> getIsBands(String vid, String did, String cid, String pid) throws Exception;

    List<GroupForConfigForm> isBnadP(final String id);

    List<GroupForConfigForm> isBnadG(final String id);

    /**
     * 查询组织下是否有device
     * @return 是否有device
     * @author Fan Lu
     */
    int isBandDevice(String id);

    /**
     * 查询组织下是否有simcard
     * @return 是否有simcard
     * @author Fan Lu
     */
    int isBandSimcard(String id);

    /**
     * 根据当前登录用户返回其组织架构以及有权限的车辆列表
     * @author FanLu
     */
    Map<String, Object> getOrgAndVehicle(String username);

    /**
     * 获取当前登录用户组织，若当前登录用户为admin，则默认其第一个下级组织
     * @author Liubangquan
     */
    String[] getCurOrgId();

    /**
     * 根据信息配置id获取车辆分组信息
     * @author Liubangquan
     */
    List<Assignment> getAssignmentByConfigId(String configId);

    /**
     * 根据信息配置id获取人员分组信息
     * @author Liubangquan
     */
    List<Assignment> getPeopleAssignmentByConfigId(String configId);

    /**
     * 根据信息配置id获取物品分组信息
     */
    List<Assignment> getThingAssignmentByConfigId(String configId);

    /**
     * 判断组织是否绑定分组
     * @author wangying
     */
    int isBandAssignment(String id);

    /**
     * 判断当前分组下的车辆是否超过100辆
     * @return
     * @author Liubangquan
     */
    JsonResultBean isVehicleCountExceedMaxNumber(String assignmentId, String assignmentName) throws Exception;

    /**
     * 根据车辆id获取车辆终端绑定表id
     * @author Liubangquan
     */
    String getConfigIdByVehicleId(String vehicleId) throws Exception;

    /**
     * 根据绑定id集合查询绑定表
     */
    List<ConfigDetailsQuery> getConfigByConfigIds(List<String> ids) throws Exception;

    /**
     * 根据关联id查询监控对象类型
     */
    String findMonitorTypeById(String configId);

    /**
     * 根据车辆id查找设备id
     * @deprecated 4.3.7
     */
    Set<String> getDeviceIdsByVIds(Set<String> vid);


    /**
     * 获得所有分组id及其分组下监控对象的数量
     */
    JSONObject getAllAssignmentVehicleNumber(String id, int type);

    /**
     * 维护极速录入列表
     * @author fanlu
     */
    void addBindInfo(String deviceId, Config1Form config1Form);

    /**
     * 根据车辆id获取当前车辆绑定个外设列表
     */

    String getPeripherals(String vehicleId);

    /**
     * 处理组装监控对象信息
     */
    MonitorInfo handleMonitorInfo(Object data, int monitorType);

    /**
     * 监控对象信息变更,重新组装监控对象信息并下发绑定到F3
     */
    void sendBindToF3(String vehicleId);

    /**
     * 根据终端id查询入网状态
     * @param deviceId
     * @return
     */

    void assembleNetWork(List<BindInfo> bindInfos);
}
