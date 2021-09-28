package com.zw.platform.domain.singleVehicle;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import lombok.Data;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 单车登录小程序修改账号密码实体类
 * @author XK
 */
@Data
public class SingleVehicleAcount implements Serializable {
    private static final long serialVersionUID = -3271184890609806576L;

    /**
     * 车辆密码  数字、字母   最长6个字符
     */
    @NotEmpty(message = "【原密码】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Size(max = 6, message = "【原密码】长度不能超过6！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Pattern(message = "【原密码】包含非法字符！", regexp = "^[a-zA-Z0-9]+$", groups = {ValidGroupAdd.class,
            ValidGroupUpdate.class})
    private String oldVehiclePassword;

    @NotEmpty(message = "【新密码】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Size(max = 6, message = "【新密码】长度不能超过6！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Pattern(message = "【新密码】包含非法字符！", regexp = "^[a-zA-Z0-9]+$", groups = {ValidGroupAdd.class,
            ValidGroupUpdate.class})
    private String newVehiclePassword;

    @NotEmpty(message = "【确认密码】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Size(max = 6, message = "【确认密码】长度不能超过6！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Pattern(message = "【确认密码】包含非法字符！", regexp = "^[a-zA-Z0-9]+$", groups = {ValidGroupAdd.class,
            ValidGroupUpdate.class})
    private String confirmNewVehiclePassword;

}
