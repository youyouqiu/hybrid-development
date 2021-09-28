package com.zw.platform.util;

import com.alibaba.fastjson.JSON;
import com.google.common.primitives.Ints;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.reportManagement.PositionalDetail;
import com.zw.platform.domain.reportManagement.TerminalPositional;
import com.zw.platform.util.common.Date8Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;


/**
 * 常用工具类
 *
 * @author hujun
 * 创建日期： 2019/2/24 15:18
 */
public class CommonUtil {
    private static final Logger log = LogManager.getLogger(CommonUtil.class);

    /**
     * 按照指定参数升序排序（目前只支持Long类型）
     *
     * @param paramName (排序参数名称，首字母必须大写)
     */
    public static <T> void sortPositionalDetailList(List<T> datas, String paramName) {

        datas.sort((o1, o2) -> {
            Class<?> c1 = o1.getClass();
            Class<?> c2 = o2.getClass();
            try {
                Long param1 = (Long) (c1.getMethod("get" + paramName).invoke(o1));
                Long param2 = (Long) (c2.getMethod("get" + paramName).invoke(o2));
                if (param1.equals(param2)) {
                    return 0;
                } else if (param1 > param2) {
                    return 1;
                } else {
                    return -1;
                }
            } catch (Exception e) { //参数值名称错误，没有对应get方法
                return 0;
            }
        });
    }

    /**
     * Returns a capacity that is sufficient to keep the map from being resized as
     * long as it grows no larger than expectedSize and the load factor is >= its
     * default (0.75).
     */
    public static int ofMapCapacity(int expectedSize) {
        if (expectedSize < 3) {
            checkArgument(expectedSize >= 0);
            return expectedSize + 1;
        }
        if (expectedSize < Ints.MAX_POWER_OF_TWO) {
            return expectedSize + expectedSize / 3;
        }
        return Integer.MAX_VALUE; // any large value
    }

    public static String getRiskVehicleKey(String vehicleId, String riskId) {
        return "RISK_" + vehicleId + "_" + riskId;
    }

    public static String getAdasSetting(String vehicleId, String riskId, Map<String, String> data) {
        return "#set#" + "RISK_" + vehicleId + "_" + riskId + "#" + JSON.toJSONString(data);
    }



    /**
     * 过滤行驶、停止异常数据
     *
     * @param positionalList positionalList
     */
    public static void getFilterPositionalDetail(List<PositionalDetail> positionalList) {
        // 里程、经纬度数据异常下标
        List<Integer> mileLocationNums = new ArrayList<>();
        // 油耗数据异常下标
        List<Integer> oilWearNums = new ArrayList<>();
        List<Integer> sensorMileLocationNums = new ArrayList<>(); //传感器里程、经纬度数据异常下标

        // 最近一条正常的里程、经纬度数据下标
        Integer normalMileLocationNum = null;
        // 最近一条正常的油耗数据下标
        Integer normalOilWearNum = null;
        Integer normalSensorMileLocationNum = null; //最近一条正常的传感器里程、经纬度数据下标

        /* 2.循环过滤替换异常数据 */
        for (int i = 0; i < positionalList.size(); i++) {
            PositionalDetail positionalDetail = positionalList.get(i);
            String longtitude = positionalDetail.getLongtitude(); //经度
            String latitude = positionalDetail.getLatitude(); //纬度
            String gpsMile = positionalDetail.getGpsMile(); //gps里程
            String sensorMile = positionalDetail.getMileageTotal(); //传感器里程
            String oilWear = positionalDetail.getTotalOilwearOne(); //油耗1

            /* 3.过滤替换里程、经纬度数据 */
            if (Objects.equals(gpsMile, "0") || Objects.equals(gpsMile, "0.0") || StringUtils.isBlank(longtitude)
                || StringUtils.isBlank(latitude) || Objects.equals(longtitude, "0") || Objects.equals(latitude, "0")
                || Objects.equals(longtitude, "0.0") || Objects.equals(latitude, "0.0")) {
                //记录异常的里程、经纬度数据下标
                mileLocationNums.add(i);
                //若有正常的里程、经纬度数据下标则进行数据替换操作
                if (normalMileLocationNum != null) {
                    replaceTravelAbnormalMileLocationData(positionalList, normalMileLocationNum, mileLocationNums,
                        false);
                }
            } else {
                //记录正常的里程、经纬度数据下标
                normalMileLocationNum = i;
                //若有异常的里程、经纬度数据下标则进行数据替换操作
                if (mileLocationNums.size() > 0) {
                    replaceTravelAbnormalMileLocationData(positionalList, normalMileLocationNum, mileLocationNums,
                        false);
                }
            }

            //传感器里程、经纬度数据替换
            if (StringUtils.isEmpty(sensorMile) || Objects.equals(sensorMile, "0") || Objects
                .equals(sensorMile, "0.0")) {
                //记录异常的里程、经纬度数据下标
                sensorMileLocationNums.add(i);
                //若有正常的里程、经纬度数据下标则进行数据替换操作
                if (normalSensorMileLocationNum != null) {
                    replaceTravelAbnormalMileLocationData(positionalList, normalSensorMileLocationNum,
                        sensorMileLocationNums, true);
                }
            } else {
                //记录正常的里程、经纬度数据下标
                normalSensorMileLocationNum = i;
                //若有异常的里程、经纬度数据下标则进行数据替换操作
                if (sensorMileLocationNums.size() > 0) {
                    replaceTravelAbnormalMileLocationData(positionalList, normalSensorMileLocationNum,
                        sensorMileLocationNums, true);
                }
            }

            /* 6.过滤替换油耗数据 */
            if (Objects.equals(oilWear, "0") || Objects.equals(oilWear, "0.0")) {
                //记录异常的油耗数据下标
                oilWearNums.add(i);
                //若有正常的油耗数据下标则进行数据替换操作
                if (normalOilWearNum != null) {
                    replaceTravelAbnormalOilWearData(positionalList, normalOilWearNum, oilWearNums);
                }
            } else {
                //记录正常的油耗数据下标
                normalOilWearNum = i;
                //若有异常的油耗数据下标则进行数据替换操作
                if (oilWearNums.size() > 0) {
                    replaceTravelAbnormalOilWearData(positionalList, normalOilWearNum, oilWearNums);
                }
            }
        }
    }

