package com.zw.talkback.repository.mysql;

import com.zw.platform.domain.basicinfo.query.PersonnelQuery;
import com.zw.talkback.domain.basicinfo.InterlocutorInfo;
import com.zw.talkback.domain.basicinfo.LeaveJobPersonnel;
import com.zw.talkback.domain.basicinfo.form.Personnel;
import com.zw.talkback.domain.basicinfo.form.PersonnelForm;
import com.zw.talkback.domain.intercom.form.IntercomObjectForm;
import com.zw.talkback.domain.intercom.info.IntercomObjectInfo;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by xiaoyun on 2019/11/1.
 */
public interface IntercomPersonnelDao {
    /**
     * 通过id得到一个 Personnel
     */
    Personnel get(final String id);

    List<Personnel> getPeople(@Param("peopleList") List<String> peopleList);

    /**
     * 查询
     */
    List<Personnel> find(final PersonnelQuery query);

    /**
     * 查询所有监控对象人（游离的+权限内分组下的人）
     * @param query
     * @return
     */
    List<Personnel> findPeosonnelByGroup(@Param("userId") String userId, @Param("groupList") List<String> groupList,
        @Param("param") PersonnelQuery query);

    /**
     * 查询所有监控对象人（所选分组下的人）
     * @param userId
     * @param query
     * @return
     */
    List<Personnel> findPeosonnelByAssign(@Param("userId") String userId, @Param("param") PersonnelQuery query);

    /**
     * 新增
     */
    boolean add(final PersonnelForm form);

    /**
     * 根据id删除一个
     */
    boolean delete(final String id);

    /**
     * 根据ids删除一个
     */
    boolean deleteMuch(@Param("ids") final String[] ids);

    /**
     * 修改
     */
    boolean update(final PersonnelForm form);

    /**
     * 根据身份证编号查询人员信息
     * @param identity
     * @return
     */
    List<Personnel> findByIdentity(@Param("identity") String identity);

    /**
     * 根据人员编号查询人员信息
     * @param peopleNumber
     * @return
     */
    List<Personnel> findByPeopleNumber(@Param("peopleNumber") String peopleNumber);

    /**
     * 批量新增
     * @param deviceForm
     * @return
     */
    boolean addByBatch(@Param("list") Collection<PersonnelForm> deviceForm);

    boolean upDateByBatch(@Param("list") Collection<PersonnelForm> deviceForm);

    Personnel findByPersonnel(String identity);

    Personnel isExist(@Param("id") String id, @Param("identity") String identity);

    Personnel findByNumber(String number);

    Personnel findByNumberId(@Param("id") String id, @Param("number") String number);

    /**
     * 通过ID获取终端编号sim卡号
     * @param id
     * @return
     * @author yangyi
     */
    Personnel findNumberByPid(@Param("id") String id);

    Personnel findPeopleById(String id);

    /**
     * 查询所有绑定或未绑定的人员详情
     * @return
     */
    List<Map<String, Object>> findIntercomPersonnelWithOutAuth();

    boolean updateWorkStae(@Param("id") String id, @Param("state") Integer state);

    boolean saveLeaveJob(@Param("list") List<LeaveJobPersonnel> list);

    List<String> findAllPeopleNumber();

    /**
     * 删除离职人员的分组关系表
     * @return
     */
    boolean deleteLeaveJob(List<String> ids);

    boolean updateWorkStaeToOnLine(String id);

    /**
     * 通过分组id获得离职人员
     * @param assignmentIdList
     * @return
     */
    List<LeaveJobPersonnel> getLeaveJobPersonnelList(List<String> assignmentIdList);

    /**
     * 统计分组下离职人员数量
     * @param assignmentId
     * @return
     */
    int countLeaveJobPeopleNum(@Param("assignmentId") String assignmentId);

    Personnel findJobByPeopleId(String id);

    List<String> findPeopleDesignateIds(String id);

    List<String> findPeopleScheduledIds(String id);

    boolean deletePeopleDesignate(String id);

    boolean deletePeopleScheduled(List<String> ids);

    boolean updateLeaveJobScheduled(@Param("list") List<String> id);

    boolean deleteLeaveJobScheduled(String id);

    Set<String> getAllPeopleIds();

    boolean deletePeopleDesignateByIds(List<String> ids);

    /**
     * 获得人员id 通过职位id
     * @param jobId
     * @return
     */
    List<String> getPeopleIdListByJobId(@Param("jobId") String jobId);

    /**
     * 通过企业id获得企业和下级企业的分组下监控对象id
     * @param groupIdList
     * @return
     */
    Set<String> getPeopleIdByGroupId(List<String> groupIdList);

    IntercomObjectForm findIntercomInfoByPeopleId(String id);

    /**
     * 通过条件查找对讲对象
     * @param monitorIdList               监控对象id
     * @param assignmentIdList            分组
     * @param skillIdList                 技能
     * @param intercomModelIdList         对讲机型
     * @param driverLicenseCategoryIdList 驾照类型
     * @param qualificationIdList         资格证
     * @param bloodTypeIdList             血型
     * @param ageRangeList                年龄范围
     * @param gender                      性别
     * @return List<InterlocutorInfo>
     */
    List<InterlocutorInfo> findInterlocutorByCondition(@Param("monitorIdList") List<String> monitorIdList,
        @Param("assignmentIdList") List<String> assignmentIdList, @Param("skillIdList") List<String> skillIdList,
        @Param("intercomModelIdList") List<String> intercomModelIdList,
        @Param("driverLicenseCategoryIdList") List<String> driverLicenseCategoryIdList,
        @Param("qualificationIdList") List<String> qualificationIdList,
        @Param("bloodTypeIdList") List<String> bloodTypeIdList, @Param("ageRangeList") List<String> ageRangeList,
        @Param("gender") String gender);

    /**
     * 通过对讲对象id 查询对讲对象信息
     * @param interlocutorIdList 对讲对象id集合
     * @return List<InterlocutorInfo>
     */
    List<InterlocutorInfo> getInterlocutorInfoByInterlocutorIdList(
        @Param("interlocutorIdList") List<Long> interlocutorIdList);

    /**
     * 通过对讲对象id 查询对讲对象信息
     * @param interlocutorId 对讲对象id
     * @return InterlocutorInfo
     */
    InterlocutorInfo getInterlocutorInfoByInterlocutorId(@Param("interlocutorId") Long interlocutorId);

    Integer findKnobNum(String id);

    List<Integer> getVehicleKnobNumbers(String vid);

    List<IntercomObjectInfo> findAssignmentUseKnobNo(@Param("intercomInfoId") String intercomInfoId);

    /**
     * 批量用户的在职状态
     * @param peopleIds
     * @return 是否更新成功
     */
    boolean updatePeopleincumbency(@Param("peopleIds") List<String> peopleIds);

    String getConfigIdByVehicleId(String vehicleId);

    String getConfigIdByVehicleIds(@Param("ids") final String[] ids);

    /**
     * 批量更新用户的在职状态
     * @param ids ids
     * @return 是否更新成功
     */
    boolean updateIncumbency(@Param("ids") List<String> ids);

}
