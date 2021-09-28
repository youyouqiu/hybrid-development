package com.zw.platform.util.common;


import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import cn.apiclub.captcha.Captcha;
import cn.apiclub.captcha.servlet.CaptchaServletUtil;


public final class CaptchaUtil {
    private static final int WIDTH = 160;

    private static final int HEIGHT = 40;

    private static final String CAPTCHA = "simpleCaptcha";

    private CaptchaUtil() {
        throw new Error("工具类不能实例化！");
    }

    public static void getCaptcha(HttpSession session, HttpServletResponse response) {
        getCaptcha(session, response, WIDTH, HEIGHT);
    }

    public static void getCaptcha(HttpSession session, HttpServletResponse response, int width, int height) {
        Captcha localCaptcha = new Captcha.Builder(width, height).addText().addNoise().build();

        CaptchaServletUtil.writeImage(response, localCaptcha.getImage());

        session.setAttribute(CAPTCHA, localCaptcha);
    }

    public static void resetCaptcha(HttpSession session) {
        session.setAttribute(CAPTCHA, null);
    }

    public static boolean checkCaptcha(HttpSession session, String checkCode) {
        Captcha localCaptcha = (Captcha) session.getAttribute(CAPTCHA);
        if (localCaptcha == null) return false;

        return localCaptcha.isCorrect(checkCode);
    }

    public static boolean checkCaptcha2(HttpSession session, String checkCode) {
        String localCaptcha = (String) session.getAttribute(CAPTCHA);
        if (localCaptcha == null) {
            return false;
        } else {
            session.setAttribute(CAPTCHA, UUID.randomUUID().toString());
            return localCaptcha.equals(checkCode.replaceAll("\"", ""));
        }
    }
}