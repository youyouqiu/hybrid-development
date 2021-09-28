package com.zw.platform.repository.modules;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.zw.platform.domain.forwardplatform.ThirdPlatForm;
import com.zw.platform.domain.forwardplatform.ThirdPlatFormConfig;
import com.zw.platform.domain.forwardplatform.ThirdPlatFormConfigView;
import com.zw.platform.domain.forwardplatform.ThirdPlatFormSubscribe;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormConfigQuery;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormQuery;

/**
 * Created by LiaoYuecai on 2017/3/3.
 */
public interface ThirdPlatFormDao {
    public List<ThirdPlatForm> findList(IntercomPlatFormQuery query);

    public boolean add(ThirdPlatForm form);

    public boolean deleteByIds(List<String> id);

    public boolean update(ThirdPlatForm form);

    public ThirdPlatForm findById(String id);
    
    /**
     * 批量查询平台信息
     * @author hujun
     * @Date 创建时间：2018年3月9日 上午10:40:33
     * @param ids
     * @return
     */
    public List<ThirdPlatForm> findByIds(List<String> ids);

    public List<ThirdPlatFormConfigView> findConfigViewList(IntercomPlatFormConfigQuery query);

    public List<ThirdPlatFormConfigView> findConfigViewListByVehicleId(String vehicleId);

    public ThirdPlatFormConfigView findConfigViewByConfigId(String configId);
    
    /**
     * 批量查询绑定关系
     * @author hujun
     * @Date 创建时间：2018年3月7日 下午5:52:34
     * @param configIds
     * @return
     */
    public List<ThirdPlatFormConfigView> findConfigViewByConfigIds(List<String> configIds);

    public Integer findConfigViewListByIds(String vehicleId, String platFormId);

    public List<String> findConFigUuidByPIds(@Param("pids") List<String> pids);

    public void addConfig(ThirdPlatFormConfig config);
    
    /**
     * 批量新增绑定关系
     * @author hujun
     * @Date 创建时间：2018年3月6日 下午4:57:15
     * @param list
     */
    public void addConfigByBatch(List<ThirdPlatFormConfig> list);

    public void updateConfigById(ThirdPlatFormConfig config);

    public boolean deleteConfigById(List<String> ids);

    public ThirdPlatFormConfig findConfigById(String id);

    List<String> findDeviceNumberByFormId(String id);

    public List<String> findConFigIdByVIds(@Param("vehicleIds") List<String> vehicleIds);

    public List<String> findDeviceNumberByVIds(@Param("vehicleIds") List<String> vehicleIds);
    
    /**
     * 查询指定平台下绑定的所有车辆id
     * @author hujun
     * @Date 创建时间：2018年3月5日 下午6:09:32
     * @return
     */
    public List<String> findVehiclesOfPlatform(String platFormId);
    
    /**
     * 根据绑定关系id查询绑定关系信息
     * @author hujun
     * @Date 创建时间：2018年3月8日 下午5:43:17
     * @param platFormIds
     * @return
     */
    public List<ThirdPlatFormSubscribe> findConfigByConfigUuid(List<String> platFormIds);
    
    /**
     * 校验808平台名称是否唯一
     * @author hujun
     * @Date 创建时间：2018年3月12日 下午3:36:02
     * @param platFormName
     * @return
     */
    String check808PlatFormSole(String platFormName);
    
    /**
     * 根据车辆绑定关系id查找车辆转发绑定关系id
     * @author hujun
     * @Date 创建时间：2018年3月16日 下午4:40:55
     * @param vcids
     * @return
     */
    List<String> findConfigIdByVconfigIds(List<String> vcids);

    /**
     * 根据车辆id查询其sim卡号
     * @param vehicleIds
     * @return
     */
    List<String> findSimCardByVids(@Param("vehicleIds") List<String> vehicleIds);
}
