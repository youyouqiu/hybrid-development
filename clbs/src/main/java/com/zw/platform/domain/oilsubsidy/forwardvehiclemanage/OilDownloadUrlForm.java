package com.zw.platform.domain.oilsubsidy.forwardvehiclemanage;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Size;
import java.util.Date;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2020/9/30 9:35
 */
@Data
public class OilDownloadUrlForm extends BaseFormBean {

    /**
     * 对接码组织id
     */
    @NotEmpty(message = "【对接码组织】不能为空！")
    private String dockingCodeOrgId;

    /**
     * 809转发平台id
     */
    @NotEmpty(message = "【转发平台】不存在！")
    private String forwardingPlatformId;

    /**
     * 油补平台下载车辆地址
     */
    @NotEmpty(message = "【webservice地址】不能为空！")
    @Size(max = 200, message = "【webservice地址】长度不能超过200！")
    private String url;

    /**
     * 用户名
     */
    @NotEmpty(message = "【用户名】不能为空！")
    @Size(max = 50, message = "【用户名】长度不能超过50！")
    private String userName;

    /**
     * 密码
     */
    @NotEmpty(message = "【密码】不能为空！")
    @Size(max = 50, message = "【密码】长度不能超过50！")
    private String password;

    /**
     * 对接码
     */
    @NotEmpty(message = "【对接码组织】不能为空！")
    @Size(max = 50, message = "【对接码】长度不能超过50！")
    private String dockingCode;

    /**
     * 下载状态（0代表下载失败,1代表下载中2.代表下载成功）
     */
    private Integer downloadStatus;

    /**
     * 下载日期
     */
    private Date downloadTime = new Date();
}
