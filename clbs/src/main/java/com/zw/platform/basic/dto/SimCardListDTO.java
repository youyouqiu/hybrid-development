package com.zw.platform.basic.dto;

import com.zw.platform.basic.domain.BaseKvDo;
import com.zw.platform.basic.domain.BaseKvtDo;
import com.zw.platform.basic.domain.SimCardListDO;
import com.zw.platform.util.common.DateUtil;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.Map;

/**
 * @author: zjc
 * @Description:sim卡列表信息do,需要和数据库一一对应
 * @Date: create in 2020/11/6 10:40
 */
@Data
public class SimCardListDTO {
    /**
     * id
     */
    private String id;

    /**
     * 下发状态
     */
    private Integer sendStatus;

    /**
     * 下发参数id
     */
    private Integer sendParamId;

    /**
     * ICCID
     */
    private String iccid;

    /**
     * IMEI
     */
    private String imei;

    /**
     * IMSI
     */
    private String imsi;
    /**
     * 终端手机号
     */
    private String simcardNumber;

    /**
     * 真实SIM卡号
     */
    private String realId;
    /**
     * 所属企业
     */
    private String groupName;

    /**
     * 启停状态0:停用,1:启用
     */
    private Integer isStart;

    /**
     * 运营商
     */
    private String operator;

    /**
     * 发放地市（1120改动）
     */
    private String placementCity;

    /**
     * 套餐流量(M)
     */
    private String simFlow;

    /**
     * 当日流量(M)
     */
    private String dayRealValue;

    /**
     * 当月流量(M)
     */
    private String monthRealValue;

    /**
     * 流量最后更新时间
     */
    private String monthTrafficDeadline;

    /**
     * 月预警流量(M)
     */
    private String alertsFlow;
    /**
     * 流量月结日
     */
    private String monthlyStatement;

    /**
     * 修正系数
     */
    private String correctionCoefficient;

    /**
     * 预警系数
     */
    private String forewarningCoefficient;

    /**
     * 小时流量阈值(M)
     */
    private String hourThresholdValue;

    /**
     * 日流量阈值(M)
     */
    private String dayThresholdValue;

    /**
     * 月流量阈值(M)
     */
    private String monthThresholdValue;

    /**
     * 激活日期
     */
    private Date openCardTime;

    /**
     * 到期时间yyyy-MM-dd
     */
    private String endTime;

    /**
     * 终端手机号
     */
    private String deviceNumber;

    /**
     * 监控对象名称
     */
    private String brand;

    /**
     * 用户前端进行参数下发
     */
    private String vehicleId;

    /**
     * 数据创建时间
     */

    private String createDataTime;

    /**
     * 数据修改时间
     */

    private String updateDataTime;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 信息配置id
     */
    private String configId;

    /**
     * 下发状态
     */
    private Integer pstatus;

    /**
     * 下发参数的id
     */
    private String paramId;

    public static SimCardListDTO buildList(SimCardListDO listDO, Map<String, BaseKvDo<String, String>> monitorIdNameMap,
        Map<String, String> orgMap, Map<String, BaseKvtDo<String, String, Integer>> sendStatusMap) {
        SimCardListDTO simCardList = new SimCardListDTO();
        BeanUtils.copyProperties(listDO, simCardList);
        BaseKvDo<String, String> kvDo = monitorIdNameMap.get(listDO.getMonitorId());
        if (kvDo != null) {
            simCardList.brand = kvDo.getFirstVal();
        }

        simCardList.groupName = orgMap.get(listDO.getOrgId());
        simCardList.vehicleId = listDO.getMonitorId();
        simCardList.updateDataTime = DateUtil.formatDate(listDO.getUpdateDataTime(), DateUtil.DATE_Y_M_D_FORMAT);
        simCardList.createDataTime = DateUtil.formatDate(listDO.getCreateDataTime(), DateUtil.DATE_Y_M_D_FORMAT);
        simCardList.endTime = DateUtil.formatDate(listDO.getCreateDataTime(), DateUtil.DATE_Y_M_D_FORMAT);
        BaseKvtDo<String, String, Integer> sendStatus = sendStatusMap.get(listDO.getId());
        if (sendStatus != null) {
            simCardList.paramId = sendStatus.getFirstValue();
            simCardList.pstatus = sendStatus.getSecondVal();
        }
        return simCardList;
    }

}
