package com.zw.platform.repository.modules;

import com.zw.platform.domain.basicinfo.Ico;
import com.zw.platform.domain.basicinfo.Personalized;
import com.zw.platform.domain.basicinfo.form.VehicleForm;
import com.zw.platform.domain.basicinfo.query.UseIco;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PersonalizedDao {

    boolean updateLogo(Personalized personalized);//平台个性化修改

    Personalized find(String groupId);//查询平台个性化设置参数

    boolean add(Personalized personalized);//新增平台个性化修改

    boolean addIco(Ico ico);//新增监控对象图标

    List<Ico> findIco();//查询所有图标

    boolean delIco(String id);//删除单个图标

    Ico findIcoByID(String id);//通过图标ID查询图标名字

    Ico findObjectIcon(String id);//通过对象ID查询该对象的图标

    Ico findSubTypeIcon(String id); //通过对象ID查询该对象的子类型图标

    boolean updateIco(@Param("defultState") String defultState, @Param("id") String id);//修改个性化图标的默认状态，1 改为 0

    boolean updateVehicleIco(@Param("list") List<String> ids, @Param("vehicleIcon") String vehicleIcon);//修改车辆图标

    boolean updatePeopleIco(@Param("list") List<String> ids, @Param("peopleIcon") String peopleIcon);//修改人员图标

    boolean updateThingIco(@Param("list") List<String> ids, @Param("thingIcon") String thingIcon);//修改物品图标

    boolean deflutVehicleIco(@Param("list") List<String> ids);//默认车辆图标

    boolean deflutPeopleIco(@Param("list") List<String> ids);//默认人员图标

    boolean deflutThingIco(@Param("list") List<String> ids);//默认物品图标

    Personalized findByPermission(@Param("groupId") String groupId, @Param("roleIds") List<String> roleIds);

    /**
     * 获取车辆使用的图标文件名
     * @param monitorIds
     * @return
     */
    List<UseIco> getVehicleUseIcoNames(@Param("monitorIds") List<String> monitorIds);

    /**
     * 获取车辆使用的图标文件名
     * @param vehicleForm
     * @return
     */
    UseIco getVehicleUseIcoNamesByType(@Param("vehicleForm") VehicleForm vehicleForm);

    /**
     * 根据图标id获取图标文件名
     * @param icoId
     * @return
     */
    String getIcoNameById(String icoId);

    /**
     * 根据子类型id获取所有使用该子类型车辆id
     * @param subTypeId
     * @return
     */
    List<String> getVidsBySubTypeId(String subTypeId);

    /**
     * 根据类别id获取所有使用该类别车辆id
     * @param categoryId
     * @return
     */
    List<String> getVidsByCategoryId(String categoryId);

    /**
     * 根据平台网址（platformSite）查询平台个性化设置参数
     * @param platformSite
     * @return
     */
    List<Personalized> findByPlatformSite(String platformSite);
}
