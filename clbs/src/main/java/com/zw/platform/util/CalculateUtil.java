/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved.
 * This software is the confidential and proprietary information of
 * ZhongWei, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with ZhongWei.
 */
package com.zw.platform.util;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.util.common.Converter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 增值服务工具类
 * <p>Title: CalculateUtil.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年9月21日下午3:58:21
 * @version 1.0
 *
 */
public class CalculateUtil {
    private static Logger log = LogManager.getLogger(CalculateUtil.class);
    private static final String FUEL_TYPE_DIESEL = "柴油";
    private static final String FUEL_TYPE_GAS = "汽油";
    private static final String FUEL_TYPE_CNG = "CNG";

    private static DecimalFormat df = new DecimalFormat("0.0000");

    /**
     * 计算时长
     * @Title: countTime
     * @param begin 开始时间（yyyy-MM-dd HH:mm:ss）
     * @param end 结束时间（yyyy-MM-dd HH:mm:ss）
     * @return
     * @return String 时间差（xx小时xx分钟）
     * @throws
     * @author Liubangquan
     */
    public static String countTime(String begin, String end) {
        int hour = 0;
        int minute = 0;
        int second = 0;
        long total_second = 0;
        StringBuffer sb = new StringBuffer();

        Date begin_date = Converter.toDate(begin, "yyyy-MM-dd HH:mm:ss");
        Date end_date = Converter.toDate(end, "yyyy-MM-dd HH:mm:ss");

        total_second = (end_date.getTime() - begin_date.getTime()) / (1000);

        hour = (int) total_second / 60 / 60;
        minute = (int) total_second / 60 % 60;
        second = (int) total_second % 60;

        sb.append(hour).append("小时").append(minute).append("分").append(second).append("秒");
        return sb.toString();
    }

    /**
     * 将 小时 转换成 xx小时xx分xx秒
     * @Title: converHourToYYYYMMDD
     * @param duration
     * @return
     * @return String
     * @throws
     * @author Liubangquan
     */
    public static String converHourToYYYYMMDD(String duration) {
        StringBuffer sb = new StringBuffer();
        double d = Converter.toDouble(duration);
        double totalSeconds = d * 60 * 60;
        int hour = (int) totalSeconds / 60 / 60;
        int minute = (int) totalSeconds / 60 % 60;
        int second = (int) totalSeconds % 60;
        sb.append(hour).append("小时").append(minute).append("分").append(second).append("秒");
        return sb.toString();
    }

    /**
     * 将如2小时23分50秒的时间转换为小时
     * @Title: timeToHour
     * @param duration 时长
     * @return
     * @return String 小时为单位的时间
     * @throws
     * @author Liubangquan
     */
    public static double timeToHour(String duration) {
        String hour = duration.substring(0, duration.indexOf("小"));
        String minute = duration.substring(duration.indexOf("时") + 1, duration.indexOf("分"));
        String second = duration.substring(duration.indexOf("分") + 1, duration.indexOf("秒"));
        double total_hour = Converter.toDouble(hour) + Converter.toDouble(minute) / 60 + Converter.toDouble(second) / 60 / 60;
        return total_hour;
    }

    /**
     * 累计时间：单位1/10小时，取值范围0-99999999
     * 将传上来的时间转换成单位为"小时"
     * @Title: converteTimeUnit
     * @param duration
     * @return
     * @return double
     * @throws
     * @author Liubangquan
     */
    public static double converteTimeUnit(String duration) {
        return Converter.toDouble(duration) * 1 / 10;
    }

    /**
     * 累计油耗：单位0.01升,取值范围0-99999999
     * 将传上来的累计油耗转换成单位为"升"
     * @Title: converteFuelUnit
     * @param fuel
     * @return
     * @return double
     * @throws
     * @author Liubangquan
     */
    public static double converteFuelUnit(String fuel) {
        return Converter.toDouble(df.format(Converter.toDouble(fuel) * 0.01));
    }

    /**
     * 单位转换:T转Kg
     * @Title: converterTtoKg
     * @param weight
     * @return
     * @return String
     * @throws
     * @author Liubangquan
     */
    public static String converterTtoKg(String weight) {
        return df.format(Converter.toDouble(weight) * 1000);
    }

