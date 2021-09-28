package com.zw.platform.basic.constant;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.zw.platform.domain.basicinfo.form.OBDVehicleDataInfo.Fields.*;

/**
 * OBD常量
 * <p>相关业务中用到的id、字段名、显示名必须引用自本枚举类，禁止使用魔法变量
 *
 * @author Zhang Yanhui
 * @since 2021/5/18 14:01
 */

@Slf4j
@Getter
public enum ObdEnum {
    /**
     * 枚举名请一律保持形如"_0x1234"，注意在{@link ObdEnum.DisplayName}中增加对应的列名（显示名称）
     * <p>若要使用按desc自动翻译，请保持格式形如："[可选]任意不含分号文本，int：状态1；int：状态2"，注意中文冒号和中文分号
     */
    _0x0008(0, true, true, obdIndicatorLampStatus, "", "0：关闭；1：打开"),
    _0x0015(2, true, true, obdFootBrakeStatus, "", "0：未制动；1：制动"),
    _0x0180(0, true, true, obdLeftFrontDoorStatus, "", "0：关闭；1：打开"),
    _0x0181(0, true, true, obdLeftFrontDoorLock, "", "0：未锁；1：锁"),
    _0x0188(0, true, true, obdRightFrontDoorStatus, "", "0：关闭；1：打开"),
    _0x0189(0, true, true, obdRightFrontDoorLock, "", "0：未锁；1：锁"),
    _0x0190(0, true, true, obdLeftRearDoorStatus, "", "0：关闭；1：打开"),
    _0x0191(0, true, true, obdLeftRearDoorLock, "", "0：未锁；1：锁"),
    _0x0198(0, true, true, obdRightRearDoorStatus, "", "0：关闭；1：打开"),
    _0x0199(0, true, true, obdRightRearDoorLock, "", "0：未锁；1：锁"),
    _0x01B0(0, true, true, obdLeftFrontWindowStatus, "", "0：关闭；1：打开"),
    _0x01B8(0, true, true, obdRightFrontWindowStatus, "", "0：关闭；1：打开"),
    _0x01C0(0, true, true, obdLeftRearWindowStatus, "", "0：关闭；1：打开"),
    _0x01C8(0, true, true, obdRightRearWindowStatus, "", "0：关闭；1：打开"),
    _0x01E0(0, true, true, obdTailBoxDoorStatus, "", "0：关闭；1：打开"),
    _0x01F0(2, true, true, obdBatteryVoltage, "V", ""),
    _0x0240(0, true, true, obdSafetyAirBagStatus, "", "0：正常；1：打开"),
    _0x0290(2, true, true, obdTotalMileage, "Km", ""),
    _0x0293(2, true, true, obdOilPressure, "kPa", ""),
    _0x0295(0, true, true, obdFaultSignalABS, "", "0：正常；1：故障"),
    _0x029A(0, true, true, obdAlarmSignalEngineOil, "", "0：正常；1：故障"),
    _0x029D(0, true, true, obdFaultSignalSRS, "", "0：正常；1：故障"),
    _0x02A1(0, true, true, obdFaultSignalECM, "", "0：正常；1：故障"),
    _0x02A5(0, true, true, obdFogLampStatus, "", "0：关闭；1：打开"),
    _0x02AA(0, true, true, obdAlarmSignalMaintain, "", "0：正常；1：故障"),
    _0x02C0(2, true, true, obdSafetyBeltStatusDriver, "", "0：未扣；1：扣"),
    _0x02C4(0, true, true, obdSafetyBeltStatusDeputyDriving, "", "0：未扣；1：扣"),
    _0x0300(2, true, true, obdRotationRate, "rpm", ""),
    _0x0303(2, true, true, obdEngineIntakeTemperature, "℃", ""),
    _0x0305(2, true, true, obdWaterTemperature, "℃", ""),
    _0x030B(2, true, true, obdInstrumentSpeed, "km/h", ""),
    _0x0342(0, true, true, obdKeyStatus, "", "0：未按；1：开锁；2：关锁；3：尾箱锁；4：长按开锁；5：长按关锁"),
    _0x0350(0, true, true, obdSteeringWheelAngle, "度", ""),
    _0x0351(0, true, true, obdSteeringWheelAngleStatus, "", "0:无效；1：左；2：中；3：右"),
    _0x0360(2, true, true, obdHandBrakeStatus, "", "0：未制动；1：制动"),
    _0x0370(2, true, true, obdAirConditionerStatus, "", "0：关闭；1：打开"),
    _0x0373(0, true, true, obdAirConditioningTemperature, "℃", ""),
    _0x040D(2, true, true, obdPercentageOfOil, "%", ""),
    _0x040F(2, true, true, obdAverage100KmOilConsumption, "L/100Km", ""),
    _0x0411(1, true, true, obdFuelInjectionQuantity, "ml/s", ""),
    _0x041E(2, true, true, obdAirFlowRate, "g/s", ""),
    _0x041F(2, true, true, obdIntakePressure, "kPa", ""),
    _0x0508(0, true, true, obdAlarmSignalTirePressure, "", "0：正常；1：故障"),
    _0x0509(0, true, true, obdLeftTurnLampStatus, "", "0：关闭；1：打开"),
    _0x050A(0, true, true, obdRightTurnLampStatus, "", "0：关闭；1：打开"),
    _0x050B(0, true, true, obdEmergencyLampStatus, "", "0：关闭；1：打开"),
    _0x050C(0, true, true, obdFullVehicleLock, "", "0：未锁；1：锁"),
    _0x050E(2, true, true, obdACCSignal, "", "0：关闭；1：打开"),
    _0x0510(2, true, true, obdWiperStatus, "", "0：关闭；1：打开"),
    _0x0511(0, true, true, obdShortDistanceMileage, "Km", "一次行程中的行驶总里程"),
    _0x0512(2, true, true, obdTotalOilConsumption, "L", ""),
    _0x0513(2, true, true, obdInstantOilConsumption, "L/h", ""),
    _0x0514(2, true, true, obdInstant100KmOilConsumption, "L/100Km", ""),
    _0x0515(2, true, true, obdRelativePositionOfThrottlePedal, "%", ""),
    _0x0516(2, true, true, obdAcceleratorPedal, "", "0：未踩；1：踩下"),
    _0x0517(2, true, true, obdOilQuantity, "L", ""),
    _0x051A(2, true, true, obdAccumulatedMileage, "Km", "设备用车速计算的累计总里程"),
    _0x0600(1, true, true, obdVehicleTravelFuelConsumption, "L", ""),
    _0x0601(1, true, true, obdEngineLoad, "%", ""),
    _0x0602(2, true, true, obdClutchStatus, "", "0：未踩；1：踩下"),
    _0x0603(1, true, true, obdNumberOfClutchesDuringTravel, "次", ""),
    _0x0604(1, true, true, obdNumberOfFootBrakesDuringTravel, "次", ""),
    _0x0605(1, true, true, obdNumberOfHandBrakesDuringTravel, "次", ""),
    _0x0608(1, true, true, obdBatteryRemainingElectricity, "%", ""),
    _0x0609(2, true, true, obdMotorTemperature, "℃", ""),
    _0x0610(2, true, true, obdControllerTemperature, "℃", ""),
    _0x0623(2, true, true, obdEnergyType, "", "1:汽油；2:c 柴油；3:LNG；4:CNG；5:电动"),
    _0x0624(1, true, true, obdTorque, "N·m", ""),
    _0x0626(1, true, true, obdFrontOxygenSensorValue, "mV", "氧浓度电压值取值范围：0-999mV "),
    _0x0627(1, true, true, obdRearOxygenSensorValue, "mV", "氧浓度电压值取值范围：0-999mV "),
    _0x0628(2, true, true, obdTernaryCatalystTemperature, "℃", ""),
    _0x0629(1, true, true, obdUreaLevel, "%", ""),
    _0x0631(1, true, true, obdNOxConcentrationRange, "ppm", "取值范围：0-100000"),
    _0x0632(1, true, true, obdMILFaultLamp, "", "0：正常；1：故障；其他值：无效"),
    _0x0633(2, true, true, obdOilTankLevelHeight, "mm", ""),
    _0x0636(2, true, true, obdEngineOilTemperature, "℃", ""),
    _0x0639(2, true, true, obdSpeedByRotationalSpeedCalculation, "km/h", ""),
    _0x0641(1, true, true, obdTorquePercentage, "%", ""),
    _0x0642(1, true, true, obdAtmosphericPressure, "kPa", ""),
    _0x0643(2, true, true, obdFuelTemperature, "℃", ""),
    _0x0644(2, true, true, obdSuperchargedAirTemperature, "℃", ""),
    _0x0645(1, true, true, obdEngineRunningTime, "h", ""),
    _0x0646(1, true, true, obdSmallLampStatus, "", "0：关闭；1：打开"),
    _0x0647(2, true, true, obdHighBeamStatus, "", "0：关闭；1：打开"),
    _0x0648(2, true, true, obdDippedHeadlightStatus, "", "0：关闭；1：打开"),
    _0x0700(2, true, false, obdVin, "", ""),
    _0xF000(2, true, true, obdEngineFuelFlow, "L/h", "数据范围：0~3212.75L/h"),
    _0xF001(2, true, true, obdScrUpNoxOutput, "ppm", "范围：-200~3212.75ppm"),
    _0xF002(2, true, true, obdScrDownNoxOutput, "ppm", "范围：-200~3212.75ppm"),
    _0xF003(2, true, true, obdIntakeVolume, "kg/h", "范围：0~3212.75ppm"),
    _0xF004(2, true, true, obdScrInletTemperature, "℃", "范围：-273~1734.96875℃"),
    _0xF005(2, true, true, obdScrOutletTemperature, "℃", "范围：-273~1734.96875℃"),
    _0xF006(2, true, true, obdDpfDifferentialPressure, "kPa", "范围：0~6425.5 kPa"),
    _0xF007(2, true, true, obdEngineCoolantTemperature, "℃", "范围：-40~210℃"),
    _0xF008(2, true, true, obdFrictionTorque, "%", "范围：-125%~125%"),
    _0xF009(2, true, true, obdEngineTorqueMode, "", "0：超速失效；1：转速控制；2：扭矩控制；3：转速/扭矩控制；9：正常"),
    _0xF00A(2, true, true, obdUreaTankTemperature, "℃", "范围：-40~210℃"),
    _0xF00B(2, true, true, obdActualUreaInjection, "ml/h", ""),
    _0xF00C(2, true, true, obdCumulativeUreaConsumption, "g", ""),
    _0xF00D(2, true, true, obdDpfExhaustTemperature, "℃", "范围：-273~1734.96875℃"),
    _0xF00E(2, true, true, obdDiagnostic, "", "有效范围 0~2，0x00：IOS15765；0x01：IOS27145；0x02：SAEJ1939；0xFE：表示无效"),
    @SuppressWarnings("checkstyle:LineLength")
    _0xF00F(2, true, true, obdDiagnosticSupportState, "", "1 Catalyst monitoring Status 催化转化器监控；2 Heated catalyst monitoring Status 加热催化转化器监控；3 Evaporative system monitoring Status 蒸发系统监控；4 Secondary air system monitoring Status 二次空气系统监控；5 A/C system refrigerant monitoring Status A/C 系统制冷剂监控；6 Exhaust Gas Sensor monitoring Status 排气传感器加热器监控；7 Exhaust Gas Sensor heater monitoring Status 排气传感器加热器监控；8 EGR/VVT system monitoring EGR 系统和 VVT监控；9 Cold start aid system monitoring Status 冷启动辅助系统监控；10 Boost pressure control system monitoring Status增压压力控制系统；11 Diesel Particulate Filter（DPF）monitoring Status DPF 监控；12 NOx converting catalyst and/or NOx adsorber monitoring Status 选择性催化还原系统（SCR）或NOx 吸附器；13 NMHC converting catalyst monitoring Status NMHC 氧化催化器监控；14 Misfire monitoring support 失火监控；15 Fuel system monitoring support 燃油系统监控；16 Comprehensive component monitoring support综合成分监控；每一位的含义：0=不支持；1=支持"),
    @SuppressWarnings("checkstyle:LineLength")
    _0xF010(2, true, true, obdDiagnosticReadyState, "", "1 Catalyst monitoring Status 催化转化器监控；2 Heated catalyst monitoring Status 加热催化转化器监控；3 Evaporative system monitoring Status 蒸发系统监控；4 Secondary air system monitoring Status 二次空气系统监控；5 A/C system refrigerant monitoring Status A/C 系统制冷剂监控；6 Exhaust Gas Sensor monitoring Status 排气传感器加热器监控；7 Exhaust Gas Sensor heater monitoring Status 排气传感器加热器监控；8 EGR/VVT system monitoring EGR 系统和 VVT监控；9 Cold start aid system monitoring Status 冷启动辅助系统监控；10 Boost pressure control system monitoring Status增压压力控制系统；11 Diesel Particulate Filter（DPF）monitoring Status DPF 监控；12 NOx converting catalyst and/or NOx adsorber monitoring Status 选择性催化还原系统（SCR）或NOx 吸附器；13 NMHC converting catalyst monitoring Status NMHC 氧化催化器监控；14 Misfire monitoring support 失火监控；15 Fuel system monitoring support 燃油系统监控；16 Comprehensive component monitoring support综合成分监控；每一位的含义：0=测试完成或者不支持；1=测试未完成"),
    _0xF011(2, true, false, obdVersion, "", "软件标定识别号由生产企业自定义，字母或数字组成"),
    _0xF012(2, true, false, obdCvn, "", "标定验证码由生产企业自定义，字母或数字组成"),
    _0xF013(2, true, false, obdIupr, "", "定义参考 SAE J 1979-DA 表 G11 "),
    _0xF014(2, true, false, obdTroubleCodes, "", "每个故障码为四字节，可按故障实际顺序进行排序"),
    _0xF0141(2, true, true, obdTroubleCodeNum, "", "有效值范围：0~253，“0xFE”表示无效"),
    _0xF0FF(1, true, false, obdAlarmInfo, "", ""),
    ;

