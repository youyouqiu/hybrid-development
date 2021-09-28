package com.zw.platform.basic.repository;

import com.github.pagehelper.Page;
import com.zw.platform.basic.domain.ProfessionalDO;
import com.zw.platform.basic.domain.ProfessionalShowDTO;
import com.zw.platform.basic.domain.ProfessionalsTypeDO;
import com.zw.platform.basic.dto.ProfessionalDTO;
import com.zw.platform.basic.dto.export.ProfessionalsExportDTO;
import com.zw.platform.domain.basicinfo.MonitorBindProfessionalDo;
import com.zw.platform.domain.basicinfo.form.ProfessionalsTypeForm;
import com.zw.platform.domain.basicinfo.query.IcCardDriverQuery;
import com.zw.platform.domain.basicinfo.query.ProfessionalsTypeQuery;
import com.zw.platform.util.imports.lock.ImportDaoLock;
import com.zw.platform.util.imports.lock.ImportTable;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 从业人员
 * @author Administrator
 */
public interface NewProfessionalsDao {

    /**
     * 根据id查询从业人员信息
     * @param ids 从业人员id集合
     * @return List<ProfessionalDO>
     */
    List<ProfessionalDO> getByIds(@Param("ids") Collection<String> ids);

    /**
     * 根据车辆id查询绑定的从业人员信息
     * 默认查询 从业人员名称、电话 身份证信息
     * @param vehicleId 车辆id
     * @return List<ProfessionalDO>
     */
    List<ProfessionalDO> findByVehicleId(String vehicleId);

    /**
     * 获取ic卡类型的id
     * @return String
     */
    String getIcTypeId();

    /**
     * 根据条件查询从业人员信息
     * @param cardNumber   cardNumber
     * @param name         name
     * @param positionType 岗位类型
     * @return ProfessionalDO
     */
    ProfessionalDO findByCarNumberNameAndPositionType(@Param("cardNumber") String cardNumber,
        @Param("name") String name, @Param("positionType") String positionType);

    /**
     * 查询ic卡上传的从业人员数量
     * @param cardNumber     cardNumber
     * @param icCardAgencies icCardAgencies
     * @param positionType   岗位类型
     * @return Integer
     */
    Integer getIcProfessionalNum(@Param("cardNumber") String cardNumber, @Param("icCardAgencies") String icCardAgencies,
        @Param("positionType") String positionType);

    /**
     * 获取ic异常名称
     * @param positionType 岗位类型
     * @return String
     */
    String getIcErrorName(@Param("positionType") String positionType);

    /**
     * 根据 身份证 和 驾驶证号 查询从业人员信息
     * @param identityOrDrivingLicenseNo 身份证
     * @return ProfessionalDO
     */
    ProfessionalDO findByIdentityOrDrivingLicenseNo(String identityOrDrivingLicenseNo);

    /**
     * 根据名称和岗位类型查询从业人员
     * @param name         name
     * @param positionType 岗位类型
     * @return ProfessionalDO
     */
    ProfessionalDO findByNameAndPositionType(@Param("name") String name, @Param("positionType") String positionType);

    /**
     * 分页查询岗位类型
     * @param query query
     * @return ProfessionalsTypeDO
     */
    Page<ProfessionalsTypeDO> findProfessionalsTypeDO(ProfessionalsTypeQuery query);

    /**
     * 修改岗位类型
     * @param professionalsTypeDO professionalsTypeDO
     * @return boolean
     */
    boolean updateProfessionalsType(ProfessionalsTypeDO professionalsTypeDO);

    /**
     * 根据岗位类型id查询使用该岗位类型的从业人员id
     * @param typeId 岗位类型id
     * @return 从业人员id
     */
    List<String> findProfessionalIdByJobType(String typeId);

    /**
     * 批量删除岗位类型
     * @param ids 岗位类型id
     * @return boolean
     */
    boolean deleteProfessionalsTypeByBatch(@Param("ids") Collection<String> ids);

    /**
     * 根据从业人员id更新岗位类型信息
     * @param ids 从业人员id
     * @return boolean
     */
    @ImportDaoLock(ImportTable.ZW_M_PROFESSIONALS_INFO)
    boolean updateProfessionPositionType(@Param("ids") Collection<String> ids);

