package com.zw.platform.service.basicinfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.domain.FuelTypeDO;
import com.zw.platform.basic.domain.VehiclePurposeDO;
import com.zw.platform.basic.dto.VehiclePurposeDTO;
import com.zw.platform.domain.basicinfo.BrandInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.VehiclePurpose;
import com.zw.platform.domain.basicinfo.VehicleType;
import com.zw.platform.domain.basicinfo.form.SynchronizeVehicleForm;
import com.zw.platform.domain.basicinfo.form.VehiclePurposeForm;
import com.zw.platform.domain.basicinfo.query.VehiclePurposeQuery;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p> Title: VehicleService.java </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team:
 * ZhongWeiTeam </p>
 * @version 1.0
 * @author: wangying
 * @date 2016年7月21日下午6:56:50
 * @deprecated
 */
@Deprecated
public interface VehicleService {

    /**
     * 获取当前用户除去待分配分组外的所有监控对象的数量
     * @param assignmentId
     * @return
     * @throws Exception
     */
    int countMonitors(String assignmentId) throws Exception;

    /**
     * 获取当前用户监控对象的所有组织和分组
     * @param assignmentId 待分配监控对象的分组ID
     * @param queryParam
     * @param queryType
     * @return
     * @throws Exception
     */
    JSONArray listMonitorTreeParentNodes(String assignmentId, String queryParam, String queryType) throws Exception;

    /**
     * 获取当前用户在指定的分组下的所有监控对象
     * @param assignmentId 指定的分组ID
     * @return
     * @throws Exception
     */
    JSONArray listMonitorsByAssignmentID(String assignmentId) throws Exception;

    /**
          * @param userId      用户名称
     * @param groupIdList 当前组织及下级组织
     * @param configFlag  是否绑定
     * @return List<VehicleInfo>
     * @throws @author wangying
     * @Title: 查询车辆，admin权限车辆树结构显示（查询用户权限的车+用户所属组织及下级组织的车）
     */

    List<VehicleInfo> findVehicleByUserAndGroup(String userId, List<String> groupIdList, boolean configFlag)
            throws Exception;

    /**
     * 根据车辆id查询车辆信息
     */
    VehicleInfo findVehicleById(String id) throws Exception;

    /**
     * 生成通用车辆列表模板
     * @param response
     * @return
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;

    /**
     * 生成工程机械列表模板
     * @param response
     * @return
     */
    boolean generateTemplateEngineering(HttpServletResponse response) throws Exception;

    /**
     * 生成货运车辆列表模板
     * @param response
     * @return
     */
    boolean generateTemplateFreight(HttpServletResponse response) throws Exception;


    /**
          * @param userList
     * @param assignmentId
     * @return boolean
     * @throws @Title: 根据用户list修改用户和车组的关联
     * @author wangying
     */
    boolean updateUserAssignByUser(String assignmentId, String userList, String ipAddress) throws Exception;


    /**
          * @param userId
     * @param vehicleList
     * @return boolean
     * @throws @Title: 根据用户id和车组id删除车组和用户的关联
     * @author wangying
     */
    boolean deleteUserAssByUserAndAssign(String userId, List<String> vehicleList) throws Exception;

    /**
          * @param userList
     * @return boolean
     * @throws @Title: 根据多个userId删除用户和车的关联
     * @author wangying
     */
    boolean deleteUserVehicleByUsers(Collection<String> userList) throws Exception;

    /**
          * @param assignmentId
     * @return List<String>
     * @throws @Title: 根据车组id和user查询车和用户的关联
     * @author wangying
     */
    List<String> findUserAssignByAid(String assignmentId, List<String> urList);

    List<VehicleType> getVehicleTypeList() throws Exception;

    VehicleInfo findByVehicle(String brand);

    boolean updateSynchronizeVehicle(SynchronizeVehicleForm form, String ipAddress) throws Exception;