    /**
     * 计算总油耗量：打火到熄火这期间产生的油耗量
     * @Title: getTotalFuel
     * @param start
     * @param end
     * @return
     * @return double
     * @throws
     * @author Liubangquan
     */
    public static double getTotalFuel(String start, String end) {
        double totalFuel = Converter.toDouble(end) - Converter.toDouble(start);
        return converteFuelUnit(df.format(totalFuel));
    }

    /**
     * 计算平均速度
     * @Title: averageSpeed
     * @param mile 里程（km）
     * @param duration 开始时间
     * @return
     * @return String 平均速度 （km/h）
     * @throws
     * @author Liubangquan
     */
    public static String averageSpeed(String mile, double duration) {
        if (Converter.toBlank(mile).equals("") || Converter.toBlank(duration).equals("") || Converter.toDouble(duration) == 0.0) {
            return df.format(0);
        }
        return df.format(Converter.toDouble(mile) / duration);
    }

    /**
     * 计算当期平均能耗-按时间
     * @Title: getCurAverageEnergy
     * @param totalEnergy 总能耗量（L/m3）
     * @param duration 时长
     * @return
     * @return String 当期平均能耗（L/h）
     * @throws
     * @author Liubangquan
     */
    public static String getCurAverageEnergy_by_time(String totalEnergy, String duration) {
        return df.format(Converter.toDouble(totalEnergy) / Converter.toDouble(duration));
    }

    /**
     * 计算当期平均能耗-按里程
     * @Title: getCurAverageEnergy_by_mile
     * <当期平均能耗>：=(总能耗量/里程)*100；
     * @param totalEnergy 总能耗量（L）
     * @param mile 里程（km）
     * @return
     * @return String 当期平均能耗（L/百公里）
     * @throws
     * @author Liubangquan
     */
    public static String getCurAverageEnergy_by_mile(String totalEnergy, String mile) {
        if (totalEnergy.equals("0.0") || mile.equals("0.0")) {
            return "0";
        } else {
            return df.format((Converter.toDouble(totalEnergy) / Converter.toDouble(mile)) * 100);
        }

    }

    /**
     * 计算能源节约量-燃料-按时间
     * @Title: getEnergySaving_fuel
     * @param baseEnergy 基准能耗
     * @param curAverageEnergy 当期平均能耗
     * @return
     * @return String 能源节约量-燃料
     * @throws
     * @author Liubangquan
     */
    public static String getEnergySaving_fuel_by_time(String baseEnergy, String curAverageEnergy, String duration) {
        return df.format((Converter.toDouble(baseEnergy) - Converter.toDouble(curAverageEnergy)) * Converter.toDouble(duration));
    }

    /**
     * 计算能源节约量-燃料-按里程
     * @Title: getEnergySaving_fuel_by_mile
     * <能源节约量>：=（基准能耗 - 当期平均能耗）*里程/100；（里程转换成百公里）
     * @param baseEnergy 基准能耗
     * @param curAverageEnergy 当期平均能耗
     * @param mile 里程
     * @return
     * @return String 能源节约量-燃料
     * @throws
     * @author Liubangquan
     */
    public static String getEnergySaving_fuel_by_mile(String baseEnergy, String curAverageEnergy, String mile) {
        return df.format((Converter.toDouble(baseEnergy) - Converter.toDouble(curAverageEnergy)) * Converter.toDouble(mile) / 100);
    }

    /**
     * 计算能源节约量-煤
     * @Title: getEnergySaving_coal
     * @param energySaving_fuel 基准能耗
     * @param fuelType 燃料类型
     * @return 能源节约量-煤
     * @return String
     * @throws
     * @author Liubangquan
     */
    public static String getEnergySaving_coal(String energySaving_fuel, String fuelType) {
        String energySaving_coal = "";
        if (Converter.toBlank(fuelType).equals(FUEL_TYPE_DIESEL)) { // 柴油
            energySaving_coal = dieselVolumnToCoalWeight(energySaving_fuel);
        } else if (Converter.toBlank(fuelType).equals(FUEL_TYPE_GAS)) { // 汽油
            energySaving_coal = gasVolumnToCoalWeight(energySaving_fuel);
        }
        return energySaving_coal;
    }

    /**
     * 计算节能率
     * @Title: getEnergySavingRate
     * @param energySaving_fuel 能源节约量
     * @param baseEnergy 基准能耗
     * @return
     * @return String 节能率
     * @throws
     * @author Liubangquan
     */
    public static String getEnergySavingRate(String energySaving_fuel, String baseEnergy, double duration) {
        return df.format(Converter.toDouble(energySaving_fuel) / (Converter.toDouble(baseEnergy) * duration) * 100);
    }