    public static String getVehicleColor(int plateColor) {
        String color;
        switch (plateColor) {
            case 1:
                color = "蓝色";
                break;
            case 2:
                color = "黄色";
                break;
            case 3:
                color = "黑色";
                break;
            case 4:
                color = "白色";
                break;
            case 5:
                color = "绿色";
                break;
            case 94:
                color = "渐变绿色";
                break;
            case 93:
                color = "黄绿色";
                break;
            default:
                color = "其他";
        }
        return color;
    }

    /**
     * 过滤出里程异常数据
     *
     * @param terminalPositionalList
     */
    public static void filterTerminalPositional(List<TerminalPositional> terminalPositionalList) {
        // 里程数据异常下标
        List<Integer> mileLocationNums = new ArrayList<>();
        // 最近一条正常的里程数据下标
        Integer normalMileLocationNum = null;
        for (int i = 0; i < terminalPositionalList.size(); i++) {
            String gpsMile = terminalPositionalList.get(i).getGpsMile(); //gps里程
            String longtitude = terminalPositionalList.get(i).getLongtitude(); //经度
            String latitude = terminalPositionalList.get(i).getLatitude(); //纬度
            if (StringUtils.isBlank(gpsMile) || Objects.equals(gpsMile, "0") || Objects.equals(gpsMile, "0.0")
                || StringUtils.isBlank(longtitude)
                || StringUtils.isBlank(latitude) || Objects.equals(longtitude, "0") || Objects.equals(latitude, "0")
                || Objects.equals(longtitude, "0.0") || Objects.equals(latitude, "0.0")) {
                //记录异常的里程、经纬度数据下标
                mileLocationNums.add(i);
                //若有异常的里程、经纬度数据下标则进行数据替换操作
                if (normalMileLocationNum != null) {
                    replaceTerminalPositional(terminalPositionalList, normalMileLocationNum, mileLocationNums);
                }
            } else {
                //记录正常下标
                normalMileLocationNum = i;
                //若有异常的里程、经纬度数据下标则进行数据替换操作
                if (mileLocationNums.size() > 0) {
                    replaceTerminalPositional(terminalPositionalList, normalMileLocationNum, mileLocationNums);
                }
            }
        }
    }

