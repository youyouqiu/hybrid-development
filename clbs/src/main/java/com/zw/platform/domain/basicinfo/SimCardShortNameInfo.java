package com.zw.platform.domain.basicinfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * sim卡简要信息
 *
 * @author Zhang Yanhui
 * @since 2019/10/16 13:56
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimCardShortNameInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
}
