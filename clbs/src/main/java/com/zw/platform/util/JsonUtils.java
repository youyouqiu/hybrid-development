package com.zw.platform.util;

import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Json局部读写工具类
 * <p>直接操作字符串，节省parse开销，节省堆内存</p>
 *
 * @author Zhang Yanhui
 * @implNote 不校验入参JSON合法性
 * @since 2019/10/14 15:12
 */

public final class JsonUtils {

    private JsonUtils() {
    }

    /**
     * Stream API Support
     *
     * @param collection 源集合
     * @param mapper 集合元素转换json方法
     * @param fieldName 属性名
     * @param valueType 属性值类型
     * @param <T>       属性值类型
     * @param <U> 源类型
     * @return 属性值stream
     */
    public static <T, U> Stream<T> streamOf(Collection<U> collection, Function<? super U, ? extends String> mapper,
                                            String fieldName, Class<T> valueType) {
        final List<String> jsons = collection.stream().map(mapper).collect(Collectors.toList());
        return getValues(jsons, fieldName, valueType).stream();
    }

    /**
     * Stream API Support
     *
     * @param jsons json集合
     * @param fieldName 属性名
     * @param valueType 属性值类型
     * @param <T>       属性值类型
     * @return 属性值stream
     */
    public static <T> Stream<T> streamOf(Collection<String> jsons, String fieldName, Class<T> valueType) {
        return getValues(jsons, fieldName, valueType).stream();
    }

    /**
     * 批量获取json单个属性值
     *
     * @param jsons     json集合
     * @param fieldName 属性名
     * @param valueType 属性值类型
     * @param <T>       属性值类型
     * @return 属性值列表
     */
    public static <T> List<T> getValues(Collection<String> jsons, String fieldName, Class<T> valueType) {
        final ValueFormatter formatter = ValueFormatter.of(valueType);
        final Pattern pattern = buildPattern(fieldName, formatter);
        final List<Object> values = jsons.stream()
            .map(json -> null == json ? null : find(formatter, pattern, json))
            .collect(Collectors.toList());
        @SuppressWarnings("unchecked") List<T> result = (List) values;
        return result;
    }

    /**
     * 获取json单个属性值
     *
     * @param json      对象json
     * @param fieldName 属性名
     * @param valueType 属性值类型
     * @param <T>       属性值类型
     * @return 属性值
     */
    public static <T> T getValue(String json, String fieldName, Class<T> valueType) {
        if (null == json) {
            return null;
        }
        final ValueFormatter formatter = ValueFormatter.of(valueType);
        final Pattern pattern = buildPattern(fieldName, formatter);
        final Object value = find(formatter, pattern, json);
        @SuppressWarnings("unchecked") T result = (T) value;
        return result;
    }

    /**
     * 生成正则pattern，其中第一个group为属性值
     *
     * @param fieldName 属性名
     * @param formatter 转化器
     * @return pattern
     */
    private static Pattern buildPattern(String fieldName, ValueFormatter formatter) {
        // 非数值类型，不得包含"，例外情况是前面有奇数个转义符\
        String content = formatter.isNumeric() ? "([^,\\]}]+)" : "(([^\"]|[^\\\\]\\\\(\\\\\\\\)*\")*)";
        return Pattern.compile("\"" + fieldName + "\":" + formatter.getLeft() + content + formatter.getRight()
            + "[,\\]}]");
    }

    private static Object find(ValueFormatter formatter, Pattern pattern, String json) {
        final Matcher matcher = pattern.matcher(json);
        return matcher.find() ? formatter.parse(matcher.group(1)) : null;
    }

    /**
     * 类型转换枚举
     */
    private enum ValueFormatter {
        //
        SHORT(Short.class, true, "", "", Short::valueOf),
        INTEGER(Integer.class, true, "", "", Integer::valueOf),
        LONG(Long.class, true, "", "", Long::valueOf),
        FLOAT(Float.class, true, "", "", Float::valueOf),
        DOUBLE(Double.class, true, "", "", Double::valueOf),
        BOOLEAN(Boolean.class, true, "", "", Boolean::valueOf),
        CHARACTER(Character.class, false, "'", "'", str -> str.charAt(0)),
        STRING(String.class, false, "\"", "\"", Function.identity()),
        OTHER(Object.class, false, "", "", o -> {
            throw new IllegalStateException("类型暂不支持");
        });

        /**
         * 值类型
         */
        private Class<?> clazz;

        /**
         * 是否是数值类型
         */
        @Getter
        private boolean numeric;

        /**
         * 左分隔符
         */
        @Getter
        private String left;

        /**
         * 右分隔符
         */
        @Getter
        private String right;

        /**
         * String转Object方法
         */
        private Function<String, ?> parseFunc;

