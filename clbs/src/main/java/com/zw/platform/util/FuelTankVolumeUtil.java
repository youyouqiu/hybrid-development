package com.zw.platform.util;


import com.zw.platform.util.common.Converter;

import java.text.DecimalFormat;


/**
 * 油箱容积计算公式
 * <p>Title: FuelTankVolumeUtil.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年11月1日下午1:53:52
 * @version 1.0
 */
public class FuelTankVolumeUtil {

    private static DecimalFormat df = new DecimalFormat("0.00"); // 保留两位小数

    /**
     *  计算矩形油箱体积
     * @param rl 长
     * @param rw 宽
     * @param ha 液位高度
     * @param rt 壁厚
     * @param rh 高
     * @param rx 下圆角半径
     * @param rs 上圆角半径
     * @return
     */
    public static double rectangleCal(double rl, double rw, double ha, double rt, double rh, double rx, double rs) {
        double volume = 0;
        // （L-2t）*(W-2t)*（HA-2t）/1000
        //volume = (L-2*t)*(W-2*t)*HA/1000;
        //下圆角半径界限
        double rxFlag = Double.parseDouble(String.format("%.5f",rx - rt));
        //上圆角半径界限
        double rsFlag = Double.parseDouble(String.format("%.5f",rh - rs - rt));
        //最大液位高度
        double rhFlag = Double.parseDouble(String.format("%.5f",rh - 2 * rt));
        if (rx == rt && rs == rt) { //无圆导角，为长方体
            volume = (rl - 2 * rt) * (rw - 2 * rt) * ha / 1000;
        } else {
            if (0 < ha && ha <= rxFlag) { //在下圆角之内
                volume = ((rl - 2 * rt) * (rw - 2 * rx) * ha) / 1000 + ((rl - 2 * rt) / 1000) 
                    * ((ha - rx + rt) * Math.pow(Math.pow(rx - rt,2) - Math.pow(ha - rx + rt,2),1.0 / 2.0)
                        + Math.pow(rx - rt,2) * Math.asin((ha - rx + rt) / (rx - rt))
                        - Math.pow(rx - rt,2) * Math.asin((rt - rx) / (rx - rt)));
            } else if (rxFlag < ha && ha <= rsFlag) { //在下圆角与上圆角之间
                volume = ((rl - 2 * rt) * (rw - 2 * rx) * (rx - rt)) / 1000
                    + ((rl - 2 * rt) * Math.PI * Math.pow(rx - rt, 2)) / 2000
                    + ((rl - 2 * rt) * (rw - 2 * rt) * (ha + rt - rx)) / 1000;
            } else if (rsFlag < ha && ha <= rhFlag) { //在上圆角之内
                volume = ((rl - 2 * rt) * (rw - 2 * rx) * (rx - rt)) / 1000
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
        }
        return Converter.toDouble(df.format(volume));
    }

    /**
     * 计算圆形油箱的体积
     * @param 长度 L
     * @param 高度H
     * @param 壁厚t
     * @param H1    量程
     * @param E  上盲区
     * @param HM 油箱刻度
     * @param variable 每一等分值
     * @return
     */
    public static double circularCal(double l, double h, double t, double ha, double variable) {
        double volume = 0;
        // 当油位高度小于半径时,即HA≤H/2-t
        if (ha <= ((h / 2) - t)) {
            // 开始值  -(H-2t)/2
            double startValue = -(h - 2 * t) / 2;
            // 结束值  HA-(H-2t)/2
            double endValue = ha - (h - 2 * t) / 2;
            // 等分数，每一等分1mm
            int k = (int) ((endValue - startValue) / variable);
            double result = 0;
            for (int i = 0; i < k; i++) {
                double y = startValue + i * variable;
                result += Math.pow(Math.pow((h - 2 * t) / 2, 2) - Math.pow(y, 2), 0.5) * variable; 
                //  √(〖((H-2t)/2)〗^2-y^2 )*dy
            }
            volume = ((l - 2 * t) / 500) * result;

            // 当油量高度大于半径时； 即HA>H/2-t时
        } else if (ha > (h / 2 - t)) {
            // 开始值 0
            double startValue = 0;
            // 结束值 HA- (H-2t)/2
            double endValue = ha - (h - 2 * t) / 2;
            // 等分数，每一等分1mm
            int k = (int) ((endValue - startValue) / variable);
            double result = 0;
            for (int i = 0; i < k; i++) {
                double y = startValue + i * variable;
                result += Math.pow(Math.pow((h - 2 * t) / 2, 2) - Math.pow(y, 2), 0.5) * variable; 
                //  √(〖((H-2t)/2)〗^2-y^2 )*dy
            }
            volume = Math.PI * Math.pow((h - 2 * t), 2) * ((l - 2 * t) / 8000) + ((l - 2 * t) / 500) * result; 
            //  PI*〖（H-2t）〗^2*(L-2*t)/8000+(L-2*t)/500*result
        }
        return Converter.toDouble(df.format(volume));
    }

    /**
     *  计算椭圆形油箱体积
     * @param L 长
     * @param W 宽
     * @param H 高
     * @param t 壁厚
     * @param HM 油箱刻度
     * @param variable 等分数
     * @return
     */
    public static double ellipseCal(double l, double w, double h, double t, double ha, double variable) {
        double volume = 0;
        // 当油位高度于半径时,即HA≤H/2-t
        if (ha <= (h / 2 - t)) {
            // 开始值 -(H-2t)/2
            double startValue = -(h - 2 * t) / 2;
            // 结束值 HA- (H-2t)/2
            double endValue = ha - (h - 2 * t) / 2;
            // 等分数，每一等分1mm
            int k = (int) ((endValue - startValue) / variable);
            double result = 0;
            for (int i = 0; i < k; i++) {
                double y = startValue + i * variable;
                result += Math.pow(1 - ((4 * Math.pow(y, 2)) / Math.pow((h - 2 * t), 2)), 0.5) * variable; 
                //  √(1-(4y^2)/〖(H-2t)〗^2 )*dy
            }
            volume = ((l - 2 * t) / 1000) * (w - 2 * t) * result; //  (L-2*t)/1000*(w-2t)*result

            // 当油量高度大于半径时； 即HA>H/2-t时
        } else if (ha > (h / 2 - t)) {
            // 开始值 0
            double startValue = 0;
            // 结束值HA-(H-2t)/2
            double endValue = ha - (h - 2 * t) / 2;
            // 等分数，每一等分1mm
            int k = (int) ((endValue - startValue) / variable);
            double result = 0;
            for (int i = 0; i < k; i++) {
                double y = startValue + i * variable;
                result += Math.pow(1 - ((4 * Math.pow(y, 2)) / Math.pow((h - 2 * t), 2)), 0.5) * variable; 
                //  √(1-(4y^2)/〖(H-2t)〗^2 )*dy
            }
            volume = Math.PI * (w - 2 * t) * (h - 2 * t) * ((l - 2 * t) / 8000)
                + ((l - 2 * t) / 1000) * (w - 2 * t) * result; 
            //  PI*(W-2*t)*(H-2*t)* (L-2*t)/8000+(L-2*t)/1000*(w-2t)*result
        }
        return Converter.toDouble(df.format(volume));
    }

    /**
     * 计算D形油箱体积
     * @param L
     * @param W
     * @param H
     * @param t
     * @param HM
     * @param variable
     * @return
     */
    public static double dxShapeCal(double l, double w, double h, double t, double ha, double variable) {
        double volume = 0;
        // 当油位高度小于半径时；HA≤H/2-t  时
        if (ha <= (h / 2 - t)) {
            // 开始值-(H-2t)/2
            double startValue = -(h - 2 * t) / 2;
            // 结束值 HA- (H-2t)/2
            double endValue = ha - (h - 2 * t) / 2;
            // 等分数，每一等分1mm
            int k = (int) ((endValue - startValue) / variable);
            double result = 0;
            for (int i = 0; i < k; i++) {
                double y = startValue + i * variable;
                result += Math.pow(Math.pow((h - 2 * t) / 2, 2) - Math.pow(y, 2), 0.5) * variable; 
                //  √(〖((H-2t)/2)〗^2-y^2 )*dy
            }
            volume = ((l - 2 * t) * (w - (h / 2) - t) * ha) / 1000 + ((l - 2 * t) / 1000) * result; 
            // ((L-2*t)*（W- H/2-t）*HA)/1000+(L-2*t)/1000*result

            // 当油位高度大于半径时； 即HA>H/2-t时
        } else if (ha > (h / 2 - t)) {
            // 开始值 0
            double startValue = 0;
            // 结束值HA- (H-2t)/2
            double endValue = ha - (h - 2 * t) / 2;
            // 等分数，每一等分1mm
            int k = (int) ((endValue - startValue) / variable);
            double result = 0;
            for (int i = 0; i < k; i++) {
                double y = startValue + i * variable;
                result += Math.pow(Math.pow((h - 2 * t) / 2, 2) - Math.pow(y, 2), 0.5) * variable; 
                //  √(〖((H-2t)/2)〗^2-y^2 )*dy
            }
            volume = ((l - 2 * t) * (w - (h / 2) - t) * ha) / 1000
                + Math.PI * Math.pow((h - 2 * t), 2) * (l - 2 * t) / 16000 + ((l - 2 * t) / 1000) * result; 
            // ((L-2*t)*（W-H/2-t）*（Hm+H+2.2-M-2t）)/1000+(PI*〖（H-2t）〗^2*(L-2*t))/16000+(L-2*t)/1000*result
        }
        return Converter.toDouble(df.format(volume));
    }

}
