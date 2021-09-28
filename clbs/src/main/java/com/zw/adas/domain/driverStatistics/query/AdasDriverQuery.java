package com.zw.adas.domain.driverStatistics.query;

import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.DateUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 司机统计查询类
 * @author zhengjc
 * @version 1.0
 * @since 2019/7/10 10:31
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class AdasDriverQuery extends BaseQueryBean {
    static final Pattern cardNumAndNamPattern = Pattern.compile("[a-zA-Z0-9\\-_\\u4e00-\\u9fa5]+");
    private String id;
    /**
     * 插卡的起始时间
     */
    private String startTime;
    /**
     * 插卡的结束时间
     */
    private String endTime;

    /**
     * 从业人员资格证号，按照逗号隔开
     */
    private String cardNumbers;

    /**
     * 车辆id字符串按照逗号隔开
     */
    private String vehicleIds;

    /**
     * 从业资格证号的集合
     */
    private Set<String> cardNumberSet;

    /**
     * 插卡时间的毫秒值
     */
    private long startTimeVal;
    /**
     * 拔卡时间的毫秒值
     */
    private long endTimeVal;

    public void initParam() {
        initQueryTime();
        initCarNumberSet();
    }

    public void initParam(Set<String> carNumbers) {
        cardNumberSet = new HashSet<>();
        for (String card : carNumbers) {
            if (!cardNumAndNamPattern.matcher(card).matches()) {
                continue;
            }
            cardNumberSet.add(StrUtil.getFixedLenStr(card, 60, "#"));
        }
        initQueryTime();
    }

    private void initCarNumberSet() {
        String[] cardNumberList = cardNumbers.split(",");
        cardNumberSet = new HashSet<>();
        for (String cn : cardNumberList) {
            if (!cardNumAndNamPattern.matcher(cn).matches()) {
                continue;
            }
            cardNumberSet.add(StrUtil.getFixedLenStr(cn, 60, "#"));
        }
    }

    private void initQueryTime() {
        if (StrUtil.areNotBlank(startTime, endTime)) {
            startTimeVal = DateUtil.getStringToLong(startTime, null);
            endTimeVal = DateUtil.getStringToLong(endTime, null);
        }
    }

    public static AdasDriverQuery getAdasDriverQuery(String cardNumber, LocalDateTime insertCardDateTime) {
        long endTimeVal = Date8Utils.getLongTime(insertCardDateTime);
        long startTimeVal =
            Date8Utils.getLongTime(LocalDateTime.of(insertCardDateTime.toLocalDate().minusDays(1), LocalTime.MIN));

        AdasDriverQuery adasDriverQuery = new AdasDriverQuery();
        adasDriverQuery.setCardNumbers(StrUtil.getFixedLenStr(cardNumber, 60, "#"));
        adasDriverQuery.setStartTimeVal(startTimeVal);
        adasDriverQuery.setEndTimeVal(endTimeVal);
        return adasDriverQuery;
    }
}
