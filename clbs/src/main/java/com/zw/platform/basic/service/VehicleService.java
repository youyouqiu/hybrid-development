package com.zw.platform.basic.service;

import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.domain.BaseKvDo;
import com.zw.platform.basic.domain.CargoGroupVehicleDO;
import com.zw.platform.basic.domain.VehicleDO;
import com.zw.platform.basic.dto.MonitorBaseDTO;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.dto.query.VehiclePageQuery;
import com.zw.platform.domain.basicinfo.query.VehicleQuery;
import com.zw.platform.domain.infoconfig.form.MonitorInfo;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 车辆模块管理接口
 * @author zhangjuan
 * @date 2020/9/25
 */
public interface VehicleService extends MonitorBaseService<VehicleDTO> {

    /**
     * 分页查询
     * @param query 分页查询条件
     * @return 分页查询结果
     */
    Page<VehicleDTO> getByPage(VehicleQuery query);

    /**
     * 分页获取车辆信息
     * @param query 分页基本查询条件
     * @return 监控对象列表
     */
    Page<VehicleDTO> getListByKeyWord(VehiclePageQuery query);

    /**
     * 根据组织分页查询监控对象
     * @param orgId 组织ID (uuid)
     * @param query 基本查询条件
     * @return 监控对象列表
     */
    Page<VehicleDTO> getListByOrg(String orgId, VehiclePageQuery query);

    /**
     * 根据分组分页查询监控对象
     * @param groupIds 分组ID集合
     * @param query    分页查询
     * @return 监控对象列表
     */
    Page<VehicleDTO> getListByGroup(Collection<String> groupIds, VehiclePageQuery query);

    /**
     * 车辆信息导出
     * @param response 响应
     * @return 是否导出成功
     * @throws Exception Exception
     */
    boolean export(HttpServletResponse response) throws Exception;

    /**
     * 批量修改车辆
     * @param ids        车辆ID
     * @param vehicleDTO 车辆修改的内容--非空未本次修改字段，为空字段保留原有内容
     * @return 是否修改成功
     */
    boolean batchUpdate(Collection<String> ids, VehicleDTO vehicleDTO);

    /**
     * 生成通用模板
     * @param response response
     * @return 是否生成成功
     * @throws Exception Exception
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;

    /**
     * 生成工程机械模板
     * @param response response
     * @return 是否生成成功
     * @throws Exception Exception
     */
    boolean generateTemplateEngineering(HttpServletResponse response) throws Exception;

    /**
     * 生成货运模板
     * @param response response
     * @return 是否生成成功
     * @throws Exception Exception
     */
    boolean generateTemplateFreight(HttpServletResponse response) throws Exception;

    /**
     * 车辆导入
     * @param file 文件模板
     * @return 导入结果
     * @throws Exception Exception
     */
    JsonResultBean importExcel(MultipartFile file) throws Exception;

    /**
     * 车辆信息批量入库
     * @param vehicleList 车辆信息
     * @return 操作结果
     */
    boolean addBatch(List<VehicleDO> vehicleList);

    /**
     * 批量删除
     * @param ids ids
     * @return 删除条数
     */
    Map<String, String> batchDel(Collection<String> ids);

    /**
     * 根据车辆类别获取用户权限下的车辆
     * @param categoryName 类别
     * @return 车辆基本信息
     */
    List<MonitorBaseDTO> getByCategoryName(String categoryName);

    /**
     * 设置保养
     * @param vehicleType 车辆类型
     * @param id          车辆ID
     * @param execute     是否修改保养里程数
     * @return JsonResultBean
     */
    JsonResultBean saveMaintained(String vehicleType, String id, boolean execute);

    /**
     * 修改车辆的行政区划相关信息
     * @param id         车辆ID
     * @param provinceId 所属省份ID
     * @param cityId     所属城市ID
     */
    void updateDivision(String id, String provinceId, String cityId);

