package com.cb.platform.domain;


import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class ItemNameQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = -4741841406532771037L;

    private String id;

    String name;

}