    /**
     * 按id查询枚举
     *
     * @param id id
     * @return ObdEnum optional
     */
    public static Optional<ObdEnum> of(Integer id) {
        return Optional.ofNullable(Cache.resolveEnum(id));
    }

    /**
     * 设置属性
     * <p>调用前<b>注意obj的类型</b>，本方法会懒加载生成obj对应class属性的缓存
     */
    private static void set(@NonNull Object obj, Integer id, String value) {
        final Map<Integer, Field> fieldMap = Cache.resolveFields(obj.getClass());
        Optional.ofNullable(fieldMap.get(id)).ifPresent(field -> {
            try {
                field.set(obj, value);
            } catch (IllegalAccessException e) {
                log.error("设置属性出错", e);
            }
        });
    }

    /**
     * 获取属性
     */
    private static Optional<String> get(@NonNull Object obj, Integer id) {
        final Map<Integer, Field> fieldMap = Cache.resolveFields(obj.getClass());
        return Optional.ofNullable(fieldMap.get(id)).map(field -> {
            try {
                return (String) field.get(obj);
            } catch (IllegalAccessException e) {
                log.error("获取属性出错", e);
                return null;
            }
        });
    }

    ObdEnum(int type, boolean showByDefault, boolean numeric, @NonNull String columnName, @NonNull String unit,
            @NonNull String desc) {
        this.id = resolveId();
        this.columnName = columnName;
        this.displayName = resolveDisplayName();
        this.unit = unit;
        this.desc = desc;
        this.type = type;
        this.numeric = numeric;
        this.showByDefault = showByDefault;
    }

