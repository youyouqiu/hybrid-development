package com.zw.platform.util.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;


public class GeoHashUtil {
    /**
     * 1 2500km;2 630km;3 78km;4 30km
     * 5 2.4km; 6 610m; 7 76m; 8 19m; 9 5m;
     */
    private static int hashLength = 5; //经纬度转化为geohash长度
    private static int latLength = 12; //纬度转化为二进制长度
    private static int lngLength = 13; //经度转化为二进制长度

    private static double minLat;//每格纬度的单位大小
    private static double minLng;//每个经度的倒下

    public static final double MIN_LAT = -90;
    public static final double MAX_LAT = 90;
    public static final double MIN_LNG = -180;
    public static final double MAX_LNG = 180;

    private static double latitude;
    private static double longitude;
    private static final char[] CHARS =
        { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
    private static final HashMap<Character, Integer> lookup = new HashMap<>();

    public GeoHashUtil() {
        setMinLatLng();
    }

    static {
        int i = 0;
        for (char c : CHARS) {
            lookup.put(c, i++);
        }
    }

    /**
     * @return
     * @Author:lulei
     * @Description: 求所在坐标点及周围点组成的九个
     */
    public List<String> getGeoHashBase32For9() {
        double leftLat = latitude - minLat;
        double rightLat = latitude + minLat;
        double upLng = longitude - minLng;
        double downLng = longitude + minLng;
        List<String> base32For9 = new ArrayList<String>();
        //左侧从上到下 3个
        String leftUp = getGeoHashBase32(leftLat, upLng);
        if (!(leftUp == null || "".equals(leftUp))) {
            base32For9.add(leftUp);
        }
        String leftMid = getGeoHashBase32(leftLat, longitude);
        if (!(leftMid == null || "".equals(leftMid))) {
            base32For9.add(leftMid);
        }
        String leftDown = getGeoHashBase32(leftLat, downLng);
        if (!(leftDown == null || "".equals(leftDown))) {
            base32For9.add(leftDown);
        }
        //中间从上到下 3个
        String midUp = getGeoHashBase32(latitude, upLng);
        if (!(midUp == null || "".equals(midUp))) {
            base32For9.add(midUp);
        }
        String midMid = getGeoHashBase32(latitude, longitude);
        if (!(midMid == null || "".equals(midMid))) {
            base32For9.add(midMid);
        }
        String midDown = getGeoHashBase32(latitude, downLng);
        if (!(midDown == null || "".equals(midDown))) {
            base32For9.add(midDown);
        }
        //右侧从上到下 3个
        String rightUp = getGeoHashBase32(rightLat, upLng);
        if (!(rightUp == null || "".equals(rightUp))) {
            base32For9.add(rightUp);
        }
        String rightMid = getGeoHashBase32(rightLat, longitude);
        if (!(rightMid == null || "".equals(rightMid))) {
            base32For9.add(rightMid);
        }
        String rightDown = getGeoHashBase32(rightLat, downLng);
        if (!(rightDown == null || "".equals(rightDown))) {
            base32For9.add(rightDown);
        }
        return base32For9;
    }

    /**
     * 设置经纬度的最小单位
     */
    private static void setMinLatLng() {
        minLat = MAX_LAT - MIN_LAT;
        for (int i = 0; i < latLength; i++) {
            minLat /= 2.0;
        }
        minLng = MAX_LNG - MIN_LNG;
        for (int i = 0; i < lngLength; i++) {
            minLng /= 2.0;
        }
    }

    /**
     * 设置经纬度转化为geohash长度
     * @param length
     * @return
     */
    public static boolean setHashLength(int length) {
        if (length < 1) {
            return false;
        }
        hashLength = length;
        latLength = (length * 5) / 2;
        if (length % 2 == 0) {
            lngLength = latLength;
        } else {
            lngLength = latLength + 1;
        }
        setMinLatLng();
        return true;
    }

    /**
     * 获取经纬度的base32字符串
     * @param lat
     * @param lng
     * @return
     */
    public static String getGeoHashBase32(double lat, double lng) {
        latitude = lat;
        longitude = lng;
        boolean[] bools = getGeoBinary(lat, lng);
        if (bools == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bools.length; i = i + 5) {
            boolean[] base32 = new boolean[5];
            for (int j = 0; j < 5; j++) {
                base32[j] = bools[i + j];
            }
            char cha = getBase32Char(base32);
            if (' ' == cha) {
                return null;
            }
            sb.append(cha);
        }
        return sb.toString();
    }

    /**
     * 将五位二进制转化为base32
     * @param base32
     * @return
     */
    private static char getBase32Char(boolean[] base32) {
        if (base32 == null || base32.length != 5) {
            return ' ';
        }
        int num = 0;
        for (boolean bool : base32) {
            num <<= 1;
            if (bool) {
                num += 1;
            }
        }
        return CHARS[num % CHARS.length];
    }

    /**
     * 获取坐标的geo二进制字符串
     * @param lat
     * @param lng
     * @return
     */
    private static boolean[] getGeoBinary(double lat, double lng) {
        boolean[] latArray = getHashArray(lat, MIN_LAT, MAX_LAT, latLength);
        boolean[] lngArray = getHashArray(lng, MIN_LNG, MAX_LNG, lngLength);
        return merge(latArray, lngArray);
    }

    /**
     * 合并经纬度二进制
     * @param latArray
     * @param lngArray
     * @return
     */
    private static boolean[] merge(boolean[] latArray, boolean[] lngArray) {
        if (latArray == null || lngArray == null) {
            return null;
        }
        boolean[] result = new boolean[lngArray.length + latArray.length];
        Arrays.fill(result, false);
        for (int i = 0; i < lngArray.length; i++) {
            result[2 * i] = lngArray[i];
        }
        for (int i = 0; i < latArray.length; i++) {
            result[2 * i + 1] = latArray[i];
        }
        return result;
    }

    /**
     * 将数字转化为geohash二进制字符串
     * @param value
     * @param min
     * @param max
     * @param length
     * @return
     */
    private static boolean[] getHashArray(double value, double min, double max, int length) {
        if (value < min || value > max) {
            return null;
        }
        if (length < 1) {
            return null;
        }
        boolean[] result = new boolean[length];
        for (int i = 0; i < length; i++) {
            double mid = (min + max) / 2.0;
            if (value > mid) {
                result[i] = true;
                min = mid;
            } else {
                result[i] = false;
                max = mid;
            }
        }
        return result;
    }

    /**
     * GeoHash解码
     * @param geohash
     * @return
     */
    public static double[] decode(String geohash) {
        StringBuilder buffer = new StringBuilder();
        for (char c : geohash.toCharArray()) {

            int i = lookup.get(c) + 32;
            buffer.append(Integer.toString(i, 2).substring(1));
        }

        BitSet lonSet = new BitSet();
        BitSet latSet = new BitSet();

        //偶数位，经度
        int j = 0;
        for (int i = 0; i < lngLength * 2; i += 2) {
            boolean isSet = false;
            if (i < buffer.length()) {
                isSet = buffer.charAt(i) == '1';
            }
            lonSet.set(j++, isSet);
        }

        //奇数位，纬度
        j = 0;
        for (int i = 1; i < latLength * 2; i += 2) {
            boolean isSet = false;
            if (i < buffer.length()) {
                isSet = buffer.charAt(i) == '1';
            }
            latSet.set(j++, isSet);
        }

        double lon = decode(lonSet, -180, 180);
        double lat = decode(latSet, -90, 90);

        return new double[] { lat, lon };
    }

    /**
     * 根据二进制和范围解码
     * @param bs
     * @param floor
     * @param ceiling
     * @return
     */
    private static double decode(BitSet bs, double floor, double ceiling) {
        double mid = 0;
        for (int i = 0; i < bs.length(); i++) {
            mid = (floor + ceiling) / 2;
            if (bs.get(i)) {
                floor = mid;
            } else {
                ceiling = mid;
            }
        }
        return mid;
    }

    public static void main(String[] args) {
        String geoHashBase32 = GeoHashUtil.getGeoHashBase32(30.589763, 107.572001);
        System.out.println(geoHashBase32);
    }

}
