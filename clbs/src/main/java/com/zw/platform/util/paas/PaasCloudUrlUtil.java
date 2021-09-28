package com.zw.platform.util.paas;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import com.zw.platform.dto.paas.PaasCloudPageDataDTO;
import com.zw.platform.dto.paas.PaasCloudResultDTO;
import com.zw.platform.util.common.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/10/21 14:01
 */
public class PaasCloudUrlUtil {
    public static final Integer SUCCESS_CODE = 10000;

    /**
     * HBase sql超时
     */
    private static final Integer HBASE_SQL_TIME_OUT_CODE = 40004;

    /**
     * API未在规定时间内返回数据
     */
    private static final Integer API_HANDLE_TIME_OUT_CODE = 40005;

    public static <T> PaasCloudResultDTO<PaasCloudPageDataDTO<T>> pageResult(String result, Class<T> clazz)
        throws Exception {
        if (StringUtils.isBlank(result)) {
            throw new BusinessException("", "查询超时，请缩小查询范围再试！");
        }
        final PaasCloudResultDTO<PaasCloudPageDataDTO<T>> passCloudResult =
            JSON.parseObject(result, buildType(PaasCloudResultDTO.class, PaasCloudPageDataDTO.class, clazz));
        if (passCloudResult == null) {
            throw new BusinessException("", "查询超时，请缩小查询范围再试！");
        }
        Integer code = passCloudResult.getCode();
        if (!Objects.equals(SUCCESS_CODE, code)) {
            if (Objects.equals(HBASE_SQL_TIME_OUT_CODE, code) || Objects.equals(API_HANDLE_TIME_OUT_CODE, code)) {
                throw new BusinessException("", "查询超时，请缩小查询范围再试！");
            } else {
                throw new RuntimeException("调用PaaSCloud接口异常: " + JSON.toJSONString(passCloudResult));
            }
        }
        return passCloudResult;
    }

    private static Type buildType(Type... types) {
        ParameterizedTypeImpl beforeType = null;
        for (int i = types.length - 1; i > 0; i--) {
            beforeType = new ParameterizedTypeImpl(new Type[] { beforeType == null ? types[i] : beforeType }, null,
                types[i - 1]);
        }
        return beforeType;
    }

    public static <T> T getResultData(String result, Class<T> clazz) throws BusinessException {
        if (StringUtils.isBlank(result)) {
            throw new BusinessException("", "查询超时，请缩小查询范围再试！");
        }
        final PaasCloudResultDTO<T> resultData = JSON.parseObject(result, buildType(PaasCloudResultDTO.class, clazz));
        if (resultData == null) {
            throw new BusinessException("", "查询超时，请缩小查询范围再试！");
        }
        Integer code = resultData.getCode();
        if (!Objects.equals(code, SUCCESS_CODE)) {
            if (Objects.equals(HBASE_SQL_TIME_OUT_CODE, code) || Objects.equals(API_HANDLE_TIME_OUT_CODE, code)) {
                throw new BusinessException("", "查询超时，请缩小查询范围再试！");
            } else {
                throw new RuntimeException("调用PaaSCloud接口异常: " + JSON.toJSONString(resultData));
            }
        }
        return resultData.getData();
    }

    public static <T> @NonNull List<T> getResultListData(String result, Class<T> clazz) throws BusinessException {
        if (StringUtils.isBlank(result)) {
            throw new BusinessException("", "查询超时，请缩小查询范围再试！");
        }
        final PaasCloudResultDTO<List<T>> resultData =
            JSON.parseObject(result, buildType(PaasCloudResultDTO.class, List.class, clazz));
        if (resultData == null) {
            throw new BusinessException("", "查询超时，请缩小查询范围再试！");
        }
        Integer code = resultData.getCode();
        if (!Objects.equals(code, SUCCESS_CODE)) {
            if (Objects.equals(HBASE_SQL_TIME_OUT_CODE, code) || Objects.equals(API_HANDLE_TIME_OUT_CODE, code)) {
                throw new BusinessException("", "查询超时，请缩小查询范围再试！");
            } else {
                throw new RuntimeException("调用PaaSCloud接口异常: " + JSON.toJSONString(resultData));
            }
        }
        final List<T> data = resultData.getData();
        return data == null ? new ArrayList<>() : data;
    }

    public static boolean getSuccess(String result) {
        if (StringUtils.isBlank(result)) {
            return false;
        }
        final PaasCloudResultDTO resultDTO = JSON.parseObject(result, PaasCloudResultDTO.class);
        return Objects.nonNull(resultDTO) && Objects.equals(resultDTO.getCode(), SUCCESS_CODE);
    }
}