    /**
     * 如果枚举名不能解析则编译不过，可以实现强制去重
     */
    private Integer resolveId() {
        return Integer.decode(name().substring(1));
    }

    /**
     * 从常量类DisplayName解析展示名称，缺省时抛出异常中止启动
     */
    private String resolveDisplayName() {
        try {
            return (String) DisplayName.class.getDeclaredField(name()).get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("未找到OBD[id=" + name().substring(1) + "]的展示名称，请在DisplayName补充常量");
        }
    }

    /**
     * 数据流ID
     */
    private final Integer id;

    /**
     * 字段名称
     */
    private final String columnName;

    /**
     * 数据流名称
     */
    private final String displayName;

    /**
     * 单位
     */
    private final String unit;

    /**
     * 描述
     */
    private final String desc;

    /**
     * 类型 0:乘用车; 1:商用车; 2:无;
     */
    private final int type;

    /**
     * 值是否是数值类型（数值类型会自动处理小数点）
     */
    private final boolean numeric;

    /**
     * 是否是默认展示列 true:默认展示; false:默认不展示;
     */
    private final boolean showByDefault;

    /**
     * 设置属性
     */
    public void set(@NonNull Object obj, String value) {
        set(obj, id, value);
    }

    /**
     * 获取属性
     * <p>调用前<b>注意obj的类型</b>，本方法会懒加载生成obj对应class属性的缓存
     */
    public Optional<String> get(@NonNull Object obj) {
        return get(obj, id);
    }