    /**
     * 计算节能率-里程能耗统计用
     * @Title: getEnergySavingRate_mile
     * <节能率>：= 能源节约量 / （基准能耗*里程）*100%；
     * @param energySaving_fuel 能源节约量
     * @param baseEnergy 基准能耗
     * @param mileage
     * @return
     * @return String 节能率
     * @throws
     * @author Liubangquan
     */
    public static String getEnergySavingRate_mile(String energySaving_fuel, String baseEnergy, double mileage) {
        double d = Converter.toDouble(baseEnergy) * (mileage / 100);
        if (d > 0) {
            return df.format(Converter.toDouble(energySaving_fuel) / (d) * 100);
        } else {
            return "";
        }
    }

    /**
     * 计算减少排放量-CO2
     * @Title: getReduceEmissions_CO2
     * @param energySaving_fuel 能源节约量
     * @param fuelType 燃料类型
     * @return
     * @return String 减少排放量-CO2
     * @throws
     * @author Liubangquan
     */
    public static String getReduceEmissions_CO2(String energySaving_fuel, String fuelType) {
        String reduceEmissions_CO2 = "";
        if (Converter.toBlank(fuelType).equals(FUEL_TYPE_DIESEL)) { // 柴油
            reduceEmissions_CO2 = dieselWeightToCO2(dieselVolumnToWeight(energySaving_fuel));
        } else if (Converter.toBlank(fuelType).equals(FUEL_TYPE_GAS)) { // 汽油
            reduceEmissions_CO2 = gasWeightToCO2(gasVolumnToWeight(energySaving_fuel));
        }
        return reduceEmissions_CO2;
    }

    /**
     * 计算减少排放量-SO2
     * @Title: getReduceEmissions_SO2
     * @param energySaving_fuel 能源节约量
     * @param fuelType 燃料类型
     * @return
     * @return String 减少排放量-SO2
     * @throws
     * @author Liubangquan
     */
    public static String getReduceEmissions_SO2(String energySaving_fuel, String fuelType) {
        String reduceEmissions_SO2 = "";
        if (Converter.toBlank(fuelType).equals(FUEL_TYPE_DIESEL)) { // 柴油
            reduceEmissions_SO2 = coalToSO2(dieselVolumnToCoalWeight(energySaving_fuel));
        } else if (Converter.toBlank(fuelType).equals(FUEL_TYPE_GAS)) { // 汽油
            reduceEmissions_SO2 = coalToSO2(gasVolumnToCoalWeight(energySaving_fuel));
        }
        return reduceEmissions_SO2;
    }

    /**
     * 计算减少排放量-NOX
     * @Title: getReduceEmissions_NOX
     * @param energySaving_fuel 能源节约量
     * @param fuelType 燃料类型
     * @return
     * @return String 减少排放量-NOX
     * @throws
     * @author Liubangquan
     */
    public static String getReduceEmissions_NOX(String energySaving_fuel, String fuelType) {
        String reduceEmissions_NOX = "";
        if (Converter.toBlank(fuelType).equals(FUEL_TYPE_DIESEL)) { // 柴油
            reduceEmissions_NOX = coalToNOX(dieselVolumnToCoalWeight(energySaving_fuel));
        } else if (Converter.toBlank(fuelType).equals(FUEL_TYPE_GAS)) { // 汽油
            reduceEmissions_NOX = coalToNOX(gasVolumnToCoalWeight(energySaving_fuel));
        }
        return reduceEmissions_NOX;
    }

    /**
     * 柴油体积转换成对应的标准煤
     * @Title: dieselToCoal
     * @param volumn 体积（升）
     * @return
     * @return String 标准煤（吨）
     * @throws
     * @author Liubangquan
     */
    public static String dieselVolumnToCoalWeight(String volumn) {
        return df.format(Converter.toDouble(1.4571 / 1163 * Converter.toDouble(volumn)));
    }

    /**
     * 汽油体积转换成对应的标准煤
     * @Title: gasToCoal
     * @param volumn 体积（升）
     * @return
     * @return String 标准煤（吨）
     * @throws
     * @author Liubangquan
     */
    public static String gasVolumnToCoalWeight(String volumn) {
        return df.format(Converter.toDouble(1.4714 / 1370 * Converter.toDouble(volumn)));
    }

