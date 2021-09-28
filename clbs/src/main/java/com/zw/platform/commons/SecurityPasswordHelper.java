package com.zw.platform.commons;

import com.zw.platform.util.common.PropsUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 使用spring的bcrypt
 */
public final class SecurityPasswordHelper {
    private static final int STRENGTH =
        Integer.parseInt(PropsUtil.getValue("security.passwordEncoderStrength", "application.properties"));

    private SecurityPasswordHelper() {
        throw new Error("工具类不能实例化！");
    }

    /**
     * 将输入的密码进行特殊处理，防止密码轻易被破解
     */
    public static String encodePassword(final String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(STRENGTH);
        return passwordEncoder.encode(password);
    }

    /**
     * 判断输入的密码是否与应用中存储的密码相符合。因为应用中存储的密码是由输入的密码经过特殊处理后生成的， 所以需要我们自己定义如何判断输入的密码和存储的密码的一致性
     */
    public static boolean isPasswordValid(final String encPass, final String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(STRENGTH);
        return passwordEncoder.matches(password, encPass);
    }

}