    private static void replaceTerminalPositional(List<TerminalPositional> terminalPositionalList,
        Integer normalMileLocationNum,
        List<Integer> mileLocationNums) {
        TerminalPositional normalPositional = terminalPositionalList.get(normalMileLocationNum);
        /* 替换异常数据 */
        for (Integer num : mileLocationNums) {
            TerminalPositional abnormalPositional = terminalPositionalList.get(num);
            abnormalPositional.setGpsMile(normalPositional.getGpsMile());
            abnormalPositional.setLongtitude(normalPositional.getLongtitude());
            abnormalPositional.setLatitude(normalPositional.getLatitude());
        }
        mileLocationNums.clear();
    }

    /**
     * 替换里程、经纬度异常数据
     *
     * @param positionalDetails     (所有位置信息)
     * @param normalMileLocationNum (正常经纬度里程数据下标)
     * @param mileLocationNums      (异常经纬度里程数据下标集合)
     */
    private static void replaceTravelAbnormalMileLocationData(List<PositionalDetail> positionalDetails,
        Integer normalMileLocationNum, List<Integer> mileLocationNums, boolean sensorFlag) {
        /* 1.获取正确数据 */
        PositionalDetail normalPositional = positionalDetails.get(normalMileLocationNum);

        /* 2.替换异常数据 */
        for (Integer num : mileLocationNums) {
            PositionalDetail abnormalPositional = positionalDetails.get(num);
            if (sensorFlag) {
                abnormalPositional.setMileageTotal(normalPositional.getMileageTotal());
            } else {
                abnormalPositional.setLongtitude(normalPositional.getLongtitude());
                abnormalPositional.setLatitude(normalPositional.getLatitude());
                abnormalPositional.setGpsMile(normalPositional.getGpsMile());
            }
        }

        /* 3.替换完毕后清空异常数据下标集合 */
        mileLocationNums.clear();
    }

    /**
     * 替换油耗异常数据
     *
     * @param positionalDetails (所有位置信息)
     * @param normalOilWearNum  (正常经纬度里程数据下标)
     * @param oilWearNums       (异常经纬度里程数据下标集合)
     */
    private static void replaceTravelAbnormalOilWearData(List<PositionalDetail> positionalDetails,
        Integer normalOilWearNum, List<Integer> oilWearNums) {
        /* 1.获取正确数据 */
        PositionalDetail normalPositional = positionalDetails.get(normalOilWearNum);

        /* 2.替换异常数据 */
        for (Integer num : oilWearNums) {
            PositionalDetail abnormalPositional = positionalDetails.get(num);
            abnormalPositional.setTotalOilwearOne(normalPositional.getTotalOilwearOne());
        }

        /* 3.替换完毕后清空异常数据下标集合 */
        oilWearNums.clear();
    }

