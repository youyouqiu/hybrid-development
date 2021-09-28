package com.zw.platform.basic.repository;

import com.zw.app.entity.appOCR.PersonnelIdentityInfoUploadEntity;
import com.zw.platform.basic.domain.PeopleBasicDO;
import com.zw.platform.basic.domain.PeopleDO;
import com.zw.platform.basic.dto.MonitorBaseDTO;
import com.zw.platform.basic.dto.PeopleDTO;
import com.zw.platform.util.imports.lock.ImportDaoLock;
import com.zw.platform.util.imports.lock.ImportTable;
import com.zw.talkback.domain.basicinfo.LeaveJobPersonnel;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 人员信息管理DAO层
 * @author zhangjuan
 * @date 2020/10/20
 */
public interface PeopleDao {

    /**
     * 获取人员按时间的排序ID
     * @return 顺序ID
     */
    List<String> getSortList();

    /**
     * 获取人员信息初始化缓存的查询列表
     * @param ids 人员ID
     * @return 人员信息集合
     */
    List<PeopleDTO> initCacheList(@Param("ids") List<String> ids);

    /**
     * 获取人员的个性化图标
     * @return 个性化图标
     */
    List<PeopleDTO> getIconList();

    /**
     * 添加
     * @param peopleDO 人员信息
     * @return 是否操作成功
     */
    @ImportDaoLock(ImportTable.ZW_M_PEOPLE_INFO)
    boolean insert(PeopleDO peopleDO);

    /**
     * 更新
     * @param peopleDO 人员信息
     * @return 是否操作成功
     */
    @ImportDaoLock(ImportTable.ZW_M_PEOPLE_INFO)
    boolean update(PeopleDO peopleDO);

    /**
     * 个性化图标更新
     * @param ids    人员ID集合
     * @param iconId 图标ID
     * @return 是否操作成功
     */
    @ImportDaoLock(ImportTable.ZW_M_PEOPLE_INFO)
    boolean updateIcon(@Param("ids") Collection<String> ids, @Param("iconId") String iconId);

    /**
     * 删除
     * @param ids 人员ID集合
     * @return 删除数量
     */
    @ImportDaoLock(ImportTable.ZW_M_PEOPLE_INFO)
    int delete(@Param("ids") Collection<String> ids);

    /**
     * 获取人员详情
     * @param id 人员Id
     * @return 人员详情
     */
    PeopleDTO getDetailById(@Param("id") String id);

    /**
     * 根据人员编号获取人员详情
     * @param number 编号
     * @return 人员详情
     */
    PeopleDTO getDetailByNumber(@Param("number") String number);

    /**
     * 根据终端号获取人员信
     * @param deviceNum 终端号
     * @return 人员信息
     */
    PeopleDTO getByDeviceNum(@Param("deviceNum") String deviceNum);

    /**
     * 批量获取人员详情
     * @param ids 人员id集合
     * @return 人员详情列表
     */
    List<PeopleDTO> getDetailByIds(@Param("ids") Collection<String> ids);

    /**
     * 获取人员信息
     * @param id 人员Id
     * @return 人员基础信息
     */
    PeopleDO getById(@Param("id") String id);

    /**
     * 根据人员编号获取人员信息
     * @param number 人员编号
     * @return 人员基础信息
     */
    PeopleDO getByNumber(@Param("number") String number);

    /**
     * 添加人员一些附加信息如技能和驾照类别信息
     * @param peopleBasicList 人员基础技能信息
     * @return 是否添加成功
     */
    boolean addBaseInfo(@Param("list") List<PeopleBasicDO> peopleBasicList);

    /**
     * 删除人员的技能和驾照类别信息
     * @param peopleIds 人员ID
     * @return 删除条数
     */
    int deleteBaseInfo(@Param("peopleIds") Collection<String> peopleIds);

    /**
     * 获取所有的人员信息
     * @return 人员基本信息列表
     */
    List<PeopleDO> getAllPeople();

    /**
     * 根据人员编号获取人员基本信息
     * @param peopleNumList 人员编号列表 可为空，为空查询全部
     * @return 人员基本信息
     */
    List<MonitorBaseDTO> getByNumbers(@Param("peopleNumList") Collection<String> peopleNumList);

    /**
     * 批量添加人员信息
     * @param peopleList 人员信息列表
     * @return 添加条数
     */
    @ImportDaoLock(ImportTable.ZW_M_PEOPLE_INFO)
    boolean addByBatch(@Param("peopleList") List<PeopleDO> peopleList);

    /**
     * 人员休息部分字段更新
     * @param peopleDO 人员信息
     * @return 是否操作成功
     */
    @ImportDaoLock(ImportTable.ZW_M_PEOPLE_INFO)
    boolean updatePartField(PeopleDO peopleDO);

    /**
     * 批量更新人员信息
     * @param ids    人员ID集合
     * @param people 人员信息
     * @return 是否更新成功
     */
    @ImportDaoLock(ImportTable.ZW_M_PEOPLE_INFO)
    boolean updateByBatch(@Param("ids") Collection<String> ids, @Param("people") PeopleDO people);

    /**
     * 根据身份证号查询人员
     * @param identity
     * @return
     */
    PeopleDTO getPeopleByIdentity(@Param("identity") String identity);

    /**
     * 根据以该编号结尾的人员标号
     * @param number 编号
     * @return 人员标号
     */
    List<String> getScanByNumber(@Param("number") String number);

    @ImportDaoLock(ImportTable.ZW_M_PEOPLE_INFO)
    boolean updatePersonIdentityCardInfo(PersonnelIdentityInfoUploadEntity entity);

    /**
     * 通过分组id获得离职人员
     * @param assignmentIdList assignmentIdList
     * @return List<LeaveJobPersonnel>
     */
    List<LeaveJobPersonnel> getLeaveJobPersonnelList(@Param("list") Collection<String> assignmentIdList);

}
