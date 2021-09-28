package com.zw.platform.repository.modules;

import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.intercomplatform.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by LiaoYuecai on 2017/3/3.
 */
public interface IntercomPlatFormDao {

    /**
     * 查询所有的对讲平台信息
     * @param query
     * @return
     */
    public List<IntercomPlatForm> findList(IntercomPlatFormQuery query);

    /**
     * 新增对讲平台信息
     * @param form
     */
    public boolean add(IntercomPlatForm form);

    /**
     * 根据id删除对讲平台
     * @param id
     */
    public boolean deleteById(String id);

    /**
     * 修改对讲平台信息
     * @param form
     */
    public boolean update(IntercomPlatForm form);

    /**
     * 根据id查询对讲平台信息
     * @param id
     * @return
     */
    public IntercomPlatForm findById(String id);

    public List<IntercomPlatFormConfigView> findConfigViewList(IntercomPlatFormConfigQuery query);

    public List<IntercomPlatFormConfigView> findConfigViewListByVehicleId(String vehicleId);

    public IntercomPlatFormConfigView findConfigViewByConfigId(String configId);

    public Integer findConfigViewListByIds(String vehicleId, String platFormId);

    public void addConfig(IntercomPlatFormConfig config);

    public void updateConfigById(IntercomPlatFormConfig config);

    public void deleteConfigById(String id);

    public IntercomPlatFormConfig findConfigById(String id);

    public List<String> findConFigIdByVIds(@Param("vehicleIds") List<String> vehicleIds);

    public List<String> findConFigIdByPIds(@Param("pids") List<String> pids);

    public String findConFigIdByVId(@Param("vehicleId") String vehicleId);
    
    /**
     * 根据分组ids查询车辆（已在config中绑定，未绑定对讲平台）
     * @param assignmentIds
     * @return
     */
    public List<VehicleInfo> findVehicleTreeByPlatform(@Param("assignmentIds") List<String> assignmentIds);

    /**
     * 根据分组ids查询车辆（已在config中绑定，未绑定第三方平台）
     * @param assignmentIds
     * @return
     */
    public List<VehicleInfo> findVehicleTreeByThirdPlatform(@Param("assignmentIds") List<String> assignmentIds);
}