    /**
          * @param type
     * @param queryObjFlag        查询对象 monitor:监控对象； vehicle:车辆
     * @param webType             1:实时监控界面，2:实时视频界面
     * @param isIncludeQuitPeople
     * @param isCarousel
     * @return JSONArray
     * @throws @Title: 车辆权限树结构(模糊搜索)
     * @author wangying
     * @deprecated 4.3.7  MonitorTreeService.getMonitorTreeFuzzy(monitorTreeQuery)
     */
    JSONArray monitorTreeFuzzy(String type, String queryParam, String queryType, String queryObjFlag, String deviceType,
        Integer webType, String isIncludeQuitPeople, Integer isCarousel) throws Exception;

    /**
          * @param type
     * @param queryObjFlag 查询对象 monitor:监控对象； vehicle:车辆
     * @return JSONArray
     * @throws @Title: 车辆权限树结构(模糊搜索)返回监控对象数量
     * @author hujun
     * @deprecated 4.3.7 新方法：MonitorTreeService.getMonitorTreeFuzzyCount(monitorTreeQuery)
     */
    int monitorTreeFuzzyCount(String type, String queryParam, String queryType, String queryObjFlag, String deviceType)
        throws Exception;

    /**
          * @param type
     * @param isIncludeQuitPeople 是否包含离职 0:不包含; 1:包含;
     * @return JSONArray
     * @throws @Title: 监控对象车辆权限树结构
     * @author lifudong
     */
    JSONObject monitorTreeByType(String type, String isIncludeQuitPeople) throws Exception;

    /**
     * 车辆权限树（除去传入的分组）
     * @param type         树的类型，single,multiple
     * @param assignmentId 当前分配监控对象的分组id
     * @param queryParam
     * @param queryType    查询类型 监控对象 分组 企业
     * @return
     * @throws Exception
     */
    JSONArray vehicleTreeForAssign(String type, String assignmentId, String queryParam, String queryType)
        throws Exception;

    JSONArray vehicleTruckTree(String type, boolean configFlag) throws Exception;

    /**
     * @param id
     * @return 通过机型id查询是否绑定车辆
     * @throws Exception
     */
    int getIsBandVehicleByBrandModelsId(String id) throws Exception;

    /**
     * TODO 查询用户权限的车
     * @param userId
     * @param groupList
     * @return List<VehicleInfo>
     * @throws @Title: findAllSendVehicle
     * @author wangying
     */
    List<VehicleInfo> findAllSendVehicle(String userId, List<String> groupList);


    // 根据车牌查询组织id
    String getGroupID(String brand) throws Exception;

    // 根据车牌查询燃油类型
    String getFuelType(String brand) throws Exception;

    /**
     * 获取所选组织下所有分组的车辆，组装分组车辆树结构（APP端使用）
     * @return
     */
    JSONArray assignVehicleTreeForApp() throws Exception;

    /**
     * 监控对象树结构，包括人和车（APP端使用）
     * @return
     */
    JSONArray monitorTreeFoApp(int limit, int page) throws Exception;

    /**
     * 根据组织id查询该组织下所有分组下的车辆id list
     * @param groupId
     * @return
     */
    List<String> findVehicleByGroupAssign(String groupId);

    /**
     * 根据车辆id 集合 查询车辆信息
     */
    List<VehicleInfo> findVehicleByIds(List<String> id);

    List<String> findParmId(String vid) throws Exception;

    List<Map<String, String>> findParmStatus(String simId);

    /**
     * 新增车辆用途
     */
    boolean addVehiclePurpose(VehiclePurposeForm vehiclePurposes, String ipAddress) throws Exception;

    /**
     * 根据车辆用途名称查询车辆用途实体
     * @param purposeCategory
     * @return
     */

    List<VehiclePurpose> findVehiclePurpose(String purposeCategory) throws Exception;

    /**
     * 查询车辆用途
     * @param query
     * @return
     */

    Page<VehiclePurposeDTO> findVehiclePurposeByPage(VehiclePurposeQuery query) throws Exception;

    /**
     * 查询所有车辆用途
     * @return
     */

    List<VehiclePurpose> findVehicleCategory() throws Exception;