    /**
     * 批量处理车辆的行政区划
     * @param vehicleList 车辆列表
     */
    void setAdministrativeDivision(List<VehicleDTO> vehicleList);

    /**
     * 获取用户拥有权限且为已绑定的营运车辆数量和维修车辆的数量
     * @return 营运车辆数量和维修车辆的数量
     */
    Map<String, Integer> getOperatingAndRepairNum();

    /**
     * 根据用户信息获取权限下车辆的ID 包含绑定和未绑定的
     * @param userDTO userDTO
     * @param keyword 监控对象名称关键字，为空未所有
     * @return 车辆ID集合
     */
    List<String> getUserOwnIdsByUser(String keyword, UserDTO userDTO);

    /**
     * 进行标记，用于区分人车物
     * @return 返回车辆类型
     */
    default MonitorTypeEnum getMonitorEnum() {
        return MonitorTypeEnum.VEHICLE;
    }

    /**
     * 通过车牌获取Id
     * @param vehicleNo
     * @return
     */
    String getIdByBrand(String vehicleNo);

    /**
     * 获取车辆ID通过车牌和颜色
     * @param vehicleNo
     * @param vehicleColor
     * @return
     */
    String getIdByBrandAndColor(String vehicleNo, Integer vehicleColor);

    /**
     * 通过终端编号查询
     * @param deviceNumber
     * @return
     */
    VehicleDTO getVehicleDTOByDeviceNumber(String deviceNumber);

    /**
     * 通过车牌获取绑定车辆的信息
     * @param brand
     * @return
     */
    VehicleDTO getBindVehicleDTOByBrand(String brand);

    /**
     * 获取车辆的所属企业
     * @param vehicleNo
     * @return
     */
    String getOrgIdByBrand(String vehicleNo);

    /**
     * 获取企业下的车辆Id
     * @param orgId
     * @return
     */
    Set<String> getVehicleIdsByOrgId(String orgId);

    /**
     * 通过终端id获取车辆信息
     * @param deviceId
     * @return
     */
    VehicleDTO getVehicleInfoByDeviceId(String deviceId);

    /**
     * 获取部分字段
     * @param vehicleId
     * @return
     */
    VehicleDTO getPartFieldById(String vehicleId);

    /**
     * 设置行政区划
     * @param vehicle
     */
    void setAdministrativeDivision(VehicleDTO vehicle);

    /**
     * 通过车辆id批量获取
     * @param vehicleIds
     * @return
     */
    List<VehicleDO> getVehicleListByIds(Collection<String> vehicleIds);

    /**
     * 得到行驶证已经过期的车辆
     * @return
     * @throws Exception
     */
    List<String> getVehicleIdsByAlreadyExpireLicense();

    /**
     * 得到行驶证达到提前提醒天数条件的车辆
     * @return
     * @throws Exception
     */
    List<String> getVehicleIdsByWillExpireLicense();

    /**
     * 得到道路运输证已经过期的车辆
     * @return
     * @throws Exception
     */
    List<String> getVehicleIdsByAlreadyExpireRoadTransport() throws Exception;

    /**
     * 得到道路运输证达到提前提醒天数条件的车辆
     * @return
     * @throws Exception
     */
    List<String> getVehicleIdsByWillExpireRoadTransport() throws Exception;

    /**
     * 得到保养有效期到期的车辆
     * @return
     * @throws Exception
     */
    List<String> getVehicleIdsByMaintenanceExpired() throws Exception;

    /**
     * 得到保养里程数不为null的车辆
     * @return
     * @throws Exception
     */
    Map<String, BaseKvDo<String, Integer>> getVehicleIdsByMaintenanceMileageIsNotNull() throws Exception;

    MonitorInfo getF3Data(VehicleDTO vehicleDTO);

    /**
     * 清除平台从未上过的线的车的垃圾数据
     * @throws Exception
     */
    void deleteNeverOnlineVehicle();

    boolean addCargoGroupVehicle(List<CargoGroupVehicleDO> cargoGroupVehicleDOS);
}
