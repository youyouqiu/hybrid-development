package com.zw.platform.controller.common;

import com.zw.platform.util.AccountLocker;
import com.zw.platform.util.spring.GlobalRtLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 用于debug
 *
 * @author Zhang Yanhui
 * @since 2021/1/22 16:07
 */

@Slf4j
@Controller
@RequestMapping("debug")
public class DebugController {
    @Autowired
    private AccountLocker accountLocker;

    /**
     * 设置接口耗时INFO日志
     */
    @RequestMapping(value = "method-rt/{duration}", method = RequestMethod.POST)
    @ResponseBody
    public String setMethodRtRecordDuration(@PathVariable("duration") String duration) {
        try {
            if (!StringUtils.isNumeric(duration)) {
                return "用法：输入分钟数，即可开启接口耗时INFO日志，持续x分钟后自动关闭，输入0可立即关闭";
            }
            final long inputDurationMinutes = Long.parseLong(duration);
            final long msPerMin = 60_000L;
            final long maxAllowed = Long.MAX_VALUE - msPerMin - System.currentTimeMillis();
            final long actualDuration = Math.min(maxAllowed, inputDurationMinutes * msPerMin);
            GlobalRtLogger.setStopwatch(actualDuration);
            return actualDuration == 0 ? "已关闭方法耗时日志" : "将在" + actualDuration / msPerMin + "分钟后关闭方法耗时日志";
        } catch (Exception e) {
            log.error("设置接口耗时INFO日志异常", e);
            return "设置失败";
        }
    }

    @RequestMapping(value = "log-level", method = RequestMethod.POST)
    @ResponseBody
    public String changeLogLevel(@RequestParam("logger") String logger, @RequestParam("level") String level) {
        Level logLevel = Level.toLevel(level, Level.INFO);
        Configurator.setLevel(logger, logLevel);
        return "日志级别成功修改为：" + logLevel.name();
    }

    @RequestMapping(value = "reset-login-fail/{username}", method = RequestMethod.POST)
    @ResponseBody
    public String resetLoginFail(@PathVariable("username") String username) {
        accountLocker.reset(username);
        return "重置用户" + username + "登录失败次数成功";
    }
}
