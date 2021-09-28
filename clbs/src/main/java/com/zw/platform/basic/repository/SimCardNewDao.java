package com.zw.platform.basic.repository;

import com.zw.platform.basic.domain.BaseKvtDo;
import com.zw.platform.basic.domain.SimCardDO;
import com.zw.platform.basic.domain.SimCardInfoDo;
import com.zw.platform.basic.domain.SimCardListDO;
import com.zw.platform.basic.dto.F3SimCardDTO;
import com.zw.platform.basic.dto.SimCardDTO;
import com.zw.platform.domain.basicinfo.SimcardInfo;
import com.zw.platform.domain.basicinfo.query.SimcardQuery;
import com.zw.platform.util.imports.lock.ImportDaoLock;
import com.zw.platform.util.imports.lock.ImportTable;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SimCardNewDao {
    /**
     * 新增sim卡信息
     * @param simCardDO
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_SIM_CARD_INFO)
    boolean add(SimCardDO simCardDO);

    /**
     * 批量新增
     * @param simCardList
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_SIM_CARD_INFO)
    boolean addByBatch(@Param("list") Collection<SimCardDO> simCardList);

    /**
     * 通过id和simCardNumber查找
     * @param number
     * @param id
     * @return
     */
    String getNoRepeatNumber(@Param("number") String number, @Param("id") String id);

    /**
     * 根据sim卡id查询绑定监控对象id
     * @param id
     * @return
     */
    String getBindMonitorId(@Param("id") String id);

    /**
     * 根据id查询sim卡
     */
    SimCardInfoDo getById(@Param("id") String id);

    /**
     * 删除sim卡
     * @param id
     * @return
     */
    void deleteById(@Param("id") String id);

    /**
     * 批量删除SIM卡
     * @param ids ids
     * @return 是否操作成功
     */
    boolean deleteByBatch(@Param("ids") Collection<String> ids);

    /**
     * 根据sim卡号查询绑定的监控对象id
     * @param number
     * @return
     */
    String getMonitorIdByNumber(@Param("number") String number);

    /**
     * 修改终端
     * @param simCardDO
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_SIM_CARD_INFO)
    boolean updateSimCard(SimCardDO simCardDO);

    /**
     * @param ids
     * @return
     */
    List<String> getBindMonitorIds(@Param("ids") List<String> ids);

    /**
     * 根据sim卡id查询sim卡列表
     * @param ids
     * @return
     */
    List<SimCardListDO> getSimCardList(@Param("ids") Collection<String> ids);

    /**
     * 模糊搜索真实sim卡号
     * @param number
     * @return
     */
    List<String> findReal(@Param("number") String number);

    /**
     * 根据sim卡号查询下发状态信息
     * @param ids sim卡id集合
     * @return 参数下发状态
     */
    @MapKey("keyName")
    Map<String, BaseKvtDo<String, String, Integer>> findSendStatusMapByIds(@Param("ids") Collection<String> ids);

    /**
     * 根据id更新sim卡号
     * @param id
     * @param number
     * @return
     */
    boolean updateNumber(@Param("id") String id, @Param("number") String number, @Param("realNum") String realNum);

    /**
     * 获取所有的sim卡号
     * @return
     */
    Set<String> getAllSimCardNumber();

    /**
     * 获取指定企业下的sim卡id
     * @param orgId
     * @return
     */
    Set<String> getOrgSimCardIds(@Param("orgId") String orgId);

    /**
     * 根据sim卡号查询sim卡
     * @param numbers 若sim卡号为空 查询全部
     * @return sim卡列表
     */
    List<SimCardDTO> getByNumbers(@Param("numbers") Collection<String> numbers);

    /**
     * 根据终端手机号获取SIM卡
     * @param simCardNumber 终端手机号
     * @return sim卡信息
     */
    SimCardDTO getByNumber(@Param("simCardNumber") String simCardNumber);

    /**
     * 获取SIM卡的顺序列表
     * @return sim卡的顺序列表  按时间升序
     */
    List<String> getSortList();

    /**
     * 根据终端Id获取SIM卡信息
     * @param deviceIds 终端Id集合
     * @return SIM卡信息
     */
    List<SimCardDTO> getByDeviceIds(@Param("deviceIds") Collection<String> deviceIds);

    /**
     * 根据sim卡id获取f3sim卡信息
     * @param id
     * @return
     */
    F3SimCardDTO getF3SimInfo(String id);

    /**
     * 根据id查询sim卡
     */
    SimcardInfo findSimcardById(@Param("id") String id);


    /**
     * 根据sim卡编号查询sim卡信息
     */
    SimcardInfo findSimcardBySimcardNumber(@Param("simcardNumber") String simcardNumber);

    /**
     * 根据用户查询其组织下的simcard
     * @author Fan Lu
     */
    List<Map<String, Object>> findSimcardByUser(@Param("groupList") List<String> groupId,
        @Param("param") SimcardQuery query);

    /**
     * 查询sim卡及其组织
     * @author Fan Lu
     */
    Map<String, Object> findSimcardGroupById(String id);

    SimcardInfo findBySIMCard(String simcardNumber);

    /**
     * 查询simcard是否已经绑定组织
     * @return 是否绑定组织
     * @author Fan Lu
     */
    int getIsBand(String id);

    SimcardInfo isExist(@Param("id") String id, @Param("simcardNumber") String simcardNumber);

    /**
     * 获取鉴权码
     */
    String getAuthCodeBySimId(String simId);

}
