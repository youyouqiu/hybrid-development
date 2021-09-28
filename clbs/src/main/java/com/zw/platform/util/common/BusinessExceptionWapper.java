package com.zw.platform.util.common;


import org.jdom2.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BusinessExceptionWapper
{
  private static String propFileName = "exceptions.xml";
  private Map<String, BusinessException> businessExceptionsMap;

  public static BusinessExceptionWapper getBusinessExceptionWapper()
  {
    return Singleton.INSTANCE;
  }

  public Map<String, BusinessException> getAllExceptionInfos()
  {
    if (this.businessExceptionsMap == null) {
      String filePath = BusinessExceptionWapper.class.getClassLoader().getResource("").getPath() + propFileName;
      this.businessExceptionsMap = new HashMap();
      Element exceptionsElement = XmlUtil.buildFromFile(filePath).getRootElement();
      List<Element> exceptionElements = exceptionsElement.getChildren();
      if ((exceptionElements != null) && (exceptionElements.size() > 0))
        for (Element exceptionElement : exceptionElements) {
          String code = exceptionElement.getChildText("code");
          BusinessException businessException = new BusinessException();
          businessException.setCode(exceptionElement.getChildText("code"));
          businessException.setType(exceptionElement.getChildText("type"));
          businessException.setDetailMsg(exceptionElement.getChildText("detailMsg"));
          businessException.setSuggestionMsg(exceptionElement.getChildText("suggestionMsg"));
          this.businessExceptionsMap.put(code, businessException);
        }
    }

    return this.businessExceptionsMap;
  }

  public static BusinessException getBusinessException(String code)
  {
    Map businessExceptionsMap = getBusinessExceptionWapper().getAllExceptionInfos();
    BusinessException exception = (BusinessException)businessExceptionsMap.get(code);

    if (exception == null)
      exception = new BusinessException();

    return exception;
  }

  private static abstract interface Singleton
  {
    public static final BusinessExceptionWapper INSTANCE = new BusinessExceptionWapper();
  }
}