    /**
     * 位置信息异常数据过滤
     * @param filterFlag 过滤开关
     */
    public static void positionalInfoAbnormalFilter(List<Positional> positionals, Boolean filterFlag) {
        if (filterFlag == null || !filterFlag) {
            return;
        }
        /* 1.初始化计算处理参数 */
        //里程、经纬度数据异常下标
        List<Integer> mileLocationNums = new ArrayList<>();
        //传感器里程、经纬度数据异常下标
        List<Integer> sensorMileLocationNums = new ArrayList<>();
        //油箱1数据异常下标
        List<Integer> oilTankOneNums = new ArrayList<>();
        //油箱2数据异常下标
        List<Integer> oilTankTwoNums = new ArrayList<>();
        //油耗数据异常下标
        List<Integer> oilWearNums = new ArrayList<>();
        //载重传感器一异常下标
        List<Integer> loadObjOneNums = new ArrayList<>();
        //载重传感器二异常下标
        List<Integer> loadObjTwoNums = new ArrayList<>();
        List<Integer> tirePressureParameterNums = new ArrayList<>();

        Integer normalMileLocationNum = null; //最近一条正常的里程、经纬度数据下标
        Integer normalSensorMileLocationNum = null; //最近一条正常的传感器里程、经纬度数据下标
        Integer normalOilTankOneNum = null; //最近一条正常的油箱1数据下标
        Integer normalOilTankTwoNum = null; //最近一条正常的油箱2数据下标
        Integer normalOilWearNum = null; //最近一条正常的油耗数据下标
        //最近一条正常的载重传感器数据下标
        Integer normalLoadObjOneNum = null;
        Integer normalLoadObjTwoNum = null;
        Integer normalTirePressureParameterNum = null;


        /* 2.循环过滤替换异常数据 */
        for (int i = 0; i < positionals.size(); i++) {
            Positional positional = positionals.get(i);
            String longtitude = positional.getLongtitude(); //经度
            String latitude = positional.getLatitude(); //纬度
            String gpsMile = positional.getGpsMile(); //gps里程
            Double sensorMile = positional.getMileageTotal(); //传感器里程
            String oilTankOne = positional.getOilTankOne(); //油箱1油量
            String oilTankTwo = positional.getOilTankTwo(); //油箱2油量
            String oilWear = positional.getTotalOilwearOne(); //油耗1
            String loadObjOne = positional.getLoadObjOne();
            String loadObjTwo = positional.getLoadObjTwo();
            String tirePressureParameter = positional.getTirePressureParameter();

            /* 3.过滤替换里程、经纬度数据 */
            if (Objects.equals(gpsMile, "0") || Objects.equals(gpsMile, "0.0") || StringUtils.isBlank(longtitude)
                || StringUtils.isBlank(latitude) || Objects.equals(longtitude, "0") || Objects.equals(latitude, "0")
                || Objects.equals(longtitude, "0.0") || Objects.equals(latitude, "0.0")) {
                //记录异常的里程、经纬度数据下标
                mileLocationNums.add(i);
                //若有正常的里程、经纬度数据下标则进行数据替换操作
                if (normalMileLocationNum != null) {
                    replaceAbnormalMileLocationData(positionals, normalMileLocationNum, mileLocationNums, false);
                }
            } else {
                //记录正常的里程、经纬度数据下标
                normalMileLocationNum = i;
                //若有异常的里程、经纬度数据下标则进行数据替换操作
                if (mileLocationNums.size() > 0) {
                    replaceAbnormalMileLocationData(positionals, normalMileLocationNum, mileLocationNums, false);
                }
            }

            //传感器里程、经纬度数据替换
            if (Objects.isNull(sensorMile) || Objects.equals(sensorMile, 0.0)) {
                //记录异常的里程、经纬度数据下标
                sensorMileLocationNums.add(i);
                //若有正常的里程、经纬度数据下标则进行数据替换操作
                if (normalSensorMileLocationNum != null) {
                    replaceAbnormalMileLocationData(positionals, normalSensorMileLocationNum, sensorMileLocationNums,
                        true);
                }
            } else {
                //记录正常的里程、经纬度数据下标
                normalSensorMileLocationNum = i;
                //若有异常的里程、经纬度数据下标则进行数据替换操作
                if (sensorMileLocationNums.size() > 0) {
                    replaceAbnormalMileLocationData(positionals, normalSensorMileLocationNum, sensorMileLocationNums,
                        true);
                }
            }

            /* 4.过滤替换油箱1数据 */
            if (StringUtils.isNotBlank(oilTankOne) && !"0".equals(oilTankOne)) {
                if (Double.parseDouble(oilTankOne) < 0.5) {
                    //记录异常的油箱1数据下标
                    oilTankOneNums.add(i);
                    //若有正常的油箱1数据下标则进行数据替换操作
                    if (normalOilTankOneNum != null) {
                        replaceAbnormalOilTankData(positionals, normalOilTankOneNum, oilTankOneNums, 1);
                    }
                } else {
                    //记录正常的油箱1数据下标
                    normalOilTankOneNum = i;
                    //若有异常的油箱1数据下标则进行数据替换操作
                    if (oilTankOneNums.size() > 0) {
                        replaceAbnormalOilTankData(positionals, normalOilTankOneNum, oilTankOneNums, 1);
                    }
                }
            }

            /* 5.过滤替换油箱2数据 */
            if (StringUtils.isNotBlank(oilTankTwo) && !"0".equals(oilTankTwo)) {
                if (Double.parseDouble(oilTankTwo) < 0.5) {
                    //记录异常的油箱1数据下标
                    oilTankTwoNums.add(i);
                    //若有正常的油箱1数据下标则进行数据替换操作
                    if (normalOilTankTwoNum != null) {
                        replaceAbnormalOilTankData(positionals, normalOilTankTwoNum, oilTankTwoNums, 2);
                    }
                } else {
                    //记录正常的油箱1数据下标
                    normalOilTankTwoNum = i;
                    //若有异常的油箱1数据下标则进行数据替换操作
                    if (oilTankTwoNums.size() > 0) {
                        replaceAbnormalOilTankData(positionals, normalOilTankTwoNum, oilTankTwoNums, 2);
                    }
                }
            }

            /* 6.过滤替换油耗数据 */
            if (Objects.equals(oilWear, "0") || Objects.equals(oilWear, "0.0")) {
                //记录异常的油耗数据下标
                oilWearNums.add(i);
                //若有正常的油耗数据下标则进行数据替换操作
                if (normalOilWearNum != null) {
                    replaceAbnormalOilWearData(positionals, normalOilWearNum, oilWearNums);
                }
            } else {
                //记录正常的油耗数据下标
                normalOilWearNum = i;
                //若有异常的油耗数据下标则进行数据替换操作
                if (oilWearNums.size() > 0) {
                    replaceAbnormalOilWearData(positionals, normalOilWearNum, oilWearNums);
                }
            }

            /*  7.过滤替换载重传感器1数据*/
            // if (!StringUtils.isNotBlank(loadObjOne)) {
            //     loadObjOneNums.add(i);
            //     if (normalLoadObjOneNum != null) {
            //         replaceAbnormalLoadObjData(positionals, normalLoadObjOneNum, loadObjOneNums, 1);
            //     }
            // } else {
            //     normalLoadObjOneNum = i;
            //     if (loadObjOneNums.size() > 0) {
            //         replaceAbnormalLoadObjData(positionals, normalLoadObjOneNum, loadObjOneNums, 1);
            //     }
            // }
            //
            // /*  8.过滤替换载重传感器2数据*/
            // if (!StringUtils.isNotBlank(loadObjTwo)) {
            //     loadObjTwoNums.add(i);
            //     if (normalLoadObjTwoNum != null) {
            //         replaceAbnormalLoadObjData(positionals, normalLoadObjTwoNum, loadObjTwoNums, 2);
            //     }
            // } else {
            //     normalLoadObjTwoNum = i;
            //     if (loadObjTwoNums.size() > 0) {
            //         replaceAbnormalLoadObjData(positionals, normalLoadObjTwoNum, loadObjTwoNums, 2);
            //     }
            // }
            //
            /*  9.过滤替换胎压传感器数据*/
            // if (StringUtils.isNotBlank(tirePressureParameter)) {
            //     //记录正常的胎压数据下标
            //     normalTirePressureParameterNum = i;
            //     if (tirePressureParameterNums.size() > 0) {
            //         //若有异常的胎压数据下标则进行数据替换操作
            //         replaceAbnormalTirePressureParameterData(positionals, normalTirePressureParameterNum,
            //             tirePressureParameterNums);
            //     }
            // } else {
            //     tirePressureParameterNums.add(i);
            //     if (normalTirePressureParameterNum != null) {
            //         //若有异常的胎压数据下标则进行数据替换操作
            //         replaceAbnormalTirePressureParameterData(positionals, normalTirePressureParameterNum,
            //             tirePressureParameterNums);
            //     }
            // }
        }
    }

