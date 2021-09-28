package com.zw.platform.util.spring;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class SpringUTF8StringBeanPostProcessor
  implements BeanPostProcessor
{
  public final Object postProcessAfterInitialization(Object bean, String beanName)
  {
    if (bean instanceof StringHttpMessageConverter) {
      List types = new ArrayList();
      types.add(new MediaType("text", "plain", Charset.forName("UTF-8")));
      ((StringHttpMessageConverter)bean).setSupportedMediaTypes(types);
    }
    return bean;
  }

  public final Object postProcessBeforeInitialization(Object bean, String beanName)
  {
    return bean;
  }
}