    /**
     * 柴油体积转重量-*
     * @Title: dieselVolumnToWeight
     * @param volumn 柴油体积（升）
     * @return
     * @return String 柴油重量（吨）
     * @throws
     * @author Liubangquan
     */
    public static String dieselVolumnToWeight(String volumn) {
        return df.format(1.0 / 1163.0 * Converter.toDouble(volumn));
    }

    /**
     * 柴油重量与标准煤重量的转换
     * @Title: dieselWeightToCoalWeight
     * @param weight 柴油的重量（吨）
     * @return
     * @return String 标准煤的重量（吨）
     * @throws
     * @author Liubangquan
     */
    public static String dieselWeightToCoalWeight(String weight) {
        return df.format(1.4571 * Converter.toDouble(weight));
    }

    /**
     * 汽油体积转重量-*
     * @Title: gasVolumnToWeight
     * @param volumn 汽油体积（升）
     * @return
     * @return String 汽油重量（吨）
     * @throws
     * @author Liubangquan
     */
    public static String gasVolumnToWeight(String volumn) {
        return df.format(1.0 / 1370.0 * Converter.toDouble(volumn));
    }

    /**
     * 汽油重量与标准煤重量的转换
     * @Title: gasWeightToCoalWeight
     * @param weight 汽油重量（吨）
     * @return
     * @return String 标准煤重量（吨）
     * @throws
     * @author Liubangquan
     */
    public static String gasWeightToCoalWeight(String weight) {
        return df.format(Converter.toDouble(1.4714 * Converter.toDouble(weight)));
    }

    /**
     * 煤与CO2的转换
     * @Title: coalToCO2
     * @param coal 煤的重量（吨）
     * @return
     * @return String CO2的重量（吨）
     * @throws
     * @author Liubangquan
     */
    public static String coalToCO2(String coal) {
        return df.format(Converter.toDouble(2.93 * Converter.toDouble(coal)));
    }

    /**
     * 煤与C的转换
     * @Title: coalToC
     * @param coal 煤的重量（吨）
     * @return
     * @return String C的重量（吨）
     * @throws
     * @author Liubangquan
     */
    public static String coalToC(String coal) {
        return df.format(Converter.toDouble(0.7991 * Converter.toDouble(coal)));
    }

    /**
     * 煤与SO2的转换
     * @Title: coalToSO2
     * @param coal 煤的重量（吨）
     * @return
     * @return String SO2的重量（kg）
     * @throws
     * @author Liubangquan
     */
    public static String coalToSO2(String coal) {
        return df.format(Converter.toDouble(8.5 * Converter.toDouble(coal)));
    }

    /**
     * 煤与NOX的转换
     * @Title: coalToNOX
     * @param coal 煤的重量（吨）
     * @return
     * @return String NOX的重量（kg）
     * @throws
     * @author Liubangquan
     */
    public static String coalToNOX(String coal) {
        return df.format(Converter.toDouble(7.4 * Converter.toDouble(coal)));
    }

    /**
     * 柴油重量与CO2的转换-*
     * @Title: dieselWeightToCO2
     * @param weight 柴油重量（吨）
     * @return
     * @return String CO2的重量（吨）
     * @throws
     * @author Liubangquan
     */
    public static String dieselWeightToCO2(String weight) {
        return df.format(3.0581 * Converter.toDouble(weight));
    }

    /**
     * 柴油重量与C的转换-*
     * @Title: dieselWeightToC
     * @param weight 柴油重量（吨）
     * @return
     * @return String C的重量（吨）
     * @throws
     * @author Liubangquan
     */
    public static String dieselWeightToC(String weight) {
        return df.format(0.8340 * Converter.toDouble(weight));
    }

    /**
     * 汽油重量与CO2的转换-*
     * @Title: gasWeightToCO2
     * @param weight 汽油重量（吨）
     * @return
     * @return String CO2重量（吨）
     * @throws
     * @author Liubangquan
     */
    public static String gasWeightToCO2(String weight) {
        return df.format(3.1507 * Converter.toDouble(weight));
    }

    /**
     * 汽油重量与C的转换-*
     * @Title: gasWeightToC
     * @param weight 汽油重量（吨）
     * @return
     * @return String C重量（吨）
     * @throws
     * @author Liubangquan
     */
    public static String gasWeightToC(String weight) {
        return df.format(Converter.toDouble(0.8593 * Converter.toDouble(weight)));
    }

