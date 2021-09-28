package com.zw.platform.service.realTimeVideo;


import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.vo.realTimeVideo.VideoAlarmParam;


/**
* @author 作者 E-mail:yangya
* @version 创建时间：2017年12月28日 下午4:59:11
* 类说明:
*/
public interface VideoAlarmParamService {
    /**
     * 根据车辆id获取视频报警参数
     * @param vehicleId
     * @return
     * @throws Exception
     */
    JsonResultBean getAlarmParam(String vehicleId) throws Exception;

    /**
     * 添加或者修改视频报警参数
     * @throws Exception
     */
    void saveVideoAlarmParam(VideoAlarmParam videoAlarmParam) throws Exception;

}