    /**
     * 查询岗位类型信息
     * @param type type
     * @return ProfessionalsTypeDO
     */
    ProfessionalsTypeDO findProfessionalsTypeByType(final String type);

    /**
     * 新增岗位类型
     * @param professionalsType  professionalsType
     * @return boolean
     */
    boolean addProfessionalsType(ProfessionalsTypeDO professionalsType);

    /**
     * 批量新增岗位类型
     * @param professionalsTypes professionalsTypes
     * @return boolean
     */
    boolean addProfessionalsTypeByBatch(
        @Param("professionalsTypes") Collection<ProfessionalsTypeDO> professionalsTypes);

    /**
     * 查询所有从业人员，无权限控制
     */
    List<ProfessionalDTO> findAllProfessionals();

    /**
     * 新增从业人员
     * @param professionalDO
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_PROFESSIONALS_INFO)
    boolean addProfessionals(ProfessionalDO professionalDO);

    /**
     * 通过id获取类型
     * @param id
     * @return
     */
    ProfessionalsTypeDO getProfessionalsType(final String id);

    ProfessionalDTO getProfessionalById(@Param("id") String id);

    /**
     * 根据id查询从业人员
     * @param ids
     * @return
     */
    List<ProfessionalDTO> findProfessionalsByIds(@Param("ids") Collection<String> ids);

    /**
     * 删除从业人员
     * @param id
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_PROFESSIONALS_INFO)
    boolean deleteProfessionalsById(String id);

    /**
     * 批量删除从业人员
     * @param ids
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_PROFESSIONALS_INFO)
    boolean deleteProfessionalsByBatch(List<String> ids);

    /**
     * 查询从业人员是否已经绑定
     * @param id
     * @return 是否绑定组织
     * @author Fan Lu
     */
    List<String> getBindVehicleIds(String id);

    Set<String> getBindIds(@Param("ids") Collection<String> ids);

    /**
     * 查询从业人员是否已经绑定
     * @param id
     * @return 是否绑定组织
     * @author Fan Lu
     */
    @ImportDaoLock(ImportTable.ZW_M_PROFESSIONALS_INFO)
    void deleteBindInfos(String id);

    /**
     * 根据从业人员查询绑定的终端ids
     * @param pid 从业人员id
     * @return 终端id集合
     */
    List<Map<String, String>> getDeviceIdByPid(String pid);

    /**
     * 修改从业人员
     * @param professionalDO
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_PROFESSIONALS_INFO)
    boolean updateProfessionals(ProfessionalDO professionalDO);

    /**
     * 根据从业人员id查找绑定的监控对象id
     * @param id
     * @return
     */
    Set<String> findBandMonitorIdByProfessionalId(@Param("id") String id);

    /**
     * 获得岗位类型信息
     * @param types 岗位类型集合
     * @return List<ProfessionalsTypeForm>
     */
    List<ProfessionalsTypeForm> getProfessionalsTypes(@Param("types") Collection<String> types);

    /**
     * 根据名称查询从业人员
     * @param names 从业人员姓名集合
     * @return List<ProfessionalsInfo>
     */
    List<ProfessionalDO> getProfessionalsByNames(@Param("names") Collection<String> names);

    /**
     * 获得数据库已存在的身份证号集合
     * @param identityColl 身份证号集合
     * @return Set<String>
     */
    Set<String> getAlreadyExistIdentitySet(@Param("list") Collection<String> identityColl);

    /**
     * 批量新增从业人员
     * @param professionalDOS
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_PROFESSIONALS_INFO)
    boolean addProfessionalsByBatch(@Param("list") Collection<ProfessionalDO> professionalDOS);

    /**
     * @param groupList
     * @return
     */
    List<ProfessionalsExportDTO> getExportProfessionalData(@Param("groupList") List<String> groupList);

    /**
     * 根据企业id查询从业人员
     * @param orgId
     * @return
     */
    List<ProfessionalDO> getProfessionalsByOrgId(@Param("orgId") String orgId);

    /**
     * 查询岗位类型导出
     * @return
     */
    List<ProfessionalsTypeDO> findAllProfessionalsType();

    ProfessionalDO findByNameExistIdentity(@Param("name") String name, @Param("identity") String identity);

