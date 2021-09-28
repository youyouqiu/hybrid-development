package com.zw.adas.domain.driverStatistics.show;

import com.alibaba.fastjson.JSONObject;
import com.zw.adas.domain.driverStatistics.bean.AdasDriverStatisticsBean;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.PrecisionUtils;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/***
 @Author zhengjc
 @Date 2019/7/10 10:45
 @Description 司机统计展现实体
 @version 1.0
 **/
@Data
public class AdasDriverStatisticsShow {

    private String id;
    /**
     * 司机名称
     */

    private String driverName;

    /**
     * 监控对象
     */
    private String monitorName;

    /**
     * 所属企业
     */

    private String groupName;
    /**
     * 从业资格证号
     */
    private String cardNumber;
    /**
     * 插卡时间
     */
    private String insertCardTime;
    /**
     * 拔卡时间
     */
    private String removeCardTime;
    /**
     * 休息次数
     */
    private Integer restTimes;
    /**
     * 行驶时长
     */
    private String travelTime;

    /**
     * 行驶时长
     */
    private transient long travelTimeVal;

    /**
     * 行驶里程
     */
    private transient Double travelMile;

    /**
     * 行驶里程
     */
    private String travelMileStr = "0.0";

    /**
     * -- habse 查询的字段 ---
     */

    /**
     * 插卡时间毫秒值
     */
    private transient long insertCardTimeVal;

    /**
     * 车辆id
     */
    private transient String monitorId;
    /**
     * 企业id
     */
    private transient String groupId;
    /**
     * 行驶开始时间段
     */
    private transient String travelStartTime;

    /**
     * 行驶结束时间段
     */
    private transient String travelEndTime;

    /**
     * 行驶开始里程段
     */
    private transient String travelStartMile;
    /**
     * 行驶结束里程段
     */
    private transient String travelEndMile;

    /**
     * 插卡当前的零点里程
     */
    private transient double zeroTimeMile;

    /**
     * 休息的开始的时间段
     */

    private transient String restStartTime;
    /**
     * 休息的结束的时间段
     */
    private transient String restEndTime;
    /**
     * 拔卡时间值
     */
    private transient Long removeCardTimeVal;

    /**
     * 当天行驶时长
     */
    private transient long todayTravelTime;

    /**
     * 当天行驶里程
     */
    private transient double todayTravelMile;

    /**
     * 当天当次行驶里程
     */
    private transient double todayLastTravelMile;

    /**
     * 当天当次行驶时长
     */
    private transient long todayLastTravelTime;

    /**
     * 行驶和休息每一段详情信息
     */

    private transient List<AdasDriverStatisticsBean> adasDriverStaticsBeans = new ArrayList<>();
    /**
     * 行驶和休息的详情信息
     */
    private List<AdasDriverStatisticsDetailShow> details = new ArrayList<>();
    /**
     * 驾驶员信息
     */
    private AdasProfessionalShow professionalShow;

    /**
     * 车牌颜色
     */
    private String plateColor;

    public void assembleData(Map<String, BindDTO> configInfo, Map<String, AdasProfessionalShow> driverInfoMaps) {
        cardNumber = cardNumber.split("_")[0];
        insertCardTime = DateUtil.getLongToDateStr(insertCardTimeVal, null);
        removeCardTime = removeCardTimeVal == null ? "----" : DateUtil.getLongToDateStr(removeCardTimeVal, null);

        id = cardNumber + "_" + driverName + "_" + insertCardTime;
        calTravelTimeAndMile();
        calTravelRestTime();
        assembleDetails();

        if (driverInfoMaps != null) {
            professionalShow = driverInfoMaps.get(cardNumber + "_" + driverName);
            cardNumber = professionalShow.getCardNumber();
        }
        if (configInfo != null) {
            BindDTO bindDTO = configInfo.get(monitorId);
            groupName = bindDTO.getOrgName();
            monitorName = bindDTO.getName();
            plateColor = PlateColor.getNameOrBlankByCode(bindDTO.getPlateColor());

        }
    }