    /**
     * 位置信息异常数据过滤，由于底层接口字段的名称进行了修改。
     * @param filterFlag 过滤开关
     */
    public static void positionalInfoAbnormalFilterNew(List<Positional> positionals, Boolean filterFlag) {
        if (filterFlag == null || !filterFlag) {
            return;
        }
        /* 1.初始化计算处理参数 */
        //里程、经纬度数据异常下标
        List<Integer> mileLocationNums = new ArrayList<>();
        //传感器里程、经纬度数据异常下标
        List<Integer> sensorMileLocationNums = new ArrayList<>();
        //油箱1数据异常下标
        List<Integer> oilTankOneNums = new ArrayList<>();
        //油箱2数据异常下标
        List<Integer> oilTankTwoNums = new ArrayList<>();
        //油耗数据异常下标
        List<Integer> oilWearNums = new ArrayList<>();
        //载重传感器一异常下标
        List<Integer> loadObjOneNums = new ArrayList<>();
        //载重传感器二异常下标
        List<Integer> loadObjTwoNums = new ArrayList<>();
        List<Integer> tirePressureParameterNums = new ArrayList<>();

        Integer normalMileLocationNum = null; //最近一条正常的里程、经纬度数据下标
        Integer normalSensorMileLocationNum = null; //最近一条正常的传感器里程、经纬度数据下标
        Integer normalOilTankOneNum = null; //最近一条正常的油箱1数据下标
        Integer normalOilTankTwoNum = null; //最近一条正常的油箱2数据下标
        Integer normalOilWearNum = null; //最近一条正常的油耗数据下标
        //最近一条正常的载重传感器数据下标
        Integer normalLoadObjOneNum = null;
        Integer normalLoadObjTwoNum = null;
        Integer normalTirePressureParameterNum = null;


        /* 2.循环过滤替换异常数据 */
        for (int i = 0; i < positionals.size(); i++) {
            Positional positional = positionals.get(i);
            String longitude = positional.getLongitude(); //经度
            positional.setLongtitude(longitude);
            String latitude = positional.getLatitude(); //纬度
            String gpsMile = positional.getGpsMile(); //gps里程
            Double sensorMile = positional.getMileageTotal(); //传感器里程
            String oilTankOne = positional.getOilTankOne(); //油箱1油量
            String oilTankTwo = positional.getOilTankTwo(); //油箱2油量
            String oilWear = positional.getTotalOilwearOne(); //油耗1
            String loadObjOne = positional.getLoadObjOne();
            String loadObjTwo = positional.getLoadObjTwo();
            String tirePressureParameter = positional.getTirePressureParameter();

            /* 3.过滤替换里程、经纬度数据 */
            if (Objects.equals(gpsMile, "0") || Objects.equals(gpsMile, "0.0") || StringUtils.isBlank(longitude)
                || StringUtils.isBlank(latitude) || Objects.equals(longitude, "0") || Objects.equals(latitude, "0")
                || Objects.equals(longitude, "0.0") || Objects.equals(latitude, "0.0")) {
                //记录异常的里程、经纬度数据下标
                mileLocationNums.add(i);
                //若有正常的里程、经纬度数据下标则进行数据替换操作
                if (normalMileLocationNum != null) {
                    replaceAbnormalMileLocationData(positionals, normalMileLocationNum, mileLocationNums, false);
                }
            } else {
                //记录正常的里程、经纬度数据下标
                normalMileLocationNum = i;
                //若有异常的里程、经纬度数据下标则进行数据替换操作
                if (mileLocationNums.size() > 0) {
                    replaceAbnormalMileLocationData(positionals, normalMileLocationNum, mileLocationNums, false);
                }
            }

            //传感器里程、经纬度数据替换
            if (Objects.isNull(sensorMile) || Objects.equals(sensorMile, 0.0)) {
                //记录异常的里程、经纬度数据下标
                sensorMileLocationNums.add(i);
                //若有正常的里程、经纬度数据下标则进行数据替换操作
                if (normalSensorMileLocationNum != null) {
                    replaceAbnormalMileLocationData(positionals, normalSensorMileLocationNum, sensorMileLocationNums,
                        true);
                }
            } else {
                //记录正常的里程、经纬度数据下标
                normalSensorMileLocationNum = i;
                //若有异常的里程、经纬度数据下标则进行数据替换操作
                if (sensorMileLocationNums.size() > 0) {
                    replaceAbnormalMileLocationData(positionals, normalSensorMileLocationNum, sensorMileLocationNums,
                        true);
                }
            }

            /* 4.过滤替换油箱1数据 */
            if (StringUtils.isNotBlank(oilTankOne) && !"0".equals(oilTankOne)) {
                if (Double.parseDouble(oilTankOne) < 0.5) {
                    //记录异常的油箱1数据下标
                    oilTankOneNums.add(i);
                    //若有正常的油箱1数据下标则进行数据替换操作
                    if (normalOilTankOneNum != null) {
                        replaceAbnormalOilTankData(positionals, normalOilTankOneNum, oilTankOneNums, 1);
                    }
                } else {
                    //记录正常的油箱1数据下标
                    normalOilTankOneNum = i;
                    //若有异常的油箱1数据下标则进行数据替换操作
                    if (oilTankOneNums.size() > 0) {
                        replaceAbnormalOilTankData(positionals, normalOilTankOneNum, oilTankOneNums, 1);
                    }
                }
            }

            /* 5.过滤替换油箱2数据 */
            if (StringUtils.isNotBlank(oilTankTwo) && !"0".equals(oilTankTwo)) {
                if (Double.parseDouble(oilTankTwo) < 0.5) {
                    //记录异常的油箱1数据下标
                    oilTankTwoNums.add(i);
                    //若有正常的油箱1数据下标则进行数据替换操作
                    if (normalOilTankTwoNum != null) {
                        replaceAbnormalOilTankData(positionals, normalOilTankTwoNum, oilTankTwoNums, 2);
                    }
                } else {
                    //记录正常的油箱1数据下标
                    normalOilTankTwoNum = i;
                    //若有异常的油箱1数据下标则进行数据替换操作
                    if (oilTankTwoNums.size() > 0) {
                        replaceAbnormalOilTankData(positionals, normalOilTankTwoNum, oilTankTwoNums, 2);
                    }
                }
            }

            /* 6.过滤替换油耗数据 */
            if (Objects.equals(oilWear, "0") || Objects.equals(oilWear, "0.0")) {
                //记录异常的油耗数据下标
                oilWearNums.add(i);
                //若有正常的油耗数据下标则进行数据替换操作
                if (normalOilWearNum != null) {
                    replaceAbnormalOilWearData(positionals, normalOilWearNum, oilWearNums);
                }
            } else {
                //记录正常的油耗数据下标
                normalOilWearNum = i;
                //若有异常的油耗数据下标则进行数据替换操作
                if (oilWearNums.size() > 0) {
                    replaceAbnormalOilWearData(positionals, normalOilWearNum, oilWearNums);
                }
            }
        }
    }