    /**
     * 按照的删除解析值
     * @param value raw value
     * @return parsed value or null as fallback
     */
    public String parseValue(String value) {
        final Map<Integer, String> descMap = Cache.resolveDesc(id);
        if (descMap.isEmpty()) {
            return value;
        }
        final int intValue;
        try {
            intValue = Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            log.error("解析OBD字段出错，id：[{}]，值：[{}]", id, value);
            return value;
        }
        final String parsedValue = descMap.get(intValue);
        if (null == parsedValue) {
            log.error("解析OBD字段出错，id：[{}]，值：[{}]", id, value);
            return "";
        } else {
            return parsedValue;
        }
    }

    /**
     * 缓存ObdEnum结构相关的计算结果，生成缓存的代码也在此处，均为永久缓存
     */
    private static class Cache {

        /**
         * class -> id -> field
         * <p>由于写时加了synchronize保证不会并发写，所以没有死锁问题，懒加载更倾向于使用最轻量的HashMap
         */
        private static final Map<Class<?>, Map<Integer, Field>> FIELD_CACHE = new HashMap<>();

        /**
         * id -> 值(int类型) -> 翻译后的值
         */
        private static final Map<Integer, Map<Integer, String>> DESC_CACHE = new HashMap<>();

        /**
         * id -> ObdEnum
         */
        private static final Map<Integer, ObdEnum> ID_ENUM_MAP;

