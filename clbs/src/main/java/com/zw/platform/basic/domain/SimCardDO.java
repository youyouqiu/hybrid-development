package com.zw.platform.basic.domain;

import com.zw.platform.basic.dto.SimCardDTO;
import com.zw.platform.basic.dto.imports.SimCardImportDTO;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.util.BSJFakeIPUtil;
import com.zw.platform.util.StrUtil;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Map;

/**
 * @Author: zjc
 * @Description:只会和数据库字段一一对应
 * @Date: create in 2020/11/4 16:02
 */
@Data
public class SimCardDO extends BaseDO {
    /**
     * 终端手机号
     */
    private String simcardNumber;
    /**
     * 启停状态0:停用,1:启用
     */
    private Integer isStart;
    /**
     * 运营商
     */
    private String operator;
    /**
     * 激活日期
     */
    private Date openCardTime;
    /**
     * 容量
     */
    private String capacity;
    /**
     * 网络类型
     */
    private String networkType;

    /**
     * 套餐流量(M)
     */
    private String simFlow;
    /**
     * 已用流量
     */
    private String useFlow;

    /**
     * 月预警流量(M)
     */
    private String alertsFlow;
    /**
     * 到期时间yyyy-MM-dd
     */
    private Date endTime;
    /**
     * ICCID
     */
    private String iccid;

    /**
     * IMSI
     */
    private String imsi;
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
     * 修正系数
     */
    private String correctionCoefficient;

    /**
     * 预警系数
     */
    private String forewarningCoefficient;

    /**
     * IMEI
     */
    private String imei;

    /**
     * 当月流量(M)
     */
    private String monthRealValue;
    /**
     * 当日流量(M)
     */
    private String dayRealValue;

    /**
     * 流量月结日
     */
    private String monthlyStatement;
    /**
     * 流量最后更新时间
     */
    private String monthTrafficDeadline;
    /**
     * 伪IP
     */
    private String fakeIP;

    /**
     * 真实SIM卡号
     */
    private String realId;

    /**
     * 备注信息
     */
    private String remark;
    /**
     * 发放地市（1120改动）
     */
    private String placementCity;

    /**
     * 终端手机号的所属企业id
     */
    private String orgId;

    public static SimCardDO getAddInstance(SimCardDTO simCardDTO, String currentUsername) {
        SimCardDO simCardDO = new SimCardDO();
        simCardDTO.initAdd(currentUsername);
        BeanUtils.copyProperties(simCardDTO, simCardDO);
        return simCardDO;
    }

    /**
     * 修改实例转换
     * @param simCard
     * @param userName
     * @return
     */
    public static SimCardDO getUpdateInstance(SimCardDTO simCard, String userName) {
        SimCardDO simCardDO = new SimCardDO();
        //初始化相关参数
        simCard.initUpdate(userName);
        BeanUtils.copyProperties(simCard, simCardDO);
        return simCardDO;
    }

    public static SimCardDO getImportData(SimCardImportDTO data, Map<String, String> orgMap, Integer isStart) {
        SimCardDO simCardDO = new SimCardDO();
        simCardDO.orgId = orgMap.get(data.getOrgName());
        simCardDO.isStart = isStart;
        simCardDO.fakeIP = BSJFakeIPUtil.integerMobileIPAddress(simCardDO.simcardNumber);
        simCardDO.monthlyStatement = "01";
        BeanUtils.copyProperties(data, simCardDO);
        simCardDO.setCreateDataTime(new Date());
        simCardDO.setCreateDataUsername(SystemHelper.getCurrentUsername());
        simCardDO.simcardNumber = data.getSimCardNumber();
        simCardDO.alertsFlow = calculateAlertsFlow(data);
        return simCardDO;
    }

    private static String calculateAlertsFlow(SimCardImportDTO simCard) {
        String one = simCard.getForewarningCoefficient();
        String two = simCard.getMonthThresholdValue();
        if (StrUtil.areNotBlank(one, two)) {
            BigDecimal v1 = new BigDecimal(one);
            BigDecimal v2 = new BigDecimal(two);
            return v1.divide(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP).multiply(v2).toString();
        }
        return null;

    }
}
