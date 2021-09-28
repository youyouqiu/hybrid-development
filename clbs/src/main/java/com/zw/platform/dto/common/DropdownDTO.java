package com.zw.platform.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 下拉选
 * @author create by zhouzongbo on 2020/9/30.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DropdownDTO implements Serializable {
    private static final long serialVersionUID = 1426175076226872177L;
    private String id;

    private String name;
}
