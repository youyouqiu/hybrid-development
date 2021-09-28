package com.zw.platform.service.monitoring.impl;

import com.alibaba.fastjson.JSON;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.oil.TimeZonePositional;
import com.zw.platform.domain.vas.history.AreaInfo;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * 定时定区域多线程批量查询
 * @author zhouzongbo on 2019/5/7 10:03
 */
public class TimeZoneQuery {

    private static final Logger logger = LogManager.getLogger(TimeZoneQuery.class);

    /**
     * 最大线程数量
     */
    private static final int MAX_THREAD_NUMBER = 2;

    /**
     * 每个线程最小的监控对象ID数量
     */
    private static final int SINGLE_THREAD_MIN_MONITOR_NUM = 50;

    /**
     * 最大数量
     */
    private static final int MAX_MONITOR_NUM = 100;

    /**
     * 完成信号:
     */
    private int threadNum = MAX_THREAD_NUMBER;
    /**
     * 结束信号
     */
    private CountDownLatch doneSignal;
    private Long startTime;
    private Long endTime;

    /**
     * 每批次查询的监控对象ID
     */
    private List<List<byte[]>> averageMonitorIdList;

    /**
     * 暂时使用公共的线程池
     */
    private ThreadPoolTaskExecutor taskExecutor;

    private AreaInfo areaInfo;

    public TimeZoneQuery(Long startTime, Long endTime,
        List<List<byte[]>> averageMonitorIdList, Integer threadNum, ThreadPoolTaskExecutor taskExecutor,
        AreaInfo areaInfo) {
        this.threadNum = threadNum;
        this.doneSignal = new CountDownLatch(threadNum);
        this.startTime = startTime;
        this.endTime = endTime;
        this.averageMonitorIdList = averageMonitorIdList;
        this.taskExecutor = taskExecutor;
        this.areaInfo = areaInfo;
    }

    /**
     * 根据所需的线程数，进行拆分
     * @param monitoryIdByteList 监控对象Byte数组
     * @return 线程数量, 当前最大线程数为10
     */
    public static int averageMonitorId(List<byte[]> monitoryIdByteList, List<List<byte[]>> averageMonitorIdList) {
        int monitorSize = monitoryIdByteList.size();
        int threadNum = 0;
        if (monitorSize > TimeZoneQuery.MAX_MONITOR_NUM) {
            averageMonitorIdList.addAll(StringUtil.averageAssign(monitoryIdByteList, TimeZoneQuery.MAX_THREAD_NUMBER));
            threadNum = MAX_THREAD_NUMBER;
        } else {
            int threadSize = monitorSize / TimeZoneQuery.SINGLE_THREAD_MIN_MONITOR_NUM;
            threadNum = monitorSize % TimeZoneQuery.SINGLE_THREAD_MIN_MONITOR_NUM == 0 ? threadSize : threadSize + 1;
            averageMonitorIdList.addAll(StringUtil.averageAssign(monitoryIdByteList, threadNum));
        }
        return threadNum;
    }

    public List<TimeZonePositional> queryPositionalList() {
        // 组装数据量
        List<TimeZonePositional> resultList = new ArrayList<>();
        List<Future<List<TimeZonePositional>>> submitThreadWork = new LinkedList<>();
        // 批量创建查询任务
        for (int i = 0; i < this.threadNum; i++) {
            submitThreadWork.add(taskExecutor.submit(
                new ThreadWork(doneSignal, startTime, endTime, averageMonitorIdList.get(i), areaInfo)));
        }
        try {
            // 等待完成, 返回数据
            doneSignal.await();
            // 如果查询出来的数据不为空, 则解析返回数据
            if (CollectionUtils.isNotEmpty(submitThreadWork)) {
                for (Future<List<TimeZonePositional>> resultFuture : submitThreadWork) {
                    List<TimeZonePositional> timeZonePositionalList = resultFuture.get();
                    if (CollectionUtils.isNotEmpty(timeZonePositionalList)) {
                        resultList.addAll(timeZonePositionalList);
                    }
                }
            }
        } catch (Exception e) {
            // 异常信息
            logger.error("address查询数据异常", e);
        }

        return resultList;
    }

    /**
     * 线程Callable工作类
     */
    class ThreadWork implements Callable<List<TimeZonePositional>> {
        private CountDownLatch doneSignal;
        private Long startTime;
        private Long endTime;
        private List<byte[]> monitoryIdByteList;
        private AreaInfo areaInfo;

        public ThreadWork(CountDownLatch doneSignal, Long startTime, Long endTime,
            List<byte[]> monitoryIdByteList, AreaInfo areaInfo) {
            this.doneSignal = doneSignal;
            this.startTime = startTime;
            this.endTime = endTime;
            this.monitoryIdByteList = monitoryIdByteList;
            this.areaInfo = areaInfo;
        }

        @Override
        public List<TimeZonePositional> call() throws Exception {
            List<TimeZonePositional> areaOnePositionalList;
            try {
                final Map<String, String> params = new HashMap<>(8);
                params.put("vehicleIds", JSON.toJSONString(UuidUtils.getUUIDStrListFromBytes(monitoryIdByteList)));
                params.put("startTime", String.valueOf(startTime));
                params.put("endTime", String.valueOf(endTime));
                params.put("areaInfo", JSON.toJSONString(areaInfo));
                String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.FIND_POSITIONAL_BY_ADDRESS, params);
                areaOnePositionalList = PaasCloudUrlUtil.getResultListData(str, TimeZonePositional.class);
            } finally {
                // 每完成一个查询, 则减少一个门闩数量,查询完成释放信息
                doneSignal.countDown();
            }
            return areaOnePositionalList;
        }
    }

}
