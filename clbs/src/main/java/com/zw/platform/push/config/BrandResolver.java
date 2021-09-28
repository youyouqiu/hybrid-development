package com.zw.platform.push.config;

import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.domain.singleVehicle.Brand;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

public class BrandResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return Brand.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        final HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (null != request) {
            String token = request.getHeader("Token");
            if (null != token) {
                String brand = RedisHelper.getString(HistoryRedisKeyEnum.VEHICLE_BRAND_TOKEN.of(token));
                if (StringUtils.isNotBlank(brand)) {
                    return new Brand(brand);
                }
            }
        }
        return new Brand();
    }
}
