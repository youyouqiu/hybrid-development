package com.zw.platform.repository.modules;

import com.zw.platform.domain.basicinfo.ConfigSimDto;
import com.zw.platform.domain.basicinfo.SimcardInfo;
import com.zw.platform.domain.basicinfo.form.SimGroupForm;
import com.zw.platform.domain.basicinfo.form.SimcardForm;
import com.zw.platform.domain.basicinfo.query.SimcardQuery;
import com.zw.platform.domain.infoconfig.dto.ConfigMonitorDTO;
import com.zw.platform.util.imports.lock.ImportDaoLock;
import com.zw.platform.util.imports.lock.ImportTable;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * sim卡Dao
 * @author wangying
 */
@Deprecated
public interface SimcardDao {

    /**
     * 根据条件查询记录总数
     */
    int countByParams(@Param("condition") SimcardQuery condition);

    /**
     * 查询sim卡信息
     */
    List<SimcardInfo> findSimcard(SimcardQuery query);

    /**
     * 根据id查询sim卡
     */
    SimcardInfo findSimcardById(@Param("id") String id);

    /**
     * 根据sim卡编号查询sim卡信息
     */
    SimcardInfo findSimcardBySimcardNumber(@Param("simcardNumber") String simcardNumber);

    /**
     * 获取所有的sim卡号
      * @return
     */
    Set<String> getAllSimCardNumber();

    /**
     * 新增sim卡
     */
    @ImportDaoLock(ImportTable.ZW_M_SIM_CARD_INFO)
    boolean addSimcard(SimcardForm simcardForm);

    /**
     * 批量新增sim卡
     */
    @ImportDaoLock(ImportTable.ZW_M_SIM_CARD_INFO)
    boolean addSimcardByBatch(@Param("list") Collection<SimcardForm> simcardFormList);

    /**
     * 修改sim卡
     */
    @ImportDaoLock(ImportTable.ZW_M_SIM_CARD_INFO)
    boolean updateSimcard(SimcardForm simcardForm);

    /**
     * 修改sim卡号(用于信息配置修改)
     * @param simcardForm
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_SIM_CARD_INFO)
    boolean updateSimcardNumber(SimcardForm simcardForm);

    /**
     * 删除sim卡信息
     */
    @ImportDaoLock(ImportTable.ZW_M_SIM_CARD_INFO)
    boolean deleteSimcardById(String id);

    /**
     * 批量删除
     */
    @ImportDaoLock(ImportTable.ZW_M_SIM_CARD_INFO)
    boolean deleteSimcardByBatch(String[] ids);

    /**
     * 根据用户查询其组织下的simcard
     * @author Fan Lu
     */
    List<Map<String, Object>> findSimcardByUser(@Param("groupList") List<String> groupId,
        @Param("param") SimcardQuery query);

    /**
     * 查询所有的simcard信息，无权限控制
     * @author Fan Lu
     */
    List<Map<String, Object>> findAllSimcard();

    /**
     * 新增sim卡与组织关联关系
     * @author Fan Lu
     */
    @ImportDaoLock(ImportTable.ZW_M_SIM_GROUP)
    boolean addSimcardGroup(SimGroupForm groupForm);

    /**
     * 修改sim卡与组织关联关系
     * @author Fan Lu
     */
    @ImportDaoLock(ImportTable.ZW_M_SIM_GROUP)
    boolean updateSimcardGroup(SimGroupForm groupForm);

    /**
     * 批量删除sim卡与组织的关联关系
     * @return boolean
     * @author wangying
     * @since 2017年1月18日 下午4:30:29
     */
    @ImportDaoLock(ImportTable.ZW_M_SIM_GROUP)
    boolean deleteSimcardGroupByBatch(String[] ids);

    /**
     * 查询sim卡及其组织
     * @author Fan Lu
     */
    Map<String, Object> findSimcardGroupById(String id);

    /**
     *获取所有有绑定关系的sim卡Id
     * @return list
     */
    List<String> getAllSimCardIdByGroup();

    /**
     * 批量导入sim卡组织关联
     * @author Fan Lu
     */
    @ImportDaoLock(ImportTable.ZW_M_SIM_GROUP)
    boolean addSimcardGroupByBatch(@Param("list") Collection<SimGroupForm> formList);

    SimcardInfo findBySIMCard(String simcardNumber);

    /**
     * 根据fekeIp查询simId
     */
    List<String> findSimIdByFakeIp(String fakeIp);

    /**
     * 查询simcard是否已经绑定组织
     * @return 是否绑定组织
     * @author Fan Lu
     */
    int getIsBand(String id);

    /**
     * 根据SIM卡id查询卡号
     * @author wjy
     */
    String getSIMcard(String id);

    SimcardInfo isExist(@Param("id") String id, @Param("simcardNumber") String simcardNumber);

    Map<String, Object> getF3SimInfo(String id);

    String findSimGroupId(String id);

    int getSimcardCount(@Param("groupList") List<String> groupId, @Param("param") SimcardQuery query);

    List<Map<String, String>> groupAndSimMap(@Param("simcardIds") List<String> simcardIds);

    List<Map<String, String>> simcardIdAndGroupId(@Param("simcardIds") List<String> asList);

    /**
     * 获取鉴权码
     */
    String getAuthCodeBySimId(String simId);

    /**
     * 更新真实SIM卡号
     */
    void updateRealSimCard(@Param("simCardId") String simCardId, @Param("realId") String realId);

    /**
     * 模糊搜索真实SIM卡号
     */
    List<String> findReal(String query);

    List<SimcardInfo> findUnbind(Collection<String> orgIds);

    List<SimcardInfo> findSimcardByIds(@Param("simCardId")String[] simCardId);

    List<String> getSimCardNumberBySimId(@Param("simCardIds") List<String> simCardIds);

    List<ConfigSimDto> listConfig();

    /**
     * 查询sim以及sim与config绑定关系
     * @return list
     */
    List<ConfigMonitorDTO> findAllSimCardConfig();
}