        <T> ValueFormatter(Class<T> clazz, boolean numeric, String left, String right, Function<String, T> parseFunc) {
            this.clazz = clazz;
            this.numeric = numeric;
            this.left = left;
            this.right = right;
            this.parseFunc = parseFunc;
        }

        public static <T> ValueFormatter of(Class<T> clazz) {
            for (ValueFormatter valueFormatter : values()) {
                if (valueFormatter.clazz.isAssignableFrom(clazz)) {
                    return valueFormatter;
                }
            }
            return OTHER;
        }

        public Object parse(String json) {
            try {
                return parseFunc.apply(json);
            } catch (Exception e) {
                throw new RuntimeException("以下内容转[" + clazz + "]格式失败：" + json);
            }
        }
    }

    public static void main(String[] args) {
        final Integer plateColor = getValue("{\"vehicleColor\":\"0\",\"standard\":0,\"vehicleCategoryId\":"
                + "\"default\",\"phoneCheck\":\"null\","
                + "\"flag\":1,\"assignGroup\":\"505cf07e-4209-1039-9490-fdd228990c45\","
                + "\"vehiclePurpose\":\"65f52144-2e9a-496e-afb9-5853157a4401\","
                + "\"groupId\":\"505cf07e-4209-1039-9490-fdd228990c45\",\"vehType\":\"其他车辆\",\"isStart\":1,"
                + "\"purposeCategory\":\"道路旅客运输\",\"plateColor\":2,\"vehicleCategoryName\":\"其他车辆\","
                + "\"simCardNumber\":\"15800001122\",\"groupName\":\"505cf07e-4209-1039-9490-fdd228990c45\","
                + "\"createDataTime\":1563948327000,\"createDataTimeStr\":\"2019-07-24\","
                + "\"id\":\"e27c771e-be87-4d5d-9f4d-dcc54be072eb\",\"code_num\":\"90\",\"brand\":\"川P01122\","
                + "\"vehicleType\":\"default\",\"assignId\":\"4d9c4062-99ae-4e5e-a8a4-cca9f6711373\","
                + "\"assign\":\"test12\"}", "plateColor", Integer.class);
        System.out.println(plateColor);
        final String groupName = getValue("{\"vehicleColor\":\"0\",\"standard\":0,\"vehicleCategoryId\":"
            + "\"default\",\"phoneCheck\":\"null\","
            + "\"flag\":1,\"assignGroup\":\"505cf07e-4209-1039-9490-fdd228990c45\","
            + "\"vehiclePurpose\":\"65f52144-2e9a-496e-afb9-5853157a4401\","
            + "\"groupId\":\"505cf07e-4209-1039-9490-fdd228990c45\",\"vehType\":\"其他车辆\",\"isStart\":1,"
            + "\"purposeCategory\":\"道路旅客运输\",\"plateColor\":2,\"vehicleCategoryName\":\"其他车辆\","
            + "\"simCardNumber\":\"15800001122\",\"groupName\":\"505cf07e-4\\\\\\\"209-1039-9490-fdd228990c45\\\","
            + "\"createDataTime\":1563948327000,\"createDataTimeStr\":\"2019-07-24\","
            + "\"id\":\"e27c771e-be87-4d5d-9f4d-dcc54be072eb\",\"code_num\":\"90\",\"brand\":\"川P01122\","
            + "\"vehicleType\":\"default\",\"assignId\":\"4d9c4062-99ae-4e5e-a8a4-cca9f6711373\","
            + "\"assign\":\"test12\"}", "groupName", String.class);
        System.out.println(groupName);
        final Character char1 = getValue("{\"vehicleColor\":\"0\",\"standard\":0,\"vehicleCategoryId\":"
            + "\"default\",\"phoneCheck\":\"null\","
            + "\"flag\":1,\"assignGroup\":\"505cf07e-4209-1039-9490-fdd228990c45\","
            + "\"vehiclePurpose\":\"65f52144-2e9a-496e-afb9-5853157a4401\","
            + "\"groupId\":\"505cf07e-4209-1039-9490-fdd228990c45\",\"vehType\":\"其他车辆\",\"isStart\":1,"
            + "\"purposeCategory\":\"道路旅客运输\",\"plateColor\":2,\"vehicleCategoryName\":\"其他车辆\","
            + "\"simCardNumber\":\"15800001122\",\"groupName\":\"505cf07e-4209-1039-9490-fdd228990c45\","
            + "\"createDataTime\":1563948327000,\"createDataTimeStr\":\"2019-07-24\","
            + "\"id\":\"e27c771e-be87-4d5d-9f4d-dcc54be072eb\",\"code_num\":\"90\",\"brand\":\"川P01122\","
            + "\"vehicleType\":\"default\",\"assignId\":\"4d9c4062-99ae-4e5e-a8a4-cca9f6711373\","
            + "\"assign\":\"test12\", \"char1\":'q'}", "char1", Character.class);
        System.out.println(char1);
    }
}
