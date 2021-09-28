package com.zw.adas.push.mqserver;

import com.alibaba.fastjson.JSON;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.zw.adas.domain.riskManagement.AdasInfo;
import com.zw.adas.push.command.AdasRefreshRiskCombatCommand;
import com.zw.adas.push.common.AdasSimpMessagingTemplateUtil;
import com.zw.adas.push.common.DisruptorExceptionHandler;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.task.AdasGuideTask;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.zeromq.ZContext;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AdasZmqServer {
    private static final Logger logger = LogManager.getLogger(AdasZmqServer.class);

    @Autowired
    private ThreadPoolTaskExecutor longTaskExecutor;

    @Autowired
    AdasGuideTask guideTask;

    @Autowired
    private AdasSimpMessagingTemplateUtil adasSimpMessagingTemplateUtil;

    @Value("${zmqConfig.adas.inproc}")
    private String inproc;

    @Value("${zmqConfig.host.connectUrl}")
    private String connectUrl;

    @Value("${adas.risk.refresh.time}")
    private int refreshTime;

    @Value("${zmqConfig.adas.threadSum}")
    private Integer threadSum;

    @Value("${zmqConfig.identity.receive}")
    private String identity;

    private AdasRefreshRiskCombatCommand refreshRiskCombat;

    private HashedWheelTimer timer;

    @Value("${zmqConfig.adas.ringBufferSize}")
    private int bufSize;

    private Disruptor<AdasMessage> disruptor;

    @PostConstruct
    public void init() {
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            ZContext context = new ZContext(1);
            //使用disruptor来处理zmq的adas消息
            int ringBufferSize = 2;
            //保证传入的size是2的幂次方，不然初始化disruptor会报异常
            while (ringBufferSize <= bufSize) {
                ringBufferSize <<= 1;
            }
            disruptor =
                new Disruptor<>(AdasMessage::new, ringBufferSize, new AdasMessageThreadFactory(), ProducerType.SINGLE,
                    new BlockingWaitStrategy());

            //执行一个定时器,用于多少秒刷新一次风控作战页面,防止风险刷险太频繁 页面假死
            if (refreshTime > 0) {
                timer = new HashedWheelTimer(1L, TimeUnit.SECONDS);
                refreshRiskCombat = new AdasRefreshRiskCombatCommand();
                refreshRiskCombat.setAdasSimpMessagingTemplateUtil(adasSimpMessagingTemplateUtil);
                TimerTask task = new TimerTask() {
                    public void run(Timeout timeout) {
                        refreshRiskCombat.executeRefresh();
                        timer.newTimeout(this, refreshTime, TimeUnit.SECONDS);//结束时候再次注册
                    }
                };
                timer.newTimeout(task, refreshTime, TimeUnit.SECONDS);
            }
            //workerName = identity + System.currentTimeMillis();
            AdasEventHandler[] adasEventHandlers = new AdasEventHandler[threadSum];
            //用threadSum个线程来消费adas风险
            for (int i = 0; i < threadSum; i++) {
                adasEventHandlers[i] = new AdasEventHandler();
            }
            disruptor.handleEventsWithWorkerPool(adasEventHandlers);
            disruptor.setDefaultExceptionHandler(new DisruptorExceptionHandler());
            disruptor.start();
            //检查ftp上车的证据文件是否创建
            if (getAdasVehicleDirFlag()) {
                logger.info("当天的adas证据文件夹没有创建，开始创建证据文件夹！");
                guideTask.createFtpMediaDirectory(false);
            }
            AdasZmqWebReceiver webReceiver =
                new AdasZmqWebReceiver(identity, connectUrl, inproc, context, disruptor, guideTask);
            longTaskExecutor.execute(webReceiver);
        }
    }

    @PreDestroy
    public void close() {
        this.disruptor.shutdown();
    }

    //验证当天最新
    public static boolean getAdasVehicleDirFlag() {
        String time = AdasGuideTask.getNowCurrentDay();
        Boolean adasVehicleDirFlag = SubscibeInfoCache.adasVehicleDirFlagMap.get(time);
        if (adasVehicleDirFlag == null) {
            boolean flag = RedisHelper.getString(HistoryRedisKeyEnum.ADAS_VEHICLE_MEDIADIR_FLAG.of(time)) != null;
            adasVehicleDirFlag = flag;
            SubscibeInfoCache.adasVehicleDirFlagMap.put(time, flag);
        }
        return !adasVehicleDirFlag;
    }

    private static class AdasMessageThreadFactory implements ThreadFactory {
        private final AtomicInteger cnt = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "AdasMessageHandler-" + this.cnt.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }

    //disruptor的处理信息的处理器
    class AdasEventHandler implements WorkHandler<AdasMessage>, EventHandler<AdasMessage> {

        @Override
        public void onEvent(AdasMessage event, long sequence, boolean endOfBatch) {
            onEvent(event);
        }

        @Override
        public void onEvent(AdasMessage event) {
            long time = System.currentTimeMillis();
            List<AdasInfo> adasInfoList = JSON.parseArray(event.message, AdasInfo.class);
            //这里做具体的消费逻辑
            getSoftFenceMsg(adasInfoList);
            long endTime = System.currentTimeMillis() - time;
            if (endTime >= 300) {
                logger.info("处理风险下发9208耗时：{}", endTime);
            }
        }

        private void getSoftFenceMsg(List<AdasInfo> adasInfoList) {
            try {
                if (adasInfoList != null && !adasInfoList.isEmpty()) {
                    //下发9208 每来一条风险 都需要推送
                    adasSimpMessagingTemplateUtil.sendRisk9208(adasInfoList);
                    //推送平台参数设置提醒
                    adasSimpMessagingTemplateUtil.pushPlatformRemind(adasInfoList);
                    //推送报警到app端
                    sendRiskToApp(adasInfoList);
                    //风险数量加1动作
                    refreshRiskCombat.getRiskNumber().add(1);
                }
            } catch (Exception e) {
                logger.error("ADAS message 转换AdasInfo.class异常", e);
            }
        }

        private void sendRiskToApp(List<AdasInfo> adasInfoList) {
            try {
                Set<String> vids = getRiskVids(adasInfoList);
                vids.forEach(vid -> adasSimpMessagingTemplateUtil.sendSecurityRiskToUsers(vid));
            } catch (Exception e) {
                logger.error("推送主动安全报警到app端报错了", e);
            }
        }

        private Set<String> getRiskVids(List<AdasInfo> adasInfoList) {

            Set<String> vids = new HashSet<>();
            for (AdasInfo risk : adasInfoList) {
                vids.add(risk.getVehicleId());
            }
            return vids;
        }
    }

    static class AdasMessage {
        private String message;

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
