package com.zw.platform.repository.modules;

import com.zw.platform.domain.functionconfig.FenceInfo;
import com.zw.platform.domain.functionconfig.form.ManageFenceFrom;
import com.zw.platform.domain.regionmanagement.FenceTypeFrom;
import com.zw.platform.domain.regionmanagement.FenceTypeInfo;
import com.zw.platform.domain.regionmanagement.UserFenceDisplaySet;
import com.zw.platform.domain.scheduledmanagement.SchedulingInfo;
import com.zw.platform.domain.taskmanagement.DesignateInfo;
import com.zw.platform.domain.taskmanagement.TaskInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/11/5 11:05
 */
public interface FenceManagementDao {

    /**
     * 获得所有围栏种类
     * @return List<FenceTypeInfo>
     */
    List<FenceTypeInfo> getAllFenceType();

    List<FenceInfo> getFenceTypeList(@Param("organizationIdList") List<String> organizationIdList);

    /**
     * 获得围栏种类信息
     * @param fenceTypeId 围栏种类id
     * @return FenceTypeInfo
     */
    FenceTypeInfo getFenceTypeInfoByFenceTypeId(String fenceTypeId);

    /**
     * 获得围栏种类信息
     * @param fenceTypeName 围栏种类名称
     * @return FenceTypeInfo
     */
    FenceTypeInfo getFenceTypeInfoByFenceTypeName(String fenceTypeName);

    /**
     * 获得围栏种类下已经绘制的围栏类型
     * @param fenceTypeId 围栏种类id
     * @return 已经绘制的围栏类型
     */
    List<String> getAlreadyDrawFenceByFenceTypeId(String fenceTypeId);

    /**
     * 新增围栏种类
     * @param fenceTypeFrom 围栏信息
     * @return boolean
     */
    boolean addFenceType(FenceTypeFrom fenceTypeFrom);

    /**
     * 删除围栏种类通过id
     * @param fenceTypeId 围栏种类id
     * @return boolean
     */
    boolean deleteFenceTypeByFenceTypeId(String fenceTypeId);

    /**
     * 修改围栏种类
     * @param fenceTypeFrom 围栏信息
     * @return boolean
     */
    boolean updateFenceType(FenceTypeFrom fenceTypeFrom);

    /**
     * 获得围栏种类下的围栏信息集合
     * @param fenceTypeId        围栏种类id
     * @param organizationIdList 组织id
     * @return List<FenceInfo>
     */
    List<FenceInfo> getFenceInfoList(@Param("fenceTypeId") String fenceTypeId,
        @Param("organizationIdList") List<String> organizationIdList);

    /**
     * 获得围栏信息
     * @param fenceTypeId 围栏种类id
     * @param fenceName   围栏名称
     * @return 围栏信息
     */
    FenceInfo getFenceInfo(@Param("fenceTypeId") String fenceTypeId, @Param("fenceName") String fenceName);

    /**
     * 获得围栏信息
     * @param fenceId 围栏id
     * @return FenceInfo
     */
    FenceInfo getFenceInfoByFenceId(String fenceId);

    /**
     * 获得围栏关联的排班
     * @param fenceId 围栏id
     * @return List<SchedulingItemInfo>
     */
    List<SchedulingInfo> getFenceRelationSchedulingInfoList(String fenceId);

    /**
     * 获得围栏关联的任务
     * @param fenceId 围栏id
     * @return List<TaskItem>
     */
    List<TaskInfo> getFenceRelationTaskInfoList(String fenceId);

    /**
     * 获得围栏管理任务指派
     * @param fenceId 围栏id
     * @return List<DesignateInfo>
     */
    List<DesignateInfo> getFenceRelationDesignateInfoList(String fenceId);

    /**
     * 修改围栏面积
     * @param fenceFrom 围栏信息
     * @return boolean
     */
    boolean updateFenceArea(ManageFenceFrom fenceFrom);

    /**
     * 获得用户围栏显示设置
     * @param userId 用户id
     * @return List<String>
     */
    List<String> getUserFenceDisplaySetting(String userId);

    List<FenceInfo> findSettingFenceInfo(@Param("userId") String userId);

    /**
     * 删除用户围栏显示设置
     * @param userId 用户id
     * @return boolean
     */
    boolean deleteUserFenceDisplaySet(String userId);

    /**
     * 保存用户围栏显示设置
     * @param userFenceDisplaySetList userFenceDisplaySetList
     * @return boolean
     */
    boolean saveUserFenceDisplaySet(List<UserFenceDisplaySet> userFenceDisplaySetList);

    /**
     * 删除围栏
     * @param fenceId fenceId
     * @return 是否删除成功
     */
    boolean deleteFenceInfo(String fenceId);
}
