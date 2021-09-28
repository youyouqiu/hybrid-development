package com.zw.platform.util.jl;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.other.protocol.jl.xml.DataElement;
import com.zw.platform.domain.other.protocol.jl.xml.EtBaseElement;
import com.zw.platform.domain.other.protocol.jl.xml.RequestRootElement;
import com.zw.platform.domain.other.protocol.jl.xml.ResponseRootElement;
import com.zw.platform.domain.other.protocol.jl.xml.SuccessContentElement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.zw.platform.util.jl.JiLinConstant.MSG;
import static com.zw.platform.util.jl.JiLinConstant.RESULT;

/**
 * 吉林企业考核接口工具
 * @author penghj
 * @version 1.0
 * @date 2020/6/12 9:12
 */
@Component
public class JiLinOrgExamineUtil {

    @Value("${jilin.org.examine.address}")
    private String ipAddress;
    @Value("${jilin.org.examine.username}")
    private String username;
    @Value("${jilin.org.examine.password}")
    private String password;

    /**
     * 发起考核请求
     * @param contentList   请求参数的content标签类列表
     * @param baseParamEnum 请求基础参数枚举
     * @param clazz         应答的content标签类型
     * @return ResponseRootElement<T>
     * @throws Exception Exception
     */
    public <T, X> ResponseRootElement<T> sendExamineRequest(List<X> contentList,
        JiLinOrgExamineInterfaceBaseParamEnum baseParamEnum, Class<T> clazz) throws Exception {
        Map<String, String> requestParam = assembleRequestParamMap(contentList, baseParamEnum);
        String responseStr = HttpClientUtil.sendPost(ipAddress, requestParam);
        if (StringUtils.isBlank(responseStr)) {
            throw new Exception("调用吉林企业考核接口异常，type = " + baseParamEnum.getType());
        }
        return xmlStrToResponseBean(responseStr, clazz);
    }

    private <X> Map<String, String> assembleRequestParamMap(List<X> contentList,
        JiLinOrgExamineInterfaceBaseParamEnum baseParamEnum) throws Exception {
        // 组装基础参数
        DataElement<X> dataElement = new DataElement<>();
        dataElement.setType(baseParamEnum.getType());
        dataElement.setTypename(baseParamEnum.getTypename());
        dataElement.setContent(contentList);

        RequestRootElement<X> requestXml = new RequestRootElement<>();
        requestXml.setServer(baseParamEnum.getServer());
        requestXml.setData(dataElement);

        // 请求参数
        Map<String, String> requestParam = new HashMap<>(16);
        requestParam.put("username", username);
        requestParam.put("password", password);
        requestParam.put("requestXml", beanToXmlStr(requestXml));
        return requestParam;
    }

    /**
     * bean转成xml字符串
     */
    private String beanToXmlStr(Object object) throws Exception {
        //获得 JAXBContext 类的新实例。参数为类的地址
        JAXBContext context = JAXBContext.newInstance(object.getClass());
        //创建一个可以用来将 java 内容树转换为 XML 数据的 Marshaller 对象。
        Marshaller marshaller = context.createMarshaller();
        //创建一个StringWriter流将接收到的对象流写入xml字符串
        StringWriter stringWriter = new StringWriter();
        //调用marshal方法进行转换
        marshaller.marshal(object, stringWriter);
        //将读取到的StringWriter流转成String返回
        return stringWriter.toString();
    }

    /**
     * 将XML字符串转为指定的bean
     */
    @SuppressWarnings("unchecked")
    private <T> ResponseRootElement<T> xmlStrToResponseBean(String xmlStr, Class<T> clazz) throws Exception {
        JAXBContext context = JAXBContext.newInstance(ResponseRootElement.class, clazz);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        StringReader reader = new StringReader(xmlStr);
        Object unmarshal = unmarshaller.unmarshal(reader);
        return (ResponseRootElement<T>) unmarshal;
    }

    /**
     * 上传返回参数解析
     * @param response response
     * @return JSONObject
     */
    public static JSONObject upLoadResolverResult(ResponseRootElement<SuccessContentElement> response) {
        final DataElement<SuccessContentElement> data = response.getData();
        EtBaseElement etBase = data.getEtBase();
        // 数据存储
        JSONObject msgResult = new JSONObject();
        msgResult.put(RESULT, JiLinConstant.RESULT_SUCCESS);
        msgResult.put(MSG, StringUtils.EMPTY);

        if (Objects.nonNull(etBase)) {
            msgResult.put(RESULT, JiLinConstant.RESULT_FAULT);
            msgResult.put(MSG, etBase.getMsg());
            return msgResult;
        }

        final List<SuccessContentElement> content = data.getContent();
        if (CollectionUtils.isNotEmpty(content)) {
            final SuccessContentElement successContentElement = content.get(0);
            msgResult.put(RESULT, successContentElement.getResult());
            msgResult.put(MSG, successContentElement.getMsg());
        }
        return msgResult;
    }
}
