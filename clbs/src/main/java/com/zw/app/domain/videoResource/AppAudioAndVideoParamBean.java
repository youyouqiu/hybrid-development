package com.zw.app.domain.videoResource;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/12/8 14:48
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AppAudioAndVideoParamBean extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 8344264313661100812L;

    @NotBlank(message = "监控对象id不能为空")
    private String monitorId;
}