    /**
     * 替换里程、经纬度异常数据
     *
     * @param positionals           (所有位置信息)
     * @param normalMileLocationNum (正常经纬度里程数据下标)
     * @param mileLocationNums      (异常经纬度里程数据下标集合)
     */
    private static void replaceAbnormalMileLocationData(List<Positional> positionals, Integer normalMileLocationNum,
        List<Integer> mileLocationNums, boolean sensorFlag) {
        /* 1.获取正确数据 */
        Positional normalPositional = positionals.get(normalMileLocationNum);

        /* 2.替换异常数据 */
        for (Integer num : mileLocationNums) {
            Positional abnormalPositional = positionals.get(num);
            if (sensorFlag) {
                abnormalPositional.setMileageTotal(normalPositional.getMileageTotal());
            } else {
                abnormalPositional.setLongtitude(normalPositional.getLongtitude());
                abnormalPositional.setLatitude(normalPositional.getLatitude());
                abnormalPositional.setGpsMile(normalPositional.getGpsMile());
                //适配底层接口字段发生改变
                abnormalPositional.setLongitude(normalPositional.getLongtitude());
            }
        }

        /* 3.替换完毕后清空异常数据下标集合 */
        mileLocationNums.clear();
    }

    /**
     * 替换油箱异常数据
     *
     * @param positionals      (所有位置信息)
     * @param normalOilTankNum (正常油箱数据下标)
     * @param oilTankNums      (异常油箱数据下标集合)
     * @param oilTankNum       (替换油箱编号)
     */
    private static void replaceAbnormalOilTankData(List<Positional> positionals, Integer normalOilTankNum,
        List<Integer> oilTankNums, Integer oilTankNum) {
        /* 1.获取正确数据 */
        Positional normalPositional = positionals.get(normalOilTankNum);

        /* 2.替换异常数据 */
        for (Integer num : oilTankNums) {
            Positional abnormalPositional = positionals.get(num);
            switch (oilTankNum) {
                case 1:
                    abnormalPositional.setOilTankOne(normalPositional.getOilTankOne());
                    break;
                case 2:
                    abnormalPositional.setOilTankTwo(normalPositional.getOilTankTwo());
                    break;
                default:
                    break;
            }
        }

        /* 3.替换完毕后清空异常数据下标集合 */
        oilTankNums.clear();
    }

