package com.zw.platform.controller.leaderboard;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.domain.leaderboard.MonitorEntity;
import com.zw.platform.service.leaderboard.MonitorService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.vo.monitor.MonitorDetailEntity;
import com.zw.platform.vo.monitor.MonitorListEntity;
import com.zw.platform.vo.monitor.MonitorProcessEntity;
import com.zw.platform.vo.monitor.MonitorQuery;
import com.zw.platform.vo.monitor.MonitorRedisEntity;
import com.zw.platform.vo.monitor.MonitorStatusEntity;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zeromq.ZMQ;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.zw.platform.util.common.RedisQueryUtil.getListToPage;

/***
 @Author gfw
 @Date 2018/10/22 9:15
 @Description 性能监控
 @version 1.0
 **/
@Controller
@RequestMapping("/adas/lb/monitor")
public class MonitorController {

    /**
     * 日志打印类
     */
    private static final Logger log = LogManager.getLogger(MonitorController.class);
    /**
     * 性能监控状态
     */
    private static final String MONITOR_STATUS = "status";
    /**
     * 性能监控列表
     */
    private static final String MONITOR_LIST = "list";
    /**
     * 性能监控详情
     */
    private static final String MONITOR_DETAIL = "detail";
    /**
     * 性能监控默认访问
     */
    private static final String MONITOR_DEFAULT = "default";

    /**
     * 数据库分区
     */
    private static final Integer REDIS_INDEX_11 = 11;

    /**
     * cup状态 0正常 1异常 2异常
     */
    String cpuStatus = "0";

    String cpuNormalLimit = "70";
    String cpuAbnormalLimit = "90";
    String cpuMax = "100";
    /**
     * mem状态 0正常 1异常 2异常
     */
    String memStatus = "0";

    String memNormalLimit = "0.75";
    String memAbnormalLimit = "0.90";
    String memMax = "1";
    /**
     * disk状态 0正常 1异常 2异常
     */
    String diskStatus = "0";

    String diskNormalLimit = "75";
    String diskAbnormalLimit = "85";
    String diskMax = "100";
    /**
     * network状态 0正常 1异常 2异常
     */
    String networkStatus = "0";

    @Value("${adas.monitor.ip}")
    private Integer monitorIp;

    @Autowired
    MonitorService monitorService;
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    /**
     * 性能监控状态
     * @return
     */
    @RequestMapping(value = MONITOR_STATUS, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMonitorStatus() {
        try {
            List<MonitorEntity> all = monitorService.findAll();
            int length = all.size();
            MonitorStatusEntity status = new MonitorStatusEntity();
            BigDecimal networInAll = BigDecimal.ZERO;
            BigDecimal networOutAll = BigDecimal.ZERO;
            StringBuilder cpuStr = new StringBuilder();
            StringBuilder memStr = new StringBuilder();
            StringBuilder diskStr = new StringBuilder();
            if (length > 0) {
                for (MonitorEntity monitorEntity : all) {
                    MonitorRedisEntity redisByIpAddress = findRedisByIpAddress(monitorEntity.getIpAddress());
                    // Cpu使用率
                    BigDecimal cpuDecimal = new BigDecimal(redisByIpAddress.getSystemCpu());
                    // 内存使用率
                    BigDecimal memDecimal = dealMemDecimal(redisByIpAddress);
                    // 硬盘使用率
                    String[] split = redisByIpAddress.getSystemDisk().split("%");
                    BigDecimal diskDecimal = new BigDecimal(split[0] + "");
                    // 网络流入
                    BigDecimal networkIn =
                        new BigDecimal(redisByIpAddress.getNetworkIn() == null ? "1" : redisByIpAddress.getNetworkIn());
                    // 网络流出
                    BigDecimal networkOut = new BigDecimal(
                        redisByIpAddress.getNetworkOut() == null ? "1" : redisByIpAddress.getNetworkOut());
                    cpuStr.append(cpuStatus(cpuDecimal)).append(",");
                    memStr.append(memStatus(memDecimal)).append(",");
                    diskStr.append(diskStatus(diskDecimal)).append(",");
                    networInAll = networInAll.add(networkIn);
                    networOutAll = networOutAll.add(networkOut);
                    taskExecutor.execute(new MessageThread(redisByIpAddress.getIpAddress(), monitorIp));
                }
                status.setCpuStatus(statusJudge(cpuStr.toString()));
                status.setMemStatus(statusJudge(memStr.toString()));
                status.setDiskStatus(statusJudge(diskStr.toString()));
                status.setNetworkStatus(networkStatus);
                status.setNetworkInflow(
                    networInAll.divide(new BigDecimal("" + length), 2, BigDecimal.ROUND_HALF_UP).toString());
                status.setNetworkOutflow(
                    networOutAll.divide(new BigDecimal("" + length), 2, BigDecimal.ROUND_HALF_UP).toString());
            }
            return new JsonResultBean(status);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取状态数据失败:", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取状态数据失败");
        }
    }

