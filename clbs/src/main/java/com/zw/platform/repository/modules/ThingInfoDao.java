package com.zw.platform.repository.modules;


import com.zw.platform.domain.basicinfo.ThingInfo;
import com.zw.platform.domain.basicinfo.form.ThingInfoForm;
import com.zw.platform.domain.basicinfo.query.ThingInfoQuery;
import com.zw.platform.domain.infoconfig.dto.ConfigMonitorDTO;
import com.zw.platform.util.imports.lock.ImportDaoLock;
import com.zw.platform.util.imports.lock.ImportTable;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;
import java.util.Map;


@Deprecated
public interface ThingInfoDao {
    /**
     * 查询
     */
    List<ThingInfo> find(ThingInfoQuery query);

    /**
     * 新增
     */
    @ImportDaoLock(ImportTable.ZW_M_THING_INFO)
    boolean add(ThingInfoForm form);

    /**
     * 批量新增
     *
     * @param form
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_THING_INFO)
    boolean addByBatch(List<ThingInfoForm> form);

    /**
     * 根据id删除一个
     */
    @ImportDaoLock(ImportTable.ZW_M_THING_INFO)
    boolean delete(String id);

    /**
     * 根据id获得一个ThingInfo
     */
    ThingInfo get(final String id);

    /**
     * 通过id查询物品信息
     * @param ids 物品id
     * @return ThingInfo
     */
    List<ThingInfo> getByIds(@Param("ids") Collection<String> ids);

    List<ThingInfo> getAll();

    /**
     * 修改 User
     */
    @ImportDaoLock(ImportTable.ZW_M_THING_INFO)
    int update(final ThingInfoForm form);

    @ImportDaoLock(ImportTable.ZW_M_THING_INFO)
    boolean addThingInfoByBatch(@Param("importList") Collection<ThingInfoForm> importList);

    List<ThingInfo> findByThingNumber(@Param("thingNumber") String thingNumber);

    /**
     * 查询物品id和编号
     *
     * @param thingNumbers 物品编号
     * @return List<ThingInfo>
     */
    List<ThingInfo> findIdAndNumbersByNumbers(@Param("numbers") Collection<String> thingNumbers);

    ThingInfo findByThingInfo(String thingNumber);

    ThingInfo isExist(@Param("id") String id, @Param("thingNumber") String thingNumber);

    /**
     * 根据ids批量删除
     */
    @ImportDaoLock(ImportTable.ZW_M_THING_INFO)
    boolean deleteMuch(@Param("ids") final String[] ids);

    /**
     * 根据type获取物品类型
     *
     * @param type
     * @return
     */
    @Select("select code, value from zw_c_dictionary where type = #{type}")
    @ResultType(List.class)
    List<Map<String, String>> findDictionaryByType(@Param("type") String type);


    /**
     * 查询所有的物品（有分组的和游离的物品），无权限控制
     *
     * @return List<Map < String, Object>>
     * @author FanLu
     */
    List<Map<String, Object>> findThingInfoWithOutAuth();

    /**
     * 物品以及绑定关系信息
     *
     * @return list
     */
    List<ConfigMonitorDTO> findAllThingConfig();

    /**
     * 修改物品编号
     *
     * @param thingInfoForm thingInfoForm
     * @return boolean
     */
    boolean updateThingNumber(ThingInfoForm thingInfoForm);

    /**
     * 批量更新企业id
     *
     * @param groupId  要改成的企业id
     * @param thingIds 物品id集合
     */
    void updateGroupIdByIdIn(@Param("groupId") String groupId, @Param("thingIds") Collection<String> thingIds);
}