    /**
     * 查询所有车辆用途
     * @return
     */

    List<FuelTypeDO> findFuelType() throws Exception;

    VehiclePurposeDO get(String id);

    /**
     * 修改车辆用途
     * @param form
     * @param ipAddress 客户端的IP地址
     * @return true:成功 false:失败
     */

    JsonResultBean updateVehiclePurpose(final VehiclePurposeForm form, String ipAddress) throws Exception;

    /**
     * 根据id删除车辆用途
     * @param id
     * @return
     */

    boolean deletePurpose(String id, String ipAddress) throws Exception;

    /**
     * 根据id批量删除车辆用途
     * @param ids
     * @return
     */

    boolean deleteVehiclePurposeMuch(List<String> ids, String ipAddress) throws Exception;

    /**
     * 生成车辆用途导入模板
     * @param response
     * @return
     */

    boolean getVehiclePurposeTemplate(HttpServletResponse response) throws Exception;

    /**
     * 导入车辆用途
     * @param file
     * @return
     * @author tangshunyu
     */

    Map importPurpose(MultipartFile file, String ipAddress) throws Exception;

    /**
     * 根据人员编号查询人员监控对象详细信息
     * @param brand 人员编号
     * @return 监控对象信息
     */
    List<Map<String, String>> findPeopleByNumber(String brand);

    /**
     * 根据车牌号查询车辆颜色
     * @param brand
     * @return
     */

    String findColorByBrand(String brand) throws Exception;

    /**
          * @param type
     * @param configFlag
     * @return JSONArray
     * @throws @Title: 车辆权限树结构(绑定温度传感器的车辆)
     * @author wangying
     */

    JSONArray vehicleTempSensorPermissionTree(String type, int sensorType, boolean configFlag) throws Exception;

    /**
     * 根据id查询车牌集合
     * @return
     */

    List<String> findBrandsByIds(Collection<String> ids);

    /**
     * 根据监控对象名称模糊匹配监控对象
     * @param name
     * @return
     */

    List<VehicleInfo> findMonitorByName(String name) throws Exception;

    /**
     * 获取车辆(请注意看assignIds的注释)
     * @return
     */
    Set<String> getRedisAssignVid();

    Set<String> getRedisAssignVid(String username);

    /**
     * 查询所有品牌
     * @return brand list
     */
    List<BrandInfo> findBrand();

    Map<String, String> getVehPurposeMap();

    /**
     * 获取809车辆静态数据(运输行业编码,车辆类型编码,业户id,业户名称,业户电话,行政区划代码)
     * @param vehicleId 车辆id
     * @return 809协议车辆静态数据
     */
    Map<String, String> get809VehicleStaticData(String vehicleId);

    String getTransTypeByPurposeType(String vehiclePurpose);

    // ---------------------------油量里程报表组织树接口---------------------

    /**
     * 获取组织树(显示的车辆，为【油量管理设置】中设置了参数的车辆)
     */
    JSONObject getOilVehicleSettingMonitorTree() throws Exception;

    /**
     * 根据分组id获取分组下的监控对象(为【油量管理设置】中设置了参数的车辆)
     */
    JSONArray getOilVehicleSetMonitorByAssign(String assignmentId, boolean isChecked, String type) throws Exception;

    /**
     * 模糊搜索组织树(显示的车辆，为【油量管理设置】中设置了参数的车辆)
     */
    JSONArray getOilVehicleSetMonitorByFuzzy(String param, String queryPattern) throws Exception;

    /**
     * 根据父节点获取父节点下的车辆数量
     */
    int getSensorVehicleNumberByPid(String parId, String type) throws Exception;

    /**
     * 根据组织id查询车辆（组装成树节点的格式）
     */
    Map<String, JSONArray> getSensorVehicleByGroupId(String groupId, String type, boolean isChecked) throws Exception;

    /**
     * 通过车辆id获取组织id
     * @param vehicleIds
     * @return
     */
    Map<String, VehicleInfo> getVehicleById(Collection<String> vehicleIds);

}
