package com.zw.platform.service.driverDiscernManage;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.driverDiscernManage.DriverDiscernManageIssueParam;
import com.zw.platform.domain.basicinfo.query.DriverDiscernManageQuery;
import com.zw.platform.dto.driverMiscern.DeviceDriverDto;
import com.zw.platform.dto.driverMiscern.DriverDiscernManageDto;
import com.zw.protocol.msg.Message;

import java.util.List;

/**
 * @Description: 驾驶员识别管理Service
 * @Author Tianzhangxu
 * @Date 2020/9/27 10:44
 */
public interface DriverDiscernManageService {

    /**
     * 分页查询驾驶员识别管理信息
     * @param query query
     * @return page
     */
    Page<DriverDiscernManageDto> list(DriverDiscernManageQuery query) throws Exception;

    /**
     * 根据车辆ID，查询对应驾驶员列表
     * @param vehicleId vehicleId
     * @return List<DeviceDriverDto>
     */
    List<DeviceDriverDto> listDriverDetail(String vehicleId);

    /**
     * 批量下发查询指令
     * @param vehicleIds vehicleIds
     */
    void sendQueryBatch(List<String> vehicleIds, String sessionId);

    /**
     * 查询指令应答处理（0x0E12）
     * @param message message
     */
    void sendQueryAckHandle(Message message);

    /**
     * 批量下发指令下发
     * @param  param
     */
    void sendIssueBatch(DriverDiscernManageIssueParam param, String sessionId);

    /**
     * 下发指令应答处理（0x0001通用应答回复）
     * @param message message
     * @param msgSn
     */
    void sendIssueAckHandle(Message message, Integer msgSn);
}
