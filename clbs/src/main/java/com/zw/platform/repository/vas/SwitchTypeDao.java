package com.zw.platform.repository.vas;

import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.alram.IoVehicleConfigInfo;
import com.zw.platform.domain.vas.switching.SwitchType;
import com.zw.platform.util.common.BaseQueryBean;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * Title:开关类传感器类型Dao
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年06月21日 14:13
 */
public interface SwitchTypeDao {

    /**
     * 查询分页数据
     *
     * @param query
     * @return
     */
    Page<SwitchType> findByPage(BaseQueryBean query);

    /**
     * 查询所有可用
     *
     * @return
     */
    List<SwitchType> findAllow();


    /**
     * 根据主键ID查询外设信息
     *
     * @param id
     * @return
     */
    SwitchType findByid(String id);

    /**
     * 通过name获取信息
     *
     * @param name
     * @return
     */
    SwitchType findByName(@Param("name") String name);

    /**
     * 通过identify获取信息
     *
     * @param identify
     * @return
     */
    SwitchType findByIdentify(@Param("identify") String identify);

    /**
     * 通过id检查是否已被绑定
     *
     * @param id
     * @return
     */
    Integer checkBind(String id);

    /**
     * 新增
     *
     * @param switchType
     */
    boolean add(SwitchType switchType);

    /**
     * 新增
     * @param switchTypes
     */
    boolean addBatch(List<SwitchType> switchTypes);




    /**
     * 修改
     *
     * @param switchType
     */
    boolean updateSwitchType(SwitchType switchType);


    /**
     * 根据ID删除外设信息
     *
     * @param id
     */
    boolean deleteById(String id);

    /**
     * 批量删除
     *
     * @param ids
     */
    boolean deleteBatchSwitchType(@Param("ids")List<String> ids);

    SwitchType findByStateRepetition(@Param("id")String id, @Param("state")String state, @Param("flag")Integer flag);

    /**
     * 获得io的功能参数类型
     * @return
     */
    List<SwitchType> getIoSwitchType();

    /**
     * 获得车辆功能id绑定的io位置
     *
     * @param vehicleIds
     * @param identify
     * @return
     */
    List<IoVehicleConfigInfo> getFuntcionIdBingIoSite(@Param("vehicleIds") Collection<String> vehicleIds,
        @Param("identify") String  identify);
}
