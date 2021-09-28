package com.zw.app.controller.shortmessage;

import com.zw.app.entity.sm.PhoneNumberEntity;
import com.zw.app.service.shortmessage.SendShortMessageService;
import com.zw.app.util.common.AppResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

import static com.zw.app.util.AppVersionUtil.dealVersionName;

/***
 @Author gfw
 @Date 2018/12/6 11:07
 @Description 短信验证处理
 @version 1.0
 **/
@Controller
@RequestMapping("/app")
public class MessageController {

    @Autowired
    SendShortMessageService sendShortMessageService;

    /**
     * 获取短信验证码
     *
     * @param phoneNumberEntity 电话号码实体
     * @return
     */
    @RequestMapping(value = "/sm/check",method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean sendSm(HttpServletRequest request, @Validated PhoneNumberEntity phoneNumberEntity, BindingResult result) {
        if (result.getAllErrors().size() != 0) {
            return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
        }
        String requestURI = request.getRequestURI();

        String meth = dealVersionName(requestURI,phoneNumberEntity.getVersion());
        Object invoke=null;
        try {
            Method method = sendShortMessageService.getClass().getMethod(meth, String.class);
            invoke = method.invoke(sendShortMessageService,phoneNumberEntity.getPhoneNumber());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (invoke == null) {
            return new AppResultBean(AppResultBean.SERVER_ERROR, "服务出错");
        } else {
            return (AppResultBean) invoke;
        }
    }

}
