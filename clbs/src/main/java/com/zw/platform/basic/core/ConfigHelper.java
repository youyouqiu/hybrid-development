package com.zw.platform.basic.core;

import com.zw.platform.util.StrUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.FtpClientUtil;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.jxls.util.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * 参数配置管理
 * @author 张娟
 */
@Component
@Getter
public class ConfigHelper {

    /**
     * 山东货运数据报表
     */
    @Value("${cargo.report.switch:false}")
    public boolean cargoReportSwitch;

    @Value("${ftp.username}")
    private String ftpUserName;

    @Value("${ftp.password}")
    private String ftpPassword;

    @Value("${ftp.host.clbs}")
    private String ftpHostClbs;

    @Value("${ftp.port.clbs}")
    private int ftpPortClbs;

    @Value("${adas.professionalFtpPath}")
    private String professionalFtpPath;

    @Value("${adas.mediaServer}")
    private String mediaServer;

    @Value("${system.ssl.enable}")
    private boolean sslEnabled;

    @Value("${max.number.assignment.monitor:100}")
    private Integer maxNumberAssignmentMonitor;

    @Value("${icoDirection}")
    private String icoDirection;

    @Value("${mediaServer.host}")
    private String videoUrl;

    @Value("${mediaServer.port.websocket.audio}")
    private int audioPort;

    @Value("${mediaServer.port.websocket.video}")
    private int videoPort;

    @Value("${mediaServer.port.websocket.resource}")
    private int resourcePort;

    /**
     * 获取ftp的从业人员资源文件路径
     * @return
     */
    public String getProfessionalUrl(String path) {
        if (StrUtil.isBlank(path)) {
            return "";
        }
        if (sslEnabled) {
            return "/mediaserver" + professionalFtpPath + path;
        }
        return mediaServer + professionalFtpPath + path;
    }

    public byte[] getFileFromFtp(String path) {
        byte[] data = new byte[0];
        if (StrUtil.isBlank(path)) {
            return data;
        }

        String splitStr = mediaServer;
        if (sslEnabled) {
            splitStr = "/mediaserver";
        }
        String fileName = path.split(splitStr)[1];
        InputStream in = null;
        try {
            in = FtpClientUtil.getFileInputStream(ftpUserName, ftpPassword, ftpHostClbs, ftpPortClbs,
                StringUtil.encodingFtpFileName(fileName));
            data = Util.toByteArray(in);
        } catch (Exception e) {
            return data;
        } finally {
            IOUtils.closeQuietly(in);
        }
        return data;
    }

}