    /**
     * 替换
     *
     * @param positionals      位置信息集合
     * @param normalLoadObjNum 正常数据下标
     * @param loadObjNums      数据下标集合
     * @param loadObjNum       异常数据下标
     */
    private static void replaceAbnormalLoadObjData(List<Positional> positionals, Integer normalLoadObjNum,
        List<Integer> loadObjNums, Integer loadObjNum) {
        /* 1.获取正确数据 */
        Positional normalPositional = positionals.get(normalLoadObjNum);

        /* 2.替换异常数据 */
        for (Integer num : loadObjNums) {
            Positional abnormalPositional = positionals.get(num);
            switch (loadObjNum) {
                case 1:
                    abnormalPositional.setLoadObjOne(normalPositional.getLoadObjOne());
                    break;
                case 2:
                    abnormalPositional.setLoadObjTwo(normalPositional.getLoadObjTwo());
                    break;
                default:
                    break;
            }
        }

        /* 3.替换完毕后清空异常数据下标集合 */
        loadObjNums.clear();
    }

    /**
     * 替换异常胎压数据
     *
     * @param positionals                    所有位置信息
     * @param normalTirePressureParameterNum 正常胎压数据下标
     * @param tirePressureParameterNums      异常胎压数据下标
     */
    private static void replaceAbnormalTirePressureParameterData(List<Positional> positionals,
        Integer normalTirePressureParameterNum, List<Integer> tirePressureParameterNums) {
        /* 1.获取正确数据 */
        Positional normalPositional = positionals.get(normalTirePressureParameterNum);

        /* 2.替换异常数据 */
        for (Integer num : tirePressureParameterNums) {
            Positional abnormalPositional = positionals.get(num);
            abnormalPositional.setTirePressureParameter(normalPositional.getTirePressureParameter());
        }

        /* 3.替换完毕后清空异常数据下标集合 */
        tirePressureParameterNums.clear();
    }

