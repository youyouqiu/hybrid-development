package com.zw.platform.util;


import java.text.DecimalFormat;

import com.zw.platform.domain.vas.oilmassmgt.FuelTank;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.zw.platform.util.common.Converter;


/**
 * 油量管理计算工具类 <p>Title: OilMassMgtUtil.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p> <p>team:
 * ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年10月31日下午4:01:41
 * @version 1.0
 */
public class OilMassMgtUtil {
    private static Logger log = LogManager.getLogger(OilMassMgtUtil.class);

    private static final double CONSTANT_1 = 0.8; // 计算油杆上盲区时需要的常量

    private static final double CONSTANT_2 = 2.2; // 计算实际下盲区时需要的常量

    private static final double CONSTANT_3 = 3; // 计算油杆有效长度（量程）时需要的常量

    private static DecimalFormat df_1 = new DecimalFormat("0.0"); // 保留一位小数

    // 基本常数工式---------------------------------------------------
    /**
     * 计算油杆上盲区
     * @Title: getE
     * @param t 油箱厚度
     * @return
     * @return double
     * @throws @author Liubangquan
     */
    public static double getE(double t) {
        double result = 0;
        try {
            result = CONSTANT_1 - Converter.toDouble(t, 0.0);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        if (result > 0) { // 因为添加油箱的时候厚度的填写范围为0.1-1，所以当油箱厚度小于0.8时，计算出实际的油杆上盲区，否则返回0
            return result;
        } else {
            return 0;
        }
    }

    /**
     * 计算实际下盲区
     * @Title: getM1
     * @param h 油箱高度
     * @param m 油杆长度
     * @return
     * @return double
     * @throws @author Liubangquan
     */
    public static double getM1(double h, double m, double t) {
        double result = 0;
        try {
            result = Converter.toDouble(h) + CONSTANT_2 - Converter.toDouble(m) - 2 * Converter.toDouble(t);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return result;
    }

    /**
     * 油杆有效长度（量程）
     * @Title: getH1
     * @param m 油杆长度
     * @return
     * @return double
     * @throws @author Liubangquan
     */
    public static double getH1(double m) {
        double result = 0;
        try {
            result = Converter.toDouble(m) - CONSTANT_3;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return result;
    }

    /**
     * 计算实际液位高度
     * @Title: getHA
     * @param hm 液位测量高度
     * @param h 高
     * @param m 油杆长度
     * @param t 油箱壁厚
     * @return
     * @return String
     * @throws @author Liubangquan
     */
    public String getHA(double hm, double h, double m, double t) {
        double result = 0;
        try {
            result = Converter.toDouble(hm) + Converter.toDouble(getM1(h, m, t));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return Converter.toBlank(result);
    }
    // --------------------------------------------------------------

    /**
     * 获取实际长度
     * @Title: getRealLength
     * @param length 长度
     * @param thickness 油箱壁厚
     * @return
     * @return String
     * @throws @author Liubangquan
     */
    public static String getRealLength(String length, String thickness) {
        String realLength = "";
        if (Converter.toDouble(length) > 0 && Converter.toDouble(thickness) > 0) {
            realLength = Converter.toBlank(Converter.toDouble(length) - 2 * Converter.toDouble(thickness));
        }
        return realLength;
    }

    /**
     * 获取实际宽度
     * @Title: getRealWidth
     * @param weight 宽度
     * @param thickness 油箱壁厚
     * @return
     * @return String
     * @throws @author Liubangquan
     */
    public static String getRealWidth(String weight, String thickness) {
        String realWidth = "";
        if (Converter.toDouble(weight) > 0 && Converter.toDouble(thickness) > 0) {
            realWidth = Converter.toBlank(Converter.toDouble(weight) - 2 * Converter.toDouble(thickness));
        }
        return realWidth;
    }

    /**
     * 获取实际高度
     * @Title: getRealHeight
     * @param height 高度
     * @param thickness 油箱壁厚
     * @return
     * @return String
     * @throws @author Liubangquan
     */
    public static String getRealHeight(String height, String thickness) {
        String realHeight = "";
        if (Converter.toDouble(height) > 0 && Converter.toDouble(thickness) > 0) {
            realHeight = Converter.toBlank(Converter.toDouble(height) - 2 * Converter.toDouble(thickness));
        }
        return realHeight;
    }

    /**
     * 计算长方体油箱的理论容积
     * @Title: get_theory_Volume_rectangle
     * @return
     * @return String
     * @author hujun
     */
    public static String get_theory_Volume_rectangle(FuelTank fuelTank) {
        double result = 0;
        try {
            String l = fuelTank.getBoxLength(); // 油箱长度
            String w = fuelTank.getWidth(); // 油箱宽度
            String h = fuelTank.getHeight(); // 油箱高度
            String t = fuelTank.getThickness(); // 油箱壁厚
            String r1 = fuelTank.getButtomRadius(); // 下圆角半径
            String r2 = fuelTank.getTopRadius(); // 上圆角半径
            //double rl = Converter.toDouble(getRealLength(l, t)) / 100;
            //double rw = Converter.toDouble(getRealWidth(w, t)) / 100;
            //double rh = Converter.toDouble(getRealHeight(h, t)) / 100;
            //result = rl * rw * rh;
            //所有计算常量转换为cm
            double rl = Converter.toDouble(l) / 10;//长
            double rw = Converter.toDouble(w) / 10;//宽
            double rh = Converter.toDouble(h) / 10;//高
            double ha = Converter.toDouble(getRealHeight(h, t)) / 10;//液位高度
            double rt = Converter.toDouble(t) / 10;//壁厚
            double rx = 5;//下圆角半径
            double rs = 5;//上圆角半径
            if (!StringUtil.isNullOrBlank(r1)) {
                rx = Converter.toDouble(r1) / 10;
            }
            if (!StringUtil.isNullOrBlank(r2)) {
                rs = Converter.toDouble(r2) / 10;
            }
            if (rx == rt && rs == rt) { //无圆导角，为长方体
                result = ((rl - 2 * rt) * (rw - 2 * rt) * (rh - 2 * rt)) / 1000;
            } else { //有圆导角，代入公式
                result = ((rl - 2 * rt) * (rw - 2 * rx) * (rx - rt)) / 1000
                    + ((rl - 2 * rt) * Math.PI * Math.pow(rx - rt, 2)) / 2000
                    + ((rl - 2 * rt) * (rw - 2 * rt) * (rh - rx - rs)) / 1000 + ((rl - 2 * rt) / 1000) 
                    * ((ha - rh + rs + rt) 
                        * Math.pow(Double.parseDouble(String.format("%.5f", Math.pow(rx - rt,2))) 
                                    - Double.parseDouble(String.format("%.5f",Math.pow(ha - rh + rs + rt,2))),1.0 / 2.0)
                        + Math.pow(rx - rt,2) 
                                    * Math.asin(Double.parseDouble(String.format("%.5f",(ha - rh + rs + rt) 
                                                                                                    / (rx - rt)))))
                    + ((rl - 2 * rt) * (rw - 2 * rs) * (ha - rh + rs + rt)) / 1000;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return Converter.toBlank(df_1.format(result));
    }

    /**
     * 计算圆柱形油箱的理论容积
     * @Title: get_theory_Volume_Circle
     * @param l 长度
     * @param h 高度
     * @param t 油箱壁厚
     * @return
     * @return String
     * @throws @author Liubangquan
     */
    public static String get_theory_Volume_Circle(String l, String h, String t) {
        double result = 0;
        try {
            double rh = Converter.toDouble(getRealHeight(h, t)) / 100;
            double r = rh / 2;
            double rl = Converter.toDouble(getRealLength(l, t)) / 100;
            result = Math.PI * r * r * rl;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return Converter.toBlank(df_1.format(result));
    }

    /**
     * 计算椭圆体油箱的理论容积
     * @Title: get_theory_Volume_oval
     * @param l 长度
     * @param w 宽度
     * @param h 高度
     * @param t 油箱壁厚
     * @return
     * @return String
     * @throws @author Liubangquan
     */
    public static String get_theory_Volume_oval(String l, String w, String h, String t) {
        double result = 0;
        try {
            double rl = Converter.toDouble(getRealLength(l, t)) / 100;
            double rw = Converter.toDouble(getRealWidth(w, t)) / 100;
            double rh = Converter.toDouble(getRealHeight(h, t)) / 100;
            result = Math.PI * (rw / 2) * (rh / 2) * rl;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return Converter.toBlank(df_1.format(result));
    }

    /**
     * 计算D形油箱的理论容积
     * @Title: get_theory_Volume_D
     * @param l 长度 单位：mm
     * @param w 宽度 单位：mm
     * @param h 高度 单位：mm
     * @param t 油箱壁厚 单位：mm
     * @return
     * @return String
     * @throws @author Liubangquan
     */
    public static String get_theory_Volume_D(String l, String w, String h, String t) {
        double result = 0;
        try {
            double rl = Converter.toDouble(getRealLength(l, t)) / 100;
            double rw = Converter.toDouble(getRealWidth(w, t)) / 100;
            double rh = Converter.toDouble(getRealHeight(h, t)) / 100;
            double vol1 = rl * (rw - rh / 2) * rh;
            double vol2 = (Math.PI * (rh / 2) * (rh / 2) / 2) * rl;
            result = vol1 + vol2;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return Converter.toBlank(df_1.format(result));
    }

    /**
     * 根据油箱形状计算油箱理论容积
     * @Title: get_theory_Volume_by_shape
     * @param fuelTank 油箱实体
     * @return
     * @return String
     * @throws @author Liubangquan
     */
    public static String get_theory_Volume_by_shape(FuelTank fuelTank) {
        String shape = fuelTank.getShape(); // 油箱形状
        String l = fuelTank.getBoxLength(); // 油箱长度
        String w = fuelTank.getWidth(); // 油箱宽度
        String h = fuelTank.getHeight(); // 油箱高度
        String t = fuelTank.getThickness(); // 油箱壁厚
        String r1 = fuelTank.getButtomRadius(); // 下圆角半径
        String r2 = fuelTank.getTopRadius(); // 上圆角半径
        String theoryVolume = "";
        if (Converter.toBlank(shape).equals("1")) { // 长方体
            theoryVolume = OilMassMgtUtil.get_theory_Volume_rectangle(fuelTank);
        } else if (Converter.toBlank(shape).equals("2")) { // 圆柱形
            theoryVolume = OilMassMgtUtil.get_theory_Volume_Circle(l, h, t);
        } else if (Converter.toBlank(shape).equals("3")) { // D形
            theoryVolume = OilMassMgtUtil.get_theory_Volume_D(l, w, h, t);
        } else if (Converter.toBlank(shape).equals("4")) { // 椭圆形
            theoryVolume = OilMassMgtUtil.get_theory_Volume_oval(l, w, h, t);
        } else { // 其他
            theoryVolume = "0";
        }
        return theoryVolume;
    }

}
