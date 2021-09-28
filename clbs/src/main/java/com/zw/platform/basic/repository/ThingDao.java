package com.zw.platform.basic.repository;

import com.zw.platform.basic.domain.BaseKvtDo;
import com.zw.platform.basic.domain.ThingDO;
import com.zw.platform.basic.dto.MonitorBaseDTO;
import com.zw.platform.basic.dto.ThingDTO;
import com.zw.platform.basic.dto.export.ThingExportDTO;
import com.zw.platform.domain.basicinfo.ThingInfo;
import com.zw.platform.util.imports.lock.ImportDaoLock;
import com.zw.platform.util.imports.lock.ImportTable;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 物品管理DAO层
 * @author XK
 * @date 2020/10/19
 */
public interface ThingDao {

    List<ThingDTO> getAll();

    /**
     * 获取物品按时间的排序ID
     * @return 顺序ID
     */
    List<String> getSortList();

    /**
     * 获取物品信息初始化缓存的查询列表
     * @param ids 物品ID
     * @return 物品信息集合
     */
    List<ThingDTO> initCacheList(@Param("ids") List<String> ids);

    /**
     * 获取物品的个性化图标
     * @return 个性化图标
     */
    List<BaseKvtDo<String, String, String>> getIconList();

    /**
     * 获取物品详情--信息全面
     * @param id id
     * @return 物品详情
     */
    ThingDTO getDetailById(@Param("id") String id);

    /**
     * 根据标号获取物品
     * @param number number
     * @return 物品详情
     */
    ThingDTO getDetailByNumber(@Param("number") String number);

    /**
     * 批量获取物品详情--信息全面
     * 若查询数据量太大不建议使用该方法，消耗性能
     * @param ids id集合
     * @return 物品详情
     */
    List<ThingDTO> getDetailByIds(@Param("ids") Collection<String> ids);

    /**
     * 通过id查询物品信息
     * @param ids 物品id
     * @return ThingInfo
     */
    List<ThingInfo> getByIds(@Param("ids") Collection<String> ids);

    /**
     * 物品添加
     * @param thingDO vehicleDO
     * @return true 添加成功 false 添加失败
     */
    @ImportDaoLock(ImportTable.ZW_M_THING_INFO)
    boolean insert(ThingDO thingDO);

    /**
     * 根据ID获取物品信息
     * @param id id
     * @return ThingDO
     */
    ThingDO getById(@Param("id") String id);

    /**
     * 根据物品编号获取监控对象
     * @param brand 物品编号
     * @return ThingDO
     */
    ThingDO getByBrand(@Param("brand") String brand);

    /**
     * 根据物品ID获取物品导出列表
     * @param ids ids
     * @return 导出列表
     */
    List<ThingExportDTO> getExportList(@Param("ids") Collection<String> ids);

    /**
     * 物品更新
     * @param vehicleDO vehicleDO
     * @return 是否操作成功
     */
    @ImportDaoLock(ImportTable.ZW_M_THING_INFO)
    boolean update(ThingDO vehicleDO);

    /**
     * 个性化图标更新
     * @param ids    物品ID集合
     * @param iconId 图标ID
     * @return 是否操作成功
     */
    @ImportDaoLock(ImportTable.ZW_M_THING_INFO)
    boolean updateIcon(@Param("ids") Collection<String> ids, @Param("iconId") String iconId);

    /**
     * 物品删除
     * @param ids ids
     * @return 删除数量
     */
    @ImportDaoLock(ImportTable.ZW_M_THING_INFO)
    int delete(@Param("ids") Collection<String> ids);

    /**
     * 批量新增物品信息
     * @param importList 物品
     * @return 更新条数
     */
    @ImportDaoLock(ImportTable.ZW_M_THING_INFO)
    boolean addThingInfoByBatch(@Param("importList") Collection<ThingDO> importList);

    /**
     * 根据物品编号查询物品基础信息
     * @param thingNumList 物品编号为空时 查询全部
     * @return 物品基础信息
     */
    List<MonitorBaseDTO> getByNumbers(@Param("thingNumList") Collection<String> thingNumList);

    /**
     * 更新物品编号
     * @param id     物品Id
     * @param number 物品编号
     * @return 是否更新成功
     */
    @ImportDaoLock(ImportTable.ZW_M_THING_INFO)
    boolean updateNumber(@Param("id") String id, @Param("number") String number);

    /**
     * 用户进行物品编号的唯一性校验
     * @param number
     * @param id
     * @return
     */
    String getThingIdByNumberAndId(@Param("number") String number, @Param("id") String id);

    /**
     * 根据以该编号结尾的物品编号
     * @param number 编号
     * @return 物品编号
     */
    List<String> getScanByNumber(@Param("number") String number);
}
