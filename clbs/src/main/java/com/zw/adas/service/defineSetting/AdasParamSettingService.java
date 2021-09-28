package com.zw.adas.service.defineSetting;

import com.github.pagehelper.Page;
import com.zw.adas.domain.define.setting.AdasJingParamSetting;
import com.zw.adas.domain.define.setting.AdasPlatformParamSetting;
import com.zw.adas.domain.define.setting.AdasSettingListDo;
import com.zw.adas.domain.define.setting.dto.AdasParamRequestDTO;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import com.zw.adas.domain.define.setting.query.AdasRiskParamQuery;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.ws.entity.t808.parameter.ParamItem;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/***
 @Author gfw
 @Date 2019/6/10 10:55
 @Description adas参数设置 (川标 /冀标)
 @version 1.0
 **/
public interface AdasParamSettingService extends IpAddressService {

    /**
     * 主动安全参数设置以及下发
     * @param requestDTO
     * @return
     * @throws Exception
     */
    boolean addAndSendParam(AdasParamRequestDTO requestDTO) throws BusinessException;

    /**
     * 条件查询
     * @param adasRiskParamQuery
     * @return
     * @throws Exception
     */
    Page<AdasSettingListDo> selectParamByCondition(AdasRiskParamQuery adasRiskParamQuery) throws Exception;

    /**
     * 获取参考对象
     * @param protocol 协议类型
     * @return
     */
    List<Map<String, Object>> findReferVehicle(Integer protocol);

    /**
     * 获取当前监控对象的参数设置信息
     * @param vid
     * @return
     */
    List<AdasParamSettingForm> findParamByVehicleId(String vid);

    /**
     * 根据车辆id和参数进行对应修改
     * @param requestDTO
     * @return
     */
    void updateParamByVehicleId(AdasParamRequestDTO requestDTO) throws BusinessException;

    /**
     * 删除参数设置
     * @param vehicleIds
     * @param ipAddress
     * @param s
     */
    void deleteRiskVehicleIds(List<String> vehicleIds, String ipAddress, String s) throws Exception;

    /**
     * 参数
     * @param vehIds
     * @param ipAddress
     */
    void sendParamSet(List<String> vehIds, String ipAddress, LinkedBlockingQueue<Map<String, String>> paramStatusQueue,
        Integer protocol);

    /**
     * 下发参数（前向，驾驶员异常，激烈驾驶）
     * @param vehicleId
     * @param adasParamSettingForms
     */
    void sendParam(String vehicleId, List<AdasParamSettingForm> adasParamSettingForms, String userName);

    JsonResultBean sendF3PInfo(String vehicleId, String sensorID, String commandType, String ipAddress);

    Map<String, Integer> getStatus(String vehicleId, Integer protocolType, String paramType);

    List<AdasPlatformParamSetting> findPlatformParamByVehicleId(String vehicleId);

    List<Map<String, String>> findAllTireModel();

    void processingThreads(LinkedBlockingQueue<Map<String, String>> queue, Integer protocol);

    void updateDirectiveStatus(Set<String> directiveIdSet);

    Set<String> findLogicChannelsByVehicleId(List<String> vehicleIds);

    void maintenanceRemoteUpgradeCache(String vehicleId);

    List<AdasJingParamSetting> findJingParamByVehicleId(String vehicleId);

    //***************京标分割线***************************

    Map<String, Map<Integer, List<ParamItem>>> insertJingParamSetting(List<String> vehicleIds,
        List<AdasJingParamSetting> adasParamSettingForm, List<AdasPlatformParamSetting> platformParamSettings,
        boolean sendFlag, String ipAddress);

    /**
     * 京标下发参数设置
     * @param vehIds
     * @param ipAddress
     */
    void sendJingParamSet(List<String> vehIds, String ipAddress);

    JsonResultBean sendPInfo(String vid, Integer type);

    void sendJing8103(List<String> vehicleIds, Map<String, Map<Integer, List<ParamItem>>> sendParamMap);
}