    /**
     * CNG体积与CO2转换
     * @Title: CNGToCO2
     * @param volumn CNG体积（m3）
     * @return
     * @return String CO2重量（kg）
     * @throws
     * @author Liubangquan
     */
    public static String CNGToCO2(String volumn) {
        return df.format(Converter.toDouble(2.66 * Converter.toDouble(volumn)));
    }

    /**
     * 能耗转排放
     * @Title: energyToEmissions
     * @param actualEnergy 实际能耗
     * @param fuelType 燃料类型
     * @return
     * @return String
     * @throws
     * @author Liubangquan
     */
    public static String energyToEmissions(String actualEnergy, String fuelType) {
        if (FUEL_TYPE_DIESEL.equals(Converter.toBlank(fuelType))) { // 柴油
            return df.format(Converter.toDouble(dieselWeightToC(dieselVolumnToWeight(actualEnergy))));
        } else if (FUEL_TYPE_GAS.equals(Converter.toBlank(fuelType))) { // 汽油
            return df.format(Converter.toDouble(gasWeightToC(gasVolumnToWeight(actualEnergy))));
        }
        return "";
    }

    /**
     * 对应Hbase时间格式
     * @param time
     * @return
     */
    public static String toHbaseTime(String time) {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String r = "";
        try {
            Date date = sdf.parse(time);
            r = date.toString();
        } catch (ParseException e) {
            log.error("error", e);
        }
        return r;
    }

    /**
     * 时间差
     * @param endTime
     * @param startTime
     * @return
     */
    public static String toDateTime(String endTime, String startTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String diffrentTime = "";
//      Date date1 = Converter.toDate(sdf.format(endTime));
//		Date date2= Converter.toDate(sdf.format(startTime));
        Date date1 = Converter.toDate(endTime, "yyyy-MM-dd HH:mm:ss");
        Date date2 = Converter.toDate(startTime, "yyyy-MM-dd HH:mm:ss");
        double diff = date1.getTime() - date2.getTime();
        double days = diff / (1000 * 60 * 60);
        diffrentTime = String.valueOf(days);
        return diffrentTime;
    }

    /**
     * 时间差S
     * @param endTime
     * @param startTime
     * @return
     */
    public static long toDateTimeS(String endTime, String startTime) {
        long diffrentTime = 0;
//        Date date1 = Converter.toDate(sdf.format(endTime));
//		Date date2= Converter.toDate(sdf.format(startTime));
        Date date1 = Converter.toDate(endTime);
        Date date2 = Converter.toDate(startTime);
        long diff = date1.getTime() - date2.getTime();
        long days = diff / (1000);
        try {
            diffrentTime = Integer.valueOf(String.valueOf(days));
        } catch (Exception e) {
            log.error("error", e);
        }
//		diffrentTime= Integer.valueOf(String.valueOf(days));
        return diffrentTime;
    }


    /**
     *status解析
     *acc,0：熄火,1：点火
     *
     */
    public static JSONObject getStatus(String status) {
        JSONObject jsonObject = new JSONObject();
        if (status != null) {
            long i = Long.parseLong(status);
            String a = Long.toBinaryString(i);
            int acc = Integer.parseInt(a.substring(a.length() - 1, a.length()));
            jsonObject.put("acc", acc);
            return jsonObject;
        }

        return jsonObject;
    }


//    public static Config getStormConfig(){
//		Config conf=new Config();
//		conf.setDebug(false);
//		conf.put("storm.thrift.transport", "org.apache.storm.security.auth.SimpleTransportPlugin");
//		conf.put(Config.STORM_NIMBUS_RETRY_TIMES, 1);
//		conf.put(Config.STORM_NIMBUS_RETRY_INTERVAL, 1);
//		conf.put(Config.STORM_NIMBUS_RETRY_INTERVAL_CEILING, 1);
//		conf.put(Config.DRPC_MAX_BUFFER_SIZE, 1048576);
//		return conf;
//	}
//
//	public static LinearDRPCTopologyBuilder construct() {
//        LinearDRPCTopologyBuilder builder = new LinearDRPCTopologyBuilder("reach");
//		builder.addBolt(new OilBolt(), 4);
//        return builder;}

}