    private void assembleDetails() {
        //排序处理
        adasDriverStaticsBeans.sort(Comparator.comparing(AdasDriverStatisticsBean::getStartTime));
        //组装详情信息
        if (CollectionUtils.isNotEmpty(adasDriverStaticsBeans)) {
            AdasDriverStatisticsDetailShow detailShow = null;
            AdasDriverStatisticsBean bean;
            for (int i = 0, len = adasDriverStaticsBeans.size(); i < len; i++) {
                bean = adasDriverStaticsBeans.get(i);
                if (detailShow == null) {
                    detailShow = new AdasDriverStatisticsDetailShow();
                }

                if (bean.getMile() != null) {
                    //该次是行驶，且上一次也是行驶，立即添加上一次数据，并生成新数据
                    if (detailShow.getTravelMile() != null) {
                        details.add(detailShow);
                        detailShow = new AdasDriverStatisticsDetailShow();
                    }
                    detailShow.setTravelStartTime(DateUtil.getLongToDateStr(bean.getStartTime(), null));
                    detailShow.setTravelEndTime(DateUtil.getLongToDateStr(bean.getEndTime(), null));
                    detailShow.setTravelTime(DateUtil.formatTime(subTime(bean.getEndTime(), bean.getStartTime())));
                    detailShow.setTravelMile(bean.getMile());
                    //最后一条且是行驶的情况，就直接添加了
                    if (i == (len - 1)) {
                        details.add(detailShow);
                    }

                } else {
                    //代表休息，代表一条完整数据，进行清空
                    detailShow.setRestStartTime(DateUtil.getLongToDateStr(bean.getStartTime(), null));
                    detailShow.setRestEndTime(DateUtil.getLongToDateStr(bean.getEndTime(), null));
                    detailShow.setRestTime(DateUtil.formatTime(bean.getEndTime() - bean.getStartTime()));
                    details.add(detailShow);
                    detailShow = null;
                }

            }
        }
    }

    private void assembleGroupNameAndMonitorName(Map<String, JSONObject> configInfo) {

    }

    private void calTravelRestTime() {
        restTimes = 0;
        if (StrUtil.areNotBlank(restStartTime, restEndTime)) {
            String[] restStartTimeArr = restStartTime.split(",");
            String[] restEndTimeArr = restEndTime.split(",");
            long startTimeVal;
            long endTimeVal;
            for (int i = 0, len = restEndTimeArr.length; i < len; i++) {
                endTimeVal = Long.parseLong(restEndTimeArr[i]);
                startTimeVal = Long.parseLong(restStartTimeArr[i]);

                //如果停止的时长大于20分钟，则计为休息
                if (endTimeVal - startTimeVal > 1200000) {
                    //4.1.2新增详情信息
                    AdasDriverStatisticsBean adasDriverStatisticsBean = new AdasDriverStatisticsBean();
                    adasDriverStatisticsBean.setStartTime(startTimeVal);
                    adasDriverStatisticsBean.setEndTime(endTimeVal);
                    adasDriverStatisticsBean.setTime(DateUtil.formatTime((endTimeVal - startTimeVal)));
                    adasDriverStaticsBeans.add(adasDriverStatisticsBean);
                    ++restTimes;
                }
            }
        }
    }

