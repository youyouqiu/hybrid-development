package com.zw.platform.repository.modules;

import com.zw.app.entity.appOCR.PersonnelIdentityInfoUploadEntity;
import com.zw.platform.domain.basicinfo.Personnel;
import com.zw.platform.domain.basicinfo.form.PersonnelForm;
import com.zw.platform.domain.basicinfo.query.PersonnelQuery;
import com.zw.platform.domain.infoconfig.dto.ConfigMonitorDTO;
import com.zw.platform.util.imports.lock.ImportDaoLock;
import com.zw.platform.util.imports.lock.ImportTable;
import com.zw.talkback.domain.basicinfo.LeaveJobPersonnel;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Tdz on 2016/7/21.
 */
@Deprecated
public interface PersonnelDao {
    /**
     * 通过id得到一个 Personnel
     */
    Personnel get(final String id);

    List<Personnel> getPeople(@Param("peopleList") List<String> peopleList);

    /**
     * 通过人员id查询
     *
     * @param peopleIds 人员id
     * @return List<Personnel>
     */
    List<Personnel> getPeopleInfoByIds(@Param("peopleIds") Collection<String> peopleIds);

    /**
     * 查询
     */
    List<Personnel> find(final PersonnelQuery query);

    /**
     * 查询所有监控对象人（游离的+权限内分组下的人）
     *
     * @param query
     * @return
     */
    List<Personnel> findPeosonnelByGroup(@Param("userId") String userId, @Param("groupList") List<String> groupList,
                                         @Param("param") PersonnelQuery query);

    /**
     * 查询所有监控对象人（所选分组下的人）
     *
     * @param userId
     * @param query
     * @return
     */
    List<Personnel> findPeosonnelByAssign(@Param("userId") String userId, @Param("param") PersonnelQuery query);

    /**
     * 新增
     */
    @ImportDaoLock(ImportTable.ZW_M_PEOPLE_INFO)
    boolean add(final PersonnelForm form);

    /**
     * 根据id删除一个
     */
    @ImportDaoLock(ImportTable.ZW_M_PEOPLE_INFO)
    boolean delete(final String id);

    /**
     * 根据ids删除一个
     */
    @ImportDaoLock(ImportTable.ZW_M_PEOPLE_INFO)
    boolean deleteMuch(@Param("ids") final String[] ids);

    /**
     * 修改
     */
    @ImportDaoLock(ImportTable.ZW_M_PEOPLE_INFO)
    boolean update(final PersonnelForm form);

    /**
     * 根据身份证编号查询人员信息
     *
     * @param identity
     * @return
     */
    List<Personnel> findByIdentity(@Param("identity") String identity);

    /**
     * 根据人员编号查询人员信息
     *
     * @param peopleNumber
     * @return
     */
    List<Personnel> findByPeopleNumber(@Param("peopleNumber") String peopleNumber);

    /**
     * 批量新增
     *
     * @param deviceForm
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_PEOPLE_INFO)
    boolean addByBatch(@Param("list") Collection<PersonnelForm> deviceForm);

    Personnel findByPersonnel(String identity);

    Personnel isExist(@Param("id") String id, @Param("identity") String identity);

    Personnel findByNumber(String number);

    Personnel findByNumberId(@Param("id") String id, @Param("number") String number);

    /**
     * 通过ID获取终端编号sim卡号
     *
     * @param id
     * @return
     * @author yangyi
     */
    Personnel findNumberByPid(@Param("id") String id);

    /**
     * 查询人员id和编号
     *
     * @param ids 不传查所有
     * @return List<Personnel>
     */
    List<Personnel> findIdAndPeopleNumbersByIds(@Param("ids") Collection<String> ids);

    /**
     * 查询人员id和编号
     *
     * @param numbers 不传查所有
     * @return List<Personnel>
     */
    List<Personnel> findIdAndNumbersByNumbers(@Param("numbers") Collection<String> numbers);

    Personnel findPeopleById(String id);

    /**
     * 查询所有绑定或未绑定的人员详情
     *
     * @return
     */
    List<Map<String, Object>> findPersonnelWithOutAuth();

    @ImportDaoLock(ImportTable.ZW_M_PEOPLE_INFO)
    boolean updatePersonIdentityCardInfo(PersonnelIdentityInfoUploadEntity entity);

    /**
     * 通过分组id获得离职人员
     *
     * @param assignmentIdList assignmentIdList
     * @return List<LeaveJobPersonnel>
     */
    List<LeaveJobPersonnel> getLeaveJobPersonnelList(@Param("list") Collection<String> assignmentIdList);

    /**
     * 获得数据库已存在的人员编号集合
     *
     * @param peopleNumberColl 人员编号集合
     * @return Set<String>
     */
    Set<String> getAlreadyExistPeopleNumberSet(@Param("list") Collection<String> peopleNumberColl);

    /**
     * 获得数据库已存在的身份证号集合
     *
     * @param identityColl 身份证号集合
     * @return Set<String>
     */
    Set<String> getAlreadyExistIdentitySet(@Param("list") Collection<String> identityColl);

    /**
     * 人员信息和绑定信息
     *
     * @return list
     */
    List<ConfigMonitorDTO> findAllPeopleConfig();

    /**
     * 获取数据库已有所有人员信息
     *
     * @return List<Personnel>
     */
    List<Personnel> findAllPeopleInfo();

    /**
     * 更新人员编号
     *
     * @param personnelForm personnelForm
     * @return boolean
     */
    boolean updatePeopleNumber(PersonnelForm personnelForm);

    /**
     * 批量更新企业id
     *
     * @param monitorIds 人员id集合
     * @param orgId      要改成的企业id
     * @param username   更新者
     */
    void updateOrgIdByIdIn(@Param("ids") Set<String> monitorIds,
                           @Param("orgId") String orgId,
                           @Param("updateTime") Date updateTime,
                           @Param("username") String username);
}
