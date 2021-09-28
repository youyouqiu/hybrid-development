package com.zw.platform.service.monitoring.impl;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.domain.realTimeVideo.FileUploadForm;
import com.zw.platform.domain.realTimeVideo.FtpBean;
import com.zw.platform.service.monitoring.ForwardVideoService;
import com.zw.platform.service.realTimeVideo.RealTimeVideoService;
import com.zw.platform.service.realTimeVideo.ResourceListService;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import java.util.ArrayList;

@Service
public class ForwardVideoServiceImpl implements ForwardVideoService {
    private static final String LOGIN_USER = "admin";

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private RealTimeVideoService realTimeVideoService;

    @Autowired
    private ResourceListService resourceListService;

    @Override
    public String getForwardedMonitorId(String plateNumber) {
        return vehicleService.getIdByBrand(plateNumber);
    }

    @Override
    public boolean anonymousLogin() {
        UserLdap user = new UserLdap(LOGIN_USER, "", true, true, true, true,
            new ArrayList<SimpleGrantedAuthority>(), null, LOGIN_USER, "", "", LOGIN_USER, "", "");
        user.setId(LdapUtils.newLdapName("uid=admin,ou=organization,dc=zwlbs,dc=com"));
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user, "");
        SecurityContextHolder.getContext().setAuthentication(token);
        return true;
    }

    @Override
    public JsonResultBean sendUploadOrder(String ip, FileUploadForm form) throws Exception {
        form.setExecuteOn(7);
        FtpBean ftpBean = resourceListService.getFtpName();
        form.setFTPServerIp(ftpBean.getHost());
        form.setFTPort(ftpBean.getPort());
        form.setFTPUserName(ftpBean.getUsername());
        String encodedPassword = Base64Utils.encodeToString(ftpBean.getPassword().getBytes());
        form.setFTPassword(encodedPassword);
        String filePath = resourceListService
            .getFTPUrl(form.getVehicleId(), "20" + form.getStartTime(), form.getChannelNumber(), form.getAlarmSign());
        form.setFileUploadPath(filePath);
        JsonResultBean resultBean = resourceListService.sendUploadOrder(form, ip);
        final JSONObject obj = (JSONObject) resultBean.getObj();
        obj.put("filePath", filePath);
        obj.put("ftpHost", ftpBean.getHost());
        return resultBean;
    }

    @Override
    public JsonResultBean getAudioAndVideoParameters(String monitorId) {
        anonymousLogin();
        return realTimeVideoService.getAudioAndVideoParameters(monitorId);
    }

}
