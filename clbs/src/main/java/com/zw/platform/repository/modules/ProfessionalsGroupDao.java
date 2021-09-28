package com.zw.platform.repository.modules;

import com.zw.platform.domain.basicinfo.form.ProfessionalsGroupForm;
import com.zw.platform.domain.basicinfo.query.ProfessionalsQuery;
import com.zw.platform.util.imports.lock.ImportDaoLock;
import com.zw.platform.util.imports.lock.ImportTable;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Title: ProfessionalsGroupDao.java
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 * @version 1.0
 * @author: wangying
 * @date 2016年7月27日下午6:35:27
 */
public interface ProfessionalsGroupDao {
    /**
     * 查询从业人员
     */
    List<Map<String, Object>> findProfessionalsWithGroup(@Param("groupList") List<String> groupList,
        @Param("param") ProfessionalsQuery query);

    /**
         * @return Map<String, Object>
     * @throws @author wangying
     * @Title: 根据id查询从业人员
     */
    Map<String, Object> findProGroupById(String proId);

    /**
         * @param form
     * @return boolean
     * @throws @author wangying
     * @Title: addProfessionsGroup
     */
    @ImportDaoLock(ImportTable.ZW_C_PROFESSIONALS_GROUP)
    boolean addProfessionsGroup(ProfessionalsGroupForm form);

    /**
         * @param formList
     * @return boolean
     * @throws @author wangying
     * @Title: addProFessionsGroupByBatch
     */
    @ImportDaoLock(ImportTable.ZW_C_PROFESSIONALS_GROUP)
    boolean addProFessionsGroupByBatch(@Param("list") Collection<ProfessionalsGroupForm> formList);

    /**
         * @param form
     * @return boolean
     * @throws @author wangying
     * @Title: updateProGroupByProId
     */
    @ImportDaoLock(ImportTable.ZW_C_PROFESSIONALS_GROUP)
    boolean updateProGroupByProId(ProfessionalsGroupForm form);

    /**
     * 查询从业人员,组和车
     */
    List<Map<String, Object>> findProfessionalsAndVehicle(@Param("groupList") List<String> groupList);

    /**
     * 删除从业人员和组织关联
     * @param id
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_C_PROFESSIONALS_GROUP)
    int deleteProGroupByProId(String id);

    /**
     * 批量删除从业人员和组织关联表
     * @param ids
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_C_PROFESSIONALS_GROUP)
    int deleteProGroupByBacth(List<String> ids);

    /**
     * 根据从业人员id查询组织id
     * @param id
     * @return
     */
    String findProfessionalsGroupId(String id);
}
