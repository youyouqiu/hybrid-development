package com.zw.lkyw.utils.sendMsgCache;

import com.alibaba.fastjson.JSONObject;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.zw.lkyw.domain.SendMsgDetail;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.github.benmanes.caffeine.cache.RemovalCause.EXPIRED;
import static com.github.benmanes.caffeine.cache.RemovalCause.SIZE;
import static com.zw.lkyw.domain.SendMsgDetail.getKey;

/***
 @Author zhengjc
 @Date 2019/12/31 17:50
 @Description 下发短信缓存
 @version 1.0
 **/
@Component
public class SendMsgCache {

    private static final int failure = 1;
    private static Logger logger = LogManager.getLogger(SendMsgCache.class);
    /**
     * 下发短信真实的缓存
     */
    private Cache<String, SendMsgDetail> msgCache;

    /**
     * 最终存储到hbase的缓存数据
     */
    private Cache<String, SendMsgDetail> storeCache;



    @PostConstruct
    public void initCache() {
        msgCache = builderMsgCache();
        storeCache = builderStoreCache();

    }

    /**
     * 短信下发立即进行数据存储
     * @param sendMsgDetail
     */
    public void putMsgCache(SendMsgDetail sendMsgDetail) {
        String key = sendMsgDetail.getKey();
        msgCache.put(key, sendMsgDetail);

    }

    /**
     * 短信下发立即进行数据存储
     * @param sendMsgDetail
     */
    public void putMsgCache(Map<String, SendMsgDetail> sendMsgDetail) {

        msgCache.putAll(sendMsgDetail);

    }

    /**
     * 短信下发终端离线立即存储
     * @param sendMsgDetail
     */
    public void putStoreCache(SendMsgDetail sendMsgDetail) {

        storeCache.put(sendMsgDetail.getStoreKey(), sendMsgDetail);
    }

    /**
     * 短信下发终端离线立即存储
     * @param sendMsgDetails
     */
    public void putStoreCache(Map<String, SendMsgDetail> sendMsgDetails) {

        storeCache.putAll(sendMsgDetails);
    }

    /**
     * 从下发短信缓存中删除，并删除下发短信的key和在要存储结果缓存中插入一条数据
     * @param monitorId
     * @param serialNumber 下发流水号
     */
    public void changeMsgCache(String monitorId, Integer serialNumber, Integer sendStatus, String failureReason) {
        //0代表下发成功，其他的都是失败
        Integer finalSendStatus = sendStatus.intValue() != 0 ? 1 : 0;
        SendMsgDetail sendMsgDetail = msgCache.getIfPresent(getKey(monitorId, serialNumber));
        if (sendMsgDetail != null) {
            sendMsgDetail.setSendStatus(finalSendStatus);
            sendMsgDetail.setFailureReason(failureReason);
            msgCache.invalidate(sendMsgDetail.getKey());
            //并将最终结果存入到结果缓存中
            storeCache.put(sendMsgDetail.getStoreKey(), sendMsgDetail);
        }
    }

    public void scanCache() {
        msgCache.cleanUp();
        storeCache.cleanUp();
    }

    public Map getSendCacheInfo() {
        Map<String, Map> result = new HashMap<>();
        result.put("消息缓存", msgCache.asMap());
        return result;
    }

    private @NonNull Cache<String, SendMsgDetail> builderMsgCache() {
        return Caffeine.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).removalListener(new MsgCacheListener())
            .build();
    }

    private @NonNull Cache<String, SendMsgDetail> builderStoreCache() {
        return Caffeine.newBuilder().expireAfterWrite(3, TimeUnit.SECONDS).maximumSize(20)
            .removalListener(new StoreCacheListener()).build();
    }

    private class StoreCacheListener implements RemovalListener<String, SendMsgDetail> {

        @Override
        public void onRemoval(@Nullable String s, @Nullable SendMsgDetail sendMsgDetail,
            @NonNull RemovalCause removalCause) {
            if (removalCause.equals(SIZE) || removalCause.equals(EXPIRED)) {
                //当有任意一条记录在被写入缓存之后65秒之内没有被访问到以及大小大于200个则进行批量存储

                List<SendMsgDetail> datas = new ArrayList<>(storeCache.asMap().values());
                Set<String> keys = storeCache.asMap().keySet();
                //失效那一条数据也要保存
                datas.add(sendMsgDetail);
                storeCache.invalidateAll(keys);
                batchStoreSendMsgResult(datas);
            }

        }

        private void batchStoreSendMsgResult(Collection<SendMsgDetail> values) {
            Map<String, String> data = new HashMap<>();
            data.put("msgDetailList", JSONObject.toJSONString(values));
            List<String> sendVehicleAndMsg =
                values.stream().map(e -> e.getMonitorName() + e.getMsgContent()).collect(Collectors.toList());
            String jsonObject = HttpClientUtil.send(PaasCloudUrlEnum.BATCH_SAVE_SEND_MSG_URL, data);
            if (StrUtil.isBlank(jsonObject)) {
                logger.error("存储结果失败了：" + JSONObject.toJSONString(jsonObject) + "条数为:" + values.size());
            }
        }
    }

    private class MsgCacheListener implements RemovalListener<String, SendMsgDetail> {

        @Override
        public void onRemoval(@Nullable String s, @Nullable SendMsgDetail sendMsgDetail,
            @NonNull RemovalCause removalCause) {
            if (RemovalCause.EXPIRED.equals(removalCause)) {
                sendMsgDetail.setSendStatus(failure);
                sendMsgDetail.setFailureReason("终端超时未应答");
                storeCache.put(sendMsgDetail.getStoreKey(), sendMsgDetail);
            }

        }
    }

}
