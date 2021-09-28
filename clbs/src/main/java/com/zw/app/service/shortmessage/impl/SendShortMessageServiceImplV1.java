package com.zw.app.service.shortmessage.impl;

import com.aliyuncs.exceptions.ClientException;
import com.zw.app.annotation.AppMethodVersion;
import com.zw.app.annotation.AppServerVersion;
import com.zw.app.controller.AppVersionConstant;
import com.zw.app.service.shortmessage.SendShortMessageService;
import com.zw.app.sm.SmComponent;
import com.zw.app.util.common.AppResultBean;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

/***
 @Author gfw
 @Date 2018/12/10 16:56
 @Description 短信发送实现
 @version 1.0
 **/
@Service
@AppServerVersion
public class SendShortMessageServiceImplV1 implements SendShortMessageService {
    @Autowired
    UserService userService;

    @Value("${app.sm.flag}")
    private boolean flag;

    @Autowired
    SmComponent smComponent;

    /**
     * 短信发送
     * @param telephone
     * @throws Exception
     */

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_ONE, url = { "/clbs/app/sm/check" })
    public AppResultBean sendMessage(String telephone) {
        //1.平台账户校验
        UserDTO userDTO = userService.getUserByUsername(telephone);
        if (userDTO != null) {
            return new AppResultBean(AppResultBean.PARAM_ERROR, "该账户未在该平台注册");
        }
        //2.随机数生成
        String rand = createSmCode();
        String templateParam = "{\"code\":" + rand + "}";
        if (flag) {
            try {
                smComponent.sendMessage(templateParam, telephone, "SMS_152511761");
            } catch (ClientException e) {
                if (e.getErrCode().equals("isv.BUSINESS_LIMIT_CONTROL")) {
                    if (e.getMessage().endsWith("1")) {
                        return new AppResultBean(AppResultBean.PARAM_ERROR, "一分钟内最多获取一次验证码");
                    } else if (e.getMessage().endsWith("5")) {
                        return new AppResultBean(AppResultBean.PARAM_ERROR, "您已超过一小时内最多短信验证次数");
                    } else if (e.getMessage().endsWith("10")) {
                        return new AppResultBean(AppResultBean.PARAM_ERROR, "您已超过一天内最多短信验证次数");
                    }
                } else {
                    return new AppResultBean(AppResultBean.PARAM_ERROR, e.getMessage());
                }
            }
        }
        RedisHelper.setString(HistoryRedisKeyEnum.APP_SMS_SEND_CODE.of(telephone), rand, 60);
        if (flag) {
            rand = "发送成功";
        }
        return new AppResultBean(rand);
    }

    /**
     * 生成随机数
     * @return
     */
    private String createSmCode() {
        StringBuilder rand = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if (i == 0) {
                rand.append(1 + new Random().nextInt(9));
            } else {
                rand.append(new Random().nextInt(10));
            }
        }
        return rand.toString();
    }
}
