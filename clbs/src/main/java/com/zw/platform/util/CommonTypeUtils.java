package com.zw.platform.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: zjc
 * @Description: 通用类型转化工具类
 * @Date: create in 2020/9/14 14:06
 */
public class CommonTypeUtils {
    private static Map<String, String> transMap = new HashMap<>();
    private static Map<String, String> typeCodeMap = new HashMap<>();

    static {
        initTransMap();

        initVehicleTypeCodeMap();
    }

    private static void initVehicleTypeCodeMap() {
        typeCodeMap.put("客车", "10");
        typeCodeMap.put("大型客车", "11");
        typeCodeMap.put("中型客车", "12");
        typeCodeMap.put("小型客车", "13");
        typeCodeMap.put("轿车", "14");
        typeCodeMap.put("大型卧铺车", "15");
        typeCodeMap.put("中型卧铺车", "16");

        typeCodeMap.put("普通货车", "20");
        typeCodeMap.put("大型普通货车", "21");
        typeCodeMap.put("中型普通货车", "22");
        typeCodeMap.put("小型普通货车", "23");
        typeCodeMap.put("专用运输车", "30");
        typeCodeMap.put("集装箱车", "31");
        typeCodeMap.put("大件运输车", "32");
        typeCodeMap.put("保温冷藏车", "33");
        typeCodeMap.put("商品车运输专用车", "34");
        typeCodeMap.put("罐车", "35");

        typeCodeMap.put("牵引车", "36");
        typeCodeMap.put("挂车", "37");
        typeCodeMap.put("平板车", "38");
        typeCodeMap.put("其他专用车", "39");
        typeCodeMap.put("危险品运输车", "40");
        typeCodeMap.put("农用车", "50");
        typeCodeMap.put("拖拉机", "60");
        typeCodeMap.put("轮式拖拉机", "61");

        typeCodeMap.put("手扶拖拉机", "62");
        typeCodeMap.put("履带拖拉机", "63");
        typeCodeMap.put("特种拖拉机", "64");
        typeCodeMap.put("其他车辆", "90");
    }

    private static void initTransMap() {
        transMap.put("道路旅客运输", "010");
        transMap.put("班车客运", "011");
        transMap.put("包车客运", "012");
        transMap.put("定线旅游", "013");
        transMap.put("非定线旅游", "014");

        transMap.put("道路货物运输", "020");
        transMap.put("道路普通货物运输", "021");
        transMap.put("货物专用运输", "022");
        transMap.put("大型物件运输", "023");
        transMap.put("道路危险货物运输", "030");
        transMap.put("营运性危险货物运输", "031");

        transMap.put("非经营性危险货物运输", "032");
        transMap.put("机动车维修", "040");
        transMap.put("汽车维修", "041");
        transMap.put("危险货物运输车辆维修", "042");
        transMap.put("摩托车维修", "043");
        transMap.put("其他机动车维修", "044");

        transMap.put("机动车驾驶员培训", "050");
        transMap.put("普通机动车驾驶员培训", "051");
        transMap.put("道路运输驾驶员从业资格培训", "052");
        transMap.put("机动车驾驶员培训教练场", "053");
        transMap.put("站场服务", "060");
        transMap.put("道路旅客运输站", "061");
        transMap.put("道路货运站（场）", "062");
        transMap.put("国际道路运输", "070");
        transMap.put("国际道路旅客运输", "071");

        transMap.put("国际道路货物运输", "072");
        transMap.put("公交运输", "080");
        transMap.put("出租运输", "090");
        transMap.put("客运出租运输", "091");
        transMap.put("货运出租运输", "092");
        transMap.put("汽车租赁", "100");
        transMap.put("客运汽车租赁", "101");
        transMap.put("货运汽车租赁", "102");
        transMap.put("FFFFF", "");
    }

    public static String getTransTypeByPurposeType(String vehiclePurpose) {
        return transMap.get(vehiclePurpose);
    }

    /**
     * 根据车辆类型名称获取车辆类型编码
     */
    public static String getVehicleTypeCode(String type) {
        return typeCodeMap.get(type);

    }
}