    private String statusJudge(String cpuStr) {
        String status = "0";
        if (cpuStr.contains("2")) {
            status = "2";
        } else if (cpuStr.contains("1")) {
            status = "1";
        }
        return status;
    }

    private String diskStatus(BigDecimal diskDecimal) {
        if (new BigDecimal(diskNormalLimit).compareTo(diskDecimal) < 0
            && diskDecimal.compareTo(new BigDecimal(diskAbnormalLimit)) <= 0) {
            return "1";
        }
        if (new BigDecimal(diskAbnormalLimit).compareTo(diskDecimal) < 0
            && diskDecimal.compareTo(new BigDecimal(diskMax)) <= 0) {
            return "2";
        }
        return "0";
    }

    private String memStatus(BigDecimal memDecimal) {
        if (new BigDecimal(memNormalLimit).compareTo(memDecimal) < 0
            && memDecimal.compareTo(new BigDecimal(memAbnormalLimit)) <= 0) {
            return "1";
        }
        if (new BigDecimal(memAbnormalLimit).compareTo(memDecimal) < 0
            && memDecimal.compareTo(new BigDecimal(memMax)) <= 0) {
            return "2";
        }
        return "0";
    }

    private String cpuStatus(BigDecimal cpuDecimal) {
        if (new BigDecimal(cpuNormalLimit).compareTo(cpuDecimal) < 0
            && cpuDecimal.compareTo(new BigDecimal(cpuAbnormalLimit)) <= 0) {
            return "1";
        }
        if (new BigDecimal(cpuAbnormalLimit).compareTo(cpuDecimal) < 0
            && cpuDecimal.compareTo(new BigDecimal(cpuMax)) <= 0) {
            return "2";
        }
        return "0";
    }

    private MonitorRedisEntity findRedisByIpAddress(String ipAddress) {
        String info = RedisHelper.getString(HistoryRedisKeyEnum.IP_ADDRESS.of(ipAddress));
        MonitorRedisEntity monitorRedisEntity = new MonitorRedisEntity();
        if (StringUtils.isNotBlank(info)) {
            String[] split = info.split("::");
            if (split.length != 2) {
                return monitorRedisEntity;
            }
            monitorRedisEntity = JSONObject.parseObject(split[0], MonitorRedisEntity.class);
            if (monitorRedisEntity.getSystemCpu().equals("us,")) {
                monitorRedisEntity.setSystemCpu("0.90");
            }
        }
        return monitorRedisEntity;
    }

