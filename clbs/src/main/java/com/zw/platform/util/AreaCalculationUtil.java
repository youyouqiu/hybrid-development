package com.zw.platform.util;

import com.zw.platform.domain.functionconfig.form.CircleForm;
import com.zw.platform.domain.functionconfig.form.LineForm;
import com.zw.platform.domain.functionconfig.form.PolygonForm;

import java.text.DecimalFormat;

/**
 * 面积计算
 * @author penghj
 * @version 1.0
 * @date 2019/11/5 10:52
 */
public class AreaCalculationUtil {

    private static DecimalFormat format = new DecimalFormat("#.0");

    /**
     * 圆面积
     */
    public static Double getCircleArea(CircleForm form) {
        Double radius = form.getRadius();
        DecimalFormat df = new DecimalFormat("#.000");
        String radFormat = df.format(radius);
        radius = Double.parseDouble(radFormat) / 1000;
        return Double.parseDouble(format.format(Math.PI * Math.pow(radius, 2)));
    }

    /**
     * 线面积
     */
    public static Double getLineArea(LineForm form) {
        Integer width = form.getWidth();
        String[] points = form.getPointSeqs().split(",");
        String[] lons = form.getLongitudes().split(",");
        String[] lats = form.getLatitudes().split(",");
        double area = 0.0;
        for (int i = 0; i < points.length - 1; i++) {
            double lng1 = Double.parseDouble(lons[i]);
            double lat1 = Double.parseDouble(lats[i]);
            double lng2 = Double.parseDouble(lons[i + 1]);
            double lat2 = Double.parseDouble(lats[i + 1]);
            double distance = getDistance(lng1, lat1, lng2, lat2);
            area += (distance * width * 2);
        }
        area = area * 0.000001;
        return Double.parseDouble(format.format(area));
    }

    /**
     * 两个点的距离
     */
    private static double getDistance(double lng1, double lat1, double lng2, double lat2) {
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double a = radLat1 - radLat2;
        double b = Math.toRadians(lng1) - Math.toRadians(lng2);
        double s = 2 * Math.asin(Math.sqrt(
            Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        // 取WGS84标准参考椭球中的地球长半径(单位:m)
        s = s * 6378137.0;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    /**
     * 多边形面积
     */
    public static Double getPolygonArea(PolygonForm form) {
        String[] points = form.getPointSeqs().split(",");
        String[] lats = form.getLatitudes().split(",");
        String[] lons = form.getLongitudes().split(",");
        int len = points.length;
        if (len < 3) {
            return 0.0;
        }
        double total =
            Double.parseDouble(lats[0]) * (Double.parseDouble(lons[lons.length - 1]) - Double.parseDouble(lons[1]));
        for (int i = 1; i < points.length; i++) {
            double lat1 = Double.parseDouble(lats[i]);
            double lng2 = Double.parseDouble(lons[i - 1]);
            double lngtem = Double.parseDouble(lons[(i + 1) % lons.length]);
            total += lat1 * (lng2 - lngtem);

        }

        return Double.parseDouble(format.format(Math.abs(total * 9101160000.085981F) * 0.000001));
    }

    private static double convertToRadian(double input) {
        return input * Math.PI / 180;
    }
}
