package com.zw.adas.repository.mysql.riskdisposerecord;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.AdasCommonParamSetting;
import com.zw.adas.domain.define.setting.AdasSettingListDo;
import com.zw.platform.basic.domain.BaseKvDo;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/***
 @Author gfw
 @Date 2019/6/10 9:58
 @Description 通用参数表操作
 @version 1.0
 **/
public interface AdasCommonParamSettingDao {

    /**
     * 新增通用参数设置 批量/单个
     * @param record
     * @return
     */
    boolean insertCommonParamBatch(List<AdasCommonParamSetting> record);

    /**
     * 参数设置列表查询
     * @param map
     * @return
     */
    List<AdasSettingListDo> selectParamList(@Param("params") Map<String, Object> map);

    /**
     * 查询vehicleId对应的参数
     * @param vehicleId
     * @return
     */
    List<AdasCommonParamSetting> selectByVehicleId(String vehicleId);

    /**
     * 查询参考对象
     * @param userId
     * @param protocol
     * @param groupList
     * @return
     */
    List<Map<String, Object>> findReferVehicle(@Param("tableName") String tableName, @Param("userId") String userId,
        @Param("protocol") Integer protocol, @Param("groupList") List<String> groupList);

    /**
     * 查询车id 是否已存在
     * @param vehicleId
     * @return
     */
    List<String> findVehicleExit(String vehicleId);

    /**
     * 根据id批量修改
     * @param commonParamSetting
     */
    void updateCommonParamById(@Param("adascom") AdasCommonParamSetting commonParamSetting, @Param("vid") String vid);

    /**
     * 批量删除
     * @param vehicleIds
     */
    void deleteCommonByBatch(@Param("vehicleIds") List<String> vehicleIds);

    /**
     * 查询需要的vid
     * @param paName
     * @param status
     * @return
     */
    List<String> selectDirect(@Param("pName") String paName, @Param("status") Integer status,
        @Param("ptype") String ptype);

    /**
     * 查询需要的vid
     * @param paName
     * @param status
     * @param ptype
     * @return
     */
    List<String> selectDirectByList(@Param("list") List<String> paName, @Param("status") Integer status,
        @Param("ptype") String ptype);

    /**
     * 获取下发过协议的车id
     * @param protocol
     * @return
     */
    List<String> selectDirectVid(@Param("protocol") String protocol);

    /**
     * 状态列表
     * @param list
     * @return
     */
    @MapKey("keyName")
    Map<String, BaseKvDo<String, Integer>> selectDirectStatus(@Param("vehicleIds") List<String> list,
        @Param("protocol") String protocol);

    /**
     * 根据车id获取车辆所属的协议
     * @param vid
     * @return
     */
    Integer selectProtocolByVid(String vid);

    /**
     * 根据协议类型批量删除下发状态
     * @param protocol
     * @param vehicleIds
     */
    void updateDirectiveByVidAndProtocol(@Param("protocol") String protocol,
        @Param("vehicleIds") List<String> vehicleIds);

    /**
     * 是否存在
     * @param adasParamSettingForm
     * @return
     */
    String findadasParam(AdasCommonParamSetting adasParamSettingForm);

    /**
     * 查询默认通用参数
     * @param start
     * @return
     */
    List<AdasCommonParamSetting> findDefaultCom(Integer start);

    /**
     * 查询通用报警参数
     * @param paramType
     * @param protocol
     * @return
     */
    List<AdasAlarmParamSetting> findDefaultAlarm(@Param("paramType") Integer paramType,
        @Param("protocol") Integer protocol);

    /**
     * 批量删除公共参数指定页签
     * @param vehicleIds
     */
    void deleteCommonByParamType(@Param("vehicleIds") List<String> vehicleIds,
        @Param("paramTypeSet") Set<Integer> paramTypeSet);

}
