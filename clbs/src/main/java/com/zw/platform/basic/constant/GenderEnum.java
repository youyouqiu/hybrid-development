package com.zw.platform.basic.constant;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 性别枚举类型
 *
 * @author zhangjuan
 */
public enum GenderEnum {
    /**
     * 1:男 2：女
     */
    MALE("1", "男"),
    FEMALE("2", "女");

    GenderEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Getter
    private String code;

    @Getter
    private String name;

    public static String getName(String code) {
        return Objects.equals(code, FEMALE.getCode()) ? FEMALE.getName() : MALE.getName();
    }

    public static String getCode(String name) {
        return Objects.equals(name, FEMALE.getName()) ? FEMALE.getCode() : MALE.getCode();
    }

    public static boolean checkGender(String name) {
        if (StringUtils.isBlank(name)) {
            return true;
        }
        for (GenderEnum genderEnum : GenderEnum.values()) {
            if (Objects.equals(name, genderEnum.getName())) {
                return false;
            }
        }
        return false;
    }
}