        static {
            final ObdEnum[] enums = ObdEnum.values();
            final Map<Integer, ObdEnum> map = new HashMap<>((int) (enums.length / .075 + 1));
            for (ObdEnum obdEnum : enums) {
                map.put(obdEnum.getId(), obdEnum);
            }
            ID_ENUM_MAP = map;
        }

        /**
         * 获取指定class的id -> field的映射
         */
        static Map<Integer, Field> resolveFields(Class<?> klass) {
            final Map<Integer, Field> fieldMap = FIELD_CACHE.get(klass);
            if (null == fieldMap) {
                synchronized (FIELD_CACHE) {
                    return FIELD_CACHE.computeIfAbsent(klass, Cache::generateFieldMap);
                }
            }
            return fieldMap;
        }

        /**
         * 生成指定class的id -> field的映射
         */
        private static Map<Integer, Field> generateFieldMap(Class<?> klass) {
            final Map<Integer, Field> generatedFieldMap = new HashMap<>(128);
            while (klass != null) {
                final Field[] fields = klass.getDeclaredFields();
                // 列名重复时报错
                final Map<String, Integer> columnNameIdMap = Arrays.stream(ObdEnum.values())
                        .collect(Collectors.toMap(ObdEnum::getColumnName, ObdEnum::getId));
                for (Field field : fields) {
                    final Integer id = columnNameIdMap.get(field.getName());
                    if (null != id) {
                        field.setAccessible(true);
                        generatedFieldMap.putIfAbsent(id, field);
                    }
                }
                klass = klass.getSuperclass();
            }
            return generatedFieldMap;
        }

        /**
         * 获取指定id的值的翻译map
         */
        static Map<Integer, String> resolveDesc(Integer id) {
            final Map<Integer, String> descMap = DESC_CACHE.get(id);
            if (null == descMap) {
                synchronized (DESC_CACHE) {
                    return DESC_CACHE.computeIfAbsent(id, Cache::generateDescMap);
                }
            }
            return descMap;
        }

