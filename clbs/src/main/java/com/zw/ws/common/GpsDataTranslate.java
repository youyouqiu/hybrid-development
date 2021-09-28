package com.zw.ws.common;

/**
 * Created by jiangxiaoqiang on 2016/11/7.
 * 适用于Google,高德体系的地图
 */
public class GpsDataTranslate {

    /**
     * 圆周率
     */
    private static double PI = Math.PI;

    /**
     * 地球的半径(单位:米)
     */
    private static double EARTH_RADIUS = 6378245.0;

    /**
     * ee: 椭球的偏心率(eccentricity of ellipsoid)
     */
    private static double ECCENTRICITY_OF_ELLIPSOID = 0.0066934216229659433;

    private static boolean IsOutOfChina(double latitude, double longitude) {
        return longitude < 72.004 || longitude > 137.8347 || (latitude < 0.8293 || latitude > 55.8271);
    }

    private static double TransformLat(double x, double y) {
        double num = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        num += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        num += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
        return num + (160.0 * Math.sin(y / 12.0 * PI) + 320.0 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
    }

    private static double TransformLon(double x, double y) {
        double num = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        num += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        num += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        return num + (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
    }

    /**
     * 地理位置纠偏
     *
     * @param wgLat
     * @param wgLon
     */
    public static double[] transform(double wgLat, double wgLon) {
        double[] latlng = new double[2];
        if (IsOutOfChina(wgLat, wgLon)) {
            latlng[0] = wgLat;
            latlng[1] = wgLon;
            return latlng;
        }
        double dLat = TransformLat(wgLon - 105.0, wgLat - 35.0);
        double dLon = TransformLon(wgLon - 105.0, wgLat - 35.0);
        double radLat = wgLat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - ECCENTRICITY_OF_ELLIPSOID * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((EARTH_RADIUS * (1 - ECCENTRICITY_OF_ELLIPSOID)) / (magic * sqrtMagic) * PI);
        dLon = (dLon * 180.0) / (EARTH_RADIUS / sqrtMagic * Math.cos(radLat) * PI);
        latlng[0] = (wgLat + dLat);
        latlng[1] = (wgLon + dLon);
        return latlng;
    }
    
    /**
     * 
     * @Description:火星做标to 84
     * @param lat
     * @param lon
     * @return
     * double[]
     * @exception:
     * @author: wangying
     * @time:2017年1月4日 上午10:17:54
     */
    public static double[] gcj_To_Gps84(double lat, double lon) {
    	double[] latlng = new double[2];
    	double[] gcj_latlng = transform(lat, lon);
    	latlng[0] = lat * 2 - gcj_latlng[0];
    	latlng[1] = lon * 2 - gcj_latlng[1];  
        return latlng;
    }  
}