    private void calTravelTimeAndMile() {
        travelTime = "";
        travelTimeVal = 0L;
        travelMile = 0.0;
        todayTravelMile = 0.0;
        boolean sameDay;
        if (StrUtil.areNotBlank(travelStartTime, travelEndTime, travelStartMile, travelEndMile)) {
            String[] travelStartTimeArr = travelStartTime.split(",");
            String[] travelEndTimeArr = travelEndTime.split(",");
            String[] startMileArr = travelStartMile.split(",");
            String[] endMileArr = travelEndMile.split(",");
            LocalDateTime startTime;
            LocalDateTime endTime;
            LocalDateTime today = LocalDateTime.now();
            double endMile;
            double startMile;
            //每一段行驶时长中间变量
            long travelTimeValTemp;
            //每一段行驶里程中间变量
            double travelMileValTemp;
            //报表数据里程、行驶时长、行驶里程、休息时长、当天有效的行驶时长计算
            for (int i = 0, len = travelEndTimeArr.length; i < len; i++) {
                startTime = Date8Utils.fromLongTime(travelStartTimeArr[i]);
                endTime = Date8Utils.fromLongTime(travelEndTimeArr[i]);
                //4.1.2新增详情信息
                AdasDriverStatisticsBean adasDriverStatisticsBean = new AdasDriverStatisticsBean();
                adasDriverStatisticsBean.setStartTime(Date8Utils.getLongTime(startTime));
                adasDriverStatisticsBean.setEndTime(Date8Utils.getLongTime(endTime));
                //计算单次行驶时长
                travelTimeValTemp = Date8Utils.getSubLongTime(endTime, startTime);
                travelTimeVal += travelTimeValTemp;
                //设置每一段驶时长
                adasDriverStatisticsBean.setTime(DateUtil.formatTime(travelTimeValTemp));
                //计算当天当次行驶时长
                if (i == len - 1) {
                    todayLastTravelTime = Date8Utils.getSubLongTime(endTime, startTime);

                }

                if (today.getDayOfMonth() == startTime.getDayOfMonth()) {
                    //计算当天行驶时长
                    sameDay = startTime.getDayOfMonth() == endTime.getDayOfMonth();
                    if (!sameDay) {
                        startTime = LocalDateTime.of(endTime.toLocalDate(), LocalTime.MIN);
                    }
                    todayTravelTime += Date8Utils.getSubLongTime(endTime, startTime);
                }

                endMile = Double.parseDouble(endMileArr[i]);
                startMile = Double.parseDouble(startMileArr[i]);
                //计算单次行驶里程
                travelMileValTemp = subMile(endMile, startMile);
                travelMile += travelMileValTemp;
                //设置每一段行驶里程
                adasDriverStatisticsBean.setMile(PrecisionUtils.roundByScale(travelMileValTemp, 1));
                //新增到详情表中
                adasDriverStaticsBeans.add(adasDriverStatisticsBean);
                //计算当天当次行驶里程
                if (i == len - 1) {
                    todayLastTravelMile = subMile(endMile, startMile);
                }
                if (today.getDayOfMonth() == startTime.getDayOfMonth()) {
                    sameDay = startTime.getDayOfMonth() == endTime.getDayOfMonth();
                    //计算当天行驶里程
                    if (!sameDay) {
                        todayTravelMile += subMile(endMile, zeroTimeMile);
                    } else {
                        todayTravelMile += subMile(endMile, startMile);
                    }

                }

            }
            travelTime = DateUtil.formatTime(travelTimeVal);

            travelMileStr = PrecisionUtils.roundByScale(travelMile, 1);

        }
    }

    private static double subMile(double endMile, double startMile) {
        //进行异常数据过滤，当结束里程大于开始里程的时候，设置为0
        double result = endMile - startMile;
        return result > 0 ? result : 0.0;
    }

    private static byte[] subByte(byte[] data, int offset, int length) {
        byte[] re = new byte[length];
        System.arraycopy(data, offset, re, 0, length);
        return re;
    }

    private static long subTime(long endTime, long startTime) {
        long result = endTime - startTime;
        return result > 0 ? result : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AdasDriverStatisticsShow that = (AdasDriverStatisticsShow) o;
        return insertCardTimeVal == that.insertCardTimeVal && cardNumber.equals(that.cardNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardNumber, insertCardTimeVal);
    }

}
