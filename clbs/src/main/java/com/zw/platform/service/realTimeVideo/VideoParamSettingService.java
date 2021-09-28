package com.zw.platform.service.realTimeVideo;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.domain.realTimeVideo.VideoSetting;
import com.zw.platform.domain.vas.alram.query.AlarmSettingQuery;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.vo.realTimeVideo.VideoParamVo;

import java.util.List;
import java.util.Map;


/**
* @author 作者 E-mail:yangya
* @version 创建时间：2017年12月26日 上午11:33:54
* 类说明:
*/
public interface VideoParamSettingService {
    /**
     * 根据条件分页查询视频参数列表
     * @param query
     * @return
     * @throws Exception
     */
    Page<Map<String, Object>> findVideoSetting(AlarmSettingQuery query);

    /**
     * 查询所有用户权限内设置了视频参数的车辆id和车牌号集合
     * @param vehicleId  车辆id
     * @param deviceType 通讯类型
     * @throws Exception Exception
     * @return List<JSONObject>
     */
    List<JSONObject> getAllReferVehicle(String vehicleId, String deviceType) throws Exception;

    /**
     *根据车辆id查询视频参数
     * @param vehicleId
     */
    Map<String, Object> getVideoParam(String vehicleId);

    /**
     * 添加音视频参数
     * @param videoParam
     */
    void saveAllParam(VideoParamVo videoParam);

    /**
     * 下发
     * @param vehicleId
     * @throws Exception
     */
    void sendVideoSetting(String vehicleId);

    /**
     * 恢复默认
     * @param vehicleId
     * @throws Exception
     */
    void delete(String vehicleId) throws Exception;

    /**
     * 批量恢复默认
     * @param vehicleIds
     * @throws Exception
     */
    void deleteMore(String vehicleIds);

    /***下面是测试代码，上线删除***/

    /**
     * 根据车辆id和通道号查询视频参数
     * @return
     * @throws Exception
     */
    JsonResultBean getVideoParam(String vehicleId, Integer logicChannel) throws Exception;

    /**
     * 添加或修改视频参数
     * @return
     * @throws Exception
     */
    void saveVideoParam(VideoSetting videoSetting);

    /**
     * 根据车辆id单独获取休眠唤醒参数设置值
     * @author hujun
     * @Date 创建时间：2018年2月10日 下午4:57:37
     * @param vehicleId
     * @return
     */
    JsonResultBean getVideoSleep(String vehicleId) throws Exception;

    /**
     * 根据终端型号默认音视频通道参数
     * @param terminalTypeId （终端类型id）
     * @param monitorId （监控对象id）
     */
    void addVideoChannelParam(String terminalTypeId, String monitorId);

    /**
     * 批量设置音视频通道参数
     */
    void addBatchVideoChannelParam(Map<String, String> vehicleBindChannelNum);
}
