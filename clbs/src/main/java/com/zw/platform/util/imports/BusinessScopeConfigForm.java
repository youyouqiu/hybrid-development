package com.zw.platform.util.imports;

import lombok.Data;

import java.util.Collection;

/**
 * @author denghuabing
 * @version V1.0
 * @date 2020/9/29
 **/
@Data
public class BusinessScopeConfigForm {
    private String id;

    private Collection<String> businessScopeIds;

    private String type;
}