        /**
         * 生成指定id的值的翻译map
         */
        private static Map<Integer, String> generateDescMap(Integer id) {
            final ObdEnum obdEnum = resolveEnum(id);
            if (null == obdEnum) {
                return Collections.emptyMap();
            }
            if (StringUtils.isNotEmpty(obdEnum.getUnit())) {
                return Collections.emptyMap();
            }
            // 按分号分隔键值对，再按冒号分隔k和v
            final String pairSeparator = "；";
            final String kvSeparator = "：";
            final String desc = obdEnum.getDesc();
            final Map<Integer, String> descMap = new HashMap<>(16);
            for (String part : desc.split(pairSeparator)) {
                final int separatorIndex = part.lastIndexOf(kvSeparator);
                if (separatorIndex > 0) {
                    String keyStr = part.substring(0, separatorIndex);
                    final String value = part.substring(separatorIndex + 1);
                    final int commentIndex = keyStr.lastIndexOf("，");
                    if (commentIndex > 0) {
                        keyStr = keyStr.substring(commentIndex + 1);
                    }
                    final Integer key;
                    try {
                        key = Integer.decode(keyStr);
                    } catch (NumberFormatException ignored) {
                        continue;
                    }
                    descMap.put(key, value);
                }
            }
            return descMap.isEmpty() ? Collections.emptyMap() : descMap;
        }

        /**
         * id 查询 enum
         */
        static ObdEnum resolveEnum(Integer id) {
            return ID_ENUM_MAP.get(id);
        }
    }

