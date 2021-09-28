package com.zw.platform.service.alarm;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.vas.alram.AlarmLinkageDTO;
import com.zw.platform.domain.vas.alram.AlarmParameter;
import com.zw.platform.domain.vas.alram.AlarmParameterDetailsDTO;
import com.zw.platform.domain.vas.alram.AlarmSetting;
import com.zw.platform.domain.vas.alram.AlarmSettingReferentDTO;
import com.zw.platform.domain.vas.alram.AlarmType;
import com.zw.platform.domain.vas.alram.form.AlarmParameterSettingForm;
import com.zw.platform.domain.vas.alram.query.AlarmSettingQuery;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description:报警参数设置Service
 * @author:wangying
 * @time:2016年12月6日 下午4:58:08
 */
public interface AlarmSettingService {

    /**
     * 查询流量传感器绑定（分页）
     * @param query
     * @return
     * @throws Exception
     */
    Page<AlarmSetting> findAlarmSetting(AlarmSettingQuery query);

    /**
     * 查询报警参数设置了的车辆
     * @return List<AlarmSetting>
     * @throws Exception
     * @throws
     * @Title: findVehicleAlarmSetting
     * @author Liubangquan
     */
    List<AlarmSetting> findVehicleAlarmSetting();

    /**
     * @param vehicleId
     * @return List<AlarmSetting>
     * @Description:根据车辆id查询
     * @exception:
     * @author: wangying
     * @time:2016年12月7日 下午3:19:33
     */
    List<AlarmSetting> findByVehicleId(String vehicleId);

    /**
     * @param vehicleId
     * @return List<AlarmSetting>
     * @throws Exception
     * @Description:根据车辆id，报警类型查询相应的报警参数
     * @exception:
     * @author: fanlu
     * @time:2017年3月31日 上午11：07
     */
    List<AlarmSetting> findAlarmSetting(String vehicleId, String alarm);

    /**
     * @return List<AlarmSetting>
     * @Description:查询所有参数设置
     * @exception:
     * @author: wangying
     * @time:2016年12月7日 下午3:37:42
     */
    List<AlarmSetting> findAllAlarmParameter();

    /**
     * 报警参数设置批量清空
     * @param vehicleIds 监控对象id
     * @return JsonResultBean
     */
    JsonResultBean deleteByVehicleIds(List<String> vehicleIds);

    JsonResultBean sendAlarm(ArrayList<JSONObject> paramList);

    /**
     * @param ids
     * @param checkedParams
     * @param deviceType
     * @throws Exception
     * @Description: 批量设置报警参数设置
     * @exception:
     * @author: wangying
     * @time:2016年12月19日 下午4:23:04
     */
    JsonResultBean updateAlarmParameterByBatch(String ids, String checkedParams, String deviceType) throws Exception;

    /**
     * @param vehicleId
     * @return
     * @throws Exception
     * @Description: 根据车辆id查询参考车牌报警参数
     */
    List<AlarmParameterSettingForm> findParameterByVehicleId(String vehicleId);

    /**
     * @param vehicleId
     * @param type
     * @return boolean
     * @throws Exception
     * @Description:根据车辆id删除车辆与报警参数设置的绑定
     * @exception:
     * @author: wangying
     * @time:2017年2月6日 上午11:26:18
     */
    boolean deleteAlarmSettingByVid(String vehicleId, Integer type);

    List<AlarmSetting> findReferVehicleByDeviceType(String deviceType);

    /**
     * 获取报警参数设置参考对象列表
     * @param deviceType -1:交通部JT/T808-2013; 11:交通部JT/T808-2019; 5:BDTD-SM; 9:ASO; 10:F3超长待机;
     * @return List<AlarmSettingReferentDTO>
     */
    List<AlarmSettingReferentDTO> getReferentList(String deviceType);

    /**
     * 获取报警参数设置详情
     * @param moId 监控对象id
     * @return AlarmParameterDetailsDTO
     */
    AlarmParameterDetailsDTO getAlarmParameterSettingDetails(String moId);

    /**
     * 保存报警参数设置
     * @param moIds                         监控对象id
     * @param alarmParameterSettingJsonStr 报警参数设置json字符串
     * @return JsonResultBean
     */
    JsonResultBean saveAlarmParameterSetting(String moIds, String alarmParameterSettingJsonStr);

    ModelAndView find808Object(String id, ModelAndView mav, String deviceType) throws Exception;

    ModelAndView findBdObject(String id, ModelAndView mav) throws Exception;

    ModelAndView findAsoObject(String id, ModelAndView mav) throws Exception;

    ModelAndView findF3Object(String id, ModelAndView mav) throws Exception;

    VehicleInfo findPeopleById(String id);

    VehicleInfo findPeopleOrVehicleOrThingById(String id);

    List<AlarmSetting> findById(String id) throws Exception;

    List<AlarmType> getAlarmType(Collection<String> vechileIds, int deviceType);

    /**
     * 获取联动策略设置
     * @param vehicleId 监控对象ID
     * @return list
     */
    List<AlarmLinkageDTO> getLinkageSettingList(String vehicleId);

    List<VehicleInfo> findReferPhotoVehicles(List<String> vehicleIds, Integer deviceType);

    JsonResultBean saveLinkageSetting(String linkageParam, String monitorIds);

    /**
     * 更新redis报警类型信息(更改了name等信息的时,在test/list中调用,更新信息.新增报警信息后,启动项目时会自动更新)
     */
    JsonResultBean updateAlarmType();

    /**
     * 获取IO报警状态
     * @param vehicleId
     * @param alarmTypeId
     * @param value
     * @return
     */
    JsonResultBean getIOAlarmStateTxt(String vehicleId, String alarmTypeId, String value);

    /**
     * 恢复默认
     * @param deviceType deviceType
     * @return
     */
    JsonResultBean resetDefaultAlarm(String deviceType);

    /**
     * 获取终端设置的报警
     * @param monitorId  monitorId
     * @param deviceType
     */
    JsonResultBean sendDeviceAlarmParam(String monitorId, String deviceType);

    /**
     * 批量删除联动策略
     * @param needRemoveMonitors needRemoveMonitors
     * @return 车辆ID和报警类型集合
     */
    Map<String, Set<String>> deleteBatchLinkageSetting(Set<String> needRemoveMonitors);

    JsonResultBean sendParameter(String vehicleId, String paramIdStr);

    JsonResultBean resetDefaultHighPrecisionAlarm(String deviceType);

    boolean deleteRoadAlarmSpeedLimit();

    /**
     * 根据id获取报警参数
     * @param alarmParameterIds alarmParameterIds
     * @return List<AlarmParameter>
     */
    List<AlarmParameter> getAlarmParameterByAlarmParameterIds(
        @Param("alarmParameterIds") Collection<String> alarmParameterIds);
}
