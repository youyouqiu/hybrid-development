package com.zw.platform.domain.param;

import java.util.HashMap;
import java.util.Map;

/**
 * 原车数据流
 * @author zhouzongbo on 2018/10/11 10:04
 */
public enum OBDDataStreamEnum {
    /**
     * 原车数据改由F3计算
     */
    OBD_INFO_1("远光灯状态",0x0647,getOpenOrCloseStatus()),
    OBD_INFO_2("近光灯状态",0x0648,getOpenOrCloseStatus()),
    OBD_INFO_3("小灯状态",0x0646,getOpenOrCloseStatus()),
    OBD_INFO_4("刹车状态(脚刹)",0x0015,getBarkingStatus()),
    OBD_INFO_5("安全带(驾驶员)",0x02C0,getSafetyBeltStatus()),
    OBD_INFO_6("空调开关",0x0370,getOpenOrCloseStatus()),
    OBD_INFO_7("能源类型",0x0623,getEnergyTypesStatus()),
    OBD_INFO_9("仪表总里程(Km)",0x0290,null),
    OBD_INFO_10("累计里程(Km)",0x051A,null),
    OBD_INFO_11("仪表记录的短途行驶里程(Km)",0x0511,null),
    OBD_INFO_12("车辆油箱油量(L)",0x0517,null),
    OBD_INFO_13("油箱液位高度(mm)",0x0633,null),
    OBD_INFO_14("累计总油耗(ml)",0x0512,null),
    OBD_INFO_15("瞬时油耗(ml/h)",0x0513,null),
    OBD_INFO_16("瞬时百公里油耗(ml/100Km)",0x0514,null),
    OBD_INFO_17("水温(℃)",0x0305,null),
    OBD_INFO_18("转速(rpm)",0x0300,null),
    OBD_INFO_19("仪表车速(km/h)",0x030B,null),
    OBD_INFO_20("电池电压(V)",0x01F0,null),
    OBD_INFO_21("机油压力(KPa)",0x0293,null),
    OBD_INFO_22("电池剩余电量(%)",0x0608,null),
    OBD_INFO_23("电池电压(V)",0x01F0,null),
    OBD_INFO_24("发/电动机运行时间(h)",0x0645,null),
    OBD_INFO_25("尿素液位(mm)",0x0629,null);

    private String name;
    private Integer id;
    private Map<Long,String> description;

    public String getName() {
        return name;
    }

    public Integer getId() {
        return id;
    }

    public Map<Long, String> getDescription() {
        return description;
    }

    OBDDataStreamEnum(String name, Integer id, Map<Long, String> description) {
        this.name = name;
        this.id = id;
        this.description = description;
    }

    public static Map<Long,String> getOpenOrCloseStatus() {
        Map<Long,String> map = new HashMap<>();
        map.put(0L,"关闭");
        map.put(1L,"打开");
        return map;
    }

    public static Map<Long,String> getBarkingStatus() {
        Map<Long,String> map = new HashMap<>();
        map.put(0L,"未制动");
        map.put(1L,"制动");
        map.put(2L,"不合理");
        map.put(3L,"不合理");
        return map;
    }

    public static Map<Long,String> getSafetyBeltStatus() {
        Map<Long,String> map = new HashMap<>();
        map.put(0L,"未扣");
        map.put(1L,"扣");
        map.put(2L,"不合理");
        map.put(3L,"不合理");
        return map;
    }

    public static Map<Long,String> getEnergyTypesStatus() {
        Map<Long,String> map = new HashMap<>();
        map.put(0L,"汽油");
        map.put(1L,"柴油");
        map.put(3L,"LNG");
        map.put(4L,"CNG");
        map.put(5L,"电动");
        return map;
    }

    public static Map<String,String> getResult(Integer id, Long status) {
        OBDDataStreamEnum[] values = OBDDataStreamEnum.values();
        Map<String,String> resultMap = new HashMap<>();
        for (OBDDataStreamEnum value : values) {
            if (value.getId().intValue() == id) {
                resultMap.put("name",value.getName());
                Map<Long, String> description = value.getDescription();
                if(description != null) {
                    String statusIsValue = description.get(status);
                    resultMap.put("status",statusIsValue);
                } else {
                    resultMap.put("status",String.valueOf(status));
                }
                return resultMap;
            }
        }
        return null;
    }
}
