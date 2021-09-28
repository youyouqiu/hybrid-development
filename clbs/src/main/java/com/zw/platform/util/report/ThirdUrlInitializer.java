package com.zw.platform.util.report;

import com.zw.platform.manager.url.AlarmLinkageUrlEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/12/13 11:21
 */
@Component
public class ThirdUrlInitializer {

    @Value("${db.host}")
    private String passIp;
    @Value("${f3.pass.port}")
    private String passPort;

    @Value("${db.host}")
    private String alarmLinkageIp;

    @Value("${alarm.linkage.project.port}")
    private String alarmLinkagePort;


    @PostConstruct
    public void init() {
        final String reportQueryAddress = address(passIp, passPort) + "/api";
        PaasCloudAdasUrlEnum.assembleUrl(reportQueryAddress);
        PaasCloudUrlEnum.assembleUrl(reportQueryAddress);
        PaasCloudAlarmUrlEnum.assembleUrl(reportQueryAddress);
        PaasCloudHBaseAccessEnum.assembleUrl(reportQueryAddress);
        final String alarmLinkageAddress = address(alarmLinkageIp, alarmLinkagePort);
        AlarmLinkageUrlEnum.assembleUrl(alarmLinkageAddress);
    }

    public static String address(String ip, String port) {
        return "http://" + ip + ":" + port;
    }
}
