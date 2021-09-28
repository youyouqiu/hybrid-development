package com.zw.adas.utils.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.ImmutableMap;
import com.zw.platform.basic.constant.RedisKeysConvert;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.ZipUtil;
import com.zw.ws.common.PublicVariable;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.List;

public class AdasControllerTemplate {

    private static final Logger log = LogManager.getLogger(AdasControllerTemplate.class);

    private static final String ERROR_PAGE = "html/errors/error_exception";

    /**
     * 通用返回结果的查询方法
     */
    public static void execute(AdasVoidFunction function, String errMsg) {
        try {
            function.execute();
        } catch (Exception e) {
            log.error(errMsg, e);

        }
    }

    /**
     * 通用返回结果的查询方法
     */
    public static JsonResultBean getResultBean(AdasQueryFunction<?> function, String errMsg) {
        try {
            Object result = function.execute();
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error(errMsg, e);
            return new JsonResultBean(JsonResultBean.FAULT);

        }
    }

    /**
     * 通用返回结果的查询方法
     */
    public static JsonResultBean getPressResultBean(AdasQueryFunction<?> function, String errMsg) {
        try {
            Object result = function.execute();
            return new JsonResultBean(ZipUtil.compress(JSON.toJSONString(result)));
        } catch (Exception e) {
            log.error(errMsg, e);
            return new JsonResultBean(JsonResultBean.FAULT);

        }
    }

    /**
     * 通用返回分页结果的查询方法
     */
    public static <T> PageGridBean getResultBean(AdasQueryFunction<List<T>> function, BaseQueryBean baseQueryBean,
        String errMsg) {
        try {
            Page<T> pageInfo = PageHelper.startPage(baseQueryBean.getPage().intValue(),
                    baseQueryBean.getLimit().intValue()).doSelectPage(function::execute);
            return new PageGridBean(baseQueryBean, pageInfo, JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error(errMsg, e);
            return new PageGridBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 功能描述:redis的分页查询
     * @return * @return : com.zw.platform.util.common.PageGridBean
     * @author zhengjc
     * @date 2019/1/17
     */
    public static PageGridBean getResultBean(BaseQueryBean baseQueryBean, RedisKeysConvert redisKeysConvert,
        String errMsg) {
        return getResultBean(baseQueryBean, redisKeysConvert, errMsg, null);
    }

    public static <T> PageGridBean getResultBean(BaseQueryBean baseQueryBean, RedisKeysConvert redisKeysConvert,
        String errMsg, AdasQueryListFunction<T> queryListFunction) {
        Page<T> results = new Page<>();
        try {
            // redis分页查询
            String userId = SystemHelper.getCurrentUserId();
            RedisKey redisDataKey = redisKeysConvert.of(userId);
            String simpleQueryParam = baseQueryBean.getSimpleQueryParam();
            if (StrUtil.isNotBlank(simpleQueryParam.trim())) {
                List<T> allData = RedisHelper.getListObj(redisDataKey, 0L, -1L);
                redisDataKey = redisKeysConvert.of(userId + "_" + simpleQueryParam);
                //只要是模拟查询，都重新放入缓存中
                setDataListToRedis(queryListFunction.execute(allData, simpleQueryParam), redisDataKey);
            }
            List<T> result = RedisHelper.getListObj(redisDataKey, (baseQueryBean.getStart() + 1),
                (baseQueryBean.getStart() + baseQueryBean.getLimit()));
            if (result != null) {
                results = queryPageList(result, baseQueryBean, redisDataKey);
            }
            return new PageGridBean(baseQueryBean, results, JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error(errMsg, e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    private static <T> Page<T> queryPageList(List<T> returnList, BaseQueryBean query, RedisKey redisKey) {
        Long listSize = RedisHelper.getListLen(redisKey);
        listSize = listSize == null ? 0 : listSize - 1;
        return RedisQueryUtil.getListToPage(returnList, query, listSize.intValue());
    }

    /**
     * 功能描述:缓存数据到redis中，进行分页查询
     * @return * @return : com.zw.platform.util.common.JsonResultBean
     * @author zhengjc
     * @date 2019/1/17
     */
    public static JsonResultBean setDataListToRedis(
            AdasQueryFunction<List<?>> function, RedisKeysConvert redisKeysConvert, String errMsg) {
        try {
            return setDataListToRedis(function.execute(), redisKeysConvert.of(SystemHelper.getCurrentUserId()));
        } catch (Exception e) {
            log.error(errMsg, e);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    private static JsonResultBean setDataListToRedis(List<?> result, RedisKey key) {
        // 再次查询前删除 key
        RedisHelper.delete(key);
        if (CollectionUtils.isEmpty(result)) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        //设置第一个元素为对象的全类名
        String className = result.get(0).getClass().getName();
        RedisHelper.batchAddToList(ImmutableMap.of(key, Collections.singleton(className)));
        // 获取组装数据存入redis管道
        RedisHelper.addToList(key, result);

        //设置过期失效时间，保持6个小时
        RedisHelper.expireKey(key, PublicVariable.REDIS_CACHE_TIMEOUT_DAY * 6);
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 通用新增数据方法
     */
    public static JsonResultBean getResultBean(AdasVoidFunction function, String errMsg) {
        try {
            function.execute();
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error(errMsg, e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    public static ModelAndView editPage(String editPage, AdasQueryFunction<?> function) {
        try {
            ModelAndView mav = new ModelAndView(editPage);
            mav.addObject("result", function.execute());
            return mav;
        } catch (Exception e) {
            return new ModelAndView(ERROR_PAGE);
        }
    }

}
