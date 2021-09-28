package com.zw.platform.service.thirdplatform;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.forwardplatform.ThirdPlatForm;
import com.zw.platform.domain.forwardplatform.ThirdPlatFormConfig;
import com.zw.platform.domain.forwardplatform.ThirdPlatFormConfigView;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormConfigQuery;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormQuery;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;

/**
 * Created by LiaoYuecai on 2017/3/3.
 */
public interface ThirdPlatFormService {
    public List<ThirdPlatForm> findList(IntercomPlatFormQuery query, boolean doPage) throws Exception;

    /**
     * 增加转发平台
     * @param form
     * @throws Exception
     */
    public JsonResultBean add(ThirdPlatForm form, String ipAddress) throws Exception;

    /**
     * 删除转发平台(包括批量删除)
     * @param ids
     * @param ipAddress
     * @return
     * @throws Exception
     */
    public JsonResultBean deleteById(List<String> ids, String ipAddress) throws Exception;

    /**
     * 修改转发平台信息
     * @param form
     * @param ipAddress
     */
    public JsonResultBean update(ThirdPlatForm form, String ipAddress) throws Exception;

    @Deprecated
    public List<VehicleInfo> getVehicleList() throws Exception;

    public void updateConfigById(ThirdPlatFormConfig config);

    public List<String> findConFigUuidByPIds(List<String> pids);

    public ThirdPlatForm findById(String id) throws Exception;

    Page<ThirdPlatFormConfigView> findConfigViewList(IntercomPlatFormConfigQuery query);

    public List<ThirdPlatFormConfigView> findConfigViewListByVehicleId(String vehicleId);

    public void addConfig(String thirdPlatformId, String vehicleIds, String ipAddress) throws Exception;

    /**
     * 解除监控对象转发
     * @param ids
     * @param ipAddress
     * @return
     * @throws Exception
     */
    public JsonResultBean deleteConfigById(List<String> ids, String ipAddress) throws Exception;

    public ThirdPlatFormConfig findConfigById(String id);

    public ThirdPlatFormConfigView findConfigViewByConfigId(String configId);

    /**
     * 批量查询绑定关系
     * @param configIds
     * @return
     * @author hujun
     * @Date 创建时间：2018年3月7日 下午5:52:34
     */
    public List<ThirdPlatFormConfigView> findConfigViewByConfigIds(List<String> configIds) throws Exception;

    public Integer findConfigViewListByIds(String vehicleId, String platFormId);

    public List<String> findConFigIdByVIds(List<String> vehicleIds);

    public List<String> findDeviceNumberByVIds(List<String> vehicleIds) throws Exception;

    /**
     * 根据车辆绑定关系id查找车辆转发绑定关系id
     * @param vcids
     * @return
     * @author hujun
     * @Date 创建时间：2018年3月16日 下午4:40:18
     */
    List<String> findConfigIdByVconfigIds(List<String> vcids);

    /**
     * 校验808平台名称是否唯一
     * @param platFormName
     * @return
     * @author hujun
     * @Date 创建时间：2018年3月12日 下午3:36:02
     */
    boolean check808PlatFormSole(String platFormName, String pid) throws Exception;

    List<String> findSimCardByVids(List<String> vehicleIds);
}