    /**
     * 性能监控列表
     * @return
     */
    @RequestMapping(value = MONITOR_LIST, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getMonitorList(MonitorQuery query) {
        try {
            Page<MonitorListEntity> page;
            List<MonitorEntity> all = monitorService.findAll();
            List<MonitorListEntity> monitorList = new ArrayList<>();
            for (MonitorEntity monitorEntity : all) {
                MonitorListEntity entity = new MonitorListEntity();
                entity.setServerName(monitorEntity.getServerName());
                entity.setIpAddress(monitorEntity.getIpAddress());
                entity.setIsOnline(isOnline(monitorEntity.getIpAddress()));
                entity.setSystemName(monitorEntity.getSystemName());
                entity.setSystemWay(monitorEntity.getServerWay());
                MonitorRedisEntity redisByIpAddress = findRedisByIpAddress(monitorEntity.getIpAddress());
                String runtime = dealRuntime(redisByIpAddress.getSystemRunningTime());
                entity.setRunTime(runtime);
                String cpu = cpuStatus(new BigDecimal(redisByIpAddress.getSystemCpu()));
                entity.setCpuStatus(cpu);
                String mem = memStatus(dealMemDecimal(redisByIpAddress));
                entity.setMemStatus(mem);
                String[] split = redisByIpAddress.getSystemDisk().split("%");
                BigDecimal diskDecimal = new BigDecimal(split[0] + "");
                String disk = diskStatus(diskDecimal);
                entity.setDiskStatus(disk);
                if (cpu.equals("2") || mem.equals("2") || disk.equals("2")) {

                    entity.setSystemStatus("2");

                } else if (cpu.equals("1") || mem.equals("1") || disk.equals("1")) {

                    entity.setSystemStatus("1");

                } else {

                    entity.setSystemStatus("0");

                }
                entity.setNetworkStatus(networkStatus);
                monitorList.add(entity);
                taskExecutor.execute(new MessageThread(redisByIpAddress.getIpAddress(), monitorIp));
            }
            int total = monitorList.size();
            // 当前页
            int curPage = query.getPage().intValue();
            // 每页条数
            int pageSize = query.getLimit().intValue();
            // 遍历开始条数
            int start = (curPage - 1) * pageSize;
            // 遍历结束条数
            int end = pageSize > (total - start) ? total : (pageSize * curPage);
            List<MonitorListEntity> list = monitorList.subList(start, end);
            page = getListToPage(list, query, total);
            return new PageGridBean(page, true);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("列表查询失败:", e);
            return new PageGridBean(false);
        }
    }

    private String dealRuntime(String systemRunningTime) {
        String runTime = systemRunningTime.replaceAll("days", "");
        String[] split1 = runTime.split("\\+");
        if (split1[1].contains("m")) {
            return split1[0].trim() + "," + "0" + "," + split1[1].replaceAll("min", "").trim();
        } else {
            String[] split2 = split1[1].split(":");
            return split1[0].trim() + "," + split2[0].trim() + "," + split2[1].trim();
        }
    }

    private BigDecimal dealMemDecimal(MonitorRedisEntity redisByIpAddress) {
        BigDecimal sysMemTotalDecimal = new BigDecimal(redisByIpAddress.getSystemMemTotal());
        BigDecimal sysMemUseDecimal = new BigDecimal(redisByIpAddress.getSystemMemUse());
        return sysMemUseDecimal.divide(sysMemTotalDecimal, 2, BigDecimal.ROUND_HALF_UP);
    }

    private String isOnline(String ipAddress) {
        boolean reachable = false;
        try {
            reachable = InetAddress.getByName(ipAddress).isReachable(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (reachable) {
            return "1";
        } else {
            return "0";
        }
    }

    /**
     * 性能监控详情
     * @param ipAddress
     * @return
     */
    @RequestMapping(value = MONITOR_DETAIL, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMonitorDetail(@RequestParam String ipAddress) {
        try {
            MonitorDetailEntity monitorDetailEntity = new MonitorDetailEntity();
            MonitorEntity monitorEntity = monitorService.findByIp(ipAddress);
            if (null == monitorEntity) {
                return new JsonResultBean(monitorDetailEntity);
            }
            monitorDetailEntity.setServerName(monitorEntity.getServerName());
            monitorDetailEntity.setIpAddress(monitorEntity.getIpAddress());
            monitorDetailEntity.setIsOnline(isOnline(monitorEntity.getIpAddress()));
            monitorDetailEntity.setSystemName(monitorEntity.getSystemName());
            monitorDetailEntity.setSystemWay(monitorEntity.getServerWay());
            MonitorRedisEntity redisByIpAddress = findRedisByIpAddress(ipAddress);
            monitorDetailEntity.setRunTime(dealRuntime(redisByIpAddress.getSystemRunningTime()));
            monitorDetailEntity.setCpuPercent(redisByIpAddress.getSystemCpu());
            monitorDetailEntity.setMemPercent(dealMemDecimal(redisByIpAddress).toString());
            monitorDetailEntity.setDiskPercent(redisByIpAddress.getSystemDisk().split("%")[0]);
            monitorDetailEntity.setMemUse(redisByIpAddress.getSystemMemUse());
            monitorDetailEntity.setMemTotal(redisByIpAddress.getSystemMemTotal());
            String processInfo = redisByIpAddress.getProcessInfo();
            String[] split = processInfo.split(":");
            List<MonitorProcessEntity> list = new ArrayList<>();
            int num = 1;
            int num1 = 1;
            for (String sp : split) {
                String[] split1 = sp.split(",");
                MonitorProcessEntity processEntity = new MonitorProcessEntity();
                String procesName = "";
                String text = split1[0];
                if (text.equals("HRegionServer")) {
                    procesName = "海量数据库服务";
                } else if (text.equals("elasticsearch")) {
                    procesName = "索引数据库服务";
                } else if (text.equals("Ddaemon.name=supervisor")) {
                    procesName = "风险实时计算服务";
                } else if (text.equals("tomcat-clbs/conf")) {
                    procesName = "web平台服务";
                } else if (text.equals("tomcat-f3/conf")) {
                    procesName = "协议解析服务";
                } else if (text.equals("ldap")) {
                    procesName = "目录数据库服务";
                } else if (text.equals("redis-server")) {
                    if (num == 1) {
                        procesName = "主内存数据库";
                    } else {
                        procesName = "从内存数据库";
                    }
                    num++;
                } else if (text.equals("mysql")) {
                    procesName = "平台数据库服务";
                } else if (text.equals("redis-sentinel")) {
                    if (num1 == 1) {
                        procesName = "主内存数据库检测";
                    } else {
                        procesName = "从内存数据库检测";
                    }
                    num1++;
                }
                processEntity.setProcessName(procesName);
                processEntity.setId(split1[1]);
                BigDecimal proCpu = new BigDecimal(split1[2]);
                BigDecimal proCore = new BigDecimal(redisByIpAddress.getCoreNum());
                processEntity.setProcessCpu(proCpu.divide(proCore, 2, BigDecimal.ROUND_HALF_UP).toString());
                processEntity.setProcessMem(split1[3]);
                list.add(processEntity);
            }
            // Cpu使用率
            BigDecimal cpuDecimal = new BigDecimal(redisByIpAddress.getSystemCpu());
            // 内存使用率
            BigDecimal memDecimal = dealMemDecimal(redisByIpAddress);
            // 硬盘使用率
            String[] split2 = redisByIpAddress.getSystemDisk().split("%");
            BigDecimal diskDecimal = new BigDecimal(split2[0] + "");
            monitorDetailEntity.setCpuStatus(cpuStatus(cpuDecimal));
            monitorDetailEntity.setMemStatus(memStatus(memDecimal));
            monitorDetailEntity.setDiskStatus(diskStatus(diskDecimal));
            monitorDetailEntity.setNetworkInflow(redisByIpAddress.getNetworkIn());
            monitorDetailEntity.setNetworkOutflow(redisByIpAddress.getNetworkOut());
            monitorDetailEntity.setNetworkStatus(networkStatus);
            if (monitorDetailEntity.getCpuStatus().equals("2") || monitorDetailEntity.getMemStatus().equals("2")
                || monitorDetailEntity.getDiskStatus().equals("2")) {

                monitorDetailEntity.setSystemStatus("2");

            } else if (monitorDetailEntity.getCpuStatus().equals("1") || monitorDetailEntity.getMemStatus().equals("1")
                || monitorDetailEntity.getDiskStatus().equals("1")) {

                monitorDetailEntity.setSystemStatus("1");

            } else {

                monitorDetailEntity.setSystemStatus("0");

            }
            monitorDetailEntity.setList(list);
            taskExecutor.execute(new MessageThread(ipAddress, monitorIp));
            return new JsonResultBean(monitorDetailEntity);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取详情数据失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取详情数据失败");
        }
    }

    /**
     * 性能监控详情
     * @return
     */
    @RequestMapping(value = MONITOR_DEFAULT, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMonitorDe() {
        HashMap map = new HashMap();
        try {
            map.put("defaultIp", monitorService.findByDefault());
            return new JsonResultBean(map);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取默认ip失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取默认ip失败");
        }
    }

    @AllArgsConstructor
    public static class MessageThread implements Runnable {
        private final String ipAddress;
        private final Integer monitorIp;

        @Override
        public void run() {
            String message = "update";
            String url = "tcp://" + ipAddress + ":" + monitorIp;
            ZMQ.Context context = ZMQ.context(1);
            ZMQ.Socket requester = context.socket(ZMQ.REQ);

            final int timeout = 10_000;
            boolean timeoutSet = false;
            final int maxRetry = 10;
            try {
                requester.connect(url);
                for (int i = 0; i < maxRetry; i++) {
                    requester.send(message.getBytes(), 0);
                    // 使用recv(0)须保证成功设置超时，不然线程可能泄漏
                    if (timeoutSet || requester.setReceiveTimeOut(timeout)) {
                        timeoutSet = true;
                        byte[] reply = requester.recv(0);
                        String receive = new String(reply);
                        log.info("Receive:" + receive);
                        if ("success".equals(receive)) {
                            log.info("更新成功！");
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("消息发送失败", e);
            } finally {
                requester.close();
                context.close();
            }
        }
    }

}