    /**
     * 替换油耗异常数据
     *
     * @param positionals      (所有位置信息)
     * @param normalOilWearNum (正常经纬度里程数据下标)
     * @param oilWearNums      (异常经纬度里程数据下标集合)
     */
    private static void replaceAbnormalOilWearData(List<Positional> positionals, Integer normalOilWearNum,
        List<Integer> oilWearNums) {
        /* 1.获取正确数据 */
        Positional normalPositional = positionals.get(normalOilWearNum);

        /* 2.替换异常数据 */
        for (Integer num : oilWearNums) {
            Positional abnormalPositional = positionals.get(num);
            abnormalPositional.setTotalOilwearOne(normalPositional.getTotalOilwearOne());
        }

        /* 3.替换完毕后清空异常数据下标集合 */
        oilWearNums.clear();
    }

    /**
     * 根据省域id、市域id获取
     *
     * @param provinceId (省域id)
     * @param cityId     (市域id)
     */
    public static String getDivisionsCode(String provinceId, String cityId) {
        if (StringUtils.isNotBlank(provinceId) || StringUtils.isNotBlank(cityId)) {
            return getProvinceCode(provinceId) + getCityCode(cityId);
        }
        return null;
    }

    /**
     * 根据省域id获取省域code
     */
    public static String getProvinceCode(String provinceId) {
        if (StringUtils.isBlank(provinceId)) {
            return "00";
        }
        int length = provinceId.length();
        switch (length) {
            case 1:
                return "0" + provinceId;
            case 2:
                return provinceId;
            default:
                return "";
        }
    }

    /**
     * 根据市域id获取市域code
     */
    public static String getCityCode(String cityId) {
        if (StringUtils.isBlank(cityId)) {
            return "0000";
        }
        int length = cityId.length();
        switch (length) {
            case 1:
                return "000" + cityId;
            case 2:
                return "00" + cityId;
            case 3:
                return "0" + cityId;
            case 4:
                return cityId;
            default:
                return "";
        }
    }

    /**
     * 取绝对值，因为Integer.MIN_VALUE的绝对值超过了Integer的表示范围，故转换为Integer.MAX_VALUE
     */
    public static int abs(int value) {
        if (value == Integer.MIN_VALUE) {
            return Integer.MAX_VALUE;
        }
        return Math.abs(value);
    }

    public static BigDecimal toBigDecimal(Double value) {
        return toBigDecimal(value.toString());
    }

    public static BigDecimal toBigDecimal(String value) {
        return new BigDecimal(value).setScale(1, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 生成一个唯一标识，避免不同时间生成的数据重复引用问题
     * 这里前端传递是202005只到月份，而我们的报表当月的数据，每天都会变
     * 当天查询的数据只要条件一样都是一样，上一个月的数据都是不变的，即只要
     * 前面的月份数据只要查询条件一样，那么生成的flag也是一样的，当月的数据，
     * 只要当天的查询条件一样，那么生成的flag也是一样的
     * @param month
     * @return
     */
    public static String getFlag(long month) {
        LocalDateTime now = LocalDateTime.now();
        //如果不是当月的数据,直接以该条件作为标记
        if (Date8Utils.getValToMonth(now) > month) {
            return month + "";
        } else {
            //如果是当月的，每天一个唯一flag,因为每天数据一样，每一天数据都不一样
            return Date8Utils.getValToDay(now) + "";
        }
    }


}