    /**
     * OBD字段显示名称，因excel要用，所以必须声明为常量
     */
    public static class DisplayName {
        public static final String _0x0008 = "示宽灯状态";
        public static final String _0x0015 = "刹车状态(脚刹)";
        public static final String _0x0180 = "左前门状态";
        public static final String _0x0181 = "左前门锁";
        public static final String _0x0188 = "右前门状态";
        public static final String _0x0189 = "右前门锁";
        public static final String _0x0190 = "左后门状态";
        public static final String _0x0191 = "左后门锁";
        public static final String _0x0198 = "右后门状态";
        public static final String _0x0199 = "右后门锁";
        public static final String _0x01B0 = "左前窗状态";
        public static final String _0x01B8 = "右前窗状态";
        public static final String _0x01C0 = "左后窗状态";
        public static final String _0x01C8 = "右后窗状态";
        public static final String _0x01E0 = "尾箱门状态";
        public static final String _0x01F0 = "电池电压(V)";
        public static final String _0x0240 = "安全气囊状态";
        public static final String _0x0290 = "仪表总里程(Km)";
        public static final String _0x0293 = "机油压力(kPa)";
        public static final String _0x0295 = "故障信号(ABS)";
        public static final String _0x029A = "报警信号(机油)";
        public static final String _0x029D = "故障信号(SRS)";
        public static final String _0x02A1 = "故障信号(ECM)";
        public static final String _0x02A5 = "雾灯状态";
        public static final String _0x02AA = "报警信号(保养)";
        public static final String _0x02C0 = "安全带(驾驶员)";
        public static final String _0x02C4 = "安全带(副驾)";
        public static final String _0x0300 = "转速(rpm)";
        public static final String _0x0303 = "发动机进气温度(℃)";
        public static final String _0x0305 = "水温(℃)";
        public static final String _0x030B = "仪表车速(km/h)";
        public static final String _0x0342 = "钥匙状态";
        public static final String _0x0350 = "方向盘转角角度(度)";
        public static final String _0x0351 = "方向盘转角状态";
        public static final String _0x0360 = "刹车状态(手刹)";
        public static final String _0x0370 = "空调开关";
        public static final String _0x0373 = "车内空调温度(℃)";
        public static final String _0x040D = "油量百分比(%)";
        public static final String _0x040F = "平均百公里油耗(L/100Km)";
        public static final String _0x0411 = "喷油量(ml/s)";
        public static final String _0x041E = "空气流量(g/s)";
        public static final String _0x041F = "进气压力(kPa)";
        public static final String _0x0508 = "报警信号(胎压)";
        public static final String _0x0509 = "左转向灯状态";
        public static final String _0x050A = "右转向灯状态";
        public static final String _0x050B = "应急灯状态";
        public static final String _0x050C = "全车锁";
        public static final String _0x050E = "ACC信号";
        public static final String _0x0510 = "雨刮状态";
        public static final String _0x0511 = "仪表记录的短途行驶里程(Km)";
        public static final String _0x0512 = "累计总油耗(L)";
        public static final String _0x0513 = "瞬时油耗(L/h)";
        public static final String _0x0514 = "瞬时百公里油耗(L/100Km)";
        public static final String _0x0515 = "油门踏板相对位置(%)";
        public static final String _0x0516 = "油门踏板";
        public static final String _0x0517 = "车辆油箱油量(L)";
        public static final String _0x051A = "累计里程(Km)";
        public static final String _0x0600 = "车辆行程耗油量(L)";
        public static final String _0x0601 = "发动机负荷(%)";
        public static final String _0x0602 = "离合状态";
        public static final String _0x0603 = "行程内离合次数(次)";
        public static final String _0x0604 = "行程内脚刹次数(次)";
        public static final String _0x0605 = "行程内手刹次数(次)";
        public static final String _0x0608 = "电池剩余电量(%)";
        public static final String _0x0609 = "电机温度(℃)";
        public static final String _0x0610 = "控制器温度(℃)";
        public static final String _0x0623 = "能源类型";
        public static final String _0x0624 = "扭矩(N·m)";
        public static final String _0x0626 = "前氧传感器示值";
        public static final String _0x0627 = "后氧传感器示值";
        public static final String _0x0628 = "三元催化器温度(℃)";
        public static final String _0x0629 = "尿素液位(%)";
        public static final String _0x0631 = "NOx浓度值范围";
        public static final String _0x0632 = "OBD状态(MIL故障灯)";
        public static final String _0x0633 = "油箱液位高度(mm)";
        public static final String _0x0636 = "机油温度(℃)";
        public static final String _0x0639 = "转速计算车速(Km/h)";
        public static final String _0x0641 = "扭矩百分比(%)";
        public static final String _0x0642 = "大气压力(kPa)";
        public static final String _0x0643 = "燃油温度(℃)";
        public static final String _0x0644 = "增压空气温度(℃)";
        public static final String _0x0645 = "发/电动机运行时间(h)";
        public static final String _0x0646 = "小灯状态";
        public static final String _0x0647 = "远光灯状态";
        public static final String _0x0648 = "近光灯状态";
        public static final String _0x0700 = "车辆识别码(VIN码)";
        public static final String _0xF000 = "发动机燃料流量(L/h)";
        public static final String _0xF001 = "SCR上游NOx传感器输出值(ppm)";
        public static final String _0xF002 = "SCR下游NOx传感器输出值(ppm)";
        public static final String _0xF003 = "进气量(kg/h)";
        public static final String _0xF004 = "SCR 入口温度(℃)";
        public static final String _0xF005 = "SCR 出口温度(℃)";
        public static final String _0xF006 = "DPF压差(kPa)";
        public static final String _0xF007 = "发动机冷却液温度(℃)";
        public static final String _0xF008 = "摩擦扭矩百分比(%)";
        public static final String _0xF009 = "发动机扭矩模式";
        public static final String _0xF00A = "尿素箱温度(℃)";
        public static final String _0xF00B = "实际尿素喷射量(ml/h)";
        public static final String _0xF00C = "累计尿素消耗(g)";
        public static final String _0xF00D = "DPF排气温度(℃)";
        public static final String _0xF00E = "OBD诊断协议";
        public static final String _0xF00F = "诊断支持状态";
        public static final String _0xF010 = "诊断就绪状态";
        public static final String _0xF011 = "软件标定识别号";
        public static final String _0xF012 = "标定验证码(CVN)";
        public static final String _0xF013 = "IUPR值";
        public static final String _0xF0141 = "故障码总数";
        public static final String _0xF014 = "故障码信息列表";
        public static final String _0xF0FF = "报警信息";
    }

}