    /**
     * 根据身份证查询从业人员
     * @param identity 身份证
     * @return list
     */
    List<ProfessionalDO> getProfessionalsByIdentity(@Param("identity") String identity);

    /**
     * 根据名称查询从业人员
     * @param name 从业人员姓名
     * @return list
     */
    List<ProfessionalDO> getProfessionalsByName(@Param("name") String name);

    /**
     * 查询ic卡从业人员组织树
     * @param ids
     * @param name
     * @return
     */
    List<ProfessionalDO> findAllIcCarDriver(@Param("ids") Set<String> ids, @Param("name") String name);

    /**
     * 查询从业人员组织树
     * @param ids
     * @param name
     * @return
     */
    List<ProfessionalDO> findAllDriver(@Param("ids") Set<String> ids, @Param("name") String name);

    /**
     * 通过从业资格证号获取插入IC卡的司机
     * @param identity 身份证号
     * @param name     名称
     * @return String
     */
    String getIcCardDriverIdByIdentityAndName(@Param("identity") String identity, @Param("name") String name);

    /**
     * 根据才有资格证号和创建时间获取从业人员
     * @param cardNumber
     * @param createTime
     * @return
     */
    String getProByCardNumberAndCreateTime(@Param("cardNumber") String cardNumber,
        @Param("createTime") Date createTime);

    /**
     * 修改从业人员身份证照片
     * @param professionalDTO
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_PROFESSIONALS_INFO)
    boolean updateProfessionalsOCRIdentity(ProfessionalDTO professionalDTO);

    /**
     * 修改从业人员驾驶证信息
     * @param professionalDTO
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_PROFESSIONALS_INFO)
    boolean updateProfessionalsOCRDriver(ProfessionalDTO professionalDTO);

    /**
     * 修改才从业资格证信息
     * @param professionalDTO
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_PROFESSIONALS_INFO)
    boolean updateProfessionalsOCRQualification(ProfessionalDTO professionalDTO);

    /**
     * 根据从业人员的从业资格证号和名称查询从业人员司机评分模块相关信息
     * @param icCardDriverQueryList
     * @return
     */
    List<ProfessionalShowDTO> getIcCardDriverInfos(
        @Param("icCardDriverQueryList") Collection<IcCardDriverQuery> icCardDriverQueryList);

    /**
     * 根据从业人员的从业人员id查询从业人员司机评分模块相关信息
     * @param ids 从业人员id集合
     * @return
     */
    List<ProfessionalShowDTO> getIcCardDriverInfoByIds(@Param("ids") Collection<String> ids);

    /**
     * 获取从业人员绑定的监控对象ID
     * @param keyword 从业人员名称关键字
     * @return 监控对象ID集合
     */
    Set<String> getBindMonitorIdsByKeyword(@Param("keyword") String keyword);

    /**
     * 根据卡号和名称获取图片路径
     * @param identity
     * @param name
     * @return
     */
    String getPhotoByCardNumberAndNameAndVersion(@Param("identity") String identity, @Param("name") String name);

    /**
     * 通过名称和卡号进行查询
     * @param driverName
     * @param cardNumber
     * @return
     */
    List<ProfessionalDTO> getProfessionalsByNameAndCardNum(@Param("name") String driverName,
        @Param("cardNumber") String cardNumber);

    /**
     * 更新从业人员的图片路径
     * @param mediaName
     * @param id
     */
    void updateIcCardPhotoGraph(@Param("photograph") String mediaName, @Param("id") String id);

    /**
     * 修改从业人员的人脸id
     * @param faceId
     * @param id
     * @return
     */
    boolean updateFaceId(@Param("faceId") String faceId, @Param("id") String id);

    /**
     * 通过名称模糊搜索从业人员
     * 只返回了id name identity 三个字段
     * @param fuzzyName fuzzyName
     * @return List<ProfessionalDO>
     */
    List<ProfessionalDO> fuzzySearchByName(String fuzzyName);

    /**
     * 获得监控对象绑定的从业人员信息
     * @param moIds 监控对象id
     * @return List<MonitorBindProfessionalDo>
     */
    List<MonitorBindProfessionalDo> getMonitorBindProfessionalList(@Param("moIds") Collection<String> moIds);
}
