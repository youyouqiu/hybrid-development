package com.cb.platform.util.page;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.util.StrUtil;
import com.zw.talkback.util.common.AssembleFunction;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Data
public class PassCloudResultBean {
    private int code;
    private Object data;
    private String message;

    /**
     * paas结果为查询超时时paas返回的code
     */
    private static final Collection<Integer> PAAS_TIMEOUT_CODES = Arrays.asList(40004, 40005);

    /**
     * 超时出参
     */
    private static final PassCloudResultBean RESULT_TIMEOUT;

    static {
        RESULT_TIMEOUT = new PassCloudResultBean();
        RESULT_TIMEOUT.setMessage("查询超时，请缩小查询范围再试！");
    }

    public static PassCloudResultBean getDefaultPageInstance() {
        PassCloudResultBean passCloudResultBean = new PassCloudResultBean();
        passCloudResultBean.code = 10000;
        passCloudResultBean.message = "";
        return passCloudResultBean;
    }

    public static PassCloudResultBean getPageInstance(String result) {
        return getPageInstance(result, null);
    }

    /**
     * @param assembleFunction 组装参数接收一个集合json对象
     */
    public static PassCloudResultBean getPageInstance(String result, AssembleFunction<List<?>> assembleFunction) {
        PassCloudResultBean passCloudResultBean;
        //异常返回
        if (StrUtil.isBlank(result)) {
            return RESULT_TIMEOUT;
        }
        passCloudResultBean = JSONObject.parseObject(result, PassCloudResultBean.class);
        if (PAAS_TIMEOUT_CODES.contains(passCloudResultBean.getCode())) {
            return RESULT_TIMEOUT;
        }
        if (null == passCloudResultBean.data) {
            return passCloudResultBean;
        }
        passCloudResultBean.data = PageResultBean.getInstance((JSONObject) passCloudResultBean.data, assembleFunction);
        return passCloudResultBean;
    }

    /**
     * @param assembleFunction 组装参数接收单个对象json字符串
     */
    public static PassCloudResultBean getPageInstanceSingle(String result, AssembleFunction<?> assembleFunction) {
        PassCloudResultBean passCloudResultBean;
        //异常返回
        if (StrUtil.isBlank(result)) {
            return RESULT_TIMEOUT;
        }
        passCloudResultBean = JSONObject.parseObject(result, PassCloudResultBean.class);
        if (PAAS_TIMEOUT_CODES.contains(passCloudResultBean.getCode())) {
            return RESULT_TIMEOUT;
        }
        if (null == passCloudResultBean.data) {
            return passCloudResultBean;
        }
        passCloudResultBean.data =
            PageResultBean.getInstanceSingle((JSONObject) passCloudResultBean.data, assembleFunction);
        return passCloudResultBean;
    }

    public static PassCloudResultBean getDataInstance(String result) {
        return getDataInstance(result, null);
    }

    public static PassCloudResultBean getDataInstance(String result, AssembleFunction<?> assembleFunction) {
        PassCloudResultBean passCloudResultBean;
        //异常返回
        if (StrUtil.isBlank(result)) {
            return RESULT_TIMEOUT;
        }
        passCloudResultBean = JSONObject.parseObject(result, PassCloudResultBean.class);
        if (PAAS_TIMEOUT_CODES.contains(passCloudResultBean.getCode())) {
            return RESULT_TIMEOUT;
        }
        if (assembleFunction != null) {
            if (passCloudResultBean.data instanceof JSONArray) {
                JSONArray dataArray = JSONObject.parseArray(JSONObject.toJSONString(passCloudResultBean.data));
                for (int i = 0, len = dataArray.size(); i < len; i++) {
                    dataArray.set(i, assembleFunction.asssemble(dataArray.getString(i)));
                }
                passCloudResultBean.data = dataArray;
            } else {
                passCloudResultBean.data =
                    assembleFunction.asssemble(JSONObject.toJSONString(passCloudResultBean.data));
            }
        }
        return passCloudResultBean;
    }

    public static PassCloudResultBean getDataInstanceFromPage(String result) {
        return getDataInstanceFromPage(result, null);
    }

    public static PassCloudResultBean getDataInstanceFromPage(String result, AssembleFunction<?> assembleFunction) {
        PassCloudResultBean passCloudResultBean;
        //异常返回
        if (StrUtil.isBlank(result)) {
            return RESULT_TIMEOUT;
        }
        passCloudResultBean = JSONObject.parseObject(result, PassCloudResultBean.class);
        if (PAAS_TIMEOUT_CODES.contains(passCloudResultBean.getCode())) {
            return RESULT_TIMEOUT;
        }
        if (null == passCloudResultBean.data) {
            return passCloudResultBean;
        }
        JSONObject jsonObject = (JSONObject) passCloudResultBean.data;
        JSONArray array = jsonObject.getJSONArray("items");
        if (CollectionUtils.isEmpty(array)) {
            return passCloudResultBean;
        }
        //取第一个元素作为结果
        if (assembleFunction != null) {
            passCloudResultBean.data = assembleFunction.asssemble(array.getString(0));
        } else {
            passCloudResultBean.data = array.get(0);
        }

        return passCloudResultBean;
    }

    public boolean isSuccess() {
        return code == 10000;
    }
}